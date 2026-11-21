package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;
import java.util.Optional;

/**
 * Vista inmutable de una regla completa, con lo que UC06 muestra al abrirla (paso 8): su enunciado,
 * su fuente, su vigencia, el alcance de su norma, si el plan la aplica o solo la informa y la
 * cadena de normas que la han afectado.
 *
 * @param id identificador de la regla
 * @param enunciado texto de la regla
 * @param fuente fuente de la regla; vacía si un error de datos la dejó «sin fuente registrada»
 * @param estado estado de la regla en el consolidado vigente
 * @param temporal si la regla es temporal por su norma o por una vigencia propia
 * @param clasificacion si el plan la aplica o solo la informa
 * @param aplicaDesde momento desde el que rige: el de su vigencia propia, si la tiene, o el de su
 *     norma; vacío si no tiene norma
 * @param vigenteHasta momento hasta el que rige, con el mismo criterio; vacío si no tiene fin
 * @param alcance alcance de su norma; vacío si no tiene norma
 * @param detalleDelAlcance texto con el que la norma fija su alcance; vacío si no lo trae
 * @param cadena la norma que la expidió y las que la afectan, de la más antigua a la más reciente;
 *     vacía si no tiene norma
 */
public record ReglaCompletaVista(
    ReglaId id,
    String enunciado,
    Optional<FuenteVista> fuente,
    EstadoRegla estado,
    boolean temporal,
    ClasificacionRegla clasificacion,
    Optional<Momento> aplicaDesde,
    Optional<Momento> vigenteHasta,
    Optional<TipoAlcance> alcance,
    Optional<String> detalleDelAlcance,
    List<EslabonVista> cadena) {

  /** Exige lo obligatorio y copia la cadena para que nadie pueda modificarla. */
  public ReglaCompletaVista {
    ArgumentChecks.obligatorio(id, "id");
    ArgumentChecks.obligatorioNoVacio(enunciado, "enunciado");
    ArgumentChecks.obligatorio(fuente, "fuente");
    ArgumentChecks.obligatorio(estado, "estado");
    ArgumentChecks.obligatorio(clasificacion, "clasificacion");
    ArgumentChecks.obligatorio(aplicaDesde, "aplicaDesde");
    ArgumentChecks.obligatorio(vigenteHasta, "vigenteHasta");
    ArgumentChecks.obligatorio(alcance, "alcance");
    ArgumentChecks.obligatorio(detalleDelAlcance, "detalleDelAlcance");
    cadena = ArgumentChecks.copiaSinNulos(cadena, "cadena");
  }

  /** Si la regla ha sido afectada por otra norma, es decir, si la cadena tiene más de un paso. */
  public boolean tieneCadena() {
    return cadena.size() > 1;
  }
}
