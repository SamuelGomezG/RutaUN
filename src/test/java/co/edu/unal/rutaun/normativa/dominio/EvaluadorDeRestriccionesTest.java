package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaDelPrograma;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.O;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.Y;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EvaluadorDeRestriccionesTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");
  private static final MedidaDeCreditos PROGRAMA = MedidaDeCreditos.delPrograma();
  private static final MedidaDeCreditos DISCIPLINAR =
      MedidaDeCreditos.deComponente(TipoComponente.DISCIPLINAR);

  @Test
  void tt03_unPrerrequisitoTomaLaCertezaYLasRazonesQueDaElContexto() {
    for (Certeza certeza : Certeza.values()) {
      ContextoDeEvaluacion contexto =
          ContextoDeEvaluacionFijo.nuevo()
              .estado(A, new EstadoRestriccion(certeza, List.of("razón de " + certeza)))
              .construir();

      EstadoRestriccion estado =
          EvaluadorDeRestricciones.evaluar(
              new Prerrequisito(B, new MateriaDelPrograma(A)), contexto);

      assertThat(estado.certeza()).isEqualTo(certeza);
      assertThat(estado.razones()).containsExactly("razón de " + certeza);
    }
  }

  @Test
  void tt03_unaMateriaQueYaNoRigeSinEquivalenciaDejaElPrerrequisitoIncierto() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.nuevo().yaNoRige(A).construir();

    EstadoRestriccion estado =
        EvaluadorDeRestricciones.evaluar(new Prerrequisito(B, new MateriaDelPrograma(A)), contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.INCIERTA);
    assertThat(estado.razones()).containsExactly("A ya no rige");
  }

  @Test
  void tt03_unaYDePrerrequisitosTomaElPeorEstadoYLasRazonesNoCumplidas() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().aprobada(A).enCurso(B).construir();

    EstadoRestriccion estado =
        EvaluadorDeRestricciones.evaluar(
            new Prerrequisito(C, new Y(new MateriaDelPrograma(A), new MateriaDelPrograma(B))),
            contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly("B está en curso");
  }

  @Test
  void tt03_unaODePrerrequisitosTomaElMejorEstadoYLasRazonesDeLaMejorRama() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).aprobada(B).construir();

    EstadoRestriccion estado =
        EvaluadorDeRestricciones.evaluar(
            new Prerrequisito(C, new O(new MateriaDelPrograma(A), new MateriaDelPrograma(B))),
            contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly("B está aprobada");
  }

  @Test
  void tt03_unaExpresionAnidadaCombinaYConO() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().aprobada(A).pendiente(B).enCurso(C).construir();

    EstadoRestriccion estado =
        EvaluadorDeRestricciones.evaluar(
            new Prerrequisito(
                A,
                new Y(
                    new MateriaDelPrograma(A),
                    new O(new MateriaDelPrograma(B), new MateriaDelPrograma(C)))),
            contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.EN_CURSO);
    assertThat(estado.razones()).containsExactly("C está en curso");
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
  void tt03_unCorrequisitoPendienteSeCumpleSiElPlanLoUbica() {
    Restriccion correquisito = new Correquisito(B, new MateriaDelPrograma(A));
    ContextoDeEvaluacion sinUbicacion = ContextoDeEvaluacionFijo.nuevo().pendiente(A).construir();
    ContextoDeEvaluacion ubicado =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).enElMismoSemestre(A).construir();

    assertThat(EvaluadorDeRestricciones.evaluar(correquisito, sinUbicacion).certeza())
        .isEqualTo(Certeza.PENDIENTE);
    assertThat(EvaluadorDeRestricciones.evaluar(correquisito, ubicado).certeza())
        .isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unPrerrequisitoNoSeCumpleSoloPorqueElPlanLoUbique() {
    Restriccion prerrequisito = new Prerrequisito(B, new MateriaDelPrograma(A));
    ContextoDeEvaluacion ubicado =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).enElMismoSemestre(A).construir();

    assertThat(EvaluadorDeRestricciones.evaluar(prerrequisito, ubicado).certeza())
        .isEqualTo(Certeza.PENDIENTE);
  }

  @Test
  void tt03_unaReglaDeHabilitacionComparaCreditosConElUmbral() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, PROGRAMA, new Creditos(40));
    ContextoDeEvaluacion insuficiente =
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(39).construir();
    ContextoDeEvaluacion suficiente =
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(40).construir();

    assertThat(EvaluadorDeRestricciones.evaluar(habilitacion, insuficiente).certeza())
        .isEqualTo(Certeza.PENDIENTE);
    assertThat(EvaluadorDeRestricciones.evaluar(habilitacion, suficiente).certeza())
        .isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unaReglaDeHabilitacionDeComponenteUsaSusCreditos() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, DISCIPLINAR, new Creditos(20));
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo()
            .creditosDelComponente(TipoComponente.DISCIPLINAR, 20)
            .creditosDelPrograma(5)
            .construir();

    EstadoRestriccion estado = EvaluadorDeRestricciones.evaluar(habilitacion, contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones().get(0)).contains("del componente DISCIPLINAR");
  }

  @Test
  void tt03_unaYDeTerminosCumplidosConservaSusRazones() {
    EstadoRestriccion estado =
        EvaluadorDeRestricciones.combinarY(
            EstadoRestriccion.cumplida("A está aprobada"),
            EstadoRestriccion.cumplida("B está aprobada"));

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones()).containsExactly("A está aprobada", "B está aprobada");
  }

  @Test
  void tt03_todasLasCombinacionesDeDosEstadosSiguenElOrdenDeCerteza() {
    for (Certeza izquierda : Certeza.values()) {
      for (Certeza derecha : Certeza.values()) {
        Certeza peor = izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
        Certeza mejor = izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;

        assertThat(EvaluadorDeRestricciones.combinarY(estado(izquierda), estado(derecha)).certeza())
            .isEqualTo(peor);
        assertThat(EvaluadorDeRestricciones.combinarO(estado(izquierda), estado(derecha)).certeza())
            .isEqualTo(mejor);
      }
    }
  }

  @Test
  void tt03_elEvaluadorRechazaRestriccionOContextoNulos() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.vacio();

    assertThatThrownBy(() -> EvaluadorDeRestricciones.evaluar(null, contexto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("restriccion no puede ser null");
    assertThatThrownBy(
            () ->
                EvaluadorDeRestricciones.evaluar(
                    new Prerrequisito(A, new MateriaDelPrograma(B)), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("contexto no puede ser null");
  }

  private static EstadoRestriccion estado(Certeza certeza) {
    return new EstadoRestriccion(certeza, List.of(certeza.name()));
  }

  private static EstadoRestriccion evaluarConContextoVacio(Restriccion restriccion) {
    return EvaluadorDeRestricciones.evaluar(restriccion, ContextoDeEvaluacionFijo.vacio());
  }
}
