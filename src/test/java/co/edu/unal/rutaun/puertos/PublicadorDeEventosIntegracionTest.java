package co.edu.unal.rutaun.puertos;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.incidentes.IncidenteDetectado;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@PruebaDeIntegracion
@RecordApplicationEvents
class PublicadorDeEventosIntegracionTest {

  @Autowired private DomainEventPublisher publicador;
  @Autowired private ApplicationEvents eventos;

  @Test
  void tt02_elContextoEntregaElIncidenteAlBusDeSpring() {
    IncidenteDetectado incidente =
        new IncidenteDePrograma(
            "UC06", "ext. 4a", ProgramaId.nuevo(), 1, "consolidado sin reglas", List.of());

    publicador.publicar(incidente);

    assertThat(eventos.stream(IncidenteDetectado.class)).containsExactly(incidente);
  }
}
