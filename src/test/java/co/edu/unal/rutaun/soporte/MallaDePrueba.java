package co.edu.unal.rutaun.soporte;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.Agrupacion;
import co.edu.unal.rutaun.normativa.dominio.Componente;
import co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion;
import co.edu.unal.rutaun.normativa.dominio.ExigenciaCreditos;
import co.edu.unal.rutaun.normativa.dominio.Malla;
import co.edu.unal.rutaun.normativa.dominio.MateriaEnMalla;
import co.edu.unal.rutaun.normativa.dominio.NodoDeMalla;
import co.edu.unal.rutaun.normativa.dominio.Obligatoriedad;
import co.edu.unal.rutaun.normativa.dominio.TipoComponente;
import java.util.List;

/**
 * Malla sintética y pequeña para probar sin persistencia. Nada de esto es normativa real: los
 * nombres, los códigos y los créditos son inventados.
 *
 * <pre>
 * Fundamentación (10 = 4 obligatorios + 6 optativos)
 *   Cálculo diferencial (obligatoria, 4)
 *   Ciencias básicas (6): Física I (3), Química básica (3)
 *     Cómputo (3): Programación I (3), Programación II (3)
 * Disciplinar (9 = 6 obligatorios + 3 optativos)
 *   Estructuras de datos (obligatoria, 3), Algoritmos (obligatoria, 3)
 *   Profundización (3): Redes (3), Bases de datos (3)
 * Libre elección (6)
 *   Electivas sugeridas (0): Cátedra de inducción (2)
 * </pre>
 */
public final class MallaDePrueba {

  private MallaDePrueba() {}

  /** Construye la malla de prueba. */
  public static Malla pequena() {
    Agrupacion computo =
        agrupacion(
            "Cómputo",
            ExigenciaCreditos.de(new Creditos(3)),
            List.of(
                optativa("9000004", "Programación I", 3, 2),
                optativa("9000005", "Programación II", 3, 3)),
            List.of());
    Agrupacion cienciasBasicas =
        agrupacion(
            "Ciencias básicas",
            ExigenciaCreditos.de(new Creditos(6)),
            List.of(
                optativa("9000002", "Física I", 3, 1), optativa("9000003", "Química básica", 3, 2)),
            List.of(computo));
    Componente fundamentacion =
        new Componente(
            "Fundamentación",
            TipoComponente.FUNDAMENTACION,
            ExigenciaCreditos.conDesglose(new Creditos(4), new Creditos(6)),
            List.of(
                MateriaEnMalla.enSemestre(
                    new CodigoMateria("9000001"),
                    "Cálculo diferencial",
                    new Creditos(4),
                    Obligatoriedad.OBLIGATORIA,
                    1)),
            List.of(cienciasBasicas));

    Agrupacion profundizacion =
        agrupacion(
            "Profundización",
            ExigenciaCreditos.de(new Creditos(3)),
            List.of(
                optativa("9000008", "Redes", 3, 5), optativa("9000009", "Bases de datos", 3, 5)),
            List.of());
    Componente disciplinar =
        new Componente(
            "Disciplinar",
            TipoComponente.DISCIPLINAR,
            ExigenciaCreditos.conDesglose(new Creditos(6), new Creditos(3)),
            List.of(
                MateriaEnMalla.enSemestre(
                    new CodigoMateria("9000006"),
                    "Estructuras de datos",
                    new Creditos(3),
                    Obligatoriedad.OBLIGATORIA,
                    3),
                MateriaEnMalla.enSemestre(
                    new CodigoMateria("9000007"),
                    "Algoritmos",
                    new Creditos(3),
                    Obligatoriedad.OBLIGATORIA,
                    4)),
            List.of(profundizacion));

    Agrupacion electivas =
        agrupacion(
            "Electivas sugeridas",
            ExigenciaCreditos.de(new Creditos(0)),
            List.of(
                MateriaEnMalla.sinSemestre(
                    new CodigoMateria("9000010"),
                    "Cátedra de inducción",
                    new Creditos(2),
                    Obligatoriedad.OPTATIVA)),
            List.of());
    ComponenteDeLibreEleccion libreEleccion =
        new ComponenteDeLibreEleccion(
            "Libre elección", ExigenciaCreditos.de(new Creditos(6)), List.of(electivas));

    return new Malla(List.of(fundamentacion, disciplinar, libreEleccion));
  }

  /** Busca un nodo de la malla por su nombre. */
  public static NodoDeMalla nodo(Malla malla, String nombre) {
    return malla
        .recorrer()
        .filter(nodo -> nodo.nombre().equals(nombre))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("la malla de prueba no tiene " + nombre));
  }

  private static Agrupacion agrupacion(
      String nombre,
      ExigenciaCreditos exigencia,
      List<MateriaEnMalla> materias,
      List<Agrupacion> subagrupaciones) {
    return new Agrupacion(AgrupacionId.nuevo(), nombre, exigencia, materias, subagrupaciones);
  }

  private static MateriaEnMalla optativa(String codigo, String nombre, int creditos, int semestre) {
    return MateriaEnMalla.enSemestre(
        new CodigoMateria(codigo),
        nombre,
        new Creditos(creditos),
        Obligatoriedad.OPTATIVA,
        semestre);
  }
}
