package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Componente;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.Correquisito;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExigenciaCreditos;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.MateriaEnMalla;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Obligatoriedad;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.TipoCargaPorPeriodo;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import co.edu.unal.rutaun.normativa.persistencia.DatosDeReglaJson;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Convierte entre los datos de una regla y la forma en que se guardan en la columna JSON. Recorre
 * las once categorías con un {@code switch} exhaustivo, de modo que el compilador avisa si se
 * agrega una nueva.
 *
 * <p>Al leer, pasa por las mismas validaciones que el dominio: los constructores rechazan un dato
 * inválido y los números obligatorios ausentes no se leen como cero. Un esquema distinto del actual
 * se rechaza.
 */
public final class DatosDeReglaJsonMapper {

  private DatosDeReglaJsonMapper() {}

  /**
   * Convierte los datos del dominio en su forma guardada.
   *
   * @param datos datos de la regla
   * @return la forma guardada, con el esquema actual
   */
  public static DatosDeReglaJson aJson(DatosDeRegla datos) {
    ArgumentChecks.obligatorio(datos, "datos");
    return switch (datos) {
      case DatosDeRegla.DeMateria deMateria ->
          new DatosDeReglaJson.MateriaJson(
              DatosDeReglaJson.ESQUEMA,
              materiaAJson(deMateria.materia()),
              nodoAJson(deMateria.contenedor()));
      case DatosDeRegla.DePrerrequisito dePrerrequisito ->
          new DatosDeReglaJson.PrerrequisitoJson(
              DatosDeReglaJson.ESQUEMA,
              dePrerrequisito.prerrequisito().materia().valor(),
              expresionAJson(dePrerrequisito.prerrequisito().expresion()));
      case DatosDeRegla.DeCorrequisito deCorrequisito ->
          new DatosDeReglaJson.CorrequisitoJson(
              DatosDeReglaJson.ESQUEMA,
              deCorrequisito.correquisito().materia().valor(),
              expresionAJson(deCorrequisito.correquisito().expresion()));
      case DatosDeRegla.DeHabilitacion deHabilitacion ->
          new DatosDeReglaJson.HabilitacionJson(
              DatosDeReglaJson.ESQUEMA,
              deHabilitacion.regla().materia().valor(),
              deHabilitacion.regla().medida().componente().map(TipoComponente::name).orElse(null),
              deHabilitacion.regla().umbral().valor());
      case DatosDeRegla.DeAgrupacion deAgrupacion ->
          new DatosDeReglaJson.AgrupacionJson(
              DatosDeReglaJson.ESQUEMA,
              agrupacionAJson(deAgrupacion.agrupacion()),
              nodoAJson(deAgrupacion.contenedor()));
      case DatosDeRegla.DeComponente deComponente ->
          new DatosDeReglaJson.ComponenteJson(
              DatosDeReglaJson.ESQUEMA, nodoAJson(deComponente.componente()));
      case DatosDeRegla.DePrograma dePrograma ->
          new DatosDeReglaJson.ProgramaJson(
              DatosDeReglaJson.ESQUEMA, dePrograma.creditosExigidos().valor());
      case DatosDeRegla.DeEquivalencia deEquivalencia ->
          new DatosDeReglaJson.EquivalenciaJson(
              DatosDeReglaJson.ESQUEMA,
              deEquivalencia.origen().stream().map(CodigoMateria::valor).sorted().toList(),
              deEquivalencia.destino().valor());
      case DatosDeRegla.DeCargaPorPeriodo deCarga ->
          new DatosDeReglaJson.CargaPorPeriodoJson(
              DatosDeReglaJson.ESQUEMA, deCarga.creditos().valor(), deCarga.tipo().name());
      case DatosDeRegla.DeRequisitoDeGrado ignored ->
          new DatosDeReglaJson.RequisitoDeGradoJson(DatosDeReglaJson.ESQUEMA);
      case DatosDeRegla.DeOtraCategoria ignored ->
          new DatosDeReglaJson.OtraJson(DatosDeReglaJson.ESQUEMA);
    };
  }

