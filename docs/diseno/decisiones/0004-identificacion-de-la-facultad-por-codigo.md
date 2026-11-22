---
title: "0004 · Identificación de la facultad por código"
doc_type: "decisión"
estado: "aceptada"
fecha: "2026-11-21"
srs_ids:
  - UC07 ext. 4c
  - UC07 ext. 4h
  - S11
reemplaza: []
depends_on:
  - ../../requisitos/casos/UC07-incorporar-acuerdo-al-consolidado.md
  - ../../datos/formato-normativa.md
---

# 0004 · Identificación de la facultad por código

## 0′ · Cuándo cargar este archivo

Antes de cambiar cómo se identifica una facultad, cómo la citan las normas o cómo se resuelve un
alcance por facultad. El contrato del JSON está en
[`formato-normativa.md`](../../datos/formato-normativa.md).

Fecha: 2026-11-21 · Estado: **aceptada**

## Pregunta

¿Cómo se identifica una facultad para que una norma y un programa se refieran a la misma, si el
nombre se escribe de varias formas y la base no tiene una tabla de facultades?

## Respuesta

**Por un código estable.** `Facultad` lleva solo el código (por ejemplo, `ING`), sin espacios a los
lados y en mayúsculas. El archivo de programas declara el código y el nombre para mostrar; el nombre
vive en `Programa`, no en cada norma. Las normas citan la facultad por su código en el órgano y en el
alcance.

Consecuencias:

- Dos facultades con el mismo código son la misma, aunque el nombre se escriba distinto.
- Un alcance `FACULTADES` comprende los programas cuyo `facultad_codigo` coincide; no compara nombres.
- Al leer una norma no hace falta el nombre de la facultad, así que su reconstrucción no depende de
  otra tabla ni de otro archivo.

Alternativas descartadas:

- **Una tabla de facultades:** más alcance del que pide la historia y una carga más que mantener.
- **Guardar el nombre en cada norma:** duplica el dato y obliga a que dos archivos coincidan.

## Límites

- El código es interno de RutaUN; no es un código oficial del SIA. Si la Universidad publica uno, se
  cambia el valor en los archivos, no el diseño.
- El nombre para mostrar puede variar entre programas; la decisión no lo unifica.

## Referencias

- UC07 ext. 4c y ext. 4h, y el supuesto S11.
- [`formato-normativa.md`](../../datos/formato-normativa.md), «`programa`» y «`organo`».
- [`esquema-de-la-base.md`](../../datos/esquema-de-la-base.md), columnas `facultad_codigo` y
  `organo_facultad_codigo`.
