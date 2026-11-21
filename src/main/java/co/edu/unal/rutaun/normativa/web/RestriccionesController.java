package co.edu.unal.rutaun.normativa.web;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.aplicacion.ConsultaDeRestriccionesOrchestrator;
import co.edu.unal.rutaun.normativa.aplicacion.RestriccionesDeLaMateria;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Página pública de UC05 (Entender restricciones de una materia): lo que una materia de un programa
 * exige para cursarla, con la fuente, la fecha de aplicación y la vigencia de cada restricción
 * (pasos 1 a 4). No exige sesión. Pide todo al orquestador del caso de uso.
 */
@Controller
class RestriccionesController {

  private final ConsultaDeRestriccionesOrchestrator orquestador;

  RestriccionesController(ConsultaDeRestriccionesOrchestrator orquestador) {
    this.orquestador = orquestador;
  }

  /**
   * Las restricciones de la materia en el consolidado vigente del programa (UC05 paso 4). Si la
   * materia no exige nada, lo dice y recuerda que eso no significa que esté ofrecida ni que haya
   * cupo (ext. 3a); una restricción sin fuente se muestra «sin fuente registrada» (ext. 4d).
   * Responde 400 si el código de la materia está vacío y 404 si el programa no está cargado, no
   * tiene una versión vigente del consolidado o la materia no figura en ella.
   */
  @GetMapping("/programas/{programa}/materias/{codigo}/restricciones")
  ModelAndView restricciones(@PathVariable UUID programa, @PathVariable String codigo) {
    RestriccionesDeLaMateria resultado =
        orquestador.restriccionesDe(new ProgramaId(programa), codigoDeLaMateria(codigo));
    return switch (resultado) {
      case RestriccionesDeLaMateria.Explicadas explicadas -> {
        ModelAndView vista = new ModelAndView("publico/restricciones");
        vista.addObject("programa", explicadas.programa());
        vista.addObject("restricciones", explicadas.restricciones());
        yield vista;
      }
      case RestriccionesDeLaMateria.ProgramaNoCargado noCargado ->
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "programa no cargado");
      case RestriccionesDeLaMateria.SinVersionVigente sinVersion ->
          throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "el programa no tiene una versión vigente");
      case RestriccionesDeLaMateria.MateriaNoEncontrada noEncontrada ->
          throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "la materia no figura en el consolidado vigente");
    };
  }

  /** El código de la materia; si viene vacío, la petición es inválida. */
  private static CodigoMateria codigoDeLaMateria(String codigo) {
    try {
      return new CodigoMateria(codigo);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "el código de la materia no puede estar vacío", e);
    }
  }
}
