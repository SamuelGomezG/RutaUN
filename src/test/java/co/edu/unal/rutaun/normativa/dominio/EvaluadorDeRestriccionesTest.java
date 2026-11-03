package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaDelPrograma;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.O;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.Y;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EvaluadorDeRestriccionesTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");
  private static final MedidaDeCreditos PROGRAMA = MedidaDeCreditos.delPrograma();
  private static final MedidaDeCreditos DISCIPLINAR = MedidaDeCreditos.deComponente("Disciplinar");

  @Test
  void tt03_unPrerrequisitoSeEvaluaSegunElEstadoDeLaMateria() {
    Map<EstadoDeMateria, Certeza> esperados =
        Map.of(
            EstadoDeMateria.APROBADA, Certeza.CUMPLIDA,
            EstadoDeMateria.EN_CURSO, Certeza.EN_CURSO,
            EstadoDeMateria.PENDIENTE, Certeza.PENDIENTE,
            EstadoDeMateria.YA_NO_RIGE, Certeza.INCIERTA);

    for (Map.Entry<EstadoDeMateria, Certeza> esperado : esperados.entrySet()) {
      ContextoDeEvaluacion contexto = contexto(Map.of(A, esperado.getKey()));
      EstadoRestriccion estado =
          EvaluadorDeRestricciones.evaluar(
              new Prerrequisito(B, new MateriaDelPrograma(A)), contexto);

      assertThat(estado.certeza()).isEqualTo(esperado.getValue());
    }
  }

  @Test
  void tt03_unaYDePrerrequisitosTomaElPeorEstadoYLasRazonesNoCumplidas() {
    ContextoDeEvaluacion contexto =
        contexto(Map.of(A, EstadoDeMateria.APROBADA, B, EstadoDeMateria.EN_CURSO));

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
        contexto(Map.of(A, EstadoDeMateria.PENDIENTE, B, EstadoDeMateria.APROBADA));

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
        contexto(
            Map.of(
                A, EstadoDeMateria.APROBADA,
                B, EstadoDeMateria.PENDIENTE,
                C, EstadoDeMateria.EN_CURSO));

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
                    "Matemáticas Básicas", Optional.empty(), TipoDeMateriaExterna.NIVELACION)));

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
                    TipoDeMateriaExterna.OTRO_PROGRAMA)));

    assertThat(estado.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(estado.razones().get(0)).contains("no se puede cursar desde este programa");
  }

  @Test
  void tt03_unCorrequisitoPendienteSeCumpleSiElPlanLoUbica() {
    Restriccion correquisito = new Correquisito(B, new MateriaDelPrograma(A));
    ContextoDeEvaluacion sinUbicacion = contexto(Map.of(A, EstadoDeMateria.PENDIENTE));
    ContextoDeEvaluacion ubicado =
        new ContextoDeEvaluacion(Map.of(A, EstadoDeMateria.PENDIENTE), Map.of(), Set.of(A));

    assertThat(EvaluadorDeRestricciones.evaluar(correquisito, sinUbicacion).certeza())
        .isEqualTo(Certeza.PENDIENTE);
    assertThat(EvaluadorDeRestricciones.evaluar(correquisito, ubicado).certeza())
        .isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unPrerrequisitoNoSeCumpleSoloPorqueElPlanLoUbique() {
    Restriccion prerrequisito = new Prerrequisito(B, new MateriaDelPrograma(A));
    ContextoDeEvaluacion ubicado =
        new ContextoDeEvaluacion(Map.of(A, EstadoDeMateria.PENDIENTE), Map.of(), Set.of(A));

    assertThat(EvaluadorDeRestricciones.evaluar(prerrequisito, ubicado).certeza())
        .isEqualTo(Certeza.PENDIENTE);
  }

  @Test
  void tt03_unaReglaDeHabilitacionComparaCreditosConElUmbral() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, PROGRAMA, new Creditos(40));
    ContextoDeEvaluacion insuficiente =
        new ContextoDeEvaluacion(Map.of(), Map.of(PROGRAMA, new Creditos(39)), Set.of());
    ContextoDeEvaluacion suficiente =
        new ContextoDeEvaluacion(Map.of(), Map.of(PROGRAMA, new Creditos(40)), Set.of());

    assertThat(EvaluadorDeRestricciones.evaluar(habilitacion, insuficiente).certeza())
        .isEqualTo(Certeza.PENDIENTE);
    assertThat(EvaluadorDeRestricciones.evaluar(habilitacion, suficiente).certeza())
        .isEqualTo(Certeza.CUMPLIDA);
  }

  @Test
  void tt03_unaReglaDeHabilitacionDeComponenteUsaSusCreditos() {
    Restriccion habilitacion = new ReglaDeHabilitacion(A, DISCIPLINAR, new Creditos(20));
    ContextoDeEvaluacion contexto =
        new ContextoDeEvaluacion(Map.of(), Map.of(DISCIPLINAR, new Creditos(20)), Set.of());

    EstadoRestriccion estado = EvaluadorDeRestricciones.evaluar(habilitacion, contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(estado.razones().get(0)).contains("del componente Disciplinar");
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
    ContextoDeEvaluacion contexto = contexto(Map.of());

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
    return new EstadoRestriccion(certeza, java.util.List.of(certeza.name()));
  }

  private static EstadoRestriccion evaluarConContextoVacio(Restriccion restriccion) {
    return EvaluadorDeRestricciones.evaluar(restriccion, contexto(Map.of()));
  }

  private static ContextoDeEvaluacion contexto(
      Map<CodigoMateria, EstadoDeMateria> estadosDeMaterias) {
    return new ContextoDeEvaluacion(estadosDeMaterias, Map.of(), Set.of());
  }
}
