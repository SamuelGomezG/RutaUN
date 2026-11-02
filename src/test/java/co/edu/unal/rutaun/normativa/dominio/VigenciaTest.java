package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VigenciaTest {

  private static final PeriodoAcademico P2026_2 = new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO);

  @Test
  void tt03_vigenciaGuardaElInicioYElFin() {
    Momento inicio = new Momento.EnPeriodo(P2026_2);
    Momento fin = new Momento.EnFecha(LocalDate.of(2027, 6, 30));

    Vigencia vigencia = new Vigencia(inicio, Optional.of(fin));

    assertThat(vigencia.aplicaDesde()).isEqualTo(inicio);
    assertThat(vigencia.vigenteHasta()).contains(fin);
  }

  @Test
  void tt03_vigenciaSinFinNoEsTemporal() {
    Vigencia vigencia = new Vigencia(new Momento.EnPeriodo(P2026_2), Optional.empty());

    assertThat(vigencia.esTemporal()).isFalse();
    assertThat(vigencia.vigenteHasta()).isEmpty();
  }

  @Test
  void tt03_vigenciaConFinEsTemporal() {
    Vigencia vigencia =
        new Vigencia(
            new Momento.EnPeriodo(P2026_2),
            Optional.of(new Momento.EnPeriodo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO))));

    assertThat(vigencia.esTemporal()).isTrue();
  }

  @Test
  void tt03_vigenciaRechazaInicioNuloYOptionalNulo() {
    assertThatThrownBy(() -> new Vigencia(null, Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("aplicaDesde no puede ser null");
    assertThatThrownBy(() -> new Vigencia(new Momento.EnPeriodo(P2026_2), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigenteHasta no puede ser null");
  }

  @Test
  void tt03_vigenciaRechazaUnFinAnteriorAlInicioCuandoAmbosSonPeriodos() {
    Momento inicio = new Momento.EnPeriodo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO));
    Momento fin = new Momento.EnPeriodo(P2026_2);

    assertThatThrownBy(() -> new Vigencia(inicio, Optional.of(fin)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigenteHasta no puede ser anterior a aplicaDesde");
  }

  @Test
  void tt03_vigenciaRechazaUnFinAnteriorAlInicioCuandoAmbosSonFechas() {
    Momento inicio = new Momento.EnFecha(LocalDate.of(2027, 3, 15));
    Momento fin = new Momento.EnFecha(LocalDate.of(2027, 3, 14));

    assertThatThrownBy(() -> new Vigencia(inicio, Optional.of(fin)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigenteHasta no puede ser anterior a aplicaDesde");
  }

  @Test
  void tt03_vigenciaNoComparaUnPeriodoConUnaFecha() {
    Vigencia vigencia =
        new Vigencia(
            new Momento.EnFecha(LocalDate.of(2027, 3, 15)),
            Optional.of(new Momento.EnPeriodo(P2026_2)));

    assertThat(vigencia.esTemporal()).isTrue();
  }
}
