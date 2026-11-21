package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;

class EstadoRestriccionPropiedadesTest {

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_yTomaElPeorEstadoYEsConmutativa(
      @ForAll EstadoRestriccion.Certeza izquierda, @ForAll EstadoRestriccion.Certeza derecha) {
    EstadoRestriccion estadoIzquierda = estado(izquierda);
    EstadoRestriccion estadoDerecha = estado(derecha);

    EstadoRestriccion combinado = estadoIzquierda.y(estadoDerecha);
    EstadoRestriccion conmutado = estadoDerecha.y(estadoIzquierda);

    assertThat(combinado.certeza()).isEqualTo(peor(izquierda, derecha));
    assertThat(conmutado.certeza()).isEqualTo(combinado.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_oTomaElMejorEstadoYEsConmutativa(
      @ForAll EstadoRestriccion.Certeza izquierda, @ForAll EstadoRestriccion.Certeza derecha) {
    EstadoRestriccion estadoIzquierda = estado(izquierda);
    EstadoRestriccion estadoDerecha = estado(derecha);

    EstadoRestriccion combinado = estadoIzquierda.o(estadoDerecha);
    EstadoRestriccion conmutado = estadoDerecha.o(estadoIzquierda);

    assertThat(combinado.certeza()).isEqualTo(mejor(izquierda, derecha));
    assertThat(conmutado.certeza()).isEqualTo(combinado.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_yEsAsociativa(
      @ForAll EstadoRestriccion.Certeza primera,
      @ForAll EstadoRestriccion.Certeza segunda,
      @ForAll EstadoRestriccion.Certeza tercera) {
    EstadoRestriccion a = estado(primera);
    EstadoRestriccion b = estado(segunda);
    EstadoRestriccion c = estado(tercera);

    EstadoRestriccion porLaIzquierda = a.y(b).y(c);
    EstadoRestriccion porLaDerecha = a.y(b.y(c));

    assertThat(porLaIzquierda.certeza()).isEqualTo(porLaDerecha.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_oEsAsociativa(
      @ForAll EstadoRestriccion.Certeza primera,
      @ForAll EstadoRestriccion.Certeza segunda,
      @ForAll EstadoRestriccion.Certeza tercera) {
    EstadoRestriccion a = estado(primera);
    EstadoRestriccion b = estado(segunda);
    EstadoRestriccion c = estado(tercera);

    EstadoRestriccion porLaIzquierda = a.o(b).o(c);
    EstadoRestriccion porLaDerecha = a.o(b.o(c));

    assertThat(porLaIzquierda.certeza()).isEqualTo(porLaDerecha.certeza());
  }

  private static EstadoRestriccion estado(EstadoRestriccion.Certeza certeza) {
    return new EstadoRestriccion(certeza, List.of(certeza.name()));
  }

  private static EstadoRestriccion.Certeza peor(
      EstadoRestriccion.Certeza izquierda, EstadoRestriccion.Certeza derecha) {
    return izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
  }

  private static EstadoRestriccion.Certeza mejor(
      EstadoRestriccion.Certeza izquierda, EstadoRestriccion.Certeza derecha) {
    return izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;
  }
}
