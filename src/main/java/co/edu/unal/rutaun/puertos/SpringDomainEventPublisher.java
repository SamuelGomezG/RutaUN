package co.edu.unal.rutaun.puertos;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Adaptador del puerto {@link DomainEventPublisher} sobre el bus de eventos de Spring. */
@Component
final class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher bus;

  SpringDomainEventPublisher(ApplicationEventPublisher bus) {
    this.bus = ArgumentChecks.obligatorio(bus, "bus");
  }

  @Override
  public void publicar(EventoDeDominio evento) {
    bus.publishEvent(ArgumentChecks.obligatorio(evento, "evento"));
  }
}
