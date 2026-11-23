package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.puertos.DomainEventPublisher;

/**
 * Dummy de {@link DomainEventPublisher}: se pasa a un constructor que lo exige, en una prueba donde
 * no debe usarse. Si algo intenta publicar un evento, falla.
 */
public final class PublicadorDeEventosDeRelleno implements DomainEventPublisher {

  @Override
  public void publicar(EventoDeDominio evento) {
    throw new IllegalStateException("El publicador de relleno no debe usarse: " + evento);
  }
}
