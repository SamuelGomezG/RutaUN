package co.edu.unal.rutaun.normativa.persistencia;

import java.time.LocalDate;

/** Las tres columnas que guardan un momento: su tipo, su fecha o su periodo. */
public record ColumnasDeMomento(String tipo, LocalDate fecha, String periodo) {}
