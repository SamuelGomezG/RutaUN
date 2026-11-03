package co.edu.unal.rutaun.spikes.motor.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConjuntoDeMateriasTest {

  @Test
  void tt05_conjuntoAgregaQuitaYCuenta() {
    ConjuntoDeMaterias conjunto = new ConjuntoDeMaterias(90);
    conjunto.agregar(3);
    conjunto.agregar(70);
    assertThat(conjunto.contiene(3)).isTrue();
    assertThat(conjunto.contiene(70)).isTrue();
    assertThat(conjunto.contiene(4)).isFalse();
    assertThat(conjunto.cardinalidad()).isEqualTo(2);
    conjunto.quitar(3);
    assertThat(conjunto.contiene(3)).isFalse();
    assertThat(conjunto.cardinalidad()).isEqualTo(1);
  }

  @Test
  void tt05_conjuntosCruzanLaPalabraDe64Bits() {
    ConjuntoDeMaterias a = ConjuntoDeMaterias.de(90, 0, 64, 89);
    ConjuntoDeMaterias b = ConjuntoDeMaterias.de(90, 64, 89);
    ConjuntoDeMaterias copia = a.copia();
    copia.intersectar(b);
    assertThat(copia.cardinalidad()).isEqualTo(2);
    assertThat(copia.contiene(64)).isTrue();
    assertThat(copia.contiene(89)).isTrue();
    assertThat(a.contieneAlguna(new long[] {1L, 0L})).isTrue();
  }
}
