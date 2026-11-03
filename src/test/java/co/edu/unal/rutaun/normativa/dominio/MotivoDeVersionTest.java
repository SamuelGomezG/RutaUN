package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MotivoDeVersionTest {

  @Test
  void tt02_cadaMotivoGuardaSusPropiosDatos() {
    NormaId norma = NormaId.nuevo();
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);
    LocalDate fecha = LocalDate.of(2026, 10, 1);

    assertThat(new MotivoDeVersion.PorIncorporacion(norma, "Ana").norma()).isEqualTo(norma);
    assertThat(new MotivoDeVersion.PorCorreccion("Ana").administrador()).isEqualTo("Ana");
    assertThat(new MotivoDeVersion.AutomaticoPorPeriodo(periodo).periodo()).isEqualTo(periodo);
    assertThat(new MotivoDeVersion.AutomaticoPorFecha(fecha).fecha()).isEqualTo(fecha);
  }

  @Test
  void tt02_motivoRechazaDatosFaltantes() {
    assertThatThrownBy(() -> new MotivoDeVersion.PorIncorporacion(null, "Ana"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("norma no puede ser null");
    assertThatThrownBy(() -> new MotivoDeVersion.PorIncorporacion(NormaId.nuevo(), " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("administrador no puede ser vacío");
    assertThatThrownBy(() -> new MotivoDeVersion.PorCorreccion(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("administrador no puede ser null");
    assertThatThrownBy(() -> new MotivoDeVersion.AutomaticoPorPeriodo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("periodo no puede ser null");
    assertThatThrownBy(() -> new MotivoDeVersion.AutomaticoPorFecha(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fecha no puede ser null");
  }
}
