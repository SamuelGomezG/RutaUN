package co.edu.unal.rutaun.utilidades;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * TT-04-T6 (#145): comprueba que el contexto tiene un solo {@link JsonMapper} y que Hibernate usa
 * ese mismo mapper para las columnas JSON.
 */
@PruebaDeIntegracion
class JsonConfigIntegracionTest {

  /** Dato con fecha, para ver cómo lo escribe el formato de Hibernate. */
  record ConFecha(LocalDate fecha) {}

  private final ApplicationContext contexto;
  private final EntityManagerFactory fabricasDeEntidades;

  /**
   * Recibe las piezas reales que se prueban.
   *
   * @param contexto contexto de la aplicación
   * @param fabricasDeEntidades fábrica de entidades de JPA
   */
  @Autowired
  JsonConfigIntegracionTest(ApplicationContext contexto, EntityManagerFactory fabricasDeEntidades) {
    this.contexto = contexto;
    this.fabricasDeEntidades = fabricasDeEntidades;
  }

  @Test
  void tt04_hayUnSoloJsonMapperYEsElDeJsonConfig() {
    Map<String, JsonMapper> mappers = contexto.getBeansOfType(JsonMapper.class);

    assertThat(mappers).hasSize(1);
    assertThat(mappers.values()).allMatch(mapper -> mapper == JsonConfig.mapper());
  }

  @Test
  void tt04_hibernateEscribeLasFechasJsonEnIso() {
    Jackson3JsonFormatMapper formato =
        (Jackson3JsonFormatMapper)
            fabricasDeEntidades
                .unwrap(SessionFactoryImplementor.class)
                .getSessionFactoryOptions()
                .getJsonFormatMapper();

    String json = formato.toString(new ConFecha(LocalDate.of(2022, 2, 1)), ConFecha.class);

    assertThat(json).contains("\"2022-02-01\"");
  }
}
