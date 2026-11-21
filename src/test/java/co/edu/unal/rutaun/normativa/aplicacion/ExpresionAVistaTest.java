package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.ExpresionVista;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** UC05 paso 4 y ext. 4a y 4k: cómo se convierte la expresión de una restricción en su vista. */
class ExpresionAVistaTest {

  private static ExpresionDeMaterias.MateriaDelPrograma a(String codigo) {
    return new ExpresionDeMaterias.MateriaDelPrograma(new CodigoMateria(codigo));
  }

  private static ExpresionVista.Materia vista(String codigo) {
    return new ExpresionVista.Materia(new CodigoMateria(codigo), "Materia " + codigo);
  }

  private static ExpresionDeMaterias externa(TipoMateriaExterna tipo) {
    return new ExpresionDeMaterias.MateriaExterna("Externa", Optional.empty(), tipo);
  }

  private static Optional<ExpresionVista> convertir(ExpresionDeMaterias expresion) {
    return ExpresionAVista.aVista(expresion, codigo -> "Materia " + codigo.valor());
  }

  @Test
  void uc05_paso4_unaMateriaSeMuestraConSuNombre() {
    assertThat(convertir(a("1"))).contains(vista("1"));
  }

  @Test
  void uc05_ext4a_unaDisyuncionMuestraTodasSusAlternativas() {
    ExpresionDeMaterias expresion = new ExpresionDeMaterias.O(a("1"), a("2"));

    assertThat(convertir(expresion))
        .contains(new ExpresionVista.AlgunaDe(List.of(vista("1"), vista("2"))));
  }

  @Test
  void uc05_ext4a_lasDisyuncionesAnidadasSeJuntan() {
    ExpresionDeMaterias expresion =
        new ExpresionDeMaterias.O(a("1"), new ExpresionDeMaterias.O(a("2"), a("3")));

    assertThat(convertir(expresion))
        .contains(new ExpresionVista.AlgunaDe(List.of(vista("1"), vista("2"), vista("3"))));
  }

  @Test
  void uc05_paso4_lasConjuncionesAnidadasSeJuntan() {
    ExpresionDeMaterias expresion =
        new ExpresionDeMaterias.Y(new ExpresionDeMaterias.Y(a("1"), a("2")), a("3"));

    assertThat(convertir(expresion))
        .contains(new ExpresionVista.TodasLas(List.of(vista("1"), vista("2"), vista("3"))));
  }

  @Test
  void uc05_ext4a_unaDisyuncionDentroDeUnaConjuncionNoSeJunta() {
    ExpresionDeMaterias expresion =
        new ExpresionDeMaterias.Y(a("1"), new ExpresionDeMaterias.O(a("2"), a("3")));

    assertThat(convertir(expresion))
        .contains(
            new ExpresionVista.TodasLas(
                List.of(vista("1"), new ExpresionVista.AlgunaDe(List.of(vista("2"), vista("3"))))));
  }

  @Test
  void uc05_ext4k_unaMateriaExternaNoSeMuestra() {
    assertThat(convertir(externa(TipoMateriaExterna.NIVELACION))).isEmpty();
    assertThat(convertir(externa(TipoMateriaExterna.OTRO_PROGRAMA))).isEmpty();
  }

  @Test
  void uc05_ext4k_unTerminoExternoDeUnaConjuncionOUnaDisyuncionSeOmite() {
    ExpresionDeMaterias conjuncion =
        new ExpresionDeMaterias.Y(a("1"), externa(TipoMateriaExterna.NIVELACION));
    ExpresionDeMaterias disyuncion =
        new ExpresionDeMaterias.O(externa(TipoMateriaExterna.OTRO_PROGRAMA), a("2"));

    assertThat(convertir(conjuncion)).contains(vista("1"));
    assertThat(convertir(disyuncion)).contains(vista("2"));
  }

  @Test
  void uc05_ext4k_unaMateriaDeOtroProgramaNoSePuedeCumplir() {
    assertThat(ExpresionAVista.noSePuedeCumplir(externa(TipoMateriaExterna.OTRO_PROGRAMA)))
        .isTrue();
  }

  @Test
  void uc05_ext4k_unaMateriaDeNivelacionSeDaPorCumplida() {
    assertThat(ExpresionAVista.noSePuedeCumplir(externa(TipoMateriaExterna.NIVELACION))).isFalse();
  }

  @Test
  void uc05_ext4k_unaMateriaDelProgramaSePuedeCumplir() {
    assertThat(ExpresionAVista.noSePuedeCumplir(a("1"))).isFalse();
  }

  @Test
  void uc05_ext4k_unaConjuncionConUnaMateriaDeOtroProgramaNoSePuedeCumplir() {
    ExpresionDeMaterias expresion =
        new ExpresionDeMaterias.Y(a("1"), externa(TipoMateriaExterna.OTRO_PROGRAMA));

    assertThat(ExpresionAVista.noSePuedeCumplir(expresion)).isTrue();
  }

  @Test
  void uc05_ext4k_unaDisyuncionSeCumpleSiAlgunaRamaSePuede() {
    ExpresionDeMaterias conUnaRamaPosible =
        new ExpresionDeMaterias.O(a("1"), externa(TipoMateriaExterna.OTRO_PROGRAMA));
    ExpresionDeMaterias sinRamaPosible =
        new ExpresionDeMaterias.O(
            externa(TipoMateriaExterna.OTRO_PROGRAMA), externa(TipoMateriaExterna.OTRO_PROGRAMA));

    assertThat(ExpresionAVista.noSePuedeCumplir(conUnaRamaPosible)).isFalse();
    assertThat(ExpresionAVista.noSePuedeCumplir(sinRamaPosible)).isTrue();
  }
}
