package co.edu.unal.rutaun.spikes.motor.medicion;

import co.edu.unal.rutaun.spikes.motor.Rutas;
import co.edu.unal.rutaun.spikes.motor.busqueda.CalculoDelPlan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Cancelacion;
import co.edu.unal.rutaun.spikes.motor.busqueda.Metricas;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Corre los escenarios, verifica los planes y escribe el CSV de mediciones. */
public final class Medir {

  private static final int CALENTAMIENTO = 3;
  private static final int MEDIDAS = 20;
  private static final int PLAZO_MS = 30_000;

  private static int plazoMs() {
    return Integer.parseInt(System.getProperty("tt05.plazo", String.valueOf(PLAZO_MS)));
  }

  private static boolean publicar() {
    return Boolean.getBoolean("tt05.publicar");
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
        correrEscenario(args[0], args[1]);
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
    correrEscenario("E0", "T2");
    correrEscenario("E1", "T1");
    correrEscenario("E1", "T2");
    correrEscenario("E1", "T4");
    correrEscenario("E2", "T2");
    correrEscenario("E3", "T2");
    correrEscenario("E4", "T2");
    correrVariante("C", null);
    correrVariante("C2", null);
    correrVariante("CC", null);
    correrVariante("D3", null);
    correrUc04();
  }

  private static void todos() {
    String[] ids = {"E0", "E1", "E2", "E3", "E4"};
    String[] topes = {"T1", "T2", "T3", "T4", "T5"};
    for (String escenario : ids) {
      for (String tope : topes) {
        correrEscenario(escenario, tope);
      }
    }
    correrVariante("F", null);
    correrVariante("C", null);
    correrVariante("C2", null);
    correrVariante("CC", null);
    correrVariante("D3", null);
    correrUc04();
    correrEstres();
  }

  private static void correrEscenario(String idEscenario, String idTope) {
    ModelosJson.EscenarioJson escenario = escenario(idEscenario);
    Tope tope = tope(idTope);
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    medir(modelo, escenario, tope, idEscenario, idTope, "medida", MEDIDAS, true);
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
    medir(modelo, escenario, tope, "V-" + idVariante, tope.id(), "medida", MEDIDAS, true);
  }

  private static void medir(
      ModeloDeCalculo modelo,
      ModelosJson.EscenarioJson escenario,
      Tope tope,
      String etiquetaEscenario,
      String etiquetaTope,
      String fase,
      int medidas,
      boolean calentar) {
    if (calentar) {
      for (int i = 0; i < CALENTAMIENTO; i++) {
        Ejecucion ejecucion = ejecutar(modelo, escenario, tope);
        if (ejecucion.resultado() instanceof ResultadoCalculo.TiempoAgotado) {
          registrar(modelo, etiquetaEscenario, etiquetaTope, fase, 0, ejecucion);
          return;
        }
      }
    }
    for (int i = 0; i < medidas; i++) {
      Ejecucion ejecucion = ejecutar(modelo, escenario, tope);
      registrar(modelo, etiquetaEscenario, etiquetaTope, fase, i, ejecucion);
      if (ejecucion.resultado() instanceof ResultadoCalculo.TiempoAgotado) {
        return;
      }
    }
  }

  private static Ejecucion ejecutar(
      ModeloDeCalculo modelo, ModelosJson.EscenarioJson escenario, Tope tope) {
    CalculoDelPlan calculo =
        new CalculoDelPlan(modelo, escenario, tope, new Cancelacion(plazoMs()));
    long inicio = System.nanoTime();
    ResultadoCalculo resultado = calculo.calcular();
    long msTotal = (System.nanoTime() - inicio) / 1_000_000L;
    return new Ejecucion(calculo, resultado, msTotal);
  }

