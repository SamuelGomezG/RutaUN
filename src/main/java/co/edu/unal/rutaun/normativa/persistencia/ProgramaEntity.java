package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Fila de la tabla {@code programa}: un programa académico.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "programa")
public class ProgramaEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(nullable = false)
  private String nombre;

  @Column(name = "codigo_sia", nullable = false)
  private String codigoSia;

  @Column(nullable = false)
  private String facultad;

  /** Constructor sin argumentos que exige JPA. */
  protected ProgramaEntity() {}

  /** Crea un programa. */
  public ProgramaEntity(UUID id, String nombre, String codigoSia, String facultad) {
    this.id = id;
    this.nombre = nombre;
    this.codigoSia = codigoSia;
    this.facultad = facultad;
  }

  @Override
  public UUID getId() {
    return id;
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

  public String getNombre() {
    return nombre;
  }

  public String getCodigoSia() {
    return codigoSia;
  }

  public String getFacultad() {
    return facultad;
  }
}
