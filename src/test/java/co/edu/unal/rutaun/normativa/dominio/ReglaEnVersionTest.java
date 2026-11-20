package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReglaEnVersionTest {

  private static Regla regla() {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(NormaId.nuevo(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        EstadoRegla.VIGENTE,
        Optional.empty(),
        new DatosDeRegla.DePrograma(new Creditos(160)));
  }

  @Test
  void tt02_unaReglaEnVersionExigeReglaYEstado() {
    Regla regla = regla();

    assertThatThrownBy(() -> new ReglaEnVersion(null, EstadoRegla.VIGENTE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("regla no puede ser null");
    assertThatThrownBy(() -> new ReglaEnVersion(regla, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estado no puede ser null");
  }

  @Test
  void tt02_laMismaReglaConOtroEstadoEsOtraReglaEnVersion() {
    Regla regla = regla();
    ReglaEnVersion programada = new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA);
    ReglaEnVersion vigente = new ReglaEnVersion(regla, EstadoRegla.VIGENTE);

    assertThat(programada)
        .isNotEqualTo(vigente)
        .isEqualTo(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA))
        .hasSameHashCodeAs(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA));
  }
}
