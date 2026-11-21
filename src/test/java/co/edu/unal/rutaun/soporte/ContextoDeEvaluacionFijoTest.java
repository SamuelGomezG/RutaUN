package co.edu.unal.rutaun.soporte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.compartido.Razon;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContextoDeEvaluacionFijoTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");
  private static final CodigoMateria D = new CodigoMateria("D");

  @Test
  void tt03_elContextoFijoImplementaLaInterfazDeNormativa() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.vacio();

    assertThat(contexto).isInstanceOf(ContextoDeEvaluacion.class);
  }

  @Test
  void tt03_elConstructorDeMapasGuardaEstadosCreditosYMateriasDelMismoSemestre() {
    ContextoDeEvaluacion contexto =
        new ContextoDeEvaluacionFijo(
            Map.of(
                A,
                EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A)),
                B,
                EstadoRestriccion.enCurso(new Razon.TerminoEnCurso(B))),
            Map.of(TipoComponente.DISCIPLINAR, new Creditos(12)),
            new Creditos(40),
            Set.of(C));

    assertThat(contexto.estadoDe(A))
        .isEqualTo(EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A)));
    assertThat(contexto.estadoDe(B))
        .isEqualTo(EstadoRestriccion.enCurso(new Razon.TerminoEnCurso(B)));
    assertThat(contexto.creditosAprobadosDelComponente(TipoComponente.DISCIPLINAR))
        .isEqualTo(new Creditos(12));
    assertThat(contexto.creditosAprobadosDelPrograma()).isEqualTo(new Creditos(40));
    assertThat(contexto.enElMismoSemestre(C)).isTrue();
  }

  @Test
  void tt03_elConstructorDeContextosArmaCadaEstadoConSuCertezaYSuRazon() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo()
            .aprobada(A)
            .enCurso(B)
            .yaNoRige(C)
            .pendiente(D)
            .construir();

    assertThat(contexto.estadoDe(A))
        .isEqualTo(EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A)));
    assertThat(contexto.estadoDe(B))
        .isEqualTo(EstadoRestriccion.enCurso(new Razon.TerminoEnCurso(B)));
    assertThat(contexto.estadoDe(C))
        .isEqualTo(EstadoRestriccion.incierta(new Razon.TerminoQueYaNoRige(C)));
    assertThat(contexto.estadoDe(D))
        .isEqualTo(EstadoRestriccion.pendiente(new Razon.TerminoPendiente(D)));
  }

  @Test
  void tt03_elConstructorDeContextosFijaCreditosYMateriasDelMismoSemestre() {
    ContextoDeEvaluacion contexto =
        ContextoDeEvaluacionFijo.nuevo()
            .creditosDelPrograma(80)
            .creditosDelComponente(TipoComponente.FUNDAMENTACION, 30)
            .enElMismoSemestre(A)
            .construir();

    assertThat(contexto.creditosAprobadosDelPrograma()).isEqualTo(new Creditos(80));
    assertThat(contexto.creditosAprobadosDelComponente(TipoComponente.FUNDAMENTACION))
        .isEqualTo(new Creditos(30));
    assertThat(contexto.enElMismoSemestre(A)).isTrue();
    assertThat(contexto.enElMismoSemestre(B)).isFalse();
  }

  @Test
  void tt03_unaMateriaSinEstadoFiguraPendienteYLosCreditosSinDatoValenCero() {
    ContextoDeEvaluacion contexto = ContextoDeEvaluacionFijo.vacio();

    assertThat(contexto.estadoDe(A).certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(contexto.estadoDe(A).razones()).containsExactly(new Razon.TerminoPendiente(A));
    assertThat(contexto.creditosAprobadosDelPrograma()).isEqualTo(new Creditos(0));
    assertThat(contexto.creditosAprobadosDelComponente(TipoComponente.DISCIPLINAR))
        .isEqualTo(new Creditos(0));
    assertThat(contexto.enElMismoSemestre(A)).isFalse();
  }

  @Test
  void tt03_elContextoNoCambiaSiSeModificanLosMapasConLosQueSeArmo() {
    Map<CodigoMateria, EstadoRestriccion> estados = new HashMap<>();
    estados.put(A, EstadoRestriccion.cumplida(new Razon.TerminoAprobado(A)));
    Set<CodigoMateria> mismoSemestre = new HashSet<>();
    ContextoDeEvaluacion contexto =
        new ContextoDeEvaluacionFijo(estados, Map.of(), new Creditos(0), mismoSemestre);

    estados.put(B, EstadoRestriccion.cumplida(new Razon.TerminoAprobado(B)));
    mismoSemestre.add(C);

    assertThat(contexto.estadoDe(B).certeza()).isEqualTo(Certeza.PENDIENTE);
    assertThat(contexto.enElMismoSemestre(C)).isFalse();
  }

  @Test
  void tt03_elContextoRechazaDatosNulos() {
    assertThatThrownBy(
            () -> new ContextoDeEvaluacionFijo(null, Map.of(), new Creditos(0), Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estados no puede ser null");
    assertThatThrownBy(
            () -> new ContextoDeEvaluacionFijo(Map.of(), null, new Creditos(0), Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditosPorComponente no puede ser null");
    assertThatThrownBy(() -> new ContextoDeEvaluacionFijo(Map.of(), Map.of(), null, Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditosDelPrograma no puede ser null");
    assertThatThrownBy(
            () -> new ContextoDeEvaluacionFijo(Map.of(), Map.of(), new Creditos(0), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materiasEnElMismoSemestre no puede ser null");
  }
}
