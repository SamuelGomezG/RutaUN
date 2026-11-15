package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * UC06-HU01-T4 (#132): qué estados muestra la consulta y cuándo una regla es «temporal», sin base
 * de datos.
 */
class ReglaVisibleYTemporalTest {

  private static final LocalDate HASTA = LocalDate.of(2030, 1, 1);

  private static Vigencia vigencia(boolean conFin) {
    return new Vigencia(
        new Momento.EnFecha(LocalDate.of(2023, 1, 11)),
        conFin ? Optional.of(new Momento.EnFecha(HASTA)) : Optional.empty());
  }

  @Test
  void uc06_paso4_seMuestranLasVigentesLasProgramadasYLasEnRevision() {
    assertThat(ConsultaNormativaService.seMuestra(EstadoRegla.VIGENTE)).isTrue();
    assertThat(ConsultaNormativaService.seMuestra(EstadoRegla.PROGRAMADA)).isTrue();
    assertThat(ConsultaNormativaService.seMuestra(EstadoRegla.EN_REVISION)).isTrue();
  }

  @Test
  void uc06_paso4_noSeMuestranLasQueYaNoRigen() {
    assertThat(ConsultaNormativaService.seMuestra(EstadoRegla.YA_NO_RIGE)).isFalse();
  }

  @Test
  void uc06_paso4_esTemporalSiLaVigenciaPropiaTieneFin() {
    assertThat(ConsultaNormativaService.esTemporal(Optional.of(vigencia(true)), null)).isTrue();
  }

  @Test
  void uc06_paso4_esTemporalSiLaNormaTieneVigenteHasta() {
    assertThat(ConsultaNormativaService.esTemporal(Optional.empty(), HASTA)).isTrue();
    assertThat(ConsultaNormativaService.esTemporal(Optional.of(vigencia(false)), HASTA)).isTrue();
  }

  @Test
  void uc06_paso4_noEsTemporalSiNiLaReglaNiLaNormaTienenFin() {
    assertThat(ConsultaNormativaService.esTemporal(Optional.empty(), null)).isFalse();
    assertThat(ConsultaNormativaService.esTemporal(Optional.of(vigencia(false)), null)).isFalse();
  }
}
