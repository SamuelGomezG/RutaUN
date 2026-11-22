package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.dominio.EvaluadorDeRestricciones;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.AlgunaDe;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;

/**
 * Demostración ejecutable de TT-03-T9 (#161) y TT-03-T7 (#163): evalúa el prerrequisito de
 * Algoritmos (2016696) contra un {@link ContextoDeEvaluacion} publicado como interfaz, con tres
 * avances sintéticos (nunca datos personales) que implementan la interfaz con {@link
 * ContextoDeEvaluacionFijo}, e imprime además que «y» y «o» son iguales sin importar el orden de
 * sus términos y los términos aplanados de esa expresión. No necesita Spring ni base de datos; se
 * ejecuta con {@code ./mvnw -q test-compile} y {@code java -cp target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDeLasRestricciones}. En Windows, el
 * classpath va entre comillas: {@code java -cp "target/classes;target/test-classes" …}.
 */
public final class DemostracionDeLasRestricciones {

  private static final CodigoMateria ESTRUCTURAS_DE_DATOS = new CodigoMateria("2016699");
  private static final CodigoMateria MATEMATICAS_DISCRETAS_II = new CodigoMateria("2025964");
  private static final CodigoMateria SISTEMAS_NUMERICOS = new CodigoMateria("2015181");
  private static final CodigoMateria PROBABILIDAD_Y_ESTADISTICA = new CodigoMateria("1000013");
  private static final CodigoMateria PROBABILIDAD_FUNDAMENTAL = new CodigoMateria("2027877");
  private static final CodigoMateria PROBABILIDAD = new CodigoMateria("2015178");

  private DemostracionDeLasRestricciones() {}

  /** Imprime la evaluación de cada avance sintético para quien revisa el PR. */
  public static void main(String[] args) {
    System.out.println("Prerrequisito de Algoritmos (2016696), art. 2 del Acuerdo 11 de 2023:");
    System.out.println("  Estructuras de Datos y (Matemáticas Discretas II o Sistemas numéricos)");
    System.out.println(
        "  y (Probabilidad y Estadística Fundamental o Probabilidad Fundamental o Probabilidad)");

    Prerrequisito algoritmos = new Prerrequisito(new CodigoMateria("2016696"), expresion());
    mostrar(
        "Avance A: todo aprobado",
        algoritmos,
        ContextoDeEvaluacionFijo.nuevo()
            .aprobada(ESTRUCTURAS_DE_DATOS)
            .aprobada(MATEMATICAS_DISCRETAS_II)
            .aprobada(PROBABILIDAD_Y_ESTADISTICA)
            .construir());
    mostrar(
        "Avance B: Matemáticas Discretas II en curso, y falta probabilidad",
        algoritmos,
        ContextoDeEvaluacionFijo.nuevo()
            .aprobada(ESTRUCTURAS_DE_DATOS)
            .enCurso(MATEMATICAS_DISCRETAS_II)
            .construir());
    mostrar(
        "Avance C: Estructuras de Datos ya no rige y no tiene equivalencia",
        algoritmos,
        ContextoDeEvaluacionFijo.nuevo()
            .yaNoRige(ESTRUCTURAS_DE_DATOS)
            .aprobada(SISTEMAS_NUMERICOS)
            .aprobada(PROBABILIDAD)
            .construir());

    System.out.println();
    System.out.println("[Expresiones «y» y «o» sin orden]");
    ExpresionDeMaterias a = new TerminoMateria(new CodigoMateria("A"));
    ExpresionDeMaterias b = new TerminoMateria(new CodigoMateria("B"));
    ExpresionDeMaterias c = new TerminoMateria(new CodigoMateria("C"));
    System.out.println(
        "  «A y B» = «B y A»: "
            + ExpresionDeMaterias.todasLas(a, b).equals(ExpresionDeMaterias.todasLas(b, a)));
    System.out.println(
        "  «A y (B y C)» = «A y B y C»: "
            + ExpresionDeMaterias.todasLas(a, ExpresionDeMaterias.todasLas(b, c))
                .equals(ExpresionDeMaterias.todasLas(a, b, c)));
    System.out.println("  Términos de la expresión de Algoritmos, aplanada:");
    for (ExpresionDeMaterias termino : ((TodasLas) expresion()).terminos()) {
      System.out.println("    - " + describir(termino));
    }
  }

  private static void mostrar(
      String avance, Prerrequisito prerrequisito, ContextoDeEvaluacion contexto) {
    EstadoRestriccion estado = EvaluadorDeRestricciones.evaluar(prerrequisito, contexto);
    System.out.println();
    System.out.println("[" + avance + "]");
    System.out.println("  certeza: " + estado.certeza());
    estado.razones().forEach(razon -> System.out.println("  - " + razon));
  }

  private static String describir(ExpresionDeMaterias expresion) {
    return switch (expresion) {
      case TerminoMateria materia -> materia.codigo().valor();
      case ExpresionDeMaterias.MateriaExterna externa -> externa.nombre();
      case TodasLas todas -> "una «y» de " + todas.terminos().size() + " términos";
      case AlgunaDe alguna -> "una «o» de " + alguna.terminos().size() + " términos";
    };
  }

  private static ExpresionDeMaterias expresion() {
    return ExpresionDeMaterias.todasLas(
        new TerminoMateria(ESTRUCTURAS_DE_DATOS),
        ExpresionDeMaterias.algunaDe(
            new TerminoMateria(MATEMATICAS_DISCRETAS_II), new TerminoMateria(SISTEMAS_NUMERICOS)),
        ExpresionDeMaterias.algunaDe(
            new TerminoMateria(PROBABILIDAD_Y_ESTADISTICA),
            new TerminoMateria(PROBABILIDAD_FUNDAMENTAL),
            new TerminoMateria(PROBABILIDAD)));
  }
}
