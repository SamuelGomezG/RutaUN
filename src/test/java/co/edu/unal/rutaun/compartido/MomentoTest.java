package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MomentoTest {

  @Test
  void tt02_momentoDePeriodoRechazaPeriodoNull() {
    assertThatThrownBy(() -> new Momento.DePeriodo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("periodo no puede ser null");
  }

  @Test
  void tt02_momentoDeFechaRechazaFechaNull() {
    assertThatThrownBy(() -> new Momento.DeFecha(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fecha no puede ser null");
  }

  @Test
  void tt02_momentoGuardaElPeriodoOLaFecha() {
    PeriodoAcademico periodo = new PeriodoAcademico(2027, ParteDelAnio.PRIMERA);
    LocalDate fecha = LocalDate.of(2027, 3, 15);

    assertThat(new Momento.DePeriodo(periodo).periodo()).isEqualTo(periodo);
    assertThat(new Momento.DeFecha(fecha).fecha()).isEqualTo(fecha);
  }

  @Test
  void tt02_momentoConLosMismosDatosEsIgual() {
    PeriodoAcademico periodo = new PeriodoAcademico(2027, ParteDelAnio.PRIMERA);

    assertThat(new Momento.DePeriodo(periodo)).isEqualTo(new Momento.DePeriodo(periodo));
    assertThat(new Momento.DeFecha(LocalDate.of(2027, 3, 15)))
        .isEqualTo(new Momento.DeFecha(LocalDate.of(2027, 3, 15)));
  }

  @Test
  void tt02_momentoSeAnalizaConUnSwitchExhaustivo() {
    Momento deFecha = new Momento.DeFecha(LocalDate.of(2027, 3, 15));
    Momento dePeriodo = new Momento.DePeriodo(new PeriodoAcademico(2027, ParteDelAnio.SEGUNDA));

    assertThat(describir(deFecha)).isEqualTo("fecha 2027-03-15");
    assertThat(describir(dePeriodo)).isEqualTo("periodo 2027-SEGUNDA");
  }

  private static String describir(Momento momento) {
    return switch (momento) {
      case Momento.DePeriodo p -> "periodo " + p.periodo().anio() + "-" + p.periodo().parte();
      case Momento.DeFecha f -> "fecha " + f.fecha();
    };
  }
}