  /**
   * Convierte la forma guardada en datos del dominio, pasando por sus validaciones.
   *
   * @param json la forma guardada
   * @return los datos del dominio
   * @throws IllegalArgumentException si el esquema es desconocido o falta un dato obligatorio
   */
  public static DatosDeRegla aDominio(DatosDeReglaJson json) {
    ArgumentChecks.obligatorio(json, "json");
    exigirEsquema(json.esquema());
    return switch (json) {
      case DatosDeReglaJson.MateriaJson materiaJson ->
          new DatosDeRegla.DeMateria(
              materiaADominio(materiaJson.materia()), nodoADominio(materiaJson.contenedor()));
      case DatosDeReglaJson.PrerrequisitoJson prerrequisitoJson ->
          new DatosDeRegla.DePrerrequisito(
              new Prerrequisito(
                  codigoObligatorio(prerrequisitoJson.materia(), "materia"),
                  expresionADominio(prerrequisitoJson.expresion())));
      case DatosDeReglaJson.CorrequisitoJson correquisitoJson ->
          new DatosDeRegla.DeCorrequisito(
              new Correquisito(
                  codigoObligatorio(correquisitoJson.materia(), "materia"),
                  expresionADominio(correquisitoJson.expresion())));
      case DatosDeReglaJson.HabilitacionJson habilitacionJson ->
          new DatosDeRegla.DeHabilitacion(
              new ReglaDeHabilitacion(
                  codigoObligatorio(habilitacionJson.materia(), "materia"),
                  habilitacionJson.componente() == null
                      ? MedidaDeCreditos.delPrograma()
                      : MedidaDeCreditos.deComponente(
                          TipoComponente.valueOf(
                              textoObligatorio(habilitacionJson.componente(), "componente"))),
                  creditosObligatorios(habilitacionJson.umbral(), "umbral")));
      case DatosDeReglaJson.AgrupacionJson agrupacionJson ->
          new DatosDeRegla.DeAgrupacion(
              agrupacionADominio(agrupacionJson.agrupacion()),
              nodoADominio(agrupacionJson.contenedor()));
      case DatosDeReglaJson.ComponenteJson componenteJson ->
          new DatosDeRegla.DeComponente(nodoADominio(componenteJson.componente()));
      case DatosDeReglaJson.ProgramaJson programaJson ->
          new DatosDeRegla.DePrograma(
              creditosObligatorios(programaJson.creditosExigidos(), "creditosExigidos"));
      case DatosDeReglaJson.EquivalenciaJson equivalenciaJson ->
          new DatosDeRegla.DeEquivalencia(
              codigosObligatorios(equivalenciaJson.origen(), "origen"),
              codigoObligatorio(equivalenciaJson.destino(), "destino"));
      case DatosDeReglaJson.CargaPorPeriodoJson cargaJson ->
          new DatosDeRegla.DeCargaPorPeriodo(
              creditosObligatorios(cargaJson.creditos(), "creditos"),
              TipoCargaPorPeriodo.valueOf(textoObligatorio(cargaJson.tipo(), "tipo")));
      case DatosDeReglaJson.RequisitoDeGradoJson ignored -> new DatosDeRegla.DeRequisitoDeGrado();
      case DatosDeReglaJson.OtraJson ignored -> new DatosDeRegla.DeOtraCategoria();
    };
  }

  private static void exigirEsquema(int esquema) {
    if (esquema != DatosDeReglaJson.ESQUEMA) {
      throw new IllegalArgumentException("esquema desconocido: " + esquema);
    }
  }

  private static DatosDeReglaJson.NodoJson nodoAJson(NodoDeMalla nodo) {
    return switch (nodo) {
      case Componente componente ->
          new DatosDeReglaJson.NodoJson.ComponenteNodo(
              componente.nombre(),
              componente.tipo().name(),
              exigenciaAJson(componente.exigencia()),
              materiasAJson(componente.materias()),
              agrupacionesAJson(componente.agrupaciones()));
      case ComponenteDeLibreEleccion libre ->
          new DatosDeReglaJson.NodoJson.LibreEleccionNodo(
              libre.nombre(),
              exigenciaAJson(libre.exigencia()),
              agrupacionesAJson(libre.agrupaciones()));
      case Agrupacion agrupacion -> agrupacionAJson(agrupacion);
    };
  }

  private static DatosDeReglaJson.NodoJson.AgrupacionNodo agrupacionAJson(Agrupacion agrupacion) {
    return new DatosDeReglaJson.NodoJson.AgrupacionNodo(
        agrupacion.id().valor(),
        agrupacion.nombre(),
        exigenciaAJson(agrupacion.exigencia()),
        materiasAJson(agrupacion.materias()),
        agrupacionesAJson(agrupacion.subagrupaciones()));
  }

