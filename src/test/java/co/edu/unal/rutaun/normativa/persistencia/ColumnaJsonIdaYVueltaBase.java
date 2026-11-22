package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * TT-04-T2 (#90): comprueba que los datos de una regla viajan de un {@code record} a la columna
 * {@code datos} (CLOB) y de vuelta, con Hibernate 7, H2 2.4.240 y Jackson 3. Las clases hijas
 * eligen la base: en memoria o en archivo.
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
abstract class ColumnaJsonIdaYVueltaBase {

  private static final JsonMapper JSON = JsonMapper.builder().build();

  @Autowired private NormaRepository normas;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private TransactionTemplate transaccion;

  @Value("${spring.datasource.url}")
  private String urlDeLaBase;

  private UUID normaId;
  private UUID reglaId;

  /** Datos de ejemplo de una regla de materia; imita lo que guardará el mapper real. */
  record DatosDeReglaPrueba(
      String materia,
      String nombre,
      int creditos,
      boolean obligatoria,
      List<String> prerrequisitos,
      LocalDate vigenteDesde) {}

  /** Prefijo de la URL JDBC que debe estar usando la prueba. */
  protected abstract String prefijoDeUrlEsperado();

  /** Borra lo que guardó la prueba, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    if (reglaId != null) {
      jdbc.update("DELETE FROM regla WHERE CAST(id AS VARCHAR) = ?", reglaId.toString());
    }
    if (normaId != null) {
      jdbc.update("DELETE FROM norma WHERE CAST(id AS VARCHAR) = ?", normaId.toString());
    }
  }

  @Test
  void tt04_laBaseEsLaEsperada() {
    assertThat(urlDeLaBase).startsWith(prefijoDeUrlEsperado());
  }

  @Test
  void tt04_columnaJsonIdaYVuelta() {
    DatosDeReglaPrueba original =
        new DatosDeReglaPrueba(
            "2015714",
            "Cálculo diferencial en una variable",
            4,
            true,
            List.of("2015710", "1000001"),
            LocalDate.of(2022, 2, 1));
    String json = JSON.writeValueAsString(original);
    ReglaEntity regla = nuevaRegla(json);
    NormaEntity norma = normas.save(nuevaNorma(List.of(regla)));
    normaId = norma.getId();
    reglaId = regla.getId();

    String leidoPorHibernate =
        transaccion.execute(
            estado ->
                normas.findDeLasReglas(List.of(reglaId)).stream()
                    .flatMap(contenedora -> contenedora.getReglas().stream())
                    .filter(guardada -> guardada.getId().equals(reglaId))
                    .findFirst()
                    .orElseThrow()
                    .getDatos());
    String leidoPorSql =
        jdbc.queryForObject(
            "SELECT datos FROM regla WHERE CAST(id AS VARCHAR) = ?",
            String.class,
            reglaId.toString());

    assertThat(leidoPorHibernate).isEqualTo(json);
    assertThat(leidoPorSql).isEqualTo(json);
    assertThat(JSON.readValue(leidoPorHibernate, DatosDeReglaPrueba.class)).isEqualTo(original);
  }

  private static NormaEntity nuevaNorma(List<ReglaEntity> reglas) {
    return new NormaEntity(
        UUID.randomUUID(),
        "ACUERDO",
        "T2-" + UUID.randomUUID().toString().substring(0, 8),
        2022,
        "Consejo Superior",
        "BASE",
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 2, 1),
        null,
        null,
        "PROGRAMAS",
        "{}",
        reglas);
  }

  private static ReglaEntity nuevaRegla(String datos) {
    return new ReglaEntity(
        UUID.randomUUID(),
        "MATERIA",
        "Cálculo diferencial",
        "La materia vale 4 créditos.",
        "Artículo 5",
        null,
        null,
        "DEL_PLAN",
        null,
        null,
        null,
        datos);
  }
}
