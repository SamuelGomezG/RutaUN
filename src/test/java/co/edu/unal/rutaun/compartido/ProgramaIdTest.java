package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProgramaIdTest {

  @Test
  void tt02_programaIdRechazaNull() {
    assertThatThrownBy(() -> new ProgramaId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser null");
  }

  @Test
  void tt02_programaIdNuevoGeneraIdentificadoresDistintos() {
    assertThat(ProgramaId.nuevo()).isNotEqualTo(ProgramaId.nuevo());
  }

  @Test
  void tt02_programaIdConElMismoUuidEsIgual() {
    UUID uuid = UUID.randomUUID();

    assertThat(new ProgramaId(uuid)).isEqualTo(new ProgramaId(uuid));
  }
}
