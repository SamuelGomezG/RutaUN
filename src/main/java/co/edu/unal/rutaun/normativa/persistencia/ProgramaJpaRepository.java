package co.edu.unal.rutaun.normativa.persistencia;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ProgramaEntidad}. */
public interface ProgramaJpaRepository extends JpaRepository<ProgramaEntidad, UUID> {}
