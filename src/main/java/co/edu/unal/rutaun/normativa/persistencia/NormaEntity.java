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

  @Column(name = "aplica_desde_tipo", nullable = false)
  private String aplicaDesdeTipo;

  @Column(name = "aplica_desde_fecha")
  private LocalDate aplicaDesdeFecha;

  @Column(name = "aplica_desde_periodo")
  private String aplicaDesdePeriodo;

  @Column(name = "vigente_hasta_tipo")
  private String vigenteHastaTipo;

  @Column(name = "vigente_hasta_fecha")
  private LocalDate vigenteHastaFecha;

  @Column(name = "vigente_hasta_periodo")
  private String vigenteHastaPeriodo;

  @Column(name = "enlace_regimen_legal")
  private String enlaceRegimenLegal;

  @Column(name = "tipo_alcance", nullable = false)
  private String tipoAlcance;

  @Lob
  @Column(name = "alcance_detalle")
  private String alcanceDetalle;

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
      ColumnasDeMomento aplicaDesde,
      ColumnasDeMomento vigenteHasta,
      String enlaceRegimenLegal,
      String tipoAlcance,
      String alcanceDetalle) {
    this.id = id;
    this.tipo = tipo;
    this.numero = numero;
    this.anio = anio;
    this.organo = organo;
    this.tipoDeEfecto = tipoDeEfecto;
    this.fechaExpedicion = fechaExpedicion;
    this.aplicaDesdeTipo = aplicaDesde.tipo();
    this.aplicaDesdeFecha = aplicaDesde.fecha();
    this.aplicaDesdePeriodo = aplicaDesde.periodo();
    this.vigenteHastaTipo = vigenteHasta == null ? null : vigenteHasta.tipo();
    this.vigenteHastaFecha = vigenteHasta == null ? null : vigenteHasta.fecha();
    this.vigenteHastaPeriodo = vigenteHasta == null ? null : vigenteHasta.periodo();
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

  public ColumnasDeMomento getAplicaDesde() {
    return new ColumnasDeMomento(aplicaDesdeTipo, aplicaDesdeFecha, aplicaDesdePeriodo);
  }

  public ColumnasDeMomento getVigenteHasta() {
    return vigenteHastaTipo == null
        ? null
        : new ColumnasDeMomento(vigenteHastaTipo, vigenteHastaFecha, vigenteHastaPeriodo);
  }

  public String getEnlaceRegimenLegal() {
    return enlaceRegimenLegal;
  }

  public String getTipoAlcance() {
    return tipoAlcance;
  }

  public String getAlcanceDetalle() {
    return alcanceDetalle;
  }
}
