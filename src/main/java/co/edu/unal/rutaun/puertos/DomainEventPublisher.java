package co.edu.unal.rutaun.puertos;

import co.edu.unal.rutaun.compartido.EventoDeDominio;

/** Puerto por el que los servicios y los orquestadores publican eventos de dominio. */
public interface DomainEventPublisher {

  /** Publica el evento para que lo reciban los oyentes de los demás contextos. */
  void publicar(EventoDeDominio evento);
}
