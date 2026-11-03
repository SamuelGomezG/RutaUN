package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Orquesta UC06 (Consultar regla del programa y su acuerdo) entre la página y la fachada de
 * consulta: lista los programas, pide las reglas de uno y, si su consolidado vigente no tiene
 * reglas (ext. 4a), publica el incidente. Lo publica el orquestador, y no la fachada ni la página,
 * porque no hay ningún agregado que guardar (AGENTS.md, «Capas y módulos»).
 *
 * <p>Pide la fachada como {@link ObjectProvider} y no como dependencia directa, porque mientras
 * Normativa no publique su implementación real la aplicación no tiene ese bean: con una dependencia
 * directa no arrancaría. Si falta, lanza {@link ConsultaNoDisponibleException}. Cuando exista la
 * implementación, este constructor pasa a recibir la fachada y se borra esa excepción.
 */
@Service
public class ConsultaDeReglasOrchestrator {

  private static final String CASO_DE_USO = "UC06";
  private static final String PASO = "ext. 4a";
  private static final String CAUSA =
      "El consolidado vigente no tiene reglas cargadas para el programa";

  private final ObjectProvider<ConsultaNormativaFacade> consulta;
  private final DomainEventPublisher eventos;

  /**
   * Crea el orquestador.
   *
   * @param consulta proveedor de la fachada de consulta, que puede no existir todavía
   * @param eventos puerto por el que se publica el incidente
   */
  public ConsultaDeReglasOrchestrator(
      ObjectProvider<ConsultaNormativaFacade> consulta, DomainEventPublisher eventos) {
    this.consulta = ArgumentChecks.obligatorio(consulta, "consulta");
    this.eventos = ArgumentChecks.obligatorio(eventos, "eventos");
  }

  /**
   * Los programas cargados (UC06 paso 2).
   *
   * @return los programas cargados; vacía si no hay ninguno
   * @throws ConsultaNoDisponibleException si la aplicación no tiene la fachada de consulta
   */
  public List<ProgramaVista> programas() {
    return fachada().programas();
  }

  /**
   * Las reglas del programa (UC06 paso 4). Si su consolidado vigente no tiene reglas (ext. 4a),
   * publica el incidente con el programa y la versión, y devuelve igualmente el consolidado, para
   * que la página lo diga.
   *
   * @param id identificador del programa
   * @return el programa y su consolidado, o la razón por la que no hay nada que mostrar
   * @throws ConsultaNoDisponibleException si la aplicación no tiene la fachada de consulta
   */
  public ReglasDelPrograma reglasDelPrograma(ProgramaId id) {
    ConsultaNormativaFacade fachada = fachada();
    Optional<ProgramaVista> programa = fachada.programa(id);
    if (programa.isEmpty()) {
      return new ReglasDelPrograma.ProgramaNoCargado();
    }
    Optional<ConsolidadoVista> consolidado = fachada.consolidadoVigente(id);
    if (consolidado.isEmpty()) {
      return new ReglasDelPrograma.SinVersionVigente(programa.get());
    }
    if (consolidado.get().sinReglas()) {
      eventos.publicar(
          new IncidenteDePrograma(
              CASO_DE_USO, PASO, id, consolidado.get().version(), CAUSA, List.of()));
    }
    return new ReglasDelPrograma.Consultadas(programa.get(), consolidado.get());
  }

  private ConsultaNormativaFacade fachada() {
    ConsultaNormativaFacade fachada = consulta.getIfAvailable();
    if (fachada == null) {
      throw new ConsultaNoDisponibleException();
    }
    return fachada;
  }
}
