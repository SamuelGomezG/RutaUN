package co.edu.unal.rutaun.normativa.persistencia;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * TT-04-T2 (#90): ida y vuelta de la columna JSON en H2 en modo archivo. Usa un archivo nuevo en
 * {@code target/h2-t2/} para no tocar {@code data/}.
 */
@PruebaDeIntegracion
class ColumnaJsonEnArchivoTest extends ColumnaJsonIdaYVueltaBase {

  @DynamicPropertySource
  static void baseEnArchivo(DynamicPropertyRegistry registro) {
    try {
      Path carpeta =
          Files.createDirectories(Path.of("target", "h2-t2", UUID.randomUUID().toString()));
      String ruta = carpeta.resolve("rutaun").toAbsolutePath().toString().replace('\\', '/');
      registro.add("spring.datasource.url", () -> "jdbc:h2:file:" + ruta);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  protected String prefijoDeUrlEsperado() {
    return "jdbc:h2:file:";
  }
}
