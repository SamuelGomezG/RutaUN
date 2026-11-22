package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.core.JacksonException;

/**
 * Convierte entre la raíz {@link VersionDelConsolidado} y sus filas: la de la versión ({@code
 * version_consolidado}) y una por regla con su estado ({@code regla_en_version}).
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link VersionDelConsolidado#reconstituir}, así que no calcula
 * nada, no genera eventos y pasa por las mismas validaciones que una versión creada. Las reglas las
 * reconstruye {@link ReglaMapper} y el mapper las une a su estado en esta versión en un {@link
 * ReglaEnVersion}; el mapper no lee la base, de modo que quien lo llama trae las reglas.
 *
 * <p>#143: la versión contiene sus filas de {@code regla_en_version}, así que una fila no puede ser
 * de otra versión y no hace falta comprobarlo.
 *
 * <p>Una versión es una raíz de solo agregar: cada llamada a {@link #aEntidad} produce
 * identificadores nuevos, y guardar dos veces la misma versión choca con la restricción de unicidad
 * del programa y el número, que es lo que se espera.
 *
 * <p>Lo que el esquema y el dominio de hoy no calzan, y qué hace el mapper (lo corrige #145):
 *
 * <ul>
 *   <li>El administrador del motivo es un texto en el dominio y {@code administrador_id} es un
 *       UUID: va dentro de {@code detalle_motivo} y la columna queda en {@code null} (D-11, #91).
 *   <li>{@code regla_en_version} no guarda el orden de las reglas, y el dominio no lo necesita: la
 *       versión es un conjunto de reglas y el orden en que se muestran lo calcula la consulta. Se
 *       leen ordenadas por identificador para que la lectura sea determinista.
 *   <li>La columna {@code cambios} no tiene contraparte en el dominio todavía: queda en {@code
 *       null} (pendiente; anotado en #145).
 * </ul>
 */
public final class VersionDelConsolidadoMapper {

  /** Largo máximo de {@code detalle_motivo} en el esquema. */
  private static final int LARGO_DEL_DETALLE = 300;

  /** Los motivos de versión, con el nombre que se guarda y la clase de su detalle. */
  private enum TipoDeMotivo {
    POR_INCORPORACION(MotivoDeVersion.PorIncorporacion.class),
    POR_CORRECCION(MotivoDeVersion.PorCorreccion.class),
    AUTOMATICO_POR_PERIODO(MotivoDeVersion.AutomaticoPorPeriodo.class),
    AUTOMATICO_POR_FECHA(MotivoDeVersion.AutomaticoPorFecha.class);

    private final Class<? extends MotivoDeVersion> clase;

    TipoDeMotivo(Class<? extends MotivoDeVersion> clase) {
      this.clase = clase;
    }

    /** Un {@code switch} exhaustivo: el compilador avisa si se agrega un motivo. */
    static TipoDeMotivo de(MotivoDeVersion motivo) {
      return switch (motivo) {
        case MotivoDeVersion.PorIncorporacion ignored -> POR_INCORPORACION;
        case MotivoDeVersion.PorCorreccion ignored -> POR_CORRECCION;
        case MotivoDeVersion.AutomaticoPorPeriodo ignored -> AUTOMATICO_POR_PERIODO;
        case MotivoDeVersion.AutomaticoPorFecha ignored -> AUTOMATICO_POR_FECHA;
      };
    }

    static TipoDeMotivo desde(String nombre) {
      ArgumentChecks.obligatorioNoVacio(nombre, "motivo");
      for (TipoDeMotivo tipo : values()) {
        if (tipo.name().equals(nombre)) {
          return tipo;
        }
      }
      throw new IllegalArgumentException("motivo de versión desconocido: " + nombre);
    }
  }

  private VersionDelConsolidadoMapper() {}

