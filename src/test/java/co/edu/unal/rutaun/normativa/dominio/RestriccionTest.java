package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import org.junit.jupiter.api.Test;

class RestriccionTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");
  private static final ExpresionDeMaterias A_Y_B_O_C =
      new ExpresionDeMaterias.Y(
          new ExpresionDeMaterias.MateriaDelPrograma(A),
          new ExpresionDeMaterias.O(
              new ExpresionDeMaterias.MateriaDelPrograma(B),
              new ExpresionDeMaterias.MateriaDelPrograma(C)));

  @Test
  void tt03_seConstruyeUnPrerrequisitoConUnaExpresionConYConO() {
    Prerrequisito prerrequisito = new Prerrequisito(A, A_Y_B_O_C);

    assertThat(prerrequisito.materia()).isEqualTo(A);
    assertThat(prerrequisito.expresion()).isEqualTo(A_Y_B_O_C);
    assertThat(prerrequisito).isInstanceOf(Restriccion.class);
  }

  @Test
  void tt03_seConstruyeUnCorrequisitoConLaMismaExpresion() {
    Correquisito correquisito = new Correquisito(B, A_Y_B_O_C);

    assertThat(correquisito.materia()).isEqualTo(B);
    assertThat(correquisito.expresion()).isEqualTo(A_Y_B_O_C);
    assertThat(correquisito).isInstanceOf(Restriccion.class);
  }

  @Test
  void tt03_seConstruyeUnaReglaDeHabilitacionDeUnComponente() {
    ReglaDeHabilitacion regla =
        new ReglaDeHabilitacion(A, MedidaDeCreditos.deComponente("Disciplinar"), new Creditos(40));

    assertThat(regla.materia()).isEqualTo(A);
    assertThat(regla.medida()).isEqualTo(MedidaDeCreditos.deComponente("Disciplinar"));
    assertThat(regla.umbral()).isEqualTo(new Creditos(40));
    assertThat(regla).isInstanceOf(Restriccion.class);
  }

  @Test
  void tt03_seConstruyeUnaReglaDeHabilitacionDelPrograma() {
    ReglaDeHabilitacion regla =
        new ReglaDeHabilitacion(A, MedidaDeCreditos.delPrograma(), new Creditos(80));

    assertThat(regla.medida().esDelPrograma()).isTrue();
    assertThat(regla.umbral()).isEqualTo(new Creditos(80));
  }

  @Test
  void tt03_lasRestriccionesRechazanDatosObligatoriosNulos() {
    assertThatThrownBy(() -> new Prerrequisito(null, A_Y_B_O_C))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Prerrequisito(A, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expresion no puede ser null");
    assertThatThrownBy(() -> new Correquisito(null, A_Y_B_O_C))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new Correquisito(A, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expresion no puede ser null");
    assertThatThrownBy(
            () -> new ReglaDeHabilitacion(null, MedidaDeCreditos.delPrograma(), new Creditos(40)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new ReglaDeHabilitacion(A, null, new Creditos(40)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("medida no puede ser null");
    assertThatThrownBy(() -> new ReglaDeHabilitacion(A, MedidaDeCreditos.delPrograma(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("umbral no puede ser null");
  }

  @Test
  void tt03_laMedidaDeCreditosRechazaUnComponenteVacio() {
    assertThatThrownBy(() -> MedidaDeCreditos.deComponente(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("componente no puede ser vacío");
  }
}
