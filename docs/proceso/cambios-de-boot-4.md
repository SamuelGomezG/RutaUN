# Cambios de Spring Boot 4 que afectan a los carriles

Nota de TT-01 (#48), PR 1 (25 de octubre de 2026). El diseño de clases se escribió
pensando en Boot 3; el esqueleto usa Boot 4.1.2. Lo verificado al construir el
esqueleto queda aquí; lo marcado «por verificar» se completa en el PR 2.

## Verificado en el PR 1

- **`@AutoConfigureMockMvc` cambió de paquete.** En Boot 3 vivía en
  `org.springframework.boot.test.autoconfigure.web.servlet`; en Boot 4.1.2 vive en
  `org.springframework.boot.webmvc.test.autoconfigure` (junto con `@WebMvcTest`).
  Afecta a toda prueba MockMvc de los carriles 2, 3 y 4.
- **El starter de pruebas web es `spring-boot-starter-webmvc-test`** e incluye
  `spring-boot-starter-test` (JUnit 6, AssertJ, Mockito, Hamcrest): con él basta
  para MockMvc y para las de integración.
- **Versiones que llegan por el BOM de Boot 4.1.2:** Hibernate 7.4.13.Final,
  Jackson 3.1.7 (paquetes `tools.jackson`, ya no `com.fasterxml.jackson`),
  Flyway 12.4.0, H2 2.4.240, JUnit 6.0.3, AssertJ 3.27.7, Mockito 5.23.0.
- **Aviso de Flyway 12 con H2 2.4:** al arrancar, Flyway registra un WARN de
  que H2 2.4.240 es más nueva que la última versión verificada (2.3.232). No
  bloquea: el arranque y la prueba de humo pasan. Se vigila en TT-04 (#51).

## Por verificar (se completa en el PR 2 y al construir sobre H2)

- **Jackson 3 (R1):** el `ObjectMapper` de `JsonConfig` y el lector de
  UC07-HU03 (#41) deben importar `tools.jackson.databind.ObjectMapper`. La API
  de lectura/escritura es casi igual, pero hay cambios menores (por verificar).
- **Hibernate 7.4 con columnas JSON (R2):** `@JdbcTypeCode(SqlTypes.JSON)` sobre
  H2 2.4 y qué librería JSON usa por dentro (`ReglaEntity` de TT-04, #51).
- **jqwik 1.10.2 con JUnit 6 (R3):** jqwik 1.10.2 compila contra JUnit Platform
  1.14.4; el BOM de Boot usa la serie 6. Se prueba con un humo en el PR 2.
- **ArchUnit 1.5.1 con JUnit 6 (R3):** se prueba con `CapasTest` y `NombresTest`.
