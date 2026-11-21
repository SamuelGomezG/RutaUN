package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.Razon;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementación de apoyo de {@link ContextoDeEvaluacion} para las pruebas y las demostraciones: el
 * estado de cada materia, los créditos y las materias del mismo semestre quedan fijos desde la
 * construcción. Una materia sin estado figura pendiente y unos créditos sin dato valen cero.
 *
 * <p>Se arma con el constructor de mapas o, más cómodo, con {@link #nuevo()}:
 *
 * <pre>{@code
 * ContextoDeEvaluacion contexto =
 *     ContextoDeEvaluacionFijo.nuevo().aprobada(A).enCurso(B).creditosDelPrograma(40).construir();
 * }</pre>
 */
public final class ContextoDeEvaluacionFijo implements ContextoDeEvaluacion {

  private final Map<CodigoMateria, EstadoRestriccion> estados;
  private final Map<TipoComponente, Creditos> creditosPorComponente;
  private final Creditos creditosDelPrograma;
  private final Set<CodigoMateria> materiasEnElMismoSemestre;

  /**
   * Arma el contexto con los datos dados; copia los mapas y el conjunto.
   *
   * @param estados estado de las materias que lo tienen; las demás figuran pendientes
   * @param creditosPorComponente créditos aprobados de cada componente
   * @param creditosDelPrograma créditos aprobados del programa
   * @param materiasEnElMismoSemestre materias que el plan ubica en el mismo semestre o antes
   */
  public ContextoDeEvaluacionFijo(
      Map<CodigoMateria, EstadoRestriccion> estados,
      Map<TipoComponente, Creditos> creditosPorComponente,
      Creditos creditosDelPrograma,
      Set<CodigoMateria> materiasEnElMismoSemestre) {
    this.estados = Map.copyOf(ArgumentChecks.obligatorio(estados, "estados"));
    this.creditosPorComponente =
        Map.copyOf(ArgumentChecks.obligatorio(creditosPorComponente, "creditosPorComponente"));
    this.creditosDelPrograma =
        ArgumentChecks.obligatorio(creditosDelPrograma, "creditosDelPrograma");
    this.materiasEnElMismoSemestre =
        Set.copyOf(
            ArgumentChecks.obligatorio(materiasEnElMismoSemestre, "materiasEnElMismoSemestre"));
  }

  /** Contexto sin ningún dato: todo pendiente y sin créditos. */
  public static ContextoDeEvaluacionFijo vacio() {
    return nuevo().construir();
  }

  /** Inicia un constructor de contextos con todo pendiente y sin créditos. */
  public static Constructor nuevo() {
    return new Constructor();
  }

  @Override
  public EstadoRestriccion estadoDe(CodigoMateria materia) {
    ArgumentChecks.obligatorio(materia, "materia");
    return estados.getOrDefault(materia, pendiente(materia));
  }

  @Override
  public Creditos creditosAprobadosDelComponente(TipoComponente componente) {
    return creditosPorComponente.getOrDefault(
        ArgumentChecks.obligatorio(componente, "componente"), new Creditos(0));
  }

  @Override
  public Creditos creditosAprobadosDelPrograma() {
    return creditosDelPrograma;
  }

  @Override
  public boolean enElMismoSemestre(CodigoMateria materia) {
    return materiasEnElMismoSemestre.contains(ArgumentChecks.obligatorio(materia, "materia"));
  }

  private static EstadoRestriccion pendiente(CodigoMateria materia) {
    return EstadoRestriccion.pendiente(new Razon.TerminoPendiente(materia));
  }

  /** Constructor de {@link ContextoDeEvaluacionFijo} para escribir las pruebas cómodamente. */
  public static final class Constructor {

    private final Map<CodigoMateria, EstadoRestriccion> estados = new HashMap<>();
    private final Map<TipoComponente, Creditos> creditosPorComponente = new HashMap<>();
    private final Set<CodigoMateria> materiasEnElMismoSemestre = new HashSet<>();
    private Creditos creditosDelPrograma = new Creditos(0);

    private Constructor() {}

    /** La materia está aprobada: certeza cumplida. */
    public Constructor aprobada(CodigoMateria materia) {
      return estado(
          materia,
          EstadoRestriccion.cumplida(
              new Razon.TerminoAprobado(ArgumentChecks.obligatorio(materia, "materia"))));
    }

    /** La materia está en curso. */
    public Constructor enCurso(CodigoMateria materia) {
      return estado(
          materia,
          EstadoRestriccion.enCurso(
              new Razon.TerminoEnCurso(ArgumentChecks.obligatorio(materia, "materia"))));
    }

    /** La materia ya no rige y ninguna equivalencia la traduce: certeza incierta. */
    public Constructor yaNoRige(CodigoMateria materia) {
      return estado(
          materia,
          EstadoRestriccion.incierta(
              new Razon.TerminoQueYaNoRige(ArgumentChecks.obligatorio(materia, "materia"))));
    }

    /** La materia está pendiente (es lo que figura si no se dice nada). */
    public Constructor pendiente(CodigoMateria materia) {
      return estado(materia, ContextoDeEvaluacionFijo.pendiente(materia));
    }

    /** Fija el estado exacto de una materia, con sus razones. */
    public Constructor estado(CodigoMateria materia, EstadoRestriccion estado) {
      estados.put(
          ArgumentChecks.obligatorio(materia, "materia"),
          ArgumentChecks.obligatorio(estado, "estado"));
      return this;
    }

    /** Fija los créditos aprobados del programa. */
    public Constructor creditosDelPrograma(int creditos) {
      creditosDelPrograma = new Creditos(creditos);
      return this;
    }

    /** Fija los créditos aprobados de un componente. */
    public Constructor creditosDelComponente(TipoComponente componente, int creditos) {
      creditosPorComponente.put(
          ArgumentChecks.obligatorio(componente, "componente"), new Creditos(creditos));
      return this;
    }

    /** El plan ubica la materia en el mismo semestre o antes. */
    public Constructor enElMismoSemestre(CodigoMateria materia) {
      materiasEnElMismoSemestre.add(ArgumentChecks.obligatorio(materia, "materia"));
      return this;
    }

    /** Arma el contexto con lo dicho hasta aquí. */
    public ContextoDeEvaluacionFijo construir() {
      return new ContextoDeEvaluacionFijo(
          estados, creditosPorComponente, creditosDelPrograma, materiasEnElMismoSemestre);
    }
  }
}
