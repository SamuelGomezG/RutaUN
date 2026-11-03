package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo.Habilitacion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Búsqueda en profundidad, semestre a semestre, con cotas, simetrías y memoización. */
public final class Buscador {

  private final ModeloDeCalculo modelo;
  private final ConjuntoDeMaterias a0;
  private final int libreInicial;
  private final Tope tope;
  private final Cancelacion cancelacion;
  private final Restricciones restricciones;
  private final Cotas cotas;
  private final ConjuntoDeMaterias extraRequeridas;
  private final int cantidadMaterias;

  private ConjuntoDeMaterias cursadas;
  private int libre;
  private int[] asignacion;
  private int[] semestre;
  private int[] bloque;
  private int[] creditosPorSemestre;
  private int marcados;
  private int creditosOptativas;
  private int semestres;
  private boolean capturar;
  private boolean encontrado;
  private Plan plan;
  private Set<Estado> fallidos;
  private final Metricas metricas = new Metricas();
  private long contadorSubconjuntos;

  public Buscador(
      ModeloDeCalculo modelo,
      ConjuntoDeMaterias a0,
      int libreInicial,
      Tope tope,
      Cancelacion cancelacion,
      Restricciones restricciones) {
    this.modelo = modelo;
    this.a0 = a0;
    this.libreInicial = libreInicial;
    this.tope = tope;
    this.cancelacion = cancelacion;
    this.restricciones = restricciones;
    this.extraRequeridas = restricciones.obligatoriasExtra;
    this.cotas = new Cotas(modelo, tope, libreInicial, extraRequeridas, cancelacion);
    this.cantidadMaterias = modelo.cantidadMaterias();
  }

  public Metricas metricas() {
    return metricas;
  }

  public Plan plan() {
    return plan;
  }

  public boolean buscar(int semestres, boolean capturar) {
    this.semestres = semestres;
    this.capturar = capturar;
    this.encontrado = false;
    this.plan = null;
    this.cursadas = a0.copia();
    this.libre = libreInicial;
    this.asignacion = modelo.asignacionInicial();
    this.semestre = new int[cantidadMaterias];
    Arrays.fill(this.semestre, -1);
    this.bloque = new int[semestres + 1];
    this.creditosPorSemestre = new int[semestres + 1];
    this.marcados = 0;
    this.creditosOptativas = 0;
    this.fallidos = new HashSet<>();
    this.contadorSubconjuntos = 0;
    this.metricas.nodos = 0;
    this.metricas.llamadasExistencia++;
    this.metricas.cancelada = false;
    for (int i = 0; i < cantidadMaterias; i++) {
      if ((cursadas.contiene(i) || extraRequeridas.contiene(i)) && asignacion[i] < 0) {
        int[] grupos = modelo.gruposDeMateria(i);
        if (grupos.length > 0) {
          asignacion[i] = grupos[0];
        }
      }
    }
    if (modelo.motivoSinPlan() != null) {
      return false;
    }
    Plan testigo =
        ConstructorTestigo.construir(modelo, a0, libreInicial, tope, restricciones, cancelacion);
    if (testigo != null && testigo.k() <= semestres) {
      this.plan = testigo;
      this.encontrado = true;
      this.metricas.tamanoMemoizacion = 0;
      return true;
    }
    if (cancelacion.cancelado()) {
      this.metricas.cancelada = true;
      return false;
    }
    boolean resultado = dfs(1);
    this.metricas.tamanoMemoizacion = Math.max(metricas.tamanoMemoizacion, fallidos.size());
    return resultado && encontrado;
  }

