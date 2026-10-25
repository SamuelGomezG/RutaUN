package co.edu.unal.rutaun.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Sirve la página de inicio del esqueleto. */
@Controller
class InicioController {

  @GetMapping("/")
  String inicio() {
    return "inicio";
  }
}
