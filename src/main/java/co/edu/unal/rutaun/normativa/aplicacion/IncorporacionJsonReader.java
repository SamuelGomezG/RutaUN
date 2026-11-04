package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Componente;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.Correquisito;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExigenciaCreditos;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.MateriaEnMalla;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Obligatoriedad;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.TipoDeAfectacion;
import co.edu.unal.rutaun.normativa.dominio.TipoDeCargaPorPeriodo;
import co.edu.unal.rutaun.normativa.dominio.TipoDeMateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import co.edu.unal.rutaun.utilidades.ConfiguracionJson;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

/**
 * Lee el JSON de UC07-HU03-T2 y construye el borrador de una incorporación. Vive en la capa de
 * aplicación para que lo usen el arranque de desarrollo y, después, el formulario de carga sin
 * rehacerlo.
 *
 * <p>El estado de cada regla queda en {@link EstadoRegla#VIGENTE}; el servicio de importación lo
 * ajusta con el periodo en curso. Las referencias de una afectación usan identificadores
 * provisionales hasta que el servicio las resuelva contra el consolidado.
 */
public final class IncorporacionJsonReader {

  private static final String VERSION = "1.0";

  private IncorporacionJsonReader() {}

  /** Lee un archivo válido o devuelve el motivo por el que no puede construir la incorporación. */
  public static ResultadoDeLectura leer(InputStream entrada) {
    ArgumentChecks.obligatorio(entrada, "entrada");
    try {
      JsonNode raiz = ConfiguracionJson.mapper().readTree(entrada);
      exigirObjeto(raiz, "raíz");
      if (!VERSION.equals(texto(raiz, "schemaVersion"))) {
        throw new IllegalArgumentException("schemaVersion debe ser " + VERSION);
      }

      ProgramaJson programa = programa(objeto(raiz, "programa"));
      NormaLeida norma = norma(objeto(raiz, "norma"));
      List<Regla> reglas = new ArrayList<>();
      for (JsonNode regla : arreglo(raiz, "reglas")) {
        reglas.add(regla(regla, norma));
      }
      return new ResultadoDeLectura.Leida(new IncorporacionLeida(programa, norma, reglas));
    } catch (IllegalArgumentException | DateTimeParseException | JacksonException e) {
      String motivo = e.getMessage();
      return new ResultadoDeLectura.Invalida(
          motivo == null || motivo.isBlank() ? "JSON mal formado" : motivo);
    }
  }

  private static ProgramaJson programa(JsonNode nodo) {
    return new ProgramaJson(
        textoNoVacio(nodo, "nombre"),
        textoNoVacio(nodo, "codigoSia"),
        textoNoVacio(nodo, "facultad"));
  }

  private static NormaLeida norma(JsonNode nodo) {
    IdentificacionNorma identificacion =
        new IdentificacionNorma(
            TipoNorma.valueOf(texto(nodo, "tipo")),
            texto(nodo, "numero"),
            entero(nodo, "anio"),
            texto(nodo, "organo"));
    return new NormaLeida(
        NormaId.nuevo(),
        identificacion,
        TipoDeEfecto.valueOf(texto(nodo, "tipoDeEfecto")),
        fecha(nodo, "fechaExpedicion"),
        vigencia(objeto(nodo, "vigencia")),
        textoOpcional(nodo, "enlaceRegimenLegal"),
        alcance(objeto(nodo, "alcance")));
  }

  private static AlcanceLeida alcance(JsonNode nodo) {
    return new AlcanceLeida(
        TipoAlcance.valueOf(texto(nodo, "tipo")),
        textos(nodo, "facultades").stream().map(Facultad::new).collect(Collectors.toSet()),
        Set.copyOf(textos(nodo, "programas")),
        Set.copyOf(textos(nodo, "exclusiones")),
        texto(nodo, "texto"));
  }

  private static Vigencia vigencia(JsonNode nodo) {
    return new Vigencia(momento(nodo, "aplicaDesde"), momentoOpcional(nodo, "vigenteHasta"));
  }

  private static Momento momento(JsonNode nodo, String campo) {
    JsonNode valor = objeto(nodo, campo);
    return switch (texto(valor, "tipo")) {
      case "PERIODO" -> new Momento.EnPeriodo(periodo(valor));
      case "FECHA" -> new Momento.EnFecha(fecha(valor, "fecha"));
      default -> throw new IllegalArgumentException(campo + ".tipo no es válido");
    };
  }

  private static Optional<Momento> momentoOpcional(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (valor.isMissingNode() || valor.isNull()) {
      return Optional.empty();
    }
    return Optional.of(momento(nodo, campo));
  }

