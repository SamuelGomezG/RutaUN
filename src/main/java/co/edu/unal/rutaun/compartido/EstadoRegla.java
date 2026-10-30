package co.edu.unal.rutaun.compartido;

/**
 * Estado de una regla en una versión del consolidado (diseño de clases, «Enums»; estados de regla
 * del glosario: «vigente», «programada», «ya no rige» y la señal «en revisión» de UC06 ext. 6d).
 * Vive en el núcleo compartido, como {@code EstadoRestriccion}: lo cruzan a otros contextos las
 * interfaces de rol que exponen reglas ({@code ReglasPorPeriodo}).
 */
public enum EstadoRegla {
  /** Rige hoy. */
  VIGENTE,
  /** Rige a partir de un periodo o fecha futura (UC06 ext. 6a). */
  PROGRAMADA,
  /** Su vigencia terminó: la norma venció u otra la derogó o modificó (UC06 ext. 5b). */
  YA_NO_RIGE,
  /** Dos reglas se contradicen sin que una afecte a la otra (UC06 ext. 6d). */
  EN_REVISION
}
