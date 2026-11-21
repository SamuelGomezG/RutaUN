package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.AlcanceJson;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import co.edu.unal.rutaun.utilidades.JsonConfig;
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
 * <p>La vigencia se guarda como un momento, en tres columnas: su tipo, su fecha o su periodo. El
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
            entidad.getOrgano()),
        TipoDeEfecto.valueOf(entidad.getTipoDeEfecto()),
        entidad.getFechaExpedicion(),
        new Vigencia(
            MomentoEnColumnas.aMomento(entidad.getAplicaDesde()),
            Optional.ofNullable(MomentoEnColumnas.aMomento(entidad.getVigenteHasta()))),
        Optional.ofNullable(entidad.getEnlaceRegimenLegal()),
        alcanceADominio(entidad));
  }

  /** Convierte la norma en la fila que se guarda en la base. */
  public static NormaEntity aEntidad(Norma norma) {
    ArgumentChecks.obligatorio(norma, "norma");
    return new NormaEntity(
        norma.id().valor(),
        norma.identificacion().tipo().name(),
        norma.identificacion().numero(),
        norma.identificacion().anio(),
        norma.identificacion().organo(),
        norma.tipoDeEfecto().name(),
        norma.fechaExpedicion(),
        MomentoEnColumnas.aColumnas(norma.vigencia().aplicaDesde()),
        norma.vigencia().vigenteHasta().map(MomentoEnColumnas::aColumnas).orElse(null),
        norma.enlaceRegimenLegal().orElse(null),
        norma.alcance().tipo().name(),
        alcanceAJson(norma.alcance()));
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
                alcance.facultades().stream().map(Facultad::nombre).sorted().toList(),
                alcance.programas().stream().map(ProgramaId::valor).sorted().toList(),
                alcance.exclusiones().stream().map(ProgramaId::valor).sorted().toList(),
                alcance.texto()));
  }

  private static <T, R> Set<R> aConjunto(List<T> lista, Function<T, R> conversion, String nombre) {
    return ArgumentChecks.copiaSinNulos(lista, nombre).stream()
        .map(conversion)
        .collect(Collectors.toSet());
  }
}
