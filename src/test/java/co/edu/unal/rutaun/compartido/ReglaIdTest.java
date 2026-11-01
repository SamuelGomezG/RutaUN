package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReglaIdTest {

  @Test
  void tt02_reglaIdRechazaNull() {
    assertThatThrownBy(() -> new ReglaId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser null");
  }

  @Test
  void tt02_reglaIdNuevoGeneraIdentificadoresDistintos() {
    assertThat(ReglaId.nuevo()).isNotEqualTo(ReglaId.nuevo());
  }

  @Test
  void tt02_reglaIdConElMismoUuidEsIgual() {
    UUID uuid = UUID.randomUUID();

    assertThat(new ReglaId(uuid)).isEqualTo(new ReglaId(uuid));
  }
}
