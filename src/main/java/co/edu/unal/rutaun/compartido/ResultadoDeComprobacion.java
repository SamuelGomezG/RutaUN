package co.edu.unal.rutaun.compartido;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de comprobar la versión antes de guardar. El caso de uso hace un {@code switch}
 * exhaustivo: si cambió algo no guarda, informa qué cambió y ofrece recalcular (UC01 ext. 10a y
 * 10c).
 */
public sealed interface ResultadoDeComprobacion {

  /** Nada de lo que el caso leyó al empezar cambió: puede guardar. */
  record SinCambios() implements ResultadoDeComprobacion {}

  /**
   * Cambió algo de lo que el caso leyó al empezar.
   *
   * @param componentes lo que cambió, en el orden de {@link ComponenteVersionado}; no está vacío
   */
  record Cambiaron(List<ComponenteVersionado> componentes) implements ResultadoDeComprobacion {

    /** Valida y copia la lista. */
    public Cambiaron {
      Objects.requireNonNull(componentes, "componentes");
      if (componentes.isEmpty()) {
        throw new IllegalArgumentException("Debe haber al menos un componente que cambió.");
      }
      componentes = List.copyOf(componentes);
    }
  }
}
