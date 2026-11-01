package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgrupacionIdTest {

  @Test
  void tt02_agrupacionIdRechazaNull() {
    assertThatThrownBy(() -> new AgrupacionId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser null");
  }

  @Test
  void tt02_agrupacionIdNuevoGeneraIdentificadoresDistintos() {
    assertThat(AgrupacionId.nuevo()).isNotEqualTo(AgrupacionId.nuevo());
  }

  @Test
  void tt02_agrupacionIdConElMismoUuidEsIgual() {
    UUID uuid = UUID.randomUUID();

    assertThat(new AgrupacionId(uuid)).isEqualTo(new AgrupacionId(uuid));
  }
}
