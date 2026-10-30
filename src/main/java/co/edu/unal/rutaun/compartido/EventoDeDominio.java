package co.edu.unal.rutaun.compartido;

/**
 * Marca los eventos de dominio. Los agregados los acumulan y el servicio que los guarda los entrega
 * al puerto {@code DomainEventPublisher}; también los publican los orquestadores cuando una
 * extensión del SRS registra un incidente sin que haya un agregado que guardar.
 */
public interface EventoDeDominio {}
