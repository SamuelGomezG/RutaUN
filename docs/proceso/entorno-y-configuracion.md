---
title: "Entorno, configuración e integración continua"
doc_type: "proceso"
depends_on:
  - ../../AGENTS.md
  - pruebas.md
---

# Entorno, configuración e integración continua

## 0′ · Cuándo cargar este archivo

Al tocar `application*.yml`, el `pom.xml` o `.github/workflows/ci.yml`; al agregar una propiedad, un perfil o un paso del build; y cuando necesites saber qué hace el arranque o qué comprueba `./mvnw verify`. Si algo falla, ve a [`solucion-de-problemas.md`](solucion-de-problemas.md).

Este documento describe la configuración **como está en el código**. Su fuente son `src/main/resources/application*.yml`, `src/test/resources/application-test.yml`, `pom.xml` y `.github/workflows/ci.yml`: si difieren de lo que dice aquí, mandan ellos. Lo que se ejecutó para escribirlo se comprobó el 12 de noviembre de 2026.

## Perfiles

| | Sin perfil | `dev` | `test` |
| --- | --- | --- | --- |
| Cómo se activa | `./mvnw spring-boot:run` | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` | `@ActiveProfiles("test")`, que trae `@PruebaDeIntegracion` |
| Archivos | `application.yml` | `application.yml` y `application-dev.yml` | `application.yml` y `application-test.yml` |
| Base de datos | H2 en archivo, `./data/rutaun` | La misma | H2 en memoria (`jdbc:h2:mem:rutaun`) |
| Migraciones | `db/migration/` | `db/migration/` | `db/migration/` y `db/humo/` (una tabla solo de pruebas) |
| Semilla | No se carga | Se carga al arrancar | No se carga; cada prueba prepara sus datos |
| Log del proyecto | `INFO` | `DEBUG` para `co.edu.unal.rutaun` | `INFO` |

El perfil `test` vive en `application-test.yml` y no en un `application.yml` de pruebas, porque ese taparía por completo el de `main`.

## Propiedades que fija el proyecto

Todo lo que no aparece en esta tabla usa el valor por defecto de Spring Boot; por ejemplo, el puerto 8080.

| Propiedad | Valor | Qué implica |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:h2:file:./data/rutaun` | La ruta es relativa al directorio desde el que arrancas. Arranca siempre desde la raíz del repositorio, o `data/` aparecerá en otro lugar. H2 en archivo admite un solo proceso a la vez. |
| `spring.datasource.username` y `password` | `sa` y vacía | Son las de H2 embebido; la base es un archivo local que git ignora. No son credenciales de ningún servicio. |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Flyway es el dueño del esquema. Hibernate solo comprueba que las entidades coincidan con las tablas, y el arranque falla si no. |
| `spring.jpa.open-in-view` | `false` | La vista no abre consultas: lo que una plantilla necesita lo trae el servicio dentro de su transacción. |
| `rutaun.periodo-en-curso` | `"2026-2"` | Periodo académico en curso (SOP-04, [#56](https://github.com/SamuelGomezG/RutaUN/issues/56)): el último que empezó, con formato `AAAA-1` o `AAAA-2`. Es el único punto donde se cambia, y los demás módulos lo leen con `PeriodoEnCursoFacade`. Si falta o tiene otro formato, la aplicación no arranca y el error nombra la propiedad. Hoy se cambia editando este valor o pasándolo como argumento (`--rutaun.periodo-en-curso=2027-1`); «Avanzar periodo», que lo haría desde la aplicación, todavía no existe. |

**No hay variables de entorno todavía.** Ningún `application*.yml` ni clase lee una. Las primeras llegan con el administrador de normativa inicial (SOP-02, [#54](https://github.com/SamuelGomezG/RutaUN/issues/54)); cuando existan, se listan aquí con su nombre, sin su valor.

Para cambiar una propiedad en una sola ejecución, sin editar archivos, pásala como argumento. Sirve para probar con una base desechable sin tocar `data/`, o con otro puerto:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev \
  "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:file:/tmp/rutaun-prueba/rutaun --server.port=18080"
```

En Windows, incluido Git Bash, abre una sesión de PowerShell (por ejemplo con `powershell` lanzado desde Git Bash) y usa ahí `.\mvnw.cmd` con los argumentos de PowerShell, no lo pegues en Git Bash. Las rutas `/tmp/…` de este ejemplo son de Unix y Java las resolvería con `C:`.

**El reloj.** `RelojConfig` publica un `Clock` en hora de Colombia (`America/Bogota`). Las fechas que pone la aplicación —la de una versión del consolidado, las de un incidente— salen de él, no del sistema operativo. Las pruebas lo reemplazan por uno fijo.

## Qué hace el arranque

1. Flyway valida las migraciones ya aplicadas y aplica las nuevas.
2. Hibernate valida las entidades contra las tablas.
3. Arranca el servidor web.
4. Solo con `dev`, `SemillaDeDesarrolloRunner` carga la semilla de `src/main/resources/semilla/`:
   - primero `programas.json`;
   - después los demás `*.json`, **en orden alfabético del nombre del archivo**. Cada norma crea una versión del consolidado, así que ese orden decide el número de cada versión ([#146](https://github.com/SamuelGomezG/RutaUN/issues/146)).

Con una base nueva, el log termina así:

```text
Programas cargados: 1 creados, 0 ya existían.
La norma ACUERDO 902 de 2026, Consejo de Facultad de Ingeniería (datos de prueba) no deja ningún plan desactualizado: todas sus reglas son informativas.
Normas cargadas: 5 creadas, 0 ya existían; 5 versiones nuevas.
```

La línea de la ext. 5a aparece porque el Acuerdo 902 (datos de prueba) solo trae reglas informativas; el Acuerdo 901 no la provoca porque tiene reglas del plan.

Repetir el arranque no duplica nada: un programa se reconoce por su código SIA y una norma por su tipo, número, año y órgano, y lo que ya está se omite (`0 creadas, 5 ya existían; 0 versiones nuevas`). Por eso un dato corregido en la semilla no llega a una base que ya la cargó. Si un archivo de la semilla es inválido, el arranque falla y no se guarda nada de ese archivo.

## Qué responde la aplicación hoy

| Ruta | Respuesta | Por qué |
| --- | --- | --- |
| `GET /` | 200, página de inicio con el enlace a la consulta | — |
| `GET /programas` (UC06 paso 2) | 200, los programas cargados | Los lee `ConsultaNormativaService` de la base; con el perfil `dev` está cargado Ingeniería de Sistemas y Computación. Las pruebas de la página usan `ConsultaNormativaFalsa`. |
| `GET /programas/{id}/reglas` (UC06 pasos 4 y 6) | 200 con las reglas del consolidado vigente por categoría; 404 si el programa no está cargado o no tiene una versión vigente; 400 si `{id}` no es un UUID o `estado` no es un estado de regla | Sin parámetros muestra las reglas vigentes, programadas y en revisión, no las que ya no rigen. Con `texto` (palabras del enunciado, del acuerdo o del artículo, sin distinguir tildes), `categoria` (nombre del glosario) o `estado` (`VIGENTE`, `PROGRAMADA`, `EN_REVISION` o `YA_NO_RIGE`; el selector de la página ofrece solo `VIGENTE`, `PROGRAMADA` y `YA_NO_RIGE`, y `EN_REVISION` se pide escribiéndolo en la URL) muestra solo las que los cumplen; las que ya no rigen salen solo con `estado=YA_NO_RIGE`, con su vigencia. Sin resultados, la página lo dice y recuerda que debe consultarse Régimen Legal (ext. 5a); eso no publica ningún incidente. Si el consolidado vigente no tiene reglas que rijan, programadas ni en revisión, con o sin búsqueda, la página dice que tiene un problema ya informado (ext. 4a) y publica el incidente; con `estado=YA_NO_RIGE` tampoco se considera sin reglas si tiene alguna que ya no rige, aunque la búsqueda por texto o categoría no encuentre nada (ext. 5a) |
| `GET /programas/{programa}/reglas/{regla}` (UC06 paso 8) | 200 con la regla completa y la cadena de normas que la han afectado; 404 si el programa no está cargado o la regla no está en su consolidado vigente; 400 si un id no es un UUID | Lo lee `ConsultaNormativaService.regla`; abre también las reglas que ya no rigen, con ese estado; el enlace a Régimen Legal sale de la norma, y si falta la página lo dice |
| `GET /programas/{programa}/materias/{codigo}/restricciones` (UC05 pasos 1 a 4) | 200 con lo que la materia exige en el consolidado vigente (prerrequisitos, correquisitos y reglas de habilitación); 404 si el programa no está cargado, no tiene una versión vigente o la materia no figura en ella; 400 si `{programa}` no es un UUID o `{codigo}` está vacío | Lo lee `ConsultaNormativaService.restriccionesDe`; muestra las reglas vigentes, programadas y en revisión, no las que ya no rigen, y no nombra las materias externas (ext. 4k). Si el prerrequisito solo se cumple con una materia de otro programa, la página dice «No se puede cursar desde este programa»; esa señal solo la dan las reglas vigentes, no las programadas ni las que están en revisión (ext. 4e). Una restricción sin fuente sale como «sin fuente registrada» y publica un incidente (ext. 4d); una materia sin restricciones lo dice y recuerda que eso no significa oferta ni cupo (ext. 3a). Todavía no hay enlace de entrada desde la malla ni desde UC06: se llega por la URL, con el `{programa}` que sale de los enlaces de `/programas` |

No hay consola de H2 ni Spring Boot Actuator: la base se consulta con la aplicación detenida ([`esquema-de-la-base.md`](../datos/esquema-de-la-base.md), «Consultas de inspección»). Tampoco hay autenticación; llega con SOP-01 ([#53](https://github.com/SamuelGomezG/RutaUN/issues/53)).

## Qué corre `./mvnw verify`

En este orden. Si un paso falla, los siguientes no corren.

| Fase de Maven | Qué corre | Qué comprueba | Solo ese paso |
| --- | --- | --- | --- |
| `validate` | Enforcer | Java `[21,22)` y Maven 3.9 o superior | `./mvnw validate` |
| `compile` y `test-compile` | Compilador | Que compila | `./mvnw test-compile` |
| `test` | Surefire | **Todas** las pruebas: unitarias, propiedades, arquitectura, integración y web. No hay un perfil que excluya alguna | `./mvnw test`, o por etiqueta o clase ([`pruebas.md`](pruebas.md)) |
| `package` | Jar | Empaqueta las clases | — |
| `verify` | Spotless y después Checkstyle | El formato (google-java-format) y el estilo (configuración de Google, en `config/checkstyle/`). Checkstyle revisa también las pruebas y falla con cualquier aviso | `./mvnw spotless:check checkstyle:check` |

Dos consecuencias prácticas:

- **El formato se revisa al final**, después de todas las pruebas. Para saberlo antes, `./mvnw spotless:check checkstyle:check` corre los dos sin pruebas, en unos segundos.
- **El jar no es ejecutable.** El `pom.xml` no declara `spring-boot-maven-plugin`, así que `package` no produce un jar con sus dependencias y `java -jar target/rutaun-*.jar` no arranca. La aplicación se ejecuta con `./mvnw spring-boot:run`. El empaquetado para desplegar no está decidido.

Los `package-info.java` quedan fuera de Spotless y de Checkstyle: google-java-format falla al formatearlos y solo llevan javadoc.

## Integración continua

`.github/workflows/ci.yml` tiene un solo trabajo, `verify`, que corre en cada solicitud de integración y en cada push a `develop` y `main`.

| Paso | Comando o acción | Para reproducirlo en tu máquina |
| --- | --- | --- |
| Java | Temurin 21, con caché de Maven | Un JDK 21 |
| Aplicación | `./mvnw -B verify` | El mismo comando |
| Spike del motor | `./mvnw -B -f spikes/tt-05-motor/pom.xml verify` | El mismo comando. Es un proyecto aparte: el `verify` de la raíz no lo incluye, y los hooks de git tampoco |
| Si algo falla | Publica el artefacto `informes-de-surefire` | Se descarga desde la página de la ejecución, en GitHub Actions; trae `target/surefire-reports/` de los dos proyectos |

Otras propiedades del flujo:

- **Una ejecución por rama.** Un push nuevo cancela la ejecución anterior de la misma rama o solicitud de integración.
- **Solo lectura.** El flujo tiene permiso de lectura sobre el contenido y no conserva credenciales al clonar: no publica ni comenta nada.
- **Acciones fijadas por commit.** Cada `uses:` lleva el SHA y, en un comentario, la versión. Al actualizar una acción se cambian los dos.
- **Nombre del check.** El ruleset de `develop` y `main` exige el check `verify` en verde (`CONTRIBUTING.md`). Si se renombra el trabajo, hay que cambiar el ruleset.

## Archivos de entorno

| Archivo | Para qué |
| --- | --- |
| `.sdkmanrc` | Fija el JDK para quien usa SDKMAN (`sdk env install`). Es opcional: quien protege la versión es Enforcer. |
| `.mvn/wrapper/` | La versión de Maven que descarga `./mvnw`. |
| `.mvn/jvm.config` | Opciones `--add-exports` del compilador de Java. Las necesita google-java-format al correr dentro de Maven; sin ellas, Spotless falla. |
| `.editorconfig` | UTF-8, fin de línea LF y sangría de 2 espacios en Java. En Markdown conserva los espacios al final de la línea, que son un salto. |
| `.gitattributes` | Normaliza el fin de línea a LF, salvo los `.cmd`, que van en CRLF. |
| `.gitignore` | `target/`, `data/`, `*.mv.db`, `*.trace.db`, `.env` y los archivos del IDE. |
| `.jqwik-database` | Lo escribe jqwik para repetir primero las propiedades que fallaron. Está versionado; si cambia al correr las pruebas, no lo incluyas en tu commit. |
| `src/test/resources/archunit.properties` | `archRule.failOnEmptyShould=false`: una regla de arquitectura sobre un paquete que todavía no tiene clases no falla. |
