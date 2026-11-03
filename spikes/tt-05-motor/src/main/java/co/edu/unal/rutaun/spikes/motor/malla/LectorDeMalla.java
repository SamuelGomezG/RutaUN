package co.edu.unal.rutaun.spikes.motor.malla;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Lee el JSON propio del spike. No usa las clases de normativa de la aplicación. */
public final class LectorDeMalla {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private LectorDeMalla() {}

  /** Lee la malla del programa. */
  public static ModelosJson.MallaJson leer(Path ruta) {
    JsonNode raiz = MAPPER.readTree(ruta);
    return new ModelosJson.MallaJson(
        fuente(raiz.get("fuente")),
        raiz.get("creditosDelPrograma").asInt(),
        raiz.get("minimoPorPeriodo").asInt(),
        enteroOpcional(raiz.get("maximoPorPeriodo")),
        componentes(raiz.get("componentes")),
        libreEleccion(raiz.get("libreEleccion")),
        prerrequisitos(raiz.get("prerrequisitos")),
        correquisitos(raiz.get("correquisitos")),
        habilitaciones(raiz.get("habilitaciones")),
        externas(raiz.get("externas")));
  }

  /** Lee los escenarios de medición. */
  public static ModelosJson.EscenariosJson leerEscenarios(Path ruta) {
    JsonNode raiz = MAPPER.readTree(ruta);
    List<ModelosJson.EscenarioJson> escenarios = new ArrayList<>();
    JsonNode nodoEscenarios = raiz.get("escenarios");
    if (nodoEscenarios != null) {
      for (int i = 0; i < nodoEscenarios.size(); i++) {
        JsonNode n = nodoEscenarios.get(i);
        escenarios.add(
            new ModelosJson.EscenarioJson(
                n.get("id").asString(),
                n.get("nombre").asString(),
                textos(n.get("aprobadas")),
                textos(n.get("enCurso")),
                n.get("libreAprobados").asInt(),
                n.get("libreEnCurso").asInt()));
      }
    }
    List<ModelosJson.TopeJson> topes = new ArrayList<>();
    JsonNode nodoTopes = raiz.get("topes");
    if (nodoTopes != null) {
      for (int i = 0; i < nodoTopes.size(); i++) {
        JsonNode n = nodoTopes.get(i);
        topes.add(
            new ModelosJson.TopeJson(
                n.get("id").asString(),
                enteroOpcional(n.get("creditos")),
                enteroOpcional(n.get("materias"))));
      }
    }
    List<ModelosJson.VarianteJson> variantes = new ArrayList<>();
    JsonNode nodoVariantes = raiz.get("variantes");
    if (nodoVariantes != null) {
      for (int i = 0; i < nodoVariantes.size(); i++) {
        JsonNode n = nodoVariantes.get(i);
        variantes.add(
            new ModelosJson.VarianteJson(
                n.get("id").asString(),
                n.get("nombre").asString(),
                n.has("sintetica") && n.get("sintetica").asBoolean(),
                n.get("escenario").asString(),
                n.get("tope").asString(),
                textos(n.get("fijadas")),
                prerrequisitos(n.get("prerrequisitos")),
                correquisitos(n.get("correquisitos"))));
      }
    }
    return new ModelosJson.EscenariosJson(escenarios, topes, variantes);
  }

  private static ModelosJson.FuenteJson fuente(JsonNode n) {
    return new ModelosJson.FuenteJson(
        n.get("planDeEstudios").asString(),
        n.get("cuadro").asString(),
        n.get("mallaEstandar").asString(),
        n.get("transcrito").asString());
  }

