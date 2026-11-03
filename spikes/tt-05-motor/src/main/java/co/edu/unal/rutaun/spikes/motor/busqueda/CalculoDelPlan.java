package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;

/**
 * Cálculo exacto por fijación sucesiva: primero el mínimo k y después, criterio a criterio, el
 * valor óptimo con el oráculo de existencia.
 */
public final class CalculoDelPlan {

  private final ModeloDeCalculo modelo;
  private final ConjuntoDeMaterias a0;
  private final int libreInicial;
  private final Tope tope;
  private final Cancelacion cancelacion;
  private final int cantidadMaterias;
  private final Metricas metricas = new Metricas();

  /**
   * Tope de nodos de la búsqueda en profundidad por cada inclusión de la vía de rescate del
   * criterio 4. Los rescates que tuvieron éxito en los escenarios medidos necesitaron entre 13 y 79
   * nodos, salvo uno de E0/T3 con 17.543. No es una norma académica: es un parámetro del motor.
   */
  private static final long NODOS_POR_INCLUSION = 1_000;

  public CalculoDelPlan(
      ModeloDeCalculo modelo,
      ModelosJson.EscenarioJson escenario,
      Tope tope,
      Cancelacion cancelacion) {
    this.modelo = modelo;
    this.tope = tope;
    this.cancelacion = cancelacion;
    this.cantidadMaterias = modelo.cantidadMaterias();
    this.a0 = new ConjuntoDeMaterias(cantidadMaterias);
    for (String codigo : escenario.aprobadas()) {
      this.a0.agregar(indice(codigo));
    }
    for (String codigo : escenario.enCurso()) {
      this.a0.agregar(indice(codigo));
    }
    int libre = modelo.libreEleccion() - escenario.libreAprobados() - escenario.libreEnCurso();
    this.libreInicial = Math.max(0, libre);
  }

  public ConjuntoDeMaterias a0() {
    return a0;
  }

  public int libreInicial() {
    return libreInicial;
  }

  public ResultadoCalculo calcular() {
    long inicio = System.nanoTime();
    if (modelo.motivoSinPlan() != null) {
      return new ResultadoCalculo.SinPlanPosible(modelo.motivoSinPlan());
    }
    if (cancelacion.cancelado()) {
      return tiempoAgotado(inicio);
    }
    int[] asignacionInicial = modelo.asignacionInicial();
    Cotas cotas =
        new Cotas(
            modelo, tope, libreInicial, new ConjuntoDeMaterias(cantidadMaterias), cancelacion);
    int cota = cotas.cotaInferior(a0, libreInicial, asignacionInicial);
    if (cota >= ModeloDeCalculo.INFINITO) {
      if (cancelacion.cancelado()) {
        return tiempoAgotado(inicio);
      }
      return new ResultadoCalculo.SinPlanPosible("La cota inferior no es finita");
    }
    int maximoSemestres = Math.max(cota, kMaximo(asignacionInicial));
    int k = -1;
    for (int candidato = Math.max(1, cota); candidato <= maximoSemestres; candidato++) {
      if (cancelacion.cancelado()) {
        return tiempoAgotado(inicio);
      }
      Restricciones base = new Restricciones(cantidadMaterias);
      ResultadoBusqueda resultado = buscar(candidato, base, false);
      if (resultado.cancelada()) {
        return tiempoAgotado(inicio);
      }
      if (resultado.encontrado()) {
        k = candidato;
        break;
      }
    }
    if (k < 0) {
      return new ResultadoCalculo.SinPlanPosible("No existe plan dentro de maximoSemestres");
    }
    final long msExistencia = (System.nanoTime() - inicio) / 1_000_000L;
    final long inicioOptimos = System.nanoTime();

    Restricciones base = new Restricciones(cantidadMaterias);
    base.maxSemestres = k;

    // c2: menos semestres con marca de autorización.
    int c2 = -1;
    for (int j = 0; j <= k; j++) {
      Restricciones prueba = base.copia();
      prueba.maxSemestresMarcados = j;
      ResultadoBusqueda resultado = buscar(k, prueba, false);
      if (resultado.cancelada()) {
        return tiempoAgotado(inicio);
      }
      if (resultado.encontrado()) {
        c2 = j;
        base = prueba;
        break;
      }
    }
    if (c2 < 0) {
      return new ResultadoCalculo.NoCalculado("No se pudo fijar el criterio 2");
    }

    // c3: menos créditos en optativas elegidas no fijadas.
    ResultadoBusqueda conPlan = buscar(k, base, true);
    if (conPlan.cancelada()) {
      return tiempoAgotado(inicio);
    }
    if (conPlan.plan() == null) {
      return new ResultadoCalculo.NoCalculado("No se pudo fijar una cota para el criterio 3");
    }
    int inferiorC3 = 0;
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      inferiorC3 += modelo.minimoCreditosOptativos(grupo.indice(), a0, asignacionInicial);
    }
    int superiorC3 = conPlan.plan().creditosOptativas();
    inferiorC3 = Math.min(inferiorC3, superiorC3);
    int c3 = busquedaBinariaC3(k, base, inferiorC3, superiorC3, inicio);
    if (c3 < 0) {
      if (cancelacion.cancelado()) {
        return tiempoAgotado(inicio);
      }
      return new ResultadoCalculo.NoCalculado("No se pudo fijar el criterio 3");
    }
    base.maxCreditosOptativas = c3;

