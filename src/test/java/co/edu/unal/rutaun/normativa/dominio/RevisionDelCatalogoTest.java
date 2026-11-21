package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RevisionDelCatalogoTest {

  private static final CodigoMateria CODIGO = new CodigoMateria("2028837");
  private static final NodoDeMalla CONTENEDOR =
      MallaDePrueba.nodo(MallaDePrueba.pequena(), "Fundamentación");

  @Test
  void tt04_unaMateriaNuevaSeRegistraConSusCreditos() {
    RevisionDelCatalogo revision =
        RevisionDelCatalogo.de(List.of(reglaDeMateria(CODIGO.valor(), 4)), List.of());

    assertThat(revision.motivos()).isEmpty();
    assertThat(revision.nuevas()).hasSize(1);
    assertThat(revision.nuevas().get(0).codigo()).isEqualTo(CODIGO);
    assertThat(revision.nuevas().get(0).creditos()).isEqualTo(new Creditos(4));
  }

  @Test
  void tt04_unaMateriaEnDosAgrupacionesSeRegistraUnaVez() {
    RevisionDelCatalogo revision =
        RevisionDelCatalogo.de(
            List.of(reglaDeMateria(CODIGO.valor(), 4), reglaDeMateria(CODIGO.valor(), 4)),
            List.of());

    assertThat(revision.motivos()).isEmpty();
    assertThat(revision.nuevas()).hasSize(1);
    assertThat(revision.nuevas().get(0).codigo()).isEqualTo(CODIGO);
  }

  @Test
  void tt04_unaMateriaYaEnElCatalogoConLosMismosCreditosNoSeRegistra() {
    RevisionDelCatalogo revision =
        RevisionDelCatalogo.de(
            List.of(reglaDeMateria(CODIGO.valor(), 4)),
            List.of(Materia.registrar(CODIGO, new Creditos(4))));

    assertThat(revision.nuevas()).isEmpty();
    assertThat(revision.motivos()).isEmpty();
  }

  @Test
  void uc07_ext6d_otrosCreditosQueLosDelCatalogoNoSeAceptan() {
    RevisionDelCatalogo revision =
        RevisionDelCatalogo.de(
            List.of(reglaDeMateria(CODIGO.valor(), 4)),
            List.of(Materia.registrar(CODIGO, new Creditos(3))));

    assertThat(revision.nuevas()).isEmpty();
    assertThat(revision.motivos()).hasSize(1);
    assertThat(revision.motivos().get(0))
        .contains(CODIGO.valor())
        .contains("le da 4")
        .contains("tiene 3")
        .contains("catálogo");
  }

  @Test
  void uc07_ext6d_dosCreditosEnLaMismaNormaNoSeAceptan() {
    RevisionDelCatalogo revision =
        RevisionDelCatalogo.de(
            List.of(reglaDeMateria(CODIGO.valor(), 5), reglaDeMateria(CODIGO.valor(), 4)),
            List.of());

    assertThat(revision.nuevas()).hasSize(1);
    assertThat(revision.motivos()).hasSize(1);
    assertThat(revision.motivos().get(0))
        .contains(CODIGO.valor())
        .contains("le da 4")
        .contains("en esta norma tiene 5");
  }

  @Test
  void tt04_lasReglasQueNoSonDeMateriaNoTocanElCatalogo() {
    Regla otra =
        Regla.registrar(
            "Regla informativa",
            "Enunciado de prueba",
            new Fuente(NormaId.nuevo(), "art. 1"),
            Optional.empty(),
            ClasificacionRegla.INFORMATIVA,
            Optional.empty(),
            new DatosDeRegla.DeOtraCategoria());

    RevisionDelCatalogo revision = RevisionDelCatalogo.de(List.of(otra), List.of());

    assertThat(revision.nuevas()).isEmpty();
    assertThat(revision.motivos()).isEmpty();
  }

  private static Regla reglaDeMateria(String codigo, int creditos) {
    MateriaEnMalla materia =
        MateriaEnMalla.sinSemestre(
            new CodigoMateria(codigo),
            "Materia " + codigo,
            new Creditos(creditos),
            Obligatoriedad.OBLIGATORIA);
    return Regla.registrar(
        codigo + " Materia " + codigo,
        "Enunciado de prueba",
        new Fuente(NormaId.nuevo(), "art. 2"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        Optional.empty(),
        new DatosDeRegla.DeMateria(materia, CONTENEDOR));
  }
}
