package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.BusquedaDeReglas;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Orquesta UC06 (Consultar regla del programa y su acuerdo) entre la página y la fachada de
 * consulta: lista los programas, pide las reglas de uno y, si su consolidado vigente no tiene
 * reglas (ext. 4a), publica el incidente. Lo publica el orquestador, y no la fachada ni la página,
 * porque no hay ningún agregado que guardar (AGENTS.md, «Capas y módulos»).
 */
@Service
public class ConsultaDeReglasOrchestrator {

  private static final String CASO_DE_USO = "UC06";
  private static final String PASO = "ext. 4a";
  private static final String CAUSA =
      "El consolidado vigente no tiene reglas cargadas para el programa";

  private final ConsultaNormativaFacade consulta;
  private final DomainEventPublisher eventos;

  /**
   * Crea el orquestador.
   *
   * @param consulta fachada de consulta de Normativa
   * @param eventos puerto por el que se publica el incidente
   */
  public ConsultaDeReglasOrchestrator(
      ConsultaNormativaFacade consulta, DomainEventPublisher eventos) {
    this.consulta = ArgumentChecks.obligatorio(consulta, "consulta");
    this.eventos = ArgumentChecks.obligatorio(eventos, "eventos");
  }

  /**
   * Los programas cargados (UC06 paso 2).
   *
   * @return los programas cargados; vacía si no hay ninguno
   */
  public List<ProgramaVista> programas() {
    return consulta.programas();
  }

  /**
   * Las reglas del programa (UC06 paso 4). Si su consolidado vigente no tiene reglas (ext. 4a),
   * publica el incidente con el programa y la versión, y devuelve igualmente el consolidado, para
   * que la página lo diga.
   *
   * @param id identificador del programa
   * @return el programa y su consolidado, o la razón por la que no hay nada que mostrar
   */
  public ReglasDelPrograma reglasDelPrograma(ProgramaId id) {
    return reglasDelPrograma(id, BusquedaDeReglas.TODAS);
  }

  /**
   * Las reglas del programa que cumplen la búsqueda (UC06 paso 6). Si ninguna la cumple, mira el
   * consolidado sin filtrar: si tiene reglas, la búsqueda no encontró nada (ext. 5a) y no publica
   * ningún incidente; si tampoco las tiene, el problema es del consolidado (ext. 4a) y publica el
   * incidente, lo mismo que sin búsqueda. En ambos casos devuelve el consolidado sin reglas y dice
   * cuál de las dos situaciones es, para que la página lo diga.
   *
   * @param id identificador del programa
   * @param busqueda texto y filtros; {@link BusquedaDeReglas#TODAS} para el listado normal
   * @return el programa y su consolidado con las reglas que cumplen la búsqueda, o la razón por la
   *     que no hay nada que mostrar
   */
  public ReglasDelPrograma reglasDelPrograma(ProgramaId id, BusquedaDeReglas busqueda) {
    ArgumentChecks.obligatorio(busqueda, "busqueda");
    Optional<ProgramaVista> programa = consulta.programa(id);
    if (programa.isEmpty()) {
      return new ReglasDelPrograma.ProgramaNoCargado();
    }
    Optional<ConsolidadoVista> consolidado = consulta.buscarReglas(id, busqueda);
    if (consolidado.isEmpty()) {
      return new ReglasDelPrograma.SinVersionVigente(programa.get());
    }
    boolean consolidadoSinReglas = consolidado.get().sinReglas() && esVacioSinFiltrar(id, busqueda);
    if (consolidadoSinReglas) {
      eventos.publicar(
          new IncidenteDePrograma(
              CASO_DE_USO, PASO, id, consolidado.get().version(), CAUSA, List.of()));
    }
    return new ReglasDelPrograma.Consultadas(
        programa.get(), consolidado.get(), consolidadoSinReglas);
  }

  private boolean esVacioSinFiltrar(ProgramaId id, BusquedaDeReglas busqueda) {
    return busqueda.esVacia()
        || consulta.consolidadoVigente(id).map(ConsolidadoVista::sinReglas).orElse(false);
  }

  /**
   * Los nombres de las categorías de reglas, en el orden del glosario, para el filtro de la página.
   *
   * @return los nombres de las categorías
   */
  public List<String> categoriasDeReglas() {
    return consulta.categoriasDeReglas();
  }

  /**
   * Una regla del programa, completa (UC06 paso 8).
   *
   * @param programa identificador del programa
   * @param regla identificador de la regla
   * @return la regla y su programa; vacío si el programa no está cargado o la regla no está en el
   *     consolidado vigente
   */
  public Optional<ReglaDelPrograma> reglaDelPrograma(ProgramaId programa, ReglaId regla) {
    return consulta
        .programa(programa)
        .flatMap(
            vista ->
                consulta
                    .regla(programa, regla)
                    .map(completa -> new ReglaDelPrograma(vista, completa)));
  }
}