  private boolean dfs(int s) {
    metricas.nodos++;
    if (cancelacion.cancelado()) {
      metricas.cancelada = true;
      return false;
    }
    if (s > semestres) {
      return cerrarPlan();
    }
    if (s - 1 + cotas.cotaInferior(cursadas, libre, asignacion) > semestres) {
      return false;
    }
    if (marcados > restricciones.maxSemestresMarcados
        || creditosOptativas > restricciones.maxCreditosOptativas) {
      return false;
    }
    for (int i = 0; i < cantidadMaterias; i++) {
      if (restricciones.semestreFijo[i] >= 0
          && restricciones.semestreFijo[i] < s
          && !cursadas.contiene(i)) {
        return false;
      }
      if (restricciones.semestreFijo[i] >= 0
          && cursadas.contiene(i)
          && semestre[i] != restricciones.semestreFijo[i]) {
        return false;
      }
      if (restricciones.semestreMaximo[i] < s && !cursadas.contiene(i)) {
        return false;
      }
      if (cursadas.contiene(i) && semestre[i] > restricciones.semestreMaximo[i]) {
        return false;
      }
    }

    Estado estado = new Estado(s, libre, marcados, cursadas.copiaPalabras(), asignacion.clone());
    if (fallidos.contains(estado)) {
      return false;
    }

    long[] utiles = Seleccion.clausulasUtiles(modelo, cursadas, restricciones);

    List<Integer> candidatos = new ArrayList<>();
    for (int i = 0; i < cantidadMaterias; i++) {
      if (cursadas.contiene(i) || !modelo.cursable(i)) {
        continue;
      }
      if (restricciones.excluidas.contiene(i)) {
        continue;
      }
      boolean requerida = Seleccion.esRequerida(i, modelo, restricciones);
      if (!requerida) {
        boolean porAgrupacion = false;
        for (int grupo : modelo.gruposDeMateria(i)) {
          if (modelo.faltan(grupo, cursadas, asignacion, extraRequeridas) > 0) {
            porAgrupacion = true;
            break;
          }
        }
        boolean util = (utiles[i >>> 6] & (1L << (i & 63))) != 0;
        if (!porAgrupacion && !util) {
          continue;
        }
      }
      if (!modelo.clausulasCumplidas(modelo.prerrequisitos(i), cursadas)) {
        continue;
      }
      if (!modelo.habilitacionesCumplidas(i, cursadas, libreInicial - libre, asignacion)) {
        continue;
      }
      if (restricciones.semestreFijo[i] >= 0 && restricciones.semestreFijo[i] != s) {
        continue;
      }
      if (restricciones.semestreMaximo[i] < s) {
        continue;
      }
      candidatos.add(i);
    }

    List<List<Integer>> clases = construirClases(candidatos);
    List<Integer> seleccion = new ArrayList<>();
    boolean resultado = recorrerClases(clases, 0, 0, 0, seleccion, s);
    if (!resultado) {
      fallidos.add(estado);
    }
    return resultado;
  }

  private List<List<Integer>> construirClases(List<Integer> candidatos) {
    Map<String, List<Integer>> porClave = new LinkedHashMap<>();
    for (int i : candidatos) {
      porClave.computeIfAbsent(claveClase(i), clave -> new ArrayList<>()).add(i);
    }
    List<List<Integer>> clases = new ArrayList<>(porClave.values());
    for (List<Integer> clase : clases) {
      clase.sort(Integer::compareTo);
    }
    clases.sort(
        Comparator.comparingInt(
                (List<Integer> clase) ->
                    Seleccion.esRequerida(clase.get(0), modelo, restricciones) ? 0 : 1)
            .thenComparingInt(clase -> clase.stream().mapToInt(modelo::creditos).min().orElse(0))
            .thenComparingInt(clase -> clase.get(0)));
    return clases;
  }

  private String claveClase(int materia) {
    if (Seleccion.esRequerida(materia, modelo, restricciones)
        || modelo.gruposDeMateria(materia).length != 1) {
      return "U" + materia;
    }
    int grupo = modelo.gruposDeMateria(materia)[0];
    StringBuilder clave = new StringBuilder();
    clave.append("C").append(grupo).append('|').append(modelo.creditos(materia)).append('|');
    for (long[] clausula : modelo.prerrequisitos(materia)) {
      clave.append(Arrays.toString(clausula)).append(';');
    }
    clave.append('|');
    for (Habilitacion habilitacion : modelo.habilitaciones(materia)) {
      clave.append(habilitacion.medida()).append(':').append(habilitacion.umbral()).append(';');
    }
    clave.append('|');
    for (int i = 0; i < cantidadMaterias; i++) {
      int indiceClausula = 0;
      for (long[] clausula : modelo.prerrequisitos(i)) {
        if ((clausula[materia >>> 6] & (1L << (materia & 63))) != 0) {
          clave.append(i).append(':').append(indiceClausula).append(';');
        }
        indiceClausula++;
      }
      indiceClausula = 0;
      for (long[] clausula : modelo.correquisitos(i)) {
        if ((clausula[materia >>> 6] & (1L << (materia & 63))) != 0) {
          clave.append('c').append(i).append(':').append(indiceClausula).append(';');
        }
        indiceClausula++;
      }
    }
    return clave.toString();
  }

