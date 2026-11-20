package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Componente;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.ExigenciaCreditos;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.MateriaEnMalla;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Demostración ejecutable de TT-02-T7 (#150): imprime una malla válida con su forma y sus valores,
 * y cuatro mallas con forma inválida que se rechazan con su motivo. Cubre UC07 ext. 6d y el
 * vocabulario del glosario «Componente», «Agrupación» y «Malla estándar»; la marca de cursable es
 * el valor derivado de S17. No necesita Spring ni base de datos; se ejecuta con {@code ./mvnw -q
 * test-compile && java -cp target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDeLaMalla}.
 */
public final class DemostracionDeLaMalla {

  private DemostracionDeLaMalla() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    Malla malla = MallaDePrueba.pequena();
    System.out.println("[Malla válida: forma y valores]");
    for (NodoDeMalla raiz : malla.componentes()) {
      imprimir(raiz, 0);
    }

    final Componente fundamentacion = (Componente) MallaDePrueba.nodo(malla, "Fundamentación");
    final Componente disciplinar = (Componente) MallaDePrueba.nodo(malla, "Disciplinar");
    final ComponenteDeLibreEleccion libreEleccion =
        (ComponenteDeLibreEleccion) MallaDePrueba.nodo(malla, "Libre elección");

    System.out.println();
    System.out.println("[Mallas inválidas: UC07 ext. 6d]");
    rechazar(
        "dos componentes de fundamentación",
        () -> Malla.de(fundamentacion, otraFundamentacion(), libreEleccion));
    rechazar(
        "sin componente de libre elección", () -> new Malla(List.of(fundamentacion, disciplinar)));

    AgrupacionId repetido = AgrupacionId.nuevo();
    rechazar(
        "agrupación repetida",
        () ->
            Malla.de(
                conAgrupacion("Fundamentación", TipoComponente.FUNDAMENTACION, repetido, "A"),
                conAgrupacion("Disciplinar", TipoComponente.DISCIPLINAR, repetido, "B"),
                libreEleccion));

    Agrupacion compartida = agrupacion(AgrupacionId.nuevo(), "Compartida");
    rechazar(
        "agrupación en dos padres",
        () ->
            Malla.de(
                conAgrupacion("Fundamentación", TipoComponente.FUNDAMENTACION, compartida),
                conAgrupacion("Disciplinar", TipoComponente.DISCIPLINAR, compartida),
                libreEleccion));

    System.out.println();
    System.out.println("[Créditos sugeridos: el semestre debe ser positivo]");
    rechazar(
        "semestre sugerido 0",
        () ->
            ExigenciaCreditos.de(new Creditos(6))
                .conSugeridos(new TreeMap<>(Map.of(0, new Creditos(2)))));
  }

  private static void imprimir(NodoDeMalla nodo, int nivel) {
    String sangria = "  ".repeat(nivel);
    System.out.println(sangria + nodo.nombre() + " · " + describir(nodo.exigencia()));
    for (MateriaEnMalla materia : nodo.materias()) {
      System.out.println(
          "  ".repeat(nivel + 1)
              + "· "
              + materia.nombre()
              + " ("
              + materia.creditos().valor()
              + " cr, semestre "
              + materia.semestreSugerido().map(String::valueOf).orElse("—")
              + ", cursable desde el programa: "
              + (materia.cursableDesdeElPrograma() ? "sí" : "no")
              + ")");
    }
    for (NodoDeMalla hijo : nodo.hijos()) {
      imprimir(hijo, nivel + 1);
    }
  }

  private static String describir(ExigenciaCreditos exigencia) {
    StringBuilder texto =
        new StringBuilder().append(exigencia.exigidos().valor()).append(" créditos exigidos");
    exigencia
        .desglose()
        .ifPresent(
            desglose ->
                texto
                    .append(" (")
                    .append(desglose.obligatorios().valor())
                    .append(" obligatorios + ")
                    .append(desglose.optativos().valor())
                    .append(" optativos)"));
    if (!exigencia.sugeridosPorSemestre().isEmpty()) {
      texto
          .append(" · sugeridos: ")
          .append(
              exigencia.sugeridosPorSemestre().entrySet().stream()
                  .map(sugerido -> "s" + sugerido.getKey() + "=" + sugerido.getValue().valor())
                  .collect(Collectors.joining(", ")));
    }
    return texto.toString();
  }

  private static void rechazar(String caso, Runnable intento) {
    try {
      intento.run();
      System.out.println("  " + caso + ": no rechazada (la demostración está mal)");
    } catch (IllegalArgumentException e) {
      System.out.println("  " + caso + " → rechazada: " + e.getMessage());
    }
  }

  private static Componente otraFundamentacion() {
    return new Componente(
        "Otra fundamentación",
        TipoComponente.FUNDAMENTACION,
        ExigenciaCreditos.de(new Creditos(0)),
        List.of(),
        List.of());
  }

  private static Componente conAgrupacion(
      String nombre, TipoComponente tipo, AgrupacionId id, String nombreDeLaAgrupacion) {
    return conAgrupacion(nombre, tipo, agrupacion(id, nombreDeLaAgrupacion));
  }

  private static Componente conAgrupacion(
      String nombre, TipoComponente tipo, Agrupacion agrupacion) {
    return new Componente(
        nombre, tipo, ExigenciaCreditos.de(new Creditos(3)), List.of(), List.of(agrupacion));
  }

  private static Agrupacion agrupacion(AgrupacionId id, String nombre) {
    return new Agrupacion(id, nombre, ExigenciaCreditos.de(new Creditos(3)), List.of(), List.of());
  }
}
