package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class EstadoRestriccionTest {

  @Test
  void tt03_elEstadoGuardaLaCertezaYLasRazones() {
    EstadoRestriccion estado =
        new EstadoRestriccion(EstadoRestriccion.Certeza.EN_CURSO, List.of("A está en curso"));

    assertThat(estado.certeza()).isEqualTo(EstadoRestriccion.Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly("A está en curso");
  }

  @Test
  void tt03_seConstruyenLosCuatroEstadosPorCerteza() {
    assertThat(EstadoRestriccion.cumplida("A aprobada").certeza())
        .isEqualTo(EstadoRestriccion.Certeza.CUMPLIDA);
    assertThat(EstadoRestriccion.enCurso("A en curso").certeza())
        .isEqualTo(EstadoRestriccion.Certeza.EN_CURSO);
    assertThat(EstadoRestriccion.incierta("A ya no rige").certeza())
        .isEqualTo(EstadoRestriccion.Certeza.INCIERTA);
    assertThat(EstadoRestriccion.pendiente("A pendiente").certeza())
        .isEqualTo(EstadoRestriccion.Certeza.PENDIENTE);
  }

  @Test
  void tt03_elEstadoNoDejaModificarSusRazones() {
    EstadoRestriccion estado = EstadoRestriccion.cumplida("A aprobada");

    assertThatThrownBy(() -> estado.razones().add("otra razón"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt03_elEstadoRechazaCertezaRazonesNulas() {
    assertThatThrownBy(() -> new EstadoRestriccion(null, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("certeza no puede ser null");
    assertThatThrownBy(() -> new EstadoRestriccion(EstadoRestriccion.Certeza.CUMPLIDA, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("razones no puede ser null");
    assertThatThrownBy(
            () ->
                new EstadoRestriccion(
                    EstadoRestriccion.Certeza.CUMPLIDA, java.util.Arrays.asList((String) null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("razones no puede tener elementos null");
  }
}
