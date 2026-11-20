package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.EslabonVista;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaCompletaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeCambio;
import co.edu.unal.rutaun.normativa.TipoNorma;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fachada de consulta falsa, con datos fijos y sintéticos, para probar la vista sin persistencia.
 * Se crea con Ingeniería de Sistemas y Computación y tres reglas, o con ese programa y un
 * consolidado vigente sin reglas (UC06 ext. 4a).
 */
public final class ConsultaNormativaFalsa implements ConsultaNormativaFacade {

  /** Identificador del programa de prueba. */
  public static final ProgramaId SISTEMAS =
      new ProgramaId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  /** Regla de carga de prueba: una sola norma y sin enlace a Régimen Legal. */
  public static final ReglaId ID_CARGA = new ReglaId(new UUID(0, 11));

  /** Regla de prerrequisito de prueba: una sola norma y con enlace a Régimen Legal. */
  public static final ReglaId ID_PRERREQUISITO = new ReglaId(new UUID(0, 12));

  /** Regla temporal de prueba: la expidió un acuerdo y la modifica otro, que es el que rige. */
  public static final ReglaId ID_TEMPORAL = new ReglaId(new UUID(0, 13));

  /** Enlace de prueba a Régimen Legal. */
  public static final String ENLACE = "https://regimen-legal.example.org/prueba-regla-12";

  private static final ProgramaVista PROGRAMA =
      new ProgramaVista(
          SISTEMAS, "Ingeniería de Sistemas y Computación", "0000", "Facultad de Ingeniería");

  private static final ConsolidadoVista CONSOLIDADO = consolidado();

  private static final ConsolidadoVista CONSOLIDADO_SIN_REGLAS =
      new ConsolidadoVista(SISTEMAS, 1, LocalDate.of(2026, 10, 1), Map.of());

  private final boolean conReglas;

  private ConsultaNormativaFalsa(boolean conReglas) {
    this.conReglas = conReglas;
  }

  private static ConsolidadoVista consolidado() {
    Map<String, List<ReglaVista>> porCategoria = new LinkedHashMap<>();
    porCategoria.put(
        "Carga mínima o máxima por periodo",
        List.of(
            new ReglaVista(
                ID_CARGA,
                "Regla de prueba: el mínimo es de 10 créditos por periodo.",
                Optional.of(fuente("1", "art. 1")),
                EstadoRegla.VIGENTE,
                false,
                ClasificacionRegla.DEL_PLAN)));
    porCategoria.put(
        "Prerrequisitos y habilitación",
        List.of(
            new ReglaVista(
                ID_PRERREQUISITO,
                "Regla de prueba: la materia B exige haber aprobado la materia A.",
                Optional.of(fuente("1", "art. 2")),
                EstadoRegla.VIGENTE,
                false,
                ClasificacionRegla.DEL_PLAN)));
    porCategoria.put(
        "Otra",
        List.of(
            new ReglaVista(
                ID_TEMPORAL,
                "Regla de prueba: medida temporal para el periodo 2026-2.",
                Optional.of(fuente("2", "art. 1")),
                EstadoRegla.VIGENTE,
                true,
                ClasificacionRegla.INFORMATIVA)));
    return new ConsolidadoVista(SISTEMAS, 1, LocalDate.of(2026, 10, 1), porCategoria);
  }

  private static ReglaCompletaVista completa(
      ReglaVista regla, Optional<String> enlace, List<EslabonVista> cadena) {
    return new ReglaCompletaVista(
        regla.id(),
        regla.enunciado(),
        regla.fuente().map(f -> new FuenteVista(f.norma(), f.articulo(), enlace)),
        regla.estado(),
        regla.temporal(),
        regla.clasificacion(),
        Optional.of(LocalDate.of(2026, 1, 15)),
        regla.temporal() ? Optional.of(LocalDate.of(2026, 12, 31)) : Optional.empty(),
        Optional.of(TipoAlcance.PROGRAMAS),
        Optional.of("Ingeniería de Sistemas y Computación (de prueba)"),
        cadena);
  }

  private static EslabonVista eslabon(
      Optional<TipoDeCambio> cambio, String numero, int mes, boolean rige) {
    return new EslabonVista(cambio, fuente(numero, "art. 1"), LocalDate.of(2026, mes, 1), rige);
  }

  private static FuenteVista fuente(String numero, String articulo) {
    return new FuenteVista(
        new NormaVista(
            TipoNorma.ACUERDO, numero, 2026, "Consejo de Facultad de Ingeniería (de prueba)"),
        articulo,
        Optional.empty());
  }

  private static Optional<ReglaCompletaVista> reglaCompleta(ReglaId id) {
    return CONSOLIDADO.reglasPorCategoria().values().stream()
        .flatMap(List::stream)
        .filter(regla -> regla.id().equals(id))
        .findFirst()
        .map(
            regla -> {
              if (ID_CARGA.equals(id)) {
                return completa(regla, Optional.empty(), List.of());
              }
              if (ID_PRERREQUISITO.equals(id)) {
                return completa(regla, Optional.of(ENLACE), List.of());
              }
              return completa(
                  regla,
                  Optional.empty(),
                  List.of(
                      eslabon(Optional.empty(), "2", 1, false),
                      eslabon(Optional.of(TipoDeCambio.MODIFICA), "3", 6, true)));
            });
  }

  /** La fachada con el programa de prueba y sus tres reglas. */
  public static ConsultaNormativaFalsa conReglas() {
    return new ConsultaNormativaFalsa(true);
  }

  /**
   * La fachada con el programa de prueba y una versión vigente del consolidado sin reglas cargadas
   * (UC06 ext. 4a).
   */
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
    if (!SISTEMAS.equals(id)) {
      return Optional.empty();
    }
    return Optional.of(conReglas ? CONSOLIDADO : CONSOLIDADO_SIN_REGLAS);
  }

  @Override
  public Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId regla) {
    if (!SISTEMAS.equals(programa) || !conReglas) {
      return Optional.empty();
    }
    return reglaCompleta(regla);
  }
}
