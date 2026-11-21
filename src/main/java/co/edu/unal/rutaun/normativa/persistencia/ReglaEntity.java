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

  @Column(name = "vigente_desde_tipo")
  private String vigenteDesdeTipo;

  @Column(name = "vigente_desde_fecha")
  private LocalDate vigenteDesdeFecha;

  @Column(name = "vigente_desde_periodo")
  private String vigenteDesdePeriodo;

  @Column(name = "vigente_hasta_tipo")
  private String vigenteHastaTipo;

  @Column(name = "vigente_hasta_fecha")
  private LocalDate vigenteHastaFecha;

  @Column(name = "vigente_hasta_periodo")
  private String vigenteHastaPeriodo;

  @Column(nullable = false)
  private String clasificacion;

  @Column(name = "tipo_afectacion")
  private String tipoAfectacion;

  @Column(name = "regla_afectada_id")
  private UUID reglaAfectadaId;

  @Column(name = "norma_afectada_id")
  private UUID normaAfectadaId;

  @Column(name = "articulo_afectado")
  private String articuloAfectado;

  @Column(name = "afectacion_desde_tipo")
  private String afectacionDesdeTipo;

  @Column(name = "afectacion_desde_fecha")
  private LocalDate afectacionDesdeFecha;

  @Column(name = "afectacion_desde_periodo")
  private String afectacionDesdePeriodo;

  /** Datos propios de la categoría, como texto JSON. */
  @Lob private String datos;

  /** Constructor sin argumentos que exige JPA. */
  protected ReglaEntity() {}

  /** Crea una regla. */
  public ReglaEntity(
      UUID id,
      UUID normaId,
      String categoria,
      String objeto,
      String enunciado,
      String fuente,
      ColumnasDeMomento vigenteDesde,
      ColumnasDeMomento vigenteHasta,
      String clasificacion,
      String tipoAfectacion,
      UUID reglaAfectadaId,
      UUID normaAfectadaId,
      String articuloAfectado,
      ColumnasDeMomento afectacionDesde,
      String datos) {
    this.id = id;
    this.normaId = normaId;
    this.categoria = categoria;
    this.objeto = objeto;
    this.enunciado = enunciado;
    this.fuente = fuente;
    this.vigenteDesdeTipo = vigenteDesde == null ? null : vigenteDesde.tipo();
    this.vigenteDesdeFecha = vigenteDesde == null ? null : vigenteDesde.fecha();
    this.vigenteDesdePeriodo = vigenteDesde == null ? null : vigenteDesde.periodo();
    this.vigenteHastaTipo = vigenteHasta == null ? null : vigenteHasta.tipo();
    this.vigenteHastaFecha = vigenteHasta == null ? null : vigenteHasta.fecha();
    this.vigenteHastaPeriodo = vigenteHasta == null ? null : vigenteHasta.periodo();
    this.clasificacion = clasificacion;
    this.tipoAfectacion = tipoAfectacion;
    this.reglaAfectadaId = reglaAfectadaId;
    this.normaAfectadaId = normaAfectadaId;
    this.articuloAfectado = articuloAfectado;
    this.afectacionDesdeTipo = afectacionDesde == null ? null : afectacionDesde.tipo();
    this.afectacionDesdeFecha = afectacionDesde == null ? null : afectacionDesde.fecha();
    this.afectacionDesdePeriodo = afectacionDesde == null ? null : afectacionDesde.periodo();
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

  public ColumnasDeMomento getVigenteDesde() {
    return vigenteDesdeTipo == null
        ? null
        : new ColumnasDeMomento(vigenteDesdeTipo, vigenteDesdeFecha, vigenteDesdePeriodo);
  }

  public ColumnasDeMomento getVigenteHasta() {
    return vigenteHastaTipo == null
        ? null
        : new ColumnasDeMomento(vigenteHastaTipo, vigenteHastaFecha, vigenteHastaPeriodo);
  }

  public ColumnasDeMomento getAfectacionDesde() {
    return afectacionDesdeTipo == null
        ? null
        : new ColumnasDeMomento(afectacionDesdeTipo, afectacionDesdeFecha, afectacionDesdePeriodo);
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

  public UUID getNormaAfectadaId() {
    return normaAfectadaId;
  }

  public String getArticuloAfectado() {
    return articuloAfectado;
  }

  public String getDatos() {
    return datos;
  }
}
