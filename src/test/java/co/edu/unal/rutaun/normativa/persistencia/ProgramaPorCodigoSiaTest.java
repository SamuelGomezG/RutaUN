package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica la búsqueda de programas por código SIA.
 *
 * <p>SOP-03 (#55).
 */
@PruebaDeIntegracion
@Transactional
class ProgramaPorCodigoSiaTest {

  private final ProgramaJpaRepository programas;

  /** Recibe el repositorio que Spring inyecta. */
  @Autowired
  ProgramaPorCodigoSiaTest(ProgramaJpaRepository programas) {
    this.programas = programas;
  }

  @Test
  void sop03_buscarPorCodigoSiaEncuentraElProgramaGuardado() {
    UUID id = UUID.randomUUID();
    programas.saveAndFlush(new ProgramaEntidad(id, "Programa de prueba", "T-001", "Ingeniería"));

    assertThat(programas.findByCodigoSia("T-001")).map(ProgramaEntidad::getId).contains(id);
  }

  @Test
  void sop03_buscarPorCodigoSiaDevuelveVacioSiNoExiste() {
    assertThat(programas.findByCodigoSia("NO-EXISTE")).isEmpty();
  }
}
