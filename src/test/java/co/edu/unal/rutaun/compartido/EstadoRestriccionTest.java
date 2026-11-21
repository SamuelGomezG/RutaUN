package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class EstadoRestriccionTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");

  @Test
  void tt03_elEstadoGuardaLaCertezaYLasRazones() {
    EstadoRestriccion estado =
        new EstadoRestriccion(
            EstadoRestriccion.Certeza.EN_CURSO, List.of(new Razon.TerminoEnCurso(A)));

    assertThat(estado.certeza()).isEqualTo(EstadoRestriccion.Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly(new Razon.TerminoEnCurso(A));
  }

  @Test
  void tt03_seConstruyenLosCuatroEstadosPorCerteza() {
    assertThat(EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A)).certeza())
        .isEqualTo(EstadoRestriccion.Certeza.CUMPLIDA);
    assertThat(EstadoRestriccion.enCurso(new Razon.TerminoEnCurso(A)).certeza())
        .isEqualTo(EstadoRestriccion.Certeza.EN_CURSO);
    assertThat(EstadoRestriccion.incierta(new Razon.TerminoQueYaNoRige(A)).certeza())
        .isEqualTo(EstadoRestriccion.Certeza.INCIERTA);
    assertThat(EstadoRestriccion.pendiente(new Razon.TerminoPendiente(A)).certeza())
        .isEqualTo(EstadoRestriccion.Certeza.PENDIENTE);
  }

  @Test
  void tt03_elEstadoNoDejaModificarSusRazones() {
    EstadoRestriccion estado = EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A));

    assertThatThrownBy(() -> estado.razones().add(new Razon.TerminoAprobado(B)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt03_unaYDeTerminosCumplidosConservaSusRazones() {
    EstadoRestriccion estado =
        EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A))
            .y(EstadoRestriccion.cumplida(new Razon.TerminoAprobado(B)));

    assertThat(estado.certeza()).isEqualTo(EstadoRestriccion.Certeza.CUMPLIDA);
    assertThat(estado.razones())
        .containsExactly(new Razon.TerminoAprobado(A), new Razon.TerminoAprobado(B));
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
    EstadoRestriccion estado = EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A));

    assertThatThrownBy(() -> estado.y(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
    assertThatThrownBy(() -> estado.o(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
  }

  private static EstadoRestriccion estado(EstadoRestriccion.Certeza certeza) {
    return new EstadoRestriccion(certeza, List.of(new Razon.TerminoAprobado(A)));
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
                    EstadoRestriccion.Certeza.CUMPLIDA, java.util.Arrays.asList((Razon) null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("razones no puede tener elementos null");
  }
}
