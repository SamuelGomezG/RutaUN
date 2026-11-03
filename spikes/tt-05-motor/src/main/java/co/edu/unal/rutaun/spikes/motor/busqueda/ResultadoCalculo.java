package co.edu.unal.rutaun.spikes.motor.busqueda;

/**
 * Resultado sellado del cálculo.
 *
 * <ul>
 *   <li>{@link Calculado}: hay plan. {@code c4Aplicado} indica si el plan sigue la lista de
 *       optativas del criterio 4 o, si no se pudo armar, el plan que fijó el criterio 3; {@code
 *       c4PorRescate}, si la lista salió de la vía de rescate (búsqueda en profundidad con tope de
 *       nodos) porque el constructor solo no reconstruyó el plan.
 *   <li>{@link SinPlanPosible}: de verdad no hay plan (cota inferior infinita o ninguno hasta el
 *       máximo de semestres); en UC01 es la ext. 5b.
 *   <li>{@link NoCalculado}: se demostró que existe un plan con {@code k} semestres, pero un paso
 *       posterior del motor no pudo completarlo. No es una conclusión sobre la normativa.
 *   <li>{@link TiempoAgotado}: el plazo venció; en UC01 es la ext. 5a.
 * </ul>
 */
public sealed interface ResultadoCalculo {

  record Calculado(
      Plan plan,
      Metricas metricas,
      long msExistencia,
      long msOptimos,
      boolean c4Aplicado,
      boolean c4PorRescate)
      implements ResultadoCalculo {}

  record TiempoAgotado(Metricas metricas, long ms) implements ResultadoCalculo {}

  record SinPlanPosible(String motivo) implements ResultadoCalculo {}

  record NoCalculado(String causa) implements ResultadoCalculo {}
}
