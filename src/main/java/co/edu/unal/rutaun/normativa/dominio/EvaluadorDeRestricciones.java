package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Evalúa una restricción contra un {@link ContextoDeEvaluacion} y combina sus términos por certeza
 * (S18): cumplida &gt; en curso &gt; incierta &gt; pendiente. Un «y» toma el peor estado de sus
 * términos y una «o», el mejor.
 */
public final class EvaluadorDeRestricciones {

  private EvaluadorDeRestricciones() {}

  /** Evalúa la restricción y devuelve su estado con las razones que lo explican. */
  public static EstadoRestriccion evaluar(Restriccion restriccion, ContextoDeEvaluacion contexto) {
    ArgumentChecks.obligatorio(restriccion, "restriccion");
    ArgumentChecks.obligatorio(contexto, "contexto");
    return switch (restriccion) {
      case Prerrequisito prerrequisito ->
          evaluarExpresion(prerrequisito.expresion(), contexto, false);
      case Correquisito correquisito -> evaluarExpresion(correquisito.expresion(), contexto, true);
      case ReglaDeHabilitacion habilitacion -> evaluarHabilitacion(habilitacion, contexto);
    };
  }

  /** Combina dos estados con «y»: toma el peor y las razones de los términos no cumplidos. */
  static EstadoRestriccion combinarY(EstadoRestriccion izquierda, EstadoRestriccion derecha) {
    ArgumentChecks.obligatorio(izquierda, "izquierda");
    ArgumentChecks.obligatorio(derecha, "derecha");
    List<String> razones = new ArrayList<>();
    if (izquierda.certeza() != EstadoRestriccion.Certeza.CUMPLIDA) {
      razones.addAll(izquierda.razones());
    }
    if (derecha.certeza() != EstadoRestriccion.Certeza.CUMPLIDA) {
      razones.addAll(derecha.razones());
    }
    if (razones.isEmpty()) {
      razones.addAll(izquierda.razones());
      razones.addAll(derecha.razones());
    }
    return new EstadoRestriccion(peor(izquierda.certeza(), derecha.certeza()), razones);
  }

  /** Combina dos estados con «o»: toma el mejor y las razones de la mejor rama. */
  static EstadoRestriccion combinarO(EstadoRestriccion izquierda, EstadoRestriccion derecha) {
    ArgumentChecks.obligatorio(izquierda, "izquierda");
    ArgumentChecks.obligatorio(derecha, "derecha");
    EstadoRestriccion.Certeza mejor = mejor(izquierda.certeza(), derecha.certeza());
    List<String> razones = new ArrayList<>();
    if (izquierda.certeza() == mejor) {
      razones.addAll(izquierda.razones());
    }
    if (derecha.certeza() == mejor) {
      razones.addAll(derecha.razones());
    }
    return new EstadoRestriccion(mejor, razones);
  }

  private static EstadoRestriccion evaluarExpresion(
      ExpresionDeMaterias expresion, ContextoDeEvaluacion contexto, boolean esCorrequisito) {
    return switch (expresion) {
      case ExpresionDeMaterias.TerminoMateria materia ->
          evaluarMateria(materia.codigo(), contexto, esCorrequisito);
      case ExpresionDeMaterias.MateriaExterna externa -> evaluarMateriaExterna(externa);
      case ExpresionDeMaterias.TodasLas todas ->
          combinar(todas.terminos(), contexto, esCorrequisito, EvaluadorDeRestricciones::combinarY);
      case ExpresionDeMaterias.AlgunaDe alguna ->
          combinar(
              alguna.terminos(), contexto, esCorrequisito, EvaluadorDeRestricciones::combinarO);
    };
  }

  private static EstadoRestriccion combinar(
      Set<ExpresionDeMaterias> terminos,
      ContextoDeEvaluacion contexto,
      boolean esCorrequisito,
      BinaryOperator<EstadoRestriccion> combinador) {
    Iterator<ExpresionDeMaterias> pendientes = terminos.iterator();
    EstadoRestriccion acumulado = evaluarExpresion(pendientes.next(), contexto, esCorrequisito);
    while (pendientes.hasNext()) {
      acumulado =
          combinador.apply(
              acumulado, evaluarExpresion(pendientes.next(), contexto, esCorrequisito));
    }
    return acumulado;
  }

  private static EstadoRestriccion evaluarMateria(
      CodigoMateria materia, ContextoDeEvaluacion contexto, boolean esCorrequisito) {
    EstadoRestriccion estado = contexto.estadoDe(materia);
    if (esCorrequisito
        && estado.certeza() == EstadoRestriccion.Certeza.PENDIENTE
        && contexto.enElMismoSemestre(materia)) {
      return EstadoRestriccion.cumplida(
          "el plan ubica " + materia.valor() + " en el mismo semestre o antes");
    }
    return estado;
  }

  private static EstadoRestriccion evaluarMateriaExterna(
      ExpresionDeMaterias.MateriaExterna externa) {
    return switch (externa.tipo()) {
      case NIVELACION ->
          EstadoRestriccion.cumplida(
              "la materia de nivelación " + externa.nombre() + " se da por cumplida");
      case OTRO_PROGRAMA ->
          EstadoRestriccion.pendiente(
              "la materia de otro programa "
                  + externa.nombre()
                  + " no se puede cursar desde este programa");
    };
  }

  private static EstadoRestriccion evaluarHabilitacion(
      ReglaDeHabilitacion habilitacion, ContextoDeEvaluacion contexto) {
    MedidaDeCreditos medida = habilitacion.medida();
    Creditos aprobados =
        medida.esDelPrograma()
            ? contexto.creditosAprobadosDelPrograma()
            : contexto.creditosAprobadosDelComponente(medida.componente().orElseThrow());
    String deQue =
        medida.esDelPrograma()
            ? "del programa"
            : "del componente " + medida.componente().orElseThrow();
    String detalle =
        "créditos aprobados "
            + deQue
            + ": "
            + aprobados.valor()
            + "; umbral: "
            + habilitacion.umbral().valor();
    if (aprobados.valor() >= habilitacion.umbral().valor()) {
      return EstadoRestriccion.cumplida(detalle);
    }
    return EstadoRestriccion.pendiente(detalle);
  }

  private static EstadoRestriccion.Certeza peor(
      EstadoRestriccion.Certeza izquierda, EstadoRestriccion.Certeza derecha) {
    return izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
  }

  private static EstadoRestriccion.Certeza mejor(
      EstadoRestriccion.Certeza izquierda, EstadoRestriccion.Certeza derecha) {
    return izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;
  }
}
