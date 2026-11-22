package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Locale;

/**
 * Facultad de la Universidad, identificada por un código estable (por ejemplo, {@code ING}). La
 * declara el archivo de programas y las normas la citan por ese código, tanto en su órgano como en
 * su alcance. El nombre para mostrar vive en {@link Programa#nombreDeFacultad()}.
 *
 * @param codigo código estable de la facultad
 */
public record Facultad(String codigo) {

  /** Exige un código no vacío y lo guarda sin espacios a los lados y en mayúsculas. */
  public Facultad {
    codigo = ArgumentChecks.obligatorioNoVacio(codigo, "codigo").strip().toUpperCase(Locale.ROOT);
  }
}
