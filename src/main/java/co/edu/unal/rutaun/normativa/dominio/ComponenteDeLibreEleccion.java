package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Componente de libre elección: el estudiante lo cubre con créditos, no con materias (glosario,
 * «Libre elección»). Si la norma lista electivas, van en agrupaciones y solo se consultan: no suman
 * a lo que cubre el componente.
 *
 * @param nombre nombre del componente
 * @param exigencia créditos de libre elección que exige el plan
 * @param agrupaciones agrupaciones de electivas que lista la norma, si las hay
 */
public record ComponenteDeLibreEleccion(
    String nombre, ExigenciaCreditos exigencia, List<Agrupacion> agrupaciones)
    implements NodoDeMalla {

  /** Exige todos los datos y deja la lista inmutable. */
  public ComponenteDeLibreEleccion {
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    ArgumentChecks.obligatorio(exigencia, "exigencia");
    agrupaciones = ArgumentChecks.copiaSinNulos(agrupaciones, "agrupaciones");
  }

  /** Un componente de libre elección no tiene materias propias: se cubre con créditos. */
  @Override
  public List<MateriaEnMalla> materias() {
    return List.of();
  }

  @Override
  public List<NodoDeMalla> hijos() {
    return List.copyOf(agrupaciones);
  }

  /** Las electivas solo se consultan: no cubren créditos, así que suma cero. */
  @Override
  public Creditos creditosDeMaterias() {
    return new Creditos(0);
  }
}
