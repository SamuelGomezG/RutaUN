package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BusquedaDeReglasTest {

  @Test
  void uc06_paso6_laBusquedaSinTextoNiFiltrosEsElListadoNormal() {
    assertThat(BusquedaDeReglas.TODAS.esVacia()).isTrue();
    assertThat(BusquedaDeReglas.de(null, null, null)).isEqualTo(BusquedaDeReglas.TODAS);
  }

  @Test
  void uc06_paso6_elTextoLaCategoriaOElEstadoLaHacenUnaBusqueda() {
    assertThat(BusquedaDeReglas.de("créditos", null, null).esVacia()).isFalse();
    assertThat(BusquedaDeReglas.de(null, "Materias", null).esVacia()).isFalse();
    assertThat(BusquedaDeReglas.de(null, null, EstadoRegla.YA_NO_RIGE).esVacia()).isFalse();
  }

  @Test
  void uc06_paso6_elTextoYLaCategoriaSeRecortanYLosVaciosSeDescartan() {
    BusquedaDeReglas busqueda = BusquedaDeReglas.de("  créditos  ", " Materias ", null);

    assertThat(busqueda.texto()).contains("créditos");
    assertThat(busqueda.categoria()).contains("Materias");
    assertThat(BusquedaDeReglas.de("   ", "", null).esVacia()).isTrue();
  }

  @Test
  void uc06_paso6_laBusquedaGuardaElEstadoPedido() {
    assertThat(BusquedaDeReglas.de(null, null, EstadoRegla.PROGRAMADA).estado())
        .contains(EstadoRegla.PROGRAMADA);
  }

  @Test
  void uc06_paso6_laBusquedaRechazaDatosNulos() {
    assertThatThrownBy(() -> new BusquedaDeReglas(null, Optional.empty(), Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("texto no puede ser null");
    assertThatThrownBy(() -> new BusquedaDeReglas(Optional.empty(), null, Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("categoria no puede ser null");
    assertThatThrownBy(() -> new BusquedaDeReglas(Optional.empty(), Optional.empty(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estado no puede ser null");
  }
}
