package co.edu.unal.rutaun.utilidades;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** TT-04-T3 (#91): comprueba que el mapper compartido hace ida y vuelta de un record. */
class ConfiguracionJsonTest {

  /** Datos de ejemplo con los tipos que usan los mappers: UUID, lista de textos y fecha. */
  record Ejemplo(UUID id, List<String> nombres, LocalDate fecha) {}

  @Test
  void tt04_jsonIdaYVuelta() {
    Ejemplo original =
        new Ejemplo(UUID.randomUUID(), List.of("Ingeniería", "Ciencias"), LocalDate.of(2024, 8, 1));

    String json = ConfiguracionJson.mapper().writeValueAsString(original);
    Ejemplo leido = ConfiguracionJson.mapper().readValue(json, Ejemplo.class);

    assertThat(leido).isEqualTo(original);
  }

  @Test
  void tt04_elMapperEsUnoSolo() {
    assertThat(ConfiguracionJson.mapper()).isSameAs(ConfiguracionJson.mapper());
  }
}
