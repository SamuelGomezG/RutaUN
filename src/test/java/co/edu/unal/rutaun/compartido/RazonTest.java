package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RazonTest {

  private static final CodigoMateria A = new CodigoMateria("A");

  @Test
  void tt03_cadaRazonExigeSusDatos() {
    assertThatThrownBy(() -> new Razon.TerminoAprobado(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Razon.TerminoEnCurso(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Razon.TerminoPendiente(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Razon.TerminoQueYaNoRige(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Razon.UbicadoPorElPlan(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Razon.CreditosDeHabilitacion(null, new Creditos(40)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("aprobados no puede ser null");
    assertThatThrownBy(() -> new Razon.CreditosDeHabilitacion(new Creditos(39), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("umbral no puede ser null");
    assertThat(new Razon.NoCursableDesdeElPrograma()).isNotNull();
    assertThat(new Razon.TerminoAprobado(A).materia()).isEqualTo(A);
  }
}
