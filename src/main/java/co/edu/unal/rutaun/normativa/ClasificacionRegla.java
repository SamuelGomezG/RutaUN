package co.edu.unal.rutaun.normativa;

/**
 * Si el plan aplica la regla o solo la informa (glosario, «Reglas del plan y reglas informativas»;
 * diseño de clases, «Enums»).
 */
public enum ClasificacionRegla {
  /** El cálculo del plan la aplica. */
  DEL_PLAN,
  /**
   * El plan no la aplica: se muestra con «solo informativa: el plan no la aplica» (UC06 ext. 6c).
   */
  INFORMATIVA
}
