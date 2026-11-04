package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica la carga de programas desde JSON.
 *
 * <p>SOP-03 (#55).
 */
@PruebaDeIntegracion
@Transactional
class ImportacionDeProgramasServiceTest {

  private static final String DOS_PROGRAMAS =
      """
      {"programas": [
        {"nombre": "Programa uno", "codigoSia": "T-001", "facultad": "Ingeniería"},
        {"nombre": "Programa dos", "codigoSia": "T-002", "facultad": "Ingeniería"}
      ]}
      """;

  private final ImportacionDeProgramasService servicio;
  private final ProgramaJpaRepository programas;

  /** Recibe el servicio y el repositorio que Spring inyecta. */
  @Autowired
  ImportacionDeProgramasServiceTest(
      ImportacionDeProgramasService servicio, ProgramaJpaRepository programas) {
    this.servicio = servicio;
    this.programas = programas;
  }

  @Test
  void sop03_importarCreaLosProgramasDelArchivo() {
    ResultadoDeCargaDeProgramas resultado = servicio.importar(DOS_PROGRAMAS);

    assertThat(resultado).isEqualTo(new ResultadoDeCargaDeProgramas(2, 0));
    assertThat(programas.findByCodigoSia("T-001")).isPresent();
    assertThat(programas.findByCodigoSia("T-002")).isPresent();
  }

  @Test
  void sop03_repetirLaCargaNoDuplicaNiCambiaElIdentificador() {
    servicio.importar(DOS_PROGRAMAS);
    UUID idInicial = programas.findByCodigoSia("T-001").orElseThrow().getId();

    ResultadoDeCargaDeProgramas segunda = servicio.importar(DOS_PROGRAMAS);

    assertThat(segunda).isEqualTo(new ResultadoDeCargaDeProgramas(0, 2));
    assertThat(programas.findByCodigoSia("T-001").orElseThrow().getId()).isEqualTo(idInicial);
  }

  @Test
  void sop03_unProgramaInvalidoImpideGuardarTodoElArchivo() {
    String json =
        """
        {"programas": [
          {"nombre": "Programa uno", "codigoSia": "T-001", "facultad": "Ingeniería"},
          {"nombre": "", "codigoSia": "T-002", "facultad": "Ingeniería"}
        ]}
        """;
    long antes = programas.count();

    assertThatThrownBy(() -> servicio.importar(json))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class)
        .hasMessageContaining("programa 2");

    assertThat(programas.count()).isEqualTo(antes);
  }

  @Test
  void sop03_unCodigoSiaRepetidoEnElArchivoSeRechaza() {
    String json =
        """
        {"programas": [
          {"nombre": "Programa uno", "codigoSia": "T-001", "facultad": "Ingeniería"},
          {"nombre": "Programa dos", "codigoSia": "T-001", "facultad": "Ingeniería"}
        ]}
        """;

    assertThatThrownBy(() -> servicio.importar(json))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class)
        .hasMessageContaining("T-001")
        .hasMessageContaining("repetido");
  }

  @Test
  void sop03_unJsonMalFormadoSeRechaza() {
    assertThatThrownBy(() -> servicio.importar("{esto no es json"))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class);
  }

  @Test
  void sop03_unArchivoSinProgramasSeRechaza() {
    assertThatThrownBy(() -> servicio.importar("{\"programas\": []}"))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class);
  }
}
