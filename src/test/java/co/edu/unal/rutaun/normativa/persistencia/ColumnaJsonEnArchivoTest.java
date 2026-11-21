package co.edu.unal.rutaun.normativa.persistencia;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * TT-04-T6 (#145): la columna JSON en H2 en modo archivo. Usa una carpeta fija que se vacía antes
 * de arrancar el contexto, para que dos corridas seguidas no se pisen.
 */
@PruebaDeIntegracion
class ColumnaJsonEnArchivoTest extends ColumnaJsonIdaYVueltaBase {

  @DynamicPropertySource
  static void baseEnArchivo(DynamicPropertyRegistry registro) {
    try {
      Path carpeta = Path.of("target", "h2-columna-json");
      if (Files.exists(carpeta)) {
        try (Stream<Path> archivos = Files.walk(carpeta)) {
          archivos.sorted(Comparator.reverseOrder()).forEach(archivo -> archivo.toFile().delete());
        }
      }
      Files.createDirectories(carpeta);
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
