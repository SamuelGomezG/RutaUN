package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExpresionDeMateriasTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");

  @Test
  void tt03_seConstruyeLaExpresionAConBAlternativaC() {
    ExpresionDeMaterias expresion =
        new ExpresionDeMaterias.Y(
            new ExpresionDeMaterias.MateriaDelPrograma(A),
            new ExpresionDeMaterias.O(
                new ExpresionDeMaterias.MateriaDelPrograma(B),
                new ExpresionDeMaterias.MateriaDelPrograma(C)));

    assertThat(expresion).isInstanceOf(ExpresionDeMaterias.Y.class);
    ExpresionDeMaterias.Y conjuncion = (ExpresionDeMaterias.Y) expresion;
    assertThat(conjuncion.izquierda()).isEqualTo(new ExpresionDeMaterias.MateriaDelPrograma(A));
    assertThat(conjuncion.derecha()).isInstanceOf(ExpresionDeMaterias.O.class);
    ExpresionDeMaterias.O disyuncion = (ExpresionDeMaterias.O) conjuncion.derecha();
    assertThat(disyuncion.izquierda()).isEqualTo(new ExpresionDeMaterias.MateriaDelPrograma(B));
    assertThat(disyuncion.derecha()).isEqualTo(new ExpresionDeMaterias.MateriaDelPrograma(C));
  }

  @Test
  void tt03_seConstruyeUnaMateriaExternaConNombreTipoYCodigoOpcional() {
    ExpresionDeMaterias.MateriaExterna externa =
        new ExpresionDeMaterias.MateriaExterna(
            "Matemáticas Básicas", Optional.empty(), TipoDeMateriaExterna.NIVELACION);

    assertThat(externa.nombre()).isEqualTo("Matemáticas Básicas");
    assertThat(externa.codigo()).isEmpty();
    assertThat(externa.tipo()).isEqualTo(TipoDeMateriaExterna.NIVELACION);
  }

  @Test
  void tt03_laMateriaExternaPuedeTenerCodigo() {
    ExpresionDeMaterias.MateriaExterna externa =
        new ExpresionDeMaterias.MateriaExterna(
            "Electrónica Análoga I",
            Optional.of(new CodigoMateria("2016699")),
            TipoDeMateriaExterna.OTRO_PROGRAMA);

    assertThat(externa.codigo()).contains(new CodigoMateria("2016699"));
    assertThat(externa.tipo()).isEqualTo(TipoDeMateriaExterna.OTRO_PROGRAMA);
  }

  @Test
  void tt03_lasExpresionesRechazanTerminosObligatoriosNulos() {
    assertThatThrownBy(() -> new ExpresionDeMaterias.MateriaDelPrograma(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    null, Optional.empty(), TipoDeMateriaExterna.NIVELACION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    "Matemáticas Básicas", null, TipoDeMateriaExterna.NIVELACION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    "Matemáticas Básicas", Optional.empty(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(
            () -> new ExpresionDeMaterias.Y(null, new ExpresionDeMaterias.MateriaDelPrograma(A)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("izquierda no puede ser null");
    assertThatThrownBy(
            () -> new ExpresionDeMaterias.O(new ExpresionDeMaterias.MateriaDelPrograma(A), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("derecha no puede ser null");
  }
}
