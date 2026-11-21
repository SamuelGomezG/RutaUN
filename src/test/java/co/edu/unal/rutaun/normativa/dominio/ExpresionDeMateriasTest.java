package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.AlgunaDe;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExpresionDeMateriasTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");

  @Test
  void tt03_seConstruyeLaExpresionAConBAlternativaC() {
    ExpresionDeMaterias expresion =
        ExpresionDeMaterias.todasLas(
            new TerminoMateria(A),
            ExpresionDeMaterias.algunaDe(new TerminoMateria(B), new TerminoMateria(C)));

    assertThat(expresion).isInstanceOf(TodasLas.class);
    TodasLas conjuncion = (TodasLas) expresion;
    assertThat(conjuncion.terminos()).contains(new TerminoMateria(A));
    AlgunaDe disyuncion =
        (AlgunaDe)
            conjuncion.terminos().stream()
                .filter(AlgunaDe.class::isInstance)
                .findFirst()
                .orElseThrow();
    assertThat(disyuncion.terminos()).containsExactly(new TerminoMateria(B), new TerminoMateria(C));
  }

  @Test
  void uc07_ext6c_unaYConLosMismosTerminosEnOtroOrdenEsIgual() {
    ExpresionDeMaterias enUnOrden =
        ExpresionDeMaterias.todasLas(new TerminoMateria(A), new TerminoMateria(B));
    ExpresionDeMaterias enOtroOrden =
        ExpresionDeMaterias.todasLas(new TerminoMateria(B), new TerminoMateria(A));
    ExpresionDeMaterias disyuncionEnUnOrden =
        ExpresionDeMaterias.algunaDe(new TerminoMateria(A), new TerminoMateria(B));
    ExpresionDeMaterias disyuncionEnOtroOrden =
        ExpresionDeMaterias.algunaDe(new TerminoMateria(B), new TerminoMateria(A));

    assertThat(enUnOrden).isEqualTo(enOtroOrden).hasSameHashCodeAs(enOtroOrden);
    assertThat(disyuncionEnUnOrden)
        .isEqualTo(disyuncionEnOtroOrden)
        .hasSameHashCodeAs(disyuncionEnOtroOrden);
  }

  @Test
  void uc07_ext6c_lasYAnidadasSeAplananYSonIguales() {
    ExpresionDeMaterias porLaDerecha =
        ExpresionDeMaterias.todasLas(
            new TerminoMateria(A),
            ExpresionDeMaterias.todasLas(new TerminoMateria(B), new TerminoMateria(C)));
    ExpresionDeMaterias porLaIzquierda =
        ExpresionDeMaterias.todasLas(
            ExpresionDeMaterias.todasLas(new TerminoMateria(A), new TerminoMateria(B)),
            new TerminoMateria(C));
    ExpresionDeMaterias plana =
        ExpresionDeMaterias.todasLas(
            new TerminoMateria(A), new TerminoMateria(B), new TerminoMateria(C));

    assertThat(porLaDerecha).isEqualTo(porLaIzquierda).isEqualTo(plana);
    TodasLas todas = (TodasLas) porLaDerecha;
    assertThat(todas.terminos()).hasSize(3).allMatch(termino -> !(termino instanceof TodasLas));

    ExpresionDeMaterias conO =
        ExpresionDeMaterias.todasLas(
            new TerminoMateria(A),
            ExpresionDeMaterias.algunaDe(new TerminoMateria(B), new TerminoMateria(C)));

    assertThat(((TodasLas) conO).terminos())
        .hasSize(2)
        .anyMatch(termino -> termino instanceof AlgunaDe);
  }

  @Test
  void tt03_unaYConUnSoloTerminoDistintoNoSeConstruye() {
    Set<ExpresionDeMaterias> repetidos = new HashSet<>();
    repetidos.add(new TerminoMateria(A));
    repetidos.add(new TerminoMateria(A));

    assertThatThrownBy(() -> new TodasLas(Set.of(new TerminoMateria(A))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una «y» necesita al menos dos términos distintos");
    assertThatThrownBy(() -> new TodasLas(repetidos))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una «y» necesita al menos dos términos distintos");
    assertThatThrownBy(() -> new AlgunaDe(Set.of(new TerminoMateria(A))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una «o» necesita al menos dos términos distintos");
  }

  @Test
  void tt03_laFabricaDevuelveElTerminoSiQuedaUno() {
    TerminoMateria termino = new TerminoMateria(A);

    assertThat(ExpresionDeMaterias.todasLas(termino, termino)).isEqualTo(termino);
    assertThat(ExpresionDeMaterias.algunaDe(termino)).isEqualTo(termino);
    assertThatThrownBy(() -> ExpresionDeMaterias.todasLas())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ExpresionDeMaterias.algunaDe())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tt03_losTerminosSeRecorrenEnElOrdenDeLaNorma() {
    List<CodigoMateria> codigos = new ArrayList<>();
    for (int numero = 1; numero <= 9; numero++) {
      codigos.add(new CodigoMateria("T" + numero));
    }
    ExpresionDeMaterias anidada = new TerminoMateria(codigos.get(8));
    for (int indice = 7; indice >= 0; indice--) {
      anidada = ExpresionDeMaterias.todasLas(new TerminoMateria(codigos.get(indice)), anidada);
    }

    assertThat(((TodasLas) anidada).terminos())
        .containsExactlyElementsOf(codigos.stream().map(TerminoMateria::new).toList());
  }

  @Test
  void tt03_seConstruyeUnaMateriaExternaConNombreTipoYCodigoOpcional() {
    ExpresionDeMaterias.MateriaExterna externa =
        new ExpresionDeMaterias.MateriaExterna(
            "Matemáticas Básicas", Optional.empty(), TipoMateriaExterna.NIVELACION);

    assertThat(externa.nombre()).isEqualTo("Matemáticas Básicas");
    assertThat(externa.codigo()).isEmpty();
    assertThat(externa.tipo()).isEqualTo(TipoMateriaExterna.NIVELACION);
  }

  @Test
  void tt03_laMateriaExternaPuedeTenerCodigo() {
    ExpresionDeMaterias.MateriaExterna externa =
        new ExpresionDeMaterias.MateriaExterna(
            "Electrónica Análoga I",
            Optional.of(new CodigoMateria("2016699")),
            TipoMateriaExterna.OTRO_PROGRAMA);

    assertThat(externa.codigo()).contains(new CodigoMateria("2016699"));
    assertThat(externa.tipo()).isEqualTo(TipoMateriaExterna.OTRO_PROGRAMA);
  }

  @Test
  void tt03_lasExpresionesRechazanTerminosObligatoriosNulos() {
    assertThatThrownBy(() -> new TerminoMateria(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    null, Optional.empty(), TipoMateriaExterna.NIVELACION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    "Matemáticas Básicas", null, TipoMateriaExterna.NIVELACION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
    assertThatThrownBy(
            () ->
                new ExpresionDeMaterias.MateriaExterna(
                    "Matemáticas Básicas", Optional.empty(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(() -> new TodasLas(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("terminos no puede ser null");
    assertThatThrownBy(() -> new AlgunaDe(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("terminos no puede ser null");

    Set<ExpresionDeMaterias> conNulo = new HashSet<>();
    conNulo.add(new TerminoMateria(A));
    conNulo.add(null);

    assertThatThrownBy(() -> new TodasLas(conNulo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("terminos no puede tener elementos null");
  }
}
