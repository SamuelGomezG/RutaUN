package co.edu.unal.rutaun.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

/**
 * SOP-06 (#58): el fragmento {@code layout/aviso-no-verifica} lleva el texto fijo del glosario. Se
 * prueba solo, sin pasar por ninguna página: los planes, las comparaciones y las explicaciones lo
 * incluirán cuando existan, y cada uno comprobará allí que lo incluye.
 */
@SpringBootTest
@ActiveProfiles("test")
class AvisoNoVerificaTest {

  @Autowired private ITemplateEngine motor;

  /** Renderiza solo el fragmento {@code aviso}, tal como lo incluiría una página. */
  private String aviso() {
    return motor.process("layout/aviso-no-verifica", Set.of("aviso"), new Context());
  }

  @Test
  void sop06_elAvisoDiceQueNoEsInscripcionNiAutorizacion() {
    assertThat(aviso())
        .contains("Aviso de lo que RutaUN no verifica")
        .contains("es una guía: no es una inscripción ni una autorización");
  }

  @Test
  void sop06_elAvisoNombraTodoLoQueRutaUNNoVerifica() {
    assertThat(aviso())
        .contains(
            "la oferta de materias",
            "los cupos y sobrecupos",
            "los horarios y cruces de clases",
            "el cupo de créditos (Acuerdo 008 de 2008, art. 11)",
            "los reconocimientos (validaciones, homologaciones y convalidaciones)",
            "las autorizaciones del Consejo de Facultad",
            "materias de nivelación",
            "el cumplimiento de los requisitos de grado");
  }

  /** Estas frases niegan: si se invierten, el aviso diría lo contrario de la regla 5. */
  @Test
  void sop06_elAvisoConservaLasFrasesQueNiegan() {
    assertThat(aviso())
        .contains("RutaUN no verifica:")
        .contains("Tampoco describe de qué trata cada materia")
        .contains("culminaría su plan de estudios, no cuándo se graduaría")
        .doesNotContain("RutaUN verifica:")
        .doesNotContain("También describe");
  }

  @Test
  void sop06_elAvisoDiceQueLaFuenteOficialEsRegimenLegal() {
    assertThat(aviso()).contains("La fuente oficial de las normas es Régimen Legal");
  }

  /** El {@code id} no se puede repetir en una página: el aviso sale una sola vez. */
  @Test
  void sop06_elAvisoSaleUnaSolaVez() {
    assertThat(aviso())
        .containsOnlyOnce("Aviso de lo que RutaUN no verifica")
        .containsOnlyOnce("RutaUN no verifica:")
        .containsOnlyOnce("id=\"aviso-no-verifica\"");
  }
}
