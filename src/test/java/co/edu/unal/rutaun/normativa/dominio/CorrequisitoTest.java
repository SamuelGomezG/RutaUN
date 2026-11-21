package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.compartido.Razon;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CorrequisitoTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");

  @Test
  void tt03_unCorrequisitoPendienteSeCumpleSiElPlanLoUbica() {
    Restriccion correquisito = new Correquisito(B, new TerminoMateria(A));
    ContextoDeEvaluacion sinUbicacion = ContextoDeEvaluacionFijo.nuevo().pendiente(A).construir();
    ContextoDeEvaluacion ubicado =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).enElMismoSemestre(A).construir();

    assertThat(correquisito.evaluar(sinUbicacion).certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(correquisito.evaluar(ubicado).certeza()).isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unCorrequisitoUbicadoDaSuRazonEstructurada() {
    ContextoDeEvaluacion ubicado =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).enElMismoSemestre(A).construir();

    EstadoRestriccion estado = new Correquisito(B, new TerminoMateria(A)).evaluar(ubicado);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly(new Razon.UbicadoPorElPlan(A));
  }

  @Test
  void tt03_unCorrequisitoQueNoEstaPendienteConservaSuCertezaAunqueElPlanLoUbique() {
    Restriccion correquisito = new Correquisito(B, new TerminoMateria(A));
    for (Certeza certeza : List.of(Certeza.EN_CURSO, Certeza.INCIERTA)) {
      Razon razon = new Razon.TerminoAprobado(A);
      ContextoDeEvaluacion ubicado =
          ContextoDeEvaluacionFijo.nuevo()
              .estado(A, new EstadoRestriccion(certeza, List.of(razon)))
              .enElMismoSemestre(A)
              .construir();

      EstadoRestriccion estado = correquisito.evaluar(ubicado);

      assertThat(estado.certeza()).isEqualTo(certeza);
      assertThat(estado.razones()).containsExactly(razon);
    }
  }

  @Test
  void tt03_unCorrequisitoRechazaUnContextoNulo() {
    Correquisito correquisito =
        new Correquisito(
            A, new MateriaExterna("Externa", Optional.empty(), TipoMateriaExterna.OTRO_PROGRAMA));

    assertThatThrownBy(() -> correquisito.evaluar(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("contexto no puede ser null");
  }
}
