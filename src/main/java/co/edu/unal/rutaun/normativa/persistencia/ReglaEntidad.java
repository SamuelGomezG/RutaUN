package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Fila de la tabla {@code regla}: una regla de una norma. */
@Entity
@Table(name = "regla")
public class ReglaEntidad {

  @Id private UUID id;

  @Column(name = "norma_id", nullable = false)
  private UUID normaId;

  @Column(nullable = false)
  private String categoria;

  @Column(nullable = false)
  private String objeto;

  @Column(nullable = false)
  private String enunciado;

  @Column(nullable = false)
  private String fuente;

  @Column(name = "vigente_desde")
  private LocalDate vigenteDesde;

  @Column(name = "vigente_hasta")
  private LocalDate vigenteHasta;

  @Column(nullable = false)
  private String clasificacion;

  @Column(name = "tipo_afectacion")
  private String tipoAfectacion;

  @Column(name = "regla_afectada_id")
  private UUID reglaAfectadaId;

  @Column(name = "articulo_afectado")
  private String articuloAfectado;

  /** Datos propios de la categoría, como texto JSON. */
  @Lob private String datos;

  /** Constructor sin argumentos que exige JPA. */
  protected ReglaEntidad() {}

  /** Crea una regla. */
  public ReglaEntidad(
      UUID id,
      UUID normaId,
      String categoria,
      String objeto,
      String enunciado,
      String fuente,
      LocalDate vigenteDesde,
      LocalDate vigenteHasta,
      String clasificacion,
      String tipoAfectacion,
      UUID reglaAfectadaId,
      String articuloAfectado,
      String datos) {
    this.id = id;
    this.normaId = normaId;
    this.categoria = categoria;
    this.objeto = objeto;
    this.enunciado = enunciado;
    this.fuente = fuente;
    this.vigenteDesde = vigenteDesde;
    this.vigenteHasta = vigenteHasta;
    this.clasificacion = clasificacion;
    this.tipoAfectacion = tipoAfectacion;
    this.reglaAfectadaId = reglaAfectadaId;
    this.articuloAfectado = articuloAfectado;
    this.datos = datos;
  }

  public UUID getId() {
    return id;
  }

  public UUID getNormaId() {
    return normaId;
  }

  public String getCategoria() {
    return categoria;
  }

  public String getObjeto() {
    return objeto;
  }

  public String getEnunciado() {
    return enunciado;
  }

  public String getFuente() {
    return fuente;
  }

  public LocalDate getVigenteDesde() {
    return vigenteDesde;
  }

  public LocalDate getVigenteHasta() {
    return vigenteHasta;
  }

  public String getClasificacion() {
    return clasificacion;
  }

  public String getTipoAfectacion() {
    return tipoAfectacion;
  }

  public UUID getReglaAfectadaId() {
    return reglaAfectadaId;
  }

  public String getArticuloAfectado() {
    return articuloAfectado;
  }

  public String getDatos() {
    return datos;
  }
}
