package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ConsolidadoEntidad}. */
public interface ConsolidadoJpaRepository extends JpaRepository<ConsolidadoEntidad, UUID> {

  /** Busca el consolidado de un programa. */
  Optional<ConsolidadoEntidad> findByProgramaId(UUID programaId);
}
