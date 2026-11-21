package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.TipoAfectacion;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.ColumnasDeMomento;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;
import tools.jackson.core.JacksonException;

/**
 * Convierte entre la {@link Regla} del dominio y su fila {@link ReglaEntity}. No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link Regla#reconstituir}, así que conserva el identificador
 * y pasa por las mismas validaciones que una regla creada. Si la base trae un dato inválido, sube
 * la {@link IllegalArgumentException} del dominio.
 *
 * <p>El estado de la regla no está en la fila de {@code regla} sino en {@code regla_en_version},
 * porque una misma regla tiene un estado distinto en cada versión del consolidado. Por eso {@link
 * #aEntidad} no lo guarda y {@link #aDominio} solo reconstruye la regla; {@code
 * VersionDelConsolidadoMapper} la une a su estado en un {@code ReglaEnVersion}.
 *
 * <p>La vigencia propia y el inicio de la afectación se guardan como momentos, cada uno en tres
 * columnas. Una afectación a un artículo guarda su norma y su artículo. La categoría va en su
 * columna y los datos propios de la categoría, como JSON (provisional hasta #145; ver {@link
 * JsonDeNormativa}).
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
   * Reconstruye la regla que guarda esta fila.
   *
   * @throws IllegalArgumentException si la fila trae un dato inválido o una categoría desconocida
   */
  public static Regla aDominio(ReglaEntity entidad) {
    ArgumentChecks.obligatorio(entidad, "entidad");
    return Regla.reconstituir(
        new ReglaId(entidad.getId()),
        entidad.getObjeto(),
        entidad.getEnunciado(),
        new Fuente(new NormaId(entidad.getNormaId()), entidad.getFuente()),
        vigenciaADominio(entidad),
        ClasificacionRegla.valueOf(entidad.getClasificacion()),
        afectacionADominio(entidad),
        datosADominio(entidad));
  }

  /**
   * Convierte la regla en la fila que se guarda en la base. No guarda el estado: va en {@code
   * regla_en_version}.
   */
  public static ReglaEntity aEntidad(Regla regla) {
    ArgumentChecks.obligatorio(regla, "regla");
    Optional<Vigencia> vigencia = regla.vigenciaPropia();
    Optional<Afectacion> afectacion = regla.afectacion();
    return new ReglaEntity(
        regla.id().valor(),
        regla.fuente().norma().valor(),
        Categoria.de(regla.datos()).name(),
        regla.objeto(),
        regla.enunciado(),
        regla.fuente().articulo(),
        vigencia.map(propia -> MomentoEnColumnas.aColumnas(propia.aplicaDesde())).orElse(null),
        vigencia.flatMap(Vigencia::vigenteHasta).map(MomentoEnColumnas::aColumnas).orElse(null),
        regla.clasificacion().name(),
        afectacion.map(a -> a.tipo().name()).orElse(null),
        afectacion.flatMap(Afectacion::reglaAfectada).map(ReglaId::valor).orElse(null),
        afectacion
            .flatMap(Afectacion::articuloAfectado)
            .map(Fuente::norma)
            .map(NormaId::valor)
            .orElse(null),
        afectacion.flatMap(Afectacion::articuloAfectado).map(Fuente::articulo).orElse(null),
        afectacion.flatMap(Afectacion::desde).map(MomentoEnColumnas::aColumnas).orElse(null),
        JsonDeNormativa.mapper().writeValueAsString(regla.datos()));
  }

  private static Optional<Vigencia> vigenciaADominio(ReglaEntity entidad) {
    ColumnasDeMomento desde = entidad.getVigenteDesde();
    ColumnasDeMomento hasta = entidad.getVigenteHasta();
    if (desde == null && hasta == null) {
      return Optional.empty();
    }
    if (desde == null) {
      throw new IllegalArgumentException("la regla tiene vigente_hasta pero no vigente_desde");
    }
    return Optional.of(
        new Vigencia(
            MomentoEnColumnas.aMomento(desde),
            Optional.ofNullable(MomentoEnColumnas.aMomento(hasta))));
  }

  private static Optional<Afectacion> afectacionADominio(ReglaEntity entidad) {
    String tipo = entidad.getTipoAfectacion();
    if (tipo == null) {
      return Optional.empty();
    }
    Optional<ReglaId> reglaAfectada =
        Optional.ofNullable(entidad.getReglaAfectadaId()).map(ReglaId::new);
    Optional<Fuente> articuloAfectado =
        Optional.ofNullable(entidad.getNormaAfectadaId())
            .map(norma -> new Fuente(new NormaId(norma), entidad.getArticuloAfectado()));
    Optional<Momento> desde =
        Optional.ofNullable(MomentoEnColumnas.aMomento(entidad.getAfectacionDesde()));
    return Optional.of(
        new Afectacion(TipoAfectacion.valueOf(tipo), reglaAfectada, articuloAfectado, desde));
  }

  private static DatosDeRegla datosADominio(ReglaEntity entidad) {
    Categoria categoria = Categoria.desde(entidad.getCategoria());
    String json = ArgumentChecks.obligatorioNoVacio(entidad.getDatos(), "datos");
    try {
      return JsonDeNormativa.mapper().readValue(json, categoria.clase);
    } catch (JacksonException e) {
      throw new IllegalArgumentException(
          "los datos de la regla " + entidad.getId() + " no son válidos para " + categoria, e);
    }
  }
}
