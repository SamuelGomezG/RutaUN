package co.edu.unal.rutaun.utilidades;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/** Humo (D8): comprueba que jqwik descubre propiedades con JUnit 6. */
class ArgumentChecksPropiedadesTest {

  @Property
  void tt01_obligatorioNoNegativoAceptaNoNegativos(
      @ForAll @IntRange(min = 0, max = 1000) int valor) {
    assertThat(ArgumentChecks.obligatorioNoNegativo(valor, "creditos")).isEqualTo(valor);
  }
}
