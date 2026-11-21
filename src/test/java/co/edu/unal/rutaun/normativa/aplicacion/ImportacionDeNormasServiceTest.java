package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.MateriaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
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
  private final NormaRepository normasGuardadas;
  private final ReglaRepository reglas;
  private final MateriaRepository materiasGuardadas;
  private final ConsolidadoRepository consolidados;
  private final VersionDelConsolidadoRepository versiones;
  private final ReglaEnVersionRepository reglasEnVersion;
  private final ProgramaRepository programasGuardados;

  @Autowired private EntityManager em;

  @Autowired
  ImportacionDeNormasServiceTest(
      ImportacionDeProgramasService programas,
      ImportacionDeNormasService normas,
      NormaRepository normasGuardadas,
      ReglaRepository reglas,
      MateriaRepository materiasGuardadas,
      ConsolidadoRepository consolidados,
      VersionDelConsolidadoRepository versiones,
      ReglaEnVersionRepository reglasEnVersion,
      ProgramaRepository programasGuardados) {
    this.programas = programas;
    this.normas = normas;
    this.normasGuardadas = normasGuardadas;
    this.reglas = reglas;
    this.materiasGuardadas = materiasGuardadas;
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

    VersionEntity primera = versionesOrdenadas().get(0);
    ReglaEnVersionEntity heredada = reglasEnVersion.findByVersionId(primera.getId()).get(0);
    // Una versión no se modifica por el dominio: el estado se fija con un UPDATE.
    em.flush();
    em.createQuery("UPDATE ReglaEnVersionEntity r SET r.estado = :estado WHERE r.id = :id")
        .setParameter("estado", EstadoRegla.YA_NO_RIGE.name())
        .setParameter("id", heredada.getId())
        .executeUpdate();
    em.clear();

    importarNorma("semilla/acuerdo-008-2008.json");

    List<VersionEntity> ordenadas = versionesOrdenadas();

    assertThat(ordenadas).extracting(VersionEntity::getNumero).containsExactly(1, 2);
    assertThat(reglasPorVersion()).containsEntry(1, 20).containsEntry(2, 27);
    assertThat(idsDeReglas(ordenadas.get(1))).containsAll(idsDeReglas(ordenadas.get(0)));
    Map<UUID, String> estadosDeLaPrimera = estadosPorRegla(ordenadas.get(0));
    Map<UUID, String> estadosDeLaSegunda = estadosPorRegla(ordenadas.get(1));
    estadosDeLaPrimera.forEach(
        (id, estado) -> assertThat(estadosDeLaSegunda).containsEntry(id, estado));
    assertThat(estadosDeLaSegunda)
        .as("el estado heredado se copia, no se recalcula")
        .containsEntry(heredada.getReglaId(), EstadoRegla.YA_NO_RIGE.name());
    assertThat(ordenadas).extracting(VersionEntity::getNumeroPlanDeEstudios).containsExactly(1, 1);
  }

  @Test
  void uc07_hu03_t4_unaNormaVencidaDejaSusReglasEnYaNoRige() throws IOException {
    cargarProgramas();

    importarNorma("json/validos/norma-vencida.json");

    assertThat(reglasEnVersion.findAll())
        .as("las reglas de una norma vencida quedan en YA_NO_RIGE")
        .isNotEmpty()
        .extracting(ReglaEnVersionEntity::getEstado)
        .containsOnly(EstadoRegla.YA_NO_RIGE.name());
  }

  @Test
  void uc07_ext5a_unaNormaConTodasSusReglasInformativasLoInforma() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-003a-2022.json");

    ResultadoDeImportacionDeNormas resultado =
        importarNorma("json/validos/norma-toda-informativa.json");

    assertThat(resultado.creada()).isTrue();
    assertThat(resultado.versionesCreadas()).isEqualTo(1);
    assertThat(resultado.ningunPlanQuedaDesactualizado()).isTrue();
    assertThat(normasGuardadas.count()).isEqualTo(2);
    assertThat(versiones.count()).isEqualTo(2);
    assertThat(reglasPorVersion()).containsEntry(2, 22);
  }

  @Test
  void uc07_ext5a_unaNormaConReglasDelPlanNoLoInforma() throws IOException {
    cargarProgramas();

    ResultadoDeImportacionDeNormas resultado = importarNorma("semilla/acuerdo-003a-2022.json");

    assertThat(resultado.creada()).isTrue();
    assertThat(resultado.ningunPlanQuedaDesactualizado()).isFalse();
  }

  @Test
  void uc07_ext5a_laNormaYaCargadaNoLoInforma() throws IOException {
    cargarProgramas();
    ResultadoDeImportacionDeNormas primera =
        importarNorma("json/validos/norma-toda-informativa.json");

    ResultadoDeImportacionDeNormas segunda =
        importarNorma("json/validos/norma-toda-informativa.json");

    assertThat(primera.ningunPlanQuedaDesactualizado()).isTrue();
    assertThat(segunda.creada()).isFalse();
    assertThat(segunda.versionesCreadas()).isZero();
    assertThat(segunda.ningunPlanQuedaDesactualizado()).isFalse();
  }

  @Test
  void uc07_hu03_t4_laVersionVigenteTieneLasReglasDeLasTresNormas() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-003a-2022.json");
    importarNorma("semilla/acuerdo-008-2008.json");
    importarNorma("semilla/acuerdo-11-2023.json");

    List<VersionEntity> ordenadas = versionesOrdenadas();

    assertThat(normasGuardadas.count()).isEqualTo(3);
    assertThat(reglas.count()).isEqualTo(276);
    assertThat(ordenadas).hasSize(3);
    assertThat(reglasPorVersion()).containsEntry(1, 20).containsEntry(2, 27).containsEntry(3, 276);
    assertThat(ordenadas)
        .extracting(VersionEntity::getNumeroPlanDeEstudios)
        .containsExactly(1, 1, 2);
    assertThat(
            versiones.findTopByProgramaIdOrderByNumeroDesc(
                programasGuardados.findByCodigoSia("2879").orElseThrow().getId()))
        .hasValueSatisfying(vigente -> assertThat(vigente.getNumero()).isEqualTo(3));

    Set<UUID> normasDeLaVigente =
        reglas.findAllById(idsDeReglas(ordenadas.get(2))).stream()
            .map(ReglaEntity::getNormaId)
            .collect(Collectors.toSet());
    assertThat(normasDeLaVigente).hasSize(3);
  }

  @Test
  void tt04_laImportacionRegistraLasMateriasEnElCatalogo() throws IOException {
    cargarProgramas();

    importarNorma("semilla/acuerdo-11-2023.json");

    assertThat(materiasGuardadas.count()).isEqualTo(105);
    assertThat(materiasGuardadas.findById("2016699"))
        .hasValueSatisfying(materia -> assertThat(materia.getCreditos()).isEqualTo(3));
  }

  @Test
  void uc07_ext6d_unaMateriaConOtrosCreditosRechazaLaIncorporacion() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-11-2023.json");
    long normasAntes = normasGuardadas.count();

    assertThatThrownBy(() -> importarNorma("json/invalidos/materia-con-otros-creditos.json"))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("2016699")
        .hasMessageContaining("le da 4")
        .hasMessageContaining("tiene 3");

    assertThat(normasGuardadas.count()).isEqualTo(normasAntes);
    assertThat(materiasGuardadas.findById("2016699"))
        .hasValueSatisfying(materia -> assertThat(materia.getCreditos()).isEqualTo(3));
    assertThat(materiasGuardadas.findById("9000099")).isEmpty();
  }

  @Test
  void tt04_otraNormaConLosMismosCreditosNoDuplicaElCatalogo() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-11-2023.json");
    String conLosMismosCreditos =
        leer("json/invalidos/materia-con-otros-creditos.json")
            .replace("\"creditos\": 4", "\"creditos\": 3");

    ResultadoDeImportacionDeNormas resultado = importarTexto(conLosMismosCreditos);

    assertThat(resultado.creada()).isTrue();
    assertThat(materiasGuardadas.count()).isEqualTo(106);
  }

  @Test
  void uc07_ext6d_dosCreditosEnLaMismaNormaNoGuardanNada() throws IOException {
    cargarProgramas();
    String conDosCreditos =
        Pattern.compile("(?<creditos>\"codigo\"\\s*:\\s*\"2028837\"[^}]*?\"creditos\"\\s*:\\s*)4")
            .matcher(leer("semilla/acuerdo-11-2023.json"))
            .replaceFirst("${creditos}5");

    assertThatThrownBy(() -> importarTexto(conDosCreditos))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("2028837")
        .hasMessageContaining("en esta norma");

    assertThat(materiasGuardadas.count()).isZero();
    assertThat(normasGuardadas.count()).isZero();
    assertThat(reglas.count()).isZero();
  }

  private String leer(String archivo) throws IOException {
    try (InputStream entrada = new ClassPathResource(archivo).getInputStream()) {
      return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private ResultadoDeImportacionDeNormas importarTexto(String json) {
    return normas.importar(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
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

  private List<VersionEntity> versionesOrdenadas() {
    return versiones.findAll().stream()
        .sorted(Comparator.comparingInt(VersionEntity::getNumero))
        .toList();
  }

  private Map<Integer, Integer> reglasPorVersion() {
    Map<Integer, Integer> porNumero = new HashMap<>();
    for (VersionEntity version : versiones.findAll()) {
      porNumero.put(version.getNumero(), reglasEnVersion.findByVersionId(version.getId()).size());
    }
    return porNumero;
  }

  private Set<UUID> idsDeReglas(VersionEntity version) {
    return reglasEnVersion.findByVersionId(version.getId()).stream()
        .map(ReglaEnVersionEntity::getReglaId)
        .collect(Collectors.toSet());
  }

  private Map<UUID, String> estadosPorRegla(VersionEntity version) {
    return reglasEnVersion.findByVersionId(version.getId()).stream()
        .collect(
            Collectors.toMap(ReglaEnVersionEntity::getReglaId, ReglaEnVersionEntity::getEstado));
  }
}
