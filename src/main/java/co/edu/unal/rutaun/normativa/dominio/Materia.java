package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Objects;

/**
 * Materia del catálogo único y global de RutaUN (glosario, «Materia»): su identidad es su {@link
 * CodigoMateria} y sus créditos son fijos. Cambiar los créditos no modifica la materia: crea otra
 * ({@link #conCreditos}). El nombre no vive aquí, sino en la regla de materia de cada versión del
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

  /**
   * Devuelve otra materia con el mismo código y esos créditos. Esta materia no cambia: los créditos
   * son fijos, así que cambiarlos crea otra materia.
   */
  public Materia conCreditos(Creditos nuevos) {
    return new Materia(codigo, nuevos);
  }

  /** Dos materias son la misma si tienen el mismo código y los mismos créditos. */
  @Override
  public boolean equals(Object otra) {
    return otra instanceof Materia materia
        && codigo.equals(materia.codigo)
        && creditos.equals(materia.creditos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codigo, creditos);
  }

  @Override
  public String toString() {
    return "Materia[codigo=" + codigo.valor() + ", creditos=" + creditos.valor() + "]";
  }
}
