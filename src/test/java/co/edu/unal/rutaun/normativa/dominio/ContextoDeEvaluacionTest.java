package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContextoDeEvaluacionTest {

  private static final CodigoMateria A = new CodigoMateria("A");
  private static final CodigoMateria B = new CodigoMateria("B");
  private static final CodigoMateria C = new CodigoMateria("C");
  private static final MedidaDeCreditos PROGRAMA = MedidaDeCreditos.delPrograma();
  private static final MedidaDeCreditos DISCIPLINAR = MedidaDeCreditos.deComponente("Disciplinar");

  @Test
  void tt03_elContextoGuardaEstadosCreditosYMateriasDelMismoSemestre() {
    ContextoDeEvaluacion contexto =
        new ContextoDeEvaluacion(
            Map.of(A, EstadoDeMateria.APROBADA, B, EstadoDeMateria.EN_CURSO),
            Map.of(PROGRAMA, new Creditos(40), DISCIPLINAR, new Creditos(12)),
            Set.of(C));

    assertThat(contexto.estadoDe(A)).isEqualTo(EstadoDeMateria.APROBADA);
    assertThat(contexto.estadoDe(B)).isEqualTo(EstadoDeMateria.EN_CURSO);
    assertThat(contexto.creditosAprobadosPara(PROGRAMA)).isEqualTo(new Creditos(40));
    assertThat(contexto.creditosAprobadosPara(DISCIPLINAR)).isEqualTo(new Creditos(12));
    assertThat(contexto.vaEnElMismoSemestre(C)).isTrue();
  }

  @Test
  void tt03_unaMateriaSinEstadoFiguraPendienteYUnaMedidaSinCreditosValeCero() {
    ContextoDeEvaluacion contexto = new ContextoDeEvaluacion(Map.of(), Map.of(), Set.of());

    assertThat(contexto.estadoDe(A)).isEqualTo(EstadoDeMateria.PENDIENTE);
    assertThat(contexto.creditosAprobadosPara(PROGRAMA)).isEqualTo(new Creditos(0));
    assertThat(contexto.vaEnElMismoSemestre(A)).isFalse();
  }

  @Test
  void tt03_elContextoNoDejaModificarSusMapasNiSuConjunto() {
    ContextoDeEvaluacion contexto =
        new ContextoDeEvaluacion(Map.of(A, EstadoDeMateria.APROBADA), Map.of(), Set.of());

    assertThatThrownBy(() -> contexto.estadosDeMaterias().put(B, EstadoDeMateria.PENDIENTE))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> contexto.creditosAprobados().put(PROGRAMA, new Creditos(1)))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> contexto.materiasEnElMismoSemestre().add(B))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt03_elContextoRechazaDatosNulos() {
    assertThatThrownBy(() -> new ContextoDeEvaluacion(null, Map.of(), Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estadosDeMaterias no puede ser null");
    assertThatThrownBy(() -> new ContextoDeEvaluacion(Map.of(), null, Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditosAprobados no puede ser null");
    assertThatThrownBy(() -> new ContextoDeEvaluacion(Map.of(), Map.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materiasEnElMismoSemestre no puede ser null");
  }
}
