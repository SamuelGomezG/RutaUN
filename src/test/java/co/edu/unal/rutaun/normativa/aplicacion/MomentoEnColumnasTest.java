package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.normativa.persistencia.ColumnasDeMomento;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MomentoEnColumnasTest {

  static Stream<Arguments> momentos() {
    return Stream.of(
        Arguments.of(
            new Momento.EnFecha(LocalDate.of(2023, 1, 11)),
            new ColumnasDeMomento("FECHA", LocalDate.of(2023, 1, 11), null)),
        Arguments.of(
            new Momento.EnPeriodo(new PeriodoAcademico(2009, ParteDelAnio.PRIMERO)),
            new ColumnasDeMomento("PERIODO", null, "2009-1")));
  }

  @ParameterizedTest
  @MethodSource("momentos")
  void tt04_unMomentoPasaAColumnasYVuelveIgual(Momento momento, ColumnasDeMomento columnas) {
    assertThat(MomentoEnColumnas.aColumnas(momento)).isEqualTo(columnas);
    assertThat(MomentoEnColumnas.aMomento(columnas)).isEqualTo(momento);
  }

  @Test
  void tt04_columnasInvalidasNoSonUnMomento() {
    assertThatThrownBy(
            () ->
                MomentoEnColumnas.aMomento(
                    new ColumnasDeMomento("OTRO", LocalDate.of(2023, 1, 11), null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("tipo de momento desconocido");
    assertThatThrownBy(() -> MomentoEnColumnas.aMomento(new ColumnasDeMomento("FECHA", null, null)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> MomentoEnColumnas.aMomento(new ColumnasDeMomento("PERIODO", null, "2009-3")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tt04_unMomentoAusenteSonTresColumnasNulas() {
    assertThat(MomentoEnColumnas.aColumnas(null))
        .isEqualTo(new ColumnasDeMomento(null, null, null));
    assertThat(MomentoEnColumnas.aMomento(new ColumnasDeMomento(null, null, null))).isNull();
  }
}
