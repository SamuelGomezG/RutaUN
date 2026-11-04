package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.Correquisito;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.MateriaEnMalla;
import co.edu.unal.rutaun.normativa.dominio.MedidaDeCreditos;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaDeHabilitacion;
import co.edu.unal.rutaun.normativa.dominio.TipoDeCargaPorPeriodo;
import co.edu.unal.rutaun.normativa.dominio.TipoDeMateriaExterna;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Reglas y normas sintéticas para las pruebas de los mappers (TT-04-T3, #91). */
final class ReglasDePrueba {

  private static final CodigoMateria A = new CodigoMateria("9000001");
  private static final CodigoMateria B = new CodigoMateria("9000006");

  private ReglasDePrueba() {}

  /** Una norma con identificación única, para no chocar con otras pruebas. */
  static Norma normaNueva() {
    return Norma.registrar(
        new IdentificacionNorma(
            TipoNorma.ACUERDO,
            "T3-" + UUID.randomUUID().toString().substring(0, 8),
            2026,
            "Consejo Superior Universitario"),
        TipoDeEfecto.BASE,
        LocalDate.of(2026, 1, 15),
        new Vigencia(new Momento.EnFecha(LocalDate.of(2026, 2, 1)), Optional.empty()),
        Optional.empty(),
        new Alcance(
            TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), "Aplica a todos los programas."));
  }

  /** Una regla del plan, sin vigencia propia ni afectación, de la norma dada. */
  static Regla regla(Norma norma, DatosDeRegla datos, EstadoRegla estado) {
    return Regla.registrar(
        "Objeto de prueba",
        "Enunciado de prueba",
        new Fuente(norma.id(), "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        estado,
        Optional.empty(),
        datos);
  }

  /** Datos de las once categorías, más los del componente de libre elección y uno anidado. */
  static List<DatosDeRegla> datosDeTodasLasCategorias() {
    Malla malla = MallaDePrueba.pequena();
    NodoDeMalla fundamentacion = MallaDePrueba.nodo(malla, "Fundamentación");
    NodoDeMalla cienciasBasicas = MallaDePrueba.nodo(malla, "Ciencias básicas");
    NodoDeMalla computo = MallaDePrueba.nodo(malla, "Cómputo");
    NodoDeMalla libreEleccion = MallaDePrueba.nodo(malla, "Libre elección");
    MateriaEnMalla materia = malla.materias().get(0);
    ExpresionDeMaterias compuesta =
        new ExpresionDeMaterias.Y(
            new ExpresionDeMaterias.MateriaDelPrograma(A),
            new ExpresionDeMaterias.O(
                new ExpresionDeMaterias.MateriaDelPrograma(B),
                new ExpresionDeMaterias.MateriaExterna(
                    "Matemáticas Básicas",
                    Optional.of(new CodigoMateria("1000001")),
                    TipoDeMateriaExterna.OTRO_PROGRAMA)));
    return List.of(
        new DatosDeRegla.DeMateria(materia, fundamentacion),
        new DatosDeRegla.DePrerrequisito(new Prerrequisito(B, compuesta)),
        new DatosDeRegla.DeCorrequisito(
            new Correquisito(
                B,
                new ExpresionDeMaterias.MateriaExterna(
                    "Nivelación de lectoescritura",
                    Optional.empty(),
                    TipoDeMateriaExterna.NIVELACION))),
        new DatosDeRegla.DeHabilitacion(
            new ReglaDeHabilitacion(
                B, MedidaDeCreditos.deComponente("Fundamentación"), new Creditos(40))),
        new DatosDeRegla.DeAgrupacion((Agrupacion) cienciasBasicas, fundamentacion),
        new DatosDeRegla.DeComponente(fundamentacion),
        new DatosDeRegla.DePrograma(new Creditos(160)),
        new DatosDeRegla.DeEquivalencia(Set.of(A, B), new CodigoMateria("9000010")),
        new DatosDeRegla.DeCargaPorPeriodo(new Creditos(10), TipoDeCargaPorPeriodo.MINIMA),
        new DatosDeRegla.DeRequisitoDeGrado(),
        new DatosDeRegla.DeOtraCategoria(),
        new DatosDeRegla.DeComponente(libreEleccion),
        new DatosDeRegla.DeAgrupacion((Agrupacion) computo, cienciasBasicas));
  }
}
