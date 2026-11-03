package co.edu.unal.rutaun.normativa.dominio;

/**
 * Estado de una materia en el avance o en un plan (glosario, «Avance» y «Cálculo del plan»). Una
 * materia que ya no rige permite explicar por qué una restricción queda incierta.
 */
public enum EstadoDeMateria {
  /** La materia está aprobada. */
  APROBADA,
  /** La materia está en curso. */
  EN_CURSO,
  /** La materia no está aprobada ni en curso. */
  PENDIENTE,
  /** La materia ya no rige en la versión del consolidado que se evalúa. */
  YA_NO_RIGE
}
