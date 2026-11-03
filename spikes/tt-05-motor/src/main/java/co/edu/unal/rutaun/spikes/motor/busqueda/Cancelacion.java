package co.edu.unal.rutaun.spikes.motor.busqueda;

/** Plazo absoluto y bandera de cancelación compartida entre el futuro y la búsqueda. */
public final class Cancelacion {

  private final long plazoNanos;
  private volatile boolean cancelado;

  public Cancelacion(long duracionMilisegundos) {
    this.plazoNanos = System.nanoTime() + duracionMilisegundos * 1_000_000L;
  }

  private Cancelacion(long plazoNanos, boolean cancelado) {
    this.plazoNanos = plazoNanos;
    this.cancelado = cancelado;
  }

  public static Cancelacion sinLimite() {
    return new Cancelacion(Long.MAX_VALUE, false);
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

  public long restanteMilisegundos() {
    long restante = (plazoNanos - System.nanoTime()) / 1_000_000L;
    return Math.max(0, restante);
  }
}
