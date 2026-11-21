package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identificación de una norma: tipo, número, año y órgano que la expide (glosario, «Acuerdo (o
 * norma)»). Una norma se registra una sola vez con esta identificación.
 *
 * <p>El número se guarda sin espacios y en mayúsculas; si son dígitos seguidos de letras, con un
 * guion entre ellos: {@code 003A}, {@code 003 a} y {@code 003-a} quedan como {@code 003-A}.
 *
 * @param tipo tipo de la norma
 * @param numero número de la norma en su forma única
 * @param anio año de expedición
 * @param organo órgano que la expide
 */
public record IdentificacionNorma(TipoNorma tipo, String numero, int anio, Organo organo) {

  private static final Pattern DIGITOS_Y_LETRAS = Pattern.compile("^(\\d+)-?([A-Z]+)$");

  /** Exige todos los datos, un año positivo y deja el número en una sola forma. */
  public IdentificacionNorma {
    ArgumentChecks.obligatorio(tipo, "tipo");
    numero = normalizarNumero(ArgumentChecks.obligatorioNoVacio(numero, "numero"));
    ArgumentChecks.obligatorioPositivo(anio, "anio");
    ArgumentChecks.obligatorio(organo, "organo");
  }

  private static String normalizarNumero(String numero) {
    String compacto = numero.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    Matcher matcher = DIGITOS_Y_LETRAS.matcher(compacto);
    return matcher.matches() ? matcher.group(1) + "-" + matcher.group(2) : compacto;
  }
}