  private static PeriodoAcademico periodo(JsonNode nodo) {
    String valor = texto(nodo, "periodo");
    String[] partes = valor.split("-");
    if (partes.length != 2 || (!"1".equals(partes[1]) && !"2".equals(partes[1]))) {
      throw new IllegalArgumentException("periodo debe tener el formato AAAA-1 o AAAA-2");
    }
    ParteDelAnio parte = "1".equals(partes[1]) ? ParteDelAnio.PRIMERO : ParteDelAnio.SEGUNDO;
    return new PeriodoAcademico(Integer.parseInt(partes[0]), parte);
  }

  private static Regla regla(JsonNode nodo, NormaLeida norma) {
    String categoria = texto(nodo, "categoria");
    JsonNode datosJson = objeto(nodo, "datos");
    String tipo = texto(datosJson, "tipo");
    DatosDeRegla datos = datos(tipo, datosJson);
    if (!categoria.equals(categoriaDe(datos))) {
      throw new IllegalArgumentException(
          "la categoría " + categoria + " no coincide con datos.tipo " + tipo);
    }
    return Regla.registrar(
        texto(nodo, "objeto"),
        texto(nodo, "enunciado"),
        new Fuente(norma.id(), texto(nodo, "articulo")),
        vigenciaPropia(nodo),
        ClasificacionRegla.valueOf(texto(nodo, "clasificacion")),
        EstadoRegla.VIGENTE,
        afectacion(nodo, norma),
        datos);
  }

  private static Optional<Vigencia> vigenciaPropia(JsonNode nodo) {
    JsonNode valor = nodo.path("vigenciaPropia");
    if (valor.isMissingNode() || valor.isNull()) {
      return Optional.empty();
    }
    return Optional.of(vigencia(objeto(nodo, "vigenciaPropia")));
  }

  private static Optional<Afectacion> afectacion(JsonNode nodo, NormaLeida norma) {
    JsonNode valor = nodo.path("afectacion");
    if (valor.isMissingNode() || valor.isNull()) {
      return Optional.empty();
    }
    TipoDeAfectacion tipo = TipoDeAfectacion.valueOf(texto(valor, "tipo"));
    JsonNode destino = objeto(valor, "destino");
    String tipoDestino = texto(destino, "tipo");
    Optional<ReglaId> reglaAfectada = Optional.empty();
    Optional<Fuente> articuloAfectado = Optional.empty();
    if ("REGLA".equals(tipoDestino)) {
      NormaId normaAfectada = normaId(objeto(destino, "norma"), norma);
      String articulo = texto(destino, "articulo");
      String objeto = texto(destino, "objeto");
      String llave = normaAfectada.valor() + "|" + articulo + "|" + objeto;
      reglaAfectada =
          Optional.of(new ReglaId(UUID.nameUUIDFromBytes(llave.getBytes(StandardCharsets.UTF_8))));
    } else if ("ARTICULO".equals(tipoDestino)) {
      articuloAfectado =
          Optional.of(
              new Fuente(normaId(objeto(destino, "norma"), norma), texto(destino, "articulo")));
    } else {
      throw new IllegalArgumentException("destino.tipo no es válido");
    }
    Optional<Momento> desde = momentoOpcional(valor, "desde");
    return Optional.of(new Afectacion(tipo, reglaAfectada, articuloAfectado, desde));
  }

  private static NormaId normaId(JsonNode nodo, NormaLeida actual) {
    String tipo = texto(nodo, "tipo");
    String numero = texto(nodo, "numero");
    int anio = entero(nodo, "anio");
    String organo = texto(nodo, "organo");
    if (tipo.equals(actual.identificacion().tipo().name())
        && numero.equals(actual.identificacion().numero())
        && anio == actual.identificacion().anio()
        && organo.equals(actual.identificacion().organo())) {
      return actual.id();
    }
    String llave = tipo + "|" + numero + "|" + anio + "|" + organo;
    return new NormaId(UUID.nameUUIDFromBytes(llave.getBytes(StandardCharsets.UTF_8)));
  }

