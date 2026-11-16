package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Periodo académico: un semestre de la Universidad, identificado como «AAAA-1» o «AAAA-2»
 * (glosario, «Periodo académico»). Los periodos se suceden en orden y no llevan fechas, porque
 * RutaUN no las guarda (S4).
 *
 * @param anio año del periodo, de cuatro cifras
 * @param parte primera o segunda parte del año
 */
public record PeriodoAcademico(int anio, ParteDelAnio parte)
    implements Comparable<PeriodoAcademico> {

  private static final int ANIO_MINIMO = 1000;
  private static final int ANIO_MAXIMO = 9999;
  private static final Pattern FORMATO = Pattern.compile("([0-9]{4})-([12])");

  /** Exige un año de cuatro cifras y una parte del año. */
  public PeriodoAcademico {
    if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
      throw new IllegalArgumentException("anio debe tener cuatro cifras");
    }
    ArgumentChecks.obligatorio(parte, "parte");
  }

  /**
   * Crea el periodo desde su texto: «AAAA-1» o «AAAA-2», por ejemplo «2026-2». Cualquier otro
   * formato se rechaza.
   *
   * @param texto el periodo escrito como año de cuatro cifras, guion y 1 o 2
   * @throws IllegalArgumentException si el texto es nulo o no tiene ese formato
   */
  public static PeriodoAcademico desdeTexto(String texto) {
    if (texto == null) {
      throw new IllegalArgumentException("periodo debe tener el formato AAAA-1 o AAAA-2");
    }
    Matcher coincidencia = FORMATO.matcher(texto);
    if (!coincidencia.matches()) {
      throw new IllegalArgumentException("periodo debe tener el formato AAAA-1 o AAAA-2");
    }
    ParteDelAnio parte =
        "1".equals(coincidencia.group(2)) ? ParteDelAnio.PRIMERO : ParteDelAnio.SEGUNDO;
    return new PeriodoAcademico(Integer.parseInt(coincidencia.group(1)), parte);
  }

  /** Devuelve el periodo que sigue a este: después de 2026-2 viene 2027-1. */
  public PeriodoAcademico siguiente() {
    return switch (parte) {
      case PRIMERO -> new PeriodoAcademico(anio, ParteDelAnio.SEGUNDO);
      case SEGUNDO -> new PeriodoAcademico(anio + 1, ParteDelAnio.PRIMERO);
    };
  }

  /** Ordena los periodos cronológicamente: primero por año y, en el mismo año, por parte. */
  @Override
  public int compareTo(PeriodoAcademico otro) {
    ArgumentChecks.obligatorio(otro, "otro");
    int porAnio = Integer.compare(anio, otro.anio);
    return porAnio != 0 ? porAnio : parte.compareTo(otro.parte);
  }
}