  private boolean recorrerClases(
      List<List<Integer>> clases,
      int indice,
      int creditos,
      int materias,
      List<Integer> seleccion,
      int s) {
    if ((++contadorSubconjuntos & 1023) == 0 && cancelacion.cancelado()) {
      metricas.cancelada = true;
      return false;
    }
    if (creditos > tope.limiteCreditos() || materias > tope.limiteMaterias()) {
      return false;
    }
    if (indice == clases.size()) {
      return probarSubconjunto(seleccion, creditos, materias, s);
    }
    List<Integer> clase = clases.get(indice);
    int maximo = clase.size();
    if (!Seleccion.esRequerida(clase.get(0), modelo, restricciones)) {
      maximo = Math.min(maximo, maximoUtil(clase));
    }
    while (maximo > 0
        && (creditos + creditosDe(clase, maximo) > tope.limiteCreditos()
            || materias + maximo > tope.limiteMaterias())) {
      maximo--;
    }
    for (int tomar = maximo; tomar >= 0; tomar--) {
      int agregados = 0;
      for (int i = 0; i < tomar; i++) {
        int materia = clase.get(i);
        if (!seleccion.contains(materia)) {
          seleccion.add(materia);
          agregados++;
        }
      }
      int creditosTomados = creditosDe(clase, tomar);
      if (recorrerClases(
          clases, indice + 1, creditos + creditosTomados, materias + agregados, seleccion, s)) {
        return true;
      }
      for (int i = 0; i < agregados; i++) {
        seleccion.remove(seleccion.size() - 1);
      }
    }
    return false;
  }

  private int maximoUtil(List<Integer> clase) {
    int materia = clase.get(0);
    int grupo = modelo.gruposDeMateria(materia)[0];
    int faltan = modelo.faltan(grupo, cursadas, asignacion, extraRequeridas);
    if (faltan <= 0) {
      return 0;
    }
    int creditos = modelo.creditos(materia);
    return Math.min(clase.size(), (faltan + creditos - 1) / creditos);
  }

  private int creditosDe(List<Integer> clase, int cantidad) {
    int total = 0;
    for (int i = 0; i < cantidad; i++) {
      total += modelo.creditos(clase.get(i));
    }
    return total;
  }

  private boolean probarSubconjunto(List<Integer> seleccion, int creditosX, int materiasX, int s) {
    for (int i = 0; i < cantidadMaterias; i++) {
      if ((restricciones.semestreFijo[i] == s || restricciones.semestreMaximo[i] == s)
          && !cursadas.contiene(i)
          && !seleccion.contains(i)) {
        return false;
      }
    }
    ConjuntoDeMaterias cursadasConSeleccion = cursadas.copia();
    for (int materia : seleccion) {
      cursadasConSeleccion.agregar(materia);
    }
    for (int materia : seleccion) {
      if (!modelo.clausulasCumplidas(modelo.correquisitos(materia), cursadasConSeleccion)) {
        return false;
      }
    }
    int[] asignacionesPrevias = new int[seleccion.size()];
    for (int i = 0; i < seleccion.size(); i++) {
      int materia = seleccion.get(i);
      asignacionesPrevias[i] = asignacion[materia];
      Seleccion.elegirAgrupacion(modelo, materia, cursadasConSeleccion, asignacion, restricciones);
    }

    int maximoBloque = Math.min(libre, tope.limiteCreditos() - creditosX);
    for (int b = maximoBloque; b >= 0; b--) {
      if ((++contadorSubconjuntos & 1023) == 0 && cancelacion.cancelado()) {
        metricas.cancelada = true;
        return false;
      }
      int creditosPorMateria = modelo.creditosPorMateriaLibre();
      if (libreInicial >= creditosPorMateria) {
        if (b > 0 && b < creditosPorMateria) {
          continue;
        }
      } else if (b > 0 && b != libreInicial) {
        continue;
      }
      if (materiasX + Math.ceilDiv(b, modelo.creditosPorMateriaLibre()) > tope.limiteMaterias()) {
        continue;
      }
      if (creditosX + b == 0) {
        continue;
      }
      if (restricciones.bloqueFijo != null
          && restricciones.bloqueFijo[s] != null
          && restricciones.bloqueFijo[s] != b) {
        continue;
      }
      boolean marcado = creditosX + b < modelo.minimoPorPeriodo();
      if (marcado && marcados + 1 > restricciones.maxSemestresMarcados) {
        continue;
      }

      final int librePrevio = libre;
      final int marcadosPrevios = marcados;
      final int optativasPrevias = creditosOptativas;
      for (int materia : seleccion) {
        cursadas.agregar(materia);
        semestre[materia] = s;
        if (!modelo.requeridas().contiene(materia)) {
          creditosOptativas += modelo.creditos(materia);
        }
      }
      libre -= b;
      bloque[s] = b;
      creditosPorSemestre[s] = creditosX + b;
      if (marcado) {
        marcados++;
      }
      final boolean resultado = dfs(s + 1);
      libre = librePrevio;
      marcados = marcadosPrevios;
      creditosOptativas = optativasPrevias;
      bloque[s] = 0;
      creditosPorSemestre[s] = 0;
      for (int materia : seleccion) {
        cursadas.quitar(materia);
        semestre[materia] = -1;
      }
      for (int i = 0; i < seleccion.size(); i++) {
        asignacion[seleccion.get(i)] = asignacionesPrevias[i];
      }
      if (resultado) {
        return true;
      }
    }
    for (int i = 0; i < seleccion.size(); i++) {
      asignacion[seleccion.get(i)] = asignacionesPrevias[i];
    }
    return false;
  }

