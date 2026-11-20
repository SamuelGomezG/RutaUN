package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReglaVistaTest {

  private static final ReglaId ID = new ReglaId(UUID.randomUUID());

  @Test
  void uc06_ext5b_unaReglaPuedeLlevarLasFechasDeSuVigencia() {
    ReglaVista regla =
        new ReglaVista(
            ID,
            "Regla de prueba.",
            Optional.empty(),
            EstadoRegla.YA_NO_RIGE,
            false,
            ClasificacionRegla.DEL_PLAN,
            Optional.of(LocalDate.of(2020, 1, 1)),
            Optional.of(LocalDate.of(2025, 12, 31)));

    assertThat(regla.aplicaDesde()).contains(LocalDate.of(2020, 1, 1));
    assertThat(regla.vigenteHasta()).contains(LocalDate.of(2025, 12, 31));
  }

  @Test
  void uc06_ext5b_sinFechasConocidasLaVigenciaQuedaVacia() {
    ReglaVista regla =
        new ReglaVista(
            ID,
            "Regla de prueba.",
            Optional.empty(),
            EstadoRegla.VIGENTE,
            false,
            ClasificacionRegla.DEL_PLAN);

    assertThat(regla.aplicaDesde()).isEmpty();
    assertThat(regla.vigenteHasta()).isEmpty();
  }

  @Test
  void uc06_ext5b_laVigenciaNoPuedeSerNula() {
    assertThatThrownBy(
            () ->
                new ReglaVista(
                    ID,
                    "Regla de prueba.",
                    Optional.empty(),
                    EstadoRegla.VIGENTE,
                    false,
                    ClasificacionRegla.DEL_PLAN,
                    null,
                    Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("aplicaDesde no puede ser null");
    assertThatThrownBy(
            () ->
                new ReglaVista(
                    ID,
                    "Regla de prueba.",
                    Optional.empty(),
                    EstadoRegla.VIGENTE,
                    false,
                    ClasificacionRegla.DEL_PLAN,
                    Optional.empty(),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigenteHasta no puede ser null");
  }
}
