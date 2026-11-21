package co.edu.unal.rutaun.utilidades;

import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * El único {@link JsonMapper} del proyecto (diseño de clases, «Capa de utilidades»): fechas en ISO
 * y lectura tolerante de campos desconocidos, para leer esquemas guardados anteriores. Lo usan
 * Spring (presentación), Hibernate (columnas JSON) y el lector del JSON de normativa.
 */
@Configuration
public class JsonConfig {

  private static final JsonMapper MAPPER =
      JsonMapper.builder()
          .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();

  /** El mapper compartido, para quien no recibe inyección (mappers estáticos y pruebas). */
  public static JsonMapper mapper() {
    return MAPPER;
  }

  /** Reemplaza el {@link JsonMapper} que configuraría Spring Boot: queda uno solo. */
  @Bean
  JsonMapper jsonMapper() {
    return MAPPER;
  }

  /** Hibernate guarda y lee las columnas JSON con el mismo mapper. */
  @Bean
  HibernatePropertiesCustomizer formatoJsonDeHibernate() {
    return propiedades ->
        propiedades.put("hibernate.type.json_format_mapper", new Jackson3JsonFormatMapper(MAPPER));
  }
}
