package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.aplicacion.ConsultaNormativaService.Entrada;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * UC06-HU01-T4 (#132): el orden de las reglas dentro de una categoría: fecha de expedición de la
 * norma, artículo, enunciado e identificador.
 */
class OrdenDeReglasTest {

  private static final LocalDate DE_2008 = LocalDate.of(2008, 4, 15);
  private static final LocalDate DE_2023 = LocalDate.of(2023, 12, 7);
  private static final UUID ID_1 = new UUID(0, 1);
  private static final UUID ID_2 = new UUID(0, 2);

  private static Entrada entrada(LocalDate expedicion, String articulo, String enunciado, UUID id) {
    ReglaVista vista =
        new ReglaVista(
            ReglaId.nuevo(),
            enunciado,
            Optional.empty(),
            EstadoRegla.VIGENTE,
            false,
            ClasificacionRegla.DEL_PLAN);
    return new Entrada("Otra", expedicion, articulo, id, vista);
  }

  private static List<String> ordenados(Entrada... entradas) {
    return List.of(entradas).stream()
        .sorted(ConsultaNormativaService.ORDEN_DE_REGLAS)
        .map(entrada -> entrada.vista().enunciado())
        .toList();
  }

  @Test
  void uc06_paso4_laNormaMasAntiguaVaPrimero() {
    assertThat(
            ordenados(
                entrada(DE_2023, "art. 1", "reciente", ID_1),
                entrada(DE_2008, "art. 9", "antigua", ID_2)))
        .containsExactly("antigua", "reciente");
  }

  @Test
  void uc06_paso4_unaReglaSinNormaVaAlFinal() {
    assertThat(
            ordenados(
                entrada(null, "art. 1", "sin norma", ID_1),
                entrada(DE_2023, "art. 9", "con norma", ID_2)))
        .containsExactly("con norma", "sin norma");
  }

  @Test
  void uc06_paso4_conLaMismaNormaElArticuloSeOrdenaPorSuNumero() {
    assertThat(
            ordenados(
                entrada(DE_2008, "art. 10", "diez", ID_1), entrada(DE_2008, "art. 2", "dos", ID_2)))
        .containsExactly("dos", "diez");
  }

  @Test
  void uc06_paso4_conElMismoArticuloElEnunciadoSeOrdenaComoEnEspanol() {
    assertThat(
            ordenados(
                entrada(DE_2008, "art. 2", "Biología", ID_1),
                entrada(DE_2008, "art. 2", "Álgebra", ID_2)))
        .containsExactly("Álgebra", "Biología");
  }

  @Test
  void uc06_paso4_siTodoLoDemasEmpataDesempataElIdentificador() {
    List<UUID> ids =
        List.of(
                entrada(DE_2008, "art. 2", "igual", ID_2),
                entrada(DE_2008, "art. 2", "igual", ID_1))
            .stream()
            .sorted(ConsultaNormativaService.ORDEN_DE_REGLAS)
            .map(Entrada::id)
            .toList();

    assertThat(ids).containsExactly(ID_1, ID_2);
  }
}
