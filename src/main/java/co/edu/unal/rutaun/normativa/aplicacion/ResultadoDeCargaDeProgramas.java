package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * Resumen de una carga de programas.
 *
 * <p>SOP-03 (#55).
 *
 * @param creados programas que no existían y se guardaron
 * @param yaExistentes programas que ya estaban (mismo código SIA) y se dejaron como estaban
 */
public record ResultadoDeCargaDeProgramas(int creados, int yaExistentes) {}
