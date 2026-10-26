package co.edu.unal.rutaun.soporte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de integración: contexto completo de la aplicación con H2 en memoria (perfil test,
 * application-test.yml) y la etiqueta {@code integracion}, para poder correrlas con {@code ./mvnw
 * test -Dgroups=integracion}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@Tag("integracion")
public @interface PruebaDeIntegracion {}
