package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.persistencia.ColumnasDeMomento;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
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
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  private static final String FECHA = "{ \"tipo\": \"FECHA\", \"fecha\": \"2026-01-15\" }";

  private final ImportacionDeProgramasService programas;
  private final ImportacionDeNormasService normas;
  private final NormaRepository normasGuardadas;
  private final ReglaRepository reglas;
  private final ConsolidadoRepository consolidados;
  private final VersionDelConsolidadoRepository versiones;
  private final ReglaEnVersionRepository reglasEnVersion;
  private final ProgramaRepository programasGuardados;
  private final Clock reloj;

  @Autowired private EntityManager em;

  @Autowired
  ImportacionDeNormasServiceTest(
      ImportacionDeProgramasService programas,
      ImportacionDeNormasService normas,
      NormaRepository normasGuardadas,
      ReglaRepository reglas,
      ConsolidadoRepository consolidados,
      VersionDelConsolidadoRepository versiones,
      ReglaEnVersionRepository reglasEnVersion,
      ProgramaRepository programasGuardados,
      Clock reloj) {
    this.programas = programas;
    this.normas = normas;
    this.normasGuardadas = normasGuardadas;
    this.reglas = reglas;
    this.consolidados = consolidados;
    this.versiones = versiones;
    this.reglasEnVersion = reglasEnVersion;
    this.programasGuardados = programasGuardados;
    this.reloj = reloj;
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
  void uc07_paso6_unaNormaConUnPeriodoDeUnAnioAnteriorQuedaVigente() throws IOException {
    cargarProgramas();

    ResultadoDeImportacionDeNormas resultado =
        importarTexto(normaCon(periodo("2020-1"), "null", "null"));

    assertThat(resultado.creada()).isTrue();
    assertThat(reglasEnVersion.findAll())
        .extracting(ReglaEnVersionEntity::getEstado)
        .containsOnly(EstadoRegla.VIGENTE.name());
    NormaEntity guardada = normasGuardadas.findAll().getFirst();
    assertThat(guardada.getAplicaDesde())
        .isEqualTo(new ColumnasDeMomento("PERIODO", null, "2020-1"));
  }

  @ParameterizedTest
  @EnumSource(CasoDePeriodo.class)
  void tt04_unPeriodoCuyoEstadoNoSePuedeCalcularSeRechaza(CasoDePeriodo caso) throws IOException {
    cargarProgramas();
    int anio = LocalDate.now(reloj).getYear();
    String esteAnio = periodo(anio + "-1");
    String anioSiguiente = periodo((anio + 1) + "-1");

    String json =
        switch (caso) {
          case INICIO_DEL_ANIO -> normaCon(esteAnio, "null", "null");
          case INICIO_FUTURO -> normaCon(anioSiguiente, "null", "null");
          case HASTA_POR_PERIODO -> normaCon(FECHA, esteAnio, "null");
          case VIGENCIA_PROPIA_DESDE -> normaCon(FECHA, "null", vigenciaPropia(esteAnio, "null"));
          case VIGENCIA_PROPIA_HASTA -> normaCon(FECHA, "null", vigenciaPropia(FECHA, esteAnio));
        };

    assertThatThrownBy(() -> importarTexto(json))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("periodo");

    assertThat(normasGuardadas.count()).isZero();
    assertThat(reglas.count()).isZero();
  }

  @Test
  void uc07_ext6b_unaAfectacionAUnArticuloDeOtraNormaSeRechaza() throws IOException {
    cargarProgramas();
    importarNorma("semilla/acuerdo-003a-2022.json");

    assertThatThrownBy(() -> importarTexto(afectacionAUnArticuloDelAcuerdo003A()))
        .isInstanceOf(ArchivoDeNormativaInvalidoException.class)
        .hasMessageContaining("Afecta un artículo de otra norma")
        .hasMessageContaining("art. 1");

    assertThat(normasGuardadas.count()).isEqualTo(1);
  }

  private ResultadoDeImportacionDeNormas importarTexto(String json) {
    return normas.importar(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }

  private static String periodo(String texto) {
    return "{ \"tipo\": \"PERIODO\", \"periodo\": \"" + texto + "\" }";
  }

  private static String vigenciaPropia(String aplicaDesde, String vigenteHasta) {
    return "{ \"aplicaDesde\": " + aplicaDesde + ", \"vigenteHasta\": " + vigenteHasta + " }";
  }

  private static String normaCon(String aplicaDesde, String vigenteHasta, String vigenciaPropia) {
    String json =
        """
        {
          "schemaVersion": "1.0",
          "programa": {
            "codigoSia": "2879",
            "nombre": "Ingeniería de Sistemas y Computación",
            "facultad": "Ingeniería"
          },
          "norma": {
            "tipo": "CIRCULAR",
            "numero": "98",
            "anio": 2026,
            "organo": "Vicerrectoría Académica (datos de prueba)",
            "tipoDeEfecto": "BASE",
            "fechaExpedicion": "2026-01-15",
            "vigencia": {
              "aplicaDesde": %s,
              "vigenteHasta": %s
            },
            "enlaceRegimenLegal": null,
            "alcance": {
              "tipo": "PROGRAMAS",
              "facultades": [],
              "programas": ["2879"],
              "exclusiones": [],
              "texto": "Norma ficticia de prueba (datos de prueba)."
            }
          },
          "reglas": [
            {
              "categoria": "OTRA",
              "objeto": "Regla ficticia (datos de prueba)",
              "enunciado": "Regla ficticia para probar la guarda de periodos (datos de prueba).",
              "articulo": "art. 1",
              "vigenciaPropia": %s,
              "clasificacion": "INFORMATIVA",
              "afectacion": null,
              "datos": { "tipo": "OTRA" }
            }
          ]
        }
        """;
    return json.formatted(aplicaDesde, vigenteHasta, vigenciaPropia);
  }

  private static String afectacionAUnArticuloDelAcuerdo003A() {
    String json =
        """
        {
          "schemaVersion": "1.0",
          "programa": {
            "codigoSia": "2879",
            "nombre": "Ingeniería de Sistemas y Computación",
            "facultad": "Ingeniería"
          },
          "norma": {
            "tipo": "CIRCULAR",
            "numero": "98",
            "anio": 2026,
            "organo": "Vicerrectoría Académica (datos de prueba)",
            "tipoDeEfecto": "BASE",
            "fechaExpedicion": "2026-01-15",
            "vigencia": {
              "aplicaDesde": { "tipo": "FECHA", "fecha": "2026-01-15" },
              "vigenteHasta": null
            },
            "enlaceRegimenLegal": null,
            "alcance": {
              "tipo": "PROGRAMAS",
              "facultades": [],
              "programas": ["2879"],
              "exclusiones": [],
              "texto": "Norma ficticia de prueba (datos de prueba)."
            }
          },
          "reglas": [
            {
              "categoria": "OTRA",
              "objeto": "Afecta un artículo de otra norma (datos de prueba)",
              "enunciado": "Regla ficticia que afecta el artículo 7 del Acuerdo 003-A (datos de prueba).",
              "articulo": "art. 1",
              "vigenciaPropia": null,
              "clasificacion": "INFORMATIVA",
              "afectacion": {
                "tipo": "MODIFICA",
                "destino": {
                  "tipo": "ARTICULO",
                  "norma": {
                    "tipo": "ACUERDO",
                    "numero": "003-A",
                    "anio": 2022,
                    "organo": "Consejo Académico"
                  },
                  "articulo": "art. 7"
                },
                "desde": null
              },
              "datos": { "tipo": "OTRA" }
            }
          ]
        }
        """;
    return json;
  }

  private enum CasoDePeriodo {
    INICIO_DEL_ANIO,
    INICIO_FUTURO,
    HASTA_POR_PERIODO,
    VIGENCIA_PROPIA_DESDE,
    VIGENCIA_PROPIA_HASTA
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
