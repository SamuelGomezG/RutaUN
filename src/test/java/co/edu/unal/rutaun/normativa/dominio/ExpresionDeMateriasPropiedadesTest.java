package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.compartido.Razon;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.AlgunaDe;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
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

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void s17_ningunaRazonNombraUnaMateriaExterna(
      @ForAll("expresionesConExternas") ExpresionDeMaterias expresion, @ForAll long semilla) {
    EstadoRestriccion estado = expresion.evaluar(contextoAleatorio(new Random(semilla))::estadoDe);
    String texto = estado.razones().toString();

    for (int numero = 1; numero <= 9; numero++) {
      assertThat(texto).doesNotContain("EXTERNA-" + numero).doesNotContain("800000" + numero);
    }
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void s18_agregarUnaRamaDeOtroProgramaNoCambiaElEstadoDeUnaO(
      @ForAll("expresionesCumplibles") ExpresionDeMaterias expresion, @ForAll long semilla) {
    ContextoDeEvaluacion contexto = contextoAleatorio(new Random(semilla));
    ExpresionDeMaterias.MateriaExterna externa =
        new ExpresionDeMaterias.MateriaExterna(
            "EXTERNA-1",
            Optional.of(new CodigoMateria("8000001")),
            TipoMateriaExterna.OTRO_PROGRAMA);

    EstadoRestriccion sola = expresion.evaluar(contexto::estadoDe);
    EstadoRestriccion conExterna =
        ExpresionDeMaterias.algunaDe(expresion, externa).evaluar(contexto::estadoDe);

    assertThat(conExterna).isEqualTo(sola);
  }

  @Provide
  Arbitrary<ExpresionDeMaterias> expresionesConExternas() {
    return generarExpresionesConExternas(0);
  }

  @Provide
  Arbitrary<ExpresionDeMaterias> expresionesCumplibles() {
    return generarExpresionesCumplibles(0);
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

  private static Arbitrary<ExpresionDeMaterias> generarExpresionesConExternas(int profundidad) {
    Arbitrary<ExpresionDeMaterias> hojas = hojasConExternas();
    if (profundidad >= PROFUNDIDAD_MAXIMA) {
      return hojas;
    }
    Arbitrary<List<ExpresionDeMaterias>> terminos =
        generarExpresionesConExternas(profundidad + 1)
            .list()
            .ofMinSize(2)
            .ofMaxSize(3)
            .uniqueElements();
    Arbitrary<ExpresionDeMaterias> compuestas =
        Combinators.combine(Arbitraries.of(true, false), terminos)
            .as(ExpresionDeMateriasPropiedadesTest::combinar);
    return profundidad == 0 ? compuestas : Arbitraries.oneOf(hojas, compuestas);
  }

  private static Arbitrary<ExpresionDeMaterias> hojasConExternas() {
    Arbitrary<ExpresionDeMaterias> terminos =
        Arbitraries.integers()
            .between(1, 9)
            .map(
                numero ->
                    (ExpresionDeMaterias) new TerminoMateria(new CodigoMateria("900000" + numero)));
    Arbitrary<ExpresionDeMaterias> externas =
        Combinators.combine(
                Arbitraries.integers().between(1, 9), Arbitraries.of(TipoMateriaExterna.values()))
            .as(
                (numero, tipo) ->
                    (ExpresionDeMaterias)
                        new ExpresionDeMaterias.MateriaExterna(
                            "EXTERNA-" + numero,
                            Optional.of(new CodigoMateria("800000" + numero)),
                            tipo));
    return Arbitraries.oneOf(terminos, externas);
  }

  private static Arbitrary<ExpresionDeMaterias> generarExpresionesCumplibles(int profundidad) {
    Arbitrary<ExpresionDeMaterias> hojas = hojasCumplibles();
    if (profundidad >= PROFUNDIDAD_MAXIMA) {
      return hojas;
    }
    Arbitrary<List<ExpresionDeMaterias>> terminos =
        generarExpresionesCumplibles(profundidad + 1)
            .list()
            .ofMinSize(2)
            .ofMaxSize(3)
            .uniqueElements();
    Arbitrary<ExpresionDeMaterias> compuestas =
        Combinators.combine(Arbitraries.of(true, false), terminos)
            .as(ExpresionDeMateriasPropiedadesTest::combinar);
    return profundidad == 0 ? compuestas : Arbitraries.oneOf(hojas, compuestas);
  }

  private static Arbitrary<ExpresionDeMaterias> hojasCumplibles() {
    Arbitrary<ExpresionDeMaterias> terminos =
        Arbitraries.integers()
            .between(1, 9)
            .map(
                numero ->
                    (ExpresionDeMaterias) new TerminoMateria(new CodigoMateria("900000" + numero)));
    Arbitrary<ExpresionDeMaterias> externas =
        Arbitraries.integers()
            .between(1, 9)
            .map(
                numero ->
                    (ExpresionDeMaterias)
                        new ExpresionDeMaterias.MateriaExterna(
                            "EXTERNA-" + numero,
                            Optional.of(new CodigoMateria("800000" + numero)),
                            TipoMateriaExterna.NIVELACION));
    return Arbitraries.oneOf(terminos, externas);
  }

  private static ContextoDeEvaluacion contextoAleatorio(Random azar) {
    ContextoDeEvaluacionFijo.Constructor constructor = ContextoDeEvaluacionFijo.nuevo();
    Certeza[] certezas = Certeza.values();
    for (int numero = 1; numero <= 9; numero++) {
      CodigoMateria materia = new CodigoMateria("900000" + numero);
      constructor.estado(
          materia,
          new EstadoRestriccion(
              certezas[azar.nextInt(certezas.length)],
              List.of(new Razon.TerminoAprobado(materia))));
    }
    constructor
        .creditosDelPrograma(azar.nextInt(200))
        .creditosDelComponente(TipoComponente.DISCIPLINAR, azar.nextInt(100));
    return constructor.construir();
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
