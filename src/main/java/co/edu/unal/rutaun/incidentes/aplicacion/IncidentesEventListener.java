package co.edu.unal.rutaun.incidentes.aplicacion;

import co.edu.unal.rutaun.incidentes.IncidenteDetectado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Escucha {@link IncidenteDetectado} y lo registra en una transacción nueva (REQUIRES_NEW), para
 * que el incidente quede guardado aunque la operación original falle (SOP-05). No usa el registro
 * de publicación con reintentos de Spring Modulith.
 */
@Component
public class IncidentesEventListener {

  private static final Logger LOG = LoggerFactory.getLogger(IncidentesEventListener.class);

  private final IncidentesService servicio;
  private final TransactionTemplate transaccionNueva;

  /**
   * Crea el oyente.
   *
   * @param servicio servicio que registra los incidentes
   * @param gestor gestor de transacciones de la aplicación
   */
  public IncidentesEventListener(IncidentesService servicio, PlatformTransactionManager gestor) {
    this.servicio = servicio;
    this.transaccionNueva = new TransactionTemplate(gestor);
    this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Registra el incidente en una transacción propia. Si el registro falla, lo anota en el log sin
   * el contenido del incidente y no propaga el error, para no tapar la falla original.
   *
   * @param evento el incidente detectado
   */
  @EventListener
  public void alDetectarIncidente(IncidenteDetectado evento) {
    try {
      transaccionNueva.executeWithoutResult(estado -> servicio.registrar(evento));
    } catch (RuntimeException e) {
      LOG.error("No se pudo registrar un incidente: {}", e.getClass().getSimpleName());
    }
  }
}