  private static DatosDeReglaJson.ExigenciaJson exigenciaAJson(ExigenciaCreditos exigencia) {
    Integer obligatorios = null;
    Integer optativos = null;
    if (exigencia.desglose().isPresent()) {
      ExigenciaCreditos.Desglose desglose = exigencia.desglose().get();
      obligatorios = desglose.obligatorios().valor();
      optativos = desglose.optativos().valor();
    }
    Map<Integer, Integer> sugeridos = new LinkedHashMap<>();
    exigencia
        .sugeridosPorSemestre()
        .forEach((semestre, creditos) -> sugeridos.put(semestre, creditos.valor()));
    return new DatosDeReglaJson.ExigenciaJson(
        exigencia.exigidos().valor(), obligatorios, optativos, sugeridos);
  }

  private static List<DatosDeReglaJson.MateriaEnMallaJson> materiasAJson(
      List<MateriaEnMalla> materias) {
    return materias.stream().map(DatosDeReglaJsonMapper::materiaAJson).toList();
  }

  private static DatosDeReglaJson.MateriaEnMallaJson materiaAJson(MateriaEnMalla materia) {
    return new DatosDeReglaJson.MateriaEnMallaJson(
        materia.codigo().valor(),
        materia.nombre(),
        materia.creditos().valor(),
        materia.obligatoriedad().name(),
        materia.semestreSugerido().orElse(null),
        materia.cursableDesdeElPrograma());
  }

  private static List<DatosDeReglaJson.NodoJson.AgrupacionNodo> agrupacionesAJson(
      List<Agrupacion> agrupaciones) {
    return agrupaciones.stream().map(DatosDeReglaJsonMapper::agrupacionAJson).toList();
  }

  private static DatosDeReglaJson.ExpresionJson expresionAJson(ExpresionDeMaterias expresion) {
    return switch (expresion) {
      case ExpresionDeMaterias.TerminoMateria termino ->
          new DatosDeReglaJson.ExpresionJson.Termino(termino.codigo().valor());
      case ExpresionDeMaterias.MateriaExterna externa ->
          new DatosDeReglaJson.ExpresionJson.Externa(
              externa.nombre(),
              externa.codigo().map(CodigoMateria::valor).orElse(null),
              externa.tipo().name());
      case ExpresionDeMaterias.TodasLas todas ->
          new DatosDeReglaJson.ExpresionJson.Todas(
              todas.terminos().stream().map(DatosDeReglaJsonMapper::expresionAJson).toList());
      case ExpresionDeMaterias.AlgunaDe alguna ->
          new DatosDeReglaJson.ExpresionJson.Alguna(
              alguna.terminos().stream().map(DatosDeReglaJsonMapper::expresionAJson).toList());
    };
  }

  private static NodoDeMalla nodoADominio(DatosDeReglaJson.NodoJson nodo) {
    ArgumentChecks.obligatorio(nodo, "nodo");
    return switch (nodo) {
      case DatosDeReglaJson.NodoJson.ComponenteNodo componente ->
          new Componente(
              textoObligatorio(componente.nombre(), "nombre"),
              TipoComponente.valueOf(textoObligatorio(componente.tipo(), "tipo")),
              exigenciaADominio(componente.exigencia()),
              materiasADominio(componente.materias()),
              agrupacionesADominio(componente.agrupaciones()));
      case DatosDeReglaJson.NodoJson.LibreEleccionNodo libre ->
          new ComponenteDeLibreEleccion(
              textoObligatorio(libre.nombre(), "nombre"),
              exigenciaADominio(libre.exigencia()),
              agrupacionesADominio(libre.agrupaciones()));
      case DatosDeReglaJson.NodoJson.AgrupacionNodo agrupacion -> agrupacionADominio(agrupacion);
    };
  }

  private static Agrupacion agrupacionADominio(DatosDeReglaJson.NodoJson.AgrupacionNodo json) {
    ArgumentChecks.obligatorio(json, "agrupacion");
    return new Agrupacion(
        new AgrupacionId(ArgumentChecks.obligatorio(json.id(), "id")),
        textoObligatorio(json.nombre(), "nombre"),
        exigenciaADominio(json.exigencia()),
        materiasADominio(json.materias()),
        agrupacionesADominio(json.subagrupaciones()));
  }

  private static ExigenciaCreditos exigenciaADominio(DatosDeReglaJson.ExigenciaJson json) {
    ArgumentChecks.obligatorio(json, "exigencia");
    Optional<ExigenciaCreditos.Desglose> desglose;
    if (json.obligatorios() == null && json.optativos() == null) {
      desglose = Optional.empty();
    } else if (json.obligatorios() != null && json.optativos() != null) {
      desglose =
          Optional.of(
              new ExigenciaCreditos.Desglose(
                  creditosObligatorios(json.obligatorios(), "obligatorios"),
                  creditosObligatorios(json.optativos(), "optativos")));
    } else {
      throw new IllegalArgumentException(
          "el desglose necesita obligatorios y optativos, y el JSON trae solo uno");
    }
    SortedMap<Integer, Creditos> sugeridos = new TreeMap<>();
    ArgumentChecks.obligatorio(json.sugeridosPorSemestre(), "sugeridosPorSemestre")
        .forEach(
            (semestre, creditos) ->
                sugeridos.put(
                    enteroObligatorio(semestre, "semestre"),
                    creditosObligatorios(creditos, "creditos sugeridos")));
    return new ExigenciaCreditos(
        creditosObligatorios(json.exigidos(), "exigidos"), desglose, sugeridos);
  }

