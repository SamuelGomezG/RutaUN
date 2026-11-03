package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Expresión de materias combinadas con «y» y «o» (glosario, «Datos de una regla»). Un prerrequisito
 * o un correquisito exigen una materia del programa o, si la norma las menciona, materias externas.
 */
public sealed interface ExpresionDeMaterias
    permits ExpresionDeMaterias.MateriaDelPrograma,
        ExpresionDeMaterias.MateriaExterna,
        ExpresionDeMaterias.Y,
        ExpresionDeMaterias.O {

  /** Materia que figura en la malla del programa. */
  record MateriaDelPrograma(CodigoMateria codigo) implements ExpresionDeMaterias {

    /** Exige el código de la materia. */
    public MateriaDelPrograma {
      ArgumentChecks.obligatorio(codigo, "codigo");
    }
  }

  /**
   * Materia que un prerrequisito menciona y que no figura en la malla del programa (glosario,
   * «Materia externa»). El nombre y, si la norma lo da, el código identifican la materia; el tipo
   * indica si es de nivelación o de otro programa.
   */
  record MateriaExterna(String nombre, Optional<CodigoMateria> codigo, TipoDeMateriaExterna tipo)
      implements ExpresionDeMaterias {

    /** Exige el nombre y el tipo; el código es opcional. */
    public MateriaExterna {
      ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
      ArgumentChecks.obligatorio(codigo, "codigo");
      ArgumentChecks.obligatorio(tipo, "tipo");
    }
  }

  /** Conjunción: se cumple si se cumplen sus dos términos. */
  record Y(ExpresionDeMaterias izquierda, ExpresionDeMaterias derecha)
      implements ExpresionDeMaterias {

    /** Exige los dos términos. */
    public Y {
      ArgumentChecks.obligatorio(izquierda, "izquierda");
      ArgumentChecks.obligatorio(derecha, "derecha");
    }
  }

  /** Disyunción: se cumple si se cumple alguno de sus dos términos. */
  record O(ExpresionDeMaterias izquierda, ExpresionDeMaterias derecha)
      implements ExpresionDeMaterias {

    /** Exige los dos términos. */
    public O {
      ArgumentChecks.obligatorio(izquierda, "izquierda");
      ArgumentChecks.obligatorio(derecha, "derecha");
    }
  }
}
