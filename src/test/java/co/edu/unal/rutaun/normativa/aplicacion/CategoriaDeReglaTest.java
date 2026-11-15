package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.TipoDeCargaPorPeriodo;
import org.junit.jupiter.api.Test;

/** UC06-HU01-T4 (#132): la categoría del glosario que corresponde a los datos de una regla. */
class CategoriaDeReglaTest {

  @Test
  void uc06_paso4_losCreditosDelProgramaVanConLosComponentes() {
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DePrograma(new Creditos(165))))
        .isEqualTo("Componentes y créditos");
  }

  @Test
  void uc06_paso4_laCargaPorPeriodoTieneSuCategoria() {
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeCargaPorPeriodo(new Creditos(10), TipoDeCargaPorPeriodo.MINIMA)))
        .isEqualTo("Carga mínima o máxima por periodo");
  }

  @Test
  void uc06_paso4_losRequisitosDeGradoYLasOtrasTienenLasSuyas() {
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeRequisitoDeGrado()))
        .isEqualTo("Requisitos de grado");
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeOtraCategoria()))
        .isEqualTo("Otra");
  }
}
