package co.edu.unal.rutaun.utilidades;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura el reloj del sistema en hora de Colombia. */
@Configuration
public class RelojConfig {

  /** Reloj inyectable; las pruebas lo reemplazan por uno fijo. */
  @Bean
  public Clock reloj() {
    return Clock.system(ZoneId.of("America/Bogota"));
  }
}
