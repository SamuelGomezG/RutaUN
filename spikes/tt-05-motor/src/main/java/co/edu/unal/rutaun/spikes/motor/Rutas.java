package co.edu.unal.rutaun.spikes.motor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Resuelve datos y resultados tanto desde la raíz del repositorio como desde el spike. */
public final class Rutas {

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

  public static Path resultados() {
    for (Path candidato :
        List.of(Path.of("resultados"), Path.of("spikes", "tt-05-motor", "resultados"))) {
      if (Files.isDirectory(candidato)) {
        return candidato;
      }
    }
    try {
      Path nuevo = Path.of("resultados");
      Files.createDirectories(nuevo);
      return nuevo;
    } catch (IOException e) {
      throw new IllegalStateException("No puedo crear resultados/", e);
    }
  }
}
