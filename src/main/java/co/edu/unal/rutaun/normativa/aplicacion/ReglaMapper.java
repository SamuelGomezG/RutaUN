package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.TipoDeAfectacion;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Optional;
import tools.jackson.core.JacksonException;

/**
 * Convierte entre la {@link Regla} del dominio y su fila {@link ReglaEntidad}. No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link Regla#reconstituir}, así que conserva el identificador
 * y pasa por las mismas validaciones que una regla creada. Si la base trae un dato inválido, sube
 * la {@link IllegalArgumentException} del dominio.
 *
 * <p>El estado de la regla no está en la fila de {@code regla} sino en {@code regla_en_version},
 * porque una misma regla tiene un estado distinto en cada versión del consolidado. Por eso {@link
 * #aEntidad} no lo guarda y {@link #aDominio} lo recibe: quien lee una versión lo toma de la fila
 * de esa versión (#152, lo saca de {@code Regla}).
 *
 * <p>La tabla no puede guardar todo lo que el dominio admite, y el mapper lo rechaza en vez de
 * inventar datos: una vigencia propia dada por periodos (#141), una afectación a un artículo y una
 * afectación con un momento «desde» (#141). La categoría va en su columna y los datos propios de la
 * categoría, como JSON (provisional hasta #145; ver {@link JsonDeNormativa}).
 */
public final class ReglaMapper {

  /** Las categorías de regla, con el nombre que se guarda y la clase de sus datos. */
  private enum Categoria {
    MATERIA(DatosDeRegla.DeMateria.class),
    PRERREQUISITO(DatosDeRegla.DePrerrequisito.class),
    CORREQUISITO(DatosDeRegla.DeCorrequisito.class),
    HABILITACION(DatosDeRegla.DeHabilitacion.class),
    AGRUPACION(DatosDeRegla.DeAgrupacion.class),
    COMPONENTE(DatosDeRegla.DeComponente.class),
    PROGRAMA(DatosDeRegla.DePrograma.class),
    EQUIVALENCIA(DatosDeRegla.DeEquivalencia.class),
    CARGA_POR_PERIODO(DatosDeRegla.DeCargaPorPeriodo.class),
    REQUISITO_DE_GRADO(DatosDeRegla.DeRequisitoDeGrado.class),
    OTRA(DatosDeRegla.DeOtraCategoria.class);

    private final Class<? extends DatosDeRegla> clase;

    Categoria(Class<? extends DatosDeRegla> clase) {
      this.clase = clase;
    }

    /** Un {@code switch} exhaustivo: el compilador avisa si se agrega una categoría de datos. */
    static Categoria de(DatosDeRegla datos) {
      return switch (datos) {
        case DatosDeRegla.DeMateria ignored -> MATERIA;
        case DatosDeRegla.DePrerrequisito ignored -> PRERREQUISITO;
        case DatosDeRegla.DeCorrequisito ignored -> CORREQUISITO;
        case DatosDeRegla.DeHabilitacion ignored -> HABILITACION;
        case DatosDeRegla.DeAgrupacion ignored -> AGRUPACION;
        case DatosDeRegla.DeComponente ignored -> COMPONENTE;
        case DatosDeRegla.DePrograma ignored -> PROGRAMA;
        case DatosDeRegla.DeEquivalencia ignored -> EQUIVALENCIA;
        case DatosDeRegla.DeCargaPorPeriodo ignored -> CARGA_POR_PERIODO;
        case DatosDeRegla.DeRequisitoDeGrado ignored -> REQUISITO_DE_GRADO;
        case DatosDeRegla.DeOtraCategoria ignored -> OTRA;
      };
    }

    static Categoria desde(String nombre) {
      ArgumentChecks.obligatorioNoVacio(nombre, "categoria");
      for (Categoria categoria : values()) {
        if (categoria.name().equals(nombre)) {
          return categoria;
        }
      }
      throw new IllegalArgumentException("categoría de regla desconocida: " + nombre);
    }
  }

  private ReglaMapper() {}

  /**
   * Reconstruye la regla que guarda esta fila, con el estado que tiene en la versión que la lee.
   *
   * @throws IllegalArgumentException si la fila trae un dato inválido o una categoría desconocida
   */
  public static Regla aDominio(ReglaEntidad entidad, EstadoRegla estado) {
    ArgumentChecks.obligatorio(entidad, "entidad");
    ArgumentChecks.obligatorio(estado, "estado");
    return Regla.reconstituir(
        new ReglaId(entidad.getId()),
        entidad.getObjeto(),
        entidad.getEnunciado(),
        new Fuente(new NormaId(entidad.getNormaId()), entidad.getFuente()),
        vigenciaADominio(entidad),
        ClasificacionRegla.valueOf(entidad.getClasificacion()),
        estado,
        afectacionADominio(entidad),
        datosADominio(entidad));
  }

