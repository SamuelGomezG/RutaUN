package co.edu.unal.rutaun.normativa.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Fila de la tabla {@code programa}: un programa académico. */
@Entity
@Table(name = "programa")
public class ProgramaEntidad {

  @Id private UUID id;

  @Column(nullable = false)
  private String nombre;

  @Column(name = "codigo_sia", nullable = false)
  private String codigoSia;

  @Column(nullable = false)
  private String facultad;

  /** Constructor sin argumentos que exige JPA. */
  protected ProgramaEntidad() {}

  /** Crea un programa. */
  public ProgramaEntidad(UUID id, String nombre, String codigoSia, String facultad) {
    this.id = id;
    this.nombre = nombre;
    this.codigoSia = codigoSia;
    this.facultad = facultad;
  }

  public UUID getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCodigoSia() {
    return codigoSia;
  }

  public String getFacultad() {
    return facultad;
  }
}
