package co.edu.unal.rutaun.normativa.persistencia;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Forma en que se guardan los datos propios de una regla en la columna {@code regla.datos}, como
 * JSON. La persistencia no importa el dominio, así que estos records repiten su forma con tipos
 * básicos; {@code DatosDeReglaJsonMapper} convierte entre unos y otros.
 *
 * <p>El discriminante {@code categoria} dice la variante, con el mismo nombre que la columna {@code
 * categoria} de la regla. Cada JSON guardado lleva {@code esquema} con su versión.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "categoria")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DatosDeReglaJson.MateriaJson.class, name = "MATERIA"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.PrerrequisitoJson.class, name = "PRERREQUISITO"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.CorrequisitoJson.class, name = "CORREQUISITO"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.HabilitacionJson.class, name = "HABILITACION"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.AgrupacionJson.class, name = "AGRUPACION"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.ComponenteJson.class, name = "COMPONENTE"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.ProgramaJson.class, name = "PROGRAMA"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.EquivalenciaJson.class, name = "EQUIVALENCIA"),
  @JsonSubTypes.Type(
      value = DatosDeReglaJson.CargaPorPeriodoJson.class,
      name = "CARGA_POR_PERIODO"),
  @JsonSubTypes.Type(
      value = DatosDeReglaJson.RequisitoDeGradoJson.class,
      name = "REQUISITO_DE_GRADO"),
  @JsonSubTypes.Type(value = DatosDeReglaJson.OtraJson.class, name = "OTRA")
})
public sealed interface DatosDeReglaJson {

  /** Versión del esquema de este JSON. */
  int ESQUEMA = 1;

  /**
   * Versión del esquema con que se guardó.
   *
   * @return la versión del esquema
   */
  int esquema();

  /**
   * Datos de una regla de materia.
   *
   * @param esquema versión del esquema
   * @param materia materia con sus datos en la malla
   * @param contenedor componente o agrupación que la contiene
   */
  record MateriaJson(int esquema, MateriaEnMallaJson materia, NodoJson contenedor)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de prerrequisito.
   *
   * @param esquema versión del esquema
   * @param materia código de la materia que se exige
   * @param expresion expresión de materias del prerrequisito
   */
  record PrerrequisitoJson(int esquema, String materia, ExpresionJson expresion)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de correquisito.
   *
   * @param esquema versión del esquema
   * @param materia código de la materia que se exige
   * @param expresion expresión de materias del correquisito
   */
  record CorrequisitoJson(int esquema, String materia, ExpresionJson expresion)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de habilitación.
   *
   * @param esquema versión del esquema
   * @param materia código de la materia que habilita
   * @param componente componente del que se miden los créditos, o {@code null} si es del programa
   * @param umbral créditos exigidos
   */
  record HabilitacionJson(int esquema, String materia, String componente, Integer umbral)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de agrupación.
   *
   * @param esquema versión del esquema
   * @param agrupacion agrupación con sus datos
   * @param contenedor componente o agrupación que la contiene
   */
  record AgrupacionJson(int esquema, NodoJson.AgrupacionNodo agrupacion, NodoJson contenedor)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de componente.
   *
   * @param esquema versión del esquema
   * @param componente componente al que se refiere la regla
   */
  record ComponenteJson(int esquema, NodoJson componente) implements DatosDeReglaJson {}

  /**
   * Datos de una regla del programa.
   *
   * @param esquema versión del esquema
   * @param creditosExigidos créditos exigidos del plan de estudios
   */
  record ProgramaJson(int esquema, Integer creditosExigidos) implements DatosDeReglaJson {}

  /**
   * Datos de una regla de equivalencia.
   *
   * @param esquema versión del esquema
   * @param origen códigos de las materias de origen, ordenados
   * @param destino código de la materia de destino
   */
  record EquivalenciaJson(int esquema, List<String> origen, String destino)
      implements DatosDeReglaJson {

    /** Deja la lista de origen inmutable. */
    public EquivalenciaJson {
      origen = ArgumentChecks.copiaSinNulos(origen, "origen");
    }
  }

  /**
   * Datos de una regla de carga mínima o máxima por periodo.
   *
   * @param esquema versión del esquema
   * @param creditos créditos de la carga
   * @param tipo si la carga es mínima o máxima
   */
  record CargaPorPeriodoJson(int esquema, Integer creditos, String tipo)
      implements DatosDeReglaJson {}

  /**
   * Datos de una regla de requisitos de grado.
   *
   * @param esquema versión del esquema
   */
  record RequisitoDeGradoJson(int esquema) implements DatosDeReglaJson {}

  /**
   * Datos de una regla de la categoría «otra».
   *
   * @param esquema versión del esquema
   */
  record OtraJson(int esquema) implements DatosDeReglaJson {}

  /**
   * Materia de la malla con sus datos.
   *
   * @param codigo código de la materia
   * @param nombre nombre de la materia
   * @param creditos créditos de la materia
   * @param obligatoriedad si es obligatoria u optativa
   * @param semestreSugerido semestre sugerido, o {@code null} si no tiene
   * @param cursableDesdeElPrograma si se puede cursar desde el programa
   */
  record MateriaEnMallaJson(
      String codigo,
      String nombre,
      Integer creditos,
      String obligatoriedad,
      Integer semestreSugerido,
      Boolean cursableDesdeElPrograma) {}

