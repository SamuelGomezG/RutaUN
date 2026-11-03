package co.edu.unal.rutaun.spikes.motor.medicion;

import co.edu.unal.rutaun.spikes.motor.Rutas;
import co.edu.unal.rutaun.spikes.motor.busqueda.CalculoDelPlan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Cancelacion;
import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.ResultadoCalculo;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.malla.LectorDeMalla;
import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Corre los escenarios, verifica los planes y escribe el CSV de mediciones. */
public final class Medir {

  private static final int CORRIDAS = 5;
  private static final int PLAZO_MS = 30_000;

  private static int plazoMs() {
    return Integer.parseInt(System.getProperty("tt05.plazo", String.valueOf(PLAZO_MS)));
  }

  private static ModelosJson.MallaJson malla;
  private static ModelosJson.EscenariosJson escenarios;
  private static final List<Fila> filas = new ArrayList<>();

  private Medir() {}

  public static void main(String[] args) throws Exception {
    Path datos = Rutas.datos();
    malla = LectorDeMalla.leer(datos.resolve("malla-sistemas.json"));
    escenarios = LectorDeMalla.leerEscenarios(datos.resolve("escenarios.json"));

    if (args.length >= 2) {
      if ("variante".equals(args[0])) {
        correrVariante(args[1], args.length > 2 ? args[2] : null);
      } else {
        correrEscenario(args[0], args[1], CORRIDAS);
      }
    } else if (args.length == 1 && "UC04".equalsIgnoreCase(args[0])) {
      correrUc04();
    } else if (args.length == 1 && "estres".equalsIgnoreCase(args[0])) {
      correrEstres();
    } else if (args.length == 1 && "curado".equalsIgnoreCase(args[0])) {
      curado();
    } else {
      todos();
    }
    if (!filas.isEmpty()) {
      escribirCsv();
      resumir();
    }
  }

  private static void curado() {
    correrEscenario("E0", "T2", CORRIDAS);
    correrEscenario("E1", "T1", CORRIDAS);
    correrEscenario("E1", "T2", CORRIDAS);
    correrEscenario("E1", "T4", CORRIDAS);
    correrEscenario("E2", "T2", CORRIDAS);
    correrEscenario("E3", "T2", CORRIDAS);
    correrEscenario("E4", "T2", CORRIDAS);
    correrVariante("C", null);
    correrVariante("D3", null);
    correrUc04();
  }

  private static void todos() {
    String[] ids = {"E0", "E1", "E2", "E3", "E4"};
    String[] topes = {"T1", "T2", "T3", "T4", "T5"};
    for (String escenario : ids) {
      for (String tope : topes) {
        if (!correrEscenario(escenario, tope, CORRIDAS)) {
          return;
        }
      }
    }
    correrVariante("F", null);
    correrVariante("C", null);
    correrVariante("D3", null);
    correrUc04();
    correrEstres();
  }

  private static boolean correrEscenario(String idEscenario, String idTope, int corridas) {
    ModelosJson.EscenarioJson escenario = escenario(idEscenario);
    Tope tope = tope(idTope);
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    return medir(modelo, escenario, tope, idEscenario, idTope, corridas);
  }

