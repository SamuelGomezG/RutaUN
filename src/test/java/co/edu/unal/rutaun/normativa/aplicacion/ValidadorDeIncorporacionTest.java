package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ValidadorDeIncorporacionTest {

  private static final ProgramaJson PROGRAMA =
      new ProgramaJson("Ingeniería de Sistemas y Computación", "2541", "Facultad de Ingeniería");
  private static final List<String> EXT6A =
      List.of(
          "/json/invalidos/regla-sin-enunciado.json",
          "/json/invalidos/regla-sin-articulo.json",
          "/json/invalidos/datos-incompletos.json");

  @Test
  void uc07_ext6a_rechazaReglasSinEnunciadoSinArticuloYConDatosIncompletos() {
    for (String ruta : EXT6A) {
      var resultado = validarArchivo(ruta);

      assertThat(resultado)
          .isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida.class);
      assertThat(((ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida) resultado).motivos())
          .isNotEmpty();
    }
  }

  @Test
  void uc07_ext6f_rechazaReglaTemporalPropiaClasificadaComoDelPlan() {
    var resultado = validarArchivo("/json/invalidos/temporal-del-plan.json");

    assertThat(resultado)
        .isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida.class);
    assertThat(((ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida) resultado).motivos())
        .anyMatch(motivo -> motivo.contains("Plan de estudios") && motivo.contains("temporal"));
  }

  @Test
  void uc07_ext6f_rechazaReglaDelPlanDeUnaNormaTemporal() {
    NormaLeida norma = norma(true);
    IncorporacionLeida incorporacion =
        new IncorporacionLeida(
            PROGRAMA,
            norma,
            List.of(
                regla(
                    norma,
                    "Regla de norma temporal",
                    Optional.empty(),
                    ClasificacionRegla.DEL_PLAN)));

    var resultado = ValidadorDeIncorporacion.validar(incorporacion);

    assertThat(resultado)
        .isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Invalida.class);
  }

  @Test
  void uc07_ext5a_aceptaUnaNormaConTodasSusReglasInformativas() {
    NormaLeida norma = norma(true);
    IncorporacionLeida incorporacion =
        new IncorporacionLeida(
            PROGRAMA,
            norma,
            List.of(
                regla(
                    norma, "Regla informativa 1", Optional.empty(), ClasificacionRegla.INFORMATIVA),
                regla(
                    norma,
                    "Regla informativa 2",
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA)));

    var resultado = ValidadorDeIncorporacion.validar(incorporacion);

    assertThat(resultado).isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Valida.class);
    assertThat(
            ((ValidadorDeIncorporacion.ResultadoDeValidacion.Valida) resultado)
                .ningunPlanQuedaDesactualizado())
        .isTrue();
  }

  @Test
  void uc07_ext5a_unaReglaDelPlanNoInformaQueNingunPlanQuedaDesactualizado() {
    NormaLeida norma = norma(false);
    IncorporacionLeida incorporacion =
        new IncorporacionLeida(
            PROGRAMA,
            norma,
            List.of(regla(norma, "Regla del plan", Optional.empty(), ClasificacionRegla.DEL_PLAN)));

    var resultado = ValidadorDeIncorporacion.validar(incorporacion);

    assertThat(resultado).isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Valida.class);
    assertThat(
            ((ValidadorDeIncorporacion.ResultadoDeValidacion.Valida) resultado)
                .ningunPlanQuedaDesactualizado())
        .isFalse();
  }

  private static ValidadorDeIncorporacion.ResultadoDeValidacion validarArchivo(String ruta) {
    try (InputStream entrada = ValidadorDeIncorporacionTest.class.getResourceAsStream(ruta)) {
      assertThat(entrada).as("archivo de prueba %s", ruta).isNotNull();
      return ValidadorDeIncorporacion.validar(IncorporacionJsonReader.leer(entrada));
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
  }

  private static NormaLeida norma(boolean temporal) {
    return new NormaLeida(
        NormaId.nuevo(),
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico"),
        TipoDeEfecto.BASE,
        LocalDate.of(2022, 5, 25),
        new Vigencia(
            new Momento.EnPeriodo(new PeriodoAcademico(2022, ParteDelAnio.SEGUNDO)),
            temporal
                ? Optional.of(
                    new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)))
                : Optional.empty()),
        Optional.empty(),
        new AlcanceLeida(
            TipoAlcance.PROGRAMAS, Set.of(), Set.of("2541"), Set.of(), "Aplica al programa."));
  }

  private static Regla regla(
      NormaLeida norma,
      String objeto,
      Optional<Vigencia> vigenciaPropia,
      ClasificacionRegla clasificacion) {
    return Regla.registrar(
        objeto,
        "Enunciado de " + objeto,
        new Fuente(norma.id(), "art. 1"),
        vigenciaPropia,
        clasificacion,
        EstadoRegla.VIGENTE,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }
}