  /**
   * Créditos que exige un nodo de la malla.
   *
   * @param exigidos créditos exigidos
   * @param obligatorios créditos obligatorios del desglose, o {@code null} si no hay desglose
   * @param optativos créditos optativos del desglose, o {@code null} si no hay desglose
   * @param sugeridosPorSemestre créditos sugeridos por semestre
   */
  record ExigenciaJson(
      Integer exigidos,
      Integer obligatorios,
      Integer optativos,
      Map<Integer, Integer> sugeridosPorSemestre) {

    /** Deja el mapa de sugeridos inmutable y en su orden. */
    public ExigenciaJson {
      sugeridosPorSemestre =
          Collections.unmodifiableMap(
              new LinkedHashMap<>(
                  ArgumentChecks.obligatorio(sugeridosPorSemestre, "sugeridosPorSemestre")));
    }
  }

  /**
   * Nodo de la malla: componente, componente de libre elección o agrupación.
   *
   * <p>El discriminante {@code @tipo} no se llama {@code tipo} porque {@link ComponenteNodo} ya
   * tiene ese campo.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@tipo")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = NodoJson.ComponenteNodo.class, name = "COMPONENTE"),
    @JsonSubTypes.Type(value = NodoJson.LibreEleccionNodo.class, name = "LIBRE_ELECCION"),
    @JsonSubTypes.Type(value = NodoJson.AgrupacionNodo.class, name = "AGRUPACION")
  })
  sealed interface NodoJson {

    /**
     * Componente de la malla.
     *
     * @param nombre nombre del componente
     * @param tipo tipo de componente
     * @param exigencia créditos que exige
     * @param materias materias del componente
     * @param agrupaciones agrupaciones del componente
     */
    record ComponenteNodo(
        String nombre,
        String tipo,
        ExigenciaJson exigencia,
        List<MateriaEnMallaJson> materias,
        List<AgrupacionNodo> agrupaciones)
        implements NodoJson {

      /** Deja las listas inmutables. */
      public ComponenteNodo {
        materias = ArgumentChecks.copiaSinNulos(materias, "materias");
        agrupaciones = ArgumentChecks.copiaSinNulos(agrupaciones, "agrupaciones");
      }
    }

    /**
     * Componente de libre elección.
     *
     * @param nombre nombre del componente
     * @param exigencia créditos que exige
     * @param agrupaciones agrupaciones del componente
     */
    record LibreEleccionNodo(
        String nombre, ExigenciaJson exigencia, List<AgrupacionNodo> agrupaciones)
        implements NodoJson {

      /** Deja la lista de agrupaciones inmutable. */
      public LibreEleccionNodo {
        agrupaciones = ArgumentChecks.copiaSinNulos(agrupaciones, "agrupaciones");
      }
    }

    /**
     * Agrupación de la malla.
     *
     * @param id identificador de la agrupación
     * @param nombre nombre de la agrupación
     * @param exigencia créditos que exige
     * @param materias materias de la agrupación
     * @param subagrupaciones agrupaciones que contiene
     */
    record AgrupacionNodo(
        UUID id,
        String nombre,
        ExigenciaJson exigencia,
        List<MateriaEnMallaJson> materias,
        List<AgrupacionNodo> subagrupaciones)
        implements NodoJson {

      /** Deja las listas inmutables. */
      public AgrupacionNodo {
        materias = ArgumentChecks.copiaSinNulos(materias, "materias");
        subagrupaciones = ArgumentChecks.copiaSinNulos(subagrupaciones, "subagrupaciones");
      }
    }
  }

  /**
   * Expresión de materias: término del programa, materia externa, «y» u «o».
   *
   * <p>Los nombres del discriminante son los del archivo de importación. Los términos de una
   * combinación van como lista, en el orden en que se dieron, para que la lectura sea determinista.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@tipo")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = ExpresionJson.Termino.class, name = "MATERIA_DEL_PROGRAMA"),
    @JsonSubTypes.Type(value = ExpresionJson.Externa.class, name = "MATERIA_EXTERNA"),
    @JsonSubTypes.Type(value = ExpresionJson.Todas.class, name = "Y"),
    @JsonSubTypes.Type(value = ExpresionJson.Alguna.class, name = "O")
  })
  sealed interface ExpresionJson {

    /**
     * Materia que figura en la malla del programa.
     *
     * @param codigo código de la materia
     */
    record Termino(String codigo) implements ExpresionJson {}

    /**
     * Materia que la norma menciona y no figura en la malla del programa.
     *
     * @param nombre nombre de la materia
     * @param codigo código de la materia, o {@code null} si la norma no lo da
     * @param tipo si es de nivelación o de otro programa
     */
    record Externa(String nombre, String codigo, String tipo) implements ExpresionJson {}

    /**
     * «Y» de varios términos.
     *
     * @param terminos términos de la combinación, en orden
     */
    record Todas(List<ExpresionJson> terminos) implements ExpresionJson {

      /** Deja la lista de términos inmutable. */
      public Todas {
        terminos = ArgumentChecks.copiaSinNulos(terminos, "terminos");
      }
    }

    /**
     * «O» de varios términos.
     *
     * @param terminos términos de la combinación, en orden
     */
    record Alguna(List<ExpresionJson> terminos) implements ExpresionJson {

      /** Deja la lista de términos inmutable. */
      public Alguna {
        terminos = ArgumentChecks.copiaSinNulos(terminos, "terminos");
      }
    }
  }
}
