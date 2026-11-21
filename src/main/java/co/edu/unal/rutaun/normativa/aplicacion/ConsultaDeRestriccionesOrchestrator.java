package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.RestriccionVista;
import co.edu.unal.rutaun.normativa.RestriccionesDeMateriaVista;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Orquesta UC05 (Entender restricciones de una materia) entre la página y la fachada de consulta:
 * pide lo que una materia exige y, por cada restricción que no tiene fuente (ext. 4d, GM 2),
 * publica un incidente. Lo publica el orquestador, y no la fachada ni la página, porque no hay
 * ningún agregado que guardar (AGENTS.md, «Capas y módulos»).
 */
@Service
public class ConsultaDeRestriccionesOrchestrator {

  private static final String CASO_DE_USO = "UC05";
  private static final String PASO = "ext. 4d";
  private static final String CAUSA = "Una regla que se muestra no tiene fuente registrada";

  private final ConsultaNormativaFacade consulta;
  private final DomainEventPublisher eventos;

  /**
   * Crea el orquestador.
   *
   * @param consulta fachada de consulta de Normativa
   * @param eventos puerto por el que se publican los incidentes
   */
  public ConsultaDeRestriccionesOrchestrator(
      ConsultaNormativaFacade consulta, DomainEventPublisher eventos) {
    this.consulta = ArgumentChecks.obligatorio(consulta, "consulta");
    this.eventos = ArgumentChecks.obligatorio(eventos, "eventos");
  }

  /**
   * Lo que la materia exige para cursarla (UC05 pasos 2 a 4). Por cada restricción sin fuente (ext.
   * 4d) publica un incidente con el programa, la versión, la causa y, como elementos, la materia y
   * la regla; la restricción se devuelve igualmente, para que la página la muestre «sin fuente
   * registrada».
   *
   * @param programa identificador del programa
   * @param materia código de la materia
   * @return lo que la materia exige, o la razón por la que no hay nada que explicar
   */
  public RestriccionesDeLaMateria restriccionesDe(ProgramaId programa, CodigoMateria materia) {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorio(materia, "materia");
    Optional<ProgramaVista> vistaDelPrograma = consulta.programa(programa);
    if (vistaDelPrograma.isEmpty()) {
      return new RestriccionesDeLaMateria.ProgramaNoCargado();
    }
    Optional<RestriccionesDeMateriaVista> restricciones =
        consulta.restriccionesDe(programa, materia);
    if (restricciones.isEmpty()) {
      return consulta.consolidadoVigente(programa).isEmpty()
          ? new RestriccionesDeLaMateria.SinVersionVigente(vistaDelPrograma.get())
          : new RestriccionesDeLaMateria.MateriaNoEncontrada(vistaDelPrograma.get(), materia);
    }
    for (RestriccionVista restriccion : restricciones.get().restricciones()) {
      if (restriccion.fuente().isEmpty()) {
        eventos.publicar(
            new IncidenteDePrograma(
                CASO_DE_USO,
                PASO,
                programa,
                restricciones.get().version(),
                CAUSA,
                List.of("materia " + materia.valor(), "regla " + restriccion.regla().valor())));
      }
    }
    return new RestriccionesDeLaMateria.Explicadas(vistaDelPrograma.get(), restricciones.get());
  }
}
