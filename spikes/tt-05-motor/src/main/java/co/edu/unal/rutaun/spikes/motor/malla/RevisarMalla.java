package co.edu.unal.rutaun.spikes.motor.malla;

import co.edu.unal.rutaun.spikes.motor.Rutas;
import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.PlanEstandar;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Comprueba la transcripción antes de medir (corte C1). */
public final class RevisarMalla {

  private static final List<String> fallos = new ArrayList<>();

  private RevisarMalla() {}

  public static void main(String[] args) {
    Path raiz = args.length > 0 ? Path.of(args[0]) : Rutas.datos();
    ModelosJson.MallaJson malla = LectorDeMalla.leer(raiz.resolve("malla-sistemas.json"));
    ModelosJson.EscenariosJson escenarios =
        LectorDeMalla.leerEscenarios(raiz.resolve("escenarios.json"));
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    int n = modelo.cantidadMaterias();

    comprobarCreditos(malla, modelo);
    comprobarAgrupaciones(modelo);
    comprobarCifras(malla, modelo);
    comprobarCiclos(modelo);
    comprobarMallaEstandar(modelo);
    comprobarEscenarios(modelo, escenarios);
    imprimirSimetrias(modelo);

    if (fallos.isEmpty()) {
      System.out.println("C1 OK: la transcripción cuadra con el anexo A y el anexo B.");
    } else {
      System.out.println("C1 FALLÓ:");
      for (String fallo : fallos) {
        System.out.println("  - " + fallo);
      }
      System.exit(1);
    }
  }

