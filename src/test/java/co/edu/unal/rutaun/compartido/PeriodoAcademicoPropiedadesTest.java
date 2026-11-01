package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

class PeriodoAcademicoPropiedadesTest {

  @Property
  void tt02_elSiguienteSiempreEsPosteriorYNoSaltaPeriodos(
      @ForAll @IntRange(min = 1000, max = 9998) int anio, @ForAll ParteDelAnio parte) {
    PeriodoAcademico periodo = new PeriodoAcademico(anio, parte);
    PeriodoAcademico siguiente = periodo.siguiente();

    assertThat(siguiente).isGreaterThan(periodo);
    assertThat(siguiente.siguiente()).isGreaterThan(siguiente);
    int semestres = (siguiente.anio() - periodo.anio()) * 2;
    semestres += siguiente.parte().numero() - periodo.parte().numero();
    assertThat(semestres).isEqualTo(1);
  }
}
