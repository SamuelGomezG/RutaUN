package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DatosDeReglaTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final ExpresionDeMaterias EXPRESION =
      new ExpresionDeMaterias.MateriaDelPrograma(A);

  private final Malla malla = MallaDePrueba.pequena();
  private final NodoDeMalla fundamentacion = MallaDePrueba.nodo(malla, "Fundamentación");
  private final NodoDeMalla cienciasBasicas = MallaDePrueba.nodo(malla, "Ciencias básicas");
  private final NodoDeMalla libreEleccion = MallaDePrueba.nodo(malla, "Libre elección");
  private final MateriaEnMalla materia = malla.materias().get(0);

  @Test
  void tt03_unSwitchExhaustivoCubreTodasLasCategorias() {
    assertThat(todas().stream().map(DatosDeReglaTest::categoria).toList())
        .containsExactly(
            "materia",
            "prerrequisito",
            "correquisito",
            "habilitación",
            "agrupación",
            "componente",
            "programa",
            "equivalencia",
            "carga por periodo",
            "requisito de grado",
            "otra");
  }

  @Test
  void tt03_datosDeMateriaGuardanLaMateriaYSuContenedor() {
    DatosDeRegla.DeMateria datos = new DatosDeRegla.DeMateria(materia, fundamentacion);

    assertThat(datos.materia()).isEqualTo(materia);
    assertThat(datos.contenedor()).isEqualTo(fundamentacion);
  }

  @Test
  void tt03_datosDeComponenteAceptanElComponenteDeLibreEleccion() {
    DatosDeRegla.DeComponente datos = new DatosDeRegla.DeComponente(libreEleccion);

    assertThat(datos.componente()).isEqualTo(libreEleccion);
  }

  @Test
  void tt03_datosDeEquivalenciaExigenAlMenosUnaMateriaDeOrigen() {
    assertThatThrownBy(() -> new DatosDeRegla.DeEquivalencia(Set.of(), B))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("origen no puede ser vacío");
  }

  @Test
  void tt03_datosDeComponenteRechazanUnaAgrupacion() {
    assertThatThrownBy(() -> new DatosDeRegla.DeComponente(cienciasBasicas))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("componente debe ser un componente de la malla");
  }

  @Test
  void tt03_datosDeReglaRechazanSusDatosObligatoriosNulos() {
    assertThatThrownBy(() -> new DatosDeRegla.DeMateria(null, fundamentacion))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeMateria(materia, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("contenedor no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DePrerrequisito(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("prerrequisito no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeCorrequisito(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("correquisito no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeHabilitacion(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("regla no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeAgrupacion(null, fundamentacion))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("agrupacion no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DePrograma(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditosExigidos no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeEquivalencia(Set.of(A), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("destino no puede ser null");
    assertThatThrownBy(() -> new DatosDeRegla.DeCargaPorPeriodo(new Creditos(10), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
  }

  private List<DatosDeRegla> todas() {
    return List.of(
        new DatosDeRegla.DeMateria(materia, fundamentacion),
        new DatosDeRegla.DePrerrequisito(new Prerrequisito(A, EXPRESION)),
        new DatosDeRegla.DeCorrequisito(new Correquisito(B, EXPRESION)),
        new DatosDeRegla.DeHabilitacion(
            new ReglaDeHabilitacion(A, MedidaDeCreditos.delPrograma(), new Creditos(80))),
        new DatosDeRegla.DeAgrupacion((Agrupacion) cienciasBasicas, fundamentacion),
        new DatosDeRegla.DeComponente(fundamentacion),
        new DatosDeRegla.DePrograma(new Creditos(160)),
        new DatosDeRegla.DeEquivalencia(Set.of(A), B),
        new DatosDeRegla.DeCargaPorPeriodo(new Creditos(10), TipoDeCargaPorPeriodo.MINIMA),
        new DatosDeRegla.DeRequisitoDeGrado(),
        new DatosDeRegla.DeOtraCategoria());
  }

  private static String categoria(DatosDeRegla datos) {
    return switch (datos) {
      case DatosDeRegla.DeMateria ignored -> "materia";
      case DatosDeRegla.DePrerrequisito ignored -> "prerrequisito";
      case DatosDeRegla.DeCorrequisito ignored -> "correquisito";
      case DatosDeRegla.DeHabilitacion ignored -> "habilitación";
      case DatosDeRegla.DeAgrupacion ignored -> "agrupación";
      case DatosDeRegla.DeComponente ignored -> "componente";
      case DatosDeRegla.DePrograma ignored -> "programa";
      case DatosDeRegla.DeEquivalencia ignored -> "equivalencia";
      case DatosDeRegla.DeCargaPorPeriodo ignored -> "carga por periodo";
      case DatosDeRegla.DeRequisitoDeGrado ignored -> "requisito de grado";
      case DatosDeRegla.DeOtraCategoria ignored -> "otra";
    };
  }
}
