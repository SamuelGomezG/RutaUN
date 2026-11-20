package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Objects;

/**
 * Materia del catálogo único y global: su identidad es su código y sus créditos son fijos (Acuerdo
 * 033 de 2007 del CSU, art. 5, parágrafo 2). Una materia con otros créditos es otra materia, con
 * otro código. Dos materias con el mismo código y créditos distintos son un catálogo incoherente,
 * que rechaza UC07 ext. 6d. El nombre no vive aquí, sino en la regla de materia de cada versión del
 * consolidado.
 */
public final class Materia {

  private final CodigoMateria codigo;
  private final Creditos creditos;

  private Materia(CodigoMateria codigo, Creditos creditos) {
    this.codigo = ArgumentChecks.obligatorio(codigo, "codigo");
    this.creditos = ArgumentChecks.obligatorio(creditos, "creditos");
  }

  /** Registra una materia nueva en el catálogo. */
  public static Materia registrar(CodigoMateria codigo, Creditos creditos) {
    return new Materia(codigo, creditos);
  }

  /** Reconstruye una materia ya guardada. */
  public static Materia reconstituir(CodigoMateria codigo, Creditos creditos) {
    return new Materia(codigo, creditos);
  }

  /** Código de la materia, que es su identidad en el catálogo. */
  public CodigoMateria codigo() {
    return codigo;
  }

  /** Créditos de la materia. */
  public Creditos creditos() {
    return creditos;
  }

  /** Dos materias son la misma si tienen el mismo código: el código es su identidad. */
  @Override
  public boolean equals(Object otra) {
    return otra instanceof Materia materia && codigo.equals(materia.codigo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codigo);
  }

  @Override
  public String toString() {
    return "Materia[codigo=" + codigo.valor() + ", creditos=" + creditos.valor() + "]";
  }
}
