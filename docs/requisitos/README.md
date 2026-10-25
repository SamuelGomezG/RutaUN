# Requisitos de RutaUN (SRS), segmentados para agentes de código

> **Fuente de verdad:** este árbol es la fuente de los requisitos de RutaUN y se cambia por solicitud de integración (versión del 25 de octubre de 2026; ver «Versión» al final).
> Doble fuente de verdad prohibida, salvo la leyenda de lectura de los casos (8 copias hash-iguales, ver `05-trazabilidad.md`).

## Reglas de oro (síntesis; la letra completa está en `00-contexto.md` §0)

1. Traza todo a IDs: `UCnn`, `UCnn-HUmm`, `UCnn ext. 4b`, `UCnn GM n` / `GE n`, `[I-n]`, `S1–S18`, `#n`.
2. Las normas académicas son **datos** del consolidado (UC07), no constantes de código; se digitan o se cargan por JSON.
3. Cuando el SRS calla, no decidas en silencio: mira `04-supuestos.md` y `07-lagunas.md`; si tampoco está, pregunta.
4. Respeta el fuera de alcance (§2.2 de `00-contexto.md`).
5. Nunca presentes un plan como inscripción ni autorización: todo plan lleva el aviso de lo que RutaUN no verifica.
6. Vocabulario del glosario (§7), en español, tal cual en UI y docs.
7. Privacidad por diseño: avance y planes son datos personales (Ley 1581 de 2012).

## Mapa de archivos

| Archivo | Papel | Cuándo | Tokens |
|---|---|---|---|
| [`00-contexto.md`](00-contexto.md) | Resumen, alcance, actores y reglas de trabajo | siempre, primero | ~4299 |
| [`01-invariantes.md`](01-invariantes.md) | Las cosas que nunca puede violar la implementación | siempre | ~2091 |
| [`03-marco-normativo.md`](03-marco-normativo.md) | Normas de ejemplo (datos, no constantes) | con UC07 o al revisar vigencias | ~1365 |
| [`04-supuestos.md`](04-supuestos.md) | S1–S18: decisiones del equipo y cotas de tiempo | antes de cálculo o validación | ~1797 |
| [`05-trazabilidad.md`](05-trazabilidad.md) | Backlog GitHub y dueños de IDs | issues/PRs y mapeo de IDs | ~905 |
| [`06-evidencia-y-ux.md`](06-evidencia-y-ux.md) | Acta ZEUS-02 (evidencia, no requisito) | contexto y decisiones de producto | ~2152 |
| [`07-lagunas.md`](07-lagunas.md) | Lo que el SRS no define; incluye pautas de UI (§14.2) | ¿falta algo? mira aquí y pregunta | ~1152 |
| [`90-modelo-de-dominio.md`](90-modelo-de-dominio.md) | Mapa de entidades orientativo (prefijo 90 = derivado) | modelado; manda el glosario | ~1159 |
| [`glosario/README.md`](glosario/README.md) | índice A-Z de los 71 términos | lookup | ~2 k |
| [`glosario/01-producto-actores-tiempo.md`](glosario/01-producto-actores-tiempo.md) | Administrador de normativa … Visitante (11 términos) | lookup por dominio | ~2237 |
| [`glosario/02-normativa-consolidado.md`](glosario/02-normativa-consolidado.md) | Acuerdo (o norma) … Vigencia (18 términos) | lookup por dominio | ~6610 |
| [`glosario/03-malla-materias.md`](glosario/03-malla-materias.md) | Agrupación … Restricción (de una materia) (19 términos) | lookup por dominio | ~4487 |
| [`glosario/04-avance.md`](glosario/04-avance.md) | Aprobada … Semestres cursados (8 términos) | lookup por dominio | ~2051 |
| [`glosario/05-calculo-plan.md`](glosario/05-calculo-plan.md) | Alternativa de carga … Tope de carga (15 términos) | lookup por dominio | ~4090 |

## Tareas → archivos (router)

| Tarea | Cargar |
|---|---|
| Implementar o revisar `UCnn` | `casos/README.md` + `casos/UCnn-…md` + `01-invariantes.md` + `04-supuestos.md` + los 5 del `glosario/` |
| Modelar dominio / persistencia | router de UC + `90-modelo-de-dominio.md` |
| Motor del plan (UC01/UC02/UC04) | casos + `glosario/05-calculo-plan.md` + `glosario/02-normativa-consolidado.md` |
| Carga de normativa (UC07) | casos + `glosario/02-normativa-consolidado.md` + `glosario/03-malla-materias.md` + `03-marco-normativo.md` |
| Consultas sin sesión (UC05/UC06) | casos + `glosario/02-normativa-consolidado.md` + `glosario/03-malla-materias.md` |
| UI / presentación | casos + `07-lagunas.md` (§14.2) y `casos/README.md` |
| Issues, commits, PRs | `05-trazabilidad.md` |
| Cerrar hueco o supuesto | `04-supuestos.md` + `07-lagunas.md` |

## Convenciones de IDs

- `UCnn` caso de uso · `UCnn-HUmm` historia de usuario · `#n` issue GitHub
- `UCnn paso n` · `UCnn ext. na` · `UCnn GM n` · `UCnn GE n` · `[I-n]` interesado (local al caso)
- `S1–S18` supuestos del equipo (`04-supuestos.md`; S18 = decisión del 25-oct)

## Contenido

- 23 archivos, incluido este README.
- Glosario: **71 términos** (70 de la Entrega 2 + «Correquisito» de la conciliación) en 5 archivos por dominio + índice A-Z.
- S1–S18 en `04-supuestos.md`; 47 historias (#1–#47) dentro de su UC; casos de uso verbatim.
- Sin datos personales del acta ZEUS-02.

## Versión

Versión del 25 de octubre de 2026: conciliada con el diseño (cambios 1 a 8 aplicados; el 8, como S18). Primera publicación en el repositorio, donde pasa a ser la fuente de los requisitos.
