package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.EslabonVista;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaCompletaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeCambio;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.AlcanceJson;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntity;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la fachada de consulta de Normativa (UC06-HU01-T4, #132): lee el consolidado
 * vigente de un programa y lo entrega como vistas, sin exponer el dominio ni las filas.
 *
 * <p>El consolidado vigente es la versión de mayor número del programa. Muestra las reglas
 * vigentes, programadas y en revisión, y deja fuera las que ya no rigen. Las agrupa por categoría
 * según el glosario («Regla») y, dentro de cada una, las ordena por fecha de expedición de su
 * norma, artículo, enunciado e identificador.
 *
 * <p>La regla completa sí abre las que ya no rigen, con ese estado: son las que la cadena de normas
 * explica.
 *
 * <p>Es solo de lectura: no publica eventos ni guarda nada. El incidente de un consolidado sin
 * reglas lo publica el orquestador de UC06.
 */
@Service
@Transactional(readOnly = true)
public class ConsultaNormativaService implements ConsultaNormativaFacade {

  static final String COMPONENTES_Y_CREDITOS = "Componentes y créditos";
  static final String MATERIAS = "Materias";
  static final String AGRUPACIONES = "Agrupaciones";
  static final String PRERREQUISITOS_Y_HABILITACION = "Prerrequisitos y habilitación";
  static final String EQUIVALENCIAS = "Equivalencias";
  static final String LIBRE_ELECCION = "Libre elección";
  static final String CARGA_POR_PERIODO = "Carga mínima o máxima por periodo";
  static final String REQUISITOS_DE_GRADO = "Requisitos de grado";
  static final String OTRA = "Otra";

  /** Las categorías en el orden del glosario. */
  private static final List<String> ORDEN_DE_CATEGORIAS =
      List.of(
          COMPONENTES_Y_CREDITOS,
          MATERIAS,
          AGRUPACIONES,
          PRERREQUISITOS_Y_HABILITACION,
          EQUIVALENCIAS,
          LIBRE_ELECCION,
          CARGA_POR_PERIODO,
          REQUISITOS_DE_GRADO,
          OTRA);

  /** Una regla lista para mostrar, con lo que hace falta para ordenarla. */
  record Entrada(
      String categoria, LocalDate expedicion, String articulo, UUID id, ReglaVista vista) {}

  /** Orden del español, que no separa las letras con tilde. */
  private static final Collator ORDEN_DE_TEXTO = Collator.getInstance(Locale.forLanguageTag("es"));

  /**
   * Orden de las reglas de una categoría: fecha de expedición de la norma (la más antigua primero;
   * sin norma, al final), artículo (por su número), enunciado e identificador.
   */
  static final Comparator<Entrada> ORDEN_DE_REGLAS =
      Comparator.comparing(Entrada::expedicion, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(Entrada::articulo, OrdenDeArticulos.INSTANCIA)
          .thenComparing(entrada -> entrada.vista().enunciado(), ORDEN_DE_TEXTO)
          .thenComparing(Entrada::id);

  private final ProgramaRepository programas;
  private final VersionDelConsolidadoRepository versiones;
  private final ReglaEnVersionRepository reglasEnVersion;
  private final ReglaRepository reglas;
  private final NormaRepository normas;

  /** Crea el servicio con los repositorios de los que lee. */
  public ConsultaNormativaService(
      ProgramaRepository programas,
      VersionDelConsolidadoRepository versiones,
      ReglaEnVersionRepository reglasEnVersion,
      ReglaRepository reglas,
      NormaRepository normas) {
    this.programas = ArgumentChecks.obligatorio(programas, "programas");
    this.versiones = ArgumentChecks.obligatorio(versiones, "versiones");
    this.reglasEnVersion = ArgumentChecks.obligatorio(reglasEnVersion, "reglasEnVersion");
    this.reglas = ArgumentChecks.obligatorio(reglas, "reglas");
    this.normas = ArgumentChecks.obligatorio(normas, "normas");
  }

  @Override
  public List<ProgramaVista> programas() {
    return programas.findAll().stream()
        .filter(programa -> versiones.existsByProgramaId(programa.getId()))
        .map(ConsultaNormativaService::aVista)
        .sorted(Comparator.comparing(ProgramaVista::nombre, ORDEN_DE_TEXTO))
        .toList();
  }

  @Override
  public Optional<ProgramaVista> programa(ProgramaId id) {
    ArgumentChecks.obligatorio(id, "id");
    return programas.findById(id.valor()).map(ConsultaNormativaService::aVista);
  }

  @Override
  public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
    ArgumentChecks.obligatorio(id, "id");
    return versiones
        .findTopByProgramaIdOrderByNumeroDesc(id.valor())
        .map(version -> aConsolidado(id, version));
  }

  @Override
  public Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId id) {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorio(id, "id");
    Optional<VersionEntity> version =
        versiones.findTopByProgramaIdOrderByNumeroDesc(programa.valor());
    Optional<ReglaEntity> encontrada = reglas.findById(id.valor());
    if (version.isEmpty() || encontrada.isEmpty()) {
      return Optional.empty();
    }
    ReglaEntity fila = encontrada.get();
    List<ReglaEntity> linaje = linajeDeLaRegla(fila);

    Map<UUID, EstadoRegla> estados = new HashMap<>();
    for (ReglaEnVersionEntity enVersion :
        reglasEnVersion.findByVersionIdAndReglaIdIn(
            version.get().getId(), linaje.stream().map(ReglaEntity::getId).toList())) {
      estados.put(enVersion.getReglaId(), EstadoRegla.valueOf(enVersion.getEstado()));
    }
    EstadoRegla estado = estados.get(fila.getId());
    if (estado == null) {
      return Optional.empty();
    }
    List<ReglaEntity> deLaVersion =
        linaje.stream().filter(regla -> estados.containsKey(regla.getId())).toList();

    Map<UUID, NormaEntity> normasPorId =
        normas
            .findAllById(deLaVersion.stream().map(ReglaEntity::getNormaId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(NormaEntity::getId, norma -> norma));

    Regla regla = ReglaMapper.aDominio(fila);
    NormaEntity norma = normasPorId.get(fila.getNormaId());
    return Optional.of(
        new ReglaCompletaVista(
            regla.id(),
            regla.enunciado(),
            Optional.ofNullable(norma).map(n -> aFuente(n, regla.fuente().articulo())),
            estado,
            esTemporal(regla.vigenciaPropia(), norma == null ? null : norma.getVigenteHasta()),
            regla.clasificacion(),
            Optional.ofNullable(fila.getVigenteDesde())
                .or(() -> Optional.ofNullable(norma).map(NormaEntity::getAplicaDesde)),
            Optional.ofNullable(fila.getVigenteHasta())
                .or(() -> Optional.ofNullable(norma).map(NormaEntity::getVigenteHasta)),
            Optional.ofNullable(norma).map(n -> TipoAlcance.valueOf(n.getTipoAlcance())),
            Optional.ofNullable(norma)
                .map(NormaEntity::getAlcanceDetalle)
                .flatMap(ConsultaNormativaService::textoDelAlcance),
            cadenaDeNormas(deLaVersion, estados, normasPorId)));
  }

  /**
   * La regla y todas las que la rodean por afectaciones: las que ella afecta, hacia arriba hasta la
   * que la expidió, y todas las que afectan a cada una, hacia abajo. Así la cadena es la misma se
   * abra la regla que se abra: la original, la que la modifica o una modificatoria de la
   * modificatoria.
   */
  private List<ReglaEntity> linajeDeLaRegla(ReglaEntity fila) {
    Map<UUID, ReglaEntity> linaje = new LinkedHashMap<>();
    linaje.put(fila.getId(), fila);
    UUID idDeLaAfectada = fila.getReglaAfectadaId();
    while (idDeLaAfectada != null && !linaje.containsKey(idDeLaAfectada)) {
      Optional<ReglaEntity> afectada = reglas.findById(idDeLaAfectada);
      if (afectada.isEmpty()) {
        break;
      }
      linaje.put(idDeLaAfectada, afectada.get());
      idDeLaAfectada = afectada.get().getReglaAfectadaId();
    }
    Deque<ReglaEntity> pendientes = new ArrayDeque<>(linaje.values());
    while (!pendientes.isEmpty()) {
      for (ReglaEntity afectante : reglas.findByReglaAfectadaId(pendientes.poll().getId())) {
        if (linaje.putIfAbsent(afectante.getId(), afectante) == null) {
          pendientes.add(afectante);
        }
      }
    }
    return new ArrayList<>(linaje.values());
  }

  /**
   * El texto con el que la norma fija su alcance. La columna guarda un JSON ({@link AlcanceJson});
   * si no trae texto, no hay nada que mostrar.
   */
  private static Optional<String> textoDelAlcance(AlcanceJson detalle) {
    return Optional.ofNullable(detalle.texto()).filter(texto -> !texto.isBlank());
  }

  /**
   * Las normas de la regla y de las que la rodean por afectaciones (ver {@link #linajeDeLaRegla}),
   * de la más antigua a la más reciente (UC06 ext. 8a). Con la misma fecha, primero la que expidió
   * la regla y luego por artículo. Marca como «rige» la regla que está vigente en el consolidado;
   * deja fuera las reglas que no están en la versión del programa y las que no tienen norma.
   */
  private static List<EslabonVista> cadenaDeNormas(
      List<ReglaEntity> deLaVersion,
      Map<UUID, EstadoRegla> estados,
      Map<UUID, NormaEntity> normasPorId) {
    List<EslabonVista> cadena = new ArrayList<>();
    for (ReglaEntity regla : deLaVersion) {
      NormaEntity norma = normasPorId.get(regla.getNormaId());
      if (norma != null) {
        cadena.add(aEslabon(regla, norma, estados));
      }
    }
    cadena.sort(
        Comparator.comparing(EslabonVista::fechaExpedicion)
            .thenComparing(eslabon -> eslabon.cambio().isPresent())
            .thenComparing(eslabon -> eslabon.fuente().articulo(), OrdenDeArticulos.INSTANCIA));
    return cadena;
  }

  private static EslabonVista aEslabon(
      ReglaEntity fila, NormaEntity norma, Map<UUID, EstadoRegla> estados) {
    Optional<TipoDeCambio> cambio =
        fila.getReglaAfectadaId() == null
            ? Optional.empty()
            : Optional.ofNullable(fila.getTipoAfectacion()).map(TipoDeCambio::valueOf);
    return new EslabonVista(
        cambio,
        aFuente(norma, fila.getFuente()),
        norma.getFechaExpedicion(),
        estados.get(fila.getId()) == EstadoRegla.VIGENTE);
  }

  private ConsolidadoVista aConsolidado(ProgramaId programa, VersionEntity version) {
    Map<UUID, EstadoRegla> estados = new HashMap<>();
    for (ReglaEnVersionEntity fila : reglasEnVersion.findByVersionId(version.getId())) {
      EstadoRegla estado = EstadoRegla.valueOf(fila.getEstado());
      if (seMuestra(estado)) {
        estados.put(fila.getReglaId(), estado);
      }
    }
    List<ReglaEntity> filas = reglas.findAllById(estados.keySet());
    Map<UUID, NormaEntity> normasPorId =
        normas.findAllById(filas.stream().map(ReglaEntity::getNormaId).distinct().toList()).stream()
            .collect(Collectors.toMap(NormaEntity::getId, norma -> norma));

    List<Entrada> entradas = new ArrayList<>();
    for (ReglaEntity fila : filas) {
      entradas.add(aEntrada(fila, estados.get(fila.getId()), normasPorId.get(fila.getNormaId())));
    }
    entradas.sort(ORDEN_DE_REGLAS);

    Map<String, List<ReglaVista>> porCategoria = new LinkedHashMap<>();
    for (String categoria : ORDEN_DE_CATEGORIAS) {
      List<ReglaVista> deLaCategoria =
          entradas.stream()
              .filter(entrada -> entrada.categoria().equals(categoria))
              .map(Entrada::vista)
              .toList();
      if (!deLaCategoria.isEmpty()) {
        porCategoria.put(categoria, deLaCategoria);
      }
    }
    return new ConsolidadoVista(programa, version.getNumero(), version.getFecha(), porCategoria);
  }

  private static Entrada aEntrada(ReglaEntity fila, EstadoRegla estado, NormaEntity norma) {
    Regla regla = ReglaMapper.aDominio(fila);
    boolean temporal =
        esTemporal(regla.vigenciaPropia(), norma == null ? null : norma.getVigenteHasta());
    ReglaVista vista =
        new ReglaVista(
            regla.id(),
            regla.enunciado(),
            Optional.ofNullable(norma).map(n -> aFuente(n, regla.fuente().articulo())),
            estado,
            temporal,
            regla.clasificacion());
    return new Entrada(
        categoria(regla.datos()),
        norma == null ? null : norma.getFechaExpedicion(),
        regla.fuente().articulo(),
        fila.getId(),
        vista);
  }

  /**
   * Si la consulta muestra una regla en este estado. El {@code switch} es exhaustivo: si se agrega
   * un estado, no compila hasta decidir si se muestra.
   */
  static boolean seMuestra(EstadoRegla estado) {
    return switch (estado) {
      case VIGENTE, PROGRAMADA, EN_REVISION -> true;
      case YA_NO_RIGE -> false;
    };
  }

  /**
   * Si la regla es temporal: su vigencia propia tiene un «vigente hasta», o lo tiene su norma
   * (glosario, «Vigencia»).
   *
   * @param vigenciaPropia vigencia de la regla, si tiene una propia
   * @param vigenteHastaDeLaNorma fecha hasta la que rige la norma; {@code null} si no la tiene o la
   *     regla no tiene norma
   */
  static boolean esTemporal(Optional<Vigencia> vigenciaPropia, LocalDate vigenteHastaDeLaNorma) {
    return vigenciaPropia.map(Vigencia::esTemporal).orElse(false) || vigenteHastaDeLaNorma != null;
  }

  private static FuenteVista aFuente(NormaEntity norma, String articulo) {
    return new FuenteVista(
        new NormaVista(
            TipoNorma.valueOf(norma.getTipo()),
            norma.getNumero(),
            norma.getAnio(),
            norma.getOrgano()),
        articulo,
        Optional.ofNullable(norma.getEnlaceRegimenLegal()));
  }

  /**
   * La categoría de una regla según el glosario. El {@code switch} es exhaustivo sobre los datos:
   * si se agrega una clase de datos, no compila hasta decidir su categoría.
   */
  static String categoria(DatosDeRegla datos) {
    return switch (datos) {
      case DatosDeRegla.DePrograma d -> COMPONENTES_Y_CREDITOS;
      case DatosDeRegla.DeComponente d ->
          d.componente() instanceof ComponenteDeLibreEleccion
              ? LIBRE_ELECCION
              : COMPONENTES_Y_CREDITOS;
      case DatosDeRegla.DeMateria d -> MATERIAS;
      case DatosDeRegla.DeAgrupacion d -> AGRUPACIONES;
      case DatosDeRegla.DePrerrequisito d -> PRERREQUISITOS_Y_HABILITACION;
      case DatosDeRegla.DeCorrequisito d -> PRERREQUISITOS_Y_HABILITACION;
      case DatosDeRegla.DeHabilitacion d -> PRERREQUISITOS_Y_HABILITACION;
      case DatosDeRegla.DeEquivalencia d -> EQUIVALENCIAS;
      case DatosDeRegla.DeCargaPorPeriodo d -> CARGA_POR_PERIODO;
      case DatosDeRegla.DeRequisitoDeGrado d -> REQUISITOS_DE_GRADO;
      case DatosDeRegla.DeOtraCategoria d -> OTRA;
    };
  }

  private static ProgramaVista aVista(ProgramaEntity programa) {
    return new ProgramaVista(
        new ProgramaId(programa.getId()),
        programa.getNombre(),
        programa.getCodigoSia(),
        programa.getFacultad());
  }
}
