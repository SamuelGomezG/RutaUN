package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class MigracionDeMomentosTest {

  private final String url =
      "jdbc:h2:mem:migracion_momentos_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";

  @Test
  void tt04_laMigracionConservaLasFechasComoMomentosDeTipoFecha() throws Exception {
    Flyway.configure()
        .dataSource(url, "sa", "")
        .target(MigrationVersion.fromVersion("2"))
        .load()
        .migrate();
    UUID norma = UUID.randomUUID();
    UUID regla = UUID.randomUUID();
    insertarNormaYReglaConFechas(norma, regla);

    Flyway.configure().dataSource(url, "sa", "").load().migrate();

    try (Connection conexion = DriverManager.getConnection(url, "sa", "");
        PreparedStatement consultar =
            conexion.prepareStatement(
                "SELECT aplica_desde_tipo, aplica_desde_fecha, aplica_desde_periodo,"
                    + " vigente_hasta_tipo FROM norma WHERE id = ?")) {
      consultar.setObject(1, norma);
      try (ResultSet fila = consultar.executeQuery()) {
        assertThat(fila.next()).isTrue();
        assertThat(fila.getString("aplica_desde_tipo")).isEqualTo("FECHA");
        assertThat(fila.getDate("aplica_desde_fecha").toLocalDate())
            .isEqualTo(LocalDate.of(2020, 2, 1));
        assertThat(fila.getString("aplica_desde_periodo")).isNull();
        assertThat(fila.getString("vigente_hasta_tipo")).isNull();
      }
    }
    try (Connection conexion = DriverManager.getConnection(url, "sa", "");
        PreparedStatement consultar =
            conexion.prepareStatement(
                "SELECT vigente_desde_tipo, vigente_desde_fecha, vigente_hasta_tipo,"
                    + " afectacion_desde_tipo FROM regla WHERE id = ?")) {
      consultar.setObject(1, regla);
      try (ResultSet fila = consultar.executeQuery()) {
        assertThat(fila.next()).isTrue();
        assertThat(fila.getString("vigente_desde_tipo")).isEqualTo("FECHA");
        assertThat(fila.getDate("vigente_desde_fecha").toLocalDate())
            .isEqualTo(LocalDate.of(2020, 3, 1));
        assertThat(fila.getString("vigente_hasta_tipo")).isNull();
        assertThat(fila.getString("afectacion_desde_tipo")).isNull();
      }
    }
  }

  private void insertarNormaYReglaConFechas(UUID norma, UUID regla) throws Exception {
    try (Connection conexion = DriverManager.getConnection(url, "sa", "");
        PreparedStatement insertarNorma =
            conexion.prepareStatement(
                "INSERT INTO norma (id, tipo, numero, anio, organo, tipo_de_efecto,"
                    + " fecha_expedicion, aplica_desde, vigente_hasta, enlace_regimen_legal,"
                    + " tipo_alcance, alcance_detalle) VALUES (?, 'ACUERDO', '1', 2020,"
                    + " 'Consejo', 'BASE', DATE '2020-01-01', DATE '2020-02-01', NULL, NULL,"
                    + " 'TODOS', '{}')");
        PreparedStatement insertarRegla =
            conexion.prepareStatement(
                "INSERT INTO regla (id, norma_id, categoria, objeto, enunciado, fuente,"
                    + " vigente_desde, vigente_hasta, clasificacion, tipo_afectacion,"
                    + " regla_afectada_id, articulo_afectado, datos) VALUES (?, ?, 'OTRA',"
                    + " 'Objeto', 'Enunciado', 'art. 1', DATE '2020-03-01', NULL, 'DEL_PLAN',"
                    + " NULL, NULL, NULL, '{}')")) {
      insertarNorma.setObject(1, norma);
      insertarNorma.executeUpdate();
      insertarRegla.setObject(1, regla);
      insertarRegla.setObject(2, norma);
      insertarRegla.executeUpdate();
    }
  }
}
