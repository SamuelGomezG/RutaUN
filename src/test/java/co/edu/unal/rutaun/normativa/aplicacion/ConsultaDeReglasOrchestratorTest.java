package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.BusquedaDeReglas;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaCompletaVista;
import co.edu.unal.rutaun.normativa.RestriccionesDeMateriaVista;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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

  /**
   * Un programa cuyo consolidado vigente no tiene reglas que rijan, pero sí una que ya no rige:
   * solo las pide quien filtra por ese estado.
   */
  private static final class SoloReglasQueYaNoRigen implements ConsultaNormativaFacade {

    private final ConsultaNormativaFacade base = ConsultaNormativaFalsa.conReglas();
    private final ConsultaNormativaFacade vacia = ConsultaNormativaFalsa.sinReglas();

    @Override
    public List<ProgramaVista> programas() {
      return base.programas();
    }

    @Override
    public Optional<ProgramaVista> programa(ProgramaId id) {
      return base.programa(id);
    }

    @Override
    public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
      return vacia.consolidadoVigente(id);
    }

    @Override
    public Optional<ConsolidadoVista> buscarReglas(ProgramaId id, BusquedaDeReglas busqueda) {
      return busqueda.estado().filter(EstadoRegla.YA_NO_RIGE::equals).isPresent()
          ? base.buscarReglas(id, busqueda)
          : vacia.buscarReglas(id, busqueda);
    }

    @Override
    public List<String> categoriasDeReglas() {
      return base.categoriasDeReglas();
    }

    @Override
    public Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId regla) {
      return Optional.empty();
    }
  }

  private final RegistroDeEventos eventos = new RegistroDeEventos();

  private ConsultaDeReglasOrchestrator orquestador(ConsultaNormativaFacade f) {
    return new ConsultaDeReglasOrchestrator(f, eventos);
  }

  @Test
  void uc06_ext4a_sinReglasPublicaUnIncidenteConProgramaVersionYCausa() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

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
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.Consultadas.class);
    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidado().sinReglas()).isTrue();
  }

  @Test
  void uc06_ext4a_conReglasNoPublicaNingunIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.Consultadas.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_ext4a_unProgramaNoCargadoNoPublicaNingunIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

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
          public Optional<ConsolidadoVista> buscarReglas(ProgramaId id, BusquedaDeReglas busqueda) {
            return consolidadoVigente(id);
          }

          @Override
          public List<String> categoriasDeReglas() {
            return List.of();
          }

          @Override
          public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
            return Optional.empty();
          }

          @Override
          public Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId regla) {
            return Optional.empty();
          }

          @Override
          public Optional<RestriccionesDeMateriaVista> restriccionesDe(
              ProgramaId programa, CodigoMateria materia) {
            return Optional.empty();
          }
        };
    var orquestador = orquestador(sinVersion);

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.SinVersionVigente.class);
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_paso6_laBusquedaPideAlaFachadaSoloLasReglasQueLaCumplen() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de("mínimo", null, null));

    ConsolidadoVista consolidado = ((ReglasDelPrograma.Consultadas) resultado).consolidado();
    assertThat(consolidado.reglasPorCategoria().values())
        .singleElement()
        .satisfies(
            reglas ->
                assertThat(reglas)
                    .singleElement()
                    .extracting(regla -> regla.id())
                    .isEqualTo(ConsultaNormativaFalsa.ID_CARGA));
  }

  @Test
  void uc06_ext5b_conElEstadoYaNoRigeSoloDevuelveLasQueYaNoRigen() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS,
            BusquedaDeReglas.de(null, null, EstadoRegla.YA_NO_RIGE));

    ConsolidadoVista consolidado = ((ReglasDelPrograma.Consultadas) resultado).consolidado();
    assertThat(consolidado.reglasPorCategoria().values().stream().flatMap(List::stream))
        .extracting(regla -> regla.estado())
        .containsOnly(EstadoRegla.YA_NO_RIGE);
  }

  @Test
  void uc06_ext5a_unaBusquedaSinResultadosNoPublicaNingunIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de("no existe", null, null));

    assertThat(resultado).isInstanceOf(ReglasDelPrograma.Consultadas.class);
    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidado().sinReglas()).isTrue();
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_ext4a_unaBusquedaConElConsolidadoSinReglasPublicaElIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de("créditos", null, null));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
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
  void uc06_ext4a_sinBusquedaElConsolidadoSinReglasSeMarcaComoTal() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado = orquestador.reglasDelPrograma(ConsultaNormativaFalsa.SISTEMAS);

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
  }

  @Test
  void uc06_ext5a_conReglasUnaBusquedaSinResultadosNoMarcaElConsolidadoSinReglas() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de("no existe", null, null));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isFalse();
  }

  @Test
  void uc06_ext4a_unaBusquedaPorCategoriaConElConsolidadoSinReglasPublicaElIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de(null, "Materias", null));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
    assertThat(eventos.publicados).hasSize(1);
  }

  @Test
  void uc06_ext4a_unaBusquedaPorEstadoConElConsolidadoSinReglasPublicaElIncidente() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de(null, null, EstadoRegla.VIGENTE));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
    assertThat(eventos.publicados).hasSize(1);
  }

  @Test
  void uc06_ext4a_pedirLasQueYaNoRigenSinQueHayaNingunaTambienEsElConsolidadoSinReglas() {
    var orquestador = orquestador(ConsultaNormativaFalsa.sinReglas());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS,
            BusquedaDeReglas.de("zzzzzz", null, EstadoRegla.YA_NO_RIGE));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
    assertThat(eventos.publicados).hasSize(1);
  }

  @Test
  void uc06_ext5a_conSoloReglasQueYaNoRigenUnaBusquedaPorEseEstadoSinResultadosNoEsUnIncidente() {
    var orquestador = orquestador(new SoloReglasQueYaNoRigen());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS,
            BusquedaDeReglas.de("zzzzzz", null, EstadoRegla.YA_NO_RIGE));

    var consultadas = (ReglasDelPrograma.Consultadas) resultado;
    assertThat(consultadas.consolidado().sinReglas()).isTrue();
    assertThat(consultadas.consolidadoSinReglas()).isFalse();
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_ext5a_pedirLasQueYaNoRigenDeUnaCategoriaSinEllasNoEsUnIncidenteSiHayEnOtra() {
    var orquestador = orquestador(new SoloReglasQueYaNoRigen());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS,
            BusquedaDeReglas.de(null, "Materias", EstadoRegla.YA_NO_RIGE));

    var consultadas = (ReglasDelPrograma.Consultadas) resultado;
    assertThat(consultadas.consolidado().sinReglas()).isTrue();
    assertThat(consultadas.consolidadoSinReglas()).isFalse();
    assertThat(eventos.publicados).isEmpty();
  }

  @Test
  void uc06_ext4a_conSoloReglasQueYaNoRigenSinElEstadoSigueSiendoElConsolidadoSinReglas() {
    var orquestador = orquestador(new SoloReglasQueYaNoRigen());

    ReglasDelPrograma resultado =
        orquestador.reglasDelPrograma(
            ConsultaNormativaFalsa.SISTEMAS, BusquedaDeReglas.de("zzzzzz", null, null));

    assertThat(((ReglasDelPrograma.Consultadas) resultado).consolidadoSinReglas()).isTrue();
    assertThat(eventos.publicados).hasSize(1);
  }

  @Test
  void uc06_paso6_ofreceLasCategoriasParaFiltrar() {
    var orquestador = orquestador(ConsultaNormativaFalsa.conReglas());

    assertThat(orquestador.categoriasDeReglas())
        .contains("Materias", "Prerrequisitos y habilitación", "Otra");
  }
}
