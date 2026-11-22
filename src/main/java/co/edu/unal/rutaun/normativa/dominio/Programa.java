package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Programa académico: la unidad que tiene un consolidado normativo (glosario, «Programa»). Es una
 * raíz: se crea con {@link #registrar} y se reconstruye desde la base con {@link #reconstituir}. Es
 * inmutable y su identidad es su {@link ProgramaId}.
 *
 * <p>La facultad se identifica por su código, que comparten todas las normas que la citan; el
 * nombre con el que se muestra vive aquí, en {@link #nombreDeFacultad()}.
 */
public final class Programa {

  private final ProgramaId id;
  private final String nombre;
  private final String codigoSia;
  private final Facultad facultad;
  private final String nombreDeFacultad;

  private Programa(
      ProgramaId id, String nombre, String codigoSia, Facultad facultad, String nombreDeFacultad) {
    this.id = ArgumentChecks.obligatorio(id, "id");
    this.nombre = ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    this.codigoSia = ArgumentChecks.obligatorioNoVacio(codigoSia, "codigoSia");
    this.facultad = ArgumentChecks.obligatorio(facultad, "facultad");
    this.nombreDeFacultad = ArgumentChecks.obligatorioNoVacio(nombreDeFacultad, "nombreDeFacultad");
  }

  /** Registra un programa nuevo, con un identificador generado en el dominio. */
  public static Programa registrar(
      String nombre, String codigoSia, Facultad facultad, String nombreDeFacultad) {
    return new Programa(ProgramaId.nuevo(), nombre, codigoSia, facultad, nombreDeFacultad);
  }

  /** Reconstruye un programa ya guardado, con su identificador. No genera uno nuevo. */
  public static Programa reconstituir(
      ProgramaId id, String nombre, String codigoSia, Facultad facultad, String nombreDeFacultad) {
    return new Programa(id, nombre, codigoSia, facultad, nombreDeFacultad);
  }

  /** Identificador del programa. */
  public ProgramaId id() {
    return id;
  }

  /** Nombre del programa. */
  public String nombre() {
    return nombre;
  }

  /** Código SIA del programa. */
  public String codigoSia() {
    return codigoSia;
  }

  /** Facultad a la que pertenece el programa, identificada por su código. */
  public Facultad facultad() {
    return facultad;
  }

  /** Nombre con el que se muestra la facultad del programa. */
  public String nombreDeFacultad() {
    return nombreDeFacultad;
  }

  /** Dos programas son el mismo si tienen el mismo identificador. */
  @Override
  public boolean equals(Object otro) {
    return otro instanceof Programa programa && id.equals(programa.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Programa[id="
        + id.valor()
        + ", codigoSia="
        + codigoSia
        + ", nombre="
        + nombre
        + ", facultad="
        + facultad.codigo()
        + "]";
  }
}
