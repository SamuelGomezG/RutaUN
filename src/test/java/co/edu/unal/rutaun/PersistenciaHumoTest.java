package co.edu.unal.rutaun;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

@PruebaDeIntegracion
class PersistenciaHumoTest {

  @Autowired private JdbcClient jdbcClient;

  @Test
  void tt01_guardaYLeeUnDatoEnH2() {
    jdbcClient.sql("INSERT INTO humo (mensaje) VALUES ('humo tt-01')").update();

    String leido =
        jdbcClient
            .sql("SELECT mensaje FROM humo WHERE mensaje = 'humo tt-01'")
            .query(String.class)
            .single();

    assertThat(leido).isEqualTo("humo tt-01");
  }
}
