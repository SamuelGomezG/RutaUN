package co.edu.unal.rutaun.arquitectura;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;

/** #143: los repositorios de raíces del historial solo agregan y leen. */
class RepositoriosTest {

  @Test
  void tt04_losRepositoriosDeSoloAgregarNoBorran() {
    for (Class<?> repositorio :
        List.of(NormaRepository.class, VersionDelConsolidadoRepository.class)) {
      for (Method metodo : repositorio.getMethods()) {
        assertThat(metodo.getName())
            .as("%s.%s no debe borrar", repositorio.getSimpleName(), metodo.getName())
            .doesNotStartWith("delete")
            .doesNotStartWith("remove");
        assertThat(metodo.isAnnotationPresent(Modifying.class))
            .as("%s.%s no debe modificar", repositorio.getSimpleName(), metodo.getName())
            .isFalse();
      }
    }
  }
}
