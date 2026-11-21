package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

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
        .hasMessageContaining("anio debe estar entre 1000 y 9999");
    assertThatThrownBy(() -> new PeriodoAcademico(10000, ParteDelAnio.PRIMERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("anio debe estar entre 1000 y 9999");
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
  void tt02_noHaySiguienteDespuesDelUltimoPeriodo() {
    PeriodoAcademico ultimo = new PeriodoAcademico(9999, ParteDelAnio.SEGUNDO);

    assertThatThrownBy(ultimo::siguiente)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("no hay periodo después de 9999-2");
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
  void tt02_compararConNullLanzaNullPointerException() {
    PeriodoAcademico periodo = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);

    assertThatThrownBy(() -> periodo.compareTo(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("otro");
  }

  @Test
  void tt02_unPeriodoSeEscribeComoAAAA1() {
    assertThat(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO).toString()).isEqualTo("2027-1");
  }

  @Test
  void sop04_periodoAcademicoSeCreaDesdeElTextoDeLaPrimeraParte() {
    assertThat(PeriodoAcademico.desdeTexto("2026-1"))
        .isEqualTo(new PeriodoAcademico(2026, ParteDelAnio.PRIMERO));
  }

  @Test
  void sop04_periodoAcademicoSeCreaDesdeElTextoDeLaSegundaParte() {
    assertThat(PeriodoAcademico.desdeTexto("2027-2"))
        .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.SEGUNDO));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "2027-3",
        "27-1",
        "2027/1",
        "2026",
        "2026-0",
        "20261",
        "2026/1",
        "abcd-1",
        "2026-1-1",
        "2026-12",
        "   "
      })
  @NullSource
  void tt02_unTextoInvalidoNoEsUnPeriodo(String texto) {
    assertThatThrownBy(() -> PeriodoAcademico.desdeTexto(texto))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tt02_unTextoConOtroFormatoExplicaElFormatoEsperado() {
    assertThatThrownBy(() -> PeriodoAcademico.desdeTexto("2026-1-1"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AAAA-1 o AAAA-2");
  }

  @Test
  void tt02_unPeriodoConEspaciosAlrededorSeLee() {
    assertThat(PeriodoAcademico.desdeTexto(" 2026-2 "))
        .isEqualTo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO));
  }

  @Test
  void sop04_periodoAcademicoRechazaUnAnioSinCuatroCifrasSignificativas() {
    assertThatThrownBy(() -> PeriodoAcademico.desdeTexto("0999-1"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
