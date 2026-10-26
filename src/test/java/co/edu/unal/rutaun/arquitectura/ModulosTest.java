package co.edu.unal.rutaun.arquitectura;

import co.edu.unal.rutaun.RutaUnApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Falla si un módulo usa las entrañas (clases internas) de otro. */
class ModulosTest {

  @Test
  void tt01_losModulosCumplenSusReglas() {
    ApplicationModules.of(RutaUnApplication.class).verify();
  }
}
