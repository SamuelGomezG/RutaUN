package co.edu.unal.rutaun.incidentes.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@PruebaDeIntegracion
@Transactional
class PersistenciaIncidentesTest {

  @Autowired private IncidenteRepository incidentes;
  @Autowired private EntityManager em;

  @Test
  void sop05_guardaYLeeUnIncidente() {
    LocalDateTime fecha = LocalDateTime.of(2026, 11, 1, 10, 0);
    IncidenteEntity incidente = incidentes.save(unIncidente(UUID.randomUUID(), 1, fecha));
    em.flush();
    em.clear();

    assertThat(incidentes.findById(incidente.getId()))
        .get()
        .extracting(IncidenteEntity::getConteo)
        .isEqualTo(1);
  }

  @Test
  void tt04_guardarComoNuevoUnIncidenteConUnIdExistenteFalla() {
    LocalDateTime fecha = LocalDateTime.of(2026, 11, 1, 10, 0);
    IncidenteEntity original = incidentes.save(unIncidente(UUID.randomUUID(), 1, fecha));
    em.flush();
    em.clear();

    assertThatThrownBy(() -> incidentes.saveAndFlush(unIncidente(original.getId(), 9, fecha)))
        .isInstanceOf(DataIntegrityViolationException.class);

    em.clear();
    assertThat(incidentes.findById(original.getId()))
        .get()
        .extracting(IncidenteEntity::getConteo)
        .isEqualTo(1);
  }

  private static IncidenteEntity unIncidente(UUID id, int conteo, LocalDateTime fecha) {
    return new IncidenteEntity(
        id,
        "UC01",
        "paso 5",
        null,
        null,
        "El calculo supero el plazo",
        "[]",
        fecha,
        fecha,
        conteo,
        false);
  }
}
