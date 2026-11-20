package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.dominio.EvaluadorDeRestricciones;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaDelPrograma;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;

/**
 * Demostración ejecutable de TT-03-T9 (#161): evalúa el prerrequisito de Algoritmos (2016696)
 * contra un {@link ContextoDeEvaluacion} publicado como interfaz, con tres avances sintéticos
 * (nunca datos personales) que implementan la interfaz con {@link ContextoDeEvaluacionFijo}. No
 * necesita Spring ni base de datos; se ejecuta con {@code ./mvnw -q test-compile} y {@code java -cp
 * target/classes:target/test-classes
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
  }

  private static void mostrar(
      String avance, Prerrequisito prerrequisito, ContextoDeEvaluacion contexto) {
    EstadoRestriccion estado = EvaluadorDeRestricciones.evaluar(prerrequisito, contexto);
    System.out.println();
    System.out.println("[" + avance + "]");
    System.out.println("  certeza: " + estado.certeza());
    estado.razones().forEach(razon -> System.out.println("  - " + razon));
  }

  private static ExpresionDeMaterias expresion() {
    return new ExpresionDeMaterias.Y(
        new MateriaDelPrograma(ESTRUCTURAS_DE_DATOS),
        new ExpresionDeMaterias.Y(
            new ExpresionDeMaterias.O(
                new MateriaDelPrograma(MATEMATICAS_DISCRETAS_II),
                new MateriaDelPrograma(SISTEMAS_NUMERICOS)),
            new ExpresionDeMaterias.O(
                new MateriaDelPrograma(PROBABILIDAD_Y_ESTADISTICA),
                new ExpresionDeMaterias.O(
                    new MateriaDelPrograma(PROBABILIDAD_FUNDAMENTAL),
                    new MateriaDelPrograma(PROBABILIDAD)))));
  }
}
