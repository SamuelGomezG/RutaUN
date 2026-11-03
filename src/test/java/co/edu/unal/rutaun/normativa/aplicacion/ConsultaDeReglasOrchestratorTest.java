package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * Orquestador de UC06 (UC06-HU01-T3, #103): publica el incidente de la ext. 4a cuando el
 * consolidado vigente no tiene reglas, y solo entonces. Usa {@link ConsultaNormativaFalsa}.
 */
class ConsultaDeReglasOrchestratorTest {

  /** Guarda los eventos que se publican, para comprobarlos. */
  private static final class RegistroDeEventos implements DomainEventPublisher {

    private final List<EventoDeDominio> publicados = new ArrayList<>();

    @Override
    public void publicar(EventoDeDominio evento) {
      publicados.add(evento);
    }
  }

  private final RegistroDeEventos eventos = new RegistroDeEventos();

  private static ObjectProvider<ConsultaNormativaFacade> conFachada(ConsultaNormativaFacade f) {
    StaticListableBeanFactory fabrica = new StaticListableBeanFactory();
    fabrica.addBean("consulta", f);
    return fabrica.getBeanProvider(ConsultaNormativaFacade.class);
  }

  private static ObjectProvider<ConsultaNormativaFacade> sinFachada() {
    return new StaticListableBeanFactory().getBeanProvider(ConsultaNormativaFacade.class);
  }

  private ConsultaDeReglasOrchestrator orquestador(ObjectProvider<ConsultaNormativaFacade> f) {
    return new ConsultaDeReglasOrchestrator(f, eventos);
  }

  @Test
  void uc06_ext4a_sinReglasPublicaUnIncidenteConProgramaVersionYCausa() {
    var orquestador = orquestador(conFachada(ConsultaNormativaFalsa.sinReglas()));

    orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(eventos.publicados)
        .containsExactly(
            new IncidenteDePrograma(
                "UC06",
                "ext. 4a",
                ConsultaNormativaFalsa.SISTEMAS,
                1,
                "El consolidado vigente no tiene reglas cargadas para el programa",
                List.of()));
  }

  @Test
  void uc06_ext4a_sinReglasDevuelveElConsolidadoParaQueLaPaginaLoDiga() {
    var orquestador = orquestador(conFachada(ConsultaNormativaFalsa.sinReglas()));

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.Consultadas.class);
    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidado().sinReglas()).isTrue();
  }

  @Test
  void uc06_ext4a_conReglasNoPublicaNingunIncidente() {
    var orquestador = orquestador(conFachada(ConsultaNormativaFalsa.conReglas()));

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.Consultadas.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_ext4a_unProgramaNoCargadoNoPublicaNingunIncidente() {
    var orquestador = orquestador(conFachada(ConsultaNormativaFalsa.sinReglas()));

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(new ProgramaId(UUID.randomUUID()));

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.ProgramaNoCargado.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_paso4_unProgramaSinVersionVigenteNoPublicaNingunIncidente() {
    ConsultaNormativaFacade sinVersion =
        new ConsultaNormativaFacade() {
          @Override
          public List<ProgramaVista> programas() {
            return ConsultaNormativaFalsa.conReglas().programas();
          }

          @Override
          public Optional<ProgramaVista> programa(ProgramaId id) {
            return ConsultaNormativaFalsa.conReglas().programa(id);
          }

          @Override
          public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
            return Optional.empty();
          }
        };
    var orquestador = orquestador(conFachada(sinVersion));

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.SinVersionVigente.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_hu01_sinFachadaLaConsultaNoEstaDisponibleYNoPublicaIncidentes() {
    var orquestador = orquestador(sinFachada());

    assertThatThrownBy(orquestador::programas).isInstanceOf(ConsultaNoDisponibleException.class);
    assertThatThrownBy(() -> orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS))
        .isInstanceOf(ConsultaNoDisponibleException.class);
    assertThat(eventos.publicados).isEmpty();
  }
}
