package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.TipoDeRestriccion;
import co.edu.unal.rutaun.normativa.aplicacion.OrdenDeRestricciones.Clave;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * UC05 paso 4: el orden de las restricciones de una materia no depende del orden en que llegan las
 * reglas; se prueba con todos los órdenes posibles.
 */
class OrdenDeRestriccionesTest {

  private static final LocalDate ANTES = LocalDate.of(2010, 5, 1);
  private static final LocalDate DESPUES = LocalDate.of(2023, 12, 12);

  private static Clave clave(TipoDeRestriccion tipo, LocalDate fecha, String articulo, int regla) {
    return new Clave(tipo, fecha, articulo, new UUID(0L, regla));
  }

  /** Todos los órdenes en que pueden llegar las claves. */
  private static List<List<Clave>> ordenes(List<Clave> claves) {
    if (claves.size() <= 1) {
      return List.of(claves);
    }
    List<List<Clave>> resultado = new ArrayList<>();
    for (int i = 0; i < claves.size(); i++) {
      List<Clave> resto = new ArrayList<>(claves);
      Clave primera = resto.remove(i);
      for (List<Clave> orden : ordenes(resto)) {
        List<Clave> completo = new ArrayList<>();
        completo.add(primera);
        completo.addAll(orden);
        resultado.add(completo);
      }
    }
    return resultado;
  }

  private static void quedanEnEsteOrden(List<Clave> esperado) {
    for (List<Clave> llegada : ordenes(esperado)) {
      assertThat(llegada.stream().sorted(OrdenDeRestricciones.ORDEN).toList()).isEqualTo(esperado);
    }
  }

  @Test
  void uc05_paso4_losPrerrequisitosVanAntesQueLosCorrequisitosYLasHabilitaciones() {
    quedanEnEsteOrden(
        List.of(
            clave(TipoDeRestriccion.PRERREQUISITO, DESPUES, "art. 9", 3),
            clave(TipoDeRestriccion.CORREQUISITO, ANTES, "art. 1", 1),
            clave(TipoDeRestriccion.HABILITACION, ANTES, "art. 1", 2)));
  }

  @Test
  void uc05_paso4_dentroDeUnaClaseVaPrimeroLaNormaExpedidaAntesYAlFinalLaQueNoTieneNorma() {
    quedanEnEsteOrden(
        List.of(
            clave(TipoDeRestriccion.PRERREQUISITO, ANTES, "art. 9", 3),
            clave(TipoDeRestriccion.PRERREQUISITO, DESPUES, "art. 1", 1),
            clave(TipoDeRestriccion.PRERREQUISITO, null, "art. 1", 2)));
  }

  @Test
  void uc05_paso4_conLaMismaFechaElArticuloSeOrdenaComoNumeroYNoComoTexto() {
    quedanEnEsteOrden(
        List.of(
            clave(TipoDeRestriccion.PRERREQUISITO, DESPUES, "art. 2", 3),
            clave(TipoDeRestriccion.PRERREQUISITO, DESPUES, "art. 10", 1)));
  }

  @Test
  void uc05_paso4_conLaMismaFechaYElMismoArticuloDesempataElIdentificadorDeLaRegla() {
    quedanEnEsteOrden(
        List.of(
            clave(TipoDeRestriccion.CORREQUISITO, DESPUES, "art. 4", 1),
            clave(TipoDeRestriccion.CORREQUISITO, DESPUES, "art. 4", 2),
            clave(TipoDeRestriccion.CORREQUISITO, DESPUES, "art. 4", 3)));
  }
}
