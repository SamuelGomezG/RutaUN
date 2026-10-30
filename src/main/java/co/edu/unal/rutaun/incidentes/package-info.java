/**
 * Incidentes: contexto de soporte que registra lo que el sistema detecta y el administrador debe
 * conocer. No conoce a los contextos de negocio: ellos le avisan con {@link IncidenteDetectado}.
 */
@ApplicationModule(
    displayName = "Incidentes",
    allowedDependencies = {"compartido", "utilidades"})
package co.edu.unal.rutaun.incidentes;

import org.springframework.modulith.ApplicationModule;
