package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Contadores de una búsqueda. */
public final class Metricas {

  public long nodos;
  public long tamanoMemoizacion;
  public long llamadasExistencia;
  public boolean cancelada;

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
