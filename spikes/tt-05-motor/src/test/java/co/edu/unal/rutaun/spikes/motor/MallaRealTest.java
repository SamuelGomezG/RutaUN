package co.edu.unal.rutaun.spikes.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.spikes.motor.busqueda.CalculoDelPlan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Cancelacion;
import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.ResultadoCalculo;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.malla.LectorDeMalla;
import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MallaRealTest {

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

  private static Tope tope(String id) {
    ModelosJson.TopeJson tope =
        escenarios.topes().stream().filter(t -> t.id().equals(id)).findFirst().orElseThrow();
    return new Tope(tope.id(), tope.creditos(), tope.materias());
  }
}
