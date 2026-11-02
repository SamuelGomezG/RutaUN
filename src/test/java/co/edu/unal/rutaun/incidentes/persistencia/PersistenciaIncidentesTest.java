package co.edu.unal.rutaun.incidentes.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@PruebaDeIntegracion
@Transactional
class PersistenciaIncidentesTest {

  @Autowired private IncidenteJpaRepository incidentes;
  @Autowired private EntityManager em;

  @Test
  void sop05_guardaYLeeUnIncidente() {
    LocalDateTime fecha = LocalDateTime.of(2026, 11, 1, 10, 0);
    IncidenteEntidad incidente =
        incidentes.save(
            new IncidenteEntidad(
                UUID.randomUUID(),
                "UC01",
                "paso 5",
                null,
                null,
                "El calculo supero el plazo",
                "[]",
                fecha,
                fecha,
                1,
                false));
    em.flush();
    em.clear();

    assertThat(incidentes.findById(incidente.getId()))
        .get()
        .extracting(IncidenteEntidad::getConteo)
        .isEqualTo(1);
  }
}
