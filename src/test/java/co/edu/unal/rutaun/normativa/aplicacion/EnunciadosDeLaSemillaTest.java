package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.soporte.SoporteDeSemilla;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class EnunciadosDeLaSemillaTest {

  @Test
  void s17_elEnunciadoDeUnaReglaNoNombraSuMateriaExterna() throws IOException {
    int reglasConMateriaExterna = 0;

    for (Resource recurso : SoporteDeSemilla.recursosDeNormas()) {
      IncorporacionJsonReader.ResultadoDeLectura resultado =
          IncorporacionJsonReader.leer(recurso.getInputStream());
      if (!(resultado instanceof IncorporacionJsonReader.ResultadoDeLectura.Leida leida)) {
        throw new AssertionError("la semilla no se pudo leer: " + resultado);
      }

      for (Regla regla : leida.incorporacion().reglas()) {
        Set<String> externas = materiasExternas(regla);
        if (externas.isEmpty()) {
          continue;
        }
        reglasConMateriaExterna++;
        String texto = normalizar(regla.enunciado() + " " + regla.objeto());
        for (String externa : externas) {
          assertThat(texto)
              .as("%s no debe nombrar la materia externa %s", regla.id(), externa)
              .doesNotContain(normalizar(externa));
        }
      }
    }

    assertThat(reglasConMateriaExterna).isEqualTo(4);
  }

  private static Set<String> materiasExternas(Regla regla) {
    return switch (regla.datos()) {
      case DatosDeRegla.DePrerrequisito datos -> externas(datos.prerrequisito().expresion());
      case DatosDeRegla.DeCorrequisito datos -> externas(datos.correquisito().expresion());
      default -> Set.of();
    };
  }

  private static Set<String> externas(ExpresionDeMaterias expresion) {
    Set<String> nombres = new HashSet<>();
    recolectar(expresion, nombres);
    return nombres;
  }

  private static void recolectar(ExpresionDeMaterias expresion, Set<String> nombres) {
    switch (expresion) {
      case ExpresionDeMaterias.MateriaExterna externa -> nombres.add(externa.nombre());
      case ExpresionDeMaterias.TodasLas todas ->
          todas.terminos().forEach(termino -> recolectar(termino, nombres));
      case ExpresionDeMaterias.AlgunaDe alguna ->
          alguna.terminos().forEach(termino -> recolectar(termino, nombres));
      case ExpresionDeMaterias.TerminoMateria termino -> {
        // Una materia del programa no es externa.
      }
    }
  }

  private static String normalizar(String texto) {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }
}
