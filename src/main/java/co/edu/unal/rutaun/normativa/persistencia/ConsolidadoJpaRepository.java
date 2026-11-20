package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ConsolidadoEntity}. */
public interface ConsolidadoJpaRepository extends JpaRepository<ConsolidadoEntity, UUID> {

  /** Busca el consolidado de un programa. */
  Optional<ConsolidadoEntity> findByProgramaId(UUID programaId);
}