  private static List<MateriaEnMalla> materiasADominio(
      List<DatosDeReglaJson.MateriaEnMallaJson> materias) {
    return ArgumentChecks.copiaSinNulos(materias, "materias").stream()
        .map(DatosDeReglaJsonMapper::materiaADominio)
        .toList();
  }

  private static MateriaEnMalla materiaADominio(DatosDeReglaJson.MateriaEnMallaJson json) {
    ArgumentChecks.obligatorio(json, "materia");
    return new MateriaEnMalla(
        codigoObligatorio(json.codigo(), "codigo"),
        textoObligatorio(json.nombre(), "nombre"),
        creditosObligatorios(json.creditos(), "creditos"),
        Obligatoriedad.valueOf(textoObligatorio(json.obligatoriedad(), "obligatoriedad")),
        Optional.ofNullable(json.semestreSugerido()),
        booleanoObligatorio(json.cursableDesdeElPrograma(), "cursableDesdeElPrograma"));
  }

  private static List<Agrupacion> agrupacionesADominio(
      List<DatosDeReglaJson.NodoJson.AgrupacionNodo> agrupaciones) {
    return ArgumentChecks.copiaSinNulos(agrupaciones, "agrupaciones").stream()
        .map(DatosDeReglaJsonMapper::agrupacionADominio)
        .toList();
  }

  private static ExpresionDeMaterias expresionADominio(DatosDeReglaJson.ExpresionJson json) {
    ArgumentChecks.obligatorio(json, "expresion");
    return switch (json) {
      case DatosDeReglaJson.ExpresionJson.Termino termino ->
          new ExpresionDeMaterias.TerminoMateria(codigoObligatorio(termino.codigo(), "codigo"));
      case DatosDeReglaJson.ExpresionJson.Externa externa ->
          new ExpresionDeMaterias.MateriaExterna(
              textoObligatorio(externa.nombre(), "nombre"),
              Optional.ofNullable(externa.codigo())
                  .map(codigo -> codigoObligatorio(codigo, "codigo")),
              TipoMateriaExterna.valueOf(textoObligatorio(externa.tipo(), "tipo")));
      case DatosDeReglaJson.ExpresionJson.Todas todas ->
          new ExpresionDeMaterias.TodasLas(terminosADominio(todas.terminos()));
      case DatosDeReglaJson.ExpresionJson.Alguna alguna ->
          new ExpresionDeMaterias.AlgunaDe(terminosADominio(alguna.terminos()));
    };
  }

  private static Set<ExpresionDeMaterias> terminosADominio(
      List<DatosDeReglaJson.ExpresionJson> terminos) {
    Set<ExpresionDeMaterias> resultado = new LinkedHashSet<>();
    ArgumentChecks.copiaSinNulos(terminos, "terminos")
        .forEach(termino -> resultado.add(expresionADominio(termino)));
    return resultado;
  }

  private static Set<CodigoMateria> codigosObligatorios(List<String> codigos, String nombre) {
    Set<CodigoMateria> resultado = new LinkedHashSet<>();
    ArgumentChecks.copiaSinNulos(codigos, nombre)
        .forEach(codigo -> resultado.add(codigoObligatorio(codigo, nombre)));
    return resultado;
  }

  private static CodigoMateria codigoObligatorio(String valor, String nombre) {
    return new CodigoMateria(textoObligatorio(valor, nombre));
  }

  private static Creditos creditosObligatorios(Integer valor, String nombre) {
    return new Creditos(enteroObligatorio(valor, nombre));
  }

  private static int enteroObligatorio(Integer valor, String nombre) {
    return ArgumentChecks.obligatorio(valor, nombre);
  }

  private static boolean booleanoObligatorio(Boolean valor, String nombre) {
    return ArgumentChecks.obligatorio(valor, nombre);
  }

  private static String textoObligatorio(String valor, String nombre) {
    return ArgumentChecks.obligatorioNoVacio(valor, nombre);
  }
}
