package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * Se lanza cuando el archivo de programas no se puede leer o trae datos inválidos.
 *
 * <p>SOP-03 (#55). Es una falla real de la carga, no una extensión del caso de uso.
 */
public final class ArchivoDeProgramasInvalidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** Crea la excepción con un mensaje. */
  public ArchivoDeProgramasInvalidoException(String mensaje) {
    super(mensaje);
  }

  /** Crea la excepción con un mensaje y su causa. */
  public ArchivoDeProgramasInvalidoException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