  private static void correrVariante(String idVariante, String idTope) {
    ModelosJson.VarianteJson variante =
        escenarios.variantes().stream()
            .filter(v -> v.id().equals(idVariante))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Variante desconocida: " + idVariante));
    ModelosJson.EscenarioJson escenario = escenario(variante.escenario());
    Tope tope = tope(idTope != null ? idTope : variante.tope());
    ModeloDeCalculo.Configuracion configuracion =
        new ModeloDeCalculo.Configuracion(
            variante.fijadas(), variante.prerrequisitos(), variante.correquisitos());
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, configuracion);
    medir(modelo, escenario, tope, "V-" + idVariante, tope.id(), CORRIDAS);
  }

  private static boolean medir(
      ModeloDeCalculo modelo,
      ModelosJson.EscenarioJson escenario,
      Tope tope,
      String etiquetaEscenario,
      String etiquetaTope,
      int corridas) {
    for (int corrida = 0; corrida < corridas; corrida++) {
      CalculoDelPlan calculo =
          new CalculoDelPlan(modelo, escenario, tope, new Cancelacion(plazoMs()));
      long inicio = System.nanoTime();
      ResultadoCalculo resultado = calculo.calcular();
      long msTotal = (System.nanoTime() - inicio) / 1_000_000L;
      Fila fila = fila(modelo, etiquetaEscenario, etiquetaTope, corrida, resultado, msTotal);
      if (resultado instanceof ResultadoCalculo.Calculado calculado) {
        List<String> errores =
            new VerificadorDePlan(modelo)
                .verificar(calculado.plan(), calculo.a0(), calculo.libreInicial(), tope);
        fila.verificador = errores.isEmpty() ? "OK" : String.join(" | ", errores);
        if (!errores.isEmpty()) {
          System.out.println("VERIFICADOR: " + fila.verificador);
        }
      }
      filas.add(fila);
      imprimir(fila);
      if (resultado instanceof ResultadoCalculo.TiempoAgotado) {
        return false;
      }
    }
    return true;
  }

  private static Fila fila(
      ModeloDeCalculo modelo,
      String escenario,
      String tope,
      int corrida,
      ResultadoCalculo resultado,
      long msTotal) {
    Fila fila = new Fila();
    fila.escenario = escenario;
    fila.tope = tope;
    fila.corrida = corrida;
    fila.msTotal = msTotal;
    if (resultado instanceof ResultadoCalculo.Calculado calculado) {
      fila.resultado = "calculado";
      fila.k = calculado.plan().k();
      fila.c2 = calculado.plan().marcados();
      fila.c3 = calculado.plan().creditosOptativas();
      int optativas = 0;
      for (int i = 0; i < modelo.cantidadMaterias(); i++) {
        if (calculado.plan().semestre(i) > 0
            && !modelo.obligatoria(i)
            && !modelo.fijas().contiene(i)) {
          optativas++;
        }
      }
      fila.optativas = optativas;
      fila.bloques = contarBloques(calculado.plan());
      fila.msExistencia = calculado.msExistencia();
      fila.msOptimos = calculado.msOptimos();
      fila.nodos = calculado.metricas().nodos;
      fila.memo = calculado.metricas().tamanoMemoizacion;
      fila.llamadas = calculado.metricas().llamadasExistencia;
    } else if (resultado instanceof ResultadoCalculo.TiempoAgotado agotado) {
      fila.resultado = "tiempo agotado";
      fila.nodos = agotado.metricas().nodos;
      fila.memo = agotado.metricas().tamanoMemoizacion;
      fila.llamadas = agotado.metricas().llamadasExistencia;
    } else if (resultado instanceof ResultadoCalculo.SinPlanPosible sinPlan) {
      fila.resultado = "sin plan: " + sinPlan.motivo();
    }
    return fila;
  }

  private static int contarBloques(Plan plan) {
    int total = 0;
    for (int bloque : plan.bloquePorSemestre()) {
      if (bloque > 0) {
        total++;
      }
    }
    return total;
  }

  private static void imprimir(Fila fila) {
    System.out.printf(
        "%-4s %-2s #%d: %-12s k=%s c2=%s c3=%s opt=%s bloques=%s ms=%d (exist=%s opt=%s) nodos=%s memo=%s llamadas=%s ver=%s%n",
        fila.escenario,
        fila.tope,
        fila.corrida,
        fila.resultado,
        fila.k,
        fila.c2,
        fila.c3,
        fila.optativas,
        fila.bloques,
        fila.msTotal,
        fila.msExistencia,
        fila.msOptimos,
        fila.nodos,
        fila.memo,
        fila.llamadas,
        fila.verificador);
  }

  private static void escribirCsv() {
    StringBuilder csv = new StringBuilder();
    csv.append(
        "escenario,tope,corrida,resultado,k,c2,c3,optativas,bloques,ms_existencia,ms_optimos,ms_total,nodos,memo,llamadas,verificador\n");
    for (Fila fila : filas) {
      csv.append(fila.escenario)
          .append(',')
          .append(fila.tope)
          .append(',')
          .append(fila.corrida)
          .append(',')
          .append(fila.resultado)
          .append(',')
          .append(valor(fila.k))
          .append(',')
          .append(valor(fila.c2))
          .append(',')
          .append(valor(fila.c3))
          .append(',')
          .append(valor(fila.optativas))
          .append(',')
          .append(valor(fila.bloques))
          .append(',')
          .append(valor(fila.msExistencia))
          .append(',')
          .append(valor(fila.msOptimos))
          .append(',')
          .append(fila.msTotal)
          .append(',')
          .append(fila.nodos)
          .append(',')
          .append(fila.memo)
          .append(',')
          .append(fila.llamadas)
          .append(',')
          .append(fila.verificador == null ? "" : fila.verificador.replace(',', ';'))
          .append('\n');
    }
    try {
      Path salida = Rutas.resultados().resolve("mediciones-" + LocalDate.now() + ".csv");
      Files.writeString(salida, csv.toString(), StandardCharsets.UTF_8);
      System.out.println("CSV: " + salida);
    } catch (IOException e) {
      throw new IllegalStateException("No puedo escribir el CSV", e);
    }
  }

  private static void resumir() {
    Map<String, List<Fila>> porCaso = new LinkedHashMap<>();
    for (Fila fila : filas) {
      porCaso.computeIfAbsent(fila.escenario + " " + fila.tope, k -> new ArrayList<>()).add(fila);
    }
    System.out.println("\nResumen (mediana / máximo de ms):");
    for (Map.Entry<String, List<Fila>> entrada : porCaso.entrySet()) {
      List<Long> tiempos = new ArrayList<>();
      for (Fila fila : entrada.getValue()) {
        tiempos.add(fila.msTotal);
      }
      tiempos.sort(Comparator.naturalOrder());
      long mediana = tiempos.get(tiempos.size() / 2);
      long maximo = tiempos.get(tiempos.size() - 1);
      System.out.printf("%-8s mediana=%6d ms  máximo=%6d ms%n", entrada.getKey(), mediana, maximo);
    }
  }

  private static void correrUc04() {
    ModelosJson.EscenarioJson escenario = escenario("E1");
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    String[] ids = {"T1", "T2", "T4"};
    System.out.println("\nUC04 secuencial:");
    long inicio = System.nanoTime();
    for (String id : ids) {
      medir(modelo, escenario, tope(id), "UC04-secuencial", id, 1);
    }
    long msSecuencial = (System.nanoTime() - inicio) / 1_000_000L;
    System.out.println("UC04 secuencial total: " + msSecuencial + " ms");

    System.out.println("UC04 en paralelo (hilos virtuales):");
    inicio = System.nanoTime();
    try (ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<String>> futuros = new ArrayList<>();
      for (String id : ids) {
        futuros.add(
            ejecutor.submit(
                () -> {
                  CalculoDelPlan calculo =
                      new CalculoDelPlan(modelo, escenario, tope(id), new Cancelacion(plazoMs()));
                  ResultadoCalculo resultado = calculo.calcular();
                  return id + ": " + etiquetaResultado(resultado);
                }));
      }
      for (Future<String> futuro : futuros) {
        System.out.println("  " + futuro.get());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Falló UC04 en paralelo", e);
    }
    long msParalelo = (System.nanoTime() - inicio) / 1_000_000L;
    System.out.println("UC04 paralelo total: " + msParalelo + " ms");
  }

  private static void correrEstres() {
    System.out.println("\nEstrés de cancelación: malla duplicada no implementada en este corte.");
  }

  private static String etiquetaResultado(ResultadoCalculo resultado) {
    if (resultado instanceof ResultadoCalculo.Calculado calculado) {
      return "calculado k="
          + calculado.plan().k()
          + " en "
          + (calculado.msExistencia() + calculado.msOptimos())
          + " ms";
    }
    if (resultado instanceof ResultadoCalculo.TiempoAgotado agotado) {
      return "tiempo agotado en " + agotado.ms() + " ms";
    }
    return "sin plan: " + ((ResultadoCalculo.SinPlanPosible) resultado).motivo();
  }

  private static ModelosJson.EscenarioJson escenario(String id) {
    return escenarios.escenarios().stream()
        .filter(e -> e.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Escenario desconocido: " + id));
  }

  private static Tope tope(String id) {
    ModelosJson.TopeJson tope =
        escenarios.topes().stream()
            .filter(t -> t.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tope desconocido: " + id));
    return new Tope(tope.id(), tope.creditos(), tope.materias());
  }

  private static String valor(Object valor) {
    return valor == null ? "" : String.valueOf(valor);
  }

  private static final class Fila {
    private String escenario;
    private String tope;
    private int corrida;
    private String resultado;
    private Integer k;
    private Integer c2;
    private Integer c3;
    private Integer optativas;
    private Integer bloques;
    private Long msExistencia;
    private Long msOptimos;
    private long msTotal;
    private long nodos;
    private long memo;
    private long llamadas;
    private String verificador;
  }
}
