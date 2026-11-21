package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

/**
 * TT-04-T6 (#145): una base cargada antes de {@code V5} no se convierte. La migración falla en la
 * primera columna obligatoria y deja intactos los datos anteriores. Sin Spring: H2 en memoria y
 * Flyway a mano.
 *
 * <p>La base se lleva hasta {@code V2}, que ya tiene las cuatro columnas con su forma anterior y es
 * independiente de las migraciones de vigencias y del esquema que vienen en otras ramas.
 */
class MigracionColumnasJsonTest {

  private static final String USUARIO = "sa";
  private static final String CLAVE = "";
  private static final String ALCANCE =
      "{\"facultades\":[],\"programas\":[],\"exclusiones\":[],\"texto\":\"Todos\"}";
  private static final String DATOS = "{\"creditos\":{\"valor\":10},\"tipo\":\"MINIMO\"}";
  private static final String ELEMENTOS = "a, b";

  private final String url =
      "jdbc:h2:mem:columnas_json_"
          + UUID.randomUUID().toString().replace("-", "")
          + ";DB_CLOSE_DELAY=-1";

  @Test
  void tt04_unaBaseAnteriorConFilasNoSeMigraNiPierdeDatos() throws SQLException {
    Flyway.configure()
        .dataSource(url, USUARIO, CLAVE)
        .locations("classpath:db/migration")
        .target(MigrationVersion.fromVersion("2"))
        .load()
        .migrate();
    DatosLegados datos = insertarDatos();

    assertThatThrownBy(() -> flyway().migrate())
        .isInstanceOf(FlywayException.class)
        .hasMessageContaining("DATOS_JSON");

    try (Connection conexion = DriverManager.getConnection(url, USUARIO, CLAVE);
        Statement sentencia = conexion.createStatement()) {
      assertThat(tipoDe(sentencia, "REGLA", "DATOS")).isEqualTo("CHARACTER LARGE OBJECT");
      assertThat(tipoDe(sentencia, "NORMA", "ALCANCE_DETALLE")).isEqualTo("CHARACTER LARGE OBJECT");
      assertThat(tipoDe(sentencia, "INCIDENTE", "ELEMENTOS")).isEqualTo("CHARACTER LARGE OBJECT");
      assertThat(texto(sentencia, "SELECT datos FROM regla WHERE id = '" + datos.reglaId() + "'"))
          .isEqualTo(DATOS);
      assertThat(
              texto(
                  sentencia,
                  "SELECT alcance_detalle FROM norma WHERE id = '" + datos.normaId() + "'"))
          .isEqualTo(ALCANCE);
      assertThat(
              texto(
                  sentencia,
                  "SELECT elementos FROM incidente WHERE id = '" + datos.incidenteId() + "'"))
          .isEqualTo(ELEMENTOS);
      assertThat(
              texto(
                  sentencia,
                  "SELECT detalle_motivo FROM version_consolidado"
                      + " WHERE motivo = 'POR_CORRECCION'"))
          .isEqualTo("{\"administrador\":\"admin\"}");
      assertThat(cuenta(sentencia, "SELECT COUNT(*) FROM version_consolidado")).isEqualTo(4);
    }
  }

