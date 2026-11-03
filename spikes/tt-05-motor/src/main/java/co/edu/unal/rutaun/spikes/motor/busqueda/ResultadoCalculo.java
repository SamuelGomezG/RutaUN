package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Resultado sellado mínimo del cálculo. */
public sealed interface ResultadoCalculo {

  record Calculado(Plan plan, Metricas metricas, long msExistencia, long msOptimos)
      implements ResultadoCalculo {}

  record TiempoAgotado(Metricas metricas, long ms) implements ResultadoCalculo {}

  record SinPlanPosible(String motivo) implements ResultadoCalculo {}
}
