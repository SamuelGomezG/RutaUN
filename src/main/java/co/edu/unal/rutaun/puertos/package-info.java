/**
 * Puertos transversales: las interfaces de frontera que reciben contextos distintos, con sus
 * adaptadores. El dominio no importa este paquete, para que Spring siga fuera del dominio.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package co.edu.unal.rutaun.puertos;

import org.springframework.modulith.ApplicationModule;
