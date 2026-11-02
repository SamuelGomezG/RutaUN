package co.edu.unal.rutaun.incidentes.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/** Fila de la tabla {@code incidente}: un grupo de incidentes iguales con su conteo. */
@Entity
@Table(name = "incidente")
public class IncidenteEntidad {

  @Id private UUID id;

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

  /** Elementos involucrados, como texto. */
  @Lob private String elementos;

  @Column(name = "primera_fecha", nullable = false)
  private LocalDateTime primeraFecha;

  @Column(name = "ultima_fecha", nullable = false)
  private LocalDateTime ultimaFecha;

  @Column(nullable = false)
  private int conteo;

  @Column(nullable = false)
  private boolean atendido;

  /** Constructor sin argumentos que exige JPA. */
  protected IncidenteEntidad() {}

  /** Crea un grupo de incidentes. */
  public IncidenteEntidad(
      UUID id,
      String casoDeUso,
      String paso,
      UUID programaId,
      Integer versionConsolidado,
      String causa,
      String elementos,
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

  public UUID getId() {
    return id;
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

  public String getElementos() {
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
