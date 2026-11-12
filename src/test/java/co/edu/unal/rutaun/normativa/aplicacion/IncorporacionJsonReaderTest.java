package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IncorporacionJsonReaderTest {

  private static final String VALIDO = "/json/validos/consolidado-prueba.json";
  private static final List<String> INVALIDOS =
      List.of(
          "/json/invalidos/regla-sin-enunciado.json",
          "/json/invalidos/regla-sin-articulo.json",
          "/json/invalidos/datos-incompletos.json",
          "/json/invalidos/temporal-del-plan.json");

  private static final String COMPONENTE_LIBRE =
      """
      {
        "schemaVersion": "1.0",
        "programa": {
          "codigoSia": "2541",
          "nombre": "Ingeniería de Sistemas y Computación",
          "facultad": "Facultad de Ingeniería"
        },
        "norma": {
          "tipo": "ACUERDO",
          "numero": "003-A",
          "anio": 2022,
          "organo": "Consejo Académico",
          "tipoDeEfecto": "BASE",
          "fechaExpedicion": "2022-05-25",
          "vigencia": {
            "aplicaDesde": { "tipo": "PERIODO", "periodo": "2022-2" },
            "vigenteHasta": null
          },
          "enlaceRegimenLegal": null,
          "alcance": {
            "tipo": "PROGRAMAS",
            "facultades": [],
            "programas": ["2541"],
            "exclusiones": [],
            "texto": "Aplica al programa de prueba."
          }
        },
        "reglas": [
          {
            "categoria": "COMPONENTE",
            "objeto": "Libre elección",
            "enunciado": "La libre elección exige 6 créditos.",
            "articulo": "art. 3",
            "vigenciaPropia": null,
            "clasificacion": "DEL_PLAN",
            "afectacion": null,
            "datos": {
              "tipo": "COMPONENTE",
              "componente": {
                "nombre": "Libre elección",
                "tipo": "LIBRE_ELECCION",
                "creditosExigidos": 6,
                "desglose": %s
              }
            }
          }
        ]
      }
      """;

  @Test
  void uc07_hu03_t2_elArchivoValidoSeLeeCompleto() {
    var resultado = leer(VALIDO);

    assertThat(resultado).isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Leida.class);
    IncorporacionLeida incorporacion =
        ((IncorporacionJsonReader.ResultadoDeLectura.Leida) resultado).incorporacion();

    assertThat(incorporacion.programa().codigoSia()).isEqualTo("2541");
    assertThat(incorporacion.norma().identificacion().numero()).isEqualTo("003-A");
    assertThat(incorporacion.norma().alcance().tipo().name()).isEqualTo("PROGRAMAS");
    assertThat(incorporacion.norma().alcance().programas()).contains("2541");
    assertThat(incorporacion.reglas()).hasSize(11);
    assertThat(incorporacion.reglas().stream().map(Regla::datos).map(this::categoria).toList())
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
  void uc07_hu03_t2_leeLaMallaYLasExpresiones() {
    IncorporacionLeida incorporacion = incorporacionValida();

    DatosDeRegla.DeMateria materia = (DatosDeRegla.DeMateria) incorporacion.reglas().get(0).datos();
    assertThat(materia.materia().codigo().valor()).isEqualTo("2024042");
    assertThat(materia.materia().semestreSugerido()).contains(1);
    assertThat(materia.contenedor()).isInstanceOf(Agrupacion.class);
    assertThat(((Agrupacion) materia.contenedor()).nombre()).isEqualTo("Matemáticas");

    DatosDeRegla.DePrerrequisito prerrequisito =
        (DatosDeRegla.DePrerrequisito) incorporacion.reglas().get(1).datos();
    assertThat(prerrequisito.prerrequisito().expresion()).isInstanceOf(ExpresionDeMaterias.Y.class);
    ExpresionDeMaterias.Y expresion =
        (ExpresionDeMaterias.Y) prerrequisito.prerrequisito().expresion();
    assertThat(expresion.derecha()).isInstanceOf(ExpresionDeMaterias.MateriaExterna.class);

    DatosDeRegla.DeAgrupacion agrupacion =
        (DatosDeRegla.DeAgrupacion) incorporacion.reglas().get(4).datos();
    assertThat(agrupacion.agrupacion().nombre()).isEqualTo("Matemáticas");
    assertThat(agrupacion.agrupacion().exigencia().exigidos().valor()).isEqualTo(16);

    DatosDeRegla.DeComponente componente =
        (DatosDeRegla.DeComponente) incorporacion.reglas().get(5).datos();
    assertThat(componente.componente()).isNotNull();
  }

  @Test
  void uc07_hu03_t2_leeComponenteDeLibreEleccionSinDesglose() {
    var resultado = leerTexto(COMPONENTE_LIBRE.formatted("null"));

    assertThat(resultado).isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Leida.class);
    IncorporacionLeida incorporacion =
        ((IncorporacionJsonReader.ResultadoDeLectura.Leida) resultado).incorporacion();
    DatosDeRegla.DeComponente componente =
        (DatosDeRegla.DeComponente) incorporacion.reglas().get(0).datos();

    assertThat(componente.componente()).isInstanceOf(ComponenteDeLibreEleccion.class);
    assertThat(((ComponenteDeLibreEleccion) componente.componente()).exigencia().exigidos().valor())
        .isEqualTo(6);
  }

  @Test
  void uc07_hu03_t2_rechazaDesgloseEnLibreEleccion() {
    var resultado =
        leerTexto(COMPONENTE_LIBRE.formatted("{\"obligatorios\": 6, \"optativos\": 0}"));

    assertThat(resultado).isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Invalida.class);
    assertThat(((IncorporacionJsonReader.ResultadoDeLectura.Invalida) resultado).motivo())
        .contains("LIBRE_ELECCION no lleva desglose");
  }

  @Test
  void uc07_hu03_t2_losArchivosInvalidosDevuelvenElMotivo() {
    for (String ruta : INVALIDOS) {
      var resultado = leer(ruta);
      assertThat(resultado).isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Invalida.class);
      assertThat(((IncorporacionJsonReader.ResultadoDeLectura.Invalida) resultado).motivo())
          .isNotBlank();
    }
  }

  @Test
  void uc07_hu03_t2_unJsonMalFormadoDevuelveResultadoInvalida() {
    var resultado =
        IncorporacionJsonReader.leer(
            new ByteArrayInputStream("{".getBytes(StandardCharsets.UTF_8)));

    assertThat(resultado).isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Invalida.class);
    assertThat(((IncorporacionJsonReader.ResultadoDeLectura.Invalida) resultado).motivo())
        .isNotBlank();
  }

  @Test
  void uc07_hu03_t2_elInputStreamEsObligatorio() {
    assertThatThrownBy(() -> IncorporacionJsonReader.leer(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("entrada no puede ser null");
  }

  /**
   * Fija las fechas de la semilla real contra los PDF de Régimen Legal. Es un espejo intencional
   * del JSON: no prueba al lector, sino que un cambio accidental de la semilla no pase inadvertido;
   * la comparación con la fuente oficial se hace en la revisión de la PR.
   *
   * <p>TT-04-T5 (#134). El Acuerdo 008 es provisional: con R-05 (#141) pasa al periodo «2009-1».
   */
  @ParameterizedTest
  @CsvSource({
    "semilla/acuerdo-003a-2022.json, 2022-10-14, 2023-01-11",
    "semilla/acuerdo-008-2008.json, 2008-04-15, 2009-01-01",
    "semilla/acuerdo-11-2023.json, 2023-12-07, 2023-12-12"
  })
  void tt04_lasNormasDeLaSemillaTienenLasFechasDeRegimenLegal(
      String archivo, String expedicion, String aplicaDesde) {
    var resultado = leer("/" + archivo);

    assertThat(resultado)
        .as("%s se lee como incorporación válida", archivo)
        .isInstanceOf(IncorporacionJsonReader.ResultadoDeLectura.Leida.class);
    NormaLeida norma =
        ((IncorporacionJsonReader.ResultadoDeLectura.Leida) resultado).incorporacion().norma();
    assertThat(norma.fechaExpedicion())
        .as("%s: fechaExpedicion", archivo)
        .isEqualTo(LocalDate.parse(expedicion));
    assertThat(norma.vigencia().aplicaDesde())
        .as("%s: aplicaDesde", archivo)
        .isEqualTo(new Momento.EnFecha(LocalDate.parse(aplicaDesde)));
  }

  private static IncorporacionLeida incorporacionValida() {
    var resultado = leer(VALIDO);
    return ((IncorporacionJsonReader.ResultadoDeLectura.Leida) resultado).incorporacion();
  }

  private static IncorporacionJsonReader.ResultadoDeLectura leerTexto(String json) {
    return IncorporacionJsonReader.leer(
        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }

  private static IncorporacionJsonReader.ResultadoDeLectura leer(String ruta) {
    try (InputStream entrada = IncorporacionJsonReaderTest.class.getResourceAsStream(ruta)) {
      assertThat(entrada).as("archivo de prueba %s", ruta).isNotNull();
      return IncorporacionJsonReader.leer(entrada);
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
  }

  private String categoria(DatosDeRegla datos) {
    return switch (datos) {
      case DatosDeRegla.DeMateria ignored -> "MATERIA";
      case DatosDeRegla.DePrerrequisito ignored -> "PRERREQUISITO";
      case DatosDeRegla.DeCorrequisito ignored -> "CORREQUISITO";
      case DatosDeRegla.DeHabilitacion ignored -> "HABILITACION";
      case DatosDeRegla.DeAgrupacion ignored -> "AGRUPACION";
      case DatosDeRegla.DeComponente ignored -> "COMPONENTE";
      case DatosDeRegla.DePrograma ignored -> "PROGRAMA";
      case DatosDeRegla.DeEquivalencia ignored -> "EQUIVALENCIA";
      case DatosDeRegla.DeCargaPorPeriodo ignored -> "CARGA_POR_PERIODO";
      case DatosDeRegla.DeRequisitoDeGrado ignored -> "REQUISITO_DE_GRADO";
      case DatosDeRegla.DeOtraCategoria ignored -> "OTRA";
    };
  }
}
