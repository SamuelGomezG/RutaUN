package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * La consulta de normativa no está disponible porque la aplicación no tiene la implementación de la
 * fachada de consulta. La presentación la traduce en una página que lo dice (503).
 */
public class ConsultaNoDisponibleException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** Crea la excepción con su mensaje fijo. */
  public ConsultaNoDisponibleException() {
    super("la consulta de normativa no está disponible");
  }
}
