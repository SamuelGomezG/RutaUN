/**
 * Núcleo compartido: los valores que todos los contextos importan y que cambian casi nunca
 * (identificadores, créditos, tiempo académico y el marcador de los eventos de dominio).
 *
 * <p>Excepción: aquí también está la comprobación de versión antes de guardar (SOP-07), que es
 * comportamiento y no un valor. El porqué está en la decisión 0003 de {@code docs/diseno/decisiones/}.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package co.edu.unal.rutaun.compartido;

import org.springframework.modulith.ApplicationModule;
