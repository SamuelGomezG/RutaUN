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
 * Fila de la tabla {@code regla_en_version}: una regla con su estado en una versión.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "regla_en_version")
public class ReglaEnVersionEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(name = "version_id", nullable = false)
  private UUID versionId;

  @Column(name = "regla_id", nullable = false)
  private UUID reglaId;

  @Column(nullable = false)
  private String estado;

  /** Constructor sin argumentos que exige JPA. */
  protected ReglaEnVersionEntity() {}

  /** Crea la relación entre una regla y una versión. */
  public ReglaEnVersionEntity(UUID id, UUID versionId, UUID reglaId, String estado) {
    this.id = id;
    this.versionId = versionId;
    this.reglaId = reglaId;
    this.estado = estado;
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

  public UUID getVersionId() {
    return versionId;
  }

  public UUID getReglaId() {
    return reglaId;
  }

  public String getEstado() {
    return estado;
  }
}
