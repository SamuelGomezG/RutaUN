package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

/** UC07-HU03-T4 (#96): carga real de la semilla mínima de normas. */
@PruebaDeIntegracion
@Transactional
class ImportacionDeNormasServiceTest {

  private final ImportacionDeProgramasService programas;
  private final ImportacionDeNormasService normas;
  private final NormaJpaRepository normasGuardadas;
  private final ReglaJpaRepository reglas;
  private final ConsolidadoJpaRepository consolidados;
  private final VersionJpaRepository versiones;
  private final ReglaEnVersionJpaRepository reglasEnVersion;
  private final ProgramaJpaRepository programasGuardados;

  @Autowired
  ImportacionDeNormasServiceTest(
      ImportacionDeProgramasService programas,
      ImportacionDeNormasService normas,
      NormaJpaRepository normasGuardadas,
      ReglaJpaRepository reglas,
      ConsolidadoJpaRepository consolidados,
      VersionJpaRepository versiones,
      ReglaEnVersionJpaRepository reglasEnVersion,
      ProgramaJpaRepository programasGuardados) {
    this.programas = programas;
    this.normas = normas;
    this.normasGuardadas = normasGuardadas;
    this.reglas = reglas;
    this.consolidados = consolidados;
    this.versiones = versiones;
    this.reglasEnVersion = reglasEnVersion;
    this.programasGuardados = programasGuardados;
  }

  @Test
  void uc07_hu03_t4_unaIncorporacionValidaGuardaNormaReglasYVersion() throws IOException {
    cargarProgramas();

    ResultadoDeImportacionDeNormas resultado = importarNorma();

    assertThat(resultado.creada()).isTrue();
    assertThat(resultado.versionesCreadas()).isEqualTo(1);
    assertThat(normasGuardadas.findAll()).hasSize(1);
    assertThat(programasGuardados.findByCodigoSia("2879")).isPresent();
    assertThat(consolidados.findAll()).hasSize(1);
    assertThat(reglas.findAll()).hasSize(10);
    assertThat(versiones.findAll()).hasSize(1);
    assertThat(reglasEnVersion.findAll()).hasSize(10);
    assertThat(reglasEnVersion.findAll())
        .extracting(fila -> fila.getEstado())
        .contains("VIGENTE", "PROGRAMADA");
    assertThat(versiones.findAll().get(0).getNumero()).isEqualTo(1);
  }

  @Test
  void uc07_hu03_t4_repetirLaCargaOmiteLaNormaYNoDuplicaVersiones() throws IOException {
    cargarProgramas();
    importarNorma();
    long normasAntes = normasGuardadas.count();
    long versionesAntes = versiones.count();

    ResultadoDeImportacionDeNormas segunda = importarNorma();

    assertThat(segunda.creada()).isFalse();
    assertThat(segunda.versionesCreadas()).isZero();
    assertThat(normasGuardadas.count()).isEqualTo(normasAntes);
    assertThat(versiones.count()).isEqualTo(versionesAntes);
  }

  private void cargarProgramas() throws IOException {
    try (InputStream entrada = new ClassPathResource("semilla/programas.json").getInputStream()) {
      programas.importar(
          new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }
  }

  private ResultadoDeImportacionDeNormas importarNorma() throws IOException {
    try (InputStream entrada =
        new ClassPathResource("semilla/acuerdo-003a-2022.json").getInputStream()) {
      return normas.importar(entrada);
    }
  }
}
