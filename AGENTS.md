# AGENTS.md · RutaUN

**RutaUN** es un planificador de carrera semestre a semestre para estudiantes de pregrado de la UNAL Sede Bogotá, calculado sobre la normativa consolidada de su programa. Proyecto del curso Ingeniería de Software I («Equipo Zeus», 2026-II). Todo va en español: código de dominio, documentación e interfaz.

**Estado.** Sprint 1 (22 de octubre al 4 de noviembre de 2026). Hoy el repositorio tiene `README.md`, `CONTRIBUTING.md`, este archivo y `CLAUDE.md`, `docs/requisitos/` y `docs/proceso/definicion-de-hecho.md`; el esqueleto (TT-01, #48) y el resto de la estructura de abajo se crean durante el sprint. Los comandos de «Construir y probar» están **previstos**, no son ejecutables hasta que exista el esqueleto. Lo marcado «pendiente» tampoco existe todavía.

## Dónde está cada cosa

| Qué | Dónde |
|---|---|
| Requisitos (SRS), fuente de verdad | `docs/requisitos/`; empieza por su `README.md` (router tarea→archivos) |
| Diseño y arquitectura | `docs/diseno/` (pendiente, DOC-04; se escribe módulo por módulo) |
| Formato del JSON de normativa | `docs/datos/formato-normativa.md` (pendiente, DOC-05) |
| Definición de Hecho | `docs/proceso/definicion-de-hecho.md` |
| Convención de pruebas | `docs/proceso/pruebas.md` (pendiente, PRU-01) |
| Flujo de ramas, commits y solicitudes de integración | `CONTRIBUTING.md` |
| Backlog | Tablero «RutaUN» (GitHub Projects) e issues de `SamuelGomezG/RutaUN` |

El diseño de clases completo vive fuera del repositorio. Lo vigente para programar está en este archivo; si necesitas un detalle que no está aquí ni en `docs/diseno/`, pregunta en vez de inventarlo.

## Reglas de oro (obligatorias en cada tarea; letra completa en `docs/requisitos/00-contexto.md` §0)

1. **Traza todo a IDs** — en código, tests, commits y PRs: `UCnn`, `UCnn-HUmm`, `UCnn paso n`, `UCnn ext. na`, `UCnn GM n`/`GE n`, `[I-n]`, `S1–S18`, `#n`.
2. **Las normas académicas son datos, no constantes de código** (se digitan o se cargan por JSON vía UC07). Las cifras del SRS (topes de créditos, etc.) son ejemplos de datos de normas reales.
3. **Si el SRS calla, no decidas en silencio**: revisa `docs/requisitos/04-supuestos.md` y `07-lagunas.md`; si tampoco está, pregunta o registra un supuesto nuevo marcado como tal.
4. Respeta el **fuera de alcance** (§2.2 del SRS): sin integración con el SIA, sin oferta/cupos/horarios, sin doble titulación, sin trámites del reglamento estudiantil.
5. **Nada de lo que muestra RutaUN es inscripción ni autorización**: todo plan, comparación o explicación lleva el «Aviso de lo que RutaUN no verifica».
6. **Vocabulario del glosario** (`docs/requisitos/glosario/`, 71 términos) en español, tal cual, en UI y documentación.
7. **Privacidad por diseño** (Ley 1581 de 2012): avance y planes son datos personales; solo los ve su dueño con sesión; nunca en logs, ni en pruebas, ni en la semilla.

Regla estructural: cada ID aparece **exactamente una vez** como texto normativo en `docs/requisitos/` (dueños de IDs en su `05-trazabilidad.md`).

## Stack

Java 21 (fijado con `.sdkmanrc` y Enforcer `[21,22)`) y **Maven 3.9.16** con wrapper (`mvnw`), **Spring Boot 4.1.2** (MVC, Thymeleaf, Data JPA, Flyway) con **Spring Modulith 2.1.2**, ArchUnit 1.5.1, H2 2.4.240 en modo archivo (en memoria en las pruebas) y Flyway 12.4.0 en SQL estándar. Por el BOM de Boot 4 vienen Hibernate 7.4 y Jackson 3 (paquetes `tools.jackson`), y las pruebas usan JUnit 6, AssertJ 3.27.7 y Mockito 5.23.0 (jqwik 1.10.2 para propiedades, por verificar contra JUnit 6); el diseño de clases hablaba de Hibernate 6 y Jackson 2, y las diferencias están en `docs/proceso/cambios-de-boot-4.md`. Google Java Style con Spotless 3.10.4 (google-java-format 1.37.0) y Checkstyle 14.3.0.

## Estructura del proyecto

▸ = existe al cerrar el sprint 1.

```
project/                          raíz del repositorio
├─ README.md · CONTRIBUTING.md    DOC-02 · DOC-03
├─ AGENTS.md · CLAUDE.md          este archivo y su compañero
├─ .sdkmanrc · .gitignore · .gitattributes · .editorconfig
├─ .gitmessage ▸                  plantilla de commits (git config commit.template .gitmessage)
├─ .github/                       workflows/ci.yml · ISSUE_TEMPLATE/ ▸ · pull_request_template.md ▸ · PULL_REQUEST_TEMPLATE/ ▸
├─ docs/
│  ├─ requisitos/ ▸               SRS (fuente de los requisitos)
│  ├─ diseno/                     arquitectura.md · decisiones/
│  ├─ datos/                      formato-normativa.md
│  ├─ proceso/                    definicion-de-hecho.md · pruebas.md
│  └─ uso/                        guia-de-uso.md (al final)
├─ config/checkstyle/             checkstyle.xml · suppressions.xml
├─ pom.xml · mvnw · mvnw.cmd · .mvn/wrapper/
├─ data/                          archivo H2; lo crea la app y git lo ignora
└─ src/
   ├─ main/
   │  ├─ java/co/edu/unal/rutaun/
   │  │  ├─ RutaUnApplication.java  @SpringBootApplication, en la raíz del paquete
   │  │  ├─ compartido/ ▸           núcleo compartido: PeriodoAcademico, identificadores, Creditos…
   │  │  ├─ utilidades/ ▸           ArgumentChecks, JsonConfig
   │  │  ├─ web/                    piezas transversales de presentación
   │  │  ├─ normativa/ ▸            raíz = API del módulo (fachadas, vistas, interfaces de rol)
   │  │  │  └─ web/ · aplicacion/ · dominio/ · persistencia/
   │  │  ├─ incidentes/ ▸           SOP-05: aplicacion/ · persistencia/
   │  │  └─ avance/ · plandecarrera/ · periodoencurso/ · identidad/   (después del sprint 1)
   │  └─ resources/
   │     ├─ application.yml · application-dev.yml · messages.properties
   │     ├─ db/migration/V1__esquema.sql
   │     ├─ semilla/              programas.json · acuerdo-003a-2022.json · …
   │     ├─ templates/            layout/ · publico/ · estudiante/ · admin/ · error.html
   │     └─ static/               css/ · js/ · img/
   └─ test/
      ├─ java/co/edu/unal/rutaun/
      │  ├─ arquitectura/         ModulosTest · CapasTest · NombresTest
      │  ├─ normativa/…           espejo de los paquetes
      │  └─ soporte/              mallas sintéticas, Clock fijo
      └─ resources/               application-test.yml · db/humo/ · json/validos/ · json/invalidos/
```

**Capas y módulos**
- Cuatro capas con dependencias **solo hacia abajo**: presentación (`web`) → negocio (`aplicacion` y `dominio`) → persistencia; todas usan utilidades.
- Dentro de un módulo: `web` usa `aplicacion`; `aplicacion` usa `dominio` y `persistencia`, que no se conocen entre sí. El dominio no conoce persistencia ni Spring. Persistencia no importa ninguna clase de negocio. Solo `aplicacion` usa los repositorios de Spring Data.
- Cada contexto es un módulo de Spring Modulith (un paquete de primer nivel). Lo que otros módulos pueden usar va en la raíz del paquete del módulo; los subpaquetes son internos. Ningún módulo usa clases internas de otro.
- `compartido`, `utilidades` y `web` también son paquetes de primer nivel, así que se declaran módulos abiertos (`@ApplicationModule(type = OPEN)` en su `package-info.java`).
- En el sprint 1 solo existen `normativa` e `incidentes` (este último de «estilo simple»: la entidad vive en `persistencia` y su lógica en el servicio de `aplicacion`). No crees módulos vacíos.
- `IncorporacionJsonReader` vive en `normativa/aplicacion`, no en presentación: lo invocan el arranque en desarrollo y, más adelante, el controlador del formulario de carga.

## Convenciones de código

- Dominio en español (`Estudiante`, `PlanDeCarrera`); estereotipos técnicos en inglés (`…Facade`, `…Orchestrator`, `…Repository`, `…Service`); sin sufijo `Impl`; identificadores solo **ASCII**, sin tildes ni eñes (`ParteDelAnio`).
- Value objects como `record`, inmutables y validados al construirse. Los agregados se crean con fábricas que validan sus invariantes (`registrar(...)`) y se reconstruyen desde la base con `reconstituir(...)`, que no emite eventos. Ninguna colección interna sale modificable; entre agregados se referencia por identificador tipado.
- Inyección solo por constructor, con dependencias `final`; nunca por campo. Entidades y valores no reciben inyección. Comandos inmutables.
- Las extensiones del SRS son **resultados sellados** (`sealed interface`) y el controlador hace un `switch` exhaustivo; las excepciones quedan para fallas reales.
- Post/Redirect/Get tras cada POST exitoso; `@Transactional` en los servicios que modifican.
- Plazos con `CompletableFuture.orTimeout` sobre hilos virtuales: cálculo del plan 30 s (S3) y validación y vista previa de UC07 60 s (S9).
- Estilo: Google Java Style con Spotless y Checkstyle. El build **falla** si algo no está formateado.

## Normativa y datos

- La normativa se carga desde JSON y nunca se escribe como constante. La semilla de Sistemas (los acuerdos 003-A de 2022, 11 de 2023 y 008 de 2008, más el archivo de programas) vive en `src/main/resources/semilla/`.
- Con el perfil `dev`, el arranque carga primero los programas (SOP-03) y luego las normas, con el mismo servicio de importación de UC07-HU03. Repetir el arranque no duplica lo ya cargado. No hay pantalla de carga ni autenticación en el sprint 1.
- El formato del JSON es un contrato con los administradores de normativa: se documenta en `docs/datos/formato-normativa.md`, y los cambios de formato se prueban con los archivos de `src/test/resources/json/`.

## Pruebas

- Cada test lleva en su nombre el ID del SRS que verifica: `uc01_ext4b_topeBajoElMinimoAdvierte` (inicia en minúscula, separadores `_`).
- Pirámide: unitarias de dominio sin Spring (JUnit 5 y AssertJ) · propiedades (jqwik) · orquestadores (Mockito, `Clock` fijo) · arquitectura (`ApplicationModules.verify()` y ArchUnit) · integración (`@SpringBootTest` o `@DataJpaTest` con H2 en memoria, marcadas con `@Tag("integracion")`) · web (MockMvc) · contrato JSON · aceptación.
- Las pruebas de arquitectura fallan si una capa depende de una superior, si un módulo usa las entrañas de otro o si alguna clase termina en `Impl`.
- Los datos de prueba son mallas sintéticas (`src/test/java/.../soporte`); nunca datos personales reales.

## Construir y probar (previsto, Maven)

```
./mvnw verify                                        # compila, verifica formato y estilo, y corre todas las pruebas
./mvnw spotless:apply                                # formatea el código
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # arranca y carga la semilla de Sistemas
```

## Definición de terminado

Un elemento cuenta como terminado solo si: está integrado en `develop` y revisado por otra persona; si incluye código, quien lo escribió y quien lo revisó pueden ejecutarlo para recrear el flujo y obtener un resultado observable que muestre que hace lo que debe; sus unitarias y las pruebas de arquitectura están en verde en la integración continua; cumple Google Java Style; y, si es historia, sus criterios de aceptación se demostraron y su issue quedó cerrado.

## Trazabilidad GitHub

- Numeración de issues: #1–#7 = UC01 · #8–#12 = UC02 · #13–#21 = UC03 · #22–#26 = UC04 · #27–#33 = UC05 · #34–#38 = UC06 · #39–#47 = UC07 · #48–#52 = TT-01…TT-05 · #53–#60 = SOP-01…SOP-08 · #61–#62 = PRU-02, PRU-04 · #63–#66 = DOC-01, DOC-03, DOC-04, DOC-06 · #67 = SOP-05-T2 · #68 = DOC-07.
- Los commits y las solicitudes de integración citan los IDs que cubren (`#n`, `UCnn-HUmm`). El formato de los mensajes y el flujo de trabajo están en `CONTRIBUTING.md`.
- Ramas: `main` solo recibe `develop`, al cierre de un sprint que la demostración dio por satisfactorio. El trabajo va en una rama por issue que se fusiona a `develop`, la rama predeterminada. Nadie hace commit directo en ninguna de las dos.

## Alcance del sprint 1 (no improvises más allá)

Solo: esqueleto con entorno reproducible, marco de pruebas y README (TT-01), dominio estructural (TT-02, TT-03), persistencia, H2 y JSON de Sistemas (TT-04), spike del motor de UC01 (TT-05), UC07-HU03 (#41, carga por JSON), UC06-HU01 (#34), alta de programas por JSON (SOP-03, #55), registro de incidentes sin pantalla (SOP-05, #57) y la guía de contribución con la Definición de Hecho (DOC-03, #64). Fuera a propósito: UC01/UC02/UC04 funcionales, UC03 completo, el resto de UC07, cuentas, sesión y avance. Los adicionales se toman en orden y solo si sobra capacidad; están en el tablero con la etiqueta `sprint-1-adicional`.

## No agregues todavía

Cachés Caffeine, semáforo de cálculos simultáneos, respaldos programados, migraciones en Java del esquema JSON, registro de publicación de eventos con reintentos, umbrales de SonarQube o PMD, Docker, OpenAPI, Javadoc generado y CHANGELOG. Se reabren solo cuando una medición o una historia lo exija; el camino es un spike con tiempo máximo, no una tarea suelta.

## Seguridad

Ninguna credencial en el repositorio: el administrador inicial usa variables de entorno. El archivo de `data/` no se versiona.