  private static DatosDeRegla datos(String tipo, JsonNode nodo) {
    return switch (tipo) {
      case "MATERIA" ->
          new DatosDeRegla.DeMateria(materia(objeto(nodo, "materia")), contenedor(nodo));
      case "PRERREQUISITO" ->
          new DatosDeRegla.DePrerrequisito(
              new Prerrequisito(
                  new CodigoMateria(texto(nodo, "materia")), expresion(objeto(nodo, "expresion"))));
      case "CORREQUISITO" ->
          new DatosDeRegla.DeCorrequisito(
              new Correquisito(
                  new CodigoMateria(texto(nodo, "materia")), expresion(objeto(nodo, "expresion"))));
      case "HABILITACION" ->
          new DatosDeRegla.DeHabilitacion(
              new ReglaDeHabilitacion(
                  new CodigoMateria(texto(nodo, "materia")),
                  medida(objeto(nodo, "medida")),
                  new Creditos(entero(nodo, "umbral"))));
      case "AGRUPACION" ->
          new DatosDeRegla.DeAgrupacion(agrupacion(objeto(nodo, "agrupacion")), contenedor(nodo));
      case "COMPONENTE" -> new DatosDeRegla.DeComponente(componente(objeto(nodo, "componente")));
      case "PROGRAMA" ->
          new DatosDeRegla.DePrograma(new Creditos(entero(nodo, "creditosExigidos")));
      case "EQUIVALENCIA" ->
          new DatosDeRegla.DeEquivalencia(
              textos(nodo, "origen").stream().map(CodigoMateria::new).collect(Collectors.toSet()),
              new CodigoMateria(texto(nodo, "destino")));
      case "CARGA_POR_PERIODO" ->
          new DatosDeRegla.DeCargaPorPeriodo(
              new Creditos(entero(nodo, "creditos")),
              TipoDeCargaPorPeriodo.valueOf(texto(nodo, "tipoCarga")));
      case "REQUISITO_DE_GRADO" -> new DatosDeRegla.DeRequisitoDeGrado();
      case "OTRA" -> new DatosDeRegla.DeOtraCategoria();
      default -> throw new IllegalArgumentException("datos.tipo no es válido: " + tipo);
    };
  }

  private static String categoriaDe(DatosDeRegla datos) {
    return switch (datos) {
      case DatosDeRegla.DeMateria materia -> "MATERIA";
      case DatosDeRegla.DePrerrequisito prerrequisito -> "PRERREQUISITO";
      case DatosDeRegla.DeCorrequisito correquisito -> "CORREQUISITO";
      case DatosDeRegla.DeHabilitacion habilitacion -> "HABILITACION";
      case DatosDeRegla.DeAgrupacion agrupacion -> "AGRUPACION";
      case DatosDeRegla.DeComponente componente -> "COMPONENTE";
      case DatosDeRegla.DePrograma programa -> "PROGRAMA";
      case DatosDeRegla.DeEquivalencia equivalencia -> "EQUIVALENCIA";
      case DatosDeRegla.DeCargaPorPeriodo carga -> "CARGA_POR_PERIODO";
      case DatosDeRegla.DeRequisitoDeGrado requisito -> "REQUISITO_DE_GRADO";
      case DatosDeRegla.DeOtraCategoria otra -> "OTRA";
    };
  }

  private static MateriaEnMalla materia(JsonNode nodo) {
    return new MateriaEnMalla(
        new CodigoMateria(texto(nodo, "codigo")),
        texto(nodo, "nombre"),
        new Creditos(entero(nodo, "creditos")),
        Obligatoriedad.valueOf(texto(nodo, "obligatoriedad")),
        enteroOpcional(nodo, "semestreSugerido"));
  }

  private static NodoDeMalla contenedor(JsonNode nodo) {
    JsonNode contenedor = objeto(nodo, "contenedor");
    String componente = texto(contenedor, "componente");
    Optional<String> agrupacion = textoOpcional(contenedor, "agrupacion");
    if (agrupacion.isPresent()) {
      return new Agrupacion(
          AgrupacionId.nuevo(),
          agrupacion.get(),
          ExigenciaCreditos.de(new Creditos(0)),
          List.of(),
          List.of());
    }
    return new Componente(
        componente,
        TipoComponente.FUNDAMENTACION,
        ExigenciaCreditos.de(new Creditos(0)),
        List.of(),
        List.of());
  }

  private static Agrupacion agrupacion(JsonNode nodo) {
    return new Agrupacion(
        AgrupacionId.nuevo(),
        texto(nodo, "nombre"),
        exigencia(nodo, "creditosExigidos"),
        List.of(),
        List.of());
  }

  private static NodoDeMalla componente(JsonNode nodo) {
    String tipo = texto(nodo, "tipo");
    JsonNode desglose = nodo.path("desglose");
    if ("LIBRE_ELECCION".equals(tipo) && !desglose.isMissingNode() && !desglose.isNull()) {
      throw new IllegalArgumentException("LIBRE_ELECCION no lleva desglose");
    }
    ExigenciaCreditos exigencia = exigencia(nodo, "creditosExigidos");
    String nombre = texto(nodo, "nombre");
    if ("LIBRE_ELECCION".equals(tipo)) {
      return new ComponenteDeLibreEleccion(nombre, exigencia, List.of());
    }
    return new Componente(nombre, TipoComponente.valueOf(tipo), exigencia, List.of(), List.of());
  }

