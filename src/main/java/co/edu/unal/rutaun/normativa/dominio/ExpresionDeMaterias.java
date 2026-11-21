package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Expresión de materias combinadas con «y» y «o» (glosario, «Datos de una regla»). Un prerrequisito
 * o un correquisito exigen una materia del programa o, si la norma las menciona, materias externas.
 * Las combinaciones no dependen del orden de sus términos ni anidan otra combinación del mismo
 * tipo.
 */
public sealed interface ExpresionDeMaterias
    permits ExpresionDeMaterias.TerminoMateria,
        ExpresionDeMaterias.MateriaExterna,
        ExpresionDeMaterias.TodasLas,
        ExpresionDeMaterias.AlgunaDe {

  /**
   * Evalúa cada término con la función que da el estado de una materia del programa. Las materias
   * externas se evalúan solas y las combinaciones se acumulan de izquierda a derecha.
   *
   * @param estadoDe estado de una materia del programa
   * @return estado de la expresión
   */
  EstadoRestriccion evaluar(Function<CodigoMateria, EstadoRestriccion> estadoDe);

  /** Materia que figura en la malla del programa. */
  record TerminoMateria(CodigoMateria codigo) implements ExpresionDeMaterias {

    /** Exige el código de la materia. */
    public TerminoMateria {
      ArgumentChecks.obligatorio(codigo, "codigo");
    }

    @Override
    public EstadoRestriccion evaluar(Function<CodigoMateria, EstadoRestriccion> estadoDe) {
      ArgumentChecks.obligatorio(estadoDe, "estadoDe");
      return estadoDe.apply(codigo);
    }
  }

  /**
   * Materia que un prerrequisito menciona y que no figura en la malla del programa (glosario,
   * «Materia externa»). El nombre y, si la norma lo da, el código identifican la materia; el tipo
   * indica si es de nivelación o de otro programa.
   */
  record MateriaExterna(String nombre, Optional<CodigoMateria> codigo, TipoMateriaExterna tipo)
      implements ExpresionDeMaterias {

    /** Exige el nombre y el tipo; el código es opcional. */
    public MateriaExterna {
      ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
      ArgumentChecks.obligatorio(codigo, "codigo");
      ArgumentChecks.obligatorio(tipo, "tipo");
    }

    @Override
    public EstadoRestriccion evaluar(Function<CodigoMateria, EstadoRestriccion> estadoDe) {
      ArgumentChecks.obligatorio(estadoDe, "estadoDe");
      return switch (tipo()) {
        case NIVELACION ->
            EstadoRestriccion.cumplida(
                "la materia de nivelación " + nombre() + " se da por cumplida");
        case OTRO_PROGRAMA ->
            EstadoRestriccion.pendiente(
                "la materia de otro programa "
                    + nombre()
                    + " no se puede cursar desde este programa");
      };
    }
  }

  /**
   * «y»: se cumple si se cumplen todos sus términos. Dos o más términos distintos, sin orden y sin
   * «y» anidadas; al recorrerlos se conserva el orden en que se dieron.
   */
  record TodasLas(Set<ExpresionDeMaterias> terminos) implements ExpresionDeMaterias {

    /** Aplana las «y» hijas y exige al menos dos términos distintos. */
    public TodasLas {
      terminos = aplanar(terminos, TodasLas.class);
      if (terminos.size() < 2) {
        throw new IllegalArgumentException("una «y» necesita al menos dos términos distintos");
      }
    }

    @Override
    public EstadoRestriccion evaluar(Function<CodigoMateria, EstadoRestriccion> estadoDe) {
      return reducir(terminos, estadoDe, EstadoRestriccion::y);
    }
  }

  /**
   * «o»: se cumple si se cumple alguno de sus términos. Dos o más términos distintos, sin orden y
   * sin «o» anidadas; al recorrerlos se conserva el orden en que se dieron.
   */
  record AlgunaDe(Set<ExpresionDeMaterias> terminos) implements ExpresionDeMaterias {

    /** Aplana las «o» hijas y exige al menos dos términos distintos. */
    public AlgunaDe {
      terminos = aplanar(terminos, AlgunaDe.class);
      if (terminos.size() < 2) {
        throw new IllegalArgumentException("una «o» necesita al menos dos términos distintos");
      }
    }

    @Override
    public EstadoRestriccion evaluar(Function<CodigoMateria, EstadoRestriccion> estadoDe) {
      return reducir(terminos, estadoDe, EstadoRestriccion::o);
    }
  }

  /** «y» de varios términos; si queda uno, devuelve ese término. */
  static ExpresionDeMaterias todasLas(ExpresionDeMaterias... terminos) {
    ArgumentChecks.obligatorio(terminos, "terminos");
    return combinar(
        Arrays.asList(terminos), TodasLas.class, "una «y» necesita al menos un término");
  }

  /** «o» de varios términos; si queda uno, devuelve ese término. */
  static ExpresionDeMaterias algunaDe(ExpresionDeMaterias... terminos) {
    ArgumentChecks.obligatorio(terminos, "terminos");
    return combinar(
        Arrays.asList(terminos), AlgunaDe.class, "una «o» necesita al menos un término");
  }

  private static EstadoRestriccion reducir(
      Set<ExpresionDeMaterias> terminos,
      Function<CodigoMateria, EstadoRestriccion> estadoDe,
      BinaryOperator<EstadoRestriccion> combinador) {
    ArgumentChecks.obligatorio(estadoDe, "estadoDe");
    Iterator<ExpresionDeMaterias> pendientes = terminos.iterator();
    EstadoRestriccion acumulado =
        ArgumentChecks.obligatorio(pendientes.next().evaluar(estadoDe), "estado");
    while (pendientes.hasNext()) {
      EstadoRestriccion siguiente =
          ArgumentChecks.obligatorio(pendientes.next().evaluar(estadoDe), "estado");
      acumulado = combinador.apply(acumulado, siguiente);
    }
    return acumulado;
  }

  private static ExpresionDeMaterias combinar(
      Collection<ExpresionDeMaterias> terminos,
      Class<? extends ExpresionDeMaterias> tipo,
      String mensajeSiVacio) {
    Set<ExpresionDeMaterias> planos = aplanar(terminos, tipo);
    if (planos.isEmpty()) {
      throw new IllegalArgumentException(mensajeSiVacio);
    }
    if (planos.size() == 1) {
      return planos.iterator().next();
    }
    return tipo == TodasLas.class ? new TodasLas(planos) : new AlgunaDe(planos);
  }

  /**
   * Copia los términos en el orden en que se dieron, rechaza los nulos y reemplaza cada combinación
   * del mismo tipo por sus términos. El resultado es inmutable.
   */
  private static Set<ExpresionDeMaterias> aplanar(
      Collection<ExpresionDeMaterias> terminos, Class<? extends ExpresionDeMaterias> tipo) {
    ArgumentChecks.obligatorio(terminos, "terminos");
    Set<ExpresionDeMaterias> planos = new LinkedHashSet<>();
    for (ExpresionDeMaterias termino : terminos) {
      if (termino == null) {
        throw new IllegalArgumentException("terminos no puede tener elementos null");
      }
      if (tipo.isInstance(termino)) {
        planos.addAll(terminosDe(termino));
      } else {
        planos.add(termino);
      }
    }
    return Collections.unmodifiableSet(planos);
  }

  private static Set<ExpresionDeMaterias> terminosDe(ExpresionDeMaterias combinacion) {
    return switch (combinacion) {
      case TodasLas todas -> todas.terminos();
      case AlgunaDe alguna -> alguna.terminos();
      default -> throw new IllegalArgumentException("la combinación no es del tipo esperado");
    };
  }
}
