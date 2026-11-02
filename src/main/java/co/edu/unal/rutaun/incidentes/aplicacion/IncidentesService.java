package co.edu.unal.rutaun.incidentes.aplicacion;

import co.edu.unal.rutaun.incidentes.IncidenteDePrograma;
import co.edu.unal.rutaun.incidentes.IncidenteDetectado;
import co.edu.unal.rutaun.incidentes.IncidenteInterno;
import co.edu.unal.rutaun.incidentes.persistencia.IncidenteEntidad;
import co.edu.unal.rutaun.incidentes.persistencia.IncidenteJpaRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra incidentes con su causa y su contexto. Los incidentes iguales y aún no atendidos se
 * agrupan en un solo registro, con primera y última fecha y conteo (SOP-05).
 */
@Service
public class IncidentesService {

  private static final int LIMITE_CORTO = 40;
  private static final int LIMITE_CAUSA = 500;

  private final IncidenteJpaRepository repositorio;
  private final Clock reloj;

  /**
   * Crea el servicio.
   *
   * @param repositorio repositorio de incidentes
   * @param reloj reloj con el que se fechan los incidentes
   */
  public IncidentesService(IncidenteJpaRepository repositorio, Clock reloj) {
    this.repositorio = repositorio;
    this.reloj = reloj;
  }

  /**
   * Registra el incidente: si ya hay uno igual sin atender, le suma una ocurrencia; si no, crea un
   * registro nuevo. Dos incidentes son iguales si coinciden caso de uso, paso, causa, programa,
   * versión del consolidado y elementos.
   *
   * @param evento el incidente detectado
   */
  @Transactional
  public void registrar(IncidenteDetectado evento) {
    IncidenteEntidad candidato = nuevo(evento);
    repositorio
        .findByCasoDeUsoAndPasoAndCausaAndAtendidoFalse(
            candidato.getCasoDeUso(), candidato.getPaso(), candidato.getCausa())
        .stream()
        .filter(existente -> esIgual(existente, candidato))
        .findFirst()
        .ifPresentOrElse(
            igual -> igual.registrarOtraOcurrencia(candidato.getUltimaFecha()),
            () -> repositorio.save(candidato));
  }

  private IncidenteEntidad nuevo(IncidenteDetectado evento) {
    LocalDateTime ahora = LocalDateTime.now(reloj);
    return new IncidenteEntidad(
        UUID.randomUUID(),
        recortar(evento.casoDeUso(), LIMITE_CORTO),
        recortar(evento.paso(), LIMITE_CORTO),
        programaDe(evento),
        versionDe(evento),
        recortar(evento.causa(), LIMITE_CAUSA),
        elementosDe(evento),
        ahora,
        ahora,
        1,
        false);
  }

  private static boolean esIgual(IncidenteEntidad existente, IncidenteEntidad candidato) {
    return Objects.equals(existente.getProgramaId(), candidato.getProgramaId())
        && Objects.equals(existente.getVersionConsolidado(), candidato.getVersionConsolidado())
        && Objects.equals(existente.getElementos(), candidato.getElementos());
  }

  private static UUID programaDe(IncidenteDetectado evento) {
    return switch (evento) {
      case IncidenteDePrograma deprograma -> deprograma.programa().valor();
      case IncidenteInterno interno -> null;
    };
  }

  private static Integer versionDe(IncidenteDetectado evento) {
    return switch (evento) {
      case IncidenteDePrograma deprograma -> deprograma.versionDelConsolidado();
      case IncidenteInterno interno -> null;
    };
  }

  private static String elementosDe(IncidenteDetectado evento) {
    return String.join(", ", evento.elementos().stream().sorted().toList());
  }

  private static String recortar(String texto, int maximo) {
    return texto.length() <= maximo ? texto : texto.substring(0, maximo);
  }
}
