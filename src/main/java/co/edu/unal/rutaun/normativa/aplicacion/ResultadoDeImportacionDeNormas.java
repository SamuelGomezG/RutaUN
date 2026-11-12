package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * Resultado de importar un archivo de normativa.
 *
 * @param creada si la norma era nueva y se incorporó
 * @param identificacion identificación de la norma leída
 * @param versionesCreadas número de versiones del consolidado creadas
 * @param ningunPlanQuedaDesactualizado si la norma se incorporó y todas sus reglas son
 *     informativas, por lo que ningún plan queda desactualizado por ella (UC07 ext. 5a)
 */
public record ResultadoDeImportacionDeNormas(
    boolean creada,
    String identificacion,
    int versionesCreadas,
    boolean ningunPlanQuedaDesactualizado) {

  /** Rechaza un número de versiones negativo y el aviso de una norma que no se incorporó. */
  public ResultadoDeImportacionDeNormas {
    if (versionesCreadas < 0) {
      throw new IllegalArgumentException("versionesCreadas no puede ser negativo");
    }
    if (!creada && ningunPlanQuedaDesactualizado) {
      throw new IllegalArgumentException(
          "una norma que no se incorporó no informa sobre planes desactualizados");
    }
  }
}
