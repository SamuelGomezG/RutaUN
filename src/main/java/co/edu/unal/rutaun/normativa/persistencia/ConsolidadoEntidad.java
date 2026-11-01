package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

/** Fila de la tabla {@code consolidado}: el consolidado de un programa, uno por programa. */
@Entity
@Table(name = "consolidado")
public class ConsolidadoEntidad {

  @Id private UUID id;

  @Column(name = "programa_id", nullable = false)
  private UUID programaId;

  // Wrapper (Long) y no long: así Spring Data sabe que la entidad es nueva mientras sea null.
  @Version
  @Column(name = "version_bloqueo", nullable = false)
  private Long version;

  /** Constructor sin argumentos que exige JPA. */
  protected ConsolidadoEntidad() {}

  /** Crea el consolidado de un programa. */
  public ConsolidadoEntidad(UUID id, UUID programaId) {
    this.id = id;
    this.programaId = programaId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getProgramaId() {
    return programaId;
  }

  public Long getVersion() {
    return version;
  }
}
