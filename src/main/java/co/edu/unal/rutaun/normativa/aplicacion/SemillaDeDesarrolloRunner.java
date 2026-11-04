package co.edu.unal.rutaun.normativa.aplicacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Carga la semilla de Sistemas al arrancar con el perfil {@code dev}.
 *
 * <p>SOP-03 (#55). Hoy solo carga los programas de {@code semilla/programas.json}. Las normas que
 * los citan en su alcance deben cargarse después de ellos: quien implemente UC07-HU03 (#41) agrega
 * su paso al final de {@link #run}. Si el archivo es inválido, el arranque falla; repetir el
 * arranque no duplica programas.
 */
@Component
@Profile("dev")
public class SemillaDeDesarrolloRunner implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SemillaDeDesarrolloRunner.class);
  private static final String ARCHIVO_DE_PROGRAMAS = "semilla/programas.json";

  private final ImportacionDeProgramasService programas;

  /** Crea el runner con el servicio que importa programas. */
  public SemillaDeDesarrolloRunner(ImportacionDeProgramasService programas) {
    this.programas = programas;
  }

  @Override
  public void run(ApplicationArguments args) {
    String json = leerArchivoDeProgramas();
    ResultadoDeCargaDeProgramas resultado = programas.importar(json);
    LOG.info(
        "Programas cargados: {} creados, {} ya existían.",
        resultado.creados(),
        resultado.yaExistentes());
  }

  private static String leerArchivoDeProgramas() {
    try {
      return new ClassPathResource(ARCHIVO_DE_PROGRAMAS).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer " + ARCHIVO_DE_PROGRAMAS, e);
    }
  }
}
