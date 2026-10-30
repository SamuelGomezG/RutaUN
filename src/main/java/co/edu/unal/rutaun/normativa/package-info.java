/**
 * Normativa: las normas y sus reglas, que son la fuente de verdad, y la vista compilada con la que
 * se evalúa todo lo demás. No depende de ningún otro contexto de negocio.
 */
@ApplicationModule(
    displayName = "Normativa",
    allowedDependencies = {"compartido", "puertos", "incidentes", "utilidades"})
package co.edu.unal.rutaun.normativa;

import org.springframework.modulith.ApplicationModule;
