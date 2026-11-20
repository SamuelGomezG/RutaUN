package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link VersionEntity}. */
public interface VersionJpaRepository extends JpaRepository<VersionEntity, UUID> {

  /** Devuelve la versión vigente del consolidado de un programa. */
  Optional<VersionEntity> findTopByProgramaIdOrderByNumeroDesc(UUID programaId);

  /** Dice si el programa tiene al menos una versión del consolidado. */
  boolean existsByProgramaId(UUID programaId);
}
