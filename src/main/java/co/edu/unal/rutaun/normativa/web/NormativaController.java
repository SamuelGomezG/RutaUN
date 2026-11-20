package co.edu.unal.rutaun.normativa.web;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.aplicacion.ConsultaDeReglasOrchestrator;
import co.edu.unal.rutaun.normativa.aplicacion.ReglaDelPrograma;
import co.edu.unal.rutaun.normativa.aplicacion.ReglasDelPrograma;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Páginas públicas de UC06 (Consultar regla del programa y su acuerdo): la lista de programas
 * cargados (paso 2), al elegir uno la página de sus reglas (paso 4) y, al elegir una regla, la
 * regla completa (paso 8). No exige sesión. Pide todo al orquestador del caso de uso.
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

  /**
   * La regla completa: su fuente, su vigencia, su alcance y la cadena de normas que la han afectado
   * (UC06 paso 8). Responde 404 si el programa no está cargado o la regla no está en su consolidado
   * vigente. Abre también las que ya no rigen, con ese estado.
   */
  @GetMapping("/programas/{programa}/reglas/{regla}")
  ModelAndView regla(@PathVariable UUID programa, @PathVariable UUID regla) {
    ReglaDelPrograma consultada =
        orquestador
            .reglaDelPrograma(new ProgramaId(programa), new ReglaId(regla))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "regla no disponible"));
    ModelAndView vista = new ModelAndView("publico/regla");
    vista.addObject("programa", consultada.programa());
    vista.addObject("regla", consultada.regla());
    return vista;
  }
}
