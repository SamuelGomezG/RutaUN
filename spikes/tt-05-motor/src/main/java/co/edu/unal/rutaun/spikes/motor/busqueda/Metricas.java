package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Contadores de una búsqueda. */
public final class Metricas {

  private long nodos;
  private long tamanoMemoizacion;
  private long llamadasExistencia;
  private boolean cancelada;

  /** Vista de solo lectura para quien informa las mediciones. */
  public record Instantanea(
      long nodos, long tamanoMemoizacion, long llamadasExistencia, boolean cancelada) {}

  public void reiniciar() {
    nodos = 0;
    tamanoMemoizacion = 0;
    llamadasExistencia = 0;
    cancelada = false;
  }

  public void registrarNodo() {
    nodos++;
  }

  public void registrarLlamada() {
    llamadasExistencia++;
  }

  public void registrarMemoizacion(long tamano) {
    tamanoMemoizacion = Math.max(tamanoMemoizacion, tamano);
  }

  public void marcarCancelada() {
    cancelada = true;
  }

  public Instantanea instantanea() {
    return new Instantanea(nodos, tamanoMemoizacion, llamadasExistencia, cancelada);
  }

  public Metricas copia() {
    Metricas copia = new Metricas();
    copia.nodos = nodos;
    copia.tamanoMemoizacion = tamanoMemoizacion;
    copia.llamadasExistencia = llamadasExistencia;
    copia.cancelada = cancelada;
    return copia;
  }

  public void sumar(Metricas otra) {
    nodos += otra.nodos;
    tamanoMemoizacion = Math.max(tamanoMemoizacion, otra.tamanoMemoizacion);
    llamadasExistencia += otra.llamadasExistencia;
    cancelada |= otra.cancelada;
  }
}
