package co.edu.unal.rutaun.compartido;

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
  void tt03_unaYDeTerminosCumplidosConservaSusRazones() {
    EstadoRestriccion estado =
        EstadoRestriccion.cumplida("A está aprobada")
            .y(EstadoRestriccion.cumplida("B está aprobada"));

    assertThat(estado.certeza()).isEqualTo(EstadoRestriccion.Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly("A está aprobada", "B está aprobada");
  }

  @Test
  void tt03_todasLasCombinacionesDeDosEstadosSiguenElOrdenDeCerteza() {
    for (EstadoRestriccion.Certeza izquierda : EstadoRestriccion.Certeza.values()) {
      for (EstadoRestriccion.Certeza derecha : EstadoRestriccion.Certeza.values()) {
        EstadoRestriccion.Certeza peor =
            izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
        EstadoRestriccion.Certeza mejor =
            izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;

        assertThat(estado(izquierda).y(estado(derecha)).certeza()).isEqualTo(peor);
        assertThat(estado(izquierda).o(estado(derecha)).certeza()).isEqualTo(mejor);
      }
    }
  }

  @Test
  void tt03_yYORechazanUnEstadoNulo() {
    EstadoRestriccion estado = EstadoRestriccion.cumplida("A está aprobada");

    assertThatThrownBy(() -> estado.y(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
    assertThatThrownBy(() -> estado.o(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
  }

  private static EstadoRestriccion estado(EstadoRestriccion.Certeza certeza) {
    return new EstadoRestriccion(certeza, List.of(certeza.name()));
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
