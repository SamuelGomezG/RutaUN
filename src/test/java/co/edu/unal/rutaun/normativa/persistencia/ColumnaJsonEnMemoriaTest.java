package co.edu.unal.rutaun.normativa.persistencia;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;

/** TT-04-T2 (#90): ida y vuelta de la columna JSON en H2 en memoria. */
@PruebaDeIntegracion
class ColumnaJsonEnMemoriaTest extends ColumnaJsonIdaYVueltaBase {

  @Override
  protected String prefijoDeUrlEsperado() {
    return "jdbc:h2:mem:";
  }
}
