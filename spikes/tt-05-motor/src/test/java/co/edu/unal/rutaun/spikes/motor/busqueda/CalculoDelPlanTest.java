package co.edu.unal.rutaun.spikes.motor.busqueda;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.spikes.motor.Rutas;
import co.edu.unal.rutaun.spikes.motor.malla.LectorDeMalla;
import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculoDelPlanTest {

  private static ModelosJson.MallaJson malla;
  private static ModelosJson.EscenariosJson escenarios;

  @BeforeAll
  static void cargar() {
    Path datos = Rutas.datos();
    malla = LectorDeMalla.leer(datos.resolve("malla-sistemas.json"));
    escenarios = LectorDeMalla.leerEscenarios(datos.resolve("escenarios.json"));
  }

  @Test
  void tt05_e0ConT2TieneDiezSemestres() {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    ModelosJson.EscenarioJson e0 = escenario("E0");
    Tope t2 = tope("T2");
    CalculoDelPlan calculo = new CalculoDelPlan(modelo, e0, t2, new Cancelacion(30_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(plan.k()).isEqualTo(10);
    assertThat(plan.creditosOptativas()).isEqualTo(78);
    assertThat(
            new VerificadorDePlan(modelo).verificar(plan, calculo.a0(), calculo.libreInicial(), t2))
        .isEmpty();
    assertThat(plan.semestre(modelo.indiceDe("2016843"))).isPositive();
    assertThat(plan.semestre(modelo.indiceDe("2016498"))).isEqualTo(-1);
    assertThat(plan.semestre(modelo.indiceDe("2016492"))).isEqualTo(-1);
    assertThat(modelo.indiceDe("2025968")).isNull();
  }

  @Test
  void tt05_e1ConT2TieneOchoSemestres() {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    ModelosJson.EscenarioJson e1 = escenario("E1");
    Tope t2 = tope("T2");
    CalculoDelPlan calculo = new CalculoDelPlan(modelo, e1, t2, new Cancelacion(30_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(plan.k()).isEqualTo(8);
    assertThat(
            new VerificadorDePlan(modelo).verificar(plan, calculo.a0(), calculo.libreInicial(), t2))
        .isEmpty();
  }

  @Test
  void tt05_constructorSinSemestresSuficientesNoDevuelvePlan() {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    Tope t2 = tope("T2");
    Restricciones restricciones = new Restricciones(modelo.cantidadMaterias());
    restricciones.maxSemestres = 9;
    assertThat(
            ConstructorTestigo.construir(
                modelo,
                new ConjuntoDeMaterias(modelo.cantidadMaterias()),
                33,
                t2,
                restricciones,
                new Cancelacion(30_000)))
        .isNull();
  }

  @ParameterizedTest
  @CsvSource({"E0,T2,10", "E1,T1,7", "E1,T2,8", "E1,T4,9", "E2,T2,4", "E3,T2,1", "E4,T2,8"})
  void tt05_elPlanFinalTieneLosSemestresDeLaCota(String idEscenario, String idTope, int semestres) {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario(idEscenario), tope(idTope), new Cancelacion(30_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(plan.k()).isEqualTo(semestres);
  }

  @Test
  void tt05_plazoVencidoDevuelveTiempoAgotado() {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario("E0"), tope("T2"), new Cancelacion(0));
    assertThat(calculo.calcular()).isInstanceOf(ResultadoCalculo.TiempoAgotado.class);
  }

  @Test
  void tt05_cancelacionRespetaElPlazo() {
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario("E0"), tope("T5"), new Cancelacion(2_000));
    long inicio = System.nanoTime();
    ResultadoCalculo resultado = calculo.calcular();
    long ms = (System.nanoTime() - inicio) / 1_000_000L;
    assertThat(resultado).isInstanceOf(ResultadoCalculo.TiempoAgotado.class);
    assertThat(ms).isLessThan(2_500L);
  }

  @Test
  void tt05_correquisitoPendienteVaEnElMismoSemestre() {
    ModelosJson.VarianteJson variante = variante("C2");
    ModeloDeCalculo modelo =
        new ModeloDeCalculo(
            malla,
            new ModeloDeCalculo.Configuracion(
                variante.fijadas(), variante.prerrequisitos(), variante.correquisitos()));
    Tope t2 = tope("T2");
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario("E1"), t2, new Cancelacion(30_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(plan.semestre(modelo.indiceDe("2016353")))
        .isEqualTo(plan.semestre(modelo.indiceDe("2016698")));
    assertThat(
            new VerificadorDePlan(modelo).verificar(plan, calculo.a0(), calculo.libreInicial(), t2))
        .isEmpty();
  }

  @Test
  void tt05_correquisitosEncadenadosNoCiclan() {
    ModeloDeCalculo modelo = modeloDe(variante("CC"));
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario("E1"), tope("T2"), new Cancelacion(10_000));
    long inicio = System.nanoTime();
    ResultadoCalculo resultado = calculo.calcular();
    long ms = (System.nanoTime() - inicio) / 1_000_000L;
    assertThat(resultado).isNotInstanceOf(ResultadoCalculo.TiempoAgotado.class);
    assertThat(ms).isLessThan(1_000L);
  }

  @Test
  void tt05_correquisitosEncadenadosRespetanElOrden() {
    ModeloDeCalculo modelo = modeloDe(variante("CC"));
    Tope t2 = tope("T2");
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario("E1"), t2, new Cancelacion(10_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(
            new VerificadorDePlan(modelo).verificar(plan, calculo.a0(), calculo.libreInicial(), t2))
        .isEmpty();
    // 1000017 exige 2016699 y 2016699 exige 2016697: cada una va con su correquisito o después.
    int cabeza = plan.semestre(modelo.indiceDe("1000017"));
    int medio = plan.semestre(modelo.indiceDe("2016699"));
    int cola = plan.semestre(modelo.indiceDe("2016697"));
    assertThat(cola).isPositive().isLessThanOrEqualTo(medio);
    assertThat(medio).isLessThanOrEqualTo(cabeza);
  }

  @Test
  void tt05_determinismoRepiteElMismoPlan() {
    ModelosJson.EscenarioJson e1 = escenario("E1");
    Tope t2 = tope("T2");
    Plan primero = null;
    for (int i = 0; i < 20; i++) {
      ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
      CalculoDelPlan calculo = new CalculoDelPlan(modelo, e1, t2, new Cancelacion(30_000));
      ResultadoCalculo resultado = calculo.calcular();
      assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
      Plan actual = ((ResultadoCalculo.Calculado) resultado).plan();
      if (primero == null) {
        primero = actual;
      } else {
        assertThat(actual.semestrePorMateria()).containsExactly(primero.semestrePorMateria());
        assertThat(actual.bloquePorSemestre()).containsExactly(primero.bloquePorSemestre());
        assertThat(actual.asignacion()).containsExactly(primero.asignacion());
      }
    }
  }

  private static ModelosJson.EscenarioJson escenario(String id) {
    return escenarios.escenarios().stream()
        .filter(e -> e.id().equals(id))
        .findFirst()
        .orElseThrow();
  }

  private static ModeloDeCalculo modeloDe(ModelosJson.VarianteJson variante) {
    return new ModeloDeCalculo(
        malla,
        new ModeloDeCalculo.Configuracion(
            variante.fijadas(), variante.prerrequisitos(), variante.correquisitos()));
  }

  private static ModelosJson.VarianteJson variante(String id) {
    return escenarios.variantes().stream().filter(v -> v.id().equals(id)).findFirst().orElseThrow();
  }

  private static Tope tope(String id) {
    ModelosJson.TopeJson tope =
        escenarios.topes().stream().filter(t -> t.id().equals(id)).findFirst().orElseThrow();
    return new Tope(tope.id(), tope.creditos(), tope.materias());
  }
}
