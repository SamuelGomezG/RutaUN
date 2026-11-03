package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Una versión del consolidado de un programa: su número, su fecha, su {@link MotivoDeVersion}, las
 * reglas con su estado y el número de la versión del plan de estudios (diseño de clases,
 * «VersionDelConsolidado»; glosario, «Programa»).
 *
 * <p>Es una raíz de solo agregar: nunca se modifica. La primera se registra con {@link #primera} y
 * cada una de las siguientes la crea la anterior con {@link #siguiente}. La vigente es la de mayor
 * número. Crear una versión no carga ni toca las anteriores. El número de la versión del plan de
 * estudios solo sube cuando cambian reglas de estructura.
 */
public final class VersionDelConsolidado {

  private final ProgramaId programa;
  private final int numero;
  private final LocalDate fecha;
  private final MotivoDeVersion motivo;
  private final List<Regla> reglas;
  private final int versionDelPlan;

  private VersionDelConsolidado(
      ProgramaId programa,
      int numero,
      LocalDate fecha,
      MotivoDeVersion motivo,
      List<Regla> reglas,
      int versionDelPlan) {
    this.programa = ArgumentChecks.obligatorio(programa, "programa");
    this.numero = ArgumentChecks.obligatorioPositivo(numero, "numero");
    this.fecha = ArgumentChecks.obligatorio(fecha, "fecha");
    this.motivo = ArgumentChecks.obligatorio(motivo, "motivo");
    this.reglas = ArgumentChecks.copiaSinNulos(reglas, "reglas");
    this.versionDelPlan = ArgumentChecks.obligatorioPositivo(versionDelPlan, "versionDelPlan");
    if (versionDelPlan > numero) {
      throw new IllegalArgumentException(
          "la versión del plan no puede superar el número de la versión");
    }
  }

  /** Registra la primera versión del consolidado de un programa: la 1, con la 1 del plan. */
  public static VersionDelConsolidado primera(
      ConsolidadoValidado resultante, MotivoDeVersion motivo, LocalDate fecha) {
    ArgumentChecks.obligatorio(resultante, "resultante");
    return new VersionDelConsolidado(
        resultante.programa(), 1, fecha, motivo, resultante.reglas(), 1);
  }

  /** Reconstruye una versión ya guardada. No calcula nada: valida y conserva lo que recibe. */
  public static VersionDelConsolidado reconstituir(
      ProgramaId programa,
      int numero,
      LocalDate fecha,
      MotivoDeVersion motivo,
      List<Regla> reglas,
      int versionDelPlan) {
    return new VersionDelConsolidado(programa, numero, fecha, motivo, reglas, versionDelPlan);
  }

  /**
   * Crea la versión que sigue a esta, con las reglas ya validadas. La versión actual no cambia. El
   * número de la versión del plan sube solo si las reglas de estructura de {@code resultante} son
   * distintas de las de esta versión: otras o con otro estado.
   *
   * @throws IllegalArgumentException si las reglas validadas son de otro programa
   */
  public VersionDelConsolidado siguiente(
      ConsolidadoValidado resultante, MotivoDeVersion motivo, LocalDate fecha) {
    ArgumentChecks.obligatorio(resultante, "resultante");
    if (!resultante.programa().equals(programa)) {
      throw new IllegalArgumentException("las reglas validadas son de otro programa");
    }
    boolean cambia = cambiaLaEstructura(reglas, resultante.reglas());
    int plan = cambia ? versionDelPlan + 1 : versionDelPlan;
    return new VersionDelConsolidado(
        programa, numero + 1, fecha, motivo, resultante.reglas(), plan);
  }

  /** La versión vigente es la de mayor número; vacío si no hay ninguna. */
  public static Optional<VersionDelConsolidado> vigenteEntre(
      Collection<VersionDelConsolidado> versiones) {
    ArgumentChecks.obligatorio(versiones, "versiones");
    return versiones.stream().max(Comparator.comparingInt(VersionDelConsolidado::numero));
  }

  /** Programa al que pertenece la versión. */
  public ProgramaId programa() {
    return programa;
  }

  /** Número de la versión dentro del consolidado del programa; empieza en 1. */
  public int numero() {
    return numero;
  }

  /** Fecha en que se creó la versión. */
  public LocalDate fecha() {
    return fecha;
  }

  /** Por qué se creó la versión. */
  public MotivoDeVersion motivo() {
    return motivo;
  }

  /** Reglas de la versión, cada una con su estado; la lista no se puede modificar. */
  public List<Regla> reglas() {
    return reglas;
  }

  /** Número de la versión del plan de estudios a la que apunta esta versión. */
  public int versionDelPlan() {
    return versionDelPlan;
  }

  /** Dos versiones son la misma si son del mismo programa y tienen el mismo número. */
  @Override
  public boolean equals(Object otra) {
    return otra instanceof VersionDelConsolidado version
        && numero == version.numero
        && programa.equals(version.programa);
  }

  @Override
  public int hashCode() {
    return Objects.hash(programa, numero);
  }

  @Override
  public String toString() {
    return "VersionDelConsolidado[programa=" + programa.valor() + ", numero=" + numero + "]";
  }

  private static boolean cambiaLaEstructura(List<Regla> antes, List<Regla> despues) {
    return !huellaDeEstructura(antes).equals(huellaDeEstructura(despues));
  }

  private static Set<ReglaConEstado> huellaDeEstructura(List<Regla> reglas) {
    Set<ReglaConEstado> huella = new HashSet<>();
    for (Regla regla : reglas) {
      if (esDeEstructura(regla)) {
        huella.add(new ReglaConEstado(regla.id(), regla.estado()));
      }
    }
    return huella;
  }

  /**
   * Una regla es de estructura si define la malla del plan de estudios: sus materias, agrupaciones
   * y componentes, o los créditos que exige el programa. Es el supuesto del equipo mientras la
   * norma no lo precise; el {@code switch} sin {@code default} obliga a decidirlo para cada
   * categoría nueva.
   */
  private static boolean esDeEstructura(Regla regla) {
    return switch (regla.datos()) {
      case DatosDeRegla.DeMateria materia -> true;
      case DatosDeRegla.DeAgrupacion agrupacion -> true;
      case DatosDeRegla.DeComponente componente -> true;
      case DatosDeRegla.DePrograma programa -> true;
      case DatosDeRegla.DePrerrequisito prerrequisito -> false;
      case DatosDeRegla.DeCorrequisito correquisito -> false;
      case DatosDeRegla.DeHabilitacion habilitacion -> false;
      case DatosDeRegla.DeEquivalencia equivalencia -> false;
      case DatosDeRegla.DeCargaPorPeriodo carga -> false;
      case DatosDeRegla.DeRequisitoDeGrado requisito -> false;
      case DatosDeRegla.DeOtraCategoria otra -> false;
    };
  }

  private record ReglaConEstado(ReglaId id, EstadoRegla estado) {}
}
