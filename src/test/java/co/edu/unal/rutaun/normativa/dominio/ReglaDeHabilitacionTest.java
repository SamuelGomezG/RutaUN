package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import org.junit.jupiter.api.Test;

class ReglaDeHabilitacionTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final MedidaDeCreditos PROGRAMA = MedidaDeCreditos.delPrograma();
  private static final MedidaDeCreditos DISCIPLINAR =
      MedidaDeCreditos.deComponente(TipoComponente.DISCIPLINAR);

  @Test
  void tt03_unaReglaDeHabilitacionComparaCreditosConElUmbral() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, PROGRAMA, new Creditos(40));
    ContextoDeEvaluacion insuficiente =
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(39).construir();
    ContextoDeEvaluacion suficiente =
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(40).construir();

    assertThat(habilitacion.evaluar(insuficiente).certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(habilitacion.evaluar(suficiente).certeza()).isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unaReglaDeHabilitacionDeComponenteUsaSusCreditos() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, DISCIPLINAR, new Creditos(20));
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo()
            .creditosDelComponente(TipoComponente.DISCIPLINAR, 20)
            .creditosDelPrograma(5)
            .construir();

    EstadoRestriccion estado = habilitacion.evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones().get(0)).contains("del componente DISCIPLINAR");
  }

  @Test
  void tt03_unaReglaDeHabilitacionRechazaUnContextoNulo() {
    ReglaDeHabilitacion habilitacion = new ReglaDeHabilitacion(A, PROGRAMA, new Creditos(40));

    assertThatThrownBy(() -> habilitacion.evaluar(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("contexto no puede ser null");
  }
}
