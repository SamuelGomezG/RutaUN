package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.normativa.TipoAlcance;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrganoTest {

  private static final Facultad ING = new Facultad("ING");
  private static final Facultad MED = new Facultad("MED");
  private static final Organo CONSEJO_ING =
      new Organo("Consejo de Facultad de Ingeniería", NivelDeOrgano.FACULTAD, Optional.of(ING));

  @Test
  void tt02_unOrganoDeFacultadExigeSuFacultad() {
    assertThatThrownBy(
            () -> new Organo("Consejo de Facultad", NivelDeOrgano.FACULTAD, Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un órgano de facultad exige su facultad");
    assertThatThrownBy(() -> new Organo("Consejo Académico", NivelDeOrgano.SEDE, Optional.of(ING)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("solo un órgano de facultad lleva facultad");
  }

  @Test
  void uc07_ext4h_unConsejoDeFacultadProponeLosProgramasDeSuFacultad() {
    Alcance propuesto = CONSEJO_ING.alcancePropuesto("Los programas de su facultad.");

    assertThat(propuesto.tipo()).isEqualTo(TipoAlcance.FACULTADES);
    assertThat(propuesto.facultades()).containsExactly(ING);
    assertThat(propuesto.programas()).isEmpty();
  }

  @Test
  void uc07_ext4h_unOrganoDeLaSedeProponeTodosLosProgramas() {
    for (NivelDeOrgano nivel : new NivelDeOrgano[] {NivelDeOrgano.SEDE, NivelDeOrgano.NACIONAL}) {
      Alcance propuesto =
          new Organo("Consejo Académico", nivel, Optional.empty())
              .alcancePropuesto("Todos los programas de pregrado.");

      assertThat(propuesto.tipo()).isEqualTo(TipoAlcance.TODOS);
      assertThat(propuesto.facultades()).isEmpty();
      assertThat(propuesto.programas()).isEmpty();
    }
  }

  @Test
  void tt02_elMismoNombreEnOtraFacultadEsOtroOrgano() {
    Organo consejoDeMedicina =
        new Organo("Consejo de Facultad", NivelDeOrgano.FACULTAD, Optional.of(MED));

    assertThat(new Organo("Consejo de Facultad", NivelDeOrgano.FACULTAD, Optional.of(ING)))
        .isNotEqualTo(consejoDeMedicina);
  }

  @Test
  void tt02_elMismoOrganoEscritoDistintoEsElMismo() {
    Organo escrito =
        new Organo(
            "  CONSEJO   de facultad de INGENIERÍA ",
            NivelDeOrgano.FACULTAD,
            Optional.of(new Facultad("ing")));

    assertThat(escrito).isEqualTo(CONSEJO_ING).hasSameHashCodeAs(CONSEJO_ING);
  }

  @Test
  void tt02_unSinonimoDelOrganoNoSeUnifica() {
    Organo consejoSuperior =
        new Organo("Consejo Superior", NivelDeOrgano.NACIONAL, Optional.empty());
    Organo consejoSuperiorUniversitario =
        new Organo("Consejo Superior Universitario", NivelDeOrgano.NACIONAL, Optional.empty());

    assertThat(consejoSuperior).isNotEqualTo(consejoSuperiorUniversitario);
  }
}
