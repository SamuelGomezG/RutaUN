package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.dominio.EstadoRestriccion.Certeza;
import java.util.List;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;

class EvaluadorDeRestriccionesPropiedadesTest {

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_yTomaElPeorEstadoYEsConmutativa(@ForAll Certeza izquierda, @ForAll Certeza derecha) {
    EstadoRestriccion estadoIzquierda = estado(izquierda);
    EstadoRestriccion estadoDerecha = estado(derecha);

    EstadoRestriccion combinado =
        EvaluadorDeRestricciones.combinarY(estadoIzquierda, estadoDerecha);
    EstadoRestriccion conmutado =
        EvaluadorDeRestricciones.combinarY(estadoDerecha, estadoIzquierda);

    assertThat(combinado.certeza()).isEqualTo(peor(izquierda, derecha));
    assertThat(conmutado.certeza()).isEqualTo(combinado.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_oTomaElMejorEstadoYEsConmutativa(@ForAll Certeza izquierda, @ForAll Certeza derecha) {
    EstadoRestriccion estadoIzquierda = estado(izquierda);
    EstadoRestriccion estadoDerecha = estado(derecha);

    EstadoRestriccion combinado =
        EvaluadorDeRestricciones.combinarO(estadoIzquierda, estadoDerecha);
    EstadoRestriccion conmutado =
        EvaluadorDeRestricciones.combinarO(estadoDerecha, estadoIzquierda);

    assertThat(combinado.certeza()).isEqualTo(mejor(izquierda, derecha));
    assertThat(conmutado.certeza()).isEqualTo(combinado.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_yEsAsociativa(
      @ForAll Certeza primera, @ForAll Certeza segunda, @ForAll Certeza tercera) {
    EstadoRestriccion a = estado(primera);
    EstadoRestriccion b = estado(segunda);
    EstadoRestriccion c = estado(tercera);

    EstadoRestriccion porLaIzquierda =
        EvaluadorDeRestricciones.combinarY(EvaluadorDeRestricciones.combinarY(a, b), c);
    EstadoRestriccion porLaDerecha =
        EvaluadorDeRestricciones.combinarY(a, EvaluadorDeRestricciones.combinarY(b, c));

    assertThat(porLaIzquierda.certeza()).isEqualTo(porLaDerecha.certeza());
  }

  @Property(tries = 1000, generation = GenerationMode.RANDOMIZED)
  void tt03_oEsAsociativa(
      @ForAll Certeza primera, @ForAll Certeza segunda, @ForAll Certeza tercera) {
    EstadoRestriccion a = estado(primera);
    EstadoRestriccion b = estado(segunda);
    EstadoRestriccion c = estado(tercera);

    EstadoRestriccion porLaIzquierda =
        EvaluadorDeRestricciones.combinarO(EvaluadorDeRestricciones.combinarO(a, b), c);
    EstadoRestriccion porLaDerecha =
        EvaluadorDeRestricciones.combinarO(a, EvaluadorDeRestricciones.combinarO(b, c));

    assertThat(porLaIzquierda.certeza()).isEqualTo(porLaDerecha.certeza());
  }

  private static EstadoRestriccion estado(Certeza certeza) {
    return new EstadoRestriccion(certeza, List.of(certeza.name()));
  }

  private static Certeza peor(Certeza izquierda, Certeza derecha) {
    return izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
  }

  private static Certeza mejor(Certeza izquierda, Certeza derecha) {
    return izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;
  }
}
