package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * UC07-HU03-T4 (#130): unión de reglas de {@link ImportacionDeNormasService}. El lector genera un
 * id nuevo por regla, así que el rechazo por id repetido no se puede provocar desde un archivo:
 * estas pruebas ejercitan la invariante del método.
 */
class ImportacionDeNormasServiceReglasAcumuladasTest {

  @Test
  void uc07_hu03_t4_laUnionConservaElOrdenDeLaVersionAnteriorYLasNuevas() {
    ReglaEnVersion anterior = enVersion("Créditos del programa", EstadoRegla.VIGENTE);
    ReglaEnVersion nueva = enVersion("Componentes del programa", EstadoRegla.VIGENTE);

    assertThat(ImportacionDeNormasService.reglasAcumuladas(List.of(anterior), List.of(nueva)))
        .containsExactly(anterior, nueva);
  }

  @Test
  void uc07_hu03_t4_laUnionConservaElEstadoDeLasReglasDeLaVersionAnterior() {
    ReglaEnVersion anterior = enVersion("Créditos del programa", EstadoRegla.YA_NO_RIGE);
    ReglaEnVersion nueva = enVersion("Componentes del programa", EstadoRegla.VIGENTE);

    assertThat(ImportacionDeNormasService.reglasAcumuladas(List.of(anterior), List.of(nueva)))
        .containsExactly(anterior, nueva);
  }

  @Test
  void uc07_hu03_t4_sinVersionAnteriorLaUnionSoloTieneLasNuevas() {
    ReglaEnVersion nueva = enVersion("Componentes del programa", EstadoRegla.VIGENTE);

    assertThat(ImportacionDeNormasService.reglasAcumuladas(List.of(), List.of(nueva)))
        .containsExactly(nueva);
  }

  @Test
  void uc07_hu03_t4_unReglaIdDeLaVersionAnteriorSeRechaza() {
    Regla regla = regla("Créditos del programa");
    ReglaEnVersion anterior = new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA);
    ReglaEnVersion nueva = new ReglaEnVersion(regla, EstadoRegla.VIGENTE);

    assertThatThrownBy(
            () -> ImportacionDeNormasService.reglasAcumuladas(List.of(anterior), List.of(nueva)))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("Créditos del programa")
        .hasMessageContaining("art. 1");
  }

  @Test
  void uc07_hu03_t4_unReglaIdRepetidoEntreLasNuevasSeRechaza() {
    ReglaEnVersion nueva = enVersion("Componentes del programa", EstadoRegla.VIGENTE);

    assertThatThrownBy(
            () -> ImportacionDeNormasService.reglasAcumuladas(List.of(), List.of(nueva, nueva)))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("Componentes del programa")
        .hasMessageContaining("está repetida en la norma que se importa");
  }

  private static ReglaEnVersion enVersion(String objeto, EstadoRegla estado) {
    return new ReglaEnVersion(regla(objeto), estado);
  }

  private static Regla regla(String objeto) {
    return Regla.registrar(
        objeto,
        "Enunciado de " + objeto,
        new Fuente(NormaId.nuevo(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.INFORMATIVA,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }
}
