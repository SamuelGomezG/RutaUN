package co.edu.unal.rutaun.spikes.motor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RutasTest {

  @Test
  void tt05_dosCorridasNuncaComparteNiPisanElMismoCsv() throws IOException {
    Path primero = Rutas.mediciones(false);
    Path segundo = Rutas.mediciones(false);
    try {
      assertThat(segundo).isNotEqualTo(primero);
      assertThat(primero).exists();
      assertThat(segundo).exists();
      assertThat(primero.getFileName().toString()).startsWith("mediciones-").endsWith(".csv");
    } finally {
      Files.deleteIfExists(primero);
      Files.deleteIfExists(segundo);
    }
  }
}
