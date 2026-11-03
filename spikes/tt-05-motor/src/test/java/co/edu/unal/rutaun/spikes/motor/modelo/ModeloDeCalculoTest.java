package co.edu.unal.rutaun.spikes.motor.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.spikes.motor.malla.ModelosJson;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ModeloDeCalculoTest {

  @Test
  void tt05_fijarUnaObligatoriaNoDuplicaSusCreditos() {
    // Componente de 12 créditos con una obligatoria (3) y tres optativas (3 cada una): faltan 9.
    // En la malla real el recorte a cero de «faltan» esconde el doble conteo, por eso es sintética.
    List<ModelosJson.MateriaJson> materias =
        List.of(
            new ModelosJson.MateriaJson("OBL", "Obligatoria", 3, true, 1),
            new ModelosJson.MateriaJson("OP1", "Optativa 1", 3, false, 1),
            new ModelosJson.MateriaJson("OP2", "Optativa 2", 3, false, 1),
            new ModelosJson.MateriaJson("OP3", "Optativa 3", 3, false, 1));
    ModelosJson.MallaJson malla =
        new ModelosJson.MallaJson(
            new ModelosJson.FuenteJson("sintética", "sintética", "sintética", "sintética"),
            12,
            10,
            null,
            List.of(new ModelosJson.ComponenteJson("DISCIPLINAR", 12, 3, 9, materias, List.of())),
            new ModelosJson.LibreEleccionJson(0, 3, Map.of()),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    ModeloDeCalculo sinFijas = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());
    ModeloDeCalculo conFija =
        new ModeloDeCalculo(
            malla, new ModeloDeCalculo.Configuracion(List.of("OBL"), List.of(), List.of()));
    ConjuntoDeMaterias nada = new ConjuntoDeMaterias(sinFijas.cantidadMaterias());

    assertThat(sinFijas.faltan(0, nada, sinFijas.asignacionInicial())).isEqualTo(9);
    assertThat(conFija.faltan(0, nada, conFija.asignacionInicial())).isEqualTo(9);
  }

  @Test
  void tt05_unaMallaConMasDe128MateriasNoDesbordaLasMascaras() {
    int cantidad = 150;
    List<ModelosJson.MateriaJson> materias =
        IntStream.range(0, cantidad)
            .mapToObj(
                i ->
                    new ModelosJson.MateriaJson("S%03d".formatted(i), "Sintética " + i, 3, true, 1))
            .toList();
    ModelosJson.MallaJson malla =
        new ModelosJson.MallaJson(
            new ModelosJson.FuenteJson("sintética", "sintética", "sintética", "sintética"),
            3 * cantidad,
            10,
            null,
            List.of(
                new ModelosJson.ComponenteJson(
                    "DISCIPLINAR", 3 * cantidad, 3 * cantidad, 0, materias, List.of())),
            new ModelosJson.LibreEleccionJson(0, 3, Map.of()),
            List.of(
                new ModelosJson.PrerrequisitoJson(
                    "S149", ModelosJson.ExpresionJson.deCodigo("S140"))),
            List.of(),
            List.of(),
            List.of());

    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());

    assertThat(modelo.cantidadMaterias()).isEqualTo(cantidad);
    int destino = modelo.indiceDe("S149");
    int requisito = modelo.indiceDe("S140");
    ConjuntoDeMaterias avance = new ConjuntoDeMaterias(cantidad);
    assertThat(modelo.clausulasCumplidas(modelo.prerrequisitos(destino), avance)).isFalse();
    avance.agregar(requisito);
    assertThat(modelo.clausulasCumplidas(modelo.prerrequisitos(destino), avance)).isTrue();

    // Todas son obligatorias: con solo los índices 0 a 127 queda sin cubrir la del índice 128 o
    // más, y con las 150 la malla queda cubierta.
    int[] asignacion = modelo.asignacionInicial();
    ConjuntoDeMaterias primeras = new ConjuntoDeMaterias(cantidad);
    for (int i = 0; i < 128; i++) {
      primeras.agregar(i);
    }
    assertThat(modelo.todoCubierto(primeras, asignacion)).isFalse();
    ConjuntoDeMaterias todas = new ConjuntoDeMaterias(cantidad);
    for (int i = 0; i < cantidad; i++) {
      todas.agregar(i);
    }
    assertThat(modelo.todoCubierto(todas, asignacion)).isTrue();
  }

  @Test
  void tt05_unaObligatoriaEnDosAgrupacionesSumaEnUnaSola() {
    // SH es obligatoria y aparece en A y en B (6 créditos cada una, con una optativa propia).
    // Debe contar solo en A, la primera: antes contaba en las dos y a B le faltaban 3 en vez de 6.
    ModelosJson.MateriaJson compartida =
        new ModelosJson.MateriaJson("SH", "Compartida", 3, true, 1);
    ModelosJson.AgrupacionJson a =
        new ModelosJson.AgrupacionJson(
            "A",
            "Agrupación A",
            6,
            List.of(compartida, new ModelosJson.MateriaJson("OA", "Optativa A", 3, false, 1)),
            List.of());
    ModelosJson.AgrupacionJson b =
        new ModelosJson.AgrupacionJson(
            "B",
            "Agrupación B",
            6,
            List.of(compartida, new ModelosJson.MateriaJson("OB", "Optativa B", 3, false, 1)),
            List.of());
    ModelosJson.MallaJson malla =
        new ModelosJson.MallaJson(
            new ModelosJson.FuenteJson("sintética", "sintética", "sintética", "sintética"),
            12,
            10,
            null,
            List.of(
                new ModelosJson.ComponenteJson("DISCIPLINAR", 12, 3, 9, List.of(), List.of(a, b))),
            new ModelosJson.LibreEleccionJson(0, 3, Map.of()),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    ModeloDeCalculo modelo = new ModeloDeCalculo(malla, ModeloDeCalculo.Configuracion.vacia());

    int grupoA = indiceDeGrupo(modelo, "A");
    int grupoB = indiceDeGrupo(modelo, "B");
    ConjuntoDeMaterias nada = new ConjuntoDeMaterias(modelo.cantidadMaterias());
    int[] asignacion = modelo.asignacionInicial();
    assertThat(asignacion[modelo.indiceDe("SH")]).isEqualTo(grupoA);
    assertThat(modelo.faltan(grupoA, nada, asignacion)).isEqualTo(3);
    assertThat(modelo.faltan(grupoB, nada, asignacion)).isEqualTo(6);

    ConjuntoDeMaterias conCompartida =
        ConjuntoDeMaterias.de(modelo.cantidadMaterias(), modelo.indiceDe("SH"));
    assertThat(modelo.creditosGrupo(grupoA, conCompartida, asignacion)).isEqualTo(3);
    assertThat(modelo.creditosGrupo(grupoB, conCompartida, asignacion)).isZero();
  }

  private static int indiceDeGrupo(ModeloDeCalculo modelo, String id) {
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      if (id.equals(grupo.id())) {
        return grupo.indice();
      }
    }
    throw new IllegalArgumentException("Grupo desconocido: " + id);
  }
}
