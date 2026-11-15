package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.soporte.SoporteDeSemilla;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

/**
 * Verifica la convención de los datos de prueba de la semilla (TT-04-T4, #133).
 *
 * <p>Un archivo de datos de prueba se reconoce por su nombre, {@code datos-de-prueba-*.json}. Su
 * norma lleva «(datos de prueba)» en el órgano, y cada una de sus reglas lo lleva en el enunciado y
 * en el objeto. Los archivos de las normas reales no llevan la marca. Los datos de prueba no traen
 * reglas de estructura ni afectaciones, porque no deben mover la versión del plan de estudios.
 *
 * <p>Es una prueba unitaria, sin Spring: lee los archivos de {@code semilla/} con el lector y el
 * validador reales, así que falla rápido si alguien agrega un dato ficticio sin marcar. El
 * descubrimiento de archivos y la guarda de origen están en {@link SoporteDeSemilla}.
 */
class DatosDePruebaDeLaSemillaTest {

  private static final String MARCA = "(datos de prueba)";

  @Test
  void tt04_t4_losArchivosDeDatosDePruebaLlevanLaMarca() throws IOException {
    List<Resource> archivosDePrueba = SoporteDeSemilla.archivosDePrueba();

    assertThat(archivosDePrueba)
        .as("debe existir al menos un archivo datos-de-prueba-*.json")
        .isNotEmpty();
    for (Resource recurso : archivosDePrueba) {
      IncorporacionLeida incorporacion = leer(recurso);
      assertThat(incorporacion.norma().identificacion().organo())
          .as("%s: el órgano de la norma", recurso.getFilename())
          .contains(MARCA);
      for (Regla regla : incorporacion.reglas()) {
        assertThat(regla.enunciado())
            .as("%s: el enunciado de \"%s\"", recurso.getFilename(), regla.objeto())
            .contains(MARCA);
        assertThat(regla.objeto())
            .as("%s: el objeto de una regla", recurso.getFilename())
            .contains(MARCA);
      }
    }
  }

  @Test
  void tt04_t4_lasNormasRealesNoLlevanLaMarca() throws IOException {
    List<Resource> normasReales =
        SoporteDeSemilla.recursosDeNormas().stream()
            .filter(recurso -> !SoporteDeSemilla.esDePrueba(recurso.getFilename()))
            .toList();

    assertThat(normasReales).as("la semilla debe traer normas reales").isNotEmpty();
    for (Resource recurso : normasReales) {
      IncorporacionLeida incorporacion = leer(recurso);
      assertThat(incorporacion.norma().identificacion().organo())
          .as("%s: el órgano de la norma", recurso.getFilename())
          .doesNotContain(MARCA);
      for (Regla regla : incorporacion.reglas()) {
        assertThat(regla.enunciado())
            .as("%s: el enunciado de \"%s\"", recurso.getFilename(), regla.objeto())
            .doesNotContain(MARCA);
        assertThat(regla.objeto())
            .as("%s: el objeto de una regla", recurso.getFilename())
            .doesNotContain(MARCA);
      }
    }
  }

  @Test
  void tt04_t4_losDatosDePruebaNoTraenReglasDeEstructuraNiAfectaciones() throws IOException {
    for (Resource recurso : SoporteDeSemilla.archivosDePrueba()) {
      IncorporacionLeida incorporacion = leer(recurso);
      for (Regla regla : incorporacion.reglas()) {
        assertThat(regla.datos())
            .as(
                "%s: \"%s\" no puede ser una regla de estructura",
                recurso.getFilename(), regla.objeto())
            .isNotInstanceOfAny(
                DatosDeRegla.DeMateria.class,
                DatosDeRegla.DeAgrupacion.class,
                DatosDeRegla.DeComponente.class,
                DatosDeRegla.DePrograma.class);
        assertThat(regla.afectacion())
            .as("%s: \"%s\" no puede tener afectación", recurso.getFilename(), regla.objeto())
            .isEmpty();
      }
    }
  }

  private static IncorporacionLeida leer(Resource recurso) throws IOException {
    try (InputStream entrada = recurso.getInputStream()) {
      ValidadorDeIncorporacion.ResultadoDeValidacion resultado =
          ValidadorDeIncorporacion.validar(IncorporacionJsonReader.leer(entrada));
      assertThat(resultado)
          .as("%s debe pasar el lector y el validador", recurso.getFilename())
          .isInstanceOf(ValidadorDeIncorporacion.ResultadoDeValidacion.Valida.class);
      return ((ValidadorDeIncorporacion.ResultadoDeValidacion.Valida) resultado).incorporacion();
    }
  }
}
