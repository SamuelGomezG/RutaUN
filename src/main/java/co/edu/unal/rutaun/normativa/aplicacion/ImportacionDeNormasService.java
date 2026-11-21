package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.ConsolidadoValidado;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.Validacion;
import co.edu.unal.rutaun.normativa.dominio.ValidadorDelConsolidado;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoEntity;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntity;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirma una incorporación leída de un archivo JSON: guarda la norma y sus reglas, y crea una
 * versión nueva del consolidado por cada programa del alcance. La operación es atómica por archivo.
 *
 * <p>UC07-HU03-T4 (#96, #130). La norma ya registrada por su identificación se omite, sin validar
 * sus reglas (UC07 paso 4). Los programas se resuelven contra los ya cargados por SOP-03; no se
 * generan identificadores de programa. Antes de guardar, {@link ValidadorDelConsolidado} valida las
 * reglas nuevas en cada programa del alcance y arma la versión resultante con las reglas de la
 * anterior, con el estado que tenían en ella, más las de la norma incorporada; las afectaciones
 * todavía no se aplican al armar la versión (#175). El resultado informa si ningún plan queda
 * desactualizado (UC07 ext. 5a, #135).
 */
@Service
public class ImportacionDeNormasService {

  private static final String ADMINISTRADOR_DE_SEMILLA = "Semilla de desarrollo";

  private final ProgramaRepository programas;
  private final NormaRepository normas;
  private final ReglaRepository reglas;
  private final ConsolidadoRepository consolidados;
  private final VersionDelConsolidadoRepository versiones;
  private final ReglaEnVersionRepository reglasEnVersion;
  private final Clock reloj;

  /** Crea el servicio con los repositorios y el reloj de la aplicación. */
  public ImportacionDeNormasService(
      ProgramaRepository programas,
      NormaRepository normas,
      ReglaRepository reglas,
      ConsolidadoRepository consolidados,
      VersionDelConsolidadoRepository versiones,
      ReglaEnVersionRepository reglasEnVersion,
      Clock reloj) {
    this.programas = ArgumentChecks.obligatorio(programas, "programas");
    this.normas = ArgumentChecks.obligatorio(normas, "normas");
    this.reglas = ArgumentChecks.obligatorio(reglas, "reglas");
    this.consolidados = ArgumentChecks.obligatorio(consolidados, "consolidados");
    this.versiones = ArgumentChecks.obligatorio(versiones, "versiones");
    this.reglasEnVersion = ArgumentChecks.obligatorio(reglasEnVersion, "reglasEnVersion");
    this.reloj = ArgumentChecks.obligatorio(reloj, "reloj");
  }

  /**
   * Importa una norma desde JSON.
   *
   * @param entrada contenido del archivo
   * @return si se creó la norma, cuántas versiones se crearon y si ningún plan queda desactualizado
   *     por ella
   * @throws ArchivoDeNormativaInvalidoException si el JSON o las reglas no pasan la validación
   */
  @Transactional
  public ResultadoDeImportacionDeNormas importar(InputStream entrada) {
    var lectura = ValidadorDeIncorporacion.validar(IncorporacionJsonReader.leer(entrada));
    if (lectura instanceof ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida invalida) {
      throw new ArchivoDeNormativaInvalidoException(String.join(" ", invalida.motivos()));
    }
    var valida = (ValidadorDeIncorporacion.ResultadoDeValidacion.Valida) lectura;
    IncorporacionLeida incorporacion = valida.incorporacion();
    NormaLeida normaLeida = incorporacion.norma();
    String identificacion = identificacion(normaLeida);
    if (normas
        .findByTipoAndNumeroAndAnioAndOrgano(
            normaLeida.identificacion().tipo().name(),
            normaLeida.identificacion().numero(),
            normaLeida.identificacion().anio(),
            normaLeida.identificacion().organo())
        .isPresent()) {
      return new ResultadoDeImportacionDeNormas(false, identificacion, 0, false);
    }

    Alcance alcance = alcance(normaLeida.alcance());
    Norma norma =
        Norma.reconstituir(
            normaLeida.id(),
            normaLeida.identificacion(),
            normaLeida.tipoDeEfecto(),
            normaLeida.fechaExpedicion(),
            normaLeida.vigencia(),
            normaLeida.enlaceRegimenLegal(),
            alcance);
    List<ProgramaEntity> programasDelAlcance = programasDelAlcance(normaLeida.alcance());
    if (programasDelAlcance.isEmpty()) {
      throw new ArchivoDeNormativaInvalidoException(
          "La norma no comprende ningún programa cargado.");
    }

    LocalDate fecha = LocalDate.now(reloj);
    List<VersionPorCrear> porCrear =
        validarEnCadaPrograma(
            programasDelAlcance,
            normaLeida.vigencia(),
            conSuEstado(incorporacion.reglas(), normaLeida, fecha));
    normas.save(NormaMapper.aEntidad(norma));
    reglas.saveAll(incorporacion.reglas().stream().map(ReglaMapper::aEntidad).toList());

    MotivoDeVersion motivo =
        new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR_DE_SEMILLA);
    int creadas = 0;
    for (VersionPorCrear version : porCrear) {
      crearVersion(version, motivo, fecha);
      creadas++;
    }
    return new ResultadoDeImportacionDeNormas(
        true, identificacion, creadas, valida.ningunPlanQuedaDesactualizado());
  }

  private static List<ReglaEnVersion> conSuEstado(
      List<Regla> reglas, NormaLeida norma, LocalDate hoy) {
    return reglas.stream()
        .map(regla -> new ReglaEnVersion(regla, estadoDe(regla, norma, hoy)))
        .toList();
  }

  private static EstadoRegla estadoDe(Regla regla, NormaLeida norma, LocalDate hoy) {
    Vigencia vigencia = regla.vigenciaPropia().orElse(norma.vigencia());
    if (vigencia.vigenteHasta().orElse(null) instanceof Momento.EnFecha hasta
        && hasta.fecha().isBefore(hoy)) {
      return EstadoRegla.YA_NO_RIGE;
    }
    if (vigencia.aplicaDesde() instanceof Momento.EnFecha desde && desde.fecha().isAfter(hoy)) {
      return EstadoRegla.PROGRAMADA;
    }
    return EstadoRegla.VIGENTE;
  }

  private Alcance alcance(AlcanceLeida leida) {
    return new Alcance(
        leida.tipo(),
        leida.facultades(),
        idsDeProgramas(leida.programas()),
        idsDeProgramas(leida.exclusiones()),
        leida.texto());
  }

  private Set<ProgramaId> idsDeProgramas(Set<String> codigos) {
    return codigos.stream()
        .map(this::programaPorCodigo)
        .map(ProgramaEntity::getId)
        .map(ProgramaId::new)
        .collect(Collectors.toSet());
  }

  private List<ProgramaEntity> programasDelAlcance(AlcanceLeida alcance) {
    Set<String> exclusiones = new HashSet<>(alcance.exclusiones());
    List<ProgramaEntity> candidatos =
        switch (alcance.tipo()) {
          case TODOS -> programas.findAll();
          case FACULTADES ->
              programas.findAll().stream()
                  .filter(
                      programa ->
                          alcance.facultades().stream()
                              .anyMatch(
                                  facultad -> facultad.nombre().equals(programa.getFacultad())))
                  .toList();
          case PROGRAMAS ->
              alcance.programas().stream().map(this::programaPorCodigo).distinct().toList();
        };
    return candidatos.stream()
        .filter(programa -> !exclusiones.contains(programa.getCodigoSia()))
        .toList();
  }

  private ProgramaEntity programaPorCodigo(String codigo) {
    return programas
        .findByCodigoSia(codigo)
        .orElseThrow(
            () ->
                new ArchivoDeNormativaInvalidoException(
                    "No hay un programa cargado con el código SIA " + codigo + "."));
  }

  private List<VersionPorCrear> validarEnCadaPrograma(
      List<ProgramaEntity> programasDelAlcance, Vigencia vigencia, List<ReglaEnVersion> nuevas) {
    List<VersionPorCrear> porCrear = new ArrayList<>(programasDelAlcance.size());
    Set<String> motivos = new LinkedHashSet<>();
    for (ProgramaEntity programa : programasDelAlcance) {
      Optional<VersionDelConsolidado> anterior =
          versiones.findTopByProgramaIdOrderByNumeroDesc(programa.getId()).map(this::cargarVersion);
      switch (ValidadorDelConsolidado.validar(
          new ProgramaId(programa.getId()), anterior, vigencia, nuevas)) {
        case Validacion.Valido valido ->
            porCrear.add(new VersionPorCrear(anterior, valido.consolidado()));
        case Validacion.Invalido invalido -> motivos.addAll(invalido.motivos());
      }
    }
    if (!motivos.isEmpty()) {
      throw new ArchivoDeNormativaInvalidoException(String.join(" ", motivos));
    }
    return porCrear;
  }

  private void crearVersion(VersionPorCrear porCrear, MotivoDeVersion motivo, LocalDate fecha) {
    ConsolidadoValidado validado = porCrear.validado();
    ConsolidadoEntity consolidado =
        consolidados
            .findByProgramaId(validado.programa().valor())
            .orElseGet(
                () ->
                    consolidados.save(
                        new ConsolidadoEntity(UUID.randomUUID(), validado.programa().valor())));
    VersionDelConsolidado version =
        porCrear
            .anterior()
            .map(v -> v.siguiente(validado, motivo, fecha))
            .orElseGet(() -> VersionDelConsolidado.primera(validado, motivo, fecha));
    VersionDelConsolidadoMapper.Filas filas =
        VersionDelConsolidadoMapper.aEntidades(version, consolidado.getId());
    versiones.save(filas.version());
    reglasEnVersion.saveAll(filas.reglasEnVersion());
  }

  private record VersionPorCrear(
      Optional<VersionDelConsolidado> anterior, ConsolidadoValidado validado) {}

  private VersionDelConsolidado cargarVersion(VersionEntity entidad) {
    List<co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity> filas =
        reglasEnVersion.findByVersionId(entidad.getId());
    List<UUID> ids = filas.stream().map(fila -> fila.getReglaId()).toList();
    Map<UUID, co.edu.unal.rutaun.normativa.persistencia.ReglaEntity> reglasPorId =
        reglas.findAllById(ids).stream()
            .collect(Collectors.toMap(regla -> regla.getId(), regla -> regla));
    return VersionDelConsolidadoMapper.aDominio(entidad, filas, reglasPorId);
  }

  private static String identificacion(NormaLeida norma) {
    return norma.identificacion().tipo()
        + " "
        + norma.identificacion().numero()
        + " de "
        + norma.identificacion().anio()
        + ", "
        + norma.identificacion().organo();
  }
}