  /**
   * Reconstruye la versión que guardan estas filas.
   *
   * @param version fila de la versión, con sus filas de {@code regla_en_version}
   * @param reglasPorId reglas citadas, ya reconstruidas con la norma que las contiene
   * @throws IllegalArgumentException si falta una regla, si el motivo o un estado son desconocidos
   *     o si algún dato es inválido para el dominio
   */
  public static VersionDelConsolidado aDominio(
      VersionEntity version, Map<UUID, Regla> reglasPorId) {
    ArgumentChecks.obligatorio(version, "version");
    ArgumentChecks.obligatorio(reglasPorId, "reglasPorId");
    List<ReglaEnVersion> reglas =
        version.getReglasEnVersion().stream()
            .sorted(Comparator.comparing(ReglaEnVersionEntity::getReglaId))
            .map(fila -> reglaDeLaVersion(version, fila, reglasPorId))
            .toList();
    return VersionDelConsolidado.reconstituir(
        new ProgramaId(version.getProgramaId()),
        version.getNumero(),
        version.getFecha(),
        motivoADominio(version),
        reglas,
        version.getNumeroPlanDeEstudios());
  }

  /**
   * Convierte la versión en la fila que se guarda en la base, con sus filas de reglas.
   *
   * @param version versión que se guarda
   * @throws IllegalArgumentException si el detalle del motivo no cabe en {@code detalle_motivo}
   */
  public static VersionEntity aEntidad(VersionDelConsolidado version) {
    ArgumentChecks.obligatorio(version, "version");
    List<ReglaEnVersionEntity> reglasEnVersion =
        version.reglas().stream()
            .map(
                enVersion ->
                    new ReglaEnVersionEntity(
                        UUID.randomUUID(),
                        enVersion.regla().id().valor(),
                        enVersion.estado().name()))
            .toList();
    return new VersionEntity(
        UUID.randomUUID(),
        version.programa().valor(),
        version.numero(),
        version.fecha(),
        TipoDeMotivo.de(version.motivo()).name(),
        detalleDelMotivo(version.motivo()),
        null,
        version.versionDelPlan(),
        null,
        diaAutomatico(version),
        reglasEnVersion);
  }

  private static LocalDate diaAutomatico(VersionDelConsolidado version) {
    return switch (version.motivo()) {
      case MotivoDeVersion.AutomaticoPorFecha ignored -> version.fecha();
      case MotivoDeVersion.PorIncorporacion ignored -> null;
      case MotivoDeVersion.PorCorreccion ignored -> null;
      case MotivoDeVersion.AutomaticoPorPeriodo ignored -> null;
    };
  }

  private static ReglaEnVersion reglaDeLaVersion(
      VersionEntity version, ReglaEnVersionEntity fila, Map<UUID, Regla> reglasPorId) {
    Regla regla = reglasPorId.get(fila.getReglaId());
    if (regla == null) {
      throw new IllegalArgumentException(
          "falta la regla "
              + fila.getReglaId()
              + " que cita la versión "
              + version.getNumero()
              + " del programa "
              + version.getProgramaId());
    }
    return new ReglaEnVersion(regla, EstadoRegla.valueOf(fila.getEstado()));
  }

  private static MotivoDeVersion motivoADominio(VersionEntity version) {
    TipoDeMotivo tipo = TipoDeMotivo.desde(version.getMotivo());
    String detalle = ArgumentChecks.obligatorioNoVacio(version.getDetalleMotivo(), "detalleMotivo");
    try {
      return JsonDeNormativa.mapper().readValue(detalle, tipo.clase);
    } catch (JacksonException e) {
      throw new IllegalArgumentException(
          "el detalle del motivo de la versión "
              + version.getNumero()
              + " no es válido para "
              + tipo,
          e);
    }
  }

  private static String detalleDelMotivo(MotivoDeVersion motivo) {
    String detalle = JsonDeNormativa.mapper().writeValueAsString(motivo);
    if (detalle.length() > LARGO_DEL_DETALLE) {
      throw new IllegalArgumentException(
          "el detalle del motivo no cabe en detalle_motivo: tiene "
              + detalle.length()
              + " caracteres y el máximo es "
              + LARGO_DEL_DETALLE);
    }
    return detalle;
  }
}
