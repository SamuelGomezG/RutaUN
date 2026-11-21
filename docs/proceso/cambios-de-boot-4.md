---
title: "Cambios de Spring Boot 4 que afectan a los carriles"
doc_type: "proceso"
depends_on:
  - ../diseno/decisiones/0001-motor-del-calculo-del-plan.md
---

# Cambios de Spring Boot 4 que afectan a los carriles

Nota de TT-01 (#48). El diseño de clases se escribió pensando en Boot 3; el
esqueleto y los carriles 2 y 3 usan Boot 4.1.2. Aquí queda lo verificado al
construir sobre Boot 4 y el porqué de las versiones fijadas.

## 0′ · Cuándo cargar este archivo

Al tocar `pom.xml`, los perfiles, las dependencias, Jackson, Hibernate, Flyway o
las pruebas, y al migrar código escrito para Boot 3. Las reglas de código están
en `AGENTS.md`; el diseño, en `docs/diseno/`.

## Verificado en el PR 1

- **`@AutoConfigureMockMvc` cambió de paquete.** En Boot 3 vivía en
  `org.springframework.boot.test.autoconfigure.web.servlet`; en Boot 4.1.2 vive en
  `org.springframework.boot.webmvc.test.autoconfigure` (junto con `@WebMvcTest`).
- **Spring Modulith 2.1.2 cambió la firma de `verify`.** En la serie 1 era
  `ApplicationModules.verify(ApplicationModules.of(App.class))`; en 2.1.2 es el
  método de instancia `ApplicationModules.of(App.class).verify()`
  (con variante `verify(VerificationOptions)` para reglas adicionales).
- **El starter de pruebas web es `spring-boot-starter-webmvc-test`** e incluye
  `spring-boot-starter-test` (JUnit 6, AssertJ, Mockito, Hamcrest): con él basta
  para MockMvc y para las de integración.
- **Versiones que llegan por el BOM de Boot 4.1.2:** Hibernate 7.4.13.Final,
  Jackson 3.1.7 (paquetes `tools.jackson`, ya no `com.fasterxml.jackson`),
  Flyway 12.4.0, H2 2.4.240, JUnit 6.0.3, AssertJ 3.27.7, Mockito 5.23.0.
- **Aviso de Flyway 12 con H2 2.4:** al arrancar, Flyway registra un WARN de
  que H2 2.4.240 es más nueva que la última versión verificada (2.3.232). No
  bloquea: el arranque y la prueba de humo pasan.

## Verificado en el PR 2

- **jqwik 1.10.2 corre con JUnit 6 (R3):** el humo descubre y ejecuta
  propiedades junto a JUnit 6.0.3 sin conflicto de motores; entra al stack.
- **ArchUnit 1.5.1 corre con JUnit 6 (R3):** `CapasTest` y `NombresTest`
  corren en `verify`. Sin `haveSimpleNameMatching` ni
  `areMetaAnnotatedWith` fluentes; los `check` van como
  `rule.check(classes)`.

## Verificado en los carriles 2 y 3

- **Jackson 3 (R1) ya se usa.** `JsonConfig` crea el `JsonMapper`
  (`tools.jackson.databind`), y el lector de UC07-HU03 usa la misma API. Ya no
  queda nada «por verificar».
- **Flyway 12 con H2 2.4** mantiene el WARN de versión, pero no bloquea; el
  arranque y las pruebas pasan. El esquema lo posee Flyway (`ddl-auto:
  validate`).

## Verificado en TT-04 (sprint 2)

- **R1.** El único `JsonMapper` de Jackson 3 es el de `JsonConfig`. Lo usan
  Spring, Hibernate y los lectores de JSON.
- **R2.** `@JdbcTypeCode(SqlTypes.JSON)` funciona sobre H2 2.4.240 con Hibernate
  7.4.13 y Jackson 3. Sin configurar, Hibernate escribe las fechas como arreglo;
  con `Jackson3JsonFormatMapper(JsonConfig.mapper())` las escribe en ISO.
- **Bases de desarrollo.** Si cambia la forma de un JSON guardado, en una base
  **desechable** borra `data/` y vuelve a cargar la semilla. Si contiene datos
  que debes conservar —normas incorporadas con UC07 fuera de `semilla/`,
  incidentes o, cuando existan, avances y planes—, no la borres: respáldala y
  migra esos datos.

## Decisiones de stack (TT-01)

La tabla de versiones vigente está en `AGENTS.md`, sección «Stack». Aquí queda
el porqué de las decisiones que no se leen en el `pom.xml`.

| # | Decisión | Porqué |
| --- | --- | --- |
| D1 | Spring Boot 4.1.2 con Modulith 2.1.2 | Última estable; Modulith 2.1.2 está construido contra Boot 4.1.2. |
| D2 | SDKMAN opcional; `.sdkmanrc` para quien lo use | La protección real es el Enforcer; obligarlo agrega un requisito a máquinas distintas. |
| D3 | Enforcer `[21,22)` | «Versión fijada» literal: evita compilar en 25 o 26 y fallar en la CI. |
| D4 | TT-01 no crea `V1__esquema.sql` | TT-04 es dueña del esquema; TT-01 prueba «guarda y lee» con una migración solo de pruebas en `src/test/resources/db/humo/`. |
| D5 | Un issue, tres PR | #48 se queda entero; los PR usan `Refs #48` y el último `Closes #48`. |
| D6 | `ArgumentChecks` en el PR 2 | Es una clase real del diseño que necesita TT-02; da el ejemplo unitario sin clases de relleno. |
| D7 | `application-test.yml` con `@ActiveProfiles("test")` | Un `application.yml` en `src/test/resources` taparía por completo el de `main`. |
| D8 | jqwik entra solo si el humo pasa con JUnit 6 | Se verificó en el PR 2: jqwik 1.10.2 corre con JUnit 6.0.3. |

Riesgos de versión y cómo se cerraron:

| # | Riesgo | Resultado |
| --- | --- | --- |
| R1 | Jackson 3 cambia de paquetes (`tools.jackson`) | Verificado: `JsonConfig` es el único `JsonMapper`. |
| R2 | Hibernate 7.4 con columnas JSON | Verificado en TT-04: columnas `JSON` con `@JdbcTypeCode(SqlTypes.JSON)`. |
| R3 | JUnit 6 con jqwik y ArchUnit | Verificado en el PR 2: ambos corren. |
| R4 | Paquetes de prueba de Boot 4 | `@AutoConfigureMockMvc` vive en `org.springframework.boot.webmvc.test.autoconfigure`; `pruebas.md` deja los ejemplos. |
| R5 | Flyway 12 con H2 2.4 y directorio sin migraciones | Resuelto: `V1__esquema.sql` existe y `application-test.yml` incluye `db/migration` y `db/humo`. |
| R6 | Ruta crítica | Cerrado: el esqueleto está en `develop`. |
