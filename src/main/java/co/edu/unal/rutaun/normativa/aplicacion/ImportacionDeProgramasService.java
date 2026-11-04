package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.Programa;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import co.edu.unal.rutaun.utilidades.ConfiguracionJson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;

/**
 * Carga programas desde un archivo JSON.
 *
 * <p>SOP-03 (#55). La carga es de todo o nada: primero se leen y validan todos los programas del
 * archivo, y solo si todos son válidos se guardan. Un programa cuyo código SIA ya existe se deja
 * como está, así que repetir la carga no duplica nada. La base no impide dos programas con el mismo
 * código SIA, por eso esa regla vive aquí.
 */
@Service
public class ImportacionDeProgramasService {

  private final ProgramaJpaRepository programas;

  /** Crea el servicio con el repositorio de programas. */
  public ImportacionDeProgramasService(ProgramaJpaRepository programas) {
    this.programas = ArgumentChecks.obligatorio(programas, "programas");
  }

  /**
   * Guarda los programas nuevos del archivo y omite los que ya existen.
   *
   * @param json contenido del archivo de programas
   * @return cuántos programas se crearon y cuántos ya existían
   * @throws ArchivoDeProgramasInvalidoException si el JSON no se puede leer o algún programa es
   *     inválido; en ese caso no se guarda ninguno
   */
  @Transactional
  public ResultadoDeCargaDeProgramas importar(String json) {
    ArgumentChecks.obligatorioNoVacio(json, "json");
    List<Programa> leidos = aProgramas(leer(json));
    int creados = 0;
    for (Programa programa : leidos) {
      if (programas.findByCodigoSia(programa.codigoSia()).isEmpty()) {
        programas.save(ProgramaMapper.aEntidad(programa));
        creados++;
      }
    }
    return new ResultadoDeCargaDeProgramas(creados, leidos.size() - creados);
  }

  private static ArchivoDeProgramasJson leer(String json) {
    try {
      return ConfiguracionJson.mapper().readValue(json, ArchivoDeProgramasJson.class);
    } catch (JacksonException e) {
      throw new ArchivoDeProgramasInvalidoException(
          "El JSON de programas no se pudo leer: " + e.getOriginalMessage(), e);
    }
  }

  private static List<Programa> aProgramas(ArchivoDeProgramasJson archivo) {
    if (archivo == null || archivo.programas() == null || archivo.programas().isEmpty()) {
      throw new ArchivoDeProgramasInvalidoException("El archivo no trae programas.");
    }
    Set<String> codigos = new HashSet<>();
    List<Programa> resultado = new ArrayList<>();
    for (ProgramaJson entrada : archivo.programas()) {
      Programa programa = aDominio(entrada, resultado.size() + 1);
      if (!codigos.add(programa.codigoSia())) {
        throw new ArchivoDeProgramasInvalidoException(
            "El código SIA " + programa.codigoSia() + " está repetido en el archivo.");
      }
      resultado.add(programa);
    }
    return List.copyOf(resultado);
  }

  private static Programa aDominio(ProgramaJson entrada, int posicion) {
    if (entrada == null) {
      throw new ArchivoDeProgramasInvalidoException(
          "El programa " + posicion + " del archivo está vacío.");
    }
    try {
      return Programa.registrar(
          entrada.nombre(), entrada.codigoSia(), new Facultad(entrada.facultad()));
    } catch (IllegalArgumentException e) {
      throw new ArchivoDeProgramasInvalidoException(
          "El programa " + posicion + " del archivo no es válido: " + e.getMessage(), e);
    }
  }
}
