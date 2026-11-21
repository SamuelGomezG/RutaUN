package co.edu.unal.rutaun.normativa.dominio;

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
 * estudios sube una sola vez por cada norma incorporada que toca reglas de estructura (S19); no lo
 * suben una versión automática ni una corrección.
 */
public final class VersionDelConsolidado {

  private final ProgramaId programa;
  private final int numero;
  private final LocalDate fecha;
  private final MotivoDeVersion motivo;
  private final List<ReglaEnVersion> reglas;
  private final int versionDelPlan;

  private VersionDelConsolidado(
      ProgramaId programa,
      int numero,
      LocalDate fecha,
      MotivoDeVersion motivo,
      List<ReglaEnVersion> reglas,
      int versionDelPlan) {
    this.programa = ArgumentChecks.obligatorio(programa, "programa");
    this.numero = ArgumentChecks.obligatorioPositivo(numero, "numero");
    this.fecha = ArgumentChecks.obligatorio(fecha, "fecha");
    this.motivo = ArgumentChecks.obligatorio(motivo, "motivo");
    this.reglas = ArgumentChecks.copiaSinNulos(reglas, "reglas");
    exigirReglasSinRepetir(this.reglas);
    this.versionDelPlan = ArgumentChecks.obligatorioPositivo(versionDelPlan, "versionDelPlan");
    if (versionDelPlan > numero) {
      throw new IllegalArgumentException(
          "la versión del plan no puede superar el número de la versión");
    }
  }

  /** Rechaza una versión que traiga dos veces la misma regla, aunque con estados distintos. */
  private static void exigirReglasSinRepetir(List<ReglaEnVersion> reglas) {
    Set<ReglaId> ids = new HashSet<>();
    for (ReglaEnVersion enVersion : reglas) {
      ReglaId id = enVersion.regla().id();
      if (!ids.add(id)) {
        throw new IllegalArgumentException("la versión trae dos veces la regla " + id.valor());
      }
    }
  }

  /**
   * Registra la primera versión del consolidado de un programa: la 1, con la 1 del plan. Rechaza
   * reglas repetidas.
   */
  public static VersionDelConsolidado primera(
      ConsolidadoValidado resultante, MotivoDeVersion motivo, LocalDate fecha) {
    ArgumentChecks.obligatorio(resultante, "resultante");
    return new VersionDelConsolidado(
        resultante.programa(), 1, fecha, motivo, resultante.reglas(), 1);
  }

  /**
   * Reconstruye una versión ya guardada. No calcula nada: valida y conserva lo que recibe, y
   * rechaza reglas repetidas.
   */
  public static VersionDelConsolidado reconstituir(
      ProgramaId programa,
      int numero,
      LocalDate fecha,
      MotivoDeVersion motivo,
      List<ReglaEnVersion> reglas,
      int versionDelPlan) {
    return new VersionDelConsolidado(programa, numero, fecha, motivo, reglas, versionDelPlan);
  }

  /**
   * Crea la versión que sigue a esta, con las reglas ya validadas. La versión actual no cambia. El
   * número de la versión del plan sube una sola vez por cada norma incorporada que toca reglas de
   * estructura, aunque esas reglas queden programadas (S19). No lo suben una corrección ni una
   * versión automática, que solo cambia el estado de las reglas (glosario, «Versión del
   * consolidado»).
   *
   * @throws IllegalArgumentException si el motivo o la fecha son nulos, si las reglas validadas son
   *     de otro programa, si la fecha es anterior a la de esta versión o si la versión trae dos
   *     veces una regla
   */
  public VersionDelConsolidado siguiente(
      ConsolidadoValidado resultante, MotivoDeVersion motivo, LocalDate fecha) {
    ArgumentChecks.obligatorio(resultante, "resultante");
    ArgumentChecks.obligatorio(motivo, "motivo");
    ArgumentChecks.obligatorio(fecha, "fecha");
    if (!resultante.programa().equals(programa)) {
      throw new IllegalArgumentException("las reglas validadas son de otro programa");
    }
    if (fecha.isBefore(this.fecha)) {
      throw new IllegalArgumentException(
          "la fecha de la versión no puede ser anterior a la de la versión previa");
    }
    boolean sube = incorporaUnaNorma(motivo) && tocaLaEstructura(reglas, resultante.reglas());
    int plan = sube ? versionDelPlan + 1 : versionDelPlan;
    return new VersionDelConsolidado(
        programa, numero + 1, fecha, motivo, resultante.reglas(), plan);
  }

