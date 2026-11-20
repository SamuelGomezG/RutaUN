package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.List;
import org.junit.jupiter.api.Test;

class MallaTest {

  private final Malla malla = MallaDePrueba.pequena();

  private Creditos creditosDeMaterias(String nombre) {
    return MallaDePrueba.nodo(malla, nombre).creditosDeMaterias();
  }

  @Test
  void tt02_mallaSeRecorreEnProfundidadSinDistinguirHojasDeCompuestos() {
    assertThat(malla.recorrer().map(NodoDeMalla::nombre).toList())
        .containsExactly(
            "Fundamentación",
            "Ciencias básicas",
            "Cómputo",
            "Disciplinar",
            "Profundización",
            "Libre elección",
            "Electivas sugeridas");
  }

  @Test
  void tt02_recorrerUnaHojaDevuelveSoloLaHojaYUnCompuestoDevuelveTodoLoQueContiene() {
    NodoDeMalla hoja = MallaDePrueba.nodo(malla, "Cómputo");
    NodoDeMalla compuesto = MallaDePrueba.nodo(malla, "Ciencias básicas");

    assertThat(hoja.hijos()).isEmpty();
    assertThat(hoja.recorrer().map(NodoDeMalla::nombre).toList()).containsExactly("Cómputo");
    assertThat(compuesto.recorrer().map(NodoDeMalla::nombre).toList())
        .containsExactly("Ciencias básicas", "Cómputo");
  }

  @Test
  void tt02_mallaSumaLosCreditosDeLasMateriasPorComponenteYPorAgrupacion() {
    assertThat(creditosDeMaterias("Fundamentación")).isEqualTo(new Creditos(16));
    assertThat(creditosDeMaterias("Ciencias básicas")).isEqualTo(new Creditos(12));
    assertThat(creditosDeMaterias("Cómputo")).isEqualTo(new Creditos(6));
    assertThat(creditosDeMaterias("Disciplinar")).isEqualTo(new Creditos(12));
    assertThat(creditosDeMaterias("Profundización")).isEqualTo(new Creditos(6));
  }

  @Test
  void tt02_mallaGuardaLosCreditosQueElPlanExigeEnCadaNodo() {
    assertThat(MallaDePrueba.nodo(malla, "Fundamentación").exigencia().exigidos())
        .isEqualTo(new Creditos(10));
    assertThat(MallaDePrueba.nodo(malla, "Ciencias básicas").exigencia().exigidos())
        .isEqualTo(new Creditos(6));
    assertThat(MallaDePrueba.nodo(malla, "Disciplinar").exigencia().exigidos())
        .isEqualTo(new Creditos(9));
    assertThat(malla.creditosExigidos()).isEqualTo(new Creditos(25));
  }

  @Test
  void tt02_laLibreEleccionSeMideEnCreditosYNoEnMaterias() {
    NodoDeMalla libreEleccion = MallaDePrueba.nodo(malla, "Libre elección");

    assertThat(libreEleccion.exigencia().exigidos()).isEqualTo(new Creditos(6));
    assertThat(libreEleccion.materias()).isEmpty();
    assertThat(libreEleccion.creditosDeMaterias()).isEqualTo(new Creditos(0));
  }

  @Test
  void tt02_lasElectivasDeLaLibreEleccionSeConsultanPeroNoSuman() {
    NodoDeMalla libreEleccion = MallaDePrueba.nodo(malla, "Libre elección");

    assertThat(libreEleccion.todasLasMaterias()).hasSize(1);
    assertThat(creditosDeMaterias("Electivas sugeridas")).isEqualTo(new Creditos(2));
  }

  @Test
  void tt02_mallaListaTodasSusMateriasUnaVez() {
    assertThat(malla.materias()).hasSize(10);
    assertThat(malla.materias().stream().map(MateriaEnMalla::codigo).distinct().count())
        .isEqualTo(10L);
  }

