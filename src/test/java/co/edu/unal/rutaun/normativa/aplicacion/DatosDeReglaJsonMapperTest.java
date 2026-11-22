package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import co.edu.unal.rutaun.normativa.persistencia.DatosDeReglaJson;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import co.edu.unal.rutaun.utilidades.JsonConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

/**
 * TT-04-T6 (#145): la forma guardada de los datos de una regla recorre las once categorías sin
 * perder campos y rechaza un esquema desconocido o un número obligatorio ausente.
 */
class DatosDeReglaJsonMapperTest {

  private static final JsonMapper ESTRICTO =
      JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();

  private final Malla malla = MallaDePrueba.pequena();
  private final NodoDeMalla fundamentacion = MallaDePrueba.nodo(malla, "Fundamentación");

  @Test
  void tt04_cadaCategoriaHaceIdaYVueltaConTodosSusCampos() {
    for (DatosDeRegla original : casos()) {
      String texto = JsonConfig.mapper().writeValueAsString(DatosDeReglaJsonMapper.aJson(original));
      DatosDeReglaJson leido = JsonConfig.mapper().readValue(texto, DatosDeReglaJson.class);

      assertThat(DatosDeReglaJsonMapper.aDominio(leido))
          .as("ida y vuelta de %s", original.getClass().getSimpleName())
          .isEqualTo(original);
    }
  }

  @Test
  void tt04_laHabilitacionDelProgramaYLaDeComponenteSeGuardanDistinto() {
    String delPrograma =
        JsonConfig.mapper()
            .writeValueAsString(
                DatosDeReglaJsonMapper.aJson(
                    new DatosDeRegla.DeHabilitacion(
                        new ReglaDeHabilitacion(
                            new CodigoMateria("9000001"),
                            MedidaDeCreditos.delPrograma(),
                            new Creditos(80)))));
    String deComponente =
        JsonConfig.mapper()
            .writeValueAsString(
                DatosDeReglaJsonMapper.aJson(
                    new DatosDeRegla.DeHabilitacion(
                        new ReglaDeHabilitacion(
                            new CodigoMateria("9000001"),
                            MedidaDeCreditos.deComponente(TipoComponente.DISCIPLINAR),
                            new Creditos(40)))));

    assertThat(delPrograma).contains("\"componente\":null").contains("\"umbral\":80");
    assertThat(deComponente).contains("\"componente\":\"DISCIPLINAR\"").contains("\"umbral\":40");
  }

  @Test
  void tt04_unEsquemaDesconocidoSeRechaza() {
    assertThatThrownBy(
            () -> DatosDeReglaJsonMapper.aDominio(new DatosDeReglaJson.ProgramaJson(2, 160)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("esquema");
  }

  @Test
  void tt04_unNumeroAusenteNoSeLeeComoCero() {
    DatosDeReglaJson json =
        JsonConfig.mapper()
            .readValue("{\"categoria\":\"PROGRAMA\",\"esquema\":1}", DatosDeReglaJson.class);

    assertThatThrownBy(() -> DatosDeReglaJsonMapper.aDominio(json))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("creditosExigidos");
  }

  @Test
  void tt04_elJsonDeLosDatosNoRepiteClaves() {
    String deComponente =
        JsonConfig.mapper()
            .writeValueAsString(
                DatosDeReglaJsonMapper.aJson(new DatosDeRegla.DeComponente(fundamentacion)));
    String deMateriaExterna =
        JsonConfig.mapper()
            .writeValueAsString(
                DatosDeReglaJsonMapper.aJson(
                    new DatosDeRegla.DePrerrequisito(
                        new Prerrequisito(
                            new CodigoMateria("A"),
                            ExpresionDeMaterias.todasLas(
                                new ExpresionDeMaterias.TerminoMateria(new CodigoMateria("B")),
                                new ExpresionDeMaterias.MateriaExterna(
                                    "Matemáticas Básicas",
                                    Optional.empty(),
                                    TipoMateriaExterna.NIVELACION))))));

    // Componente y MateriaExterna tienen un campo «tipo»: el discriminante es «@tipo».
    assertThat(deComponente).contains("\"@tipo\":\"COMPONENTE\"").contains("\"tipo\":");
    assertThat(deMateriaExterna).contains("\"@tipo\":\"MATERIA_EXTERNA\"").contains("\"tipo\":");
    assertThat(ESTRICTO.readTree(deComponente)).isNotNull();
    assertThat(ESTRICTO.readTree(deMateriaExterna)).isNotNull();
  }

  @Test
  void tt04_unaExpresionGuardadaSeLeeIgualYEnElMismoOrden() {
    List<ExpresionDeMaterias> partes = new ArrayList<>();
    for (int numero = 1; numero <= 7; numero++) {
      partes.add(new ExpresionDeMaterias.TerminoMateria(new CodigoMateria("T" + numero)));
    }
    partes.add(
        ExpresionDeMaterias.algunaDe(
            new ExpresionDeMaterias.TerminoMateria(new CodigoMateria("T8")),
            new ExpresionDeMaterias.TerminoMateria(new CodigoMateria("T9"))));
    partes.add(
        new ExpresionDeMaterias.MateriaExterna(
            "Matemáticas Básicas", Optional.empty(), TipoMateriaExterna.NIVELACION));
    DatosDeRegla.DePrerrequisito original =
        new DatosDeRegla.DePrerrequisito(
            new Prerrequisito(
                new CodigoMateria("9000010"),
                ExpresionDeMaterias.todasLas(partes.toArray(ExpresionDeMaterias[]::new))));

    String json = JsonConfig.mapper().writeValueAsString(DatosDeReglaJsonMapper.aJson(original));
    DatosDeReglaJson leido = JsonConfig.mapper().readValue(json, DatosDeReglaJson.class);
    DatosDeRegla.DePrerrequisito vuelto =
        (DatosDeRegla.DePrerrequisito) DatosDeReglaJsonMapper.aDominio(leido);

    assertThat(vuelto).isEqualTo(original);
    ExpresionDeMaterias.TodasLas todas =
        (ExpresionDeMaterias.TodasLas) vuelto.prerrequisito().expresion();
    assertThat(todas.terminos()).containsExactlyElementsOf(partes);
    assertThat(json)
        .contains("\"@tipo\":\"Y\"")
        .contains("\"terminos\"")
        .doesNotContain("izquierda");
  }

  @Test
  void tt04_elLectorEstrictoDetectaUnaClaveRepetida() {
    // Control de la prueba anterior: sin esto, un lector que no detecta repetidas la dejaría pasar.
    assertThatThrownBy(() -> ESTRICTO.readTree("{\"tipo\":\"A\",\"tipo\":\"B\"}"))
        .isInstanceOf(StreamReadException.class);
  }

  private static List<DatosDeRegla> casos() {
    List<DatosDeRegla> casos = new ArrayList<>(ReglasDePrueba.datosDeTodasLasCategorias());
    casos.add(
        new DatosDeRegla.DeHabilitacion(
            new ReglaDeHabilitacion(
                new CodigoMateria("9000001"), MedidaDeCreditos.delPrograma(), new Creditos(80))));
    return casos;
  }
}
