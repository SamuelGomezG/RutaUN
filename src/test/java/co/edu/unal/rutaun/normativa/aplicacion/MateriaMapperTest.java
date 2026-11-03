package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.Materia;
import co.edu.unal.rutaun.normativa.persistencia.MateriaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.MateriaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link Materia} (dominio, entidad, base, entidad, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate.
 */
@PruebaDeIntegracion
class MateriaMapperTest {

  @Autowired private MateriaJpaRepository materias;

  private String codigoGuardado;

  /** Borra lo que guardó la prueba, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    if (codigoGuardado != null) {
      materias.deleteById(codigoGuardado);
    }
  }

  @Test
  void tt04_materiaIdaYVuelta() {
    codigoGuardado = "T3-" + UUID.randomUUID().toString().substring(0, 8);
    Materia original = Materia.registrar(new CodigoMateria(codigoGuardado), new Creditos(4));

    materias.save(MateriaMapper.aEntidad(original));
    MateriaEntidad leida = materias.findById(codigoGuardado).orElseThrow();
    Materia reconstruida = MateriaMapper.aDominio(leida);

    assertThat(reconstruida).isEqualTo(original);
  }

  @Test
  void tt04_materiaConCreditosNegativosNoSeReconstituye() {
    MateriaEntidad invalida = new MateriaEntidad("T3-NEG", -1);

    assertThatThrownBy(() -> MateriaMapper.aDominio(invalida))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("negativo");
  }
}
