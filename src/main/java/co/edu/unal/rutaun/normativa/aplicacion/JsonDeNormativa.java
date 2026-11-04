package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Componente;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.utilidades.ConfiguracionJson;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link JsonMapper} con el que los mappers de Normativa guardan y leen los datos de una regla y el
 * detalle del motivo de una versión. Es el de {@link ConfiguracionJson} más dos <em>mix-ins</em>
 * para los tipos con subtipos, de modo que el dominio no lleva anotaciones de Jackson.
 *
 * <p>TT-04-T3 (#91). Es provisional: el diseño pide records propios de persistencia y un campo
 * {@code esquema} en cada JSON guardado (DOC-09, R-09), y cuando eso exista este mapper desaparece.
 * Mientras tanto el formato guardado sigue las clases del dominio: si una cambia, los datos que ya
 * están en {@code data/} dejan de leerse, y se borra {@code data/} y se recarga la semilla.
 *
 * <p>El discriminante se llama {@code @tipo} y no {@code tipo} porque {@link Componente} y {@link
 * ExpresionDeMaterias.MateriaExterna} ya tienen un campo {@code tipo}: con el mismo nombre el JSON
 * saldría con la clave repetida, y un JSON con una clave repetida no es válido. Los nombres de los
 * subtipos de las expresiones son los del formato de UC07-HU03 (#93).
 *
 * <p>La categoría de primer nivel de unos datos no lleva discriminante: la dice la columna {@code
 * categoria} de la regla, y {@link ReglaMapper} lee con la clase concreta.
 */
final class JsonDeNormativa {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@tipo")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = Componente.class, name = "COMPONENTE"),
    @JsonSubTypes.Type(value = ComponenteDeLibreEleccion.class, name = "LIBRE_ELECCION"),
    @JsonSubTypes.Type(value = Agrupacion.class, name = "AGRUPACION")
  })
  private interface NodoDeMallaMixIn {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@tipo")
  @JsonSubTypes({
    @JsonSubTypes.Type(
        value = ExpresionDeMaterias.MateriaDelPrograma.class,
        name = "MATERIA_DEL_PROGRAMA"),
    @JsonSubTypes.Type(value = ExpresionDeMaterias.MateriaExterna.class, name = "MATERIA_EXTERNA"),
    @JsonSubTypes.Type(value = ExpresionDeMaterias.Y.class, name = "Y"),
    @JsonSubTypes.Type(value = ExpresionDeMaterias.O.class, name = "O")
  })
  private interface ExpresionDeMateriasMixIn {}

  private static final JsonMapper MAPPER =
      ConfiguracionJson.mapper()
          .rebuild()
          .addMixIn(NodoDeMalla.class, NodoDeMallaMixIn.class)
          .addMixIn(ExpresionDeMaterias.class, ExpresionDeMateriasMixIn.class)
          .build();

  private JsonDeNormativa() {}

  /** Devuelve el mapper de los mappers de Normativa. */
  static JsonMapper mapper() {
    return MAPPER;
  }
}
