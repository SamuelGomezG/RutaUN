package co.edu.unal.rutaun.normativa.persistencia;

import java.util.List;
import java.util.UUID;

/**
 * Forma en que se guarda el alcance de una norma en la columna {@code alcance_detalle}, como texto
 * JSON. El tipo del alcance no va aquí: tiene su propia columna, {@code tipo_alcance}. Usa solo
 * tipos básicos, porque la persistencia no puede importar el dominio.
 *
 * @param facultades nombres de las facultades comprendidas
 * @param programas identificadores de los programas comprendidos
 * @param exclusiones identificadores de los programas excluidos
 * @param texto texto con el que la norma fija el alcance
 */
public record AlcanceJson(
    List<String> facultades, List<UUID> programas, List<UUID> exclusiones, String texto) {}
