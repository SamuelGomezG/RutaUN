package co.edu.unal.rutaun.spikes.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.malla.LectorDeMalla;
import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.PlanEstandar;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerificadorMallaEstandarTest {

  private static ModeloDeCalculo modelo;
  private static Tope t2;

  @BeforeAll
  static void cargar() {
    Path datos = Rutas.datos();
    ModelosJson.MallaJson malla = LectorDeMalla.leer(datos.resolve("malla-sistemas.json"));
    modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    t2 = new Tope("T2", 18, 6);
  }

  @Test
  void tt05_aceptaLaMallaEstandarAdaptada() {
    Plan plan = PlanEstandar.adaptado(modelo);
    assertThat(verificar(plan)).isEmpty();
  }

  @Test
  void tt05_rechazaElIntercambioDeRedesYEstructuras() {
    Plan plan =
        PlanEstandar.intercambiar(modelo, PlanEstandar.adaptado(modelo), "2025967", "2016699");
    assertThat(verificar(plan)).anyMatch(error -> error.contains("Prerrequisito"));
  }

  @Test
  void tt05_rechazaElTallerAntesDeLaHabilitacion() {
    Plan plan =
        PlanEstandar.intercambiar(modelo, PlanEstandar.adaptado(modelo), "2024045", "2015970");
    List<String> errores = verificar(plan);
    assertThat(errores).anyMatch(error -> error.contains("Habilitación"));
    assertThat(errores).allMatch(error -> error.contains("Habilitación"));
  }

  @Test
  void tt05_rechazaQuitarComputacionVisual() {
    Plan plan =
        PlanEstandar.quitarYAgregarBloque(modelo, PlanEstandar.adaptado(modelo), "2025960", 8, 3);
    List<String> errores = verificar(plan);
    assertThat(errores).anyMatch(error -> error.contains("cubrir"));
    assertThat(errores).anyMatch(error -> error.contains("bloques"));
  }

  private static List<String> verificar(Plan plan) {
    return new VerificadorDePlan(modelo)
        .verificar(plan, new ConjuntoDeMaterias(modelo.cantidadMaterias()), 33, t2);
  }
}
