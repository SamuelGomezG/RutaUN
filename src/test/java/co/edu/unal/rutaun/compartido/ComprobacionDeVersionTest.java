package co.edu.unal.rutaun.compartido;

import static co.edu.unal.rutaun.compartido.ComponenteVersionado.AVANCE;
import static co.edu.unal.rutaun.compartido.ComponenteVersionado.PERIODO_EN_CURSO;
import static co.edu.unal.rutaun.compartido.ComponenteVersionado.PLAN_VIGENTE;
import static co.edu.unal.rutaun.compartido.ComponenteVersionado.VERSION_DEL_CONSOLIDADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ComprobacionDeVersionTest {

  private final AtomicReference<String> avance = new AtomicReference<>("3");
  private final AtomicReference<String> consolidado = new AtomicReference<>("1");
  private final AtomicReference<String> plan = new AtomicReference<>("7");
  private final AtomicReference<String> periodo = new AtomicReference<>("2026-2");

  private List<FuenteDeVersion> fuentes() {
    return List.of(
        FuenteDeVersion.de(AVANCE, avance::get),
        FuenteDeVersion.de(VERSION_DEL_CONSOLIDADO, consolidado::get),
        FuenteDeVersion.de(PLAN_VIGENTE, plan::get),
        FuenteDeVersion.de(PERIODO_EN_CURSO, periodo::get));
  }

  private static ResultadoDeComprobacion cambiaron(ComponenteVersionado... componentes) {
    return new ResultadoDeComprobacion.Cambiaron(List.of(componentes));
  }

  private List<FuenteDeVersion> fuentesAlReves() {
    return List.of(
        FuenteDeVersion.de(PERIODO_EN_CURSO, periodo::get),
        FuenteDeVersion.de(PLAN_VIGENTE, plan::get),
        FuenteDeVersion.de(VERSION_DEL_CONSOLIDADO, consolidado::get),
        FuenteDeVersion.de(AVANCE, avance::get));
  }

  @Test
  void sop07_sinCambiosDevuelveSinCambios() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes()))
        .isEqualTo(new ResultadoDeComprobacion.SinCambios());
  }

  @Test
  void uc01_ext10a_avanceCambiadoSeInforma() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    avance.set("4");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes())).isEqualTo(cambiaron(AVANCE));
  }

  @Test
  void uc01_ext10a_versionDelConsolidadoCambiadaSeInforma() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    consolidado.set("2");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes()))
        .isEqualTo(cambiaron(VERSION_DEL_CONSOLIDADO));
  }

  @Test
  void uc01_ext10c_planVigenteCambiadoSeInforma() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    plan.set("8");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes()))
        .isEqualTo(cambiaron(PLAN_VIGENTE));
  }

  @Test
  void uc01_ext10c_periodoCambiadoSeInforma() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    periodo.set("2027-1");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes()))
        .isEqualTo(cambiaron(PERIODO_EN_CURSO));
  }

  @Test
  void sop07_variosCambiosSeInformanEnElOrdenDelEnum() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentesAlReves());
    periodo.set("2027-1");
    avance.set("4");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentesAlReves()))
        .isEqualTo(cambiaron(AVANCE, PERIODO_EN_CURSO));
  }

  @Test
  void sop07_planAusenteAlCargarYPresenteDespuesEsCambio() {
    plan.set(FuenteDeVersion.SIN_VALOR);
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    plan.set("1");

    assertThat(ComprobacionDeVersion.comprobar(huella, fuentes()))
        .isEqualTo(cambiaron(PLAN_VIGENTE));
  }

  @Test
  void sop07_soloCompruebaLosComponentesQueElCasoLeyo() {
    List<FuenteDeVersion> soloAvanceYPeriodo =
        List.of(
            FuenteDeVersion.de(AVANCE, avance::get),
            FuenteDeVersion.de(PERIODO_EN_CURSO, periodo::get));
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(soloAvanceYPeriodo);
    plan.set("8");

    assertThat(ComprobacionDeVersion.comprobar(huella, soloAvanceYPeriodo))
        .isEqualTo(new ResultadoDeComprobacion.SinCambios());
  }

  @Test
  void sop07_comprobarIgnoraLasFuentesQueNoEstabanEnLaHuella() {
    List<FuenteDeVersion> soloAvanceYPeriodo =
        List.of(
            FuenteDeVersion.de(AVANCE, avance::get),
            FuenteDeVersion.de(PERIODO_EN_CURSO, periodo::get));
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(soloAvanceYPeriodo);
    plan.set("8");
    List<FuenteDeVersion> conUnaDeMas =
        List.of(
            FuenteDeVersion.de(AVANCE, avance::get),
            FuenteDeVersion.de(PERIODO_EN_CURSO, periodo::get),
            FuenteDeVersion.de(PLAN_VIGENTE, plan::get));

    assertThat(ComprobacionDeVersion.comprobar(huella, conUnaDeMas))
        .isEqualTo(new ResultadoDeComprobacion.SinCambios());
  }

  @Test
  void sop07_fuenteFaltanteParaUnComponenteDeLaHuellaFalla() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());
    List<FuenteDeVersion> soloAvance = List.of(FuenteDeVersion.de(AVANCE, avance::get));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> ComprobacionDeVersion.comprobar(huella, soloAvance))
        .withMessageContaining("Falta la fuente");
  }

  @Test
  void sop07_dosFuentesDelMismoComponenteFalla() {
    List<FuenteDeVersion> repetidas =
        List.of(FuenteDeVersion.de(AVANCE, avance::get), FuenteDeVersion.de(AVANCE, avance::get));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> ComprobacionDeVersion.tomar(repetidas))
        .withMessageContaining("dos fuentes");
  }

  @Test
  void sop07_sinFuentesFalla() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> ComprobacionDeVersion.tomar(List.of()))
        .withMessageContaining("al menos un componente");
  }

  @Test
  void sop07_fuenteQueDevuelveNullAlTomarFalla() {
    List<FuenteDeVersion> conNull = List.of(FuenteDeVersion.de(AVANCE, () -> null));

    assertThatNullPointerException()
        .isThrownBy(() -> ComprobacionDeVersion.tomar(conNull))
        .withMessageContaining("SIN_VALOR");
  }

  @Test
  void sop07_fuenteQueDevuelveNullAlComprobarFalla() {
    HuellaDeVersion huella =
        ComprobacionDeVersion.tomar(List.of(FuenteDeVersion.de(AVANCE, avance::get)));
    List<FuenteDeVersion> conNull = List.of(FuenteDeVersion.de(AVANCE, () -> null));

    assertThatNullPointerException()
        .isThrownBy(() -> ComprobacionDeVersion.comprobar(huella, conNull))
        .withMessageContaining("SIN_VALOR");
  }

  @Test
  void sop07_laHuellaNoCambiaSiSeModificaElMapaOriginal() {
    Map<ComponenteVersionado, String> valores = new EnumMap<>(ComponenteVersionado.class);
    valores.put(AVANCE, "3");
    HuellaDeVersion huella = new HuellaDeVersion(valores);

    valores.put(AVANCE, "4");

    assertThat(huella.valores()).containsEntry(AVANCE, "3");
  }

  @Test
  void sop07_laHuellaNoSePuedeModificarPorFuera() {
    HuellaDeVersion huella = ComprobacionDeVersion.tomar(fuentes());

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> huella.valores().put(AVANCE, "9"));
  }

  @Test
  void sop07_cambiaronNoCambiaSiSeModificaLaListaOriginal() {
    List<ComponenteVersionado> lista = new ArrayList<>(List.of(AVANCE));
    ResultadoDeComprobacion.Cambiaron resultado = new ResultadoDeComprobacion.Cambiaron(lista);

    lista.add(PLAN_VIGENTE);

    assertThat(resultado.componentes()).containsExactly(AVANCE);
  }

  @Test
  void sop07_losComponentesQueCambiaronNoSePuedenModificarPorFuera() {
    ResultadoDeComprobacion.Cambiaron resultado =
        new ResultadoDeComprobacion.Cambiaron(new ArrayList<>(List.of(AVANCE)));

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> resultado.componentes().add(PLAN_VIGENTE));
  }
}
