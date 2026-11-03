package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;

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
      case ExpresionDeMaterias.MateriaDelPrograma materia ->
          evaluarMateria(materia.codigo(), contexto, esCorrequisito);
      case ExpresionDeMaterias.MateriaExterna externa -> evaluarMateriaExterna(externa);
      case ExpresionDeMaterias.Y conjuncion ->
          combinarY(
              evaluarExpresion(conjuncion.izquierda(), contexto, esCorrequisito),
              evaluarExpresion(conjuncion.derecha(), contexto, esCorrequisito));
      case ExpresionDeMaterias.O disyuncion ->
          combinarO(
              evaluarExpresion(disyuncion.izquierda(), contexto, esCorrequisito),
              evaluarExpresion(disyuncion.derecha(), contexto, esCorrequisito));
    };
  }

  private static EstadoRestriccion evaluarMateria(
      CodigoMateria materia, ContextoDeEvaluacion contexto, boolean esCorrequisito) {
    EstadoDeMateria estado = contexto.estadoDe(materia);
    return switch (estado) {
      case APROBADA -> EstadoRestriccion.cumplida(materia.valor() + " está aprobada");
      case EN_CURSO -> EstadoRestriccion.enCurso(materia.valor() + " está en curso");
      case YA_NO_RIGE -> EstadoRestriccion.incierta(materia.valor() + " ya no rige");
      case PENDIENTE -> {
        if (esCorrequisito && contexto.vaEnElMismoSemestre(materia)) {
          yield EstadoRestriccion.cumplida(
              "el plan ubica " + materia.valor() + " en el mismo semestre o antes");
        }
        yield EstadoRestriccion.pendiente(materia.valor() + " está pendiente");
      }
    };
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
    Creditos aprobados = contexto.creditosAprobadosPara(habilitacion.medida());
    String medida =
        habilitacion.medida().esDelPrograma()
            ? "del programa"
            : "del componente " + habilitacion.medida().componente().orElseThrow();
    String detalle =
        "créditos aprobados "
            + medida
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
