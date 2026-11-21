---
title: "0003 · Comprobación de versión antes de guardar en compartido (SOP-07)"
doc_type: "decisión"
estado: "aceptada"
fecha: "2026-11-20"
srs_ids:
  - UC01 ext. 10a
  - UC01 ext. 10c
  - UC02 ext. 11a
  - UC02 ext. 11c
  - UC03 ext. 7a
  - UC03 ext. 7b
  - UC03 ext. 7c
  - UC04 ext. 10a
  - UC04 ext. 10c
reemplaza: []
depends_on:
  - ../../requisitos/casos/UC01-generar-plan-de-carrera.md
---

# 0003 · Comprobación de versión antes de guardar en compartido (SOP-07)

## 0′ · Cuándo cargar este archivo

Antes de conectar la comprobación de versión a un caso que guarda, o de agregar o cambiar clases de
esa comprobación en `compartido`. Los pasos para conectarla están en
[`recetas.md`](../../proceso/recetas.md), «Conectar la comprobación de versión de un caso que
guarda».

Fecha: 2026-11-20 · Estado: **aceptada**

## Pregunta

¿En qué módulo vive la comprobación de versión antes de guardar (SOP-07, #59), si la usan casos de
uso de contextos distintos (UC01 a UC04) y el paquete `compartido` está descrito como «los valores
que todos los contextos importan y que cambian casi nunca»?

## Respuesta

**Vive en `compartido`, como excepción a esa descripción.** Son cinco clases:
`ComponenteVersionado`, `FuenteDeVersion`, `HuellaDeVersion`, `ResultadoDeComprobacion` y
`ComprobacionDeVersion`. Es un mecanismo sin Spring que no conoce ningún contexto: cada caso le
entrega sus fuentes, que devuelven el valor actual de cada componente como texto, y la comprobación
solo compara la huella tomada al cargar con la de justo antes de guardar. El resultado es sellado,
así que cada caso debe decidir qué hacer si algo cambió.

Alternativas descartadas:

- **En un contexto (Avance, Plan de carrera…):** los otros casos tendrían que depender de él solo
  para comprobar una versión, y el mecanismo no es de ninguno.
- **Una comprobación en cada caso:** repite la lógica que #59 pide no repetir.

El enum `ComponenteVersionado` nombra conceptos de Avance, Plan de carrera, Normativa y Periodo en
curso, pero solo los nombra: el valor de cada uno lo lee la fuente que arma cada caso.

## Consecuencias

- `compartido` es un módulo abierto: los contextos no declaran dependencia para usar la
  comprobación.
- La comprobación solo detecta. Qué mensaje se da y qué se hace con lo que el estudiante tenía en
  pantalla lo decide cada caso (UC01, UC02 y UC04 descartan lo mostrado y ofrecen recalcular; UC03 no guarda nada: en la 7a y la 7c conserva en pantalla los cambios sin guardar del estudiante, y en la 7b descarta y señala solo los que recaen en materias que ya no rigen)
- No reemplaza el `@Version` de JPA, que sigue protegiendo frente a dos guardados simultáneos.
- Los criterios de cada caso viven en sus historias (#4, #25 y las de UC02 y UC03), no en #59.

## Límites

- La huella no se escribe en los logs ni se guarda más allá de la sesión (regla de oro 7).
- Un componente nuevo se agrega al enum y exige que cada caso que lo use arme su fuente.

## Referencias

- SOP-07 (#59) y su PR.
- Receta: [`recetas.md`](../../proceso/recetas.md), «Conectar la comprobación de versión de un caso
  que guarda».
- Invariante «Comprobación previa al guardar», [`01-invariantes.md`](../../requisitos/01-invariantes.md) §4.4.
- Descripción del paquete: `package-info.java` de `compartido`.
