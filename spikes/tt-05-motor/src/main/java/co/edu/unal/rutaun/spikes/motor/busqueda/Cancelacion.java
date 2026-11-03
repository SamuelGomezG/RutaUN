package co.edu.unal.rutaun.spikes.motor.busqueda;

/**
 * Plazo absoluto y bandera de cancelación compartida entre el futuro y la búsqueda.
 *
 * <p>El reloj ({@link System#nanoTime()}) se consulta en la primera llamada a {@link #cancelado()}
 * y luego cada {@value #LLAMADAS_ENTRE_LECTURAS} llamadas: consultarlo en cada iteración de los
 * bucles internos multiplicó por 5,5 el tiempo del cálculo. El ritmo lo decide solo esta clase.
 *
 * <p>El contador no es {@code volatile}: cada instancia la consulta un solo hilo, el del cálculo.
 * Desde otro hilo solo se llama a {@link #cancelar()}, que escribe la bandera {@code volatile}.
 */
public final class Cancelacion {

  private static final int LLAMADAS_ENTRE_LECTURAS = 256;

  private final long plazoNanos;
  private volatile boolean cancelado;
  private int llamadasDesdeLaLectura;

  public Cancelacion(long duracionMilisegundos) {
    this.plazoNanos = System.nanoTime() + duracionMilisegundos * 1_000_000L;
  }

  public boolean cancelado() {
    if (cancelado) {
      return true;
    }
    if (llamadasDesdeLaLectura == 0 && System.nanoTime() - plazoNanos >= 0) {
      cancelado = true;
    }
    llamadasDesdeLaLectura = (llamadasDesdeLaLectura + 1) % LLAMADAS_ENTRE_LECTURAS;
    return cancelado;
  }

  public void cancelar() {
    cancelado = true;
  }
}
