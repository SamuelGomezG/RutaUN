---
title: "0002 · Datos de prueba en la semilla (D-12)"
doc_type: "decisión"
estado: "aceptada"
fecha: "2026-11-14"
srs_ids:
  - UC01
  - UC03
  - UC05
  - UC07
reemplaza: []
depends_on:
  - ../../datos/formato-normativa.md
  - ../../requisitos/casos/UC01-generar-plan-de-carrera.md
  - ../../requisitos/glosario/02-normativa-consolidado.md
---

# 0002 · Datos de prueba en la semilla (D-12)

## 0′ · Cuándo cargar este archivo

Antes de cambiar los datos de prueba de `src/main/resources/semilla/`, de interpretar un plan
calculado con el perfil `dev` o de retirar un caso ficticio porque una norma real ya lo cubre. El
detalle del marcado y de cada archivo está en [`formato-normativa.md`](../../datos/formato-normativa.md),
«Datos de prueba de la semilla».

Fecha: 2026-11-14 · Estado: **aceptada**

## Pregunta

¿Dónde viven los casos que pide ENT-04 y que la normativa real de Sistemas todavía no puede
aportar —máximo por periodo, correquisito, regla temporal y vigencia programada—, y cómo se
clasifican?

## Respuesta

**Se acepta D-12: dos normas ficticias dentro de la semilla real, marcadas «(datos de prueba)», en
archivos `datos-de-prueba-*.json`.** El órgano de la norma y el enunciado y el objeto de cada regla
llevan la marca; `DatosDePruebaDeLaSemillaTest` vigila la convención. La alternativa —una carpeta
aparte con otro perfil— mantendría limpia la semilla real, pero exigiría cambiar
`SemillaDeDesarrolloRunner` y su prueba, y la demostración con el perfil `dev` no vería los casos.

**El máximo por periodo (25 créditos) y el correquisito son reglas del plan (`DEL_PLAN`).** Es lo
que ENT-04 pedía: que UC01, UC03 y UC05 tengan un tope y un correquisito con qué ejercitarse. La
regla temporal con «vigencia hasta» y la norma programada son informativas, porque una regla
temporal no puede ser del plan (UC07 ext. 6f).

## Consecuencias

- **Con el perfil `dev` se cargan, como reglas del plan, un máximo de 25 créditos y un correquisito
  inventados.** UC01, UC03 y UC05 deberán aplicarlos cuando se implementen; hoy el código principal
  todavía no lo hace. El correquisito exige Cálculo Diferencial (1000004) o Cálculo Diferencial en
  una Variable (2016377). El glosario dice que RutaUN no supone ningún máximo permanente: este tope
  es un dato cargado de prueba, no un supuesto del código (regla de oro 2).
- El correquisito cita las dos materias alternativas de la malla para no marcar un incumplimiento
  falso a quien curse la otra.
- **La demostración del sprint que use `dev` mostrará normas que no existen.** Se distinguen por la
  marca «(datos de prueba)» en el órgano y en cada regla.
- Los datos de prueba no traen reglas de estructura ni afectaciones: no mueven la versión del plan
  de estudios ni dependen de #175.
- Se retiran caso por caso cuando una norma real cubra el caso; si una norma ficticia se queda sin
  reglas, se borra su archivo. Retirar un archivo no limpia una base ya cargada: hay que borrar
  `data/` y volver a arrancar con el perfil `dev`.

## Límites

- Las vigencias van por fecha; los periodos académicos llegan con #141.
- Las afectaciones no se aplican todavía (#175).
- El estado se fija al importar y no se recalcula cuando pasa el tiempo (TT-04-T12).

## Referencias

- D-12, revisión del sprint 1 (documento interno del equipo); #133 y #51.
- Formato y archivos: [`formato-normativa.md`](../../datos/formato-normativa.md), «Datos de prueba
  de la semilla».
- Clasificación: [`02-normativa-consolidado.md`](../../requisitos/glosario/02-normativa-consolidado.md),
  «Regla» y «Tope de créditos».
