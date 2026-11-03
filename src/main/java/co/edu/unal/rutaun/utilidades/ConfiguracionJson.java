package co.edu.unal.rutaun.utilidades;

import tools.jackson.databind.json.JsonMapper;

/**
 * Configuración compartida de Jackson para convertir a JSON y desde JSON los datos que la
 * persistencia guarda en columnas de texto. El {@link JsonMapper} es inmutable y seguro entre
 * hilos, así que se crea una sola vez.
 */
public final class ConfiguracionJson {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private ConfiguracionJson() {}

  /** Devuelve el mapper compartido. */
  public static JsonMapper mapper() {
    return MAPPER;
  }
}
