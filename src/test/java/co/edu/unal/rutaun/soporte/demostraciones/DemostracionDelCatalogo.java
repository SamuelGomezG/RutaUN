package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.Materia;
import java.util.HashSet;
import java.util.Set;

/**
 * Demostración ejecutable de TT-02-T2 (#148): una materia se identifica solo por su código. Crea
 * dos materias con el mismo código y créditos distintos y muestra que son la misma, con el mismo
 * hash, y que un conjunto guarda una sola. No necesita Spring ni base de datos; se ejecuta con
 * {@code ./mvnw -q test-compile && java -cp target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDelCatalogo}.
 */
public final class DemostracionDelCatalogo {

  private DemostracionDelCatalogo() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    CodigoMateria codigo = new CodigoMateria("2016699");
    Materia conTres = Materia.registrar(codigo, new Creditos(3));
    Materia conCuatro = Materia.registrar(codigo, new Creditos(4));

    System.out.println("Materia registrada: " + conTres);
    System.out.println("Misma materia con otros créditos: " + conCuatro);
    System.out.println("¿Son la misma materia? " + conTres.equals(conCuatro));
    System.out.println("¿Tienen el mismo hash? " + (conTres.hashCode() == conCuatro.hashCode()));

    Set<Materia> conjunto = new HashSet<>();
    conjunto.add(conTres);
    boolean entroLaSegunda = conjunto.add(conCuatro);
    System.out.println("¿Entró la segunda al conjunto? " + entroLaSegunda);
    System.out.println("Materias en el conjunto: " + conjunto.size());
  }
}
