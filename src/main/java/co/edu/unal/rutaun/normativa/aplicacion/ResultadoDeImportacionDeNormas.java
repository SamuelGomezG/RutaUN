package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * Resultado de importar un archivo de normativa.
 *
 * @param creada si la norma era nueva y se incorporó
 * @param identificacion identificación de la norma leída
 * @param versionesCreadas número de versiones del consolidado creadas
 */
public record ResultadoDeImportacionDeNormas(
    boolean creada, String identificacion, int versionesCreadas) {

  /** Rechaza un número de versiones negativo. */
  public ResultadoDeImportacionDeNormas {
    if (versionesCreadas < 0) {
      throw new IllegalArgumentException("versionesCreadas no puede ser negativo");
    }
  }
}