  private static ExigenciaCreditos exigencia(JsonNode nodo, String campo) {
    Creditos exigidos = new Creditos(entero(nodo, campo));
    JsonNode desglose = nodo.path("desglose");
    if (desglose.isMissingNode() || desglose.isNull()) {
      return ExigenciaCreditos.de(exigidos);
    }
    return new ExigenciaCreditos(
        exigidos,
        Optional.of(
            new ExigenciaCreditos.Desglose(
                new Creditos(entero(desglose, "obligatorios")),
                new Creditos(entero(desglose, "optativos")))));
  }

  private static ExpresionDeMaterias expresion(JsonNode nodo) {
    return switch (texto(nodo, "tipo")) {
      case "MATERIA_DEL_PROGRAMA" ->
          new ExpresionDeMaterias.MateriaDelPrograma(new CodigoMateria(texto(nodo, "codigo")));
      case "MATERIA_EXTERNA" ->
          new ExpresionDeMaterias.MateriaExterna(
              texto(nodo, "nombre"),
              codigoOpcional(nodo),
              TipoDeMateriaExterna.valueOf(texto(nodo, "origen")));
      case "Y" ->
          new ExpresionDeMaterias.Y(
              expresion(objeto(nodo, "izquierda")), expresion(objeto(nodo, "derecha")));
      case "O" ->
          new ExpresionDeMaterias.O(
              expresion(objeto(nodo, "izquierda")), expresion(objeto(nodo, "derecha")));
      default -> throw new IllegalArgumentException("expresion.tipo no es válido");
    };
  }

  private static Optional<CodigoMateria> codigoOpcional(JsonNode nodo) {
    return textoOpcional(nodo, "codigo").map(CodigoMateria::new);
  }

  private static MedidaDeCreditos medida(JsonNode nodo) {
    return switch (texto(nodo, "tipo")) {
      case "PROGRAMA" -> MedidaDeCreditos.delPrograma();
      case "COMPONENTE" -> MedidaDeCreditos.deComponente(texto(nodo, "componente"));
      default -> throw new IllegalArgumentException("medida.tipo no es válido");
    };
  }

  private static Optional<String> textoOpcional(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (valor.isMissingNode() || valor.isNull()) {
      return Optional.empty();
    }
    if (!valor.isString()) {
      throw new IllegalArgumentException(campo + " debe ser texto");
    }
    return Optional.of(valor.asString());
  }

  private static Optional<Integer> enteroOpcional(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (valor.isMissingNode() || valor.isNull()) {
      return Optional.empty();
    }
    if (!valor.isInt()) {
      throw new IllegalArgumentException(campo + " debe ser un entero");
    }
    return Optional.of(valor.intValue());
  }

  private static String texto(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (!valor.isString()) {
      throw new IllegalArgumentException(campo + " debe ser texto");
    }
    return valor.asString();
  }

  private static String textoNoVacio(JsonNode nodo, String campo) {
    return ArgumentChecks.obligatorioNoVacio(texto(nodo, campo), campo);
  }

  private static int entero(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (!valor.isInt()) {
      throw new IllegalArgumentException(campo + " debe ser un entero");
    }
    return valor.intValue();
  }

  private static LocalDate fecha(JsonNode nodo, String campo) {
    return LocalDate.parse(texto(nodo, campo));
  }

  private static JsonNode objeto(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    exigirObjeto(valor, campo);
    return valor;
  }

  private static void exigirObjeto(JsonNode nodo, String campo) {
    if (!nodo.isObject()) {
      throw new IllegalArgumentException(campo + " debe ser un objeto");
    }
  }

  private static JsonNode arreglo(JsonNode nodo, String campo) {
    JsonNode valor = nodo.path(campo);
    if (!valor.isArray()) {
      throw new IllegalArgumentException(campo + " debe ser un arreglo");
    }
    return valor;
  }

  private static List<String> textos(JsonNode nodo, String campo) {
    List<String> valores = new ArrayList<>();
    for (JsonNode valor : arreglo(nodo, campo)) {
      if (!valor.isString()) {
        throw new IllegalArgumentException(campo + " debe contener textos");
      }
      valores.add(valor.asString());
    }
    return valores;
  }

  /** Resultado sellado de la lectura. */
  public sealed interface ResultadoDeLectura
      permits ResultadoDeLectura.Leida, ResultadoDeLectura.Invalida {

    /** El archivo se leyó completo. */
    record Leida(IncorporacionLeida incorporacion) implements ResultadoDeLectura {

      /** Exige la incorporación. */
      public Leida {
        ArgumentChecks.obligatorio(incorporacion, "incorporacion");
      }
    }

    /** El archivo no se pudo convertir en una incorporación. */
    record Invalida(String motivo) implements ResultadoDeLectura {

      /** Exige un motivo no vacío. */
      public Invalida {
        ArgumentChecks.obligatorioNoVacio(motivo, "motivo");
      }
    }
  }
}
