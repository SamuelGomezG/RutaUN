package co.edu.unal.rutaun.spikes.motor.modelo;

import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Modelo compacto de la malla: índices en orden del consolidado, máscaras y CNF. */
public final class ModeloDeCalculo {

  public static final int PROGRAMA = -1;
  public static final int INFINITO = 1_000_000;

  /** Configuración específica de un escenario o variante. */
  public record Configuracion(
      List<String> fijas,
      List<ModelosJson.PrerrequisitoJson> prerrequisitosExtra,
      List<ModelosJson.CorrequisitoJson> correquisitosExtra) {

    public Configuracion {
      fijas = fijas == null ? List.of() : List.copyOf(fijas);
      prerrequisitosExtra =
          prerrequisitosExtra == null ? List.of() : List.copyOf(prerrequisitosExtra);
      correquisitosExtra = correquisitosExtra == null ? List.of() : List.copyOf(correquisitosExtra);
    }

    public static Configuracion vacia() {
      return new Configuracion(List.of(), List.of(), List.of());
    }
  }

  /** Regla de habilitación ya resuelta a una medida del modelo. */
  public record Habilitacion(int medida, int umbral) {}

  /** Agrupación o componente. */
  public static final class Grupo {
    private final int indice;
    private final String id;
    private final String nombre;
    private final int creditos;
    private final int padre;
    private final boolean componente;
    private final String tipoComponente;
    private final int componenteRaiz;
    private final List<Integer> hijos = new ArrayList<>();
    private final List<Integer> materiasDirectas = new ArrayList<>();
    private long[] mascaraDirecta;
    private long[] mascaraSubarbol;
    private long[] mascaraRequeridasDirectas;
    private long[] mascaraOptativasDirectas;
    private int creditosRequeridasDirectas;
    private int creditosHijas;

    private Grupo(
        int indice,
        String id,
        String nombre,
        int creditos,
        int padre,
        boolean componente,
        String tipoComponente,
        int componenteRaiz) {
      this.indice = indice;
      this.id = id;
      this.nombre = nombre;
      this.creditos = creditos;
      this.padre = padre;
      this.componente = componente;
      this.tipoComponente = tipoComponente;
      this.componenteRaiz = componenteRaiz;
    }

    public int indice() {
      return indice;
    }

    public String id() {
      return id;
    }

    public String nombre() {
      return nombre;
    }

    public int creditos() {
      return creditos;
    }

    public int padre() {
      return padre;
    }

    public boolean esComponente() {
      return componente;
    }

    public String tipoComponente() {
      return tipoComponente;
    }

    public int componenteRaiz() {
      return componenteRaiz;
    }

    public List<Integer> hijos() {
      return hijos;
    }

    public List<Integer> materiasDirectas() {
      return materiasDirectas;
    }

    public long[] mascaraDirecta() {
      return mascaraDirecta;
    }

    public long[] mascaraSubarbol() {
      return mascaraSubarbol;
    }

    public int creditosHijas() {
      return creditosHijas;
    }
  }

  private final ModelosJson.MallaJson malla;
  private final int n;
  private final String[] codigos;
  private final String[] nombres;
  private final int[] creditos;
  private final boolean[] obligatoria;
  private final Integer[] semestre;
  private final boolean[] mostrada;
  private final boolean[] cursable;
  private final List<int[]> gruposDeMateria;
  private final List<List<long[]>> prerrequisitos;
  private final List<List<long[]>> correquisitos;
  private final List<List<Habilitacion>> habilitaciones;
  private final List<Grupo> grupos;
  private final Map<String, Integer> porCodigo;
  private final int libreEleccion;
  private final int minimoPorPeriodo;
  private final int creditosDelPrograma;
  private final int componenteDisciplinar;
  private final ConjuntoDeMaterias requeridas;
  private final ConjuntoDeMaterias fijas;
  private final String motivoSinPlan;

