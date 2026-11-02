package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Norma que se incorpora al consolidado (glosario, «Acuerdo (o norma)»). Es una raíz: se crea con
 * {@link #registrar} y se reconstruye desde la base con {@link #reconstituir}. Es inmutable y su
 * identidad es su {@link NormaId}; su identificación (tipo, número, año y órgano) se registra una
 * sola vez.
 */
public final class Norma {

  private final NormaId id;
  private final IdentificacionNorma identificacion;
  private final TipoDeEfecto tipoDeEfecto;
  private final LocalDate fechaExpedicion;
  private final Vigencia vigencia;
  private final Optional<String> enlaceRegimenLegal;
  private final Alcance alcance;

  private Norma(
      NormaId id,
      IdentificacionNorma identificacion,
      TipoDeEfecto tipoDeEfecto,
      LocalDate fechaExpedicion,
      Vigencia vigencia,
      Optional<String> enlaceRegimenLegal,
      Alcance alcance) {
    this.id = ArgumentChecks.obligatorio(id, "id");
    this.identificacion = ArgumentChecks.obligatorio(identificacion, "identificacion");
    this.tipoDeEfecto = ArgumentChecks.obligatorio(tipoDeEfecto, "tipoDeEfecto");
    this.fechaExpedicion = ArgumentChecks.obligatorio(fechaExpedicion, "fechaExpedicion");
    this.vigencia = ArgumentChecks.obligatorio(vigencia, "vigencia");
    this.enlaceRegimenLegal = ArgumentChecks.obligatorio(enlaceRegimenLegal, "enlaceRegimenLegal");
    this.enlaceRegimenLegal.ifPresent(
        valor -> ArgumentChecks.obligatorioNoVacio(valor, "enlaceRegimenLegal"));
    this.alcance = ArgumentChecks.obligatorio(alcance, "alcance");
    if (tipoDeEfecto.exigeVigenciaHasta() && !vigencia.esTemporal()) {
      throw new IllegalArgumentException(
          "una excepción o medida temporal debe tener vigencia hasta");
    }
  }

  /** Registra una norma nueva, con un identificador generado en el dominio. */
  public static Norma registrar(
      IdentificacionNorma identificacion,
      TipoDeEfecto tipoDeEfecto,
      LocalDate fechaExpedicion,
      Vigencia vigencia,
      Optional<String> enlaceRegimenLegal,
      Alcance alcance) {
    return new Norma(
        NormaId.nuevo(),
        identificacion,
        tipoDeEfecto,
        fechaExpedicion,
        vigencia,
        enlaceRegimenLegal,
        alcance);
  }

  /** Reconstruye una norma ya guardada, con su identificador. No genera uno nuevo. */
  public static Norma reconstituir(
      NormaId id,
      IdentificacionNorma identificacion,
      TipoDeEfecto tipoDeEfecto,
      LocalDate fechaExpedicion,
      Vigencia vigencia,
      Optional<String> enlaceRegimenLegal,
      Alcance alcance) {
    return new Norma(
        id, identificacion, tipoDeEfecto, fechaExpedicion, vigencia, enlaceRegimenLegal, alcance);
  }

  /** Identificador de la norma. */
  public NormaId id() {
    return id;
  }

  /** Identificación de la norma: tipo, número, año y órgano. */
  public IdentificacionNorma identificacion() {
    return identificacion;
  }

  /** Efecto de la norma sobre el consolidado. */
  public TipoDeEfecto tipoDeEfecto() {
    return tipoDeEfecto;
  }

  /** Fecha en que se expidió la norma. */
  public LocalDate fechaExpedicion() {
    return fechaExpedicion;
  }

  /** Vigencia de la norma: desde cuándo rige y hasta cuándo, si tiene fin. */
  public Vigencia vigencia() {
    return vigencia;
  }

  /** Enlace a Régimen Legal, si la norma ya está publicada allí. */
  public Optional<String> enlaceRegimenLegal() {
    return enlaceRegimenLegal;
  }

  /** Alcance de la norma: los programas a los que aplica. */
  public Alcance alcance() {
    return alcance;
  }

  /** Dos normas son la misma si tienen el mismo identificador. */
  @Override
  public boolean equals(Object otra) {
    return otra instanceof Norma norma && id.equals(norma.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Norma[id=" + id.valor() + ", identificacion=" + identificacion + "]";
  }
}
