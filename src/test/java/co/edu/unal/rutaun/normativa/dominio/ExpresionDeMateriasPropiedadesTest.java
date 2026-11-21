package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.AlgunaDe;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class ExpresionDeMateriasPropiedadesTest {

  private static final int PROFUNDIDAD_MAXIMA = 3;

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_reordenarLosTerminosNoCambiaLaExpresion(
      @ForAll("expresiones") ExpresionDeMaterias expresion, @ForAll long semilla) {
    ExpresionDeMaterias reordenada = reordenar(expresion, new Random(semilla));

    assertThat(reordenada).isEqualTo(expresion).hasSameHashCodeAs(expresion);
  }

  @Provide
  Arbitrary<ExpresionDeMaterias> expresiones() {
    return expresiones(0);
  }

  private static Arbitrary<ExpresionDeMaterias> expresiones(int profundidad) {
    Arbitrary<ExpresionDeMaterias> hojas = hojas();
    if (profundidad >= PROFUNDIDAD_MAXIMA) {
      return hojas;
    }
    Arbitrary<List<ExpresionDeMaterias>> terminos =
        expresiones(profundidad + 1).list().ofMinSize(2).ofMaxSize(3).uniqueElements();
    Arbitrary<ExpresionDeMaterias> compuestas =
        Combinators.combine(Arbitraries.of(true, false), terminos)
            .as(ExpresionDeMateriasPropiedadesTest::combinar);
    return profundidad == 0 ? compuestas : Arbitraries.oneOf(hojas, compuestas);
  }

  private static Arbitrary<ExpresionDeMaterias> hojas() {
    Arbitrary<ExpresionDeMaterias> terminos =
        Arbitraries.integers()
            .between(9000001, 9000009)
            .map(
                numero -> (ExpresionDeMaterias) new TerminoMateria(new CodigoMateria("" + numero)));
    Arbitrary<ExpresionDeMaterias> externas =
        Arbitraries.of(TipoMateriaExterna.values())
            .map(
                tipo ->
                    (ExpresionDeMaterias)
                        new ExpresionDeMaterias.MateriaExterna(
                            "Materia externa", Optional.empty(), tipo));
    return Arbitraries.oneOf(terminos, externas);
  }

  private static ExpresionDeMaterias combinar(boolean todas, List<ExpresionDeMaterias> terminos) {
    ExpresionDeMaterias[] arreglo = terminos.toArray(ExpresionDeMaterias[]::new);
    return todas ? ExpresionDeMaterias.todasLas(arreglo) : ExpresionDeMaterias.algunaDe(arreglo);
  }

  private static ExpresionDeMaterias reordenar(ExpresionDeMaterias expresion, Random azar) {
    return switch (expresion) {
      case TodasLas todas -> ExpresionDeMaterias.todasLas(barajar(todas.terminos(), azar));
      case AlgunaDe alguna -> ExpresionDeMaterias.algunaDe(barajar(alguna.terminos(), azar));
      default -> expresion;
    };
  }

  private static ExpresionDeMaterias[] barajar(Set<ExpresionDeMaterias> terminos, Random azar) {
    List<ExpresionDeMaterias> reordenados = new ArrayList<>();
    for (ExpresionDeMaterias termino : terminos) {
      reordenados.add(reordenar(termino, azar));
    }
    Collections.shuffle(reordenados, azar);
    return reordenados.toArray(ExpresionDeMaterias[]::new);
  }
}
