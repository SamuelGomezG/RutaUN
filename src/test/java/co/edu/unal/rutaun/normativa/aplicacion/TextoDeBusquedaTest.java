package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoNorma;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** La búsqueda por texto de UC06 (paso 6): enunciado, acuerdo y artículo, sin tildes. */
class TextoDeBusquedaTest {

  private static final ReglaVista REGLA =
      new ReglaVista(
          new ReglaId(UUID.randomUUID()),
          "El mínimo de créditos por periodo es de 10.",
          Optional.of(
              new FuenteVista(
                  new NormaVista(
                      TipoNorma.ACUERDO, "11", 2023, "Consejo de Facultad de Ingeniería"),
                  "art. 4",
                  Optional.empty())),
          EstadoRegla.VIGENTE,
          false,
          ClasificacionRegla.DEL_PLAN);

  @Test
  void uc06_paso6_buscaEnElEnunciado() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "mínimo")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "periodo")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "prerrequisito")).isFalse();
  }

  @Test
  void uc06_paso6_noDistingueMayusculasNiTildes() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "CREDITOS")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "minimo")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "Créditos")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "INGENIERIA")).isTrue();
  }

  @Test
  void uc06_paso6_cadaPalabraDebeSerElComienzoDeUnaPalabraDeLaRegla() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "credit")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "créd")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "redito")).isFalse();
    assertThat(TextoDeBusqueda.coincide(REGLA, "ditos")).isFalse();
  }

  @Test
  void uc06_paso6_unNumeroNoSeEncuentraDentroDeUnCodigoMasLargo() {
    ReglaVista conCodigo =
        new ReglaVista(
            REGLA.id(),
            "Cátedra nacional de inducción (1000089-B): 2 créditos, optativa.",
            REGLA.fuente(),
            EstadoRegla.VIGENTE,
            false,
            ClasificacionRegla.DEL_PLAN);

    assertThat(TextoDeBusqueda.coincide(conCodigo, "008")).isFalse();
    assertThat(TextoDeBusqueda.coincide(conCodigo, "1000089")).isTrue();
    assertThat(TextoDeBusqueda.coincide(conCodigo, "1000089-B")).isTrue();
  }

  @Test
  void uc06_paso6_losGuionesYLosSignosSeparanPalabras() {
    ReglaVista deLaNormaConGuion =
        new ReglaVista(
            REGLA.id(),
            "Plan de estudios.",
            Optional.of(
                new FuenteVista(
                    new NormaVista(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico"),
                    "art. 5, literal a",
                    Optional.empty())),
            EstadoRegla.VIGENTE,
            false,
            ClasificacionRegla.DEL_PLAN);

    assertThat(TextoDeBusqueda.coincide(deLaNormaConGuion, "acuerdo 003")).isTrue();
    assertThat(TextoDeBusqueda.coincide(deLaNormaConGuion, "003-A")).isTrue();
    assertThat(TextoDeBusqueda.coincide(deLaNormaConGuion, "literal")).isTrue();
  }

  @Test
  void uc06_paso6_buscaEnElAcuerdoYEnElArticulo() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "acuerdo")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "acuerdo 11 de 2023")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "Consejo de Facultad")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "art. 4")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "resolución")).isFalse();
    assertThat(TextoDeBusqueda.coincide(REGLA, "2024")).isFalse();
  }

  @Test
  void uc06_paso6_laReglaDebeContenerTodasLasPalabrasEnCualquierOrden() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "periodo créditos")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "créditos acuerdo")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "créditos prerrequisito")).isFalse();
  }

  @Test
  void uc06_paso6_losEspaciosSobrantesNoCambianElResultado() {
    assertThat(TextoDeBusqueda.coincide(REGLA, "  créditos    periodo ")).isTrue();
    assertThat(TextoDeBusqueda.coincide(REGLA, "créditos\tperiodo")).isTrue();
  }

  @Test
  void uc06_paso6_unaReglaSinFuenteSoloSeBuscaEnSuEnunciado() {
    ReglaVista sinFuente =
        new ReglaVista(
            REGLA.id(),
            REGLA.enunciado(),
            Optional.empty(),
            EstadoRegla.VIGENTE,
            false,
            ClasificacionRegla.DEL_PLAN);

    assertThat(TextoDeBusqueda.coincide(sinFuente, "créditos")).isTrue();
    assertThat(TextoDeBusqueda.coincide(sinFuente, "acuerdo")).isFalse();
  }
}
