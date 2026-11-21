package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.Razon;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.Correquisito;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.AlgunaDe;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.Restriccion;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import java.util.Optional;

/**
 * Demostración ejecutable de TT-03-T9 (#161), TT-03-T7 (#163), TT-03-T10 (#164) y TT-03-T4 (#165):
 * evalúa el prerrequisito de Algoritmos (2016696) contra un {@link ContextoDeEvaluacion} publicado
 * como interfaz, con tres avances sintéticos (nunca datos personales) que implementan la interfaz
 * con {@link ContextoDeEvaluacionFijo}, e imprime además que «y» y «o» son iguales sin importar el
 * orden de sus términos, los términos aplanados de esa expresión y la evaluación de un
 * correquisito, una habilitación del programa y de un componente, y materias externas. Traduce cada
 * {@link Razon} a un texto y comprueba que ninguna nombra una materia externa (S17). No necesita
 * Spring ni base de datos; se ejecuta con {@code ./mvnw -q test-compile} y {@code java -cp
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
  private static final CodigoMateria SINTETICA = new CodigoMateria("9000001");
  private static final CodigoMateria MATERIA_DE_LA_RESTRICCION = new CodigoMateria("9000002");

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

    System.out.println();
    System.out.println("[Las demás restricciones]");
    mostrarRestriccion(
        "Correquisito sin ubicar",
        new Correquisito(MATERIA_DE_LA_RESTRICCION, new TerminoMateria(SINTETICA)),
        ContextoDeEvaluacionFijo.nuevo().pendiente(SINTETICA).construir());
    mostrarRestriccion(
        "Correquisito ubicado",
        new Correquisito(MATERIA_DE_LA_RESTRICCION, new TerminoMateria(SINTETICA)),
        ContextoDeEvaluacionFijo.nuevo()
            .pendiente(SINTETICA)
            .enElMismoSemestre(SINTETICA)
            .construir());
    mostrarRestriccion(
        "Habilitación con 39",
        new ReglaDeHabilitacion(
            MATERIA_DE_LA_RESTRICCION, MedidaDeCreditos.delPrograma(), new Creditos(40)),
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(39).construir());
    mostrarRestriccion(
        "Habilitación con 40",
        new ReglaDeHabilitacion(
            MATERIA_DE_LA_RESTRICCION, MedidaDeCreditos.delPrograma(), new Creditos(40)),
        ContextoDeEvaluacionFijo.nuevo().creditosDelPrograma(40).construir());
    mostrarRestriccion(
        "Habilitación de componente",
        new ReglaDeHabilitacion(
            MATERIA_DE_LA_RESTRICCION,
            MedidaDeCreditos.deComponente(TipoComponente.DISCIPLINAR),
            new Creditos(20)),
        ContextoDeEvaluacionFijo.nuevo()
            .creditosDelComponente(TipoComponente.DISCIPLINAR, 20)
            .construir());
    mostrarRestriccion(
        "Externa de nivelación",
        new Prerrequisito(
            MATERIA_DE_LA_RESTRICCION,
            new MateriaExterna(
                "Externa de nivelación", Optional.empty(), TipoMateriaExterna.NIVELACION)),
        ContextoDeEvaluacionFijo.vacio());
    mostrarRestriccion(
        "Externa de otro programa",
        new Prerrequisito(
            MATERIA_DE_LA_RESTRICCION,
            new MateriaExterna(
                "Externa de otro programa", Optional.empty(), TipoMateriaExterna.OTRO_PROGRAMA)),
        ContextoDeEvaluacionFijo.vacio());
    mostrarRestriccion(
        "Término o externa de otro programa",
        new Prerrequisito(
            MATERIA_DE_LA_RESTRICCION,
            ExpresionDeMaterias.algunaDe(
                new TerminoMateria(SINTETICA),
                new MateriaExterna(
                    "Externa de otro programa",
                    Optional.empty(),
                    TipoMateriaExterna.OTRO_PROGRAMA))),
        ContextoDeEvaluacionFijo.nuevo().pendiente(SINTETICA).construir());
  }

  /** Traduce cada variante de {@link Razon} a un texto para leerla en la demostración. */
  private static String traducir(Razon razon) {
    return switch (razon) {
      case Razon.TerminoAprobado termino -> termino.materia().valor() + " está aprobada";
      case Razon.TerminoEnCurso termino -> termino.materia().valor() + " está en curso";
      case Razon.TerminoPendiente termino -> termino.materia().valor() + " está pendiente";
      case Razon.TerminoQueYaNoRige termino -> termino.materia().valor() + " ya no rige";
      case Razon.UbicadoPorElPlan termino ->
          "el plan ubica " + termino.materia().valor() + " en el mismo semestre o antes";
      case Razon.CreditosDeHabilitacion creditos ->
          "créditos aprobados: "
              + creditos.aprobados().valor()
              + "; umbral: "
              + creditos.umbral().valor();
      case Razon.NoCursableDesdeElPrograma noCursable -> "no se puede cursar desde este programa";
    };
  }

  private static void mostrar(
      String avance, Prerrequisito prerrequisito, ContextoDeEvaluacion contexto) {
    EstadoRestriccion estado = prerrequisito.evaluar(contexto);
    System.out.println();
    System.out.println("[" + avance + "]");
    System.out.println("  certeza: " + estado.certeza());
    estado.razones().forEach(razon -> System.out.println("  - " + traducir(razon)));
  }

  private static void mostrarRestriccion(
      String etiqueta, Restriccion restriccion, ContextoDeEvaluacion contexto) {
    EstadoRestriccion estado = restriccion.evaluar(contexto);
    System.out.println(
        "  "
            + etiqueta
            + ": "
            + estado.certeza()
            + " ["
            + estado.razones().stream()
                .map(DemostracionDeLasRestricciones::traducir)
                .collect(java.util.stream.Collectors.joining(", "))
            + "]");
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
