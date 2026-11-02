package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FacultadTest {

  @Test
  void tt02_facultadRechazaNull() {
    assertThatThrownBy(() -> new Facultad(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser null");
  }

  @Test
  void tt02_facultadRechazaNombreVacio() {
    assertThatThrownBy(() -> new Facultad("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
  }

  @Test
  void tt02_facultadGuardaElNombreYSeIgualaPorNombre() {
    assertThat(new Facultad("Ingeniería").nombre()).isEqualTo("Ingeniería");
    assertThat(new Facultad("Ingeniería")).isEqualTo(new Facultad("Ingeniería"));
  }
}
