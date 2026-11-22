package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repositorio de Spring Data de {@link VersionEntity}.
 *
 * <p>#143: es una raíz del historial, así que solo agrega y lee. No expone métodos de borrado.
 */
public interface VersionDelConsolidadoRepository extends Repository<VersionEntity, UUID> {

  /** Guarda la versión y, por cascada, sus filas de reglas. */
  <S extends VersionEntity> S save(S version);

  /** Devuelve la versión vigente del consolidado de un programa. */
  Optional<VersionEntity> findTopByProgramaIdOrderByNumeroDesc(UUID programaId);

  /** Dice si el programa tiene al menos una versión del consolidado. */
  boolean existsByProgramaId(UUID programaId);
}
