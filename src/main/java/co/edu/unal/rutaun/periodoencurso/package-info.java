/**
 * Periodo en curso: el último periodo académico que empezó. Es un valor único y configurable que
 * leen los demás contextos (el plan, el avance y la comprobación de versión) a través de {@link
 * PeriodoEnCursoFacade}.
 */
@ApplicationModule(
    displayName = "Periodo en curso",
    allowedDependencies = {"compartido"})
package co.edu.unal.rutaun.periodoencurso;

import org.springframework.modulith.ApplicationModule;
