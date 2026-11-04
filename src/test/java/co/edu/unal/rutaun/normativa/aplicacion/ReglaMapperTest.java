package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.TipoDeAfectacion;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link Regla} (dominio, entidad, base, entidad, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
@PruebaDeIntegracion
class ReglaMapperTest {

  @Autowired private NormaJpaRepository normas;
  @Autowired private ReglaJpaRepository reglas;

  private Norma norma;
  private final List<UUID> reglasGuardadas = new ArrayList<>();

  /** Guarda la norma de la que cuelgan las reglas (la tabla {@code regla} tiene su clave). */
  @BeforeEach
  void guardarLaNorma() {
    norma = ReglasDePrueba.normaNueva();
    normas.save(NormaMapper.aEntidad(norma));
  }

  /** Borra lo que guardó la prueba, en orden inverso, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    reglas.deleteAllById(reglasGuardadas);
    normas.deleteById(norma.id().valor());
  }

  @Test
  void tt04_reglaDeCadaCategoriaIdaYVuelta() {
    List<DatosDeRegla> todos = ReglasDePrueba.datosDeTodasLasCategorias();

    for (DatosDeRegla datos : todos) {
      Regla original = ReglasDePrueba.regla(norma, datos, EstadoRegla.VIGENTE);

      Regla reconstruida = guardarYLeer(original, EstadoRegla.VIGENTE);

      // Regla.equals solo compara el id; la comparación recursiva revisa todos los campos.
      assertThat(reconstruida)
          .as("regla de datos %s", datos.getClass().getSimpleName())
          .usingRecursiveComparison()
          .isEqualTo(original);
    }
  }

  @Test
  void tt04_reglaConVigenciaPropiaYAfectacionIdaYVuelta() {
    Regla afectada =
        ReglasDePrueba.regla(norma, new DatosDeRegla.DeOtraCategoria(), EstadoRegla.VIGENTE);
    guardarYLeer(afectada, EstadoRegla.VIGENTE);
    Regla conVigenciaHasta =
        Regla.registrar(
            "Carga temporal",
            "Rige durante el 2026",
            new Fuente(norma.id(), "art. 2 (transitorio)"),
            Optional.of(
                new Vigencia(
                    new Momento.EnFecha(LocalDate.of(2026, 1, 1)),
                    Optional.of(new Momento.EnFecha(LocalDate.of(2026, 12, 31))))),
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.PROGRAMADA,
            Optional.of(
                new Afectacion(
                    TipoDeAfectacion.EXCEPTUA,
                    Optional.of(afectada.id()),
                    Optional.empty(),
                    Optional.empty())),
            new DatosDeRegla.DeOtraCategoria());
    Regla conVigenciaAbierta =
        Regla.registrar(
            "Carga permanente",
            "Rige desde el 2027",
            new Fuente(norma.id(), "art. 3"),
            Optional.of(
                new Vigencia(new Momento.EnFecha(LocalDate.of(2027, 1, 1)), Optional.empty())),
            ClasificacionRegla.DEL_PLAN,
            EstadoRegla.PROGRAMADA,
            Optional.empty(),
            new DatosDeRegla.DeRequisitoDeGrado());

    assertThat(guardarYLeer(conVigenciaHasta, EstadoRegla.PROGRAMADA))
        .usingRecursiveComparison()
        .isEqualTo(conVigenciaHasta);
    assertThat(guardarYLeer(conVigenciaAbierta, EstadoRegla.PROGRAMADA))
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
    Regla conArticulo =
        conAfectacion(
            new Afectacion(
                TipoDeAfectacion.MODIFICA,
                Optional.empty(),
                Optional.of(new Fuente(norma.id(), "art. 7")),
                Optional.empty()));

    assertThatThrownBy(() -> ReglaMapper.aEntidad(conArticulo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("afectación a un artículo");
  }

  @Test
  void tt04_reglaConAfectacionDesdeUnMomentoNoSeGuarda() {
    Regla conDesde =
        conAfectacion(
            new Afectacion(
                TipoDeAfectacion.DEROGA,
                Optional.of(ReglaId.nuevo()),
                Optional.empty(),
                Optional.of(new Momento.EnFecha(LocalDate.of(2027, 1, 1)))));

    assertThatThrownBy(() -> ReglaMapper.aEntidad(conDesde))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("«desde»");
  }

  @Test
  void tt04_reglaConCategoriaDesconocidaNoSeReconstituye() {
    ReglaEntidad invalida = entidadAMano("DECRETO", "{}");

    assertThatThrownBy(() -> ReglaMapper.aDominio(invalida, EstadoRegla.VIGENTE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("DECRETO");
  }

  @Test
  void tt04_reglaConDatosIncompletosNoSeReconstituye() {
    // «PROGRAMA» exige creditosExigidos; el JSON vacío no lo trae.
    ReglaEntidad invalida = entidadAMano("PROGRAMA", "{}");

    assertThatThrownBy(() -> ReglaMapper.aDominio(invalida, EstadoRegla.VIGENTE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PROGRAMA");
  }

  /** Guarda la regla, la lee de la base y la reconstruye con el estado que tiene en su versión. */
  private Regla guardarYLeer(Regla regla, EstadoRegla estadoEnLaVersion) {
    reglasGuardadas.add(regla.id().valor());
    reglas.save(ReglaMapper.aEntidad(regla));
    ReglaEntidad leida = reglas.findById(regla.id().valor()).orElseThrow();
    return ReglaMapper.aDominio(leida, estadoEnLaVersion);
  }

  private Regla conVigencia(Vigencia vigencia) {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(norma.id(), "art. 1"),
        Optional.of(vigencia),
        ClasificacionRegla.INFORMATIVA,
        EstadoRegla.VIGENTE,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }

  private Regla conAfectacion(Afectacion afectacion) {
    return Regla.registrar(
        "Objeto",
        "Enunciado",
        new Fuente(norma.id(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        EstadoRegla.VIGENTE,
        Optional.of(afectacion),
        new DatosDeRegla.DeOtraCategoria());
  }

  private ReglaEntidad entidadAMano(String categoria, String datos) {
    return new ReglaEntidad(
        UUID.randomUUID(),
        norma.id().valor(),
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
