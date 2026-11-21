package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.compartido.EstadoRestriccion.Certeza;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.MateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias.TerminoMateria;
import co.edu.unal.rutaun.soporte.ContextoDeEvaluacionFijo;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;

/** Prueba temporal de TT-03-T10 (#164): el evaluador y cada restricción dan lo mismo. */
class EvaluacionEquivalentePropiedadesTest {

  private static final List<CodigoMateria> CODIGOS =
      List.of(
          new CodigoMateria("9000001"),
          new CodigoMateria("9000002"),
          new CodigoMateria("9000003"),
          new CodigoMateria("9000004"),
          new CodigoMateria("9000005"),
          new CodigoMateria("9000006"));

  @Property(tries = 10000, generation = GenerationMode.RANDOMIZED)
  void tt03_evaluarEnLaRestriccionDaLoMismoQueElEvaluador(@ForAll long semilla) {
    Random azar = new Random(semilla);
    Restriccion restriccion = restriccion(azar);
    ContextoDeEvaluacion contexto = contexto(azar);

    assertThat(restriccion.evaluar(contexto))
        .isEqualTo(EvaluadorDeRestricciones.evaluar(restriccion, contexto));
  }

  private static Restriccion restriccion(Random azar) {
    CodigoMateria materia = CODIGOS.get(azar.nextInt(CODIGOS.size()));
    return switch (azar.nextInt(3)) {
      case 0 -> new Prerrequisito(materia, expresion(azar, azar.nextInt(4)));
      case 1 -> new Correquisito(materia, expresion(azar, azar.nextInt(4)));
      default -> new ReglaDeHabilitacion(materia, medida(azar), new Creditos(azar.nextInt(81)));
    };
  }

  private static MedidaDeCreditos medida(Random azar) {
    if (azar.nextBoolean()) {
      return MedidaDeCreditos.delPrograma();
    }
    TipoComponente[] componentes = TipoComponente.values();
    return MedidaDeCreditos.deComponente(componentes[azar.nextInt(componentes.length)]);
  }

  private static ContextoDeEvaluacion contexto(Random azar) {
    ContextoDeEvaluacionFijo.Constructor constructor = ContextoDeEvaluacionFijo.nuevo();
    for (CodigoMateria codigo : CODIGOS) {
      Certeza certeza = Certeza.values()[azar.nextInt(Certeza.values().length)];
      constructor.estado(
          codigo, new EstadoRestriccion(certeza, List.of(codigo.valor() + " " + certeza)));
      if (azar.nextBoolean()) {
        constructor.enElMismoSemestre(codigo);
      }
    }
    constructor.creditosDelPrograma(azar.nextInt(81));
    for (TipoComponente componente : TipoComponente.values()) {
      constructor.creditosDelComponente(componente, azar.nextInt(81));
    }
    return constructor.construir();
  }

  private static ExpresionDeMaterias expresion(Random azar, int nivel) {
    if (nivel <= 0 || azar.nextInt(3) == 0) {
      return termino(azar, 0);
    }
    boolean conjuncion = azar.nextBoolean();
    int cuantos = 2 + azar.nextInt(3);
    Set<ExpresionDeMaterias> terminos = new LinkedHashSet<>();
    int intentos = 0;
    while (terminos.size() < cuantos && intentos < 50) {
      terminos.add(termino(azar, nivel - 1));
      intentos++;
    }
    if (terminos.size() < 2) {
      terminos.add(termino(azar, 0));
    }
    ExpresionDeMaterias[] arreglo = terminos.toArray(ExpresionDeMaterias[]::new);
    return conjuncion
        ? ExpresionDeMaterias.todasLas(arreglo)
        : ExpresionDeMaterias.algunaDe(arreglo);
  }

  private static ExpresionDeMaterias termino(Random azar, int nivel) {
    if (nivel > 0 && azar.nextBoolean()) {
      return expresion(azar, nivel - 1);
    }
    int elegido = azar.nextInt(CODIGOS.size() + 2);
    if (elegido < CODIGOS.size()) {
      return new TerminoMateria(CODIGOS.get(elegido));
    }
    boolean nivelacion = elegido == CODIGOS.size();
    return new MateriaExterna(
        nivelacion ? "Externa de nivelación" : "Externa de otro programa",
        Optional.empty(),
        nivelacion ? TipoMateriaExterna.NIVELACION : TipoMateriaExterna.OTRO_PROGRAMA);
  }
}
