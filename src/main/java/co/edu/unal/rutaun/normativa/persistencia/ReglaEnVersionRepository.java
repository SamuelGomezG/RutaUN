package co.edu.unal.rutaun.normativa.persistencia;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ReglaEnVersionEntity}. */
public interface ReglaEnVersionRepository extends JpaRepository<ReglaEnVersionEntity, UUID> {

  /** Las filas de las reglas de una versión, cada una con su estado en ella. Sin orden. */
  List<ReglaEnVersionEntity> findByVersionId(UUID versionId);
}
