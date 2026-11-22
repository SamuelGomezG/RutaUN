package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.TipoAfectacion;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link Regla} (dominio, entidad, base, entidad, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate. La regla se guarda dentro de su norma y
 * se lee en una transacción, porque la colección es perezosa (#143).
 */
@PruebaDeIntegracion
class ReglaMapperTest {

  @Autowired private NormaRepository normas;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private TransactionTemplate transaccion;

  private final List<UUID> normasGuardadas = new ArrayList<>();
  private final List<UUID> reglasGuardadas = new ArrayList<>();

  /**
   * Borra lo que guardó la prueba, en orden inverso, porque aquí sí se hizo commit y una regla no
   * se puede borrar antes que la que la afecta.
   */
  @AfterEach
  void limpiar() {
    for (int i = reglasGuardadas.size() - 1; i >= 0; i--) {
      jdbc.update(
          "DELETE FROM regla WHERE CAST(id AS VARCHAR) = ?", reglasGuardadas.get(i).toString());
    }
    for (UUID normaId : normasGuardadas) {
      jdbc.update("DELETE FROM norma WHERE CAST(id AS VARCHAR) = ?", normaId.toString());
    }
  }

  @Test
  void tt04_reglaDeCadaCategoriaIdaYVuelta() {
    List<DatosDeRegla> todos = ReglasDePrueba.datosDeTodasLasCategorias();

    for (DatosDeRegla datos : todos) {
      Norma norma = ReglasDePrueba.normaNueva();
      Regla original = ReglasDePrueba.regla(norma, datos);

      Regla reconstruida = guardarYLeer(norma, original);

      // Regla.equals solo compara el id; la comparación recursiva revisa todos los campos.
      assertThat(reconstruida)
          .as("regla de datos %s", datos.getClass().getSimpleName())
          .usingRecursiveComparison()
          .isEqualTo(original);
    }
  }

  @Test
  void tt04_reglaConVigenciaPropiaYAfectacionIdaYVuelta() {
    Norma normaDeLaAfectada = ReglasDePrueba.normaNueva();
    Regla afectada = ReglasDePrueba.regla(normaDeLaAfectada, new DatosDeRegla.DeOtraCategoria());
    guardarYLeer(normaDeLaAfectada, afectada);

    Norma normaConVigencia = ReglasDePrueba.normaNueva();
    Regla conVigenciaHasta =
        Regla.registrar(
            "Carga temporal",
            "Rige durante el 2026",
            new Fuente(normaConVigencia.id(), "art. 2 (transitorio)"),
            Optional.of(
                new Vigencia(
                    new Momento.EnFecha(LocalDate.of(2026, 1, 1)),
                    Optional.of(new Momento.EnFecha(LocalDate.of(2026, 12, 31))))),
            ClasificacionRegla.INFORMATIVA,
            Optional.of(
                new Afectacion(
                    TipoAfectacion.EXCEPTUA,
                    Optional.of(afectada.id()),
                    Optional.empty(),
                    Optional.empty())),
            new DatosDeRegla.DeOtraCategoria());
    Norma normaAbierta = ReglasDePrueba.normaNueva();
    Regla conVigenciaAbierta =
        Regla.registrar(
            "Carga permanente",
            "Rige desde el 2027",
            new Fuente(normaAbierta.id(), "art. 3"),
            Optional.of(
                new Vigencia(new Momento.EnFecha(LocalDate.of(2027, 1, 1)), Optional.empty())),
            ClasificacionRegla.DEL_PLAN,
            Optional.empty(),
            new DatosDeRegla.DeRequisitoDeGrado());

    assertThat(guardarYLeer(normaConVigencia, conVigenciaHasta))
        .usingRecursiveComparison()
        .isEqualTo(conVigenciaHasta);
    assertThat(guardarYLeer(normaAbierta, conVigenciaAbierta))
        .usingRecursiveComparison()
        .isEqualTo(conVigenciaAbierta);
  }

  @Test
  void tt04_reglaConVigenciaPorPeriodoNoSeGuarda() {
    Momento periodo = new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.PRIMERO));
    Momento fecha = new Momento.EnFecha(LocalDate.of(2026, 1, 1));
    Regla porPeriodo = conVigencia(new Vigencia(periodo, Optional.empty()));
    Regla finPorPeriodo = conVigencia(new Vigencia(fecha, Optional.of(periodo)));

    assertThatThrownBy(() -> ReglaMapper.aEntidad(porPeriodo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("vigenciaPropia.aplicaDesde");
    assertThatThrownBy(() -> ReglaMapper.aEntidad(finPorPeriodo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("vigenciaPropia.vigenteHasta");
  }

  @Test
  void tt04_reglaQueAfectaUnArticuloNoSeGuarda() {
    Norma norma = ReglasDePrueba.normaNueva();
    Regla conArticulo =
        conAfectacion(
            norma,
            new Afectacion(
                TipoAfectacion.MODIFICA,
                Optional.empty(),
                Optional.of(new Fuente(norma.id(), "art. 7")),
                Optional.empty()));

    assertThatThrownBy(() -> ReglaMapper.aEntidad(conArticulo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("afectación a un artículo");
  }

  @Test
  void tt04_reglaConAfectacionDesdeUnMomentoNoSeGuarda() {
    Norma norma = ReglasDePrueba.normaNueva();
    Regla conDesde =
        conAfectacion(
            norma,
            new Afectacion(
                TipoAfectacion.DEROGA,
                Optional.of(ReglaId.nuevo()),
                Optional.empty(),
                Optional.of(new Momento.EnFecha(LocalDate.of(2027, 1, 1)))));

    assertThatThrownBy(() -> ReglaMapper.aEntidad(conDesde))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("«desde»");
  }

  @Test
  void tt04_reglaConCategoriaDesconocidaNoSeReconstituye() {
    Norma norma = ReglasDePrueba.normaNueva();
    ReglaEntity invalida = entidadAMano("DECRETO", "{}");

    assertThatThrownBy(() -> ReglaMapper.aDominio(invalida, norma.id()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("DECRETO");
  }

  @Test
  void tt04_reglaConDatosIncompletosNoSeReconstituye() {
    // «PROGRAMA» exige creditosExigidos; el JSON vacío no lo trae.
    Norma norma = ReglasDePrueba.normaNueva();
    ReglaEntity invalida = entidadAMano("PROGRAMA", "{}");

    assertThatThrownBy(() -> ReglaMapper.aDominio(invalida, norma.id()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PROGRAMA");
  }

  /** Guarda la norma con su regla, la lee de la base y la reconstruye. */
  private Regla guardarYLeer(Norma norma, Regla regla) {
    normasGuardadas.add(norma.id().valor());
    reglasGuardadas.add(regla.id().valor());
    normas.save(NormaMapper.aEntidad(norma, List.of(regla)));
    NormaId normaId = norma.id();
    UUID reglaId = regla.id().valor();
    return transaccion.execute(
        estado -> {
          NormaEntity leida = normas.findById(normaId.valor()).orElseThrow();
          ReglaEntity fila =
              leida.getReglas().stream()
                  .filter(guardada -> guardada.getId().equals(reglaId))
                  .findFirst()
                  .orElseThrow();
          return ReglaMapper.aDominio(fila, normaId);
        });
  }

  private static Regla conVigencia(Vigencia vigencia) {
    Norma norma = ReglasDePrueba.normaNueva();
    return conVigencia(norma, vigencia);
  }

  private static Regla conVigencia(Norma norma, Vigencia vigencia) {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(norma.id(), "art. 1"),
        Optional.of(vigencia),
        ClasificacionRegla.INFORMATIVA,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }

  private static Regla conAfectacion(Norma norma, Afectacion afectacion) {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(norma.id(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        Optional.of(afectacion),
        new DatosDeRegla.DeOtraCategoria());
  }

  private static ReglaEntity entidadAMano(String categoria, String datos) {
    return new ReglaEntity(
        UUID.randomUUID(),
        categoria,
        "Objeto",
        "Enunciado",
        "art. 1",
        null,
        null,
        "DEL_PLAN",
        null,
        null,
        null,
        datos);
  }
}
