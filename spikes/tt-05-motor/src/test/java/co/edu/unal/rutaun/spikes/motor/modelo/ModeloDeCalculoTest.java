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
  }
}
