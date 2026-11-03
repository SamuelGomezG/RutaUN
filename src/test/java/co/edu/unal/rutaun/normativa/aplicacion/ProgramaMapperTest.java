package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.Programa;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link Programa} (dominio, entidad, base, entidad, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
@PruebaDeIntegracion
class ProgramaMapperTest {

  @Autowired private ProgramaJpaRepository programas;

  private UUID programaId;

  /** Borra lo que guardó la prueba, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    if (programaId != null) {
      programas.deleteById(programaId);
    }
  }

  @Test
  void tt04_programaIdaYVuelta() {
    Programa original =
        Programa.registrar(
            "Ingeniería de Sistemas y Computación", "2879", new Facultad("Ingeniería"));
    programaId = original.id().valor();

    programas.save(ProgramaMapper.aEntidad(original));
    ProgramaEntidad leida = programas.findById(programaId).orElseThrow();
    Programa reconstruido = ProgramaMapper.aDominio(leida);

    // Programa.equals solo compara el id; la comparación recursiva revisa todos los campos.
    assertThat(reconstruido).usingRecursiveComparison().isEqualTo(original);
  }

  @Test
  void tt04_programaConNombreEnBlancoNoSeReconstituye() {
    ProgramaEntidad invalida = new ProgramaEntidad(UUID.randomUUID(), "   ", "2879", "Ingeniería");

    assertThatThrownBy(() -> ProgramaMapper.aDominio(invalida))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nombre");
  }
}
