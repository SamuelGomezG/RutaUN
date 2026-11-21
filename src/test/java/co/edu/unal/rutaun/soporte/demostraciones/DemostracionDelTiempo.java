package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;

/**
 * Demostración ejecutable de TT-02-T5 (#151): los periodos académicos se escriben y se leen como
 * «AAAA-1» o «AAAA-2», se validan y se suceden en orden. Imprime tres periodos consecutivos, la ida
 * y vuelta entre objeto y texto, el motivo con que se rechazan un texto inválido y un año fuera de
 * rango, el error del último periodo y la lectura con espacios alrededor. No necesita Spring ni
 * base de datos; se ejecuta con {@code ./mvnw -q test-compile} y {@code java -cp
 * target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDelTiempo}.
 */
public final class DemostracionDelTiempo {

  private DemostracionDelTiempo() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    PeriodoAcademico primero = new PeriodoAcademico(2027, ParteDelAnio.PRIMERO);
    PeriodoAcademico segundo = primero.siguiente();
    PeriodoAcademico tercero = segundo.siguiente();

    System.out.println("Periodos consecutivos:");
    System.out.println("  " + primero + " -> " + segundo + " -> " + tercero);

    System.out.println();
    System.out.println("Ida y vuelta entre objeto y texto:");
    PeriodoAcademico leido = PeriodoAcademico.desdeTexto(primero.toString());
    System.out.println("  " + primero + " -> \"" + primero + "\" -> " + leido);
    System.out.println("  ¿El mismo periodo? " + primero.equals(leido));

    System.out.println();
    System.out.println("Textos rechazados:");
    imprimirRechazo("2027-3");
    imprimirRechazo("0999-1");

    System.out.println();
    System.out.println("Último periodo:");
    PeriodoAcademico ultimo = new PeriodoAcademico(9999, ParteDelAnio.SEGUNDO);
    try {
      PeriodoAcademico siguiente = ultimo.siguiente();
      System.out.println("  " + ultimo + " tiene siguiente: " + siguiente);
    } catch (IllegalStateException e) {
      System.out.println("  " + ultimo + " no tiene siguiente: " + e.getMessage());
    }

    System.out.println();
    System.out.println("Espacios alrededor:");
    System.out.println("  \" 2026-2 \" -> " + PeriodoAcademico.desdeTexto(" 2026-2 "));
  }

  private static void imprimirRechazo(String texto) {
    try {
      PeriodoAcademico periodo = PeriodoAcademico.desdeTexto(texto);
      System.out.println("  \"" + texto + "\" se aceptó: " + periodo);
    } catch (IllegalArgumentException e) {
      System.out.println("  \"" + texto + "\" se rechazó: " + e.getMessage());
    }
  }
}
