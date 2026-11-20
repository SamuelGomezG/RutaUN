package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import org.junit.jupiter.api.Test;

class MateriaTest {

  private static final CodigoMateria CODIGO = new CodigoMateria("2015867");
  private static final CodigoMateria OTRO_CODIGO = new CodigoMateria("2015868");

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
    Materia reconstituida = Materia.reconstituir(CODIGO, new Creditos(4));

    assertThat(reconstituida).isEqualTo(Materia.registrar(CODIGO, new Creditos(4)));
    assertThat(reconstituida.creditos()).isEqualTo(new Creditos(4));
  }

  @Test
  void tt02_dosMateriasConElMismoCodigoSonLaMisma() {
    Materia a = Materia.reconstituir(CODIGO, new Creditos(3));
    Materia b = Materia.reconstituir(CODIGO, new Creditos(4));

    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
  }

  @Test
  void tt02_dosMateriasConCodigoDistintoSonMateriasDistintas() {
    Materia a = Materia.reconstituir(CODIGO, new Creditos(3));
    Materia b = Materia.reconstituir(OTRO_CODIGO, new Creditos(3));

    assertThat(a).isNotEqualTo(b);
  }
}
