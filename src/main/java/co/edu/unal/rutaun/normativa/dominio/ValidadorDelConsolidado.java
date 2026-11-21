package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Valida las reglas nuevas de una incorporación y arma, si pasan, el {@link ConsolidadoValidado}
 * del programa (diseño de clases, «ValidadorDelConsolidado»). Es el único camino para obtener uno.
 *
 * <p>UC07 ext. 6f: una regla clasificada como {@code DEL_PLAN} cuya norma es temporal se rechaza,
 * porque debe ser {@code INFORMATIVA}. La vigencia propia temporal ya la impide {@link Regla} al
 * construir la regla, así que aquí solo se mira la vigencia de la norma. También se rechaza una
 * regla que ya está en la versión anterior o repetida entre las nuevas. Las reglas de la versión
 * anterior no se vuelven a validar: ya pasaron cuando se incorporaron (supuesto del equipo, #154).
 *
 * <p>#42 (UC07-HU04) agregará aquí las demás verificaciones de coherencia.
 */
public final class ValidadorDelConsolidado {

  private ValidadorDelConsolidado() {}

  /**
   * Valida las reglas nuevas y arma las resultantes: las de la versión anterior, con su estado, más
   * las nuevas, según UC07-HU03-T4 (#130).
   *
   * @param programa programa al que pertenecen las reglas
   * @param anterior versión anterior del programa, si la hay
   * @param vigenciaDeLaNorma vigencia de la norma que se incorpora; decide si es temporal
   * @param nuevas reglas de la norma incorporada, con su estado
   * @return los motivos de rechazo o el consolidado validado
   * @throws IllegalArgumentException si falta un argumento o la versión anterior es de otro
   *     programa
   */
  public static Validacion validar(
      ProgramaId programa,
      Optional<VersionDelConsolidado> anterior,
      Vigencia vigenciaDeLaNorma,
      List<ReglaEnVersion> nuevas) {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorio(anterior, "anterior");
    ArgumentChecks.obligatorio(vigenciaDeLaNorma, "vigenciaDeLaNorma");
    List<ReglaEnVersion> reglasNuevas = ArgumentChecks.copiaSinNulos(nuevas, "nuevas");

    List<ReglaEnVersion> reglasAnteriores = List.of();
    if (anterior.isPresent()) {
      VersionDelConsolidado versionAnterior = anterior.get();
      if (!versionAnterior.programa().equals(programa)) {
        throw new IllegalArgumentException("la versión anterior es de otro programa");
      }
      reglasAnteriores = versionAnterior.reglas();
    }

    Set<ReglaId> idsDeLasAnteriores = new HashSet<>();
    for (ReglaEnVersion enVersion : reglasAnteriores) {
      idsDeLasAnteriores.add(enVersion.regla().id());
    }
    Set<ReglaId> idsDeLasNuevas = new HashSet<>();
    List<String> motivos = new ArrayList<>();
    for (ReglaEnVersion enVersion : reglasNuevas) {
      Regla regla = enVersion.regla();
      if (vigenciaDeLaNorma.esTemporal() && regla.clasificacion() == ClasificacionRegla.DEL_PLAN) {
        motivos.add(
            prefijo(regla) + "es temporal y está clasificada como DEL_PLAN; debe ser INFORMATIVA.");
      }
      boolean yaEnLaAnterior = idsDeLasAnteriores.contains(regla.id());
      boolean yaEntreLasNuevas = !idsDeLasNuevas.add(regla.id());
      if (yaEnLaAnterior) {
        motivos.add(prefijo(regla) + "ya está en la versión anterior del consolidado.");
      } else if (yaEntreLasNuevas) {
        motivos.add(prefijo(regla) + "está repetida en la norma que se importa.");
      }
    }
    if (!motivos.isEmpty()) {
      return new Validacion.Invalido(motivos);
    }

    List<ReglaEnVersion> resultantes =
        new ArrayList<>(reglasAnteriores.size() + reglasNuevas.size());
    resultantes.addAll(reglasAnteriores);
    resultantes.addAll(reglasNuevas);
    return new Validacion.Valido(new ConsolidadoValidado(programa, resultantes));
  }

  private static String prefijo(Regla regla) {
    return "La regla \"" + regla.objeto() + "\" (" + regla.fuente().articulo() + ") ";
  }
}
