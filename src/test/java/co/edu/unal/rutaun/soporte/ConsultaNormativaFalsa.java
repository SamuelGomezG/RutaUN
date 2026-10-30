package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoNorma;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fachada de consulta falsa, con datos fijos y sintéticos, para probar la vista sin persistencia.
 * Se crea con Ingeniería de Sistemas y Computación y tres reglas, o con ese programa sin reglas
 * (UC06 ext. 4a).
 */
public final class ConsultaNormativaFalsa implements ConsultaNormativaFacade {

  /** Identificador del programa de prueba. */
  public static final ProgramaId SISTEMAS =
      new ProgramaId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  private static final ProgramaVista PROGRAMA =
      new ProgramaVista(
          SISTEMAS, "Ingeniería de Sistemas y Computación", "0000", "Facultad de Ingeniería");

  private static final ConsolidadoVista CONSOLIDADO =
      new ConsolidadoVista(
          SISTEMAS,
          1,
          LocalDate.of(2026, 10, 1),
          Map.of(
              "Carga por periodo",
              List.of(
                  new ReglaVista(
                      "Regla de prueba: el mínimo es de 10 créditos por periodo.",
                      Optional.of(fuente("1", "art. 1")),
                      EstadoRegla.VIGENTE,
                      false)),
              "Prerrequisito",
              List.of(
                  new ReglaVista(
                      "Regla de prueba: la materia B exige haber aprobado la materia A.",
                      Optional.of(fuente("1", "art. 2")),
                      EstadoRegla.VIGENTE,
                      false)),
              "Informativa",
              List.of(
                  new ReglaVista(
                      "Regla de prueba: medida temporal para el periodo 2026-2.",
                      Optional.of(fuente("2", "art. 1")),
                      EstadoRegla.VIGENTE,
                      true))));

  private final boolean conReglas;

  private ConsultaNormativaFalsa(boolean conReglas) {
    this.conReglas = conReglas;
  }

  private static FuenteVista fuente(String numero, String articulo) {
    return new FuenteVista(
        new NormaVista(
            TipoNorma.ACUERDO, numero, 2026, "Consejo de Facultad de Ingeniería (de prueba)"),
        articulo,
        Optional.empty());
  }

  /** La fachada con el programa de prueba y sus tres reglas. */
  public static ConsultaNormativaFalsa conReglas() {
    return new ConsultaNormativaFalsa(true);
  }

  /** La fachada con el programa de prueba y sin reglas cargadas (UC06 ext. 4a). */
  public static ConsultaNormativaFalsa sinReglas() {
    return new ConsultaNormativaFalsa(false);
  }

  @Override
  public List<ProgramaVista> programas() {
    return List.of(PROGRAMA);
  }

  @Override
  public Optional<ProgramaVista> programa(ProgramaId id) {
    return SISTEMAS.equals(id) ? Optional.of(PROGRAMA) : Optional.empty();
  }

  @Override
  public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
    return conReglas && SISTEMAS.equals(id) ? Optional.of(CONSOLIDADO) : Optional.empty();
  }
}
