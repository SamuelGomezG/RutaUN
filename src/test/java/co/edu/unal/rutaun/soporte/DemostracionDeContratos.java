package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import java.util.Optional;

/**
 * Demostración ejecutable de los contratos de TT-02-T0 (#82): imprime el consolidado de la fachada
 * falsa con el orden de las categorías, la fuente estructurada y el estado de cada regla, más el
 * caso de una regla sin fuente registrada (UC06 ext. 6e) y el de un consolidado sin reglas (UC06
 * ext. 4a). No necesita Spring ni base de datos; se ejecuta con {@code ./mvnw -q test-compile &&
 * java -cp target/classes:target/test-classes co.edu.unal.rutaun.soporte.DemostracionDeContratos}.
 */
public final class DemostracionDeContratos {

  private DemostracionDeContratos() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    ConsolidadoVista consolidado =
        ConsultaNormativaFalsa.conReglas()
            .consolidadoVigente(ConsultaNormativaFalsa.SISTEMAS)
            .orElseThrow();

    System.out.println(
        "Consolidado v" + consolidado.version() + " · actualizado " + consolidado.fecha());
    consolidado
        .reglasPorCategoria()
        .forEach(
            (categoria, reglas) -> {
              System.out.println();
              System.out.println("[" + categoria + "]");
              reglas.forEach(regla -> System.out.println("  " + describir(regla)));
            });

    System.out.println();
    System.out.println("[Caso UC06 ext. 6e: regla sin fuente registrada]");
    System.out.println(
        "  "
            + describir(
                new ReglaVista(
                    "Regla de prueba sin fuente.",
                    Optional.empty(),
                    EstadoRegla.EN_REVISION,
                    false)));

    ConsolidadoVista sinReglas =
        ConsultaNormativaFalsa.sinReglas()
            .consolidadoVigente(ConsultaNormativaFalsa.SISTEMAS)
            .orElseThrow();
    System.out.println();
    System.out.println("[Caso UC06 ext. 4a: consolidado sin reglas]");
    System.out.println(
        "  sin reglas: "
            + sinReglas.sinReglas()
            + " · versión para el incidente: v"
            + sinReglas.version());
  }

  private static String describir(ReglaVista regla) {
    String fuente =
        regla
            .fuente()
            .map(
                f ->
                    f.norma().tipo()
                        + " "
                        + f.norma().numero()
                        + " de "
                        + f.norma().anio()
                        + " ("
                        + f.norma().organo()
                        + "), "
                        + f.articulo()
                        + f.enlace().map(enlace -> " · " + enlace).orElse(""))
            .orElse("sin fuente registrada");
    return regla.estado()
        + (regla.temporal() ? " · temporal" : "")
        + " · "
        + regla.enunciado()
        + " · ["
        + fuente
        + "]";
  }
}
