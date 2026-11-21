package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Vista inmutable de lo que exige una restricción: una materia, o varias combinadas con «y» y «o»
 * (UC05 paso 4 y ext. 4a). Una conjunción o una disyunción siempre tiene al menos dos términos, y
 * ninguno de ellos es otra del mismo tipo: «A y (B y C)» se lee como «A, B y C».
 */
public sealed interface ExpresionVista
    permits ExpresionVista.Materia, ExpresionVista.TodasLas, ExpresionVista.AlgunaDe {

  /**
   * La expresión escrita para leerla (UC05 paso 4, «qué la satisface»): los nombres de las materias
   * unidos con «y» y «o», con paréntesis cuando una conjunción y una disyunción se anidan, como en
   * «Fundamentos de Mecánica y (Cálculo Integral o Cálculo integral en una variable)». Con más de
   * dos términos, los separa con comas: «A, B y C».
   */
  String redaccion();

  /**
   * Una materia del programa.
   *
   * @param codigo código de la materia
   * @param nombre nombre de la materia en la versión vigente del consolidado
   */
  record Materia(CodigoMateria codigo, String nombre) implements ExpresionVista {

    /** Exige el código y el nombre. */
    public Materia {
      ArgumentChecks.obligatorio(codigo, "codigo");
      ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    }

    @Override
    public String redaccion() {
      return nombre;
    }
  }

  /**
   * Se cumple si se cumplen todos sus términos («y»).
   *
   * @param terminos al menos dos términos, en el orden de la norma
   */
  record TodasLas(List<ExpresionVista> terminos) implements ExpresionVista {

    /** Exige al menos dos términos y deja la lista inmutable. */
    public TodasLas {
      terminos = ArgumentChecks.copiaSinNulos(terminos, "terminos");
      if (terminos.size() < 2) {
        throw new IllegalArgumentException("una conjunción necesita al menos dos términos");
      }
    }

    @Override
    public String redaccion() {
      return unir(terminos, " y ", AlgunaDe.class);
    }
  }

  /**
   * Se cumple si se cumple alguno de sus términos («o»).
   *
   * @param alternativas al menos dos alternativas, en el orden de la norma
   */
  record AlgunaDe(List<ExpresionVista> alternativas) implements ExpresionVista {

    /** Exige al menos dos alternativas y deja la lista inmutable. */
    public AlgunaDe {
      alternativas = ArgumentChecks.copiaSinNulos(alternativas, "alternativas");
      if (alternativas.size() < 2) {
        throw new IllegalArgumentException("una disyunción necesita al menos dos alternativas");
      }
    }

    @Override
    public String redaccion() {
      return unir(alternativas, " o ", TodasLas.class);
    }
  }

  /**
   * Une los términos: todos separados por comas menos el último, que va con el conector. Los de la
   * clase indicada, que son de otro tipo que quien los une, van entre paréntesis.
   */
  private static String unir(
      List<ExpresionVista> terminos, String conector, Class<? extends ExpresionVista> otroTipo) {
    List<String> partes =
        terminos.stream()
            .map(
                termino ->
                    otroTipo.isInstance(termino)
                        ? "(" + termino.redaccion() + ")"
                        : termino.redaccion())
            .toList();
    String ultimo = partes.getLast();
    return String.join(", ", partes.subList(0, partes.size() - 1)) + conector + ultimo;
  }
}