  public ModeloDeCalculo(ModelosJson.MallaJson malla, Configuracion configuracion) {
    this.malla = malla;
    this.libreEleccion = malla.libreEleccion().creditos();
    this.minimoPorPeriodo = malla.minimoPorPeriodo();
    this.creditosDelPrograma = malla.creditosDelPrograma();

    List<Grupo> gruposTemporales = new ArrayList<>();
    List<MateriaDatos> materiasTemporales = new ArrayList<>();
    Map<String, MateriaDatos> porCodigoTemporal = new LinkedHashMap<>();
    List<String> externasNivelacion = new ArrayList<>();
    List<String> externasOtro = new ArrayList<>();
    for (ModelosJson.ExternaJson externa : malla.externas()) {
      if ("NIVELACION".equals(externa.tipo())) {
        externasNivelacion.add(externa.nombre());
      } else {
        externasOtro.add(externa.nombre());
      }
    }

    for (ModelosJson.ComponenteJson componente : malla.componentes()) {
      int indiceComponente = gruposTemporales.size();
      Grupo grupoComponente =
          new Grupo(
              indiceComponente,
              componente.tipo(),
              componente.tipo(),
              componente.creditos(),
              -1,
              true,
              componente.tipo(),
              indiceComponente);
      gruposTemporales.add(grupoComponente);
      for (ModelosJson.MateriaJson materia : componente.materias()) {
        agregarMateria(materiasTemporales, porCodigoTemporal, materia, indiceComponente);
      }
      for (ModelosJson.AgrupacionJson agrupacion : componente.agrupaciones()) {
        agregarAgrupacion(
            gruposTemporales,
            materiasTemporales,
            porCodigoTemporal,
            agrupacion,
            indiceComponente,
            indiceComponente);
      }
    }

    // Índices temporales de materias directas, para derivar el semestre.
    for (int i = 0; i < materiasTemporales.size(); i++) {
      for (int grupo : materiasTemporales.get(i).grupos) {
        gruposTemporales.get(grupo).materiasDirectas().add(i);
      }
    }

    // Orden del consolidado: semestre, mostrada antes que derivada, código.
    for (MateriaDatos materia : materiasTemporales) {
      if (materia.semestre != null) {
        continue;
      }
      Integer derivado = null;
      for (int grupo : materia.grupos) {
        for (int materiaDelGrupo : gruposTemporales.get(grupo).materiasDirectas()) {
          MateriaDatos otra = materiasTemporales.get(materiaDelGrupo);
          if (otra.semestre != null && (derivado == null || otra.semestre < derivado)) {
            derivado = otra.semestre;
          }
        }
      }
      materia.semestreDerivado = derivado;
    }
    materiasTemporales.sort(
        Comparator.comparing(
                (MateriaDatos m) -> m.semestreEfectivo(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(m -> !m.mostrada)
            .thenComparing(m -> m.codigo));

    n = materiasTemporales.size();
    codigos = new String[n];
    nombres = new String[n];
    creditos = new int[n];
    obligatoria = new boolean[n];
    semestre = new Integer[n];
    mostrada = new boolean[n];
    cursable = new boolean[n];
    gruposDeMateria = new ArrayList<>(n);
    porCodigo = new HashMap<>();
    Set<String> codigosFijas = new LinkedHashSet<>(configuracion.fijas());
    for (int i = 0; i < n; i++) {
      MateriaDatos materia = materiasTemporales.get(i);
      codigos[i] = materia.codigo;
      nombres[i] = materia.nombre;
      creditos[i] = materia.creditos;
      obligatoria[i] = materia.obligatoria;
      semestre[i] = materia.semestreEfectivo();
      mostrada[i] = materia.mostrada;
      cursable[i] = true;
      porCodigo.put(materia.codigo, i);
      gruposDeMateria.add(new int[0]);
    }

    // Reindexar grupos y sus materias directas.
    grupos = gruposTemporales;
    for (Grupo grupo : grupos) {
      grupo.materiasDirectas.clear();
    }
    for (int i = 0; i < n; i++) {
      MateriaDatos materia = materiasTemporales.get(i);
      int[] gruposMateria = materia.grupos.stream().mapToInt(Integer::intValue).toArray();
      gruposDeMateria.set(i, gruposMateria);
      for (int grupo : gruposMateria) {
        grupos.get(grupo).materiasDirectas.add(i);
      }
    }
    for (Grupo grupo : grupos) {
      grupo.materiasDirectas.sort(Integer::compareTo);
      if (grupo.padre >= 0) {
        grupos.get(grupo.padre).hijos.add(grupo.indice);
        grupos.get(grupo.padre).creditosHijas += grupo.creditos;
      }
    }
    componenteDisciplinar = buscarComponente("DISCIPLINAR");

    ConjuntoDeMaterias fijasTemporal = new ConjuntoDeMaterias(n);
    for (String codigo : codigosFijas) {
      Integer indice = porCodigo.get(codigo);
      if (indice == null) {
        throw new IllegalArgumentException("Optativa fijada desconocida: " + codigo);
      }
      fijasTemporal.agregar(indice);
    }
    this.fijas = fijasTemporal;

    // Asignación inicial de las fijas: primera agrupación (determinista).
    int[] asignacionInicial = new int[n];
    for (int i = 0; i < n; i++) {
      asignacionInicial[i] = gruposDeMateria.get(i).length == 1 ? gruposDeMateria.get(i)[0] : -1;
    }
    for (int i = 0; i < n; i++) {
      if (fijas.contiene(i)) {
        int[] gruposMateria = gruposDeMateria.get(i);
        if (gruposMateria.length > 0) {
          asignacionInicial[i] = gruposMateria[0];
        }
      }
    }

    // Requeridas = obligatorias + fijas.
    ConjuntoDeMaterias requeridasTemporal = new ConjuntoDeMaterias(n);
    for (int i = 0; i < n; i++) {
      if (obligatoria[i] || fijas.contiene(i)) {
        requeridasTemporal.agregar(i);
      }
    }
    requeridas = requeridasTemporal;

    // Máscaras de grupos.
    for (Grupo grupo : grupos) {
      grupo.mascaraDirecta = new long[palabras()];
      grupo.mascaraRequeridasDirectas = new long[palabras()];
      grupo.mascaraOptativasDirectas = new long[palabras()];
      for (int materia : grupo.materiasDirectas) {
        grupo.mascaraDirecta[materia >>> 6] |= 1L << (materia & 63);
      }
    }
    // Las fijas cuentan como requeridas de la agrupación asignada.
    for (int i = 0; i < n; i++) {
      if (!fijas.contiene(i) || asignacionInicial[i] < 0) {
        continue;
      }
      Grupo grupo = grupos.get(asignacionInicial[i]);
      grupo.mascaraRequeridasDirectas[i >>> 6] |= 1L << (i & 63);
      grupo.creditosRequeridasDirectas += creditos[i];
    }
    for (int i = 0; i < n; i++) {
      if (!obligatoria[i]) {
        continue;
      }
      for (int grupoIndice : gruposDeMateria.get(i)) {
        Grupo grupo = grupos.get(grupoIndice);
        grupo.mascaraRequeridasDirectas[i >>> 6] |= 1L << (i & 63);
        grupo.creditosRequeridasDirectas += creditos[i];
      }
    }
    for (Grupo grupo : grupos) {
      for (int materia : grupo.materiasDirectas) {
        if ((grupo.mascaraRequeridasDirectas[materia >>> 6] & (1L << (materia & 63))) == 0) {
          grupo.mascaraOptativasDirectas[materia >>> 6] |= 1L << (materia & 63);
        }
      }
    }
    for (int g = grupos.size() - 1; g >= 0; g--) {
      Grupo grupo = grupos.get(g);
      grupo.mascaraSubarbol = grupo.mascaraDirecta.clone();
      for (int hija : grupo.hijos) {
        long[] mascaraHija = grupos.get(hija).mascaraSubarbol;
        for (int j = 0; j < palabras(); j++) {
          grupo.mascaraSubarbol[j] |= mascaraHija[j];
        }
      }
    }

    // CNF de prerrequisitos y correquisitos.
    prerrequisitos = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      prerrequisitos.add(new ArrayList<>());
    }
    correquisitos = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      correquisitos.add(new ArrayList<>());
    }
    Set<String> nivelacion = new HashSet<>(externasNivelacion);
    Set<String> otroPrograma = new HashSet<>(externasOtro);
    List<ModelosJson.PrerrequisitoJson> todosPrerrequisitos =
        new ArrayList<>(malla.prerrequisitos());
    todosPrerrequisitos.addAll(configuracion.prerrequisitosExtra());
    for (ModelosJson.PrerrequisitoJson prerrequisito : todosPrerrequisitos) {
      Integer indice = porCodigo.get(prerrequisito.materia());
      if (indice == null) {
        throw new IllegalArgumentException(
            "Prerrequisito de materia desconocida: " + prerrequisito.materia());
      }
      Cnf cnf = Cnf.de(prerrequisito.expresion(), nivelacion, otroPrograma);
      if (cnf.falso) {
        cursable[indice] = false;
        continue;
      }
      prerrequisitos.get(indice).addAll(aMascaras(cnf, indice));
    }
    List<ModelosJson.CorrequisitoJson> todosCorrequisitos = new ArrayList<>(malla.correquisitos());
    todosCorrequisitos.addAll(configuracion.correquisitosExtra());
    for (ModelosJson.CorrequisitoJson correquisito : todosCorrequisitos) {
      Integer indice = porCodigo.get(correquisito.materia());
      if (indice == null) {
        throw new IllegalArgumentException(
            "Correquisito de materia desconocida: " + correquisito.materia());
      }
      Cnf cnf = Cnf.de(correquisito.expresion(), nivelacion, otroPrograma);
      if (cnf.falso) {
        cursable[indice] = false;
        continue;
      }
      correquisitos.get(indice).addAll(aMascaras(cnf, indice));
    }

    // Propagar no cursables: quitar sus literales y marcar cláusulas vacías.
    boolean cambio = true;
    while (cambio) {
      cambio = false;
      for (int i = 0; i < n; i++) {
        if (!cursable[i]) {
          continue;
        }
        List<long[]> clausulas = prerrequisitos.get(i);
        for (long[] clausula : clausulas) {
          boolean tieneCursable = false;
          for (int j = 0; j < n; j++) {
            if ((clausula[j >>> 6] & (1L << (j & 63))) != 0 && cursable[j]) {
              tieneCursable = true;
              break;
            }
          }
          if (!tieneCursable) {
            cursable[i] = false;
            cambio = true;
            break;
          }
        }
      }
    }
    for (int i = 0; i < n; i++) {
      if (!cursable[i]) {
        prerrequisitos.get(i).clear();
      } else {
        for (long[] clausula : prerrequisitos.get(i)) {
          for (int j = 0; j < n; j++) {
            if (!cursable[j]) {
              clausula[j >>> 6] &= ~(1L << (j & 63));
            }
          }
        }
      }
    }

    // Habilitaciones.
    habilitaciones = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      habilitaciones.add(new ArrayList<>());
    }
    for (ModelosJson.HabilitacionJson habilitacion : malla.habilitaciones()) {
      Integer indice = porCodigo.get(habilitacion.materia());
      if (indice == null) {
        throw new IllegalArgumentException(
            "Habilitación de materia desconocida: " + habilitacion.materia());
      }
      int medida;
      if ("PROGRAMA".equals(habilitacion.medida())) {
        medida = PROGRAMA;
      } else {
        medida = buscarComponente(habilitacion.componente());
        if (medida < 0) {
          throw new IllegalArgumentException(
              "Componente de habilitación desconocido: " + habilitacion.componente());
        }
      }
      habilitaciones.get(indice).add(new Habilitacion(medida, habilitacion.creditos()));
    }

