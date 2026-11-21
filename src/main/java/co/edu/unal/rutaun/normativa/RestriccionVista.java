package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Vista inmutable de una restricción de una materia, con lo que UC05 muestra de ella (paso 4, GE 1
 * y GE 2): qué exige, su fuente, su fecha de aplicación y su vigencia.
 *
 * @param regla regla del consolidado que la expresa; con ella se abre la regla completa
 * @param tipo si es un prerrequisito o un correquisito
 * @param enunciado texto de la regla
 * @param exige materias que la satisfacen
 * @param fuente fuente de la regla; vacía si quedó «sin fuente registrada» (ext. 4d)
 * @param estado estado de la regla en el consolidado vigente
 * @param temporal si la regla es temporal por su norma o por una vigencia propia (ext. 4e)
 * @param aplicaDesde fecha desde la que rige: la de su vigencia propia, si la tiene, o la de su
 *     norma; vacía si no tiene norma
 * @param vigenteHasta fecha hasta la que rige, con el mismo criterio; vacía si no tiene fin
 */
public record RestriccionVista(
    ReglaId regla,
    TipoDeRestriccion tipo,
    String enunciado,
    ExpresionVista exige,
    Optional<FuenteVista> fuente,
    EstadoRegla estado,
    boolean temporal,
    Optional<LocalDate> aplicaDesde,
    Optional<LocalDate> vigenteHasta) {

  /** Exige lo obligatorio; la fuente y las fechas pueden faltar, pero no ser null. */
  public RestriccionVista {
    ArgumentChecks.obligatorio(regla, "regla");
    ArgumentChecks.obligatorio(tipo, "tipo");
    ArgumentChecks.obligatorioNoVacio(enunciado, "enunciado");
    ArgumentChecks.obligatorio(exige, "exige");
    ArgumentChecks.obligatorio(fuente, "fuente");
    ArgumentChecks.obligatorio(estado, "estado");
    ArgumentChecks.obligatorio(aplicaDesde, "aplicaDesde");
    ArgumentChecks.obligatorio(vigenteHasta, "vigenteHasta");
  }
}
