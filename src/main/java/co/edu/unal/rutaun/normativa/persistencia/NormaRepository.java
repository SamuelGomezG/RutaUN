package co.edu.unal.rutaun.normativa.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de Spring Data de {@link NormaEntity}.
 *
 * <p>#143: es una raíz del historial, así que solo agrega y lee. No expone métodos de borrado.
 */
public interface NormaRepository extends Repository<NormaEntity, UUID> {

  /** Guarda la norma y, por cascada, sus reglas. */
  <S extends NormaEntity> S save(S norma);

  /** Busca una norma por su identificación única. */
  Optional<NormaEntity> findByTipoAndNumeroAndAnioAndOrgano(
      String tipo, String numero, int anio, String organo);

  /**
   * Dice si alguna norma contiene esa regla.
   *
   * <p>UC07 ext. 6b (#143): reemplaza la consulta que hacía el repositorio de reglas, que ya no
   * existe.
   */
  boolean existsByReglasId(UUID reglaId);

  /**
   * Las normas que contienen alguna de esas reglas, cada una con todas sus reglas.
   *
   * <p>#143. La subconsulta elige las normas por regla; la colección se carga sin filtro para que
   * ninguna norma quede incompleta en memoria.
   */
  @Query("select distinct n from NormaEntity n join n.reglas r where r.id in :reglas")
  List<NormaEntity> findDeLasReglas(@Param("reglas") Collection<UUID> reglas);

  /**
   * Las normas que contienen una regla que afecta a la regla dada, cada una con todas sus reglas.
   *
   * <p>#143. Quien la usa filtra después las reglas de cada norma por {@code reglaAfectadaId}.
   */
  @Query(
      "select distinct n from NormaEntity n join n.reglas r"
          + " where r.reglaAfectadaId = :reglaAfectada")
  List<NormaEntity> findDeLasReglasQueAfectanA(@Param("reglaAfectada") UUID reglaAfectada);
}
