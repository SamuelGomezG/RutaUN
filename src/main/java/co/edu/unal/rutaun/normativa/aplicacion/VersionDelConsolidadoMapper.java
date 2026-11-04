package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntidad;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.core.JacksonException;

/**
 * Convierte entre la raíz {@link VersionDelConsolidado} y sus filas: la de la versión ({@code
 * version_consolidado}) y una por regla con su estado ({@code regla_en_version}). No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link VersionDelConsolidado#reconstituir}, así que no calcula
 * nada, no genera eventos y pasa por las mismas validaciones que una versión creada. Las reglas las
 * reconstruye {@link ReglaMapper} con el estado que cada una tiene en esta versión; el mapper no
 * lee la base, de modo que quien lo llama trae las filas.
 *
 * <p>Una versión es una raíz de solo agregar: cada llamada a {@link #aEntidades} produce
 * identificadores nuevos, y guardar dos veces la misma versión choca con la restricción de unicidad
 * del programa y el número, que es lo que se espera.
 *
 * <p>Lo que el esquema y el dominio de hoy no calzan, y qué hace el mapper (la revisión del sprint
 * 1 lo corrige):
 *
 * <ul>
 *   <li>El administrador del motivo es un texto en el dominio y {@code administrador_id} es un
 *       UUID: va dentro de {@code detalle_motivo} y la columna queda en {@code null} (R-06).
 *   <li>La tabla {@code consolidado} no existe en el dominio: {@code consolidadoId} entra como
 *       parámetro (R-06).
 *   <li>{@code regla_en_version} no guarda el orden de las reglas: se leen ordenadas por
 *       identificador.
 *   <li>La columna {@code cambios} no tiene contraparte en el dominio todavía: queda en {@code
 *       null}.
 * </ul>
 */
public final class VersionDelConsolidadoMapper {

  /** Largo máximo de {@code detalle_motivo} en el esquema. */
  private static final int LARGO_DEL_DETALLE = 300;

  /**
   * Las filas de una versión: la de la versión y una por regla, con su estado.
   *
   * @param version fila de {@code version_consolidado}
   * @param reglasEnVersion filas de {@code regla_en_version}, una por regla de la versión
   */
  public record Filas(VersionEntidad version, List<ReglaEnVersionEntidad> reglasEnVersion) {

    /** Exige la fila de la versión y deja la lista inmutable. */
    public Filas {
      ArgumentChecks.obligatorio(version, "version");
      reglasEnVersion = ArgumentChecks.copiaSinNulos(reglasEnVersion, "reglasEnVersion");
    }
  }

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
   * @param version fila de la versión
   * @param reglasEnVersion filas de {@code regla_en_version} de esa versión
   * @param reglasPorId filas de {@code regla} de todas las reglas citadas, por identificador
   * @throws IllegalArgumentException si una fila no es de esta versión, si falta una regla, si el
   *     motivo o un estado son desconocidos o si algún dato es inválido para el dominio
   */
  public static VersionDelConsolidado aDominio(
      VersionEntidad version,
      List<ReglaEnVersionEntidad> reglasEnVersion,
      Map<UUID, ReglaEntidad> reglasPorId) {
    ArgumentChecks.obligatorio(version, "version");
    ArgumentChecks.obligatorio(reglasEnVersion, "reglasEnVersion");
    ArgumentChecks.obligatorio(reglasPorId, "reglasPorId");
    List<Regla> reglas =
        reglasEnVersion.stream()
            .sorted(Comparator.comparing(ReglaEnVersionEntidad::getReglaId))
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
   * Convierte la versión en las filas que se guardan en la base.
   *
   * @param version versión que se guarda
   * @param consolidadoId fila de {@code consolidado} del programa, que quien guarda busca o crea
   * @throws IllegalArgumentException si el detalle del motivo no cabe en {@code detalle_motivo}
   */
  public static Filas aEntidades(VersionDelConsolidado version, UUID consolidadoId) {
    ArgumentChecks.obligatorio(version, "version");
    ArgumentChecks.obligatorio(consolidadoId, "consolidadoId");
    UUID versionId = UUID.randomUUID();
    VersionEntidad fila =
        new VersionEntidad(
            versionId,
            consolidadoId,
            version.programa().valor(),
            version.numero(),
            version.fecha(),
            TipoDeMotivo.de(version.motivo()).name(),
            detalleDelMotivo(version.motivo()),
            null,
            version.versionDelPlan(),
            null);
    List<ReglaEnVersionEntidad> reglasEnVersion =
        version.reglas().stream()
            .map(
                regla ->
                    new ReglaEnVersionEntidad(
                        UUID.randomUUID(), versionId, regla.id().valor(), regla.estado().name()))
            .toList();
    return new Filas(fila, reglasEnVersion);
  }

  private static Regla reglaDeLaVersion(
      VersionEntidad version, ReglaEnVersionEntidad fila, Map<UUID, ReglaEntidad> reglasPorId) {
    if (!version.getId().equals(fila.getVersionId())) {
      throw new IllegalArgumentException(
          "la regla " + fila.getReglaId() + " no es de la versión " + version.getId());
    }
    ReglaEntidad regla = reglasPorId.get(fila.getReglaId());
    if (regla == null) {
      throw new IllegalArgumentException(
          "falta la regla "
              + fila.getReglaId()
              + " que cita la versión "
              + version.getNumero()
              + " del programa "
              + version.getProgramaId());
    }
    return ReglaMapper.aDominio(regla, EstadoRegla.valueOf(fila.getEstado()));
  }

  private static MotivoDeVersion motivoADominio(VersionEntidad version) {
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
