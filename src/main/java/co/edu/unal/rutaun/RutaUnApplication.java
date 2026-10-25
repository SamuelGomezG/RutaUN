package co.edu.unal.rutaun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de RutaUN, en la raíz del paquete para que el
 * escaneo de componentes cubra todos los módulos.
 */
@SpringBootApplication
public class RutaUnApplication {

  public static void main(String[] args) {
    SpringApplication.run(RutaUnApplication.class, args);
  }
}
