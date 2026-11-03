package co.edu.unal.rutaun.spikes.motor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Resuelve datos y salidas tanto desde la raíz del repositorio como desde el spike. */
public final class Rutas {

  private static final DateTimeFormatter MARCA =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

  private Rutas() {}

  private static final List<Path> CANDIDATOS =
      List.of(Path.of("datos"), Path.of("spikes", "tt-05-motor", "datos"));

  public static Path datos() {
    for (Path candidato : CANDIDATOS) {
      if (Files.isDirectory(candidato)) {
        return candidato;
      }
    }
    throw new IllegalStateException(
        "No encuentro el directorio datos/; ejecuta desde la raíz o desde spikes/tt-05-motor");
  }

  public static Path mediciones(boolean publicar) {
    Path raiz = datos().toAbsolutePath();
    if ("datos".equals(raiz.getFileName().toString())) {
      raiz = raiz.getParent();
    }
    Path base = raiz.resolve(publicar ? "resultados" : "target/mediciones");
    try {
      Files.createDirectories(base);
    } catch (IOException e) {
      throw new IllegalStateException("No puedo crear " + base, e);
    }
    return base.resolve("mediciones-" + MARCA.format(LocalDateTime.now()) + ".csv");
  }
}
