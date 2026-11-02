package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Lapso en el que rige una norma (glosario, «Vigencia»): desde su fecha de aplicación hasta su
 * «vigencia hasta» o sin fin. Cada extremo es un {@link Momento}: un periodo académico o una fecha.
 * RutaUN no compara un periodo con una fecha, porque no guarda las fechas de los periodos (S4);
 * solo rechaza un fin anterior al inicio cuando ambos son del mismo tipo.
 *
 * @param aplicaDesde momento desde el que rige
 * @param vigenteHasta momento hasta el que rige; vacío si no tiene fin
 */
public record Vigencia(Momento aplicaDesde, Optional<Momento> vigenteHasta) {

  /** Exige el inicio y un optional no nulo; si ambos extremos son comparables, exige el orden. */
  public Vigencia {
    ArgumentChecks.obligatorio(aplicaDesde, "aplicaDesde");
    ArgumentChecks.obligatorio(vigenteHasta, "vigenteHasta");
    vigenteHasta.ifPresent(hasta -> validarOrden(aplicaDesde, hasta));
  }

  /** Si la vigencia tiene un fin, es decir, si la norma o la regla es temporal. */
  public boolean esTemporal() {
    return vigenteHasta.isPresent();
  }

  private static void validarOrden(Momento desde, Momento hasta) {
    if (desde instanceof Momento.EnPeriodo inicio
        && hasta instanceof Momento.EnPeriodo fin
        && fin.periodo().compareTo(inicio.periodo()) < 0) {
      throw new IllegalArgumentException("vigenteHasta no puede ser anterior a aplicaDesde");
    }
    if (desde instanceof Momento.EnFecha inicio
        && hasta instanceof Momento.EnFecha fin
        && fin.fecha().isBefore(inicio.fecha())) {
      throw new IllegalArgumentException("vigenteHasta no puede ser anterior a aplicaDesde");
    }
  }
}
