package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Norma leída del JSON antes de resolver los programas de su alcance. El {@link NormaId} es
 * provisional y lo conserva la importación al crear la norma definitiva; las reglas ya lo usan como
 * fuente.
 *
 * @param id identificador provisional de la norma
 * @param identificacion identificación de la norma
 * @param tipoDeEfecto efecto de la norma
 * @param fechaExpedicion fecha de expedición
 * @param vigencia vigencia de la norma
 * @param enlaceRegimenLegal enlace a Régimen Legal, si existe
 * @param alcance alcance con facultades y códigos SIA, sin identificadores internos
 */
public record NormaLeida(
    NormaId id,
    IdentificacionNorma identificacion,
    TipoDeEfecto tipoDeEfecto,
    LocalDate fechaExpedicion,
    Vigencia vigencia,
    Optional<String> enlaceRegimenLegal,
    AlcanceLeida alcance) {

  /** Exige los datos y valida el enlace y la vigencia de las medidas temporales. */
  public NormaLeida {
    ArgumentChecks.obligatorio(id, "id");
    ArgumentChecks.obligatorio(identificacion, "identificacion");
    ArgumentChecks.obligatorio(tipoDeEfecto, "tipoDeEfecto");
    ArgumentChecks.obligatorio(fechaExpedicion, "fechaExpedicion");
    ArgumentChecks.obligatorio(vigencia, "vigencia");
    ArgumentChecks.obligatorio(enlaceRegimenLegal, "enlaceRegimenLegal");
    enlaceRegimenLegal.ifPresent(
        valor -> ArgumentChecks.obligatorioNoVacio(valor, "enlaceRegimenLegal"));
    ArgumentChecks.obligatorio(alcance, "alcance");
    if (tipoDeEfecto.exigeVigenciaHasta() && !vigencia.esTemporal()) {
      throw new IllegalArgumentException(
          "una excepción o medida temporal debe tener vigencia hasta");
    }
  }
}
