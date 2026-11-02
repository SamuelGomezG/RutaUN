package co.edu.unal.rutaun.incidentes.persistencia;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de Spring Data de {@link IncidenteEntidad}. */
public interface IncidenteJpaRepository extends JpaRepository<IncidenteEntidad, UUID> {

  /**
   * Busca los incidentes sin atender con el mismo caso de uso, paso y causa. Quien llama termina de
   * comparar programa, versión y elementos.
   *
   * @param casoDeUso caso de uso del incidente
   * @param paso paso o extensión del incidente
   * @param causa causa del incidente
   * @return los candidatos a ser el mismo incidente
   */
  List<IncidenteEntidad> findByCasoDeUsoAndPasoAndCausaAndAtendidoFalse(
      String casoDeUso, String paso, String causa);
}
