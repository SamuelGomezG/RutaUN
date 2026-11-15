package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link VersionEntidad}. */
public interface VersionJpaRepository extends JpaRepository<VersionEntidad, UUID> {

  /** Devuelve la versión vigente del consolidado de un programa. */
  Optional<VersionEntidad> findTopByProgramaIdOrderByNumeroDesc(UUID programaId);

  /** Dice si el programa tiene al menos una versión del consolidado. */
  boolean existsByProgramaId(UUID programaId);
}
