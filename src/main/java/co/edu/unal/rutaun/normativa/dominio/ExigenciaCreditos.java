package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Créditos que el plan de estudios exige a un componente o a una agrupación y, si la norma lo dice,
 * cuántos son de obligatorias y cuántos de optativas, y los créditos que la malla estándar sugiere
 * en cada semestre. Es lo común de los nodos de la malla: vive aquí y no en una clase base.
 *
 * @param exigidos créditos exigidos, incluidos los de todo lo que el nodo contiene
 * @param desglose créditos de obligatorias y de optativas; vacío si la norma no los separa
 * @param sugeridosPorSemestre créditos sugeridos por semestre de la malla estándar (semestre →
 *     créditos); vacío si la norma no los da. Son datos de la norma: no se validan contra los
 *     exigidos
 */
public record ExigenciaCreditos(
    Creditos exigidos,
    Optional<Desglose> desglose,
    SortedMap<Integer, Creditos> sugeridosPorSemestre) {

  /**
   * Créditos exigidos en obligatorias y en optativas.
   *
   * @param obligatorios créditos de materias obligatorias
   * @param optativos créditos de materias optativas
   */
  public record Desglose(Creditos obligatorios, Creditos optativos) {

    /** Exige ambos valores. */
    public Desglose {
      ArgumentChecks.obligatorio(obligatorios, "obligatorios");
      ArgumentChecks.obligatorio(optativos, "optativos");
    }
  }

  /** Exige los créditos, un desglose que sume lo exigido y semestres sugeridos positivos. */
  public ExigenciaCreditos {
    ArgumentChecks.obligatorio(exigidos, "exigidos");
    ArgumentChecks.obligatorio(desglose, "desglose");
    if (desglose.isPresent()
        && !desglose.get().obligatorios().suma(desglose.get().optativos()).equals(exigidos)) {
      throw new IllegalArgumentException("el desglose debe sumar los créditos exigidos");
    }
    ArgumentChecks.obligatorio(sugeridosPorSemestre, "sugeridosPorSemestre");
    SortedMap<Integer, Creditos> copia = new TreeMap<>();
    for (Map.Entry<Integer, Creditos> sugerido : sugeridosPorSemestre.entrySet()) {
      Integer semestre = ArgumentChecks.obligatorio(sugerido.getKey(), "semestre");
      ArgumentChecks.obligatorioPositivo(semestre, "semestre");
      copia.put(semestre, ArgumentChecks.obligatorio(sugerido.getValue(), "créditos sugeridos"));
    }
    sugeridosPorSemestre = Collections.unmodifiableSortedMap(copia);
  }

  /** Exigencia sin desglose ni créditos sugeridos por semestre. */
  public static ExigenciaCreditos de(Creditos exigidos) {
    return new ExigenciaCreditos(exigidos, Optional.empty(), Collections.emptySortedMap());
  }

  /** Exigencia con su desglose en obligatorias y optativas; sin créditos sugeridos. */
  public static ExigenciaCreditos conDesglose(Creditos obligatorios, Creditos optativos) {
    Desglose desglose = new Desglose(obligatorios, optativos);
    return new ExigenciaCreditos(
        desglose.obligatorios().suma(desglose.optativos()),
        Optional.of(desglose),
        Collections.emptySortedMap());
  }

  /** Devuelve una exigencia igual con estos créditos sugeridos por semestre. */
  public ExigenciaCreditos conSugeridos(SortedMap<Integer, Creditos> sugeridos) {
    return new ExigenciaCreditos(exigidos, desglose, sugeridos);
  }
}
