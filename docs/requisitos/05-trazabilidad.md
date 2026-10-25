---
title: "Trazabilidad y backlog"
doc_type: "trazabilidad"
srs_ids:
  - S1..S18
  - UC01..UC07
  - §12
depends_on:
  - 00-contexto.md
  - 01-invariantes.md
  - 03-marco-normativo.md
  - 04-supuestos.md
  - 90-modelo-de-dominio.md
  - glosario/README.md
  - casos/README.md
  - 06-evidencia-y-ux.md
  - 07-lagunas.md
---
# 12. Backlog en GitHub Projects

> **Cuándo cargar:** al abrir issues, PRs o commits, para mappear IDs y números, y al revisar si algún ID quedó huérfano.

## Regla de dueño único por ID

Cada ID (`UC01 ext. 4b`, `UC03-HU05`, `S7`… `S18`, `#17`, `[I-n]`) aparece como texto normativo **exactamente una vez** en este árbol. Los índices y enlaces no cuentan como aparición normativa. Excepción declarada: la leyenda de lectura de los casos (§9 del origen) se replica verbatim en `casos/README.md` y en los 7 archivos de caso (8 copias, hash-iguales).

El backlog contiene las **47 historias de usuario** de §11.

- **Proyecto:** `https://github.com/users/SamuelGomezG/projects/3` (tablero «RutaUN», *View 1*; repositorio `SamuelGomezG/RutaUN`, público). El docente está invitado como colaborador.
- **Estado de las 47 historias:** todas en estado **Todo** y asignadas a `SamuelGomezG`. Por caso de uso: UC01: 7 · UC02: 5 · UC03: 9 · UC04: 5 · UC05: 7 · UC06: 5 · UC07: 9.
- **Formato de cada *issue*** (Figura 5.4, p. ej. #1): título `UCnn-HUmm · <Título>`; cuerpo con las secciones **Descripción** (resumen de una línea), **Historia** (Como… quiero… para…), **Criterios de aceptación** (lista) y **Caso de uso relacionado**; **etiqueta** = `UCnn`; **hito (*milestone*)** = «UCnn · Nombre del caso»; proyecto «RutaUN»; estado «Todo».
- **Numeración:** el número del *issue* sigue el orden del tablero: #1–#7 = UC01-HU01…HU07; #8–#12 = UC02; #13–#21 = UC03; #22–#26 = UC04; #27–#33 = UC05; #34–#38 = UC06; #39–#47 = UC07 (ver cada historia en §11).

| Hito / etiqueta | Issues | Historias |
|---|---|---|
| UC01 · Generar plan de carrera | #1–#7 | UC01-HU01 a HU07 |
| UC02 · Replanificar plan de carrera | #8–#12 | UC02-HU01 a HU05 |
| UC03 · Registrar avance en la malla | #13–#21 | UC03-HU01 a HU09 |
| UC04 · Comparar alternativas de carga | #22–#26 | UC04-HU01 a HU05 |
| UC05 · Entender restricciones de una materia | #27–#33 | UC05-HU01 a HU07 |
| UC06 · Consultar regla del programa y su acuerdo | #34–#38 | UC06-HU01 a HU05 |
| UC07 · Incorporar acuerdo al consolidado del programa | #39–#47 | UC07-HU01 a HU09 |
