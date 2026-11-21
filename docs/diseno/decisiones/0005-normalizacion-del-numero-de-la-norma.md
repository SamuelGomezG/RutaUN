---
title: "0005 · Normalización del número de la norma"
doc_type: "decisión"
estado: "aceptada"
fecha: "2026-11-21"
srs_ids:
  - UC07 ext. 4c
  - UC07 paso 4
reemplaza: []
depends_on:
  - ../../requisitos/casos/UC07-incorporar-acuerdo-al-consolidado.md
  - ../../datos/formato-normativa.md
---

# 0005 · Normalización del número de la norma

## 0′ · Cuándo cargar este archivo

Antes de cambiar cómo se guarda o se compara el número de una norma. El contrato del JSON está en
[`formato-normativa.md`](../../datos/formato-normativa.md).

Fecha: 2026-11-21 · Estado: **aceptada**

## Pregunta

¿Cómo se guarda el número para que una misma norma escrita de varias formas se reconozca como ya
registrada (UC07 ext. 4c)?

## Respuesta

**Sin espacios, en mayúsculas y con un guion entre dígitos y letras.** El número se compacta y se
pasa a mayúsculas; si queda como dígitos, un guion opcional y letras, se guarda como
`dígitos-letras`. Así `003A`, `003 a`, `003-a` y ` 003-a ` quedan como `003-A`. Las demás formas se
conservan: `008` y `A-12` no cambian.

El número normalizado es parte de la identificación única junto con el tipo, el año y la clave del
órgano.

## Límites

- Se pierde la escritura original del número. El valor canónico es el que se muestra y el que se
  compara.
- No se unifican ceros a la izquierda: `8` y `008` siguen siendo números distintos.
- La normalización no decide sinónimos de órganos; eso es otra comparación.

## Referencias

- UC07 ext. 4c y UC07 paso 4.
- [`formato-normativa.md`](../../datos/formato-normativa.md), «`numero`».
- [`esquema-de-la-base.md`](../../datos/esquema-de-la-base.md), restricción `uq_norma_identificacion`.
