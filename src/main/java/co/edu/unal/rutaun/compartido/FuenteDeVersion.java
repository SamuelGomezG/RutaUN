package co.edu.unal.rutaun.compartido;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Dice a qué {@link ComponenteVersionado} corresponde una lectura y cómo leer su valor actual como
 * texto. Cada caso de uso crea sus fuentes con el estudiante y el programa que atiende; así la
 * comprobación no conoce a ningún módulo.
 */
public interface FuenteDeVersion {

  /** Valor que se usa cuando el componente aún no existe, por ejemplo un estudiante sin plan. */
  String SIN_VALOR = "sin valor";

  /** Devuelve el componente al que corresponde esta fuente. */
  ComponenteVersionado componente();

  /** Lee el valor actual del componente. Nunca devuelve {@code null}: usa {@link #SIN_VALOR}. */
  String leer();

  /** Crea una fuente para un componente a partir de la forma de leer su valor actual. */
  static FuenteDeVersion de(ComponenteVersionado componente, Supplier<String> lector) {
    Objects.requireNonNull(componente, "componente");
    Objects.requireNonNull(lector, "lector");
    return new FuenteDeVersion() {
      @Override
      public ComponenteVersionado componente() {
        return componente;
      }

      @Override
      public String leer() {
        return lector.get();
      }
    };
  }
}