  /**
   * La versión vigente es la de mayor número; vacío si no hay ninguna. Las versiones deben ser del
   * mismo programa.
   *
   * @throws IllegalArgumentException si las versiones son de programas distintos
   */
  public static Optional<VersionDelConsolidado> vigenteEntre(
      Collection<VersionDelConsolidado> versiones) {
    ArgumentChecks.obligatorio(versiones, "versiones");
    Set<ProgramaId> programas = new HashSet<>();
    for (VersionDelConsolidado version : versiones) {
      programas.add(version.programa());
    }
    if (programas.size() > 1) {
      throw new IllegalArgumentException("las versiones son de programas distintos");
    }
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

  /** Reglas de la versión, cada una con su estado en ella; la lista no se puede modificar. */
  public List<ReglaEnVersion> reglas() {
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

  /** Dice si el motivo incorpora una norma, que es cuando la versión del plan puede subir (S19). */
  private static boolean incorporaUnaNorma(MotivoDeVersion motivo) {
    return switch (motivo) {
      case MotivoDeVersion.PorIncorporacion ignored -> true;
      case MotivoDeVersion.PorCorreccion ignored -> false;
      case MotivoDeVersion.AutomaticoPorPeriodo ignored -> false;
      case MotivoDeVersion.AutomaticoPorFecha ignored -> false;
    };
  }

  private static boolean tocaLaEstructura(
      List<ReglaEnVersion> antes, List<ReglaEnVersion> despues) {
    return cambiaLaEstructura(antes, despues) || algunaNuevaAfectaLaEstructura(antes, despues);
  }

  private static boolean cambiaLaEstructura(
      List<ReglaEnVersion> antes, List<ReglaEnVersion> despues) {
    return !idsDeEstructura(antes).equals(idsDeEstructura(despues));
  }

  private static boolean algunaNuevaAfectaLaEstructura(
      List<ReglaEnVersion> antes, List<ReglaEnVersion> despues) {
    Set<ReglaId> idsDeAntes = idsDeReglas(antes);
    Set<ReglaId> idsDeEstructura = idsDeEstructura(antes);
    Set<Fuente> fuentesDeEstructura = fuentesDeEstructura(antes);
    for (ReglaEnVersion enVersion : despues) {
      Regla regla = enVersion.regla();
      if (idsDeAntes.contains(regla.id())) {
        continue;
      }
      Optional<Afectacion> afectacion = regla.afectacion();
      if (afectacion.isEmpty()) {
        continue;
      }
      boolean afectaUnaRegla =
          afectacion.get().reglaAfectada().map(idsDeEstructura::contains).orElse(false);
      boolean afectaUnArticulo =
          afectacion.get().articuloAfectado().map(fuentesDeEstructura::contains).orElse(false);
      if (afectaUnaRegla || afectaUnArticulo) {
        return true;
      }
    }
    return false;
  }

  private static Set<ReglaId> idsDeReglas(List<ReglaEnVersion> reglas) {
    Set<ReglaId> ids = new HashSet<>();
    for (ReglaEnVersion enVersion : reglas) {
      ids.add(enVersion.regla().id());
    }
    return ids;
  }

  private static Set<Fuente> fuentesDeEstructura(List<ReglaEnVersion> reglas) {
    Set<Fuente> fuentes = new HashSet<>();
    for (ReglaEnVersion enVersion : reglas) {
      if (esDeEstructura(enVersion.regla())) {
        fuentes.add(enVersion.regla().fuente());
      }
    }
    return fuentes;
  }

  private static Set<ReglaId> idsDeEstructura(List<ReglaEnVersion> reglas) {
    Set<ReglaId> ids = new HashSet<>();
    for (ReglaEnVersion enVersion : reglas) {
      if (esDeEstructura(enVersion.regla())) {
        ids.add(enVersion.regla().id());
      }
    }
    return ids;
  }

  /**
   * Una regla es de estructura si define la malla del plan de estudios: sus materias, agrupaciones
   * y componentes, o los créditos que exige el programa (S19). El {@code switch} sin {@code
   * default} obliga a decidirlo para cada categoría nueva.
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
}
