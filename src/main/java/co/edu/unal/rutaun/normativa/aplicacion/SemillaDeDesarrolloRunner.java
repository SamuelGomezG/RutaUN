package co.edu.unal.rutaun.normativa.aplicacion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Carga la semilla de Sistemas al arrancar con el perfil {@code dev}.
 *
 * <p>SOP-03 (#55) carga primero los programas. Después, UC07-HU03-T4 (#96) carga los archivos de
 * normas de {@code semilla/*.json}, omitiendo {@code programas.json}. Si un archivo es inválido, el
 * arranque falla; repetir el arranque no duplica programas ni normas.
 */
@Component
@Profile("dev")
public class SemillaDeDesarrolloRunner implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SemillaDeDesarrolloRunner.class);
  private static final String ARCHIVO_DE_PROGRAMAS = "semilla/programas.json";
  private static final String PATRON_DE_NORMAS = "classpath:semilla/*.json";
  private static final String NOMBRE_DE_PROGRAMAS = "programas.json";

  private final ImportacionDeProgramasService programas;
  private final ImportacionDeNormasService normas;

  /** Crea el runner con los servicios que importan programas y normas. */
  public SemillaDeDesarrolloRunner(
      ImportacionDeProgramasService programas, ImportacionDeNormasService normas) {
    this.programas = programas;
    this.normas = normas;
  }

  @Override
  public void run(ApplicationArguments args) {
    String json = leerArchivoDeProgramas();
    ResultadoDeCargaDeProgramas resultado = programas.importar(json);
    LOG.info(
        "Programas cargados: {} creados, {} ya existían.",
        resultado.creados(),
        resultado.yaExistentes());

    int creadas = 0;
    int existentes = 0;
    int versiones = 0;
    for (Resource recurso : recursosDeNormas()) {
      try (InputStream entrada = recurso.getInputStream()) {
        ResultadoDeImportacionDeNormas resultadoNorma = normas.importar(entrada);
        if (resultadoNorma.creada()) {
          creadas++;
        } else {
          existentes++;
        }
        versiones += resultadoNorma.versionesCreadas();
      } catch (IOException e) {
        throw new IllegalStateException(
            "No se pudo leer el archivo de normativa " + recurso.getFilename(), e);
      }
    }
    LOG.info(
        "Normas cargadas: {} creadas, {} ya existían; {} versiones nuevas.",
        creadas,
        existentes,
        versiones);
  }

  private static String leerArchivoDeProgramas() {
    try {
      return new ClassPathResource(ARCHIVO_DE_PROGRAMAS).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer " + ARCHIVO_DE_PROGRAMAS, e);
    }
  }

  private static List<Resource> recursosDeNormas() {
    try {
      Resource[] recursos =
          new PathMatchingResourcePatternResolver().getResources(PATRON_DE_NORMAS);
      return Arrays.stream(recursos)
          .filter(recurso -> !NOMBRE_DE_PROGRAMAS.equals(recurso.getFilename()))
          .sorted(Comparator.comparing(Resource::getFilename))
          .toList();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer la carpeta " + PATRON_DE_NORMAS, e);
    }
  }
}
