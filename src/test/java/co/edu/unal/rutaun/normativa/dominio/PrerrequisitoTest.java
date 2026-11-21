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

class PrerrequisitoTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");

  @Test
  void tt03_unPrerrequisitoTomaLaCertezaYLasRazonesQueDaElContexto() {
    for (Certeza certeza : Certeza.values()) {
      Razon razon = new Razon.TerminoAprobado(A);
      ContextoDeEvaluacion contexto =
          ContextoDeEvaluacionFijo.nuevo()
              .estado(A, new EstadoRestriccion(certeza, List.of(razon)))
              .construir();

      EstadoRestriccion estado = new Prerrequisito(B, new TerminoMateria(A)).evaluar(contexto);

      assertThat(estado.certeza()).isEqualTo(certeza);
      assertThat(estado.razones()).containsExactly(razon);
    }
  }

  @Test
  void tt03_unaMateriaQueYaNoRigeSinEquivalenciaDejaElPrerrequisitoIncierto() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.nuevo().yaNoRige(A).construir();

    EstadoRestriccion estado = new Prerrequisito(B, new TerminoMateria(A)).evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.INCIERTA);
    assertThat(estado.razones()).containsExactly(new Razon.TerminoQueYaNoRige(A));
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
    assertThat(estado.razones()).containsExactly(new Razon.TerminoEnCurso(B));
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
    assertThat(estado.razones()).containsExactly(new Razon.TerminoAprobado(B));
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
    assertThat(estado.razones()).containsExactly(new Razon.TerminoEnCurso(C));
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
    assertThat(estado.razones())
        .containsExactly(new Razon.TerminoAprobado(B), new Razon.TerminoAprobado(C));
  }

  @Test
  void uc05_ext4k_unaMateriaDeNivelacionSeDaPorCumplidaYNoSeLista() {
    MateriaExterna nivelacion =
        new MateriaExterna("Matemáticas Básicas", Optional.empty(), TipoMateriaExterna.NIVELACION);

    EstadoRestriccion sola = evaluarConContextoVacio(new Prerrequisito(A, nivelacion));

    assertThat(sola.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(sola.razones()).isEmpty();

    EstadoRestriccion conPendiente =
        new Prerrequisito(A, ExpresionDeMaterias.todasLas(nivelacion, new TerminoMateria(B)))
            .evaluar(ContextoDeEvaluacionFijo.nuevo().pendiente(B).construir());

    assertThat(conPendiente.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(conPendiente.razones()).containsExactly(new Razon.TerminoPendiente(B));

    EstadoRestriccion conAprobada =
        new Prerrequisito(A, ExpresionDeMaterias.todasLas(nivelacion, new TerminoMateria(B)))
            .evaluar(ContextoDeEvaluacionFijo.nuevo().aprobada(B).construir());

    assertThat(conAprobada.certeza()).isEqualTo(Certeza.CUMPLIDA);
    assertThat(conAprobada.razones()).containsExactly(new Razon.TerminoAprobado(B));
  }

  @Test
  void uc05_ext4k_unaRamaODeOtroProgramaNoAparece() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.nuevo().pendiente(A).construir();
    MateriaExterna electronica =
        new MateriaExterna(
            "Electrónica Análoga I",
            Optional.of(new CodigoMateria("2016695")),
            TipoMateriaExterna.OTRO_PROGRAMA);

    EstadoRestriccion estado =
        new Prerrequisito(A, ExpresionDeMaterias.algunaDe(new TerminoMateria(A), electronica))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(estado.razones()).containsExactly(new Razon.TerminoPendiente(A));
    assertThat(estado.razones().toString()).doesNotContain("Electrónica").doesNotContain("2016695");
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
    assertThat(estado.razones()).containsExactly(new Razon.NoCursableDesdeElPrograma());
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

  @Test
  void uc05_ext4k_siSoloQuedaUnaMateriaDeOtroProgramaLaMateriaNoEsCursable() {
    MateriaExterna externaX = materiaDeOtroPrograma("Externa X", "8000011");
    MateriaExterna externaY = materiaDeOtroPrograma("Externa Y", "8000012");

    EstadoRestriccion sola = evaluarConContextoVacio(new Prerrequisito(A, externaX));

    assertThat(sola.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(sola.razones()).containsExactly(new Razon.NoCursableDesdeElPrograma());

    EstadoRestriccion alguna =
        evaluarConContextoVacio(
            new Prerrequisito(A, ExpresionDeMaterias.algunaDe(externaX, externaY)));

    assertThat(alguna.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(alguna.razones()).containsExactly(new Razon.NoCursableDesdeElPrograma());
  }

  @Test
  void uc05_ext4k_unaRamaYConUnaMateriaDeOtroProgramaNoAparece() {
    MateriaExterna externa = materiaDeOtroPrograma("Externa X", "8000011");
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().aprobada(A).pendiente(B).construir();

    EstadoRestriccion estado =
        new Prerrequisito(
                A,
                ExpresionDeMaterias.algunaDe(
                    ExpresionDeMaterias.todasLas(new TerminoMateria(A), externa),
                    new TerminoMateria(B)))
            .evaluar(contexto);

    assertThat(estado.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(estado.razones()).containsExactly(new Razon.TerminoPendiente(B));
  }

  @Test
  void uc05_ext4k_unaYConUnaMateriaDeOtroProgramaSoloDiceNoCursable() {
    MateriaExterna externaX = materiaDeOtroPrograma("Externa X", "8000011");
    MateriaExterna externaY = materiaDeOtroPrograma("Externa Y", "8000012");
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo().pendiente(A).pendiente(B).construir();

    EstadoRestriccion unaY =
        new Prerrequisito(A, ExpresionDeMaterias.todasLas(new TerminoMateria(A), externaX))
            .evaluar(contexto);

    assertThat(unaY.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(unaY.razones()).containsExactly(new Razon.NoCursableDesdeElPrograma());

    EstadoRestriccion dosRamasNoCumplibles =
        new Prerrequisito(
                A,
                ExpresionDeMaterias.algunaDe(
                    ExpresionDeMaterias.todasLas(new TerminoMateria(A), externaX),
                    ExpresionDeMaterias.todasLas(new TerminoMateria(B), externaY)))
            .evaluar(contexto);

    assertThat(dosRamasNoCumplibles.certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(dosRamasNoCumplibles.razones())
        .containsExactly(new Razon.NoCursableDesdeElPrograma());
  }

  private static MateriaExterna materiaDeOtroPrograma(String nombre, String codigo) {
    return new MateriaExterna(
        nombre, Optional.of(new CodigoMateria(codigo)), TipoMateriaExterna.OTRO_PROGRAMA);
  }

  private static EstadoRestriccion evaluarConContextoVacio(Restriccion restriccion) {
    return restriccion.evaluar(ContextoDeEvaluacionFijo.vacio());
  }
}
