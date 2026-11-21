# Documentación de RutaUN

Mapa de `docs/` y reglas para escribir en él. Cada área tiene su propia entrada; este archivo solo dice a cuál ir y dónde va lo que quieras documentar. No repite el contenido de ninguna.

## Áreas

| Área | Qué responde | Empieza por |
| --- | --- | --- |
| [`requisitos/`](requisitos/README.md) | Qué debe hacer RutaUN: el SRS, fuente de los requisitos | Su `README.md`, que dice qué archivos cargar según la tarea |
| `diseno/` | Cómo está construido y por qué | Hoy solo [`decisiones/`](diseno/decisiones/); el resto del diseño se publica con [#176](https://github.com/SamuelGomezG/RutaUN/issues/176) y la guía de arquitectura con [#65](https://github.com/SamuelGomezG/RutaUN/issues/65) |
| `datos/` | Qué datos entran y cómo quedan guardados | [`formato-normativa.md`](datos/formato-normativa.md) (lo que entra) y [`esquema-de-la-base.md`](datos/esquema-de-la-base.md) (cómo queda) |
| `proceso/` | Cómo se construye, se prueba y se opera el proyecto | La tabla «Por tarea» de abajo |
| `uso/` | Cómo se usa, por rol | Todavía no existe; llega con [#66](https://github.com/SamuelGomezG/RutaUN/issues/66) |

Fuera de `docs/`: [`README.md`](../README.md) (qué es y cómo arrancarlo), [`AGENTS.md`](../AGENTS.md) (reglas para programar), [`CONTRIBUTING.md`](../CONTRIBUTING.md) (flujo de trabajo) y [`spikes/tt-05-motor/README.md`](../spikes/tt-05-motor/README.md) (el prototipo del motor, que no es parte de la aplicación).

## Por tarea

| Vas a… | Lee |
| --- | --- |
| Implementar o revisar un caso de uso o una historia | [`requisitos/README.md`](requisitos/README.md) |
| Arrancar el proyecto, entender un perfil o lo que hace `./mvnw verify` | [`proceso/entorno-y-configuracion.md`](proceso/entorno-y-configuracion.md) |
| Resolver un fallo del build, del arranque o de la CI | [`proceso/solucion-de-problemas.md`](proceso/solucion-de-problemas.md) |
| Crear un módulo, una migración, una norma de la semilla, un incidente o conectar la comprobación de versión de un caso que guarda | [`proceso/recetas.md`](proceso/recetas.md) |
| Escribir o mover una prueba | [`proceso/pruebas.md`](proceso/pruebas.md) |
| Escribir o leer un JSON de normativa | [`datos/formato-normativa.md`](datos/formato-normativa.md) |
| Consultar la base o cambiar una tabla | [`datos/esquema-de-la-base.md`](datos/esquema-de-la-base.md) |
| Tocar el cálculo del plan | [`diseno/decisiones/0001-motor-del-calculo-del-plan.md`](diseno/decisiones/0001-motor-del-calculo-del-plan.md) |
| Cambiar dependencias o migrar código pensado para Boot 3 | [`proceso/cambios-de-boot-4.md`](proceso/cambios-de-boot-4.md) |
| Abrir o revisar una solicitud de integración | [`CONTRIBUTING.md`](../CONTRIBUTING.md) y [`proceso/definicion-de-hecho.md`](proceso/definicion-de-hecho.md) |

## Dónde va lo que quieras escribir

Cada cosa vive en un solo lugar. Si ya existe en otro documento, enlázalo.

| Lo que tienes | Va en | Cómo |
| --- | --- | --- |
| Un requisito, un supuesto o algo que el SRS no define | `requisitos/` (`04-supuestos.md`, `07-lagunas.md` o el caso de uso) | Por solicitud de integración; cada ID tiene un solo dueño (`requisitos/05-trazabilidad.md`) |
| Una decisión de diseño y su porqué | `diseno/decisiones/`, un archivo numerado por decisión | Con los IDs del SRS que toca; la convención de sus metadatos se fija con [#191](https://github.com/SamuelGomezG/RutaUN/issues/191) |
| La estructura del código: contextos, capas, clases | `diseno/` | Según la estructura de [#176](https://github.com/SamuelGomezG/RutaUN/issues/176) |
| Una regla para programar que aplica en cada tarea | [`AGENTS.md`](../AGENTS.md) | Corta; el detalle va en `diseno/` y se enlaza |
| Una regla de cómo trabajamos: ramas, commits, revisión | [`CONTRIBUTING.md`](../CONTRIBUTING.md) | — |
| Un formato de datos o la forma de las tablas | `datos/` | Con un ejemplo que se pueda ejecutar o cargar |
| Cómo se construye, se prueba, se configura o se diagnostica | `proceso/` | Con comandos que se puedan copiar |
| Un recorrido para quien usa la aplicación | `uso/` | Por rol |
| Un plan de trabajo, el resultado de una revisión interna o el reparto de un sprint | No van en el repositorio | Lo que deba durar se lleva a su sitio de esta tabla, y los hallazgos se citan por su issue |

## Cómo se escribe un documento

1. **Metadatos y «Cuándo cargar».** Cada archivo abre con *frontmatter* (`title`, `doc_type` y `depends_on` con los archivos que hay que haber leído antes) y una sección «0′ · Cuándo cargar este archivo» de dos o tres líneas. Quien llega, persona o agente de código, decide con eso si lo abre. Los `README.md` que solo enrutan no los llevan.
2. **Una sola fuente.** Antes de escribir un dato, busca si ya vive en otro archivo. El resumen de un tema en otro documento es una frase y un enlace.
3. **Lo derivado se declara.** Un documento que describe algo cuya fuente es el código (una tabla, una configuración, una ruta) dice cuál es esa fuente y cómo comprobarlo. Si difieren, manda la fuente.
4. **IDs.** Lo que implementa o explica un requisito cita su ID (`UC07 ext. 5a`, `S3`) y lo pendiente cita su issue (`#141`). «Pendiente» sin issue no dice quién lo hará.
5. **Español y glosario.** El vocabulario de `requisitos/glosario/`, tal cual (regla de oro 6).
6. **Sin datos personales ni nombres de personas**, tampoco en los ejemplos (regla de oro 7).
7. **Diagramas en Mermaid**, dentro del Markdown: GitHub los dibuja y el historial muestra qué cambió.
8. **Cortos.** Un archivo que crece se parte por tema y su carpeta gana un `README.md` que enruta, como `requisitos/`.
9. **Se actualiza con el cambio.** La solicitud de integración que cambia un comportamiento actualiza el documento que lo describe, en el mismo commit (`CONTRIBUTING.md`, «Commits»). En un cambio que solo toca documentación se comprueba que los enlaces estén vivos.
