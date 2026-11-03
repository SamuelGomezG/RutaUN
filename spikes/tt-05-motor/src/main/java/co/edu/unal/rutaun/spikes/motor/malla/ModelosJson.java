package co.edu.unal.rutaun.spikes.motor.malla;

import java.util.List;
import java.util.Map;

/** Records de lectura del JSON propio del spike (no es el contrato de normativa). */
public final class ModelosJson {

  private ModelosJson() {}

  /** Raíz de la malla. */
  public record MallaJson(
      FuenteJson fuente,
      int creditosDelPrograma,
      int minimoPorPeriodo,
      Integer maximoPorPeriodo,
      List<ComponenteJson> componentes,
      LibreEleccionJson libreEleccion,
      List<PrerrequisitoJson> prerrequisitos,
      List<CorrequisitoJson> correquisitos,
      List<HabilitacionJson> habilitaciones,
      List<ExternaJson> externas) {}

  /** Fuentes citadas por la transcripción. */
  public record FuenteJson(
      String planDeEstudios, String cuadro, String mallaEstandar, String transcrito) {}

  /** Componente del plan de estudios. */
  public record ComponenteJson(
      String tipo,
      int creditos,
      int obligatorios,
      int optativos,
      List<MateriaJson> materias,
      List<AgrupacionJson> agrupaciones) {}

  /** Agrupación o subagrupación. */
  public record AgrupacionJson(
      String id,
      String nombre,
      int creditos,
      List<MateriaJson> materias,
      List<AgrupacionJson> subagrupaciones) {}

  /** Materia del cuadro. */
  public record MateriaJson(
      String codigo, String nombre, int creditos, boolean obligatoria, Integer semestre) {}

  /** Créditos de libre elección. */
  public record LibreEleccionJson(
      int creditos, int creditosPorMateria, Map<String, Integer> sugeridosPorSemestre) {}

  /** Prerrequisito de una materia. */
  public record PrerrequisitoJson(String materia, ExpresionJson expresion) {}

  /** Correquisito de una materia (solo en variantes sintéticas). */
  public record CorrequisitoJson(String materia, ExpresionJson expresion) {}

  /** Regla de habilitación. */
  public record HabilitacionJson(String materia, String medida, String componente, int creditos) {}

  /** Materia externa al programa. */
  public record ExternaJson(String nombre, String tipo) {}

  /**
   * Expresión de una restricción. Solo uno de los campos está presente: {@code codigo}, {@code
   * todas}, {@code alguna} o {@code externa}.
   */
  public record ExpresionJson(
      String codigo, List<ExpresionJson> todas, List<ExpresionJson> alguna, String externa) {

    public static ExpresionJson deCodigo(String codigo) {
      return new ExpresionJson(codigo, null, null, null);
    }

    public static ExpresionJson deTodas(List<ExpresionJson> todas) {
      return new ExpresionJson(null, todas, null, null);
    }

    public static ExpresionJson deAlguna(List<ExpresionJson> alguna) {
      return new ExpresionJson(null, null, alguna, null);
    }

    public static ExpresionJson deExterna(String externa) {
      return new ExpresionJson(null, null, null, externa);
    }
  }

  /** Raíz de los escenarios de medición. */
  public record EscenariosJson(
      List<EscenarioJson> escenarios, List<TopeJson> topes, List<VarianteJson> variantes) {}

  /** Avance sintético de un escenario. */
  public record EscenarioJson(
      String id,
      String nombre,
      List<String> aprobadas,
      List<String> enCurso,
      int libreAprobados,
      int libreEnCurso) {}

  /** Tope de carga. */
  public record TopeJson(String id, Integer creditos, Integer materias) {}

  /** Variante de una medición. */
  public record VarianteJson(
      String id,
      String nombre,
      Boolean sintetica,
      String escenario,
      String tope,
      List<String> fijadas,
      List<PrerrequisitoJson> prerrequisitos,
      List<CorrequisitoJson> correquisitos) {}
}
