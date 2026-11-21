package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import java.util.List;
import org.junit.jupiter.api.Test;

/** UC05 paso 4 y ext. 4a: cómo se escribe lo que satisface una restricción. */
class ExpresionVistaTest {

  private static ExpresionVista.Materia materia(String nombre) {
    return new ExpresionVista.Materia(new CodigoMateria("cod-" + nombre), nombre);
  }

  @Test
  void uc05_paso4_unaMateriaSeEscribeConSuNombre() {
    assertThat(materia("Estructuras de Datos").redaccion()).isEqualTo("Estructuras de Datos");
  }

  @Test
  void uc05_paso4_dosTerminosSeUnenConYYConO() {
    ExpresionVista todas = new ExpresionVista.TodasLas(List.of(materia("A"), materia("B")));
    ExpresionVista alguna = new ExpresionVista.AlgunaDe(List.of(materia("A"), materia("B")));

    assertThat(todas.redaccion()).isEqualTo("A y B");
    assertThat(alguna.redaccion()).isEqualTo("A o B");
  }

  @Test
  void uc05_ext4a_variosTerminosSeSeparanConComasYElUltimoVaConElConector() {
    ExpresionVista todas =
        new ExpresionVista.TodasLas(List.of(materia("A"), materia("B"), materia("C")));
    ExpresionVista alguna =
        new ExpresionVista.AlgunaDe(List.of(materia("A"), materia("B"), materia("C")));

    assertThat(todas.redaccion()).isEqualTo("A, B y C");
    assertThat(alguna.redaccion()).isEqualTo("A, B o C");
  }

  @Test
  void uc05_ext4a_unaAlternativaDentroDeUnaConjuncionVaEntreParentesis() {
    ExpresionVista expresion =
        new ExpresionVista.TodasLas(
            List.of(
                materia("Fundamentos de Mecánica"),
                new ExpresionVista.AlgunaDe(
                    List.of(
                        materia("Cálculo Integral"),
                        materia("Cálculo integral en una variable")))));

    assertThat(expresion.redaccion())
        .isEqualTo(
            "Fundamentos de Mecánica y (Cálculo Integral o Cálculo integral en una variable)");
  }

  @Test
  void uc05_ext4a_unaConjuncionDentroDeUnaDisyuncionVaEntreParentesis() {
    ExpresionVista expresion =
        new ExpresionVista.AlgunaDe(
            List.of(
                materia("A"), new ExpresionVista.TodasLas(List.of(materia("B"), materia("C")))));

    assertThat(expresion.redaccion()).isEqualTo("A o (B y C)");
  }

  @Test
  void uc05_ext4a_unaConjuncionOUnaDisyuncionExigeAlMenosDosTerminos() {
    assertThatThrownBy(() -> new ExpresionVista.TodasLas(List.of(materia("A"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ExpresionVista.AlgunaDe(List.of(materia("A"))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void uc05_ext4c_unaCondicionSeEscribeConElTextoDeLaRegla() {
    ExpresionVista condicion =
        new ExpresionVista.Condicion("Materia E: haber aprobado 40 créditos del componente.");

    assertThat(condicion.redaccion())
        .isEqualTo("Materia E: haber aprobado 40 créditos del componente.");
  }

  @Test
  void uc05_ext4c_unaCondicionSinTextoSeRechaza() {
    assertThatThrownBy(() -> new ExpresionVista.Condicion(" "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
