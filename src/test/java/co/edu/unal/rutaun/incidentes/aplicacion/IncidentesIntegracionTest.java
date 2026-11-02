package co.edu.unal.rutaun.incidentes.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.incidentes.IncidenteDetectado;
import co.edu.unal.rutaun.incidentes.IncidenteInterno;
import co.edu.unal.rutaun.incidentes.persistencia.IncidenteEntidad;
import co.edu.unal.rutaun.incidentes.persistencia.IncidenteJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Pruebas de integración del registro de incidentes (SOP-05, #57). */
@PruebaDeIntegracion
class IncidentesIntegracionTest {

  private static final UUID PROGRAMA = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final IncidentesService servicio;
  private final ApplicationEventPublisher publicador;
  private final IncidenteJpaRepository repositorio;
  private final TransactionTemplate transaccion;

  /**
   * Recibe las piezas reales que se prueban.
   *
   * @param servicio servicio de incidentes
   * @param publicador publicador de eventos de Spring
   * @param repositorio repositorio de incidentes
   * @param gestor gestor de transacciones
   */
  @Autowired
  IncidentesIntegracionTest(
      IncidentesService servicio,
      ApplicationEventPublisher publicador,
      IncidenteJpaRepository repositorio,
      PlatformTransactionManager gestor) {
    this.servicio = servicio;
    this.publicador = publicador;
    this.repositorio = repositorio;
    this.transaccion = new TransactionTemplate(gestor);
  }

  @BeforeEach
  void limpiar() {
    repositorio.deleteAll();
  }

  @Test
  void sop05_registraUnIncidenteConCausaYContexto() {
    publicador.publishEvent(eventoDePrograma("Programa sin reglas cargadas"));

    List<IncidenteEntidad> guardados = repositorio.findAll();
    assertThat(guardados).hasSize(1);
    assertThat(guardados.get(0).getCausa()).isEqualTo("Programa sin reglas cargadas");
    assertThat(guardados.get(0).getCasoDeUso()).isEqualTo("UC06-HU01");
    assertThat(guardados.get(0).getProgramaId()).isEqualTo(PROGRAMA);
    assertThat(guardados.get(0).getVersionConsolidado()).isEqualTo(1);
  }

  @Test
  void sop05_registraUnIncidenteInternoSinPrograma() {
    publicador.publishEvent(eventoInterno("Falta el periodo en curso"));

    List<IncidenteEntidad> guardados = repositorio.findAll();
    assertThat(guardados).hasSize(1);
    assertThat(guardados.get(0).getProgramaId()).isNull();
    assertThat(guardados.get(0).getVersionConsolidado()).isNull();
  }

  @Test
  void sop05_registraAunqueLaOperacionOriginalFalle() {
    assertThatThrownBy(
            () ->
                transaccion.executeWithoutResult(
                    estado -> {
                      publicador.publishEvent(eventoDePrograma("Falla de prueba"));
                      throw new IllegalStateException("falla la operación original");
                    }))
        .isInstanceOf(IllegalStateException.class);

    assertThat(repositorio.findAll()).hasSize(1);
  }

  @Test
  void sop05_incidentesIgualesSeAgrupanConConteoYFechas() {
    servicio.registrar(eventoDePrograma("Misma causa"));
    servicio.registrar(eventoDePrograma("Misma causa"));

    List<IncidenteEntidad> guardados = repositorio.findAll();
    assertThat(guardados).hasSize(1);
    assertThat(guardados.get(0).getConteo()).isEqualTo(2);
    assertThat(guardados.get(0).getUltimaFecha())
        .isAfterOrEqualTo(guardados.get(0).getPrimeraFecha());
  }

  @Test
  void sop05_incidentesDeCausaDistintaNoSeAgrupan() {
    servicio.registrar(eventoDePrograma("Una causa"));
    servicio.registrar(eventoDePrograma("Otra causa"));

    assertThat(repositorio.findAll()).hasSize(2);
  }

  @Test
  void sop05_incidenteAtendidoNoSeAgrupaYSeRegistraDeNuevo() {
    LocalDateTime fecha = LocalDateTime.of(2026, 11, 1, 10, 0);
    repositorio.save(
        new IncidenteEntidad(
            UUID.randomUUID(),
            "UC06-HU01",
            "ext. 4a",
            PROGRAMA,
            1,
            "Misma causa",
            "programa",
            fecha,
            fecha,
            1,
            true));

    servicio.registrar(eventoDePrograma("Misma causa"));

    assertThat(repositorio.findAll()).hasSize(2);
  }

  private static IncidenteDetectado eventoDePrograma(String causa) {
    return new IncidenteDePrograma(
        "UC06-HU01", "ext. 4a", new ProgramaId(PROGRAMA), 1, causa, List.of("programa"));
  }

  private static IncidenteDetectado eventoInterno(String causa) {
    return new IncidenteInterno("UC01", "paso 5", causa, List.of());
  }
}