  @Test
  void tt02_mallaRechazaUnaAgrupacionSueltaComoComponente() {
    Agrupacion suelta =
        new Agrupacion(
            AgrupacionId.nuevo(),
            "Suelta",
            ExigenciaCreditos.de(new Creditos(3)),
            List.of(),
            List.of());

    assertThatThrownBy(() -> new Malla(List.of(suelta)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una agrupación debe estar dentro de un componente");
  }

  @Test
  void tt02_mallaRechazaComponentesNullOConElementosNull() {
    assertThatThrownBy(() -> new Malla(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("componentes no puede ser null");
    assertThatThrownBy(() -> new Malla(java.util.Arrays.asList((NodoDeMalla) null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("componentes no puede tener elementos null");
  }

  @Test
  void tt02_mallaNoDejaModificarSusComponentes() {
    assertThatThrownBy(() -> malla.componentes().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void uc07_ext6d_unaMallaSinLibreEleccionNoSeConstruye() {
    assertThatThrownBy(() -> new Malla(List.of(fundamentacion(), disciplinar())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("libre elección");
  }

  @Test
  void uc07_ext6d_unaMallaConDosComponentesDeFundamentacionNoSeConstruye() {
    Componente otra =
        new Componente(
            "Otra fundamentación",
            TipoComponente.FUNDAMENTACION,
            ExigenciaCreditos.de(new Creditos(0)),
            List.of(),
            List.of());

    assertThatThrownBy(() -> Malla.de(fundamentacion(), otra, libreEleccion()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2 de fundamentación")
        .hasMessageContaining("0 de disciplinar");
  }

  @Test
  void uc07_ext6d_unaMallaConDosLibresEleccionNoSeConstruye() {
    assertThatThrownBy(
            () ->
                new Malla(
                    List.of(fundamentacion(), disciplinar(), libreEleccion(), libreEleccion())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2 de libre elección");
  }

  @Test
  void tt02_unaAgrupacionRepetidaNoSeAcepta() {
    AgrupacionId id = AgrupacionId.nuevo();
    Agrupacion intermedia =
        agrupacion(AgrupacionId.nuevo(), "Intermedia", List.of(agrupacion(id, "Repetida A")));
    Agrupacion directa = agrupacion(id, "Repetida B");
    Componente fundamentacion =
        componente("Fundamentación", TipoComponente.FUNDAMENTACION, intermedia);
    Componente disciplinar = componente("Disciplinar", TipoComponente.DISCIPLINAR, directa);

    assertThatThrownBy(() -> Malla.de(fundamentacion, disciplinar, libreEleccion()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dos agrupaciones distintas usan el id");
  }

  @Test
  void tt02_unaAgrupacionEnDosPadresNoSeAcepta() {
    Agrupacion compartida = agrupacion(AgrupacionId.nuevo(), "Compartida");
    Componente fundamentacion =
        componente("Fundamentación", TipoComponente.FUNDAMENTACION, compartida);
    Componente disciplinar = componente("Disciplinar", TipoComponente.DISCIPLINAR, compartida);

    assertThatThrownBy(() -> Malla.de(fundamentacion, disciplinar, libreEleccion()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("aparece dos veces");
  }

  @Test
  void tt02_mallaDeExigeSusTresComponentes() {
    assertThatThrownBy(() -> Malla.de(null, disciplinar(), libreEleccion()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fundamentacion no puede ser null");
  }

  private Componente fundamentacion() {
    return (Componente) MallaDePrueba.nodo(malla, "Fundamentación");
  }

  private Componente disciplinar() {
    return (Componente) MallaDePrueba.nodo(malla, "Disciplinar");
  }

  private ComponenteDeLibreEleccion libreEleccion() {
    return (ComponenteDeLibreEleccion) MallaDePrueba.nodo(malla, "Libre elección");
  }

  private static Agrupacion agrupacion(AgrupacionId id, String nombre) {
    return agrupacion(id, nombre, List.of());
  }

  private static Agrupacion agrupacion(AgrupacionId id, String nombre, List<Agrupacion> hijas) {
    return new Agrupacion(id, nombre, ExigenciaCreditos.de(new Creditos(3)), List.of(), hijas);
  }

  private static Componente componente(String nombre, TipoComponente tipo, Agrupacion agrupacion) {
    return new Componente(
        nombre, tipo, ExigenciaCreditos.de(new Creditos(3)), List.of(), List.of(agrupacion));
  }
}
