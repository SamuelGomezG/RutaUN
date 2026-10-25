# RutaUN

![Estado](https://img.shields.io/static/v1?label=ESTADO&message=EN%20DESARROLLO&color=RED&style=for-the-badge)
![Sprint](https://img.shields.io/static/v1?label=SPRINT&message=1%20%C2%B7%2022%20oct%20%E2%80%93%204%20nov%202026&color=blue&style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-MVC%20%2B%20Thymeleaf-6DB33F?style=for-the-badge)

<!-- Insignia de la integración continua: agregarla cuando exista .github/workflows/ci.yml (TT-01, #48). -->

Planificador de carrera semestre a semestre para estudiantes de pregrado de la UNAL Sede Bogotá, calculado sobre la normativa consolidada de su programa.

> [!NOTE]
> El proyecto está en el sprint 1. Hoy el repositorio contiene los requisitos y las guías del equipo; el esqueleto técnico llega con la tarea TT-01 ([#48](https://github.com/SamuelGomezG/RutaUN/issues/48)). Los pasos de [Abrir y ejecutar el proyecto](#abrir-y-ejecutar-el-proyecto) son los previstos y todavía no funcionan.

### Tópicos

- [Descripción del proyecto](#descripción-del-proyecto)
- [Funcionalidades](#funcionalidades)
- [Herramientas utilizadas](#herramientas-utilizadas)
- [Acceso al proyecto](#acceso-al-proyecto)
- [Abrir y ejecutar el proyecto](#abrir-y-ejecutar-el-proyecto)
- [Pruebas](#pruebas)
- [Documentación](#documentación)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Meta del sprint 1](#meta-del-sprint-1)
- [Contribuir](#contribuir)
- [Privacidad y seguridad](#privacidad-y-seguridad)
- [Licencia](#licencia)
- [Equipo](#equipo)

## Descripción del proyecto

RutaUN es una herramienta para que los estudiantes de pregrado de la Sede Bogotá de la Universidad Nacional de Colombia planeen su carrera semestre a semestre. El estudiante registra su avance en la malla, genera un plan de carrera dentro de un tope de carga (créditos o materias por semestre), lo replanifica cuando se atrasa o cambia algo, y compara alternativas de carga para ver cuántos semestres le faltan con cada una.

Todo se calcula sobre un **consolidado normativo versionado** de cada programa: reglas con su fuente (norma, artículo o numeral), su vigencia y su alcance, más la malla estándar del programa. Cualquier visitante, con o sin sesión, puede consultar la normativa y las restricciones de las materias de los programas cargados. La normativa entra al sistema como datos que carga un administrador de normativa, no como constantes del código.

El problema nace de lo que cuentan los estudiantes: llevan su avance rayando el PDF de la malla, organizan sus materias en una hoja de cálculo, y la normativa de un programa no está reunida en un solo lugar.

> [!IMPORTANT]
> RutaUN es una guía, no una inscripción ni una autorización, y no es fuente oficial: la fuente oficial de las normas es Régimen Legal. Todo plan, comparación o explicación lleva el «Aviso de lo que RutaUN no verifica».

Proyecto del curso Ingeniería de Software I («Equipo Zeus», 2026-II).

## Funcionalidades

| Caso de uso | Qué permite | Quién | Estado |
| --- | --- | --- | --- |
| UC01 · Generar plan de carrera | Un plan semestre a semestre dentro de un tope de carga | Estudiante | Planeado |
| UC02 · Replanificar plan de carrera | Recalcular el plan cuando algo cambia | Estudiante | Planeado |
| UC03 · Registrar avance en la malla | Marcar lo aprobado y lo que se cursa, por componente y agrupación | Estudiante | Planeado |
| UC04 · Comparar alternativas de carga | Ver cuántos semestres faltan con cada tope | Estudiante | Planeado |
| UC05 · Entender restricciones de una materia | Prerrequisitos, correquisitos y reglas de habilitación de una materia | Visitante | Planeado |
| UC06 · Consultar regla del programa y su acuerdo | Las reglas de un programa, con su estado y su fuente | Visitante | En el sprint 1 (UC06-HU01) |
| UC07 · Incorporar acuerdo al consolidado del programa | Cargar normas y reglas al consolidado de un programa | Administrador de normativa | En el sprint 1 (UC07-HU03, solo por archivo JSON) |

**Fuera de alcance:** otras sedes, integración con el SIA, oferta de materias, cupos y horarios, doble titulación, trámites del reglamento estudiantil y describir de qué trata cada materia. La lista completa está en [`docs/requisitos/00-contexto.md`](docs/requisitos/00-contexto.md) (§2.2).

## Herramientas utilizadas

| Herramienta | Para qué |
| --- | --- |
| [Java 21](https://openjdk.org/projects/jdk/21/) | Lenguaje; `switch` exhaustivo sobre tipos sellados |
| [Maven](https://maven.apache.org) (con wrapper) | Construcción y dependencias |
| [Spring Boot](https://spring.io/projects/spring-boot) | Aplicación web con MVC |
| [Thymeleaf](https://www.thymeleaf.org) | Vistas en el servidor |
| [H2](https://www.h2database.com) y [Flyway](https://flywaydb.org) | Base de datos en archivo y sus migraciones en SQL estándar |
| [Spring Modulith](https://spring.io/projects/spring-modulith) y [ArchUnit](https://www.archunit.org) | Verificación automática de módulos y capas |
| [Spotless](https://github.com/diffplug/spotless) y [Checkstyle](https://checkstyle.org) | Google Java Style; el build falla si algo no está formateado |
| [JUnit 5](https://junit.org/junit5/), [AssertJ](https://assertj.github.io/doc/), [Mockito](https://site.mockito.org) y [jqwik](https://jqwik.net) | Pruebas |

## Acceso al proyecto

Puedes [acceder al código fuente](https://github.com/SamuelGomezG/RutaUN) o seguir el trabajo en el [tablero «RutaUN»](https://github.com/users/SamuelGomezG/projects/3) y en los [issues](https://github.com/SamuelGomezG/RutaUN/issues). El trabajo vive en `develop`, la rama predeterminada; `main` guarda lo que se mostró y se dio por satisfactorio al cierre de cada sprint.

## Abrir y ejecutar el proyecto

> [!NOTE]
> Estos pasos son los previstos. Funcionan cuando TT-01 ([#48](https://github.com/SamuelGomezG/RutaUN/issues/48)) entregue el esqueleto.

**Requisitos previos:** [Git](https://git-scm.com/downloads) y un JDK 21. No necesitas instalar Maven: el proyecto trae su wrapper.

1. Clona el repositorio:

```sh
git clone https://github.com/SamuelGomezG/RutaUN.git
cd RutaUN
```

2. (Opcional) Instala la versión de Java que fija el proyecto con [SDKMAN](https://sdkman.io):

```sh
sdk env install
```

3. Compila, verifica el formato y corre todas las pruebas:

```sh
./mvnw verify
```

4. Arranca la aplicación con la semilla de Sistemas (el perfil `dev` carga los programas y las normas desde `src/main/resources/semilla/`):

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> [!IMPORTANT]
> RutaUN queda en [http://localhost:8080/](http://localhost:8080/), el puerto por defecto de Spring Boot. En Windows usa `mvnw.cmd` en lugar de `./mvnw`.

## Pruebas

Cada prueba lleva en su nombre el ID del requisito que verifica, por ejemplo `uc01_ext4b_topeBajoElMinimoAdvierte`. Hay pruebas unitarias del dominio sin Spring, de arquitectura (módulos y capas), de integración con H2 en memoria, de la capa web con MockMvc y de contrato para el formato del JSON de normativa. Se corren todas con:

```sh
./mvnw verify
```

## Documentación

| Documento | Contenido |
| --- | --- |
| [`docs/requisitos/`](docs/requisitos/README.md) | El SRS, fuente de los requisitos: casos de uso, historias, glosario y supuestos |
| [`AGENTS.md`](AGENTS.md) | Reglas de oro, estructura, convenciones de código y pruebas |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Cómo contribuir: ramas, commits, solicitudes de integración y revisión |
| [`docs/proceso/definicion-de-hecho.md`](docs/proceso/definicion-de-hecho.md) | Cuándo un elemento cuenta como terminado |
| `docs/diseno/` | Arquitectura y decisiones (pendiente) |
| `docs/datos/formato-normativa.md` | Formato del JSON de normativa (pendiente) |

## Estructura del repositorio

```
.
├─ docs/            requisitos, diseño, formato de datos y proceso
├─ src/main/        código y recursos (Java, plantillas, migraciones, semilla de normativa)
├─ src/test/        pruebas
├─ config/          configuración de Checkstyle
├─ AGENTS.md        guía para quien programa, agentes incluidos
└─ CONTRIBUTING.md  guía de contribución
```

La estructura completa de carpetas y paquetes está en [`AGENTS.md`](AGENTS.md).

## Meta del sprint 1

Al cierre del sprint (miércoles 4 de noviembre de 2026) se podrá demostrar, de punta a punta:

- La carga por JSON de las normas y reglas de Ingeniería de Sistemas y Computación al consolidado del programa (UC07-HU03).
- La consulta, sin iniciar sesión, de las reglas del programa agrupadas por categoría, con su estado, su etiqueta de temporal y la versión del consolidado (UC06-HU01).
- Las pruebas de arquitectura en verde en la integración continua.
- Un informe del spike del motor de UC01, con el enfoque elegido y tiempos medidos sobre la malla real de Sistemas.

## Contribuir

Lee la [guía de contribución](CONTRIBUTING.md): explica cómo tomar un issue, nombrar las ramas, escribir commits atómicos y abrir una solicitud de integración contra `develop` que revise otra persona.

## Privacidad y seguridad

El avance y los planes de un estudiante son datos personales (Ley 1581 de 2012): solo los ve su dueño con sesión y nunca se escriben en los logs. En el repositorio no hay credenciales ni datos personales. Si encuentras un problema de seguridad, avísale al equipo por un canal privado y no abras un issue público con datos sensibles.

## Licencia

Pendiente de decidir con el docente y el equipo. Hasta entonces el repositorio no declara una licencia de uso.

## Equipo

Equipo Zeus · Ingeniería de Software I · Universidad Nacional de Colombia, Sede Bogotá · 2026-II.

| [<img src="https://avatars.githubusercontent.com/u/82069601?v=4" width=115><br><sub>Samuel Gómez Guio</sub>](https://github.com/SamuelGomezG) | [<img src="https://avatars.githubusercontent.com/u/205631703?v=4" width=115><br><sub>Elkin Stiven Chaparro Silva</sub>](https://github.com/StivenChaparro) | [<img src="https://avatars.githubusercontent.com/u/123595109?v=4" width=115><br><sub>David Alejandro Contreras García</sub>](https://github.com/AlexanderHartz) | [<img src="https://avatars.githubusercontent.com/u/188786827?v=4" width=115><br><sub>Lucas Daniel Forero Garzón</sub>](https://github.com/SunlessHeaven) | [<img src="https://avatars.githubusercontent.com/u/115485750?v=4" width=115><br><sub>Julián Camilo Hortúa Franco</sub>](https://github.com/Hortua) |
| :---: | :---: | :---: | :---: | :---: |
