package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.VersionJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC07-HU03-T4 (#96): carga real de la semilla mínima de normas. Los casos de acumulación entre
 * versiones cubren el defecto #130.
 */
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
    assertThat(reglas.findAll()).hasSize(20);
    assertThat(versiones.findAll()).hasSize(1);
    assertThat(reglasEnVersion.findAll()).hasSize(20);
    assertThat(reglasEnVersion.findAll()).extracting(fila -> fila.getEstado()).contains("VIGENTE");
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

  @Test
  void uc07_hu03_t4_laVersionNuevaConservaLasReglasDeLaAnterior() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-003a-2022.json");

    VersionEntidad primera = versionesOrdenadas().get(0);
    ReglaEnVersionEntidad heredada = reglasEnVersion.findByVersionId(primera.getId()).get(0);
    reglasEnVersion.saveAndFlush(
        new ReglaEnVersionEntidad(
            heredada.getId(),
            heredada.getVersionId(),
            heredada.getReglaId(),
            EstadoRegla.YA_NO_RIGE.name()));

    importarNorma("semilla/acuerdo-008-2008.json");

    List<VersionEntidad> ordenadas = versionesOrdenadas();

    assertThat(ordenadas).extracting(VersionEntidad::getNumero).containsExactly(1, 2);
    assertThat(reglasPorVersion()).containsEntry(1, 20).containsEntry(2, 27);
    assertThat(idsDeReglas(ordenadas.get(1))).containsAll(idsDeReglas(ordenadas.get(0)));
    Map<UUID, String> estadosDeLaPrimera = estadosPorRegla(ordenadas.get(0));
    Map<UUID, String> estadosDeLaSegunda = estadosPorRegla(ordenadas.get(1));
    estadosDeLaPrimera.forEach(
        (id, estado) -> assertThat(estadosDeLaSegunda).containsEntry(id, estado));
    assertThat(estadosDeLaSegunda)
        .as("el estado heredado se copia, no se recalcula")
        .containsEntry(heredada.getReglaId(), EstadoRegla.YA_NO_RIGE.name());
    assertThat(ordenadas).extracting(VersionEntidad::getNumeroPlanDeEstudios).containsExactly(1, 1);
  }

  @Test
  void uc07_hu03_t4_laVersionVigenteTieneLasReglasDeLasTresNormas() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-003a-2022.json");
    importarNorma("semilla/acuerdo-008-2008.json");
    importarNorma("semilla/acuerdo-11-2023.json");

    List<VersionEntidad> ordenadas = versionesOrdenadas();

    assertThat(normasGuardadas.count()).isEqualTo(3);
    assertThat(reglas.count()).isEqualTo(276);
    assertThat(ordenadas).hasSize(3);
    assertThat(reglasPorVersion()).containsEntry(1, 20).containsEntry(2, 27).containsEntry(3, 276);
    assertThat(ordenadas)
        .extracting(VersionEntidad::getNumeroPlanDeEstudios)
        .containsExactly(1, 1, 2);
    assertThat(
            versiones.findTopByProgramaIdOrderByNumeroDesc(
                programasGuardados.findByCodigoSia("2879").orElseThrow().getId()))
        .hasValueSatisfying(vigente -> assertThat(vigente.getNumero()).isEqualTo(3));

    Set<UUID> normasDeLaVigente =
        reglas.findAllById(idsDeReglas(ordenadas.get(2))).stream()
            .map(ReglaEntidad::getNormaId)
            .collect(Collectors.toSet());
    assertThat(normasDeLaVigente).hasSize(3);
  }

  private void cargarProgramas() throws IOException {
    try (InputStream entrada = new ClassPathResource("semilla/programas.json").getInputStream()) {
      programas.importar(
          new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }
  }

  private ResultadoDeImportacionDeNormas importarNorma() throws IOException {
    return importarNorma("semilla/acuerdo-003a-2022.json");
  }

  private ResultadoDeImportacionDeNormas importarNorma(String archivo) throws IOException {
    try (InputStream entrada = new ClassPathResource(archivo).getInputStream()) {
      return normas.importar(entrada);
    }
  }

  private List<VersionEntidad> versionesOrdenadas() {
    return versiones.findAll().stream()
        .sorted(Comparator.comparingInt(VersionEntidad::getNumero))
        .toList();
  }

  private Map<Integer, Integer> reglasPorVersion() {
    Map<Integer, Integer> porNumero = new HashMap<>();
    for (VersionEntidad version : versiones.findAll()) {
      porNumero.put(version.getNumero(), reglasEnVersion.findByVersionId(version.getId()).size());
    }
    return porNumero;
  }

  private Set<UUID> idsDeReglas(VersionEntidad version) {
    return reglasEnVersion.findByVersionId(version.getId()).stream()
        .map(ReglaEnVersionEntidad::getReglaId)
        .collect(Collectors.toSet());
  }

  private Map<UUID, String> estadosPorRegla(VersionEntidad version) {
    return reglasEnVersion.findByVersionId(version.getId()).stream()
        .collect(
            Collectors.toMap(ReglaEnVersionEntidad::getReglaId, ReglaEnVersionEntidad::getEstado));
  }
}
