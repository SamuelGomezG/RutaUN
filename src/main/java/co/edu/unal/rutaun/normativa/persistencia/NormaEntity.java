package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

/**
 * Fila de la tabla {@code norma}: una norma con su vigencia y su alcance.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "norma")
public class NormaEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

  @Column(nullable = false)
  private String tipo;

  @Column(nullable = false)
  private String numero;

  @Column(nullable = false)
  private int anio;

  @Column(nullable = false)
  private String organo;

  @Column(name = "tipo_de_efecto", nullable = false)
  private String tipoDeEfecto;

  @Column(name = "fecha_expedicion", nullable = false)
  private LocalDate fechaExpedicion;

  @Column(name = "aplica_desde", nullable = false)
  private LocalDate aplicaDesde;

  @Column(name = "vigente_hasta")
  private LocalDate vigenteHasta;

  @Column(name = "enlace_regimen_legal")
  private String enlaceRegimenLegal;

  @Column(name = "tipo_alcance", nullable = false)
  private String tipoAlcance;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "alcance_detalle", nullable = false)
  private AlcanceJson alcanceDetalle;

  /** Constructor sin argumentos que exige JPA. */
  protected NormaEntity() {}

  /** Crea una norma. */
  public NormaEntity(
      UUID id,
      String tipo,
      String numero,
      int anio,
      String organo,
      String tipoDeEfecto,
      LocalDate fechaExpedicion,
      LocalDate aplicaDesde,
      LocalDate vigenteHasta,
      String enlaceRegimenLegal,
      String tipoAlcance,
      AlcanceJson alcanceDetalle) {
    this.id = id;
    this.tipo = tipo;
    this.numero = numero;
    this.anio = anio;
    this.organo = organo;
    this.tipoDeEfecto = tipoDeEfecto;
    this.fechaExpedicion = fechaExpedicion;
    this.aplicaDesde = aplicaDesde;
    this.vigenteHasta = vigenteHasta;
    this.enlaceRegimenLegal = enlaceRegimenLegal;
    this.tipoAlcance = tipoAlcance;
    this.alcanceDetalle = alcanceDetalle;
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

  public String getTipo() {
    return tipo;
  }

  public String getNumero() {
    return numero;
  }

  public int getAnio() {
    return anio;
  }

  public String getOrgano() {
    return organo;
  }

  public String getTipoDeEfecto() {
    return tipoDeEfecto;
  }

  public LocalDate getFechaExpedicion() {
    return fechaExpedicion;
  }

  public LocalDate getAplicaDesde() {
    return aplicaDesde;
  }

  public LocalDate getVigenteHasta() {
    return vigenteHasta;
  }

  public String getEnlaceRegimenLegal() {
    return enlaceRegimenLegal;
  }

  public String getTipoAlcance() {
    return tipoAlcance;
  }

  public AlcanceJson getAlcanceDetalle() {
    return alcanceDetalle;
  }
}
