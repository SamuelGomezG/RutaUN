package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TodasLas;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

/**
 * TT-04-T3 (#91): el JSON de los datos de una regla es válido. Sin base de datos: solo mira el
 * texto que produce {@link JsonDeNormativa}.
 */
class JsonDeNormativaTest {

  private static final JsonMapper ESTRICTO =
      JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();

  private final Malla malla = MallaDePrueba.pequena();
  private final NodoDeMalla fundamentacion = MallaDePrueba.nodo(malla, "Fundamentación");

  @Test
  void tt04_elJsonDeLosDatosNoRepiteClaves() {
    String deComponente =
        JsonDeNormativa.mapper().writeValueAsString(new DatosDeRegla.DeComponente(fundamentacion));
    String deMateriaExterna =
        JsonDeNormativa.mapper()
            .writeValueAsString(
                new DatosDeRegla.DePrerrequisito(
                    new Prerrequisito(
                        new CodigoMateria("A"),
                        ExpresionDeMaterias.todasLas(
                            new TerminoMateria(new CodigoMateria("B")),
                            new ExpresionDeMaterias.MateriaExterna(
                                "Matemáticas Básicas",
                                Optional.empty(),
                                TipoMateriaExterna.NIVELACION)))));

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
      partes.add(new TerminoMateria(new CodigoMateria("T" + numero)));
    }
    partes.add(
        ExpresionDeMaterias.algunaDe(
            new TerminoMateria(new CodigoMateria("T8")),
            new TerminoMateria(new CodigoMateria("T9"))));
    partes.add(
        new ExpresionDeMaterias.MateriaExterna(
            "Matemáticas Básicas", Optional.empty(), TipoMateriaExterna.NIVELACION));
    DatosDeRegla.DePrerrequisito original =
        new DatosDeRegla.DePrerrequisito(
            new Prerrequisito(
                new CodigoMateria("9000010"),
                ExpresionDeMaterias.todasLas(partes.toArray(ExpresionDeMaterias[]::new))));

    String json = JsonDeNormativa.mapper().writeValueAsString(original);
    DatosDeRegla.DePrerrequisito leido =
        JsonDeNormativa.mapper().readValue(json, DatosDeRegla.DePrerrequisito.class);

    assertThat(leido).isEqualTo(original);
    TodasLas todas = (TodasLas) leido.prerrequisito().expresion();
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
}
