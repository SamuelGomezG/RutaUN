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
 * Fila de la tabla {@code regla}: una regla de una norma.
 *
 * <p>Guardar no sobrescribe: si el id ya existe, la inserción falla; para cambiar la fila, se lee y
 * se modifica la entidad leída.
 */
@Entity
@Table(name = "regla")
public class ReglaEntity implements Persistable<UUID> {

  @Id private UUID id;

  /** Si la fila todavía no está en la base. JPA no lo guarda. */
  @Transient private boolean nueva = true;

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
  protected ReglaEntity() {}

  /** Crea una regla. */
  public ReglaEntity(
      UUID id,
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
