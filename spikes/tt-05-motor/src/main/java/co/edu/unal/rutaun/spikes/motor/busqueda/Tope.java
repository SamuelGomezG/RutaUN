package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Tope de carga: límite de créditos y de materias, cualquiera puede ser «sin límite». */
public record Tope(String id, Integer creditos, Integer materias) {

  public int limiteCreditos() {
    return creditos == null ? Integer.MAX_VALUE : creditos;
  }

  public int limiteMaterias() {
    return materias == null ? Integer.MAX_VALUE : materias;
  }

  public boolean tieneLimiteCreditos() {
    return creditos != null;
  }

  public boolean tieneLimiteMaterias() {
    return materias != null;
  }

  @Override
  public String toString() {
    return id
        + " ("
        + (creditos == null ? "sin límite" : creditos)
        + " cr., "
        + (materias == null ? "sin límite" : materias)
        + " mat.)";
  }
}
