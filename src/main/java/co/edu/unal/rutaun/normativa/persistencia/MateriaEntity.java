package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Fila de la tabla {@code materia}: el catálogo global de materias.
 *
 * <p>Guardar no sobrescribe: si el código ya existe, la inserción falla; para cambiar la fila, se
 * lee y se modifica la entidad leída.
 */
@Entity
@Table(name = "materia")
public class MateriaEntity implements Persistable<String> {

  @Id private String codigo;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(nullable = false)
  private int creditos;

  /** Constructor sin argumentos que exige JPA. */
  protected MateriaEntity() {}

  /** Crea una materia. */
  public MateriaEntity(String codigo, int creditos) {
    this.codigo = codigo;
    this.creditos = creditos;
  }

  @Override
  public String getId() {
    return codigo;
  }

  /** Spring Data hace {@code persist}, y no {@code merge}, mientras la entidad sea nueva. */
  @Override
  public boolean isNew() {
    return nueva;
  }

  /** Marca la entidad como ya guardada; la llaman JPA al insertar y al leer. */
  @PostPersist
  @PostLoad
  void marcarComoGuardada() {
    nueva = false;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getCreditos() {
    return creditos;
  }
}
