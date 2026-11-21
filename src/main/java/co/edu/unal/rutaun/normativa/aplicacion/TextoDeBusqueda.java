package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.ReglaVista;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * La búsqueda por texto de UC06 (paso 6): decide si una regla contiene lo que el visitante
 * escribió. No distingue mayúsculas ni tildes, y cada palabra buscada debe ser el comienzo de una
 * palabra de la regla, en cualquier orden: «credito» encuentra «créditos», pero «008» no encuentra
 * el código «1000089».
 */
final class TextoDeBusqueda {

  /** Las marcas que la descomposición NFD separa de las letras: las tildes y la diéresis. */
  private static final Pattern MARCAS = Pattern.compile("\\p{M}+");

  /** Todo lo que no es letra ni dígito separa palabras: espacios, signos y guiones. */
  private static final Pattern SEPARADORES = Pattern.compile("[^\\p{L}\\p{N}]+");

  private TextoDeBusqueda() {}

  /**
   * Si cada palabra del texto es el comienzo de una palabra del enunciado de la regla o de su
   * fuente (tipo, número, año y órgano de la norma, y artículo).
   */
  static boolean coincide(ReglaVista regla, String texto) {
    List<String> palabrasDeLaRegla = palabras(contenidoDe(regla));
    return palabras(texto).stream()
        .allMatch(
            buscada -> palabrasDeLaRegla.stream().anyMatch(palabra -> palabra.startsWith(buscada)));
  }

  private static String contenidoDe(ReglaVista regla) {
    StringBuilder contenido = new StringBuilder(regla.enunciado());
    regla
        .fuente()
        .ifPresent(
            fuente ->
                contenido
                    .append(' ')
                    .append(fuente.norma().tipo().name())
                    .append(' ')
                    .append(fuente.norma().numero())
                    .append(' ')
                    .append(fuente.norma().anio())
                    .append(' ')
                    .append(fuente.norma().organo())
                    .append(' ')
                    .append(fuente.articulo()));
    return contenido.toString();
  }

  private static List<String> palabras(String texto) {
    String sinMarcas =
        MARCAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
    return SEPARADORES
        .splitAsStream(sinMarcas.toLowerCase(Locale.ROOT))
        .filter(palabra -> !palabra.isEmpty())
        .toList();
  }
}
