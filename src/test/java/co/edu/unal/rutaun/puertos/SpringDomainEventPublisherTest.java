package co.edu.unal.rutaun.puertos;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SpringDomainEventPublisherTest {

  private record EventoDePrueba() implements EventoDeDominio {}

  @Test
  void tt02_publicarEntregaElEventoAlBusDeSpring() {
    ApplicationEventPublisher bus = mock(ApplicationEventPublisher.class);
    EventoDeDominio evento = new EventoDePrueba();

    new SpringDomainEventPublisher(bus).publicar(evento);

    verify(bus).publishEvent(evento);
  }

  @Test
  void tt02_publicarRechazaUnEventoNulo() {
    SpringDomainEventPublisher publicador =
        new SpringDomainEventPublisher(mock(ApplicationEventPublisher.class));

    assertThatThrownBy(() -> publicador.publicar(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("evento no puede ser null");
  }

  @Test
  void tt02_elPublicadorExigeUnBus() {
    assertThatThrownBy(() -> new SpringDomainEventPublisher(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("bus no puede ser null");
  }
}