  private static void comprobarCreditos(ModelosJson.MallaJson malla, ModeloDeCalculo modelo) {
    int sumaComponentes = 0;
    for (ModelosJson.ComponenteJson componente : malla.componentes()) {
      sumaComponentes += componente.creditos();
      if (componente.obligatorios() + componente.optativos() != componente.creditos()) {
        fallar(
            "Desglose del componente "
                + componente.tipo()
                + ": "
                + componente.obligatorios()
                + " + "
                + componente.optativos()
                + " != "
                + componente.creditos());
      }
    }
    int total = sumaComponentes + malla.libreEleccion().creditos();
    if (total != malla.creditosDelPrograma()) {
      fallar("Créditos del programa: " + total + " != " + malla.creditosDelPrograma());
    }
    int obligatorias = 0;
    int creditosObligatorios = 0;
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if (modelo.obligatoria(i)) {
        obligatorias++;
        creditosObligatorios += modelo.creditos(i);
      }
    }
    if (obligatorias != 17 || creditosObligatorios != 54) {
      fallar(
          "Obligatorias: "
              + obligatorias
              + " materias y "
              + creditosObligatorios
              + " créditos (esperado 17 y 54)");
    }
  }

  private static void comprobarAgrupaciones(ModeloDeCalculo modelo) {
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      int requeridas = 0;
      int optativas = 0;
      for (int materia : grupo.materiasDirectas()) {
        if (modelo.requeridas().contiene(materia)) {
          requeridas += modelo.creditos(materia);
        } else if (modelo.cursable(materia)) {
          optativas += modelo.creditos(materia);
        }
      }
      int exigido = requeridas + grupo.creditosHijas();
      if (exigido > grupo.creditos()) {
        fallar(
            "Agrupación "
                + grupo.id()
                + ": obligatorias + hijas = "
                + exigido
                + " > "
                + grupo.creditos());
      }
      int faltan = grupo.creditos() - exigido;
      if (optativas < faltan) {
        fallar(
            "Agrupación "
                + grupo.id()
                + ": optativas cursables "
                + optativas
                + " < faltan "
                + faltan);
      }
    }
  }

  private static void comprobarCifras(ModelosJson.MallaJson malla, ModeloDeCalculo modelo) {
    int cursablesNo = 0;
    int conPrerrequisito = 0;
    int cadena = 0;
    Set<String> codigosConPrerrequisito = new HashSet<>();
    for (ModelosJson.PrerrequisitoJson prerrequisito : malla.prerrequisitos()) {
      codigosConPrerrequisito.add(prerrequisito.materia());
    }
    ConjuntoDeMaterias vacio = new ConjuntoDeMaterias(modelo.cantidadMaterias());
    int[] memo = new int[modelo.cantidadMaterias()];
    Arrays.fill(memo, -1);
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if (!modelo.cursable(i)) {
        cursablesNo++;
      }
      if (codigosConPrerrequisito.contains(modelo.codigo(i))) {
        conPrerrequisito++;
      }
      int profundidad = modelo.profundidad(i, vacio, memo);
      if (profundidad < ModeloDeCalculo.INFINITO) {
        cadena = Math.max(cadena, profundidad);
      }
    }
    comprobar("Materias distintas", modelo.cantidadMaterias(), 90);
    comprobar("Materias con prerrequisito", conPrerrequisito, 83);
    comprobar("Reglas de habilitación", malla.habilitaciones().size(), 14);
    comprobar("Correquisitos", malla.correquisitos().size(), 0);
    comprobar("Externas", malla.externas().size(), 3);
    comprobar("No cursables", cursablesNo, 2);
    comprobar("Cadena más larga", cadena, 7);
  }

  private static void comprobar(String nombre, int real, int esperado) {
    System.out.println(nombre + ": " + real);
    if (real != esperado) {
      fallar(nombre + ": " + real + " != " + esperado);
    }
  }

  private static void comprobarCiclos(ModeloDeCalculo modelo) {
    int[] estado = new int[modelo.cantidadMaterias()];
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if (hayCiclo(modelo, i, estado)) {
        fallar("Ciclo de prerrequisitos en " + modelo.codigo(i));
        return;
      }
    }
    System.out.println("Ciclos de prerrequisitos: ninguno");
  }

  private static boolean hayCiclo(ModeloDeCalculo modelo, int materia, int[] estado) {
    if (estado[materia] == 1) {
      return true;
    }
    if (estado[materia] == 2) {
      return false;
    }
    estado[materia] = 1;
    for (long[] clausula : modelo.prerrequisitos(materia)) {
      for (int i = 0; i < modelo.cantidadMaterias(); i++) {
        if ((clausula[i >>> 6] & (1L << (i & 63))) != 0 && hayCiclo(modelo, i, estado)) {
          return true;
        }
      }
    }
    estado[materia] = 2;
    return false;
  }

  private static void comprobarMallaEstandar(ModeloDeCalculo modelo) {
    Plan plan = PlanEstandar.adaptado(modelo);
    Tope t2 = new Tope("T2", 18, 6);
    List<String> errores =
        new VerificadorDePlan(modelo)
            .verificar(
                plan,
                new ConjuntoDeMaterias(modelo.cantidadMaterias()),
                modelo.libreEleccion(),
                t2);
    if (!errores.isEmpty()) {
      fallar("La malla estándar adaptada no pasa el verificador: " + errores);
      return;
    }
    System.out.println("Malla estándar adaptada: aceptada con T2");

    Plan mutacion1 = PlanEstandar.intercambiar(modelo, plan, "2025967", "2016699");
    List<String> errores1 =
        new VerificadorDePlan(modelo)
            .verificar(
                mutacion1,
                new ConjuntoDeMaterias(modelo.cantidadMaterias()),
                modelo.libreEleccion(),
                t2);
    exigirError(errores1, "Prerrequisito", "mutación 1 (Redes/Estructuras)");

    Plan mutacion2 = PlanEstandar.intercambiar(modelo, plan, "2024045", "2015970");
    List<String> errores2 =
        new VerificadorDePlan(modelo)
            .verificar(
                mutacion2,
                new ConjuntoDeMaterias(modelo.cantidadMaterias()),
                modelo.libreEleccion(),
                t2);
    exigirError(errores2, "Habilitación", "mutación 2 (Taller/Métodos)");
    for (String error : errores2) {
      if (!error.contains("Habilitación")) {
        fallar("La mutación 2 falla por otra causa: " + error);
      }
    }

    Plan mutacion3 = PlanEstandar.quitarYAgregarBloque(modelo, plan, "2025960", 8, 3);
    List<String> errores3 =
        new VerificadorDePlan(modelo)
            .verificar(
                mutacion3,
                new ConjuntoDeMaterias(modelo.cantidadMaterias()),
                modelo.libreEleccion(),
                t2);
    exigirError(errores3, "cubrir", "mutación 3 (quitar Computación visual)");
    exigirError(errores3, "bloques", "mutación 3 (libre elección)");
  }

  private static void exigirError(List<String> errores, String fragmento, String caso) {
    if (errores.stream().noneMatch(error -> error.contains(fragmento))) {
      fallar(caso + ": no se detectó «" + fragmento + "»; errores: " + errores);
    } else {
      System.out.println(caso + ": rechazada por " + fragmento);
    }
  }

  private static void comprobarEscenarios(
      ModeloDeCalculo modelo, ModelosJson.EscenariosJson escenarios) {
    for (ModelosJson.EscenarioJson escenario : escenarios.escenarios()) {
      ConjuntoDeMaterias aprobadas = new ConjuntoDeMaterias(modelo.cantidadMaterias());
      ConjuntoDeMaterias enCurso = new ConjuntoDeMaterias(modelo.cantidadMaterias());
      for (String codigo : escenario.aprobadas()) {
        aprobadas.agregar(indice(modelo, codigo));
      }
      for (String codigo : escenario.enCurso()) {
        enCurso.agregar(indice(modelo, codigo));
      }
      for (String codigo : escenario.aprobadas()) {
        int materia = indice(modelo, codigo);
        if (!modelo.clausulasCumplidas(modelo.prerrequisitos(materia), aprobadas)) {
          fallar(
              "Escenario "
                  + escenario.id()
                  + ": "
                  + codigo
                  + " no tiene sus prerrequisitos aprobados");
        }
      }
      for (String codigo : escenario.enCurso()) {
        int materia = indice(modelo, codigo);
        if (!modelo.clausulasCumplidas(modelo.prerrequisitos(materia), aprobadas)) {
          fallar(
              "Escenario "
                  + escenario.id()
                  + ": "
                  + codigo
                  + " en curso no tiene sus prerrequisitos aprobados");
        }
      }
      System.out.println("Escenario " + escenario.id() + ": coherente");
    }
  }

  private static void imprimirSimetrias(ModeloDeCalculo modelo) {
    Map<String, List<Integer>> clases = new LinkedHashMap<>();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if (modelo.requeridas().contiene(i) || !modelo.cursable(i)) {
        continue;
      }
      if (modelo.gruposDeMateria(i).length != 1) {
        continue;
      }
      StringBuilder clave = new StringBuilder();
      clave.append(modelo.gruposDeMateria(i)[0]).append('|');
      clave.append(modelo.creditos(i)).append('|');
      for (long[] clausula : modelo.prerrequisitos(i)) {
        clave.append(Arrays.toString(clausula)).append(';');
      }
      clave.append('|');
      for (ModeloDeCalculo.Habilitacion habilitacion : modelo.habilitaciones(i)) {
        clave.append(habilitacion.medida()).append(':').append(habilitacion.umbral()).append(';');
      }
      clases.computeIfAbsent(clave.toString(), k -> new ArrayList<>()).add(i);
    }
    System.out.println("Clases de optativas intercambiables:");
    for (List<Integer> clase : clases.values()) {
      if (clase.size() > 1) {
        List<String> codigos = new ArrayList<>();
        for (int materia : clase) {
          codigos.add(modelo.codigo(materia));
        }
        System.out.println("  " + codigos);
      }
    }
    comprobarMismaClase(modelo, "1000004", "2016377");
    comprobarMismaClase(modelo, "2015734", "2026573");
    comprobarMismaClase(modelo, "2016353", "2027641");
    comprobarMismaClase(modelo, "2025966", "2027642");
    comprobarMismaClase(modelo, "2025966", "2027628");
    String[] sistemasInteligentes = {"2025995", "2016748", "2023251", "2027631", "2017290"};
    for (int i = 1; i < sistemasInteligentes.length; i++) {
      comprobarMismaClase(modelo, sistemasInteligentes[0], sistemasInteligentes[i]);
    }
    String[] taller = {
      "2024045", "2016615", "2016093", "2016091", "2026551", "2016600", "2016599", "2016741"
    };
    for (int i = 1; i < taller.length; i++) {
      comprobarMismaClase(modelo, taller[0], taller[i]);
    }
  }

  private static void comprobarMismaClase(ModeloDeCalculo modelo, String codigoA, String codigoB) {
    int a = indice(modelo, codigoA);
    int b = indice(modelo, codigoB);
    if (modelo.gruposDeMateria(a).length != 1
        || modelo.gruposDeMateria(b).length != 1
        || modelo.gruposDeMateria(a)[0] != modelo.gruposDeMateria(b)[0]
        || modelo.creditos(a) != modelo.creditos(b)
        || !Arrays.deepEquals(
            modelo.prerrequisitos(a).toArray(), modelo.prerrequisitos(b).toArray())) {
      fallar("Se esperaba la misma clase de simetría para " + codigoA + " y " + codigoB);
    }
  }

  private static int indice(ModeloDeCalculo modelo, String codigo) {
    Integer indice = modelo.indiceDe(codigo);
    if (indice == null) {
      throw new IllegalArgumentException("Código desconocido: " + codigo);
    }
    return indice;
  }

  private static void fallar(String mensaje) {
    fallos.add(mensaje);
  }
}
