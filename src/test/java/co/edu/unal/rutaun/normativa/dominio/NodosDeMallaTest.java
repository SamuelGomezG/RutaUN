package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NodosDeMallaTest {

  private static final ExigenciaCreditos TRES = ExigenciaCreditos.de(new Creditos(3));
  private static final MateriaEnMalla FISICA =
      MateriaEnMalla.sinSemestre(
          new CodigoMateria("9000002"), "Física I", new Creditos(3), Obligatoriedad.OPTATIVA);

  @Test
  void tt02_agrupacionRechazaDatosInvalidos() {
    AgrupacionId id = AgrupacionId.nuevo();

    assertThatThrownBy(() -> new Agrupacion(null, "A", TRES, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("id no puede ser null");
    assertThatThrownBy(() -> new Agrupacion(id, " ", TRES, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
    assertThatThrownBy(() -> new Agrupacion(id, "A", null, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("exigencia no puede ser null");
    assertThatThrownBy(() -> new Agrupacion(id, "A", TRES, null, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materias no puede ser null");
    assertThatThrownBy(() -> new Agrupacion(id, "A", TRES, List.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("subagrupaciones no puede ser null");
  }

  @Test
  void tt02_componenteRechazaDatosInvalidos() {
    assertThatThrownBy(
            () -> new Componente(null, TipoComponente.DISCIPLINAR, TRES, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser null");
    assertThatThrownBy(() -> new Componente("C", null, TRES, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(
            () -> new Componente("C", TipoComponente.DISCIPLINAR, null, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("exigencia no puede ser null");
    assertThatThrownBy(() -> new Componente("C", TipoComponente.DISCIPLINAR, TRES, null, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materias no puede ser null");
    assertThatThrownBy(() -> new Componente("C", TipoComponente.DISCIPLINAR, TRES, List.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("agrupaciones no puede ser null");
  }

  @Test
  void tt02_componenteDeLibreEleccionRechazaDatosInvalidos() {
    assertThatThrownBy(() -> new ComponenteDeLibreEleccion("", TRES, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
    assertThatThrownBy(() -> new ComponenteDeLibreEleccion("L", null, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("exigencia no puede ser null");
    assertThatThrownBy(() -> new ComponenteDeLibreEleccion("L", TRES, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("agrupaciones no puede ser null");
  }

  @Test
  void tt02_unNodoSinHijosEsUnaHojaYSusMateriasSonLasSuyas() {
    Agrupacion hoja =
        new Agrupacion(AgrupacionId.nuevo(), "Hoja", TRES, List.of(FISICA), List.of());

    assertThat(hoja.hijos()).isEmpty();
    assertThat(hoja.todasLasMaterias()).containsExactly(FISICA);
    assertThat(hoja.creditosDeMaterias()).isEqualTo(new Creditos(3));
  }

  @Test
  void tt02_unaMateriaUbicadaEnVariasAgrupacionesSeSumaEnCadaUna() {
    Agrupacion una = new Agrupacion(AgrupacionId.nuevo(), "Una", TRES, List.of(FISICA), List.of());
    Agrupacion otra =
        new Agrupacion(AgrupacionId.nuevo(), "Otra", TRES, List.of(FISICA), List.of());
    Componente componente =
        new Componente("C", TipoComponente.DISCIPLINAR, TRES, List.of(), List.of(una, otra));

    assertThat(componente.creditosDeMaterias()).isEqualTo(new Creditos(6));
    assertThat(componente.todasLasMaterias()).hasSize(2);
  }

  @Test
  void tt02_losNodosNoSeAfectanPorCambiosEnLasListasQueRecibieron() {
    List<MateriaEnMalla> materias = new ArrayList<>(List.of(FISICA));
    Agrupacion agrupacion = new Agrupacion(AgrupacionId.nuevo(), "A", TRES, materias, List.of());

    materias.clear();

    assertThat(agrupacion.materias()).containsExactly(FISICA);
  }

  @Test
  void tt02_losNodosNoDejanModificarSusListas() {
    Agrupacion agrupacion =
        new Agrupacion(AgrupacionId.nuevo(), "A", TRES, List.of(FISICA), List.of());
    Componente componente =
        new Componente("C", TipoComponente.DISCIPLINAR, TRES, List.of(FISICA), List.of(agrupacion));

    assertThatThrownBy(() -> agrupacion.materias().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> componente.agrupaciones().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> componente.hijos().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
