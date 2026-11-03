package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Plazo absoluto y bandera de cancelación compartida entre el futuro y la búsqueda. */
public final class Cancelacion {

  private final long plazoNanos;
  private volatile boolean cancelado;

  public Cancelacion(long duracionMilisegundos) {
    this.plazoNanos = System.nanoTime() + duracionMilisegundos * 1_000_000L;
  }

  public boolean cancelado() {
    if (!cancelado && System.nanoTime() - plazoNanos >= 0) {
      cancelado = true;
    }
    return cancelado;
  }

  public void cancelar() {
    cancelado = true;
  }
}
