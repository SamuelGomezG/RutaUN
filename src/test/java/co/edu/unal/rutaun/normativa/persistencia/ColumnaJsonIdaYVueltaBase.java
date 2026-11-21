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

/**
 * TT-04-T6 (#145): los datos de una regla viajan de un record de persistencia a la columna {@code
 * datos} JSON y de vuelta, con Hibernate 7, H2 2.4.240 y Jackson 3. Las clases hijas eligen la
 * base: en memoria o en archivo.
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
abstract class ColumnaJsonIdaYVueltaBase {

  @Autowired private NormaRepository normas;
  @Autowired private ReglaRepository reglas;
  @Autowired private JdbcTemplate jdbc;

  @Value("${spring.datasource.url}")
  private String urlDeLaBase;

  private UUID normaId;
  private UUID reglaId;

  /** Prefijo de la URL JDBC que debe estar usando la prueba. */
  protected abstract String prefijoDeUrlEsperado();

  /** Borra lo que guardó la prueba, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    if (reglaId != null) {
      reglas.deleteById(reglaId);
    }
    if (normaId != null) {
      normas.deleteById(normaId);
    }
  }

  @Test
  void tt04_laBaseEsLaEsperada() {
    assertThat(urlDeLaBase).startsWith(prefijoDeUrlEsperado());
  }

  @Test
  void tt04_columnaJsonIdaYVuelta() {
    DatosDeReglaJson original =
        new DatosDeReglaJson.CargaPorPeriodoJson(DatosDeReglaJson.ESQUEMA, 10, "MINIMO");
    NormaEntity norma = normas.save(nuevaNorma());
    normaId = norma.getId();
    ReglaEntity regla = reglas.save(nuevaRegla(normaId, original));
    reglaId = regla.getId();

    DatosDeReglaJson leido = reglas.findById(reglaId).orElseThrow().getDatos();
    String crudo =
        jdbc.queryForObject(
            "SELECT CAST(datos AS VARCHAR(2000)) FROM regla WHERE id = ?", String.class, reglaId);

    assertThat(leido).isEqualTo(original);
    assertThat(crudo).contains("\"categoria\":\"CARGA_POR_PERIODO\"").contains("\"esquema\":1");
  }

  @Test
  void tt04_lasCuatroColumnasSonJson() {
    assertThat(tipoDe("REGLA", "DATOS")).isEqualTo("JSON");
    assertThat(tipoDe("NORMA", "ALCANCE_DETALLE")).isEqualTo("JSON");
    assertThat(tipoDe("VERSION_CONSOLIDADO", "CAMBIOS")).isEqualTo("JSON");
    assertThat(tipoDe("INCIDENTE", "ELEMENTOS")).isEqualTo("JSON");
  }

  private String tipoDe(String tabla, String columna) {
    return jdbc.queryForObject(
        "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS"
            + " WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
        String.class,
        tabla,
        columna);
  }

  private static NormaEntity nuevaNorma() {
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
        new AlcanceJson(AlcanceJson.ESQUEMA, List.of(), List.of(), List.of(), null));
  }

  private static ReglaEntity nuevaRegla(UUID normaId, DatosDeReglaJson datos) {
    return new ReglaEntity(
        UUID.randomUUID(),
        normaId,
        "CARGA_POR_PERIODO",
        "Carga mínima",
        "La carga mínima es de 10 créditos.",
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
