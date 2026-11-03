package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.utilidades.ConfiguracionJson;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class FormatoNormativaTest {

  private static final String VALIDO = "/json/validos/consolidado-prueba.json";
  private static final List<String> INVALIDOS =
      List.of(
          "/json/invalidos/regla-sin-enunciado.json",
          "/json/invalidos/regla-sin-articulo.json",
          "/json/invalidos/datos-incompletos.json",
          "/json/invalidos/temporal-del-plan.json");

  @Test
  void uc07_hu03_t1_elArchivoValidoTieneElEsquemaEsperado() throws Exception {
    var json = leer(VALIDO);

    assertThat(json.path("schemaVersion").asString()).isEqualTo("1.0");
    assertThat(json.path("programa").path("codigoSia").asString()).isEqualTo("2541");
    assertThat(json.path("norma").path("tipo").asString()).isEqualTo("ACUERDO");
    assertThat(json.path("reglas").size()).isEqualTo(11);

    List<String> categorias = new ArrayList<>();
    for (var regla : json.path("reglas")) {
      assertThat(regla.path("categoria").asString())
          .isEqualTo(regla.path("datos").path("tipo").asString());
      categorias.add(regla.path("datos").path("tipo").asString());
    }
    assertThat(categorias)
        .containsExactly(
            "MATERIA",
            "PRERREQUISITO",
            "CORREQUISITO",
            "HABILITACION",
            "AGRUPACION",
            "COMPONENTE",
            "PROGRAMA",
            "EQUIVALENCIA",
            "CARGA_POR_PERIODO",
            "REQUISITO_DE_GRADO",
            "OTRA");
  }

  @Test
  void uc07_hu03_t1_losArchivosInvalidosSonJsonParseable() throws Exception {
    for (String ruta : INVALIDOS) {
      var json = leer(ruta);
      assertThat(json.path("schemaVersion").asString()).isEqualTo("1.0");
      assertThat(json.path("reglas").size()).isEqualTo(1);
    }
  }

  @Test
  void uc07_hu03_t1_losArchivosInvalidosTienenElDefectoEsperado() throws Exception {
    var sinEnunciado = leer("/json/invalidos/regla-sin-enunciado.json").path("reglas").path(0);
    var sinArticulo = leer("/json/invalidos/regla-sin-articulo.json").path("reglas").path(0);
    var datosIncompletos = leer("/json/invalidos/datos-incompletos.json").path("reglas").path(0);
    var temporalDelPlan = leer("/json/invalidos/temporal-del-plan.json").path("reglas").path(0);

    assertThat(sinEnunciado.has("enunciado")).isFalse();
    assertThat(sinArticulo.has("articulo")).isFalse();
    assertThat(datosIncompletos.path("datos").path("materia").has("creditos")).isFalse();
    assertThat(temporalDelPlan.path("clasificacion").asString()).isEqualTo("DEL_PLAN");
    assertThat(temporalDelPlan.path("vigenciaPropia").path("vigenteHasta").isNull()).isFalse();
  }

  private static JsonNode leer(String ruta) throws Exception {
    try (InputStream entrada = FormatoNormativaTest.class.getResourceAsStream(ruta)) {
      assertThat(entrada).as("archivo de prueba %s", ruta).isNotNull();
      return ConfiguracionJson.mapper().readTree(entrada);
    }
  }
}
