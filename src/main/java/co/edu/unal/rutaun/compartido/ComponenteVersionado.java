package co.edu.unal.rutaun.compartido;

/**
 * Cada cosa que un caso de uso lee al empezar y que no debe haber cambiado cuando va a guardar
 * (UC01 paso 10, UC02 paso 11, UC03 paso 7 y UC04 paso 10). El orden de las constantes es el orden
 * en que se informan los cambios.
 */
public enum ComponenteVersionado {
  /** El avance del estudiante. */
  AVANCE,
  /** La versión vigente del consolidado del programa. */
  VERSION_DEL_CONSOLIDADO,
  /** El plan vigente del estudiante. */
  PLAN_VIGENTE,
  /** El periodo en curso. */
  PERIODO_EN_CURSO
}
