package co.edu.unal.rutaun.normativa.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link MateriaEntidad}. */
public interface MateriaJpaRepository extends JpaRepository<MateriaEntidad, String> {}