    // Plan que cumple los criterios 1 a 3; sirve de respaldo si el criterio 4 no puede aplicarse.
    ResultadoBusqueda conC3 = buscar(k, base, true);
    if (conC3.cancelada()) {
      return tiempoAgotado(inicio);
    }
    Plan planC3 = conC3.plan();

    // c4: lista de optativas elegidas, en orden del consolidado. Vía rápida: el constructor decide
    // cada inclusión. Si no logra reconstruir el plan, vía de rescate: la búsqueda en profundidad,
    // con un tope de nodos por inclusión, decide en su lugar.
    ResultadoC4 listaRapida = fijarListaDeOptativas(k, base, false);
    if (listaRapida.cancelada()) {
      return tiempoAgotado(inicio);
    }
    Plan planBase = listaRapida.plan();
    boolean c4PorRescate = false;
    if (planBase == null) {
      ResultadoC4 listaRescate = fijarListaDeOptativas(k, base, true);
      if (listaRescate.cancelada()) {
        return tiempoAgotado(inicio);
      }
      planBase = listaRescate.plan();
      c4PorRescate = planBase != null;
    }
    final boolean c4Aplicado = planBase != null;
    if (!c4Aplicado) {
      planBase = planC3;
    }
    if (planBase == null) {
      return new ResultadoCalculo.NoCalculado("No se pudo reconstruir el plan");
    }

