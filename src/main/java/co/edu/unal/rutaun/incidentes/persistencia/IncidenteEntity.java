package co.edu.unal.rutaun.incidentes.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

/**
 * Fila de la tabla {@code incidente}: un grupo de incidentes iguales con su conteo.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "incidente")
public class IncidenteEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(name = "caso_de_uso", nullable = false)
  private String casoDeUso;

  @Column(nullable = false)
  private String paso;

  @Column(name = "programa_id")
  private UUID programaId;

  @Column(name = "version_consolidado")
  private Integer versionConsolidado;

  @Column(nullable = false)
  private String causa;

  /** Elementos involucrados, en la columna JSON. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private ElementosJson elementos;

  @Column(name = "primera_fecha", nullable = false)
  private LocalDateTime primeraFecha;

  @Column(name = "ultima_fecha", nullable = false)
  private LocalDateTime ultimaFecha;

  @Column(nullable = false)
  private int conteo;

  @Column(nullable = false)
  private boolean atendido;

  /** Constructor sin argumentos que exige JPA. */
  protected IncidenteEntity() {}

  /** Crea un grupo de incidentes. */
  public IncidenteEntity(
      UUID id,
      String casoDeUso,
      String paso,
      UUID programaId,
      Integer versionConsolidado,
      String causa,
      ElementosJson elementos,
      LocalDateTime primeraFecha,
      LocalDateTime ultimaFecha,
      int conteo,
      boolean atendido) {
    this.id = id;
    this.casoDeUso = casoDeUso;
    this.paso = paso;
    this.programaId = programaId;
    this.versionConsolidado = versionConsolidado;
    this.causa = causa;
    this.elementos = elementos;
    this.primeraFecha = primeraFecha;
    this.ultimaFecha = ultimaFecha;
    this.conteo = conteo;
    this.atendido = atendido;
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

  public String getCasoDeUso() {
    return casoDeUso;
  }

  public String getPaso() {
    return paso;
  }

  public UUID getProgramaId() {
    return programaId;
  }

  public Integer getVersionConsolidado() {
    return versionConsolidado;
  }

  public String getCausa() {
    return causa;
  }

  public ElementosJson getElementos() {
    return elementos;
  }

  public LocalDateTime getPrimeraFecha() {
    return primeraFecha;
  }

  public LocalDateTime getUltimaFecha() {
    return ultimaFecha;
  }

  public int getConteo() {
    return conteo;
  }

  public boolean isAtendido() {
    return atendido;
  }

  /**
   * Suma una ocurrencia más de este mismo incidente.
   *
   * @param fecha momento de la nueva ocurrencia, que pasa a ser la última fecha
   */
  public void registrarOtraOcurrencia(LocalDateTime fecha) {
    this.conteo++;
    this.ultimaFecha = fecha;
  }
}
