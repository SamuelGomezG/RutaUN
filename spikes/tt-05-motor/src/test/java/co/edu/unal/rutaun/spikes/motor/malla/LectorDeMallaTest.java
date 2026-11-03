package co.edu.unal.rutaun.spikes.motor.malla;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.spikes.motor.Rutas;
import co.edu.unal.rutaun.spikes.motor.busqueda.CalculoDelPlan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Cancelacion;
import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.ResultadoCalculo;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LectorDeMallaTest {

  private static final String CREDITOS_POR_MATERIA = "\"creditosPorMateria\": 3,";

  @TempDir Path temporal;

  @Test
  void tt05_sinCreditosPorMateriaLaCargaFalla() throws IOException {
    Path malla = mallaCon("");
    assertThatThrownBy(() -> LectorDeMalla.leer(malla))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("libreEleccion.creditosPorMateria");
  }

  @Test
  void tt05_creditosPorMateriaLibreSeLeenDeLaMalla() throws IOException {
    // Constructor, optimizador y verificador deben leer el mismo dato, no un 3 escrito en código.
    Path malla = mallaCon("\"creditosPorMateria\": 4,");
    ModelosJson.MallaJson datos = LectorDeMalla.leer(malla);
    ModeloDeCalculo modelo = new ModeloDeCalculo(datos, ModeloDeCalculo.Configuracion.vacia());
    assertThat(modelo.creditosPorMateriaLibre()).isEqualTo(4);

    ModelosJson.EscenariosJson escenarios =
        LectorDeMalla.leerEscenarios(Rutas.datos().resolve("escenarios.json"));
    ModelosJson.EscenarioJson e1 =
        escenarios.escenarios().stream().filter(e -> e.id().equals("E1")).findFirst().orElseThrow();
    ModelosJson.TopeJson t =
        escenarios.topes().stream().filter(x -> x.id().equals("T2")).findFirst().orElseThrow();
    Tope t2 = new Tope(t.id(), t.creditos(), t.materias());
    CalculoDelPlan calculo = new CalculoDelPlan(modelo, e1, t2, new Cancelacion(30_000));
    ResultadoCalculo resultado = calculo.calcular();
    assertThat(resultado).isInstanceOf(ResultadoCalculo.Calculado.class);
    Plan plan = ((ResultadoCalculo.Calculado) resultado).plan();
    assertThat(
            new VerificadorDePlan(modelo).verificar(plan, calculo.a0(), calculo.libreInicial(), t2))
        .isEmpty();
  }

  private Path mallaCon(String reemplazo) throws IOException {
    String original =
        Files.readString(Rutas.datos().resolve("malla-sistemas.json"), StandardCharsets.UTF_8);
    assertThat(original).contains(CREDITOS_POR_MATERIA);
    Path salida = temporal.resolve("malla.json");
    Files.writeString(
        salida, original.replace(CREDITOS_POR_MATERIA, reemplazo), StandardCharsets.UTF_8);
    return salida;
  }
}
