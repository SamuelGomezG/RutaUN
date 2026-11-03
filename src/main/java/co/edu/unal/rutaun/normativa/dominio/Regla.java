package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Regla del consolidado (glosario, «Regla»): un enunciado sobre un objeto, con su fuente, su
 * vigencia propia si la tiene, su clasificación, su estado y, si afecta a otra regla, su
 * afectación. Su categoría es el tipo de sus {@link DatosDeRegla}. Es inmutable y su identidad es
 * su {@link ReglaId}.
 */
public final class Regla {

  private final ReglaId id;
  private final String objeto;
  private final String enunciado;
  private final Fuente fuente;
  private final Optional<Vigencia> vigenciaPropia;
  private final ClasificacionRegla clasificacion;
  private final EstadoRegla estado;
  private final Optional<Afectacion> afectacion;
  private final DatosDeRegla datos;

  private Regla(
      ReglaId id,
      String objeto,
      String enunciado,
      Fuente fuente,
      Optional<Vigencia> vigenciaPropia,
      ClasificacionRegla clasificacion,
      EstadoRegla estado,
      Optional<Afectacion> afectacion,
      DatosDeRegla datos) {
    this.id = ArgumentChecks.obligatorio(id, "id");
    this.objeto = ArgumentChecks.obligatorioNoVacio(objeto, "objeto");
    this.enunciado = ArgumentChecks.obligatorioNoVacio(enunciado, "enunciado");
    this.fuente = ArgumentChecks.obligatorio(fuente, "fuente");
    this.vigenciaPropia = ArgumentChecks.obligatorio(vigenciaPropia, "vigenciaPropia");
    this.clasificacion = ArgumentChecks.obligatorio(clasificacion, "clasificacion");
    this.estado = ArgumentChecks.obligatorio(estado, "estado");
    this.afectacion = ArgumentChecks.obligatorio(afectacion, "afectacion");
    this.datos = ArgumentChecks.obligatorio(datos, "datos");
    if (clasificacion == ClasificacionRegla.DEL_PLAN
        && vigenciaPropia.isPresent()
        && vigenciaPropia.get().esTemporal()) {
      throw new IllegalArgumentException("una regla temporal no puede ser regla del plan");
    }
  }

  /** Registra una regla nueva, con un identificador generado en el dominio. */
  public static Regla registrar(
      String objeto,
      String enunciado,
      Fuente fuente,
      Optional<Vigencia> vigenciaPropia,
      ClasificacionRegla clasificacion,
      EstadoRegla estado,
      Optional<Afectacion> afectacion,
      DatosDeRegla datos) {
    return new Regla(
        ReglaId.nuevo(),
        objeto,
        enunciado,
        fuente,
        vigenciaPropia,
        clasificacion,
        estado,
        afectacion,
        datos);
  }

  /** Reconstruye una regla ya guardada, con su identificador. No genera uno nuevo. */
  public static Regla reconstituir(
      ReglaId id,
      String objeto,
      String enunciado,
      Fuente fuente,
      Optional<Vigencia> vigenciaPropia,
      ClasificacionRegla clasificacion,
      EstadoRegla estado,
      Optional<Afectacion> afectacion,
      DatosDeRegla datos) {
    return new Regla(
        id, objeto, enunciado, fuente, vigenciaPropia, clasificacion, estado, afectacion, datos);
  }

  /** Identificador de la regla. */
  public ReglaId id() {
    return id;
  }

  /** Objeto al que se refiere la regla. */
  public String objeto() {
    return objeto;
  }

  /** Enunciado de la regla. */
  public String enunciado() {
    return enunciado;
  }

  /** Fuente de la regla: su norma y su artículo o numeral. */
  public Fuente fuente() {
    return fuente;
  }

  /** Vigencia propia de la regla; vacía si rige con la de su norma. */
  public Optional<Vigencia> vigenciaPropia() {
    return vigenciaPropia;
  }

  /** Si el plan aplica la regla o solo la informa. */
  public ClasificacionRegla clasificacion() {
    return clasificacion;
  }

  /** Estado de la regla en la versión del consolidado. */
  public EstadoRegla estado() {
    return estado;
  }

  /** Regla o artículo que esta regla modifica, deroga o exceptúa; vacío si no afecta a otra. */
  public Optional<Afectacion> afectacion() {
    return afectacion;
  }

  /** Datos propios de la categoría de la regla. */
  public DatosDeRegla datos() {
    return datos;
  }

  /** Dos reglas son la misma si tienen el mismo identificador. */
  @Override
  public boolean equals(Object otra) {
    return otra instanceof Regla regla && id.equals(regla.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Regla[id=" + id.valor() + ", objeto=" + objeto + ", fuente=" + fuente + "]";
  }
}
