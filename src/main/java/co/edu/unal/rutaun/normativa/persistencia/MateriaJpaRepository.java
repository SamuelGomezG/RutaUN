package co.edu.unal.rutaun.normativa.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link MateriaEntity}. */
public interface MateriaJpaRepository extends JpaRepository<MateriaEntity, String> {}
