package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MateriaEnMallaTest {

  private static final CodigoMateria CODIGO = new CodigoMateria("9000001");

  @Test
  void tt02_materiaEnMallaGuardaSusDatosYSuSemestreSugerido() {
    MateriaEnMalla materia =
        MateriaEnMalla.enSemestre(
            CODIGO, "Cálculo diferencial", new Creditos(4), Obligatoriedad.OBLIGATORIA, 1);

    assertThat(materia.codigo()).isEqualTo(CODIGO);
    assertThat(materia.nombre()).isEqualTo("Cálculo diferencial");
    assertThat(materia.creditos()).isEqualTo(new Creditos(4));
    assertThat(materia.obligatoriedad()).isEqualTo(Obligatoriedad.OBLIGATORIA);
    assertThat(materia.semestreSugerido()).contains(1);
  }

  @Test
  void tt02_materiaEnMallaPuedeNoTenerSemestreSugerido() {
    MateriaEnMalla materia =
        MateriaEnMalla.sinSemestre(CODIGO, "Electiva", new Creditos(2), Obligatoriedad.OPTATIVA);

    assertThat(materia.semestreSugerido()).isEmpty();
  }

  @Test
  void tt02_materiaEnMallaRechazaUnSemestreSugeridoNoPositivo() {
    assertThatThrownBy(
            () ->
                MateriaEnMalla.enSemestre(
                    CODIGO, "Cálculo", new Creditos(4), Obligatoriedad.OBLIGATORIA, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("semestreSugerido debe ser mayor que cero");
  }

  @Test
  void tt02_materiaEnMallaRechazaDatosInvalidos() {
    Creditos tres = new Creditos(3);
    Optional<Integer> sin = Optional.empty();

    assertThatThrownBy(
            () -> new MateriaEnMalla(null, "M", tres, Obligatoriedad.OPTATIVA, sin, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(
            () -> new MateriaEnMalla(CODIGO, " ", tres, Obligatoriedad.OPTATIVA, sin, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
    assertThatThrownBy(
            () -> new MateriaEnMalla(CODIGO, "M", null, Obligatoriedad.OPTATIVA, sin, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditos no puede ser null");
    assertThatThrownBy(() -> new MateriaEnMalla(CODIGO, "M", tres, null, sin, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("obligatoriedad no puede ser null");
    assertThatThrownBy(
            () -> new MateriaEnMalla(CODIGO, "M", tres, Obligatoriedad.OPTATIVA, null, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("semestreSugerido no puede ser null");
  }

  @Test
  void tt02_lasFabricasDejanLaMateriaCursableDesdeElPrograma() {
    MateriaEnMalla sinSemestre =
        MateriaEnMalla.sinSemestre(CODIGO, "Electiva", new Creditos(2), Obligatoriedad.OPTATIVA);
    MateriaEnMalla enSemestre =
        MateriaEnMalla.enSemestre(
            CODIGO, "Cálculo", new Creditos(4), Obligatoriedad.OBLIGATORIA, 1);

    assertThat(sinSemestre.cursableDesdeElPrograma()).isTrue();
    assertThat(enSemestre.cursableDesdeElPrograma()).isTrue();
  }

  @Test
  void tt02_unaMateriaPuedeNoSerCursableDesdeElPrograma() {
    MateriaEnMalla materia =
        new MateriaEnMalla(
            CODIGO,
            "Física II",
            new Creditos(3),
            Obligatoriedad.OBLIGATORIA,
            Optional.of(2),
            false);

    assertThat(materia.cursableDesdeElPrograma()).isFalse();
  }
}
