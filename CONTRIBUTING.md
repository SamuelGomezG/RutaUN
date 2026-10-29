# Guía de contribución

Esta guía explica cómo trabajamos en RutaUN: cómo tomar un issue, nombrar las ramas, escribir los commits, abrir una solicitud de integración (PR) y revisar la de otra persona. Las reglas de código (capas, nombres, pruebas) están en [`AGENTS.md`](AGENTS.md); aquí está el flujo.

> [!NOTE]
> La integración continua corre con la acción [`verify`](.github/workflows/ci.yml): compila, verifica formato y estilo y corre todas las pruebas en cada PR y en cada push a `develop` y `main`. La protección de ramas (el ruleset «critical branches protection») exige, para fusionar en esas dos ramas, un PR con la aprobación de otra persona, sus conversaciones resueltas y el check `verify` en verde.

### Tópicos

- [Antes de empezar](#antes-de-empezar)
- [Flujo de trabajo](#flujo-de-trabajo)
- [Ramas](#ramas)
- [Commits](#commits)
- [Hooks de git](#hooks-de-git)
- [Solicitudes de integración](#solicitudes-de-integración)
- [Revisión](#revisión)
- [Tipos de issue y sub-issues](#tipos-de-issue-y-sub-issues)
- [Requisitos y documentación](#requisitos-y-documentación)
- [Qué no subir](#qué-no-subir)

## Antes de empezar

1. Lee el `README.md` y [`AGENTS.md`](AGENTS.md): trae las reglas de oro, la estructura de carpetas y las convenciones de código.
2. Los requisitos están en `docs/requisitos/`; empieza por su `README.md`, que dice qué archivos cargar según tu tarea.
3. Prepara el entorno: un JDK 21 (con SDKMAN, `sdk env install`) y el wrapper de Maven (`./mvnw`).
4. Activa la plantilla de commits y los [hooks de git](#hooks-de-git), una vez por clon: `git config commit.template .gitmessage` y `git config core.hooksPath .githooks`.
5. Escoge un issue del tablero «RutaUN», de la iteración en curso. Asígnatelo y pásalo a *In Progress*.

## Flujo de trabajo

Un issue, una rama, una solicitud de integración.

1. Actualiza `develop` y crea tu rama desde ahí.
2. Haz commits atómicos.
3. Antes de abrir el PR, si cambiaste código, corre `./mvnw verify` (debe quedar en verde); si el cambio es solo de documentación, comprueba que los enlaces estén vivos. En ambos casos, ejecuta tu cambio siguiendo los pasos que vas a poner en el PR.
4. Abre el PR contra `develop` y pasa el issue a *In Review*.
5. Otra persona lo revisa: ejecuta el cambio, comprueba el resultado y lee el código. Respondes a sus comentarios con commits nuevos.
6. Con la aprobación y la integración continua en verde, quien abrió el PR lo fusiona en `develop` con **Rebase and merge**, para conservar los commits atómicos; no uses *Squash*. Nadie fusiona un PR sin la aprobación de otra persona.
7. El issue se cierra con el PR (`Closes #n`), porque `develop` es la rama predeterminada del repositorio. Pasa a *Done* cuando los criterios de aceptación se demostraron funcionando.

## Ramas

| Rama | Para qué | Quién escribe en ella |
| --- | --- | --- |
| `main` | Lo que se mostró y se dio por satisfactorio al cierre de cada sprint | Solo el PR de promoción desde `develop` |
| `develop` | Integración: todo el trabajo del sprint en curso. Es la rama predeterminada del repositorio | Los PR de las ramas de trabajo |
| Ramas de trabajo | Un issue cada una | Quien toma el issue |

Las ramas de trabajo salen de `develop` y vuelven a `develop` por PR. Nadie hace commit directo en `develop` ni en `main`.

El nombre de una rama de trabajo es el ID del issue en minúsculas, un guion y un resumen corto:

```
uc06-hu01-reglas-del-programa
tt-02-nucleo-estructura
doc-03-guia-de-contribucion
uc01-hu01-t2-formulario-del-tope      # sub-issue
```

### Promoción de `develop` a `main`

Al cierre de cada sprint, en la demostración, el equipo decide si el sprint está completo y satisfactorio. Si lo está:

1. `develop` tiene la integración continua en verde y los elementos comprometidos cumplen la [Definición de Hecho](docs/proceso/definicion-de-hecho.md).
2. Se abre un PR de `develop` a `main`, titulado `Sprint N`, con la lista de issues terminados y lo que se mostró en la demostración. Lo aprueba otra persona. Tiene su propia plantilla, [`promocion-de-sprint.md`](.github/PULL_REQUEST_TEMPLATE/promocion-de-sprint.md), que GitHub no ofrece sola: se abre con `gh pr create --base main --head develop --title "Sprint N" --body-file .github/PULL_REQUEST_TEMPLATE/promocion-de-sprint.md`.
3. Se fusiona con **Create a merge commit** (ni *Rebase* ni *Squash*), para que `main` tenga una marca por sprint.
4. Se etiqueta la fusión como `sprint-N` y `develop` se alinea con `main` (`git switch develop && git merge --ff-only main`).

Si el sprint no se da por satisfactorio, `main` no cambia: el trabajo sigue en `develop` y se promueve cuando lo esté. Una corrección que necesite `main` también se hace en `develop` y llega con la siguiente promoción.

## Commits

Con `git commit` (sin `-m`) se abre tu editor con la plantilla [`.gitmessage`](.gitmessage): sus líneas con `#` te guían y no se guardan. Con `-m` la plantilla no aparece, así que el formato de abajo sigue valiendo.

Un commit es **atómico** cuando contiene un solo cambio lógico, deja el proyecto en un estado válido (compila y pasa las pruebas, o, si es documentación, con los enlaces vivos) y se puede revertir sin deshacer nada más. En la práctica:

- Un cambio de comportamiento lleva en el mismo commit su prueba y el documento que describe el cambio.
- Un refactor no se mezcla con un cambio de comportamiento.
- El formato lo aplica Spotless (`./mvnw spotless:apply`); no hace falta un commit aparte para eso.

El mensaje tiene este formato:

```
<ID>: <resumen en infinitivo, hasta 72 caracteres>

<Cuerpo opcional: qué cambia y por qué, no cómo.>

Refs #<n>
```

- `<ID>` es el del issue: `UC06-HU01`, `TT-02`, `DOC-03`, `UC01-HU01-T2`.
- El commit cita su issue con `Refs #n`. El cierre (`Closes #n`) va en la descripción del PR, no en los commits.
- Si el cambio toca una extensión o un paso del SRS, nómbralo en el cuerpo (`UC01 ext. 4a`).

Ejemplos:

```
UC06-HU01: mostrar las reglas del programa por categoría

Agrupa las reglas vigentes y programadas y muestra su estado y la etiqueta
«temporal» (UC06 paso 4).

Refs #34
```

```
TT-02: modelar la versión del consolidado como raíz de solo agregar

Refs #49
```

No uses mensajes como «arreglos», «wip» o «cambios», ni mezcles temas distintos en un commit.

## Hooks de git

Los hooks de [`.githooks/`](.githooks) revisan tu trabajo en tu máquina antes de que el commit exista y antes de que se empuje. Se activan una vez por clon (paso 4 de [Antes de empezar](#antes-de-empezar)).

| Hook | Cuándo corre | Qué comprueba | Tarda |
| --- | --- | --- | --- |
| `commit-msg` | Al guardar el mensaje | El formato de arriba: `<ID>: ` y un verbo en infinitivo, asunto de hasta 72 caracteres sin punto final, línea 2 vacía, cuerpo de hasta 72 caracteres por línea, pie `Refs #n` y ningún `Closes #n`. No revisa los mensajes que genera git (merge, revert, fixup y squash) | Al instante |
| `pre-commit` | Antes de crear el commit | Que no estés en `develop` ni en `main`; que no subas `data/`, `*.mv.db`, `.env` ni claves privadas; y, si el commit toca `src/`, `config/`, `.mvn/`, `pom.xml` o `mvnw`, que `./mvnw verify` termine en verde (formato, estilo, arquitectura y pruebas). Si solo cambias documentación, no corre Maven | Unos 11 s si hay código |
| `pre-push` | Antes de empujar | Si lo que empujas toca esos mismos archivos de código y build, que `./mvnw verify` termine en verde. Cubre lo que `pre-commit` no ve: los commits que un rebase reaplica y los hechos con `--no-verify`. Solo mira ramas (no etiquetas ni borrados) y compara con lo que el remoto ya tiene, o con `origin/develop` si la rama es nueva. Si empujas una rama distinta de la actual, avisa y no la verifica. | Unos 11 s si hay código |

- **Son una ayuda, no la barrera.** La CI y el ruleset de GitHub repiten la comprobación antes de fusionar. En una emergencia, `git commit --no-verify` y `git push --no-verify` los saltan. Conviene no hacerlo: un push que falla la CI descarta las aprobaciones del PR, y `pre-push` existe para evitarlo.
- **Si falla el formato**, ejecuta `./mvnw spotless:apply`, vuelve a preparar los archivos con `git add` y repite el commit. Si falla el mensaje, el propio hook te da el comando para reabrirlo sin perderlo.
- **`verify` revisa todo tu árbol de trabajo**, no solo lo que preparaste con `git add`; `pre-commit` avisa si hay cambios de código sin preparar y `pre-push`, si hay sin confirmar.
- **Viven en el árbol de trabajo.** En una rama que no contenga `.githooks/` (por ejemplo, una creada antes de que se fusionaran), git no ejecuta nada y no avisa: rebásala sobre `develop`.
- **Necesitan JDK 21** en el entorno desde el que haces el commit o el push. Si lo haces desde un IDE con otro `JAVA_HOME`, el Enforcer de Maven lo dirá.
- **En Windows** deberían funcionar con Git Bash, que viene con Git for Windows, pero no se han probado ahí.
- **Probarlos:** `.githooks/probar.sh` ejercita los tres con casos buenos y malos, sin tocar tu repositorio. Si cambias un hook, cambia ahí sus casos.
- Si `verify` pasa de 30 s, conviene dejarlo solo en `pre-push` y quitarlo de `pre-commit`; el hook te avisa cuando ocurra.

## Solicitudes de integración

- **Cortas.** Si no se explica en tres líneas o pide más de 3 puntos de esfuerzo, se parte.
- **Título:** el del issue, por ejemplo `UC06-HU01 · Elegir un programa y ver sus reglas`.
- **Descripción:** GitHub la abre con la plantilla [`.github/pull_request_template.md`](.github/pull_request_template.md); llénala completa y borra solo los comentarios. Pide:
  - qué cambia y por qué;
  - `Closes #n` si el PR termina el issue, o `Refs #n` si lo deja abierto;
  - los pasos, extensiones o criterios del SRS que cubre;
  - cómo ejecutarlo: los pasos para recrear el flujo y el resultado que se espera ver (la aplicación, una prueba de aceptación, un comando o un script);
  - la lista de la [Definición de Hecho](docs/proceso/definicion-de-hecho.md);
  - si cambió un comportamiento, qué documento actualizó.
- Verifica antes de pedir revisión: si cambiaste código, `./mvnw verify` en verde; si es solo documentación, los enlaces vivos. En ambos casos, el diff sin credenciales, datos personales ni archivos locales.

## Revisión

La revisa una persona distinta de quien escribió el cambio. Revisa:

- que el cambio **se ejecuta**: sigue los pasos del PR, recrea el flujo y comprueba que el resultado es el esperado; si no puedes reproducirlo, el PR no está listo;
- los **criterios de aceptación** del issue, probándolos en la aplicación si es una historia;
- que cada prueba lleve en su nombre el ID del SRS que verifica;
- que se respeten las **reglas de oro** de [`AGENTS.md`](AGENTS.md): normas como datos, el «Aviso de lo que RutaUN no verifica», el vocabulario del glosario y la privacidad;
- que las pruebas de arquitectura sigan en verde y el estilo esté aplicado;
- que la documentación afectada esté actualizada.

Quien revisa aprueba, pide cambios o comenta. Quien escribió el cambio responde a cada comentario antes de fusionar.

## Tipos de issue y sub-issues

| Tipo | Título | Etiquetas |
| --- | --- | --- |
| Historia | `UCnn-HUmm · <Título>` | `UCnn` |
| Tarea técnica | `TT-nn · <Título>` | `tarea-tecnica` |
| Spike (con tiempo máximo) | `TT-nn · <Título>` | `spike` |
| Soporte, pruebas y documentación | `SOP-nn`, `PRU-nn`, `DOC-nn` | `sop`, `pru`, `doc` |
| Sub-issue de una historia | `UCnn-HUmm-Tk · Verbo + objeto` | `UCnn` |
| Defecto | `Defecto · UCnn-HUmm · Qué falla` | `defecto` |

Al crear un issue, GitHub ofrece una plantilla por tipo (en [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE)): sub-issue, tarea (TT, SOP, PRU y DOC), spike y defecto. No hay plantilla de historia ni issue en blanco: las historias salen del SRS, así que primero se cambia `docs/requisitos/` por PR (ver [Requisitos y documentación](#requisitos-y-documentación)).

Los issues del sprint llevan además `sprint-1` (comprometido) o `sprint-1-adicional`; los puntos, el carril y la iteración viven en los campos `Puntos`, `Carril` y `Sprint` del tablero.

Una historia se **parte en sub-issues** solo cuando entra a la iteración en curso o a la siguiente. El título es `UCnn-HUmm-Tk · Verbo + objeto` (la `T` evita confundirlo con los supuestos S1–S18). Cada sub-issue:

- vale como máximo 3 puntos;
- se parte por criterio, no por capa: cada extensión del SRS suele ser uno;
- cita los pasos, extensiones y garantías que cubre;
- lo compartido entre dos historias se construye una sola vez y ambas dependen de ello.

Las estimaciones usan la escala de Fibonacci y se acuerdan con *planning poker*. Lo que supere 8 puntos se parte.

## Requisitos y documentación

- **Los requisitos se cambian aquí**, por PR, en `docs/requisitos/`: ese árbol es la fuente de verdad. Cada ID aparece exactamente una vez como texto normativo (los dueños están en su `05-trazabilidad.md`).
- Si el SRS calla, no decidas en silencio: revisa `04-supuestos.md` y `07-lagunas.md`; si tampoco está, pregunta en el issue o registra un supuesto nuevo marcado como tal.
- La documentación va en español, con el vocabulario del glosario tal cual.
- El PR que cambia un comportamiento actualiza el documento que lo describe.

## Qué no subir

Credenciales, el contenido de `data/`, datos personales (de estudiantes reales, en pruebas o en la semilla) y archivos locales del IDE.