    Plan planC5 =
        PlanOptimizador.optimizarSemestres(
            modelo, a0, libreInicial, tope, planBase, c2, k, cancelacion);
    if (planC5 == null) {
      return tiempoAgotado(inicio);
    }
    Plan planFinal =
        PlanOptimizador.optimizarBloques(modelo, a0, libreInicial, tope, planC5, c2, cancelacion);
    if (planFinal == null) {
      return tiempoAgotado(inicio);
    }
    if (planFinal.k() != k) {
      throw new IllegalStateException(
          "El plan final tiene " + planFinal.k() + " semestres y la cota fijó " + k);
    }
    long msOptimos = (System.nanoTime() - inicioOptimos) / 1_000_000L;
    return new ResultadoCalculo.Calculado(
        planFinal, metricas.copia(), msExistencia, msOptimos, c4Aplicado, c4PorRescate);
  }

  /**
   * Recorre las optativas en el orden del consolidado y deja en la lista las que admiten un plan.
   * Con {@code conRescate} falso decide el constructor y el plan es su reconstrucción final; con
   * {@code conRescate} verdadero decide {@link #buscar} con un tope de {@link #NODOS_POR_INCLUSION}
   * nodos y el plan es el último que devolvió. Una exclusión por rescate significa «no se halló
   * plan con ese tope», no «no existe».
   */
  private ResultadoC4 fijarListaDeOptativas(int k, Restricciones baseC3, boolean conRescate) {
    Restricciones lista = baseC3.copia();
    lista.soloIncluidas = false;
    lista.incluidas = new ConjuntoDeMaterias(cantidadMaterias);
    lista.obligatoriasExtra = new ConjuntoDeMaterias(cantidadMaterias);
    lista.excluidas = new ConjuntoDeMaterias(cantidadMaterias);
    Plan ultimoPlan = null;
    for (int materia = 0; materia < cantidadMaterias; materia++) {
      if (cancelacion.cancelado()) {
        return ResultadoC4.CANCELADO;
      }
      if (!modelo.cursable(materia) || modelo.requeridas().contiene(materia)) {
        continue;
      }
      lista.incluidas.agregar(materia);
      lista.obligatoriasExtra.agregar(materia);
      Plan testigo;
      if (conRescate) {
        ResultadoBusqueda inclusion = buscar(k, lista, true, NODOS_POR_INCLUSION);
        if (inclusion.cancelada()) {
          return ResultadoC4.CANCELADO;
        }
        testigo = inclusion.plan();
      } else {
        testigo = ConstructorTestigo.construir(modelo, a0, libreInicial, tope, lista, cancelacion);
      }
      if (testigo != null) {
        ultimoPlan = testigo;
      } else {
        if (cancelacion.cancelado()) {
          return ResultadoC4.CANCELADO;
        }
        lista.incluidas.quitar(materia);
        lista.obligatoriasExtra.quitar(materia);
        lista.excluidas.agregar(materia);
      }
    }
    if (!conRescate) {
      ultimoPlan = ConstructorTestigo.construir(modelo, a0, libreInicial, tope, lista, cancelacion);
      if (ultimoPlan == null && cancelacion.cancelado()) {
        return ResultadoC4.CANCELADO;
      }
    }
    return new ResultadoC4(ultimoPlan, false);
  }

  private int busquedaBinariaC3(
      int k, Restricciones base, int inferior, int superior, long inicio) {
    int lo = Math.max(0, inferior);
    int hi = superior;
    while (lo < hi) {
      int medio = (lo + hi) >>> 1;
      Restricciones prueba = base.copia();
      prueba.maxCreditosOptativas = medio;
      ResultadoBusqueda resultado = buscar(k, prueba, false);
      if (resultado.cancelada()) {
        return -1;
      }
      if (resultado.encontrado()) {
        hi = medio;
      } else {
        lo = medio + 1;
      }
    }
    return lo;
  }

  private int kMaximo(int[] asignacionInicial) {
    int pendientes = 0;
    long[] palabras = modelo.requeridas().palabras();
    for (int i = 0; i < cantidadMaterias; i++) {
      if ((palabras[i >>> 6] & (1L << (i & 63))) != 0 && !a0.contiene(i)) {
        pendientes++;
      }
    }
    int materiasOptativas = 0;
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      int minimo = modelo.minimoMateriasOptativas(grupo.indice(), a0, asignacionInicial);
      if (minimo >= ModeloDeCalculo.INFINITO) {
        return Math.max(
            1, pendientes + Math.ceilDiv(libreInicial, modelo.creditosPorMateriaLibre()));
      }
      materiasOptativas += minimo;
    }
    int calculado =
        pendientes
            + materiasOptativas
            + Math.ceilDiv(libreInicial, modelo.creditosPorMateriaLibre());
    return Math.max(
        1, Math.min(ModeloDeCalculo.LIMITE_DE_SEMESTRES, Math.max(calculado, pendientes + 1)));
  }

  private ResultadoBusqueda buscar(int k, Restricciones restricciones, boolean capturar) {
    return buscar(k, restricciones, capturar, Long.MAX_VALUE);
  }

  private ResultadoBusqueda buscar(
      int k, Restricciones restricciones, boolean capturar, long limiteDeNodos) {
    Buscador buscador = new Buscador(modelo, a0, libreInicial, tope, cancelacion, restricciones);
    buscador.limitarNodos(limiteDeNodos);
    boolean encontrado = buscador.buscar(k, capturar);
    Metricas parciales = buscador.metricas();
    metricas.sumar(parciales);
    return new ResultadoBusqueda(encontrado, buscador.plan(), parciales.instantanea().cancelada());
  }

  private int indice(String codigo) {
    Integer indice = modelo.indiceDe(codigo);
    if (indice == null) {
      throw new IllegalArgumentException("Código de escenario desconocido: " + codigo);
    }
    return indice;
  }

  private ResultadoCalculo tiempoAgotado(long inicio) {
    long ms = (System.nanoTime() - inicio) / 1_000_000L;
    metricas.marcarCancelada();
    return new ResultadoCalculo.TiempoAgotado(metricas.copia(), ms);
  }

  private record ResultadoBusqueda(boolean encontrado, Plan plan, boolean cancelada) {}

  private record ResultadoC4(Plan plan, boolean cancelada) {
    private static final ResultadoC4 CANCELADO = new ResultadoC4(null, true);
  }
}
