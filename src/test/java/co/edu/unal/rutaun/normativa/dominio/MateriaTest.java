package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import org.junit.jupiter.api.Test;

class MateriaTest {

  private static final CodigoMateria CODIGO = new CodigoMateria("2015867");

  @Test
  void tt02_materiaRegistrarGuardaCodigoYCreditos() {
    Materia materia = Materia.registrar(CODIGO, new Creditos(3));

    assertThat(materia.codigo()).isEqualTo(CODIGO);
    assertThat(materia.creditos()).isEqualTo(new Creditos(3));
  }

  @Test
  void tt02_materiaRegistrarRechazaCodigoOCreditosNull() {
    assertThatThrownBy(() -> Materia.registrar(null, new Creditos(3)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(() -> Materia.registrar(CODIGO, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditos no puede ser null");
  }

  @Test
  void tt02_materiaReconstituirDevuelveLaMismaMateriaQueRegistrar() {
    assertThat(Materia.reconstituir(CODIGO, new Creditos(3)))
        .isEqualTo(Materia.registrar(CODIGO, new Creditos(3)));
  }

  @Test
  void tt02_materiaCambiarLosCreditosCreaOtraMateria() {
    Materia original = Materia.registrar(CODIGO, new Creditos(3));

    Materia otra = original.conCreditos(new Creditos(4));

    assertThat(otra).isNotEqualTo(original);
    assertThat(otra.codigo()).isEqualTo(CODIGO);
    assertThat(otra.creditos()).isEqualTo(new Creditos(4));
    assertThat(original.creditos()).isEqualTo(new Creditos(3));
  }

  @Test
  void tt02_materiaConLosMismosCreditosEsLaMismaMateria() {
    Materia original = Materia.registrar(CODIGO, new Creditos(3));

    assertThat(original.conCreditos(new Creditos(3)))
        .isEqualTo(original)
        .hasSameHashCodeAs(original);
  }

  @Test
  void tt02_materiaConCreditosRechazaNull() {
    Materia original = Materia.registrar(CODIGO, new Creditos(3));

    assertThatThrownBy(() -> original.conCreditos(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditos no puede ser null");
  }
}
