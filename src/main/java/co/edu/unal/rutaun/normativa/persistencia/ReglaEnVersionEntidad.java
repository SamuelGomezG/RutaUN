package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Fila de la tabla {@code regla_en_version}: una regla con su estado en una versión. */
@Entity
@Table(name = "regla_en_version")
public class ReglaEnVersionEntidad {

  @Id private UUID id;

  @Column(name = "version_id", nullable = false)
  private UUID versionId;

  @Column(name = "regla_id", nullable = false)
  private UUID reglaId;

  @Column(nullable = false)
  private String estado;

  /** Constructor sin argumentos que exige JPA. */
  protected ReglaEnVersionEntidad() {}

  /** Crea la relación entre una regla y una versión. */
  public ReglaEnVersionEntidad(UUID id, UUID versionId, UUID reglaId, String estado) {
    this.id = id;
    this.versionId = versionId;
    this.reglaId = reglaId;
    this.estado = estado;
  }

  public UUID getId() {
    return id;
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
