package co.edu.unal.rutaun.arquitectura;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.RutaUnApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Falla si un módulo usa las entrañas (clases internas) de otro. */
class ModulosTest {

  @Test
  void tt01_losModulosCumplenSusReglas() {
    ApplicationModules.of(RutaUnApplication.class).verify();
  }

  @Test
  void tt02_losModulosDeLosContratosSeReconocen() {
    ApplicationModules modulos = ApplicationModules.of(RutaUnApplication.class);

    assertThat(modulos.stream().map(modulo -> modulo.getIdentifier().toString()))
        .contains("compartido", "puertos", "normativa", "incidentes");
  }
}
