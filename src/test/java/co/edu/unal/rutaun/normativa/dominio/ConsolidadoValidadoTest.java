package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConsolidadoValidadoTest {

  private static Regla regla() {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(NormaId.nuevo(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        EstadoRegla.VIGENTE,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }

  @Test
  void tt02_consolidadoValidadoGuardaElProgramaYSusReglas() {
    ProgramaId programa = ProgramaId.nuevo();
    Regla regla = regla();

    ConsolidadoValidado validado = new ConsolidadoValidado(programa, List.of(regla));

    assertThat(validado.programa()).isEqualTo(programa);
    assertThat(validado.reglas()).containsExactly(regla);
  }

  @Test
  void tt02_consolidadoValidadoNoSeAfectaPorCambiosEnLaListaQueRecibio() {
    Regla regla = regla();
    List<Regla> reglas = new ArrayList<>(List.of(regla));
    ConsolidadoValidado validado = new ConsolidadoValidado(ProgramaId.nuevo(), reglas);

    reglas.clear();

    assertThat(validado.reglas()).containsExactly(regla);
  }

  @Test
  void tt02_consolidadoValidadoNoDejaModificarSusReglas() {
    ConsolidadoValidado validado = new ConsolidadoValidado(ProgramaId.nuevo(), List.of(regla()));

    assertThatThrownBy(() -> validado.reglas().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt02_consolidadoValidadoRechazaDatosNull() {
    assertThatThrownBy(() -> new ConsolidadoValidado(null, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("programa no puede ser null");
    assertThatThrownBy(() -> new ConsolidadoValidado(ProgramaId.nuevo(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("reglas no puede ser null");
  }
}