  private boolean cerrarPlan() {
    if (libre != 0
        || !modelo.todoCubierto(cursadas, asignacion)
        || !cursadas.contieneTodas(restricciones.obligatoriasExtra.palabras())) {
      return false;
    }
    for (int i = 0; i < cantidadMaterias; i++) {
      if (restricciones.semestreFijo[i] >= 0 && !cursadas.contiene(i)) {
        return false;
      }
      if (restricciones.semestreMaximo[i] < Integer.MAX_VALUE
          && !cursadas.contiene(i)
          && restricciones.semestreMaximo[i] <= semestres
          && (modelo.requeridas().contiene(i) || restricciones.incluidas.contiene(i))) {
        return false;
      }
      if (cursadas.contiene(i) && semestre[i] > restricciones.semestreMaximo[i]) {
        return false;
      }
    }
    if (marcados > restricciones.maxSemestresMarcados
        || creditosOptativas > restricciones.maxCreditosOptativas) {
      return false;
    }
    if (!optativasMinimas()) {
      return false;
    }
    encontrado = true;
    if (capturar) {
      plan =
          new Plan(
              semestres,
              semestre.clone(),
              asignacion.clone(),
              Arrays.copyOf(bloque, semestres + 1),
              marcados,
              creditosOptativas,
              Arrays.copyOf(creditosPorSemestre, semestres + 1));
    }
    return true;
  }

  private boolean optativasMinimas() {
    int[] libreAntes = new int[semestres + 1];
    for (int s = 1; s <= semestres; s++) {
      libreAntes[s] = libreAntes[s - 1] + bloque[s - 1];
    }
    for (int m = 0; m < cantidadMaterias; m++) {
      if (semestre[m] <= 0 || modelo.requeridas().contiene(m)) {
        continue;
      }
      ConjuntoDeMaterias sinM = cursadas.copia();
      sinM.quitar(m);
      int[] asignacionSinM = asignacion.clone();
      asignacionSinM[m] = -1;
      boolean necesaria = !modelo.todoCubierto(sinM, asignacionSinM);
      if (!necesaria) {
        necesaria = rompeRestricciones(sinM, asignacionSinM, libreAntes);
      }
      if (!necesaria) {
        return false;
      }
    }
    return true;
  }

  private boolean rompeRestricciones(
      ConjuntoDeMaterias sinM, int[] asignacionSinM, int[] libreAntes) {
    for (int j = 0; j < cantidadMaterias; j++) {
      if (semestre[j] <= 0 || !sinM.contiene(j)) {
        continue;
      }
      ConjuntoDeMaterias antes = a0.copia();
      ConjuntoDeMaterias hasta = a0.copia();
      for (int p = 0; p < cantidadMaterias; p++) {
        if (p == j || !sinM.contiene(p) || semestre[p] <= 0) {
          continue;
        }
        if (semestre[p] < semestre[j]) {
          antes.agregar(p);
        }
        if (semestre[p] <= semestre[j]) {
          hasta.agregar(p);
        }
      }
      if (!modelo.clausulasCumplidas(modelo.prerrequisitos(j), antes)) {
        return true;
      }
      if (!modelo.habilitacionesCumplidas(
          j, antes, libreAntes[Math.min(semestre[j], semestres)], asignacionSinM)) {
        return true;
      }
      if (!modelo.clausulasCumplidas(modelo.correquisitos(j), hasta)) {
        return true;
      }
    }
    return false;
  }

  private static final class Estado {
    private final int semestre;
    private final int libre;
    private final int marcados;
    private final long[] palabras;
    private final int[] asignacion;

    private Estado(int semestre, int libre, int marcados, long[] palabras, int[] asignacion) {
      this.semestre = semestre;
      this.libre = libre;
      this.marcados = marcados;
      this.palabras = palabras;
      this.asignacion = asignacion;
    }

    @Override
    public boolean equals(Object objeto) {
      if (this == objeto) {
        return true;
      }
      if (!(objeto instanceof Estado otro)) {
        return false;
      }
      return semestre == otro.semestre
          && libre == otro.libre
          && marcados == otro.marcados
          && Arrays.equals(palabras, otro.palabras)
          && Arrays.equals(asignacion, otro.asignacion);
    }

    @Override
    public int hashCode() {
      int resultado = 31 * semestre + libre;
      resultado = 31 * resultado + marcados;
      resultado = 31 * resultado + Arrays.hashCode(palabras);
      return 31 * resultado + Arrays.hashCode(asignacion);
    }
  }
}