  private static List<ModelosJson.ComponenteJson> componentes(JsonNode nodo) {
    List<ModelosJson.ComponenteJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.ComponenteJson(
              n.get("tipo").asString(),
              n.get("creditos").asInt(),
              n.get("obligatorios").asInt(),
              n.get("optativos").asInt(),
              materias(n.get("materias")),
              agrupaciones(n.get("agrupaciones"))));
    }
    return lista;
  }

  private static List<ModelosJson.AgrupacionJson> agrupaciones(JsonNode nodo) {
    List<ModelosJson.AgrupacionJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.AgrupacionJson(
              n.get("id").asString(),
              n.get("nombre").asString(),
              n.get("creditos").asInt(),
              materias(n.get("materias")),
              agrupaciones(n.get("subagrupaciones"))));
    }
    return lista;
  }

  private static List<ModelosJson.MateriaJson> materias(JsonNode nodo) {
    List<ModelosJson.MateriaJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.MateriaJson(
              n.get("codigo").asString(),
              n.get("nombre").asString(),
              n.get("creditos").asInt(),
              n.get("obligatoria").asBoolean(),
              enteroOpcional(n.get("semestre"))));
    }
    return lista;
  }

  private static ModelosJson.LibreEleccionJson libreEleccion(JsonNode n) {
    Map<String, Integer> sugeridos = new LinkedHashMap<>();
    JsonNode nodoSugeridos = n.get("sugeridosPorSemestre");
    if (nodoSugeridos != null) {
      for (String campo : nodoSugeridos.propertyNames()) {
        sugeridos.put(campo, nodoSugeridos.get(campo).asInt());
      }
    }
    if (!n.has("creditosPorMateria")) {
      // Un dato que falta no se inventa: los créditos de una materia de libre elección son un
      // dato de la norma del programa.
      throw new IllegalArgumentException(
          "Falta libreEleccion.creditosPorMateria en la malla: los créditos de una materia de "
              + "libre elección son un dato de la normativa y no tienen valor por omisión");
    }
    int creditosPorMateria = n.get("creditosPorMateria").asInt();
    if (creditosPorMateria <= 0) {
      throw new IllegalArgumentException(
          "libreEleccion.creditosPorMateria debe ser positivo y es " + creditosPorMateria);
    }
    return new ModelosJson.LibreEleccionJson(
        n.get("creditos").asInt(), creditosPorMateria, sugeridos);
  }

  private static List<ModelosJson.PrerrequisitoJson> prerrequisitos(JsonNode nodo) {
    List<ModelosJson.PrerrequisitoJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.PrerrequisitoJson(
              n.get("materia").asString(), expresion(n.get("expresion"))));
    }
    return lista;
  }

  private static List<ModelosJson.CorrequisitoJson> correquisitos(JsonNode nodo) {
    List<ModelosJson.CorrequisitoJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.CorrequisitoJson(
              n.get("materia").asString(), expresion(n.get("expresion"))));
    }
    return lista;
  }

  private static List<ModelosJson.HabilitacionJson> habilitaciones(JsonNode nodo) {
    List<ModelosJson.HabilitacionJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(
          new ModelosJson.HabilitacionJson(
              n.get("materia").asString(),
              n.get("medida").asString(),
              n.has("componente") ? n.get("componente").asString() : null,
              n.get("creditos").asInt()));
    }
    return lista;
  }

  private static List<ModelosJson.ExternaJson> externas(JsonNode nodo) {
    List<ModelosJson.ExternaJson> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      JsonNode n = nodo.get(i);
      lista.add(new ModelosJson.ExternaJson(n.get("nombre").asString(), n.get("tipo").asString()));
    }
    return lista;
  }

  private static ModelosJson.ExpresionJson expresion(JsonNode n) {
    if (n.isString()) {
      return ModelosJson.ExpresionJson.deCodigo(n.asString());
    }
    if (n.has("externa")) {
      return ModelosJson.ExpresionJson.deExterna(n.get("externa").asString());
    }
    if (n.has("todas")) {
      return ModelosJson.ExpresionJson.deTodas(expresiones(n.get("todas")));
    }
    if (n.has("alguna")) {
      return ModelosJson.ExpresionJson.deAlguna(expresiones(n.get("alguna")));
    }
    throw new IllegalArgumentException("Expresión sin forma conocida: " + n);
  }

  private static List<ModelosJson.ExpresionJson> expresiones(JsonNode nodo) {
    List<ModelosJson.ExpresionJson> lista = new ArrayList<>();
    for (int i = 0; i < nodo.size(); i++) {
      lista.add(expresion(nodo.get(i)));
    }
    return lista;
  }

  private static List<String> textos(JsonNode nodo) {
    List<String> lista = new ArrayList<>();
    if (nodo == null) {
      return lista;
    }
    for (int i = 0; i < nodo.size(); i++) {
      lista.add(nodo.get(i).asString());
    }
    return lista;
  }

  private static Integer enteroOpcional(JsonNode nodo) {
    return nodo == null || nodo.isNull() ? null : nodo.asInt();
  }
}
