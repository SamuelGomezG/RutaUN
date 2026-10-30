/**
 * Núcleo compartido: los valores que todos los contextos importan y que cambian casi nunca
 * (identificadores, créditos y el marcador de los eventos de dominio).
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package co.edu.unal.rutaun.compartido;

import org.springframework.modulith.ApplicationModule;
