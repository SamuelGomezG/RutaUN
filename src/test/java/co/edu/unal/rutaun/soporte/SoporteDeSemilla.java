package co.edu.unal.rutaun.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.aplicacion.SemillaDeDesarrolloRunner;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Descubre los archivos de la semilla de normativa para las pruebas (TT-04-T4, #133).
 *
 * <p>Centraliza el patrón de {@code semilla/}, la exclusión de {@code programas.json} y el orden
 * alfabético que usa {@link SemillaDeDesarrolloRunner}. Antes de entregar los archivos comprueba
 * que {@code semilla/} se resuelva solo desde la salida de producción, para que una copia en {@code
 * src/test/resources/semilla/} no oculte la semilla real.
 */
public final class SoporteDeSemilla {

  private static final String PATRON = "classpath:semilla/*.json";
  private static final String PROGRAMAS = "programas.json";
  private static final String PREFIJO_DE_PRUEBA = "datos-de-prueba-";

  private SoporteDeSemilla() {}

  /** Los archivos de normas de la semilla, sin {@code programas.json}, en orden alfabético. */
  public static List<Resource> recursosDeNormas() throws IOException {
    comprobarOrigen();
    return Arrays.stream(new PathMatchingResourcePatternResolver().getResources(PATRON))
        .filter(recurso -> !PROGRAMAS.equals(recurso.getFilename()))
        .sorted(Comparator.comparing(Resource::getFilename))
        .toList();
  }

  /** Los archivos de datos de prueba, reconocidos por el prefijo de su nombre. */
  public static List<Resource> archivosDePrueba() throws IOException {
    return recursosDeNormas().stream()
        .filter(recurso -> esDePrueba(recurso.getFilename()))
        .toList();
  }

  /** Si el archivo es de datos de prueba, por el prefijo de su nombre. */
  public static boolean esDePrueba(String nombre) {
    return nombre.startsWith(PREFIJO_DE_PRUEBA);
  }

  /** Exige que {@code semilla/} se resuelva solo desde la salida de producción. */
  public static void comprobarOrigen() throws IOException {
    URL produccion =
        SemillaDeDesarrolloRunner.class.getProtectionDomain().getCodeSource().getLocation();
    List<URL> raices =
        Collections.list(Thread.currentThread().getContextClassLoader().getResources("semilla/"));

    assertThat(raices)
        .as(
            "semilla/ debe resolverse solo desde la salida de producción; "
                + "borra src/test/resources/semilla/ y corre ./mvnw clean")
        .isNotEmpty()
        .allSatisfy(raiz -> assertThat(raiz.getPath()).startsWith(produccion.getPath()));
  }
}
