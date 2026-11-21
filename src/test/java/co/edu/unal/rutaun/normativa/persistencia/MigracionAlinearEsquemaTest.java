package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

/**
 * RNF-1 de #142: la migración que alinea el esquema conserva una base ya cargada y llena el día de
 * las versiones automáticas por fecha. Sin Spring: H2 en memoria y Flyway a mano.
 */
class MigracionAlinearEsquemaTest {

  private static final String URL =
      "jdbc:h2:mem:migracion_"
          + UUID.randomUUID().toString().replace("-", "")
          + ";DB_CLOSE_DELAY=-1";
  private static final String USUARIO = "sa";
  private static final String CLAVE = "";

  @Test
  void tt04_laMigracionConservaLasVersionesYLlenaElDiaAutomatico() throws SQLException {
    String anterior = versionAnterior(flyway());

    Flyway.configure()
        .dataSource(URL, USUARIO, CLAVE)
        .locations("classpath:db/migration")
        .target(MigrationVersion.fromVersion(anterior))
        .load()
        .migrate();
    insertarDatos();

    flyway().migrate();

    try (Connection conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
        Statement sentencia = conexion.createStatement()) {
      assertThat(cuenta(sentencia, "SELECT COUNT(*) FROM version_consolidado")).isEqualTo(3);
      assertThat(
              cuenta(
                  sentencia,
                  "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES"
                      + " WHERE LOWER(TABLE_NAME) = 'consolidado'"))
          .isZero();
      assertThat(
              cuenta(
                  sentencia,
                  "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS"
                      + " WHERE LOWER(TABLE_NAME) = 'version_consolidado'"
                      + " AND LOWER(COLUMN_NAME) = 'consolidado_id'"))
          .isZero();
      assertThat(
              cuenta(
                  sentencia,
                  "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS"
                      + " WHERE LOWER(TABLE_NAME) = 'version_consolidado'"
                      + " AND LOWER(COLUMN_NAME) = 'dia_automatico'"))
          .isEqualTo(1);

      verificarDias(sentencia);
    }
  }

  private static Flyway flyway() {
    return Flyway.configure()
        .dataSource(URL, USUARIO, CLAVE)
        .locations("classpath:db/migration")
        .load();
  }

  /** La versión de la migración inmediatamente anterior a la que alinea el esquema. */
  private static String versionAnterior(Flyway flyway) {
    MigrationInfo[] todas = flyway.info().all();
    for (int i = 1; i < todas.length; i++) {
      if (todas[i].getScript().contains("alinear_con_el_diseno")) {
        return todas[i - 1].getVersion().getVersion();
      }
    }
    throw new IllegalStateException("no encontré V*__alinear_con_el_diseno.sql");
  }

  private static void insertarDatos() throws SQLException {
    UUID programaId = UUID.randomUUID();
    UUID consolidadoId = UUID.randomUUID();
    try (Connection conexion = DriverManager.getConnection(URL, USUARIO, CLAVE)) {
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
      insertarVersion(conexion, consolidadoId, programaId, 1, "POR_INCORPORACION");
      insertarVersion(conexion, consolidadoId, programaId, 2, "AUTOMATICO_POR_FECHA");
      insertarVersion(conexion, consolidadoId, programaId, 3, "AUTOMATICO_POR_PERIODO");
    }
  }

  private static void insertarVersion(
      Connection conexion, UUID consolidadoId, UUID programaId, int numero, String motivo)
      throws SQLException {
    try (PreparedStatement sentencia =
        conexion.prepareStatement(
            "INSERT INTO version_consolidado"
                + " (id, consolidado_id, programa_id, numero, fecha, motivo,"
                + " numero_plan_de_estudios)"
                + " VALUES (?, ?, ?, ?, DATE '2026-12-01', ?, 1)")) {
      sentencia.setObject(1, UUID.randomUUID());
      sentencia.setObject(2, consolidadoId);
      sentencia.setObject(3, programaId);
      sentencia.setInt(4, numero);
      sentencia.setString(5, motivo);
      sentencia.executeUpdate();
    }
  }

  private static void verificarDias(Statement sentencia) throws SQLException {
    try (ResultSet filas =
        sentencia.executeQuery(
            "SELECT numero, fecha, dia_automatico FROM version_consolidado ORDER BY numero")) {
      assertThat(filas.next()).isTrue();
      assertThat(filas.getInt("numero")).isEqualTo(1);
      assertThat(filas.getDate("dia_automatico")).isNull();

      assertThat(filas.next()).isTrue();
      assertThat(filas.getInt("numero")).isEqualTo(2);
      LocalDate fecha = filas.getDate("fecha").toLocalDate();
      assertThat(filas.getDate("dia_automatico").toLocalDate()).isEqualTo(fecha);

      assertThat(filas.next()).isTrue();
      assertThat(filas.getInt("numero")).isEqualTo(3);
      assertThat(filas.getDate("dia_automatico")).isNull();

      assertThat(filas.next()).isFalse();
    }
  }

  private static long cuenta(Statement sentencia, String sql) throws SQLException {
    try (ResultSet resultado = sentencia.executeQuery(sql)) {
      resultado.next();
      return resultado.getLong(1);
    }
  }
}