  private DatosLegados insertarDatos() throws SQLException {
    UUID programaId = UUID.randomUUID();
    UUID consolidadoId = UUID.randomUUID();
    UUID normaId = UUID.randomUUID();
    UUID reglaId = UUID.randomUUID();
    UUID incidenteId = UUID.randomUUID();
    try (Connection conexion = DriverManager.getConnection(url, USUARIO, CLAVE)) {
      try (PreparedStatement sentencia =
          conexion.prepareStatement(
              "INSERT INTO programa (id, nombre, codigo_sia, facultad) VALUES (?, ?, ?, ?)")) {
        sentencia.setObject(1, programaId);
        sentencia.setString(2, "Ingeniería de Sistemas");
        sentencia.setString(3, "2879");
        sentencia.setString(4, "Ingeniería");
        sentencia.executeUpdate();
      }
      try (PreparedStatement sentencia =
          conexion.prepareStatement(
              "INSERT INTO consolidado (id, programa_id, version_bloqueo) VALUES (?, ?, 0)")) {
        sentencia.setObject(1, consolidadoId);
        sentencia.setObject(2, programaId);
        sentencia.executeUpdate();
      }
      try (PreparedStatement sentencia =
          conexion.prepareStatement(
              "INSERT INTO norma (id, tipo, numero, anio, organo, tipo_de_efecto,"
                  + " fecha_expedicion, aplica_desde, vigente_hasta, enlace_regimen_legal,"
                  + " tipo_alcance, alcance_detalle)"
                  + " VALUES (?, 'ACUERDO', '003-A', 2022, 'Consejo Superior', 'BASE',"
                  + " DATE '2022-01-01', DATE '2022-02-01', NULL, NULL, 'TODOS', ?)")) {
        sentencia.setObject(1, normaId);
        sentencia.setString(2, ALCANCE);
        sentencia.executeUpdate();
      }
      try (PreparedStatement sentencia =
          conexion.prepareStatement(
              "INSERT INTO regla (id, norma_id, categoria, objeto, enunciado, fuente,"
                  + " vigente_desde, vigente_hasta, clasificacion, tipo_afectacion,"
                  + " regla_afectada_id, articulo_afectado, datos)"
                  + " VALUES (?, ?, 'CARGA_POR_PERIODO', 'Carga mínima',"
                  + " 'La carga mínima es de 10 créditos.', 'Artículo 5', NULL, NULL,"
                  + " 'DEL_PLAN', NULL, NULL, NULL, ?)")) {
        sentencia.setObject(1, reglaId);
        sentencia.setObject(2, normaId);
        sentencia.setString(3, DATOS);
        sentencia.executeUpdate();
      }
      try (PreparedStatement sentencia =
          conexion.prepareStatement(
              "INSERT INTO incidente (id, caso_de_uso, paso, programa_id, version_consolidado,"
                  + " causa, elementos, primera_fecha, ultima_fecha, conteo, atendido)"
                  + " VALUES (?, 'UC01', 'paso 5', NULL, NULL, 'El cálculo superó el plazo', ?,"
                  + " TIMESTAMP '2026-11-01 10:00:00', TIMESTAMP '2026-11-01 10:00:00', 1,"
                  + " FALSE)")) {
        sentencia.setObject(1, incidenteId);
        sentencia.setString(2, ELEMENTOS);
        sentencia.executeUpdate();
      }
      insertarVersion(
          conexion,
          consolidadoId,
          programaId,
          1,
          "POR_INCORPORACION",
          "{\"norma\":{\"valor\":\"" + normaId + "\"},\"administrador\":\"admin\"}");
      insertarVersion(
          conexion,
          consolidadoId,
          programaId,
          2,
          "POR_CORRECCION",
          "{\"administrador\":\"admin\"}");
      insertarVersion(
          conexion,
          consolidadoId,
          programaId,
          3,
          "AUTOMATICO_POR_PERIODO",
          "{\"periodo\":{\"anio\":2026,\"parte\":\"PRIMERO\"}}");
      insertarVersion(
          conexion,
          consolidadoId,
          programaId,
          4,
          "AUTOMATICO_POR_FECHA",
          "{\"fecha\":\"2022-02-01\"}");
    }
    return new DatosLegados(normaId, reglaId, incidenteId);
  }

  private static void insertarVersion(
      Connection conexion,
      UUID consolidadoId,
      UUID programaId,
      int numero,
      String motivo,
      String detalle)
      throws SQLException {
    try (PreparedStatement sentencia =
        conexion.prepareStatement(
            "INSERT INTO version_consolidado"
                + " (id, consolidado_id, programa_id, numero, fecha, motivo, detalle_motivo,"
                + " administrador_id, numero_plan_de_estudios, cambios)"
                + " VALUES (?, ?, ?, ?, DATE '2026-12-01', ?, ?, NULL, 1, NULL)")) {
      sentencia.setObject(1, UUID.randomUUID());
      sentencia.setObject(2, consolidadoId);
      sentencia.setObject(3, programaId);
      sentencia.setInt(4, numero);
      sentencia.setString(5, motivo);
      sentencia.setString(6, detalle);
      sentencia.executeUpdate();
    }
  }

  private Flyway flyway() {
    return Flyway.configure()
        .dataSource(url, USUARIO, CLAVE)
        .locations("classpath:db/migration")
        .load();
  }

  private static String tipoDe(Statement sentencia, String tabla, String columna)
      throws SQLException {
    try (ResultSet fila =
        sentencia.executeQuery(
            "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_NAME = '"
                + tabla
                + "' AND COLUMN_NAME = '"
                + columna
                + "'")) {
      return fila.next() ? fila.getString(1) : null;
    }
  }

  private static String texto(Statement sentencia, String sql) throws SQLException {
    try (ResultSet fila = sentencia.executeQuery(sql)) {
      return fila.next() ? fila.getString(1) : null;
    }
  }

  private static long cuenta(Statement sentencia, String sql) throws SQLException {
    try (ResultSet fila = sentencia.executeQuery(sql)) {
      fila.next();
      return fila.getLong(1);
    }
  }

  private record DatosLegados(UUID normaId, UUID reglaId, UUID incidenteId) {}
}
