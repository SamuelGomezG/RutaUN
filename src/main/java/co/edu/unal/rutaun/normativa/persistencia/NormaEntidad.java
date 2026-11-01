package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Fila de la tabla {@code norma}: una norma con su vigencia y su alcance. */
@Entity
@Table(name = "norma")
public class NormaEntidad {

  @Id private UUID id;

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

  @Lob
  @Column(name = "alcance_detalle")
  private String alcanceDetalle;

  /** Constructor sin argumentos que exige JPA. */
  protected NormaEntidad() {}

  /** Crea una norma. */
  public NormaEntidad(
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
      String alcanceDetalle) {
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

  public UUID getId() {
    return id;
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

  public String getAlcanceDetalle() {
    return alcanceDetalle;
  }
}
