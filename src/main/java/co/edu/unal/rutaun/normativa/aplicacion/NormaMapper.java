package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.NivelDeOrgano;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Organo;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.AlcanceJson;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import co.edu.unal.rutaun.utilidades.JsonConfig;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convierte entre la raíz {@link Norma} y su fila {@link NormaEntity}. No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link Norma#reconstituir}, así que no genera un identificador
 * nuevo y pasa por las mismas validaciones que una norma creada. Si la base trae un dato inválido,
 * sube la {@link IllegalArgumentException} del dominio.
 *
 * <p>La tabla {@code norma} guarda la vigencia solo como fechas. Por eso {@link #aEntidad} rechaza
 * una vigencia dada por periodos académicos, y {@link #aDominio} siempre construye fechas. El
 * alcance se guarda como JSON en {@code alcance_detalle}, con las listas ordenadas para que el
 * texto sea estable.
 */
public final class NormaMapper {

  private NormaMapper() {}

  /** Reconstruye la norma que guarda esta fila. */
  public static Norma aDominio(NormaEntity entidad) {
    ArgumentChecks.obligatorio(entidad, "entidad");
    return Norma.reconstituir(
        new NormaId(entidad.getId()),
        new IdentificacionNorma(
            TipoNorma.valueOf(entidad.getTipo()),
            entidad.getNumero(),
            entidad.getAnio(),
            organoADominio(entidad)),
        TipoDeEfecto.valueOf(entidad.getTipoDeEfecto()),
        entidad.getFechaExpedicion(),
        new Vigencia(
            new Momento.EnFecha(entidad.getAplicaDesde()),
            Optional.ofNullable(entidad.getVigenteHasta()).<Momento>map(Momento.EnFecha::new)),
        Optional.ofNullable(entidad.getEnlaceRegimenLegal()),
        alcanceADominio(entidad));
  }

  /**
   * Convierte la norma en la fila que se guarda en la base.
   *
   * <p>Falla con {@link IllegalArgumentException} si la vigencia de la norma está dada por un
   * periodo académico, porque la tabla solo guarda fechas.
   */
  public static NormaEntity aEntidad(Norma norma) {
    ArgumentChecks.obligatorio(norma, "norma");
    return new NormaEntity(
        norma.id().valor(),
        norma.identificacion().tipo().name(),
        norma.identificacion().numero(),
        norma.identificacion().anio(),
        norma.identificacion().organo().nombre(),
        norma.identificacion().organo().nivel().name(),
        norma.identificacion().organo().facultad().map(Facultad::codigo).orElse(null),
        norma.identificacion().organo().clave(),
        norma.tipoDeEfecto().name(),
        norma.fechaExpedicion(),
        aFecha(norma.vigencia().aplicaDesde(), "aplicaDesde"),
        norma.vigencia().vigenteHasta().map(hasta -> aFecha(hasta, "vigenteHasta")).orElse(null),
        norma.enlaceRegimenLegal().orElse(null),
        norma.alcance().tipo().name(),
        alcanceAJson(norma.alcance()));
  }

  private static Organo organoADominio(NormaEntity entidad) {
    return new Organo(
        entidad.getOrganoNombre(),
        NivelDeOrgano.valueOf(entidad.getOrganoNivel()),
        Optional.ofNullable(entidad.getOrganoFacultadCodigo()).map(Facultad::new));
  }

  private static Alcance alcanceADominio(NormaEntity entidad) {
    AlcanceJson json =
        JsonConfig.mapper()
            .readValue(
                ArgumentChecks.obligatorioNoVacio(entidad.getAlcanceDetalle(), "alcanceDetalle"),
                AlcanceJson.class);
    return new Alcance(
        TipoAlcance.valueOf(entidad.getTipoAlcance()),
        aConjunto(json.facultades(), Facultad::new, "facultades"),
        aConjunto(json.programas(), ProgramaId::new, "programas"),
        aConjunto(json.exclusiones(), ProgramaId::new, "exclusiones"),
        json.texto());
  }

  private static String alcanceAJson(Alcance alcance) {
    return JsonConfig.mapper()
        .writeValueAsString(
            new AlcanceJson(
                alcance.facultades().stream().map(Facultad::codigo).sorted().toList(),
                alcance.programas().stream().map(ProgramaId::valor).sorted().toList(),
                alcance.exclusiones().stream().map(ProgramaId::valor).sorted().toList(),
                alcance.texto()));
  }

  private static <T, R> Set<R> aConjunto(List<T> lista, Function<T, R> conversion, String nombre) {
    return ArgumentChecks.copiaSinNulos(lista, nombre).stream()
        .map(conversion)
        .collect(Collectors.toSet());
  }

  private static LocalDate aFecha(Momento momento, String nombre) {
    return switch (momento) {
      case Momento.EnFecha enFecha -> enFecha.fecha();
      case Momento.EnPeriodo enPeriodo ->
          throw new IllegalArgumentException(
              nombre + " debe ser una fecha: la tabla norma no guarda periodos académicos");
    };
  }
}
