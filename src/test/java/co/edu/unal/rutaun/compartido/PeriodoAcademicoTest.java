package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PeriodoAcademicoTest {

  @Test
  void tt02_periodoAcademicoGuardaAnioYParte() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA);

    assertThat(periodo.anio()).isEqualTo(2026);
    assertThat(periodo.parte()).isEqualTo(ParteDelAnio.SEGUNDA);
  }

  @Test
  void tt02_periodoAcademicoRechazaParteNull() {
    assertThatThrownBy(() -> new PeriodoAcademico(2026, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("parte no puede ser null");
  }

  @Test
  void tt02_periodoAcademicoRechazaUnAnioSinCuatroCifras() {
    assertThatThrownBy(() -> new PeriodoAcademico(999, ParteDelAnio.PRIMERA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe tener cuatro cifras");
    assertThatThrownBy(() -> new PeriodoAcademico(10000, ParteDelAnio.PRIMERA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe tener cuatro cifras");
  }

  @Test
  void tt02_periodoAcademicoSiguienteDeLaPrimeraEsLaSegundaDelMismoAnio() {
    assertThat(new PeriodoAcademico(2027, ParteDelAnio.PRIMERA).siguiente())
        .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.SEGUNDA));
  }

  @Test
  void tt02_periodoAcademicoSiguienteDeLaSegundaEsLaPrimeraDelAnioSiguiente() {
    assertThat(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA).siguiente())
        .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERA));
  }

  @Test
  void tt02_periodoAcademicoNoTieneSiguienteMasAllaDelUltimoAnio() {
    PeriodoAcademico ultimo = new PeriodoAcademico(9999, ParteDelAnio.SEGUNDA);

    assertThatThrownBy(ultimo::siguiente).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tt02_periodoAcademicoSeOrdenaPorAnioYLuegoPorParte() {
    PeriodoAcademico p20261 = new PeriodoAcademico(2026, ParteDelAnio.PRIMERA);
    PeriodoAcademico p20262 = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA);
    PeriodoAcademico p20271 = new PeriodoAcademico(2027, ParteDelAnio.PRIMERA);

    assertThat(List.of(p20271, p20261, p20262).stream().sorted())
        .containsExactly(p20261, p20262, p20271);
  }

  @Test
  void tt02_periodoAcademicoIgualCompararDaCero() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA);

    assertThat(periodo.compareTo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA))).isZero();
  }

  @Test
  void tt02_periodoAcademicoRechazaCompararConNull() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDA);

    assertThatThrownBy(() -> periodo.compareTo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
  }
}
