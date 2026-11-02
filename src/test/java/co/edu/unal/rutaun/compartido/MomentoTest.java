package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MomentoTest {

  @Test
  void tt02_momentoEnPeriodoRechazaPeriodoNull() {
    assertThatThrownBy(() -> new Momento.EnPeriodo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("periodo no puede ser null");
  }

  @Test
  void tt02_momentoEnFechaRechazaFechaNull() {
    assertThatThrownBy(() -> new Momento.EnFecha(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fecha no puede ser null");
  }

  @Test
  void tt02_momentoGuardaElPeriodoOLaFecha() {
    PeriodoAcademico periodo = new PeriodoAcademico(2027, ParteDelAnio.PRIMERO);
    LocalDate fecha = LocalDate.of(2027, 3, 15);

    assertThat(new Momento.EnPeriodo(periodo).periodo()).isEqualTo(periodo);
    assertThat(new Momento.EnFecha(fecha).fecha()).isEqualTo(fecha);
  }

  @Test
  void tt02_momentoConLosMismosDatosEsIgual() {
    PeriodoAcademico periodo = new PeriodoAcademico(2027, ParteDelAnio.PRIMERO);

    assertThat(new Momento.EnPeriodo(periodo)).isEqualTo(new Momento.EnPeriodo(periodo));
    assertThat(new Momento.EnFecha(LocalDate.of(2027, 3, 15)))
        .isEqualTo(new Momento.EnFecha(LocalDate.of(2027, 3, 15)));
  }

  @Test
  void tt02_momentoSeAnalizaConUnSwitchExhaustivo() {
    Momento deFecha = new Momento.EnFecha(LocalDate.of(2027, 3, 15));
    Momento dePeriodo = new Momento.EnPeriodo(new PeriodoAcademico(2027, ParteDelAnio.SEGUNDO));

    assertThat(describir(deFecha)).isEqualTo("fecha 2027-03-15");
    assertThat(describir(dePeriodo)).isEqualTo("periodo 2027-SEGUNDO");
  }

  private static String describir(Momento momento) {
    return switch (momento) {
      case Momento.EnPeriodo p -> "periodo " + p.periodo().anio() + "-" + p.periodo().parte();
      case Momento.EnFecha f -> "fecha " + f.fecha();
    };
  }
}
