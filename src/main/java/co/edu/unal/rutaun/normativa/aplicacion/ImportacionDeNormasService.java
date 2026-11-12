package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.ConsolidadoValidado;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.VersionJpaRepository;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
 * <p>UC07-HU03-T4 (#96, #130). La norma ya registrada por su identificación se omite. Los programas
 * se resuelven contra los ya cargados por SOP-03; no se generan identificadores de programa. La
 * versión nueva del consolidado lleva las reglas de la versión anterior del programa, con el estado
 * que tenían en ella, más las de la norma incorporada validadas por {@link
 * ValidadorDeIncorporacion}; las afectaciones todavía no se aplican al armar la versión (#175). El
 * resultado informa si ningún plan queda desactualizado (UC07 ext. 5a, #135).
 */
@Service
public class ImportacionDeNormasService {

  private static final String ADMINISTRADOR_DE_SEMILLA = "Semilla de desarrollo";

  private final ProgramaJpaRepository programas;
  private final NormaJpaRepository normas;
  private final ReglaJpaRepository reglas;
  private final ConsolidadoJpaRepository consolidados;
  private final VersionJpaRepository versiones;
  private final ReglaEnVersionJpaRepository reglasEnVersion;
  private final Clock reloj;

  /** Crea el servicio con los repositorios y el reloj de la aplicación. */
  public ImportacionDeNormasService(
      ProgramaJpaRepository programas,
      NormaJpaRepository normas,
      ReglaJpaRepository reglas,
      ConsolidadoJpaRepository consolidados,
      VersionJpaRepository versiones,
      ReglaEnVersionJpaRepository reglasEnVersion,
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
    List<ProgramaEntidad> programasDelAlcance = programasDelAlcance(normaLeida.alcance());
    if (programasDelAlcance.isEmpty()) {
      throw new ArchivoDeNormativaInvalidoException(
          "La norma no comprende ningún programa cargado.");
    }

    LocalDate fecha = LocalDate.now(reloj);
    List<Regla> reglasConEstado = conEstado(incorporacion.reglas(), normaLeida, fecha);
    normas.save(NormaMapper.aEntidad(norma));
    reglas.saveAll(reglasConEstado.stream().map(ReglaMapper::aEntidad).toList());

    MotivoDeVersion motivo =
        new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR_DE_SEMILLA);
    int creadas = 0;
    for (ProgramaEntidad programa : programasDelAlcance) {
      crearVersion(programa, reglasConEstado, motivo, fecha);
      creadas++;
    }
    return new ResultadoDeImportacionDeNormas(
        true, identificacion, creadas, valida.ningunPlanQuedaDesactualizado());
  }

  private static List<Regla> conEstado(List<Regla> reglas, NormaLeida norma, LocalDate hoy) {
    return reglas.stream()
        .map(
            regla ->
                Regla.reconstituir(
                    regla.id(),
                    regla.objeto(),
                    regla.enunciado(),
                    regla.fuente(),
                    regla.vigenciaPropia(),
                    regla.clasificacion(),
                    estadoDe(regla, norma, hoy),
                    regla.afectacion(),
                    regla.datos()))
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
        .map(ProgramaEntidad::getId)
        .map(ProgramaId::new)
        .collect(Collectors.toSet());
  }

  private List<ProgramaEntidad> programasDelAlcance(AlcanceLeida alcance) {
    Set<String> exclusiones = new HashSet<>(alcance.exclusiones());
    List<ProgramaEntidad> candidatos =
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

  private ProgramaEntidad programaPorCodigo(String codigo) {
    return programas
        .findByCodigoSia(codigo)
        .orElseThrow(
            () ->
                new ArchivoDeNormativaInvalidoException(
                    "No hay un programa cargado con el código SIA " + codigo + "."));
  }

  private void crearVersion(
      ProgramaEntidad programa, List<Regla> reglas, MotivoDeVersion motivo, LocalDate fecha) {
    ConsolidadoEntidad consolidado =
        consolidados
            .findByProgramaId(programa.getId())
            .orElseGet(
                () ->
                    consolidados.save(new ConsolidadoEntidad(UUID.randomUUID(), programa.getId())));
    Optional<VersionDelConsolidado> anterior =
        versiones.findTopByProgramaIdOrderByNumeroDesc(programa.getId()).map(this::cargarVersion);
    List<Regla> resultantes =
        reglasAcumuladas(anterior.map(VersionDelConsolidado::reglas).orElse(List.of()), reglas);
    ConsolidadoValidado validado =
        ConsolidadoValidado.paraImportacion(new ProgramaId(programa.getId()), resultantes);
    VersionDelConsolidado version =
        anterior
            .map(v -> v.siguiente(validado, motivo, fecha))
            .orElseGet(() -> VersionDelConsolidado.primera(validado, motivo, fecha));
    VersionDelConsolidadoMapper.Filas filas =
        VersionDelConsolidadoMapper.aEntidades(version, consolidado.getId());
    versiones.save(filas.version());
    reglasEnVersion.saveAll(filas.reglasEnVersion());
  }

  /**
   * Las reglas de la versión siguiente: las de la anterior, con el estado que tenían en ella, más
   * las nuevas. Todavía no aplica afectaciones (#175). R-33 (#154) lleva esta unión al validador
   * del consolidado.
   *
   * <p>El rechazo compara {@link ReglaId}. El lector crea cada regla con {@link Regla#registrar},
   * que genera un id nuevo, así que hoy no se puede provocar desde un archivo: la comprobación
   * defiende a los llamadores programáticos y al futuro validador del consolidado.
   *
   * @throws ArchivoDeNormativaInvalidoException si una regla nueva ya está en la versión anterior o
   *     está repetida entre las nuevas
   */
  static List<Regla> reglasAcumuladas(List<Regla> anteriores, List<Regla> nuevas) {
    Set<ReglaId> idsDeLasAnteriores =
        anteriores.stream().map(Regla::id).collect(Collectors.toSet());
    Set<ReglaId> idsDeLasNuevas = new HashSet<>();
    for (Regla regla : nuevas) {
      boolean yaEnLaAnterior = idsDeLasAnteriores.contains(regla.id());
      boolean yaEntreLasNuevas = !idsDeLasNuevas.add(regla.id());
      if (yaEnLaAnterior || yaEntreLasNuevas) {
        throw new ArchivoDeNormativaInvalidoException(
            "La regla \""
                + regla.objeto()
                + "\" ("
                + regla.fuente().articulo()
                + ") "
                + (yaEnLaAnterior
                    ? "ya está en la versión anterior del consolidado."
                    : "está repetida en la norma que se importa."));
      }
    }
    List<Regla> resultantes = new ArrayList<>(anteriores.size() + nuevas.size());
    resultantes.addAll(anteriores);
    resultantes.addAll(nuevas);
    return resultantes;
  }

  private VersionDelConsolidado cargarVersion(VersionEntidad entidad) {
    List<co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntidad> filas =
        reglasEnVersion.findByVersionId(entidad.getId());
    List<UUID> ids = filas.stream().map(fila -> fila.getReglaId()).toList();
    Map<UUID, co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad> reglasPorId =
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
