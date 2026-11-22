package co.edu.unal.rutaun.normativa.persistencia;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

  /** Día de la versión automática por fecha; nulo en los demás motivos. */
  @Column(name = "dia_automatico")
  private LocalDate diaAutomatico;

  /**
   * Las filas de las reglas en esta versión. La columna {@code version_id} la administra esta
   * colección, y la fila no la mapea como campo propio (#143).
   */
  @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = false)
  @JoinColumn(name = "version_id", nullable = false, updatable = false)
  private List<ReglaEnVersionEntity> reglasEnVersion = new ArrayList<>();

  /** Constructor sin argumentos que exige JPA. */
  protected VersionEntity() {}

  /** Crea una versión del consolidado con sus filas. */
  public VersionEntity(
      UUID id,
      UUID programaId,
      int numero,
      LocalDate fecha,
      String motivo,
      String detalleMotivo,
      UUID administradorId,
      int numeroPlanDeEstudios,
      String cambios,
      LocalDate diaAutomatico,
      List<ReglaEnVersionEntity> reglasEnVersion) {
    this.id = id;
    this.programaId = programaId;
    this.numero = numero;
    this.fecha = fecha;
    this.motivo = motivo;
    this.detalleMotivo = detalleMotivo;
    this.administradorId = administradorId;
    this.numeroPlanDeEstudios = numeroPlanDeEstudios;
    this.cambios = cambios;
    this.diaAutomatico = diaAutomatico;
    this.reglasEnVersion =
        new ArrayList<>(ArgumentChecks.copiaSinNulos(reglasEnVersion, "reglasEnVersion"));
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

  public LocalDate getDiaAutomatico() {
    return diaAutomatico;
  }

  /** Las reglas que la versión contiene. Devuelve una copia que no se puede modificar. */
  public List<ReglaEnVersionEntity> getReglasEnVersion() {
    return List.copyOf(reglasEnVersion);
  }
}