  /**
   * Convierte la regla en la fila que se guarda en la base. No guarda el estado: va en {@code
   * regla_en_version}.
   *
   * @throws IllegalArgumentException si la regla tiene algo que la tabla no puede guardar: una
   *     vigencia propia por periodo, una afectación a un artículo o una afectación con «desde»
   */
  public static ReglaEntidad aEntidad(Regla regla) {
    ArgumentChecks.obligatorio(regla, "regla");
    Optional<Vigencia> vigencia = regla.vigenciaPropia();
    Optional<Afectacion> afectacion = regla.afectacion();
    afectacion.ifPresent(ReglaMapper::exigirQueSePuedaGuardar);
    return new ReglaEntidad(
        regla.id().valor(),
        regla.fuente().norma().valor(),
        Categoria.de(regla.datos()).name(),
        regla.objeto(),
        regla.enunciado(),
        regla.fuente().articulo(),
        vigencia
            .map(propia -> aFecha(propia.aplicaDesde(), "vigenciaPropia.aplicaDesde"))
            .orElse(null),
        vigencia
            .flatMap(Vigencia::vigenteHasta)
            .map(hasta -> aFecha(hasta, "vigenciaPropia.vigenteHasta"))
            .orElse(null),
        regla.clasificacion().name(),
        afectacion.map(a -> a.tipo().name()).orElse(null),
        afectacion.flatMap(Afectacion::reglaAfectada).map(ReglaId::valor).orElse(null),
        null,
        JsonDeNormativa.mapper().writeValueAsString(regla.datos()));
  }

  private static Optional<Vigencia> vigenciaADominio(ReglaEntidad entidad) {
    LocalDate desde = entidad.getVigenteDesde();
    LocalDate hasta = entidad.getVigenteHasta();
    if (desde == null && hasta == null) {
      return Optional.empty();
    }
    if (desde == null) {
      throw new IllegalArgumentException("la regla tiene vigente_hasta pero no vigente_desde");
    }
    return Optional.of(
        new Vigencia(
            new Momento.EnFecha(desde),
            Optional.ofNullable(hasta).<Momento>map(Momento.EnFecha::new)));
  }

  private static Optional<Afectacion> afectacionADominio(ReglaEntidad entidad) {
    String tipo = entidad.getTipoAfectacion();
    if (entidad.getArticuloAfectado() != null) {
      throw new IllegalArgumentException(
          "la regla afecta un artículo, y la tabla regla lo guarda sin su norma: no se puede"
              + " reconstruir la afectación");
    }
    if (tipo == null && entidad.getReglaAfectadaId() == null) {
      return Optional.empty();
    }
    if (tipo == null || entidad.getReglaAfectadaId() == null) {
      throw new IllegalArgumentException(
          "la afectación necesita su tipo y la regla afectada, y la fila trae solo uno");
    }
    return Optional.of(
        new Afectacion(
            TipoDeAfectacion.valueOf(tipo),
            Optional.of(new ReglaId(entidad.getReglaAfectadaId())),
            Optional.empty(),
            Optional.empty()));
  }

  private static DatosDeRegla datosADominio(ReglaEntidad entidad) {
    Categoria categoria = Categoria.desde(entidad.getCategoria());
    String json = ArgumentChecks.obligatorioNoVacio(entidad.getDatos(), "datos");
    try {
      return JsonDeNormativa.mapper().readValue(json, categoria.clase);
    } catch (JacksonException e) {
      throw new IllegalArgumentException(
          "los datos de la regla " + entidad.getId() + " no son válidos para " + categoria, e);
    }
  }

  private static void exigirQueSePuedaGuardar(Afectacion afectacion) {
    if (afectacion.articuloAfectado().isPresent()) {
      throw new IllegalArgumentException(
          "la afectación a un artículo no se guarda: la tabla regla no guarda la norma del"
              + " artículo afectado");
    }
    if (afectacion.desde().isPresent()) {
      throw new IllegalArgumentException(
          "la afectación con un momento «desde» no se guarda: la tabla regla no tiene dónde");
    }
  }

  private static LocalDate aFecha(Momento momento, String nombre) {
    return switch (momento) {
      case Momento.EnFecha enFecha -> enFecha.fecha();
      case Momento.EnPeriodo enPeriodo ->
          throw new IllegalArgumentException(
              nombre + " debe ser una fecha: la tabla regla no guarda periodos académicos");
    };
  }
}
