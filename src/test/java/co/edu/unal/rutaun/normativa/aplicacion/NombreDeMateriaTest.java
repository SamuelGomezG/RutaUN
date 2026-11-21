package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.aplicacion.NombreDeMateria.Candidato;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * UC05 paso 4: el nombre con que se muestra una materia no depende del orden en que llegan las
 * reglas; se prueba con todos los órdenes posibles.
 */
class NombreDeMateriaTest {

  private static final CodigoMateria MATERIA = new CodigoMateria("2016699");
  private static final CodigoMateria OTRA = new CodigoMateria("1000004");
  private static final LocalDate HOY = LocalDate.of(2023, 12, 12);
  private static final LocalDate FUTURO = LocalDate.of(2099, 1, 1);

  private static Candidato candidato(
      CodigoMateria materia, String nombre, EstadoRegla estado, LocalDate expedicion, int regla) {
    return new Candidato(materia, nombre, estado, expedicion, new UUID(0L, regla));
  }

  /** Todos los órdenes en que pueden llegar los candidatos. */
  private static List<List<Candidato>> ordenes(List<Candidato> candidatos) {
    if (candidatos.size() <= 1) {
      return List.of(candidatos);
    }
    List<List<Candidato>> resultado = new ArrayList<>();
    for (int i = 0; i < candidatos.size(); i++) {
      List<Candidato> resto = new ArrayList<>(candidatos);
      Candidato primero = resto.remove(i);
      for (List<Candidato> orden : ordenes(resto)) {
        List<Candidato> completo = new ArrayList<>();
        completo.add(primero);
        completo.addAll(orden);
        resultado.add(completo);
      }
    }
    return resultado;
  }

  private static void elNombreEs(String esperado, List<Candidato> candidatos) {
    for (List<Candidato> orden : ordenes(candidatos)) {
      assertThat(NombreDeMateria.elegir(orden)).containsEntry(MATERIA, esperado);
    }
  }

  @Test
  void uc05_paso4_laReglaVigenteGanaALaProgramadaEnCualquierOrden() {
    elNombreEs(
        "Nombre que rige",
        List.of(
            candidato(MATERIA, "Nombre que rige", EstadoRegla.VIGENTE, HOY, 1),
            candidato(MATERIA, "Nombre futuro", EstadoRegla.PROGRAMADA, FUTURO, 2),
            candidato(MATERIA, "Nombre en revisión", EstadoRegla.EN_REVISION, FUTURO, 3)));
  }

  @Test
  void uc05_paso4_siNingunaRigeGanaLaProgramadaYLuegoLaQueEstaEnRevision() {
    elNombreEs(
        "Nombre futuro",
        List.of(
            candidato(MATERIA, "Nombre futuro", EstadoRegla.PROGRAMADA, FUTURO, 2),
            candidato(MATERIA, "Nombre en revisión", EstadoRegla.EN_REVISION, FUTURO, 3)));
  }

  @Test
  void uc05_paso4_conElMismoEstadoGanaLaNormaExpedidaMasRecientemente() {
    elNombreEs(
        "Nombre nuevo",
        List.of(
            candidato(MATERIA, "Nombre viejo", EstadoRegla.VIGENTE, LocalDate.of(2008, 1, 1), 1),
            candidato(MATERIA, "Nombre nuevo", EstadoRegla.VIGENTE, LocalDate.of(2023, 12, 12), 2),
            candidato(MATERIA, "Sin fecha", EstadoRegla.VIGENTE, null, 3)));
  }

  @Test
  void uc05_paso4_conTodoIgualGanaElIdentificadorMasBajo() {
    elNombreEs(
        "Primero",
        List.of(
            candidato(MATERIA, "Segundo", EstadoRegla.VIGENTE, HOY, 2),
            candidato(MATERIA, "Primero", EstadoRegla.VIGENTE, HOY, 1)));
  }

  @Test
  void uc05_paso4_cadaMateriaTieneSuPropioNombre() {
    var nombres =
        NombreDeMateria.elegir(
            List.of(
                candidato(MATERIA, "Una", EstadoRegla.VIGENTE, HOY, 1),
                candidato(OTRA, "Otra", EstadoRegla.PROGRAMADA, FUTURO, 2)));

    assertThat(nombres).containsEntry(MATERIA, "Una").containsEntry(OTRA, "Otra");
  }

  @Test
  void uc05_paso4_unaMateriaSinCandidatosNoTieneNombre() {
    assertThat(NombreDeMateria.elegir(List.of())).isEmpty();
  }
}
