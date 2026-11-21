package co.edu.unal.rutaun.compartido;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Lo que un caso de uso leyó al empezar (paso 2): el valor de cada componente versionado que le
 * importa. Es inmutable.
 *
 * @param valores valor leído de cada componente, en el orden de {@link ComponenteVersionado}
 */
public record HuellaDeVersion(Map<ComponenteVersionado, String> valores) {

  /** Valida y copia los valores. */
  public HuellaDeVersion {
    Objects.requireNonNull(valores, "valores");
    if (valores.isEmpty()) {
      throw new IllegalArgumentException("La huella necesita al menos un componente.");
    }
    EnumMap<ComponenteVersionado, String> copia = new EnumMap<>(valores);
    copia.forEach((componente, valor) -> Objects.requireNonNull(valor, "valor de " + componente));
    valores = Collections.unmodifiableMap(copia);
  }
}
