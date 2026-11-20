package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Fila de la tabla {@code version_consolidado}: una versión inmutable del consolidado.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "version_consolidado")
public class VersionEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(name = "consolidado_id", nullable = false)
  private UUID consolidadoId;

  @Column(name = "programa_id", nullable = false)
  private UUID programaId;

  @Column(nullable = false)
  private int numero;

  @Column(nullable = false)
  private LocalDate fecha;

  @Column(nullable = false)
  private String motivo;

  @Column(name = "detalle_motivo")
  private String detalleMotivo;

  @Column(name = "administrador_id")
  private UUID administradorId;

  @Column(name = "numero_plan_de_estudios", nullable = false)
  private int numeroPlanDeEstudios;

  /** Cambios respecto a la versión anterior, como texto JSON. */
  @Lob private String cambios;

  /** Constructor sin argumentos que exige JPA. */
  protected VersionEntity() {}

  /** Crea una versión del consolidado. */
  public VersionEntity(
      UUID id,
      UUID consolidadoId,
      UUID programaId,
      int numero,
      LocalDate fecha,
      String motivo,
      String detalleMotivo,
      UUID administradorId,
      int numeroPlanDeEstudios,
      String cambios) {
    this.id = id;
    this.consolidadoId = consolidadoId;
    this.programaId = programaId;
    this.numero = numero;
    this.fecha = fecha;
    this.motivo = motivo;
    this.detalleMotivo = detalleMotivo;
    this.administradorId = administradorId;
    this.numeroPlanDeEstudios = numeroPlanDeEstudios;
    this.cambios = cambios;
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

  public UUID getConsolidadoId() {
    return consolidadoId;
  }

  public UUID getProgramaId() {
    return programaId;
  }

  public int getNumero() {
    return numero;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public String getMotivo() {
    return motivo;
  }

  public String getDetalleMotivo() {
    return detalleMotivo;
  }

  public UUID getAdministradorId() {
    return administradorId;
  }

  public int getNumeroPlanDeEstudios() {
    return numeroPlanDeEstudios;
  }

  public String getCambios() {
    return cambios;
  }
}
