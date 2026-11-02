package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PeriodoAcademicoTest {

  @Test
  void tt02_periodoAcademicoGuardaAnioYParte() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);

    assertThat(periodo.anio()).isEqualTo(2026);
    assertThat(periodo.parte()).isEqualTo(ParteDelAnio.SEGUNDO);
  }

  @Test
  void tt02_periodoAcademicoRechazaParteNull() {
    assertThatThrownBy(() -> new PeriodoAcademico(2026, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("parte no puede ser null");
  }

  @Test
  void tt02_periodoAcademicoRechazaUnAnioSinCuatroCifras() {
    assertThatThrownBy(() -> new PeriodoAcademico(999, ParteDelAnio.PRIMERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe tener cuatro cifras");
    assertThatThrownBy(() -> new PeriodoAcademico(10000, ParteDelAnio.PRIMERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe tener cuatro cifras");
  }

  @Test
  void tt02_periodoAcademicoSiguienteDeLaPrimeraEsLaSegundaDelMismoAnio() {
    assertThat(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO).siguiente())
        .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.SEGUNDO));
  }

  @Test
  void tt02_periodoAcademicoSiguienteDeLaSegundaEsLaPrimeraDelAnioSiguiente() {
    assertThat(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO).siguiente())
        .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO));
  }

  @Test
  void tt02_periodoAcademicoNoTieneSiguienteMasAllaDelUltimoAnio() {
    PeriodoAcademico ultimo = new PeriodoAcademico(9999, ParteDelAnio.SEGUNDO);

    assertThatThrownBy(ultimo::siguiente).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tt02_periodoAcademicoSeOrdenaPorAnioYLuegoPorParte() {
    PeriodoAcademico p20261 = new PeriodoAcademico(2026, ParteDelAnio.PRIMERO);
    PeriodoAcademico p20262 = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);
    PeriodoAcademico p20271 = new PeriodoAcademico(2027, ParteDelAnio.PRIMERO);

    assertThat(List.of(p20271, p20261, p20262).stream().sorted())
        .containsExactly(p20261, p20262, p20271);
  }

  @Test
  void tt02_periodoAcademicoIgualCompararDaCero() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);

    assertThat(periodo.compareTo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO))).isZero();
  }

  @Test
  void tt02_periodoAcademicoRechazaCompararConNull() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);

    assertThatThrownBy(() -> periodo.compareTo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otro no puede ser null");
  }
}
