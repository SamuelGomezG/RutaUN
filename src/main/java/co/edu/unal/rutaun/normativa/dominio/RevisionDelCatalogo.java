package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Revisión de las materias de una norma contra el catálogo global y contra la propia norma: reúne
 * las materias que faltan en el catálogo y los motivos por los que la incorporación se rechaza.
 *
 * <p>El catálogo es único y global: el código es la identidad de la materia y sus créditos son
 * fijos (Acuerdo 033 de 2007 del CSU, art. 5, parágrafo 2). UC07 ext. 6d no acepta una misma
 * materia con créditos distintos, ni frente al catálogo ni dentro de la misma norma. El resto de
 * esa extensión —nombres distintos, cuentas de créditos y malla utilizable— lo cubre #42.
 *
 * @param nuevas materias que faltan en el catálogo, una por código y en orden de aparición
 * @param motivos incoherencias encontradas, sin repetir
 */
public record RevisionDelCatalogo(List<Materia> nuevas, List<String> motivos) {

  /** Copia las dos listas sin nulos. */
  public RevisionDelCatalogo {
    nuevas = ArgumentChecks.copiaSinNulos(nuevas, "nuevas");
    motivos = ArgumentChecks.copiaSinNulos(motivos, "motivos");
  }

  /**
   * Revisa las reglas de materia de una norma contra el catálogo.
   *
   * @param reglas reglas de la norma, en su orden
   * @param catalogo materias ya registradas
   * @return las materias que faltan y los motivos de rechazo
   */
  public static RevisionDelCatalogo de(List<Regla> reglas, Collection<Materia> catalogo) {
    ArgumentChecks.obligatorio(reglas, "reglas");
    ArgumentChecks.obligatorio(catalogo, "catalogo");
    Map<CodigoMateria, Creditos> porCodigo = new HashMap<>();
    for (Materia materia : catalogo) {
      ArgumentChecks.obligatorio(materia, "materia del catálogo");
      porCodigo.put(materia.codigo(), materia.creditos());
    }

    List<Materia> nuevas = new ArrayList<>();
    Set<CodigoMateria> registradas = new LinkedHashSet<>();
    Map<CodigoMateria, Creditos> enLaNorma = new HashMap<>();
    Set<String> motivos = new LinkedHashSet<>();
    for (Regla regla : reglas) {
      ArgumentChecks.obligatorio(regla, "regla");
      if (!(regla.datos() instanceof DatosDeRegla.DeMateria datos)) {
        continue;
      }
      CodigoMateria codigo = datos.materia().codigo();
      Creditos creditos = datos.materia().creditos();
      Creditos delCatalogo = porCodigo.get(codigo);
      if (delCatalogo != null && !delCatalogo.equals(creditos)) {
        motivos.add(
            motivo(regla, creditos, codigo)
                + ", que en el catálogo tiene "
                + delCatalogo.valor()
                + ".");
      }
      Creditos enEstaNorma = enLaNorma.putIfAbsent(codigo, creditos);
      if (enEstaNorma != null && !enEstaNorma.equals(creditos)) {
        motivos.add(
            motivo(regla, creditos, codigo)
                + ", que en esta norma tiene "
                + enEstaNorma.valor()
                + ".");
      }
      if (delCatalogo == null && registradas.add(codigo)) {
        nuevas.add(Materia.registrar(codigo, creditos));
      }
    }
    return new RevisionDelCatalogo(nuevas, List.copyOf(motivos));
  }

  private static String motivo(Regla regla, Creditos creditos, CodigoMateria codigo) {
    return "La regla \""
        + regla.objeto()
        + "\" ("
        + regla.fuente().articulo()
        + ") le da "
        + creditos.valor()
        + " créditos a la materia "
        + codigo.valor();
  }
}
