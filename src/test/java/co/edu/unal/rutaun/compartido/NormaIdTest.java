package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NormaIdTest {

  @Test
  void tt02_normaIdRechazaNull() {
    assertThatThrownBy(() -> new NormaId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser null");
  }

  @Test
  void tt02_normaIdNuevoGeneraIdentificadoresDistintos() {
    assertThat(NormaId.nuevo()).isNotEqualTo(NormaId.nuevo());
  }

  @Test
  void tt02_normaIdConElMismoUuidEsIgual() {
    UUID uuid = UUID.randomUUID();

    assertThat(new NormaId(uuid)).isEqualTo(new NormaId(uuid));
  }
}