    String motivo = null;
    for (int i = 0; i < n; i++) {
      if (obligatoria[i] && !cursable[i]) {
        motivo = "La obligatoria " + codigos[i] + " no es cursable desde el programa";
        break;
      }
    }
    motivoSinPlan = motivo;
  }

  public static int palabras() {
    return 2;
  }

  private static void agregarMateria(
      List<MateriaDatos> materias,
      Map<String, MateriaDatos> porCodigo,
      ModelosJson.MateriaJson materia,
      int grupo) {
    MateriaDatos datos = porCodigo.get(materia.codigo());
    if (datos == null) {
      datos = new MateriaDatos(materia);
      porCodigo.put(materia.codigo(), datos);
      materias.add(datos);
    } else {
      if (!datos.nombre.equals(materia.nombre()) || datos.creditos != materia.creditos()) {
        throw new IllegalArgumentException(
            "Materia repetida con datos distintos: " + materia.codigo());
      }
      datos.obligatoria |= materia.obligatoria();
    }
    if (!datos.grupos.contains(grupo)) {
      datos.grupos.add(grupo);
    }
  }

  private static void agregarAgrupacion(
      List<Grupo> grupos,
      List<MateriaDatos> materias,
      Map<String, MateriaDatos> porCodigo,
      ModelosJson.AgrupacionJson agrupacion,
      int padre,
      int componenteRaiz) {
    int indice = grupos.size();
    Grupo grupo =
        new Grupo(
            indice,
            agrupacion.id(),
            agrupacion.nombre(),
            agrupacion.creditos(),
            padre,
            false,
            null,
            componenteRaiz);
    grupos.add(grupo);
    for (ModelosJson.MateriaJson materia : agrupacion.materias()) {
      agregarMateria(materias, porCodigo, materia, indice);
    }
    for (ModelosJson.AgrupacionJson subagrupacion : agrupacion.subagrupaciones()) {
      agregarAgrupacion(grupos, materias, porCodigo, subagrupacion, indice, componenteRaiz);
    }
  }

  private List<long[]> aMascaras(Cnf cnf, int materia) {
    List<long[]> resultado = new ArrayList<>();
    for (List<String> clausula : cnf.clausulas) {
      long[] mascara = new long[palabras()];
      for (String codigo : clausula) {
        Integer indice = porCodigo.get(codigo);
        if (indice == null) {
          throw new IllegalArgumentException(
              "Código de restricción desconocido en " + codigos[materia] + ": " + codigo);
        }
        mascara[indice >>> 6] |= 1L << (indice & 63);
      }
      resultado.add(mascara);
    }
    return resultado;
  }

  private int buscarComponente(String tipo) {
    if (tipo == null) {
      return -1;
    }
    for (Grupo grupo : grupos) {
      if (grupo.esComponente()
          && grupo.tipoComponente() != null
          && grupo.tipoComponente().equalsIgnoreCase(tipo)) {
        return grupo.indice();
      }
    }
    return -1;
  }

  public ModelosJson.MallaJson malla() {
    return malla;
  }

  public int cantidadMaterias() {
    return n;
  }

  public String codigo(int materia) {
    return codigos[materia];
  }

  public String nombre(int materia) {
    return nombres[materia];
  }

  public int creditos(int materia) {
    return creditos[materia];
  }

  public boolean obligatoria(int materia) {
    return obligatoria[materia];
  }

  public Integer semestre(int materia) {
    return semestre[materia];
  }

  public boolean mostrada(int materia) {
    return mostrada[materia];
  }

  public boolean cursable(int materia) {
    return cursable[materia];
  }

  public int libreEleccion() {
    return libreEleccion;
  }

  public int minimoPorPeriodo() {
    return minimoPorPeriodo;
  }

  public int creditosDelPrograma() {
    return creditosDelPrograma;
  }

  public int componenteDisciplinar() {
    return componenteDisciplinar;
  }

  public ConjuntoDeMaterias requeridas() {
    return requeridas;
  }

  public ConjuntoDeMaterias fijas() {
    return fijas;
  }

  public String motivoSinPlan() {
    return motivoSinPlan;
  }

  public Integer indiceDe(String codigo) {
    return porCodigo.get(codigo);
  }

  public int[] gruposDeMateria(int materia) {
    return gruposDeMateria.get(materia);
  }

  public List<long[]> prerrequisitos(int materia) {
    return prerrequisitos.get(materia);
  }

  public List<long[]> correquisitos(int materia) {
    return correquisitos.get(materia);
  }

  public List<Habilitacion> habilitaciones(int materia) {
    return habilitaciones.get(materia);
  }

  public List<Grupo> grupos() {
    return grupos;
  }

  public Grupo grupo(int indice) {
    return grupos.get(indice);
  }

  public int cantidadGrupos() {
    return grupos.size();
  }

  public int[] asignacionInicial() {
    int[] asignacion = new int[n];
    for (int i = 0; i < n; i++) {
      asignacion[i] = gruposDeMateria.get(i).length == 1 ? gruposDeMateria.get(i)[0] : -1;
    }
    for (int i = 0; i < n; i++) {
      if (fijas.contiene(i) && gruposDeMateria.get(i).length > 0) {
        asignacion[i] = gruposDeMateria.get(i)[0];
      }
    }
    return asignacion;
  }

  public int faltan(int grupoIndice, ConjuntoDeMaterias D, int[] asignacion) {
    return faltan(grupoIndice, D, asignacion, null);
  }

  public int faltan(
      int grupoIndice, ConjuntoDeMaterias D, int[] asignacion, ConjuntoDeMaterias extraRequeridas) {
    Grupo grupo = grupos.get(grupoIndice);
    int cubierto = grupo.creditosRequeridasDirectas + grupo.creditosHijas;
    long[] mascaraOptativas = grupo.mascaraOptativasDirectas;
    long[] extra = extraRequeridas == null ? null : extraRequeridas.palabras();
    for (int i = 0; i < n; i++) {
      if ((mascaraOptativas[i >>> 6] & (1L << (i & 63))) == 0 || asignacion[i] != grupoIndice) {
        continue;
      }
      boolean cuenta = D.contiene(i);
      if (!cuenta && extra != null) {
        cuenta = (extra[i >>> 6] & (1L << (i & 63))) != 0;
      }
      if (cuenta) {
        cubierto += creditos[i];
      }
    }
    return Math.max(0, grupo.creditos - cubierto);
  }

  public boolean cubierta(int grupoIndice, ConjuntoDeMaterias D, int[] asignacion) {
    Grupo grupo = grupos.get(grupoIndice);
    if (!D.contieneTodas(grupo.mascaraRequeridasDirectas)) {
      return false;
    }
    if (faltan(grupoIndice, D, asignacion) > 0) {
      return false;
    }
    for (int hija : grupo.hijos) {
      if (!cubierta(hija, D, asignacion)) {
        return false;
      }
    }
    return true;
  }

  public boolean todoCubierto(ConjuntoDeMaterias D, int[] asignacion) {
    for (Grupo grupo : grupos) {
      if (grupo.padre() < 0 && !cubierta(grupo.indice(), D, asignacion)) {
        return false;
      }
    }
    return true;
  }

  /** Créditos de una agrupación, contando hijos y topando en lo exigido. */
  public int creditosGrupo(int grupoIndice, ConjuntoDeMaterias D, int[] asignacion) {
    Grupo grupo = grupos.get(grupoIndice);
    int bruto = 0;
    for (int materia : grupo.materiasDirectas) {
      if (D.contiene(materia) && asignacion[materia] == grupoIndice) {
        bruto += creditos[materia];
      }
    }
    for (int hija : grupo.hijos) {
      bruto += creditosGrupo(hija, D, asignacion);
    }
    return Math.min(grupo.creditos, bruto);
  }

  public int creditosMedida(int medida, ConjuntoDeMaterias D, int libreColocado, int[] asignacion) {
    if (medida == PROGRAMA) {
      int total = 0;
      for (Grupo grupo : grupos) {
        if (grupo.padre() < 0) {
          total += creditosGrupo(grupo.indice(), D, asignacion);
        }
      }
      return total + Math.min(libreColocado, libreEleccion);
    }
    return creditosGrupo(medida, D, asignacion);
  }

  public boolean habilitacionesCumplidas(
      int materia, ConjuntoDeMaterias D, int libreColocado, int[] asignacion) {
    for (Habilitacion habilitacion : habilitaciones.get(materia)) {
      if (creditosMedida(habilitacion.medida(), D, libreColocado, asignacion)
          < habilitacion.umbral()) {
        return false;
      }
    }
    return true;
  }

  public boolean clausulasCumplidas(List<long[]> clausulas, ConjuntoDeMaterias D) {
    for (long[] clausula : clausulas) {
      if (!D.contieneAlguna(clausula)) {
        return false;
      }
    }
    return true;
  }

  public int minimoCreditosOptativos(int grupoIndice, ConjuntoDeMaterias D, int[] asignacion) {
    return minimoCreditosOptativos(grupoIndice, D, asignacion, null);
  }

  public int minimoCreditosOptativos(
      int grupoIndice, ConjuntoDeMaterias D, int[] asignacion, ConjuntoDeMaterias extraRequeridas) {
    int necesidad = faltan(grupoIndice, D, asignacion, extraRequeridas);
    if (necesidad <= 0) {
      return 0;
    }
    Grupo grupo = grupos.get(grupoIndice);
    List<Integer> opciones = new ArrayList<>();
    int maximo = 0;
    long[] extra = extraRequeridas == null ? null : extraRequeridas.palabras();
    for (int materia : grupo.materiasDirectas) {
      if (D.contiene(materia) || !cursable[materia]) {
        continue;
      }
      if (extra != null && (extra[materia >>> 6] & (1L << (materia & 63))) != 0) {
        continue;
      }
      if ((grupo.mascaraOptativasDirectas[materia >>> 6] & (1L << (materia & 63))) == 0) {
        continue;
      }
      opciones.add(creditos[materia]);
      maximo = Math.max(maximo, creditos[materia]);
    }
    if (opciones.isEmpty()) {
      return INFINITO;
    }
    boolean[] alcanzable = new boolean[necesidad + maximo + 1];
    alcanzable[0] = true;
    for (int credito : opciones) {
      for (int suma = alcanzable.length - 1 - credito; suma >= 0; suma--) {
        if (alcanzable[suma]) {
          alcanzable[suma + credito] = true;
        }
      }
    }
    for (int suma = necesidad; suma < alcanzable.length; suma++) {
      if (alcanzable[suma]) {
        return suma;
      }
    }
    return INFINITO;
  }

  public int minimoMateriasOptativas(int grupoIndice, ConjuntoDeMaterias D, int[] asignacion) {
    return minimoMateriasOptativas(grupoIndice, D, asignacion, null);
  }

  public int minimoMateriasOptativas(
      int grupoIndice, ConjuntoDeMaterias D, int[] asignacion, ConjuntoDeMaterias extraRequeridas) {
    int necesidad = faltan(grupoIndice, D, asignacion, extraRequeridas);
    if (necesidad <= 0) {
      return 0;
    }
    Grupo grupo = grupos.get(grupoIndice);
    List<Integer> opciones = new ArrayList<>();
    int maximo = 0;
    long[] extra = extraRequeridas == null ? null : extraRequeridas.palabras();
    for (int materia : grupo.materiasDirectas) {
      if (D.contiene(materia) || !cursable[materia]) {
        continue;
      }
      if (extra != null && (extra[materia >>> 6] & (1L << (materia & 63))) != 0) {
        continue;
      }
      if ((grupo.mascaraOptativasDirectas[materia >>> 6] & (1L << (materia & 63))) == 0) {
        continue;
      }
      opciones.add(creditos[materia]);
      maximo = Math.max(maximo, creditos[materia]);
    }
    if (opciones.isEmpty()) {
      return INFINITO;
    }
    int limite = necesidad + maximo;
    int[] minMaterias = new int[limite + 1];
    java.util.Arrays.fill(minMaterias, INFINITO);
    minMaterias[0] = 0;
    for (int credito : opciones) {
      for (int suma = limite - credito; suma >= 0; suma--) {
        if (minMaterias[suma] != INFINITO) {
          minMaterias[suma + credito] =
              Math.min(minMaterias[suma + credito], minMaterias[suma] + 1);
        }
      }
    }
    int mejor = INFINITO;
    for (int suma = necesidad; suma <= limite; suma++) {
      mejor = Math.min(mejor, minMaterias[suma]);
    }
    return mejor;
  }

  /** Profundidad de la cadena de prerrequisitos de una materia desde D. */
  public int profundidad(int materia, ConjuntoDeMaterias D, int[] memo) {
    if (D.contiene(materia)) {
      return 0;
    }
    if (memo[materia] >= 0) {
      return memo[materia];
    }
    if (!cursable[materia]) {
      memo[materia] = INFINITO;
      return INFINITO;
    }
    List<long[]> clausulas = prerrequisitos.get(materia);
    if (clausulas.isEmpty()) {
      memo[materia] = 1;
      return 1;
    }
    int maximo = 0;
    for (long[] clausula : clausulas) {
      if (D.contieneAlguna(clausula)) {
        continue;
      }
      int minimo = INFINITO;
      for (int i = 0; i < n; i++) {
        if ((clausula[i >>> 6] & (1L << (i & 63))) != 0) {
          if (!D.contiene(i)) {
            minimo = Math.min(minimo, profundidad(i, D, memo));
          }
        }
      }
      if (minimo == INFINITO) {
        memo[materia] = INFINITO;
        return INFINITO;
      }
      maximo = Math.max(maximo, minimo);
    }
    memo[materia] = 1 + maximo;
    return memo[materia];
  }

  private static final class MateriaDatos {
    private final String codigo;
    private final String nombre;
    private final int creditos;
    private boolean obligatoria;
    private final Integer semestre;
    private final boolean mostrada;
    private Integer semestreDerivado;
    private final List<Integer> grupos = new ArrayList<>();

    private MateriaDatos(ModelosJson.MateriaJson materia) {
      this.codigo = materia.codigo();
      this.nombre = materia.nombre();
      this.creditos = materia.creditos();
      this.obligatoria = materia.obligatoria();
      this.semestre = materia.semestre();
      this.mostrada = materia.semestre() != null;
    }

    private Integer semestreEfectivo() {
      return semestre != null ? semestre : semestreDerivado;
    }
  }

  private static final class Cnf {
    private final boolean falso;
    private final boolean verdadero;
    private final List<List<String>> clausulas;

    private Cnf(boolean falso, boolean verdadero, List<List<String>> clausulas) {
      this.falso = falso;
      this.verdadero = verdadero;
      this.clausulas = clausulas;
    }

    private static Cnf falso() {
      return new Cnf(true, false, List.of());
    }

    private static Cnf verdadero() {
      return new Cnf(false, true, List.of());
    }

    private static Cnf clausula(String codigo) {
      List<String> clausula = new ArrayList<>();
      clausula.add(codigo);
      List<List<String>> clausulas = new ArrayList<>();
      clausulas.add(clausula);
      return new Cnf(false, false, clausulas);
    }

    private static Cnf de(
        ModelosJson.ExpresionJson expresion, Set<String> nivelacion, Set<String> otroPrograma) {
      if (expresion.codigo() != null) {
        return clausula(expresion.codigo());
      }
      if (expresion.externa() != null) {
        if (nivelacion.contains(expresion.externa())) {
          return verdadero();
        }
        if (otroPrograma.contains(expresion.externa())) {
          return falso();
        }
        throw new IllegalArgumentException("Externa desconocida: " + expresion.externa());
      }
      if (expresion.todas() != null) {
        List<Cnf> hijos = new ArrayList<>();
        for (ModelosJson.ExpresionJson hija : expresion.todas()) {
          hijos.add(de(hija, nivelacion, otroPrograma));
        }
        for (Cnf hijo : hijos) {
          if (hijo.falso) {
            return falso();
          }
        }
        List<List<String>> clausulas = new ArrayList<>();
        for (Cnf hijo : hijos) {
          if (!hijo.verdadero) {
            clausulas.addAll(hijo.clausulas);
          }
        }
        if (clausulas.isEmpty()) {
          return verdadero();
        }
        return new Cnf(false, false, clausulas);
      }
      if (expresion.alguna() != null) {
        List<Cnf> hijos = new ArrayList<>();
        for (ModelosJson.ExpresionJson hija : expresion.alguna()) {
          hijos.add(de(hija, nivelacion, otroPrograma));
        }
        for (Cnf hijo : hijos) {
          if (hijo.verdadero) {
            return verdadero();
          }
        }
        List<Cnf> activos = new ArrayList<>();
        for (Cnf hijo : hijos) {
          if (!hijo.falso) {
            activos.add(hijo);
          }
        }
        if (activos.isEmpty()) {
          return falso();
        }
        List<List<String>> resultado = new ArrayList<>();
        resultado.add(new ArrayList<>());
        for (Cnf hijo : activos) {
          List<List<String>> siguiente = new ArrayList<>();
          for (List<String> parcial : resultado) {
            for (List<String> clausulaHija : hijo.clausulas) {
              List<String> union = new ArrayList<>(parcial);
              for (String literal : clausulaHija) {
                if (!union.contains(literal)) {
                  union.add(literal);
                }
              }
              siguiente.add(union);
            }
          }
          resultado = siguiente;
        }
        return new Cnf(false, false, resultado);
      }
      throw new IllegalArgumentException("Expresión vacía");
    }
  }
}
