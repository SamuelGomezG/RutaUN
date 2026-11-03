package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link Norma} (dominio, entidad, base, entidad, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
@PruebaDeIntegracion
class NormaMapperTest {

  private static final String ALCANCE_TODOS =
      "{\"facultades\":[],\"programas\":[],\"exclusiones\":[],\"texto\":\"Todos\"}";

  @Autowired private NormaJpaRepository normas;

  private UUID normaId;

  /** Borra lo que guardó la prueba, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    if (normaId != null) {
      normas.deleteById(normaId);
    }
  }

  @Test
  void tt04_normaIdaYVuelta() {
    ProgramaId primero = ProgramaId.nuevo();
    ProgramaId segundo = ProgramaId.nuevo();
    ProgramaId tercero = ProgramaId.nuevo();
    Norma original =
        Norma.registrar(
            new IdentificacionNorma(
                TipoNorma.ACUERDO, numeroUnico(), 2024, "Consejo Superior Universitario"),
            TipoDeEfecto.MODIFICATORIA,
            LocalDate.of(2024, 7, 15),
            new Vigencia(
                new Momento.EnFecha(LocalDate.of(2024, 8, 1)),
                Optional.of(new Momento.EnFecha(LocalDate.of(2025, 7, 31)))),
            Optional.of("https://www.legal.unal.edu.co/rlunal/home/doc.jsp?d_i=12345"),
            new Alcance(
                TipoAlcance.PROGRAMAS,
                Set.of(),
                Set.of(primero, segundo, tercero),
                Set.of(segundo),
                "Aplica a tres programas, salvo uno."));
    normaId = original.id().valor();

    normas.save(NormaMapper.aEntidad(original));
    NormaEntidad leida = normas.findById(normaId).orElseThrow();
    Norma reconstruida = NormaMapper.aDominio(leida);

    // Norma.equals solo compara el id; la comparación recursiva revisa todos los campos.
    assertThat(reconstruida).usingRecursiveComparison().isEqualTo(original);
  }

  @Test
  void tt04_normaMinimaIdaYVuelta() {
    Norma original =
        Norma.registrar(
            new IdentificacionNorma(TipoNorma.RESOLUCION, numeroUnico(), 2023, "Consejo de Sede"),
            TipoDeEfecto.BASE,
            LocalDate.of(2023, 3, 1),
            new Vigencia(new Momento.EnFecha(LocalDate.of(2023, 3, 1)), Optional.empty()),
            Optional.empty(),
            new Alcance(
                TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), "Aplica a todos los programas."));
    normaId = original.id().valor();

    normas.save(NormaMapper.aEntidad(original));
    NormaEntidad leida = normas.findById(normaId).orElseThrow();
    Norma reconstruida = NormaMapper.aDominio(leida);

    assertThat(reconstruida).usingRecursiveComparison().isEqualTo(original);
  }

  @Test
  void tt04_normaConTipoDesconocidoNoSeReconstituye() {
    NormaEntidad invalida = entidadAMano("DECRETO", "TODOS", ALCANCE_TODOS);

    assertThatThrownBy(() -> NormaMapper.aDominio(invalida))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("DECRETO");
  }

  @Test
  void tt04_normaConAlcanceIncoherenteNoSeReconstituye() {
    String conFacultades =
        "{\"facultades\":[\"Ingeniería\"],\"programas\":[],"
            + "\"exclusiones\":[],\"texto\":\"Todos\"}";
    NormaEntidad invalida = entidadAMano("ACUERDO", "TODOS", conFacultades);

    assertThatThrownBy(() -> NormaMapper.aDominio(invalida))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("alcance de todos");
  }

  @Test
  void tt04_normaConPeriodoNoSeGuarda() {
    Norma conPeriodo =
        Norma.registrar(
            new IdentificacionNorma(TipoNorma.CIRCULAR, numeroUnico(), 2026, "Vicerrectoría"),
            TipoDeEfecto.BASE,
            LocalDate.of(2026, 1, 20),
            new Vigencia(
                new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.PRIMERO)),
                Optional.empty()),
            Optional.empty(),
            new Alcance(
                TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), "Aplica a todos los programas."));

    assertThatThrownBy(() -> NormaMapper.aEntidad(conPeriodo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("aplicaDesde");
  }

  private static String numeroUnico() {
    return "T3-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private static NormaEntidad entidadAMano(String tipo, String tipoAlcance, String alcanceJson) {
    return new NormaEntidad(
        UUID.randomUUID(),
        tipo,
        "1",
        2024,
        "Consejo Superior Universitario",
        "BASE",
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 2, 1),
        null,
        null,
        tipoAlcance,
        alcanceJson);
  }
}
