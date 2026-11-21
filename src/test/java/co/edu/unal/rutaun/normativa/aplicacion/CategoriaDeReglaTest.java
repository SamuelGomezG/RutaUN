package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Correquisito;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.TipoCargaPorPeriodo;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** UC06-HU01-T4 (#132): la categoría del glosario que corresponde a los datos de una regla. */
class CategoriaDeReglaTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final ExpresionDeMaterias EXPRESION = new ExpresionDeMaterias.TerminoMateria(A);

  private final Malla malla = MallaDePrueba.pequena();
  private final NodoDeMalla fundamentacion = MallaDePrueba.nodo(malla, "Fundamentación");
  private final NodoDeMalla cienciasBasicas = MallaDePrueba.nodo(malla, "Ciencias básicas");
  private final NodoDeMalla libreEleccion = MallaDePrueba.nodo(malla, "Libre elección");

  @Test
  void uc06_paso4_losCreditosDelProgramaYLosComponentesVanJuntos() {
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DePrograma(new Creditos(165))))
        .isEqualTo("Componentes y créditos");
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeComponente(fundamentacion)))
        .isEqualTo("Componentes y créditos");
  }

  @Test
  void uc06_paso4_elComponenteDeLibreEleccionTieneSuPropiaCategoria() {
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeComponente(libreEleccion)))
        .isEqualTo("Libre elección");
  }

  @Test
  void uc06_paso4_lasMateriasYLasAgrupacionesTienenLasSuyas() {
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeMateria(malla.materias().get(0), fundamentacion)))
        .isEqualTo("Materias");
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeAgrupacion((Agrupacion) cienciasBasicas, fundamentacion)))
        .isEqualTo("Agrupaciones");
  }

  @Test
  void uc06_paso4_prerrequisitosCorrequisitosYHabilitacionVanJuntos() {
    String esperada = "Prerrequisitos y habilitación";

    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DePrerrequisito(new Prerrequisito(A, EXPRESION))))
        .isEqualTo(esperada);
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeCorrequisito(new Correquisito(A, EXPRESION))))
        .isEqualTo(esperada);
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeHabilitacion(
                    new ReglaDeHabilitacion(A, MedidaDeCreditos.delPrograma(), new Creditos(80)))))
        .isEqualTo(esperada);
  }

  @Test
  void uc06_paso4_lasEquivalenciasLaCargaLosRequisitosDeGradoYLasOtrasTienenLasSuyas() {
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeEquivalencia(Set.of(A), new CodigoMateria("B"))))
        .isEqualTo("Equivalencias");
    assertThat(
            ConsultaNormativaService.categoria(
                new DatosDeRegla.DeCargaPorPeriodo(new Creditos(10), TipoCargaPorPeriodo.MINIMO)))
        .isEqualTo("Carga mínima o máxima por periodo");
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeRequisitoDeGrado()))
        .isEqualTo("Requisitos de grado");
    assertThat(ConsultaNormativaService.categoria(new DatosDeRegla.DeOtraCategoria()))
        .isEqualTo("Otra");
  }
}
