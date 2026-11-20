package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@PruebaDeIntegracion
@Transactional
class PersistenciaNormativaTest {

  @Autowired private ProgramaJpaRepository programas;
  @Autowired private MateriaJpaRepository materias;
  @Autowired private NormaJpaRepository normas;
  @Autowired private ReglaJpaRepository reglas;
  @Autowired private ConsolidadoJpaRepository consolidados;
  @Autowired private VersionJpaRepository versiones;
  @Autowired private ReglaEnVersionJpaRepository reglasEnVersion;
  @Autowired private EntityManager em;

  private void vaciarMemoria() {
    em.flush();
    em.clear(); // obliga a que la lectura venga de la base de datos
  }

  private ProgramaEntity nuevoPrograma() {
    return programas.save(
        new ProgramaEntity(UUID.randomUUID(), "Ingenieria de Sistemas", "2879", "Ingenieria"));
  }

  private NormaEntity nuevaNorma() {
    return normas.save(
        new NormaEntity(
            UUID.randomUUID(),
            "ACUERDO",
            "003-A",
            2022,
            "Consejo Superior",
            "BASE",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 2, 1),
            null,
            null,
            "PROGRAMAS",
            "{}"));
  }

  private VersionEntity nuevaVersion(ConsolidadoEntity consolidado, int numero) {
    return new VersionEntity(
        UUID.randomUUID(),
        consolidado.getId(),
        consolidado.getProgramaId(),
        numero,
        LocalDate.of(2026, 10, 22),
        "INCORPORACION",
        null,
        null,
        1,
        "[]");
  }

  @Test
  void tt04_guardaYLeeUnPrograma() {
    ProgramaEntity programa = nuevoPrograma();
    vaciarMemoria();

    assertThat(programas.findById(programa.getId()))
        .get()
        .extracting(ProgramaEntity::getCodigoSia)
        .isEqualTo("2879");
  }

  @Test
  void tt04_guardaYLeeUnaMateria() {
    materias.save(new MateriaEntity("2015714", 3));
    vaciarMemoria();

    assertThat(materias.findById("2015714"))
        .get()
        .extracting(MateriaEntity::getCreditos)
        .isEqualTo(3);
  }

  @Test
  void tt04_guardaYLeeUnaNorma() {
    NormaEntity norma = nuevaNorma();
    vaciarMemoria();

    assertThat(normas.findById(norma.getId()))
        .get()
        .extracting(NormaEntity::getNumero)
        .isEqualTo("003-A");
  }

  @Test
  void tt04_guardaYLeeUnaReglaConSusDatosJson() {
    NormaEntity norma = nuevaNorma();
    String json = "{\"materia\":\"2015714\",\"creditos\":3}";
    ReglaEntity regla =
        reglas.save(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "MATERIA",
                "Calculo diferencial",
                "La materia vale 3 creditos.",
                "Articulo 5",
                null,
                null,
                "DEL_PLAN",
                null,
                null,
                null,
                json));
    vaciarMemoria();

    assertThat(reglas.findById(regla.getId()))
        .get()
        .extracting(ReglaEntity::getDatos)
        .isEqualTo(json);
  }

  @Test
  void tt04_guardaYLeeUnConsolidadoConSuVersionYSusReglas() {
    ProgramaEntity programa = nuevoPrograma();
    NormaEntity norma = nuevaNorma();
    ReglaEntity regla =
        reglas.save(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "OTRA",
                "General",
                "Regla informativa.",
                "Articulo 1",
                null,
                null,
                "INFORMATIVA",
                null,
                null,
                null,
                null));
    ConsolidadoEntity consolidado =
        consolidados.save(new ConsolidadoEntity(UUID.randomUUID(), programa.getId()));
    VersionEntity version = versiones.save(nuevaVersion(consolidado, 1));
    ReglaEnVersionEntity enVersion =
        reglasEnVersion.save(
            new ReglaEnVersionEntity(UUID.randomUUID(), version.getId(), regla.getId(), "VIGENTE"));
    vaciarMemoria();

    assertThat(reglasEnVersion.findById(enVersion.getId()))
        .get()
        .extracting(ReglaEnVersionEntity::getEstado)
        .isEqualTo("VIGENTE");
    assertThat(consolidados.findById(consolidado.getId())).isPresent();
    assertThat(versiones.findById(version.getId()))
        .get()
        .extracting(VersionEntity::getNumero)
        .isEqualTo(1);
  }

  @Test
  void tt04_dosVersionesDelMismoProgramaConElMismoNumeroFallan() {
    ProgramaEntity programa = nuevoPrograma();
    ConsolidadoEntity consolidado =
        consolidados.save(new ConsolidadoEntity(UUID.randomUUID(), programa.getId()));
    versiones.saveAndFlush(nuevaVersion(consolidado, 1));

    assertThatThrownBy(() -> versiones.saveAndFlush(nuevaVersion(consolidado, 1)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
