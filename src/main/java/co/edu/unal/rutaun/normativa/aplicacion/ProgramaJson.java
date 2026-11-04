package co.edu.unal.rutaun.normativa.aplicacion;

/**
 * Un programa tal como aparece en el archivo JSON de programas.
 *
 * <p>SOP-03 (#55). Es solo la forma del archivo: no valida nada. Las validaciones las hace el
 * dominio al crear el {@code Programa}.
 *
 * @param nombre nombre del programa
 * @param codigoSia código SIA del programa
 * @param facultad nombre de la facultad
 */
public record ProgramaJson(String nombre, String codigoSia, String facultad) {}