  private static void registrar(
      ModeloDeCalculo modelo,
      String escenario,
      String tope,
      String fase,
      int corrida,
      Ejecucion ejecucion) {
    Fila fila =
        fila(modelo, escenario, tope, fase, corrida, ejecucion.resultado(), ejecucion.msTotal());
    if (ejecucion.resultado() instanceof ResultadoCalculo.Calculado calculado) {
      List<String> errores =
          new VerificadorDePlan(modelo)
              .verificar(
                  calculado.plan(),
                  ejecucion.calculo().a0(),
                  ejecucion.calculo().libreInicial(),
                  tope(tope));
      fila.verificador = errores.isEmpty() ? "OK" : String.join(" | ", errores);
      if (!errores.isEmpty()) {
        System.out.println("VERIFICADOR: " + fila.verificador);
      }
    }
    filas.add(fila);
    imprimir(fila);
  }

  private static Fila fila(
      ModeloDeCalculo modelo,
      String escenario,
      String tope,
      String fase,
      int corrida,
      ResultadoCalculo resultado,
      long msTotal) {
    Fila fila = new Fila();
    fila.fase = fase;
    fila.escenario = escenario;
    fila.tope = tope;
    fila.corrida = corrida;
    fila.msTotal = msTotal;
    if (resultado instanceof ResultadoCalculo.Calculado calculado) {
      fila.resultado = "calculado";
      fila.semestres = calculado.plan().k();
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
      Metricas.Instantanea metricas = calculado.metricas().instantanea();
      fila.nodos = metricas.nodos();
      fila.memo = metricas.tamanoMemoizacion();
      fila.llamadas = metricas.llamadasExistencia();
      fila.huella = calculado.plan().huella();
    } else if (resultado instanceof ResultadoCalculo.TiempoAgotado agotado) {
      fila.resultado = "tiempo agotado";
      Metricas.Instantanea metricas = agotado.metricas().instantanea();
      fila.nodos = metricas.nodos();
      fila.memo = metricas.tamanoMemoizacion();
      fila.llamadas = metricas.llamadasExistencia();
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
        "%-16s %-4s %-2s #%d: %-12s k=%s c2=%s c3=%s opt=%s bloques=%s "
            + "ms=%d (exist=%s opt=%s) nodos=%s memo=%s llamadas=%s huella=%s ver=%s%n",
        fila.fase,
        fila.escenario,
        fila.tope,
        fila.corrida,
        fila.resultado,
        fila.semestres,
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
        fila.huella,
        fila.verificador);
  }

  private static void escribirCsv() {
    StringBuilder csv = new StringBuilder();
    csv.append(
        "fase,escenario,tope,corrida,resultado,k,c2,c3,optativas,bloques,"
            + "ms_existencia,ms_optimos,ms_total,nodos,memo,llamadas,huella,verificador\n");
    for (Fila fila : filas) {
      csv.append(fila.fase)
          .append(',')
          .append(fila.escenario)
          .append(',')
          .append(fila.tope)
          .append(',')
          .append(fila.corrida)
          .append(',')
          .append(fila.resultado)
          .append(',')
          .append(valor(fila.semestres))
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
          .append(valor(fila.huella))
          .append(',')
          .append(fila.verificador == null ? "" : fila.verificador.replace(',', ';'))
          .append('\n');
    }
    try {
      Path salida = Rutas.mediciones(publicar());
      Files.writeString(salida, csv.toString(), StandardCharsets.UTF_8);
      System.out.println("CSV: " + salida);
    } catch (IOException e) {
      throw new IllegalStateException("No puedo escribir el CSV", e);
    }
  }

