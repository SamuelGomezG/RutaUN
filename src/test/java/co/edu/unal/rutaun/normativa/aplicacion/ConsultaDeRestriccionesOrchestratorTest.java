package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ExpresionVista;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.RestriccionVista;
import co.edu.unal.rutaun.normativa.RestriccionesDeMateriaVista;
import co.edu.unal.rutaun.normativa.TipoDeRestriccion;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Orquestador de UC05 (UC05-HU01, #27): publica un incidente por cada restricción sin fuente (ext.
 * 4d, GM 2), y solo por ellas. Usa una fachada simulada.
 */
class ConsultaDeRestriccionesOrchestratorTest {

  private static final ProgramaId SISTEMAS = new ProgramaId(new UUID(0, 1));
  private static final CodigoMateria ESTRUCTURAS = new CodigoMateria("2016699");
  private static final ReglaId CON_FUENTE = new ReglaId(new UUID(0, 21));
  private static final ReglaId SIN_FUENTE = new ReglaId(new UUID(0, 22));
  private static final ProgramaVista PROGRAMA =
      new ProgramaVista(SISTEMAS, "Ingeniería de Sistemas y Computación", "2879", "Ingeniería");
  private static final String CAUSA = "Una regla que se muestra no tiene fuente registrada";

  /** Guarda los eventos que se publican, para comprobarlos. */
  private static final class RegistroDeEventos implements DomainEventPublisher {

    private final List<EventoDeDominio> publicados = new ArrayList<>();

    @Override
    public void publicar(EventoDeDominio evento) {
      publicados.add(evento);
    }
  }

  private final RegistroDeEventos eventos = new RegistroDeEventos();
  private final ConsultaNormativaFacade consulta = mock(ConsultaNormativaFacade.class);
  private final ConsultaDeRestriccionesOrchestrator orquestador =
      new ConsultaDeRestriccionesOrchestrator(consulta, eventos);

  private static RestriccionVista restriccion(ReglaId regla, Optional<FuenteVista> fuente) {
    return new RestriccionVista(
        regla,
        TipoDeRestriccion.PRERREQUISITO,
        "Prerrequisito de Estructuras de Datos: Programación Orientada a Objetos.",
        new ExpresionVista.Materia(
            new CodigoMateria("2016375"), "Programación Orientada a Objetos"),
        fuente,
        EstadoRegla.VIGENTE,
        false,
        Optional.of(LocalDate.of(2023, 12, 12)),
        Optional.empty());
  }

  private static Optional<FuenteVista> fuente() {
    return Optional.of(
        new FuenteVista(
            new NormaVista(TipoNorma.ACUERDO, "11", 2023, "Consejo de Facultad de Ingeniería"),
            "art. 2",
            Optional.empty()));
  }

  private void laMateriaExige(RestriccionVista... restricciones) {
    when(consulta.programa(SISTEMAS)).thenReturn(Optional.of(PROGRAMA));
    when(consulta.restriccionesDe(SISTEMAS, ESTRUCTURAS))
        .thenReturn(
            Optional.of(
                new RestriccionesDeMateriaVista(
                    SISTEMAS,
                    3,
                    LocalDate.of(2026, 10, 15),
                    ESTRUCTURAS,
                    "Estructuras de Datos",
                    List.of(restricciones))));
  }

  @Test
  void uc05_ext4d_unaRestriccionSinFuentePublicaUnIncidenteConProgramaVersionCausaYElementos() {
    laMateriaExige(restriccion(SIN_FUENTE, Optional.empty()));

    orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(eventos.publicados)
        .containsExactly(
            new IncidenteDePrograma(
                "UC05",
                "ext. 4d",
                SISTEMAS,
                3,
                CAUSA,
                List.of("materia 2016699", "regla " + SIN_FUENTE.valor())));
  }

  @Test
  void uc05_ext4d_laRestriccionSinFuenteSeDevuelveParaQueLaPaginaLaMuestre() {
    laMateriaExige(restriccion(SIN_FUENTE, Optional.empty()));

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado).isInstanceOf(RestriccionesDeLaMateria.Explicadas.class);
    assertThat(((RestriccionesDeLaMateria.Explicadas) resultado).restricciones().restricciones())
        .extracting(RestriccionVista::regla)
        .containsExactly(SIN_FUENTE);
  }

  @Test
  void uc05_ext4d_soloPublicaElIncidenteDeLasRestriccionesSinFuente() {
    laMateriaExige(restriccion(CON_FUENTE, fuente()), restriccion(SIN_FUENTE, Optional.empty()));

    orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(eventos.publicados)
        .singleElement()
        .isInstanceOfSatisfying(
            IncidenteDePrograma.class,
            incidente -> assertThat(incidente.elementos()).contains("regla " + SIN_FUENTE.valor()));
  }

  @Test
  void uc05_ext4d_conFuenteNoPublicaNingunIncidente() {
    laMateriaExige(restriccion(CON_FUENTE, fuente()));

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado).isInstanceOf(RestriccionesDeLaMateria.Explicadas.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc05_ext3a_unaMateriaSinRestriccionesNoPublicaNingunIncidente() {
    laMateriaExige();

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado).isInstanceOf(RestriccionesDeLaMateria.Explicadas.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc05_precondicion1_unProgramaNoCargadoNoPublicaNingunIncidente() {
    when(consulta.programa(SISTEMAS)).thenReturn(Optional.empty());

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado).isInstanceOf(RestriccionesDeLaMateria.ProgramaNoCargado.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc05_precondicion1_unProgramaSinVersionVigenteNoPublicaNingunIncidente() {
    when(consulta.programa(SISTEMAS)).thenReturn(Optional.of(PROGRAMA));
    when(consulta.restriccionesDe(SISTEMAS, ESTRUCTURAS)).thenReturn(Optional.empty());
    when(consulta.consolidadoVigente(SISTEMAS)).thenReturn(Optional.empty());

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado).isEqualTo(new RestriccionesDeLaMateria.SinVersionVigente(PROGRAMA));
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc05_paso2_unaMateriaQueNoEstaEnElConsolidadoNoPublicaNingunIncidente() {
    when(consulta.programa(SISTEMAS)).thenReturn(Optional.of(PROGRAMA));
    when(consulta.restriccionesDe(SISTEMAS, ESTRUCTURAS)).thenReturn(Optional.empty());
    when(consulta.consolidadoVigente(SISTEMAS))
        .thenReturn(
            Optional.of(new ConsolidadoVista(SISTEMAS, 3, LocalDate.of(2026, 10, 15), Map.of())));

    RestriccionesDeLaMateria resultado = orquestador.restriccionesDe(SISTEMAS, ESTRUCTURAS);

    assertThat(resultado)
        .isEqualTo(new RestriccionesDeLaMateria.MateriaNoEncontrada(PROGRAMA, ESTRUCTURAS));
    assertThat(eventos.publicados).isEmpty();
  }
}
