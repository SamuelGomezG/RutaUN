package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla {@code materia}: el catálogo global de materias. */
@Entity
@Table(name = "materia")
public class MateriaEntidad {

  @Id private String codigo;

  @Column(nullable = false)
  private int creditos;

  /** Constructor sin argumentos que exige JPA. */
  protected MateriaEntidad() {}

  /** Crea una materia. */
  public MateriaEntidad(String codigo, int creditos) {
    this.codigo = codigo;
    this.creditos = creditos;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getCreditos() {
    return creditos;
  }
}
