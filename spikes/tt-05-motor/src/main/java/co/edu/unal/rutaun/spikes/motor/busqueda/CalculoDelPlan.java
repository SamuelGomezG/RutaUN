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
  private final int n;
  private final Metricas metricas = new Metricas();

  public CalculoDelPlan(
      ModeloDeCalculo modelo,
      ModelosJson.EscenarioJson escenario,
      Tope tope,
      Cancelacion cancelacion) {
    this.modelo = modelo;
    this.tope = tope;
    this.cancelacion = cancelacion;
    this.n = modelo.cantidadMaterias();
    this.a0 = new ConjuntoDeMaterias(n);
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
    if (modelo.motivoSinPlan() != null) {
      return new ResultadoCalculo.SinPlanPosible(modelo.motivoSinPlan());
    }
    int[] asignacionInicial = modelo.asignacionInicial();
    Cotas cotas = new Cotas(modelo, tope, libreInicial, new ConjuntoDeMaterias(n));
    int cota = cotas.cotaInferior(a0, libreInicial, asignacionInicial);
    if (cota >= ModeloDeCalculo.INFINITO) {
      return new ResultadoCalculo.SinPlanPosible("La cota inferior no es finita");
    }
    int kMax = Math.max(cota, kMaximo(asignacionInicial));
    long inicio = System.nanoTime();
    int k = -1;
    for (int candidato = Math.max(1, cota); candidato <= kMax; candidato++) {
      if (cancelacion.cancelado()) {
        return tiempoAgotado(inicio);
      }
      Restricciones base = new Restricciones(n);
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
      return new ResultadoCalculo.SinPlanPosible("No existe plan dentro de kMax");
    }
    long msExistencia = (System.nanoTime() - inicio) / 1_000_000L;
    long inicioOptimos = System.nanoTime();

    Restricciones base = new Restricciones(n);
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
      return new ResultadoCalculo.SinPlanPosible("No se pudo fijar el criterio 2");
    }

    // c3: menos créditos en optativas elegidas no fijadas.
    ResultadoBusqueda conPlan = buscar(k, base, true);
    if (conPlan.cancelada()) {
      return tiempoAgotado(inicio);
    }
    if (conPlan.plan() == null) {
      return new ResultadoCalculo.SinPlanPosible("No se pudo fijar una cota para el criterio 3");
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
      return new ResultadoCalculo.SinPlanPosible("No se pudo fijar el criterio 3");
    }
    base.maxCreditosOptativas = c3;

    // c4: lista de optativas elegidas, en orden del consolidado, lexicográficamente menor.
    base.soloIncluidas = false;
    base.incluidas = new ConjuntoDeMaterias(n);
    base.obligatoriasExtra = new ConjuntoDeMaterias(n);
    base.excluidas = new ConjuntoDeMaterias(n);
    for (int materia = 0; materia < n; materia++) {
      if (!modelo.cursable(materia) || modelo.requeridas().contiene(materia)) {
        continue;
      }
      base.incluidas.agregar(materia);
      base.obligatoriasExtra.agregar(materia);
      Plan testigoC4 = ConstructorTestigo.construir(modelo, a0, libreInicial, tope, base);
      if (testigoC4 == null) {
        base.incluidas.quitar(materia);
        base.obligatoriasExtra.quitar(materia);
        base.excluidas.agregar(materia);
      }
    }

    Plan planBase = ConstructorTestigo.construir(modelo, a0, libreInicial, tope, base);
    if (planBase == null) {
      ResultadoBusqueda respaldo = buscar(k, base, true);
      if (respaldo.cancelada()) {
        return tiempoAgotado(inicio);
      }
      planBase = respaldo.plan();
    }
    if (planBase == null) {
      return new ResultadoCalculo.SinPlanPosible("No se pudo reconstruir el plan inicial");
    }

    Plan planC5 =
        PlanOptimizador.optimizarSemestres(modelo, a0, libreInicial, tope, planBase, c2, k);
    Plan planFinal = PlanOptimizador.optimizarBloques(modelo, a0, libreInicial, tope, planC5, c2);
    if (planFinal.k() != k) {
      throw new IllegalStateException(
          "El plan final tiene " + planFinal.k() + " semestres y la cota fijó " + k);
    }
    long msOptimos = (System.nanoTime() - inicioOptimos) / 1_000_000L;
    return new ResultadoCalculo.Calculado(planFinal, metricas.copia(), msExistencia, msOptimos);
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
    for (int i = 0; i < n; i++) {
      if ((palabras[i >>> 6] & (1L << (i & 63))) != 0 && !a0.contiene(i)) {
        pendientes++;
      }
    }
    int materiasOptativas = 0;
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      int minimo = modelo.minimoMateriasOptativas(grupo.indice(), a0, asignacionInicial);
      if (minimo >= ModeloDeCalculo.INFINITO) {
        return Math.max(1, pendientes + ceilDiv(libreInicial, 3));
      }
      materiasOptativas += minimo;
    }
    int calculado = pendientes + materiasOptativas + ceilDiv(libreInicial, 3);
    return Math.max(1, Math.min(200, Math.max(calculado, pendientes + 1)));
  }

  private ResultadoBusqueda buscar(int k, Restricciones restricciones, boolean capturar) {
    Buscador buscador = new Buscador(modelo, a0, libreInicial, tope, cancelacion, restricciones);
    boolean encontrado = buscador.buscar(k, capturar);
    Metricas parciales = buscador.metricas();
    metricas.sumar(parciales);
    return new ResultadoBusqueda(encontrado, buscador.plan(), parciales.cancelada);
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
    metricas.cancelada = true;
    return new ResultadoCalculo.TiempoAgotado(metricas.copia(), ms);
  }

  private static int ceilDiv(int numerador, int denominador) {
    return (numerador + denominador - 1) / denominador;
  }

  private record ResultadoBusqueda(boolean encontrado, Plan plan, boolean cancelada) {}
}
