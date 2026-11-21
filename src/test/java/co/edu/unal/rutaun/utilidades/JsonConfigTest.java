package co.edu.unal.rutaun.utilidades;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** TT-04-T6 (#145): comprueba la política del mapper compartido. */
class JsonConfigTest {

  /** Datos de ejemplo con los tipos que usan los mappers: UUID, lista de textos y fecha. */
  record Ejemplo(UUID id, List<String> nombres, LocalDate fecha) {}

  private static Ejemplo ejemplo() {
    return new Ejemplo(
        UUID.randomUUID(), List.of("Ingeniería", "Ciencias"), LocalDate.of(2022, 2, 1));
  }

  @Test
  void tt04_jsonIdaYVuelta() {
    Ejemplo original = ejemplo();

    String json = JsonConfig.mapper().writeValueAsString(original);
    Ejemplo leido = JsonConfig.mapper().readValue(json, Ejemplo.class);

    assertThat(leido).isEqualTo(original);
  }

  @Test
  void tt04_laFechaSeEscribeEnIso() {
    String json = JsonConfig.mapper().writeValueAsString(ejemplo());

    assertThat(json).contains("\"2022-02-01\"");
  }

  @Test
  void tt04_seIgnoraUnCampoDesconocido() {
    Ejemplo original = ejemplo();
    String json = JsonConfig.mapper().writeValueAsString(original);
    String conCampoDesconocido = json.substring(0, json.length() - 1) + ",\"desconocido\":true}";

    assertThat(JsonConfig.mapper().readValue(conCampoDesconocido, Ejemplo.class))
        .isEqualTo(original);
  }

  @Test
  void tt04_elMapperEsUnoSolo() {
    assertThat(JsonConfig.mapper()).isSameAs(JsonConfig.mapper());
  }
}
