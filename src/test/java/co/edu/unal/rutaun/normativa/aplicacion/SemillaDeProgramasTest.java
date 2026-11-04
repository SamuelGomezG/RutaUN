package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica que el archivo real {@code semilla/programas.json} se carga.
 *
 * <p>SOP-03 (#55).
 */
@PruebaDeIntegracion
@Transactional
class SemillaDeProgramasTest {

  private final ImportacionDeProgramasService servicio;
  private final ProgramaJpaRepository programas;

  /** Recibe el servicio y el repositorio que Spring inyecta. */
  @Autowired
  SemillaDeProgramasTest(ImportacionDeProgramasService servicio, ProgramaJpaRepository programas) {
    this.servicio = servicio;
    this.programas = programas;
  }

  @Test
  void sop03_laSemillaDeSistemasSeCarga() throws IOException {
    String json =
        new ClassPathResource("semilla/programas.json").getContentAsString(StandardCharsets.UTF_8);

    ResultadoDeCargaDeProgramas resultado = servicio.importar(json);

    assertThat(resultado.creados() + resultado.yaExistentes()).isEqualTo(1);
    ProgramaEntidad sistemas = programas.findByCodigoSia("2879").orElseThrow();
    assertThat(sistemas.getNombre()).isEqualTo("Ingeniería de Sistemas y Computación");
    assertThat(sistemas.getFacultad()).isEqualTo("Ingeniería");
  }
}
