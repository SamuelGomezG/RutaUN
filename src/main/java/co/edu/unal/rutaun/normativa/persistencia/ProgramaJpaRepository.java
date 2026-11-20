package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de {@link ProgramaEntity}. */
public interface ProgramaJpaRepository extends JpaRepository<ProgramaEntity, UUID> {

  /**
   * Busca un programa por su código SIA.
   *
   * <p>SOP-03 (#55). La tabla no garantiza que el código sea único, así que quien guarda programas
   * debe buscar primero. Si hubiera dos filas con el mismo código, falla con una excepción.
   */
  Optional<ProgramaEntity> findByCodigoSia(String codigoSia);
}
