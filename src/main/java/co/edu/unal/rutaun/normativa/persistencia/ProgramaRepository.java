package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de {@link ProgramaEntity}. */
public interface ProgramaRepository extends JpaRepository<ProgramaEntity, UUID> {

  /**
   * Busca un programa por su código SIA.
   *
   * <p>SOP-03 (#55). El código es único por `uq_programa_codigo_sia`; quien guarda programas busca
   * primero y omite los que ya existen, así que repetir la carga no choca con la restricción.
   */
  Optional<ProgramaEntity> findByCodigoSia(String codigoSia);
}
