package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Creditos;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class ExigenciaCreditosTest {

  @Test
  void tt02_exigenciaSinDesgloseGuardaSoloLosCreditosExigidos() {
    ExigenciaCreditos exigencia = ExigenciaCreditos.de(new Creditos(16));

    assertThat(exigencia.exigidos()).isEqualTo(new Creditos(16));
    assertThat(exigencia.desglose()).isEmpty();
  }

  @Test
  void tt02_exigenciaConDesgloseSumaObligatoriosYOptativos() {
    ExigenciaCreditos exigencia = ExigenciaCreditos.conDesglose(new Creditos(15), new Creditos(36));

    assertThat(exigencia.exigidos()).isEqualTo(new Creditos(51));
    assertThat(exigencia.desglose())
        .contains(new ExigenciaCreditos.Desglose(new Creditos(15), new Creditos(36)));
  }

  @Test
  void tt02_exigenciaRechazaUnDesgloseQueNoSumaLoExigido() {
    ExigenciaCreditos.Desglose desglose =
        new ExigenciaCreditos.Desglose(new Creditos(15), new Creditos(36));

    assertThatThrownBy(
            () ->
                new ExigenciaCreditos(
                    new Creditos(50), Optional.of(desglose), Collections.emptySortedMap()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("el desglose debe sumar los créditos exigidos");
  }

  @Test
  void tt02_exigenciaRechazaDatosNull() {
    assertThatThrownBy(() -> ExigenciaCreditos.de(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("exigidos no puede ser null");
    assertThatThrownBy(
            () -> new ExigenciaCreditos(new Creditos(1), null, Collections.emptySortedMap()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("desglose no puede ser null");
    assertThatThrownBy(() -> new ExigenciaCreditos.Desglose(null, new Creditos(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("obligatorios no puede ser null");
    assertThatThrownBy(() -> new ExigenciaCreditos.Desglose(new Creditos(1), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("optativos no puede ser null");
  }

  @Test
  void tt02_laExigenciaGuardaLosCreditosSugeridosPorSemestre() {
    SortedMap<Integer, Creditos> sugeridos = new TreeMap<>();
    sugeridos.put(5, new Creditos(4));
    sugeridos.put(1, new Creditos(2));

    ExigenciaCreditos exigencia = ExigenciaCreditos.de(new Creditos(6)).conSugeridos(sugeridos);

    assertThat(exigencia.exigidos()).isEqualTo(new Creditos(6));
    assertThat(exigencia.desglose()).isEmpty();
    assertThat(exigencia.sugeridosPorSemestre())
        .containsExactly(Map.entry(1, new Creditos(2)), Map.entry(5, new Creditos(4)));
    assertThat(ExigenciaCreditos.de(new Creditos(6)).sugeridosPorSemestre()).isEmpty();
    assertThat(
            ExigenciaCreditos.conDesglose(new Creditos(4), new Creditos(2)).sugeridosPorSemestre())
        .isEmpty();
  }

  @Test
  void tt02_unSemestreSugeridoNoPositivoSeRechaza() {
    SortedMap<Integer, Creditos> cero = new TreeMap<>();
    cero.put(0, new Creditos(2));
    SortedMap<Integer, Creditos> negativo = new TreeMap<>();
    negativo.put(-1, new Creditos(2));

    assertThatThrownBy(() -> ExigenciaCreditos.de(new Creditos(6)).conSugeridos(cero))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("semestre");
    assertThatThrownBy(() -> ExigenciaCreditos.de(new Creditos(6)).conSugeridos(negativo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("semestre");
  }

  @Test
  void tt02_losCreditosSugeridosNoSePuedenModificarNiDependenDelMapaOriginal() {
    SortedMap<Integer, Creditos> original = new TreeMap<>();
    original.put(1, new Creditos(2));
    ExigenciaCreditos exigencia = ExigenciaCreditos.de(new Creditos(6)).conSugeridos(original);

    original.put(2, new Creditos(3));

    assertThat(exigencia.sugeridosPorSemestre()).containsOnly(Map.entry(1, new Creditos(2)));
    assertThatThrownBy(() -> exigencia.sugeridosPorSemestre().put(3, new Creditos(1)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt02_exigenciaRechazaSugeridosNullOConValoresNull() {
    assertThatThrownBy(() -> ExigenciaCreditos.de(new Creditos(6)).conSugeridos(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sugeridosPorSemestre no puede ser null");

    SortedMap<Integer, Creditos> conValorNull = new TreeMap<>();
    conValorNull.put(1, null);
    assertThatThrownBy(() -> ExigenciaCreditos.de(new Creditos(6)).conSugeridos(conValorNull))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("créditos sugeridos no puede ser null");

    SortedMap<Integer, Creditos> conClaveNull =
        new TreeMap<Integer, Creditos>(Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));
    conClaveNull.put(null, new Creditos(2));
    assertThatThrownBy(() -> ExigenciaCreditos.de(new Creditos(6)).conSugeridos(conClaveNull))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("semestre no puede ser null");
  }
}
