package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** UC06-HU01-T4 (#132): los artículos se ordenan por su número, no por su texto. */
class OrdenDeArticulosTest {

  private static List<String> ordenados(String... articulos) {
    List<String> lista = new ArrayList<>(List.of(articulos));
    lista.sort(OrdenDeArticulos.INSTANCIA);
    return lista;
  }

  @Test
  void uc06_paso4_elArticuloDosVaAntesQueElDiez() {
    assertThat(ordenados("art. 10", "art. 2", "art. 1", "art. 100"))
        .containsExactly("art. 1", "art. 2", "art. 10", "art. 100");
  }

  @Test
  void uc06_paso4_elTextoQueSigueAlNumeroDesempata() {
    assertThat(ordenados("art. 2 (transitorio)", "art. 2", "art. 3"))
        .containsExactly("art. 2", "art. 2 (transitorio)", "art. 3");
  }

  @Test
  void uc06_paso4_losCerosALaIzquierdaNoCambianElNumero() {
    assertThat(OrdenDeArticulos.INSTANCIA.compare("art. 02", "art. 10")).isNegative();
    assertThat(OrdenDeArticulos.INSTANCIA.compare("art. 02", "art. 2")).isZero();
  }

  @Test
  void uc06_paso4_losNumerosMuyLargosNoDesbordan() {
    assertThat(OrdenDeArticulos.INSTANCIA.compare("art. 99999999999999999999", "art. 1"))
        .isPositive();
  }
}
