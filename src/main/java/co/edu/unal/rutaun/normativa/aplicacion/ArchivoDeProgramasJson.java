package co.edu.unal.rutaun.normativa.aplicacion;

import java.util.List;

/**
 * Raíz del archivo JSON de programas: un objeto con la lista {@code programas}.
 *
 * <p>SOP-03 (#55).
 *
 * @param programas los programas que define el archivo
 */
public record ArchivoDeProgramasJson(List<ProgramaJson> programas) {}
