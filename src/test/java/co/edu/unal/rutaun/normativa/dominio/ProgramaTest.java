package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.ProgramaId;
import org.junit.jupiter.api.Test;

class ProgramaTest {

  private static final Facultad INGENIERIA = new Facultad("Ingeniería");

  @Test
  void tt02_programaRegistrarGuardaSusDatos() {
    Programa programa = Programa.registrar("Ingeniería de Sistemas", "2879", INGENIERIA);

    assertThat(programa.nombre()).isEqualTo("Ingeniería de Sistemas");
    assertThat(programa.codigoSia()).isEqualTo("2879");
    assertThat(programa.facultad()).isEqualTo(INGENIERIA);
  }

  @Test
  void tt02_programaRegistrarGeneraUnIdentificadorNuevoCadaVez() {
    Programa uno = Programa.registrar("Ingeniería de Sistemas", "2879", INGENIERIA);
    Programa otro = Programa.registrar("Ingeniería de Sistemas", "2879", INGENIERIA);

    assertThat(uno.id()).isNotEqualTo(otro.id());
    assertThat(uno).isNotEqualTo(otro);
  }

  @Test
  void tt02_programaRegistrarRechazaDatosInvalidos() {
    assertThatThrownBy(() -> Programa.registrar(null, "2879", INGENIERIA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser null");
    assertThatThrownBy(() -> Programa.registrar(" ", "2879", INGENIERIA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
    assertThatThrownBy(() -> Programa.registrar("Ingeniería de Sistemas", "", INGENIERIA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigoSia no puede ser vacío");
    assertThatThrownBy(() -> Programa.registrar("Ingeniería de Sistemas", "2879", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("facultad no puede ser null");
  }

  @Test
  void tt02_programaReconstituirConservaElIdentificador() {
    ProgramaId id = ProgramaId.nuevo();

    Programa programa = Programa.reconstituir(id, "Ingeniería de Sistemas", "2879", INGENIERIA);

    assertThat(programa.id()).isEqualTo(id);
    assertThat(programa.nombre()).isEqualTo("Ingeniería de Sistemas");
  }

  @Test
  void tt02_programaReconstituirRechazaIdentificadorNull() {
    assertThatThrownBy(
            () -> Programa.reconstituir(null, "Ingeniería de Sistemas", "2879", INGENIERIA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("id no puede ser null");
  }

  @Test
  void tt02_programaConElMismoIdentificadorEsElMismoPrograma() {
    ProgramaId id = ProgramaId.nuevo();

    Programa uno = Programa.reconstituir(id, "Ingeniería de Sistemas", "2879", INGENIERIA);
    Programa otro = Programa.reconstituir(id, "Sistemas y Computación", "2879", INGENIERIA);

    assertThat(uno).isEqualTo(otro).hasSameHashCodeAs(otro);
  }
}
