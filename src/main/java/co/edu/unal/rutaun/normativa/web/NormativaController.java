package co.edu.unal.rutaun.normativa.web;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.aplicacion.ConsultaDeReglasOrchestrator;
import co.edu.unal.rutaun.normativa.aplicacion.ConsultaNoDisponibleException;
import co.edu.unal.rutaun.normativa.aplicacion.ReglasDelPrograma;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Páginas públicas de UC06 (Consultar regla del programa y su acuerdo): la lista de programas
 * cargados (paso 2) y, al elegir uno, la página de sus reglas (paso 4). No exige sesión. Pide todo
 * al orquestador del caso de uso; si la aplicación no tiene aún la fachada de consulta, las páginas
 * responden 503 y lo dicen.
 */
@Controller
class NormativaController {

  private final ConsultaDeReglasOrchestrator orquestador;

  NormativaController(ConsultaDeReglasOrchestrator orquestador) {
    this.orquestador = orquestador;
  }

  /** Lista los programas cargados para que el visitante elija uno (UC06 paso 2). */
  @GetMapping("/programas")
  ModelAndView programas() {
    return new ModelAndView("publico/programas", "programas", orquestador.programas());
  }

  /**
   * Página de las reglas del programa elegido, agrupadas por categoría, con la versión del
   * consolidado y su fecha (UC06 paso 4). Si el consolidado no tiene reglas, lo dice (ext. 4a).
   * Responde 404 si el programa no está cargado o no tiene una versión vigente del consolidado.
   */
  @GetMapping("/programas/{id}/reglas")
  ModelAndView reglas(@PathVariable UUID id) {
    return switch (orquestador.reglasDelPrograma(new ProgramaId(id))) {
      case ReglasDelPrograma.Consultadas consultadas -> {
        ModelAndView vista = new ModelAndView("publico/reglas");
        vista.addObject("programa", consultadas.programa());
        vista.addObject("consolidado", consultadas.consolidado());
        yield vista;
      }
      case ReglasDelPrograma.ProgramaNoCargado noCargado ->
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "programa no cargado");
      case ReglasDelPrograma.SinVersionVigente sinVersion ->
          throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "el programa no tiene una versión vigente");
    };
  }

  /** Dice que la consulta no está disponible, con estado 503. */
  @ExceptionHandler(ConsultaNoDisponibleException.class)
  ModelAndView noDisponible() {
    ModelAndView vista = new ModelAndView("publico/no-disponible");
    vista.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
    return vista;
  }
}
