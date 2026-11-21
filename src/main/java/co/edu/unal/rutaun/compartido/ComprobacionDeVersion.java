package co.edu.unal.rutaun.compartido;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Comprobación de versión antes de guardar (SOP-07, UC01 paso 10, UC02 paso 11, UC03 paso 7 y UC04
 * paso 10). El caso de uso la usa en dos momentos: {@link #tomar} al empezar (paso 2) y {@link
 * #comprobar} justo antes de guardar. No guarda ni recalcula: solo dice si algo cambió y qué.
 *
 * <p>No sustituye al {@code @Version} de cada agregado, que sigue siendo la última guarda frente a
 * dos guardados simultáneos.
 */
public final class ComprobacionDeVersion {

  private ComprobacionDeVersion() {}

  /** Lee las fuentes y guarda lo leído como la huella con la que el caso empezó. */
  public static HuellaDeVersion tomar(List<FuenteDeVersion> fuentes) {
    Map<ComponenteVersionado, String> valores = new EnumMap<>(ComponenteVersionado.class);
    fuentesPorComponente(fuentes)
        .forEach((componente, fuente) -> valores.put(componente, leer(fuente)));
    return new HuellaDeVersion(valores);
  }

  /**
   * Vuelve a leer las fuentes y las compara con la huella. Solo se comparan los componentes que
   * están en la huella; si falta la fuente de alguno, lanza {@link IllegalArgumentException}.
   */
  public static ResultadoDeComprobacion comprobar(
      HuellaDeVersion cargada, List<FuenteDeVersion> fuentes) {
    Objects.requireNonNull(cargada, "cargada");
    Map<ComponenteVersionado, FuenteDeVersion> porComponente = fuentesPorComponente(fuentes);
    List<ComponenteVersionado> cambiados = new ArrayList<>();
    cargada
        .valores()
        .forEach(
            (componente, valorCargado) -> {
              FuenteDeVersion fuente = porComponente.get(componente);
              if (fuente == null) {
                throw new IllegalArgumentException("Falta la fuente de " + componente + ".");
              }
              if (!valorCargado.equals(leer(fuente))) {
                cambiados.add(componente);
              }
            });
    return cambiados.isEmpty()
        ? new ResultadoDeComprobacion.SinCambios()
        : new ResultadoDeComprobacion.Cambiaron(cambiados);
  }

  private static Map<ComponenteVersionado, FuenteDeVersion> fuentesPorComponente(
      List<FuenteDeVersion> fuentes) {
    Objects.requireNonNull(fuentes, "fuentes");
    Map<ComponenteVersionado, FuenteDeVersion> porComponente =
        new EnumMap<>(ComponenteVersionado.class);
    for (FuenteDeVersion fuente : fuentes) {
      Objects.requireNonNull(fuente, "fuente");
      if (porComponente.put(fuente.componente(), fuente) != null) {
        throw new IllegalArgumentException("Hay dos fuentes para " + fuente.componente() + ".");
      }
    }
    return porComponente;
  }

  private static String leer(FuenteDeVersion fuente) {
    return Objects.requireNonNull(
        fuente.leer(),
        "La fuente de " + fuente.componente() + " devolvió null: usa FuenteDeVersion.SIN_VALOR.");
  }
}