  private static void resumir() {
    Map<String, List<Fila>> porCaso = new LinkedHashMap<>();
    for (Fila fila : filas) {
      porCaso
          .computeIfAbsent(
              fila.fase + " " + fila.escenario + " " + fila.tope, k -> new ArrayList<>())
          .add(fila);
    }
    System.out.println("\nResumen (mediana / p95 / máximo de ms):");
    for (Map.Entry<String, List<Fila>> entrada : porCaso.entrySet()) {
      List<Long> tiempos = new ArrayList<>();
      for (Fila fila : entrada.getValue()) {
        tiempos.add(fila.msTotal);
      }
      tiempos.sort(Comparator.naturalOrder());
      long mediana = tiempos.get(tiempos.size() / 2);
      int indiceP95 = (int) Math.ceil(0.95 * tiempos.size()) - 1;
      if (indiceP95 < 0) {
        indiceP95 = 0;
      }
      long p95 = tiempos.get(indiceP95);
      long maximo = tiempos.get(tiempos.size() - 1);
      System.out.printf(
          "%-32s mediana=%6d p95=%6d máximo=%6d%n", entrada.getKey(), mediana, p95, maximo);
    }
  }

  private static void correrUc04() {
    ModelosJson.EscenarioJson escenario = escenario("E1");
    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    String[] ids = {"T1", "T2", "T3", "T4", "T5"};
    System.out.println("\nUC04 secuencial:");
    long inicio = System.nanoTime();
    for (String id : ids) {
      medir(modelo, escenario, tope(id), "UC04-secuencial", id, "uc04-secuencial", 1, false);
    }
    long msSecuencial = (System.nanoTime() - inicio) / 1_000_000L;
    System.out.println("UC04 secuencial total: " + msSecuencial + " ms");
    correrUc04Paralelo(modelo, escenario, ids);
  }

  private static void correrUc04Paralelo(
      ModeloDeCalculo modelo, ModelosJson.EscenarioJson escenario, String[] ids) {
    System.out.println("UC04 en paralelo (hilos virtuales):");
    long inicio = System.nanoTime();
    try (ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<ResultadoCalculo>> futuros = new ArrayList<>();
      List<CalculoDelPlan> calculos = new ArrayList<>();
      List<Long> inicios = new ArrayList<>();
      for (String id : ids) {
        Cancelacion cancelacion = new Cancelacion(plazoMs());
        CalculoDelPlan calculo = new CalculoDelPlan(modelo, escenario, tope(id), cancelacion);
        long inicioTarea = System.nanoTime();
        CompletableFuture<ResultadoCalculo> futuro =
            CompletableFuture.supplyAsync(calculo::calcular, ejecutor)
                .orTimeout(plazoMs(), TimeUnit.MILLISECONDS)
                .whenComplete(
                    (resultado, error) -> {
                      if (esTimeout(error)) {
                        cancelacion.cancelar();
                      }
                    });
        futuros.add(futuro);
        calculos.add(calculo);
        inicios.add(inicioTarea);
      }
      for (int i = 0; i < ids.length; i++) {
        String id = ids[i];
        ResultadoCalculo resultado;
        try {
          resultado = futuros.get(i).join();
        } catch (CompletionException e) {
          resultado = new ResultadoCalculo.TiempoAgotado(new Metricas(), plazoMs());
        }
        long msTotal = (System.nanoTime() - inicios.get(i)) / 1_000_000L;
        Ejecucion ejecucion = new Ejecucion(calculos.get(i), resultado, msTotal);
        registrar(modelo, "UC04-paralelo", id, "uc04-paralelo", 0, ejecucion);
      }
    }
    long msParalelo = (System.nanoTime() - inicio) / 1_000_000L;
    System.out.println("UC04 paralelo total: " + msParalelo + " ms");
  }

  private static boolean esTimeout(Throwable error) {
    Throwable actual = error;
    while (actual != null) {
      if (actual instanceof TimeoutException) {
        return true;
      }
      actual = actual.getCause();
    }
    return false;
  }

  private static void correrEstres() {
    System.out.println("\nEstrés de cancelación: malla duplicada no implementada en este corte.");
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

  private record Ejecucion(CalculoDelPlan calculo, ResultadoCalculo resultado, long msTotal) {}

  private static final class Fila {
    private String fase;
    private String escenario;
    private String tope;
    private int corrida;
    private String resultado;
    private Integer semestres;
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
    private Integer huella;
    private String verificador;
  }
}
