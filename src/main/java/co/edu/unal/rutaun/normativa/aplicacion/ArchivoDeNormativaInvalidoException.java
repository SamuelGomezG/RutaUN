package co.edu.unal.rutaun.normativa.aplicacion;

/** Un archivo de normativa no se pudo leer o no pasó la validación de UC07. */
public class ArchivoDeNormativaInvalidoException extends RuntimeException {

  /** Crea la excepción con su motivo. */
  public ArchivoDeNormativaInvalidoException(String mensaje) {
    super(mensaje);
  }

  /** Crea la excepción con su motivo y su causa. */
  public ArchivoDeNormativaInvalidoException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
