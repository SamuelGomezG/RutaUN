package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PrerrequisitoTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");

  @Test
  void tt03_unPrerrequisitoTomaLaCertezaYLasRazonesQueDaElContexto() {
    for (Certeza certeza : Certeza.values()) {
      ContextoDeEvaluacion contexto =
          ContextoDeEvaluacionFijo.nuevo()
              .estado(A, new EstadoRestriccion(certeza, List.of("razón de " + certeza)))
              .construir();

      EstadoRestriccion estado = new Prerrequisito(B, new TerminoMateria(A)).evaluar(contexto);

      assertThat(estado.certeza()).isEqualTo(certeza);
      assertThat(estado.razones()).containsExactly("razón de " + certeza);
    }
  }

  @Test
  void tt03_unaMateriaQueYaNoRigeSinEquivalenciaDejaElPrerrequisitoIncierto() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.nuevo().yaNoRige(A).construir();

    EstadoRestriccion estado = new Prerrequisito(B, new TerminoMateria(A)).evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.INCIERTA);
    assertThat(estado.razones()).containsExactly("A ya no rige");
  }

  @Test
  void tt03_unaYDePrerrequisitosTomaElPeorEstadoYLasRazonesNoCumplidas() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().aprobada(A).enCurso(B).construir();

    EstadoRestriccion estado =
        new Prerrequisito(
                C, ExpresionDeMaterias.todasLas(new TerminoMateria(A), new TerminoMateria(B)))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly("B está en curso");
  }

  @Test
  void tt03_unaODePrerrequisitosTomaElMejorEstadoYLasRazonesDeLaMejorRama() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).aprobada(B).construir();

    EstadoRestriccion estado =
        new Prerrequisito(
                C, ExpresionDeMaterias.algunaDe(new TerminoMateria(A), new TerminoMateria(B)))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly("B está aprobada");
  }

  @Test
  void tt03_unaExpresionAnidadaCombinaYConO() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().aprobada(A).pendiente(B).enCurso(C).construir();

    EstadoRestriccion estado =
        new Prerrequisito(
                A,
                ExpresionDeMaterias.todasLas(
                    new TerminoMateria(A),
                    ExpresionDeMaterias.algunaDe(new TerminoMateria(B), new TerminoMateria(C))))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly("C está en curso");
  }

  @Test
  void tt03_unaODeTresTerminosConservaLasRazonesDeLosMejores() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).aprobada(B).aprobada(C).construir();

    EstadoRestriccion estado =
        new Prerrequisito(
                B,
                ExpresionDeMaterias.algunaDe(
                    new TerminoMateria(A), new TerminoMateria(B), new TerminoMateria(C)))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly("B está aprobada", "C está aprobada");
  }

  @Test
  void tt03_unaMateriaDeNivelacionSeDaPorCumplida() {
    EstadoRestriccion estado =
        evaluarConContextoVacio(
            new Prerrequisito(
                A,
                new MateriaExterna(
                    "Matemáticas Básicas", Optional.empty(), TipoMateriaExterna.NIVELACION)));

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unaMateriaDeOtroProgramaNoSePuedeCumplir() {
    EstadoRestriccion estado =
        evaluarConContextoVacio(
            new Prerrequisito(
                A,
                new MateriaExterna(
                    "Electrónica Análoga I",
                    Optional.of(new CodigoMateria("2016699")),
                    TipoMateriaExterna.OTRO_PROGRAMA)));

    assertThat(estado.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(estado.razones().get(0)).contains("no se puede cursar desde este programa");
  }

  @Test
  void tt03_unPrerrequisitoNoSeCumpleSoloPorqueElPlanLoUbique() {
    Restriccion prerrequisito = new Prerrequisito(B, new TerminoMateria(A));
    ContextoDeEvaluacion ubicado =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).enElMismoSemestre(A).construir();

    assertThat(prerrequisito.evaluar(ubicado).certeza()).isEqualTo(Certeza.PENDIENTE);
  }

  @Test
  void tt03_unPrerrequisitoRechazaUnContextoNulo() {
    Prerrequisito prerrequisito =
        new Prerrequisito(
            A, new MateriaExterna("Externa", Optional.empty(), TipoMateriaExterna.NIVELACION));

    assertThatThrownBy(() -> prerrequisito.evaluar(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("contexto no puede ser null");
  }

  private static EstadoRestriccion evaluarConContextoVacio(Restriccion restriccion) {
    return restriccion.evaluar(ContextoDeEvaluacionFijo.vacio());
  }
}
