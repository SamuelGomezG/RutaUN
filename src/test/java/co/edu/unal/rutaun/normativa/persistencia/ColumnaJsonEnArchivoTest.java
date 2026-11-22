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
 * TT-04-T6 (#145): la columna JSON en H2 en modo archivo. Usa una carpeta fija con un subdirectorio
 * exclusivo por proceso, que se vacía antes de arrancar el contexto: dos corridas seguidas no se
 * pisan y dos ejecuciones simultáneas (por ejemplo, el IDE y Maven) no comparten la base.
 */
@PruebaDeIntegracion
class ColumnaJsonEnArchivoTest extends ColumnaJsonIdaYVueltaBase {

  @DynamicPropertySource
  static void baseEnArchivo(DynamicPropertyRegistry registro) {
    try {
      Path raiz = Path.of("target", "h2-columna-json");
      Files.createDirectories(raiz);
      Path carpeta = raiz.resolve("pid-" + ProcessHandle.current().pid());
      if (Files.exists(carpeta)) {
        try (Stream<Path> archivos = Files.walk(carpeta)) {
          for (Path archivo : archivos.sorted(Comparator.reverseOrder()).toList()) {
            Files.deleteIfExists(archivo);
            if (Files.exists(archivo)) {
              throw new IOException(
                  "no se pudo vaciar la carpeta de la base: " + archivo.toAbsolutePath());
            }
          }
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
