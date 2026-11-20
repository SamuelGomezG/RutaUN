package co.edu.unal.rutaun.normativa.persistencia;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ReglaEntity}. */
public interface ReglaJpaRepository extends JpaRepository<ReglaEntity, UUID> {}
