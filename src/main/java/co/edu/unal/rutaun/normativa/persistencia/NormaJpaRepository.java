package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link NormaEntidad}. */
public interface NormaJpaRepository extends JpaRepository<NormaEntidad, UUID> {

  /** Busca una norma por su identificación única. */
  Optional<NormaEntidad> findByTipoAndNumeroAndAnioAndOrgano(
      String tipo, String numero, int anio, String organo);
}
