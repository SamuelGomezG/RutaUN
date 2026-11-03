package co.edu.unal.rutaun.normativa.web;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Páginas públicas de UC06 (Consultar regla del programa y su acuerdo): la lista de programas
 * cargados (paso 2) y, al elegir uno, la página de sus reglas. No exige sesión.
 *
 * <p>Pide la fachada de consulta como {@link ObjectProvider} y no como dependencia directa, porque
 * mientras Normativa no publique su implementación real la aplicación no tiene ese bean: con una
 * dependencia directa no arrancaría. Si falta, las páginas responden 503 y lo dicen. Cuando exista
 * la implementación, este constructor pasa a recibir la fachada y se borra la rama de «no
 * disponible».
 */
@Controller
class ProgramasController {

  private final ObjectProvider<ConsultaNormativaFacade> consulta;

  ProgramasController(ObjectProvider<ConsultaNormativaFacade> consulta) {
    this.consulta = consulta;
  }

  /** Lista los programas cargados para que el visitante elija uno (UC06 paso 2). */
  @GetMapping("/programas")
  ModelAndView programas() {
    ConsultaNormativaFacade fachada = consulta.getIfAvailable();
    if (fachada == null) {
      return noDisponible();
    }
    return new ModelAndView("publico/programas", "programas", fachada.programas());
  }

  /** Página de las reglas del programa elegido; responde 404 si el programa no está cargado. */
  @GetMapping("/programas/{id}/reglas")
  ModelAndView reglas(@PathVariable UUID id) {
    ConsultaNormativaFacade fachada = consulta.getIfAvailable();
    if (fachada == null) {
      return noDisponible();
    }
    ProgramaVista programa =
        fachada
            .programa(new ProgramaId(id))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "programa no cargado"));
    return new ModelAndView("publico/reglas", "programa", programa);
  }

  private static ModelAndView noDisponible() {
    ModelAndView vista = new ModelAndView("publico/no-disponible");
    vista.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
    return vista;
  }
}
