package co.edu.unal.rutaun.normativa.persistencia;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link ReglaEntity}. */
public interface ReglaRepository extends JpaRepository<ReglaEntity, UUID> {

  /** Las reglas que afectan a esa regla concreta: la modifican, la derogan o la exceptúan. */
  List<ReglaEntity> findByReglaAfectadaId(UUID reglaAfectadaId);
}
