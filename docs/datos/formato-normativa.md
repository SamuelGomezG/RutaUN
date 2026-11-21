---
title: "Formato del JSON de normativa"
doc_type: "datos"
depends_on:
  - ../requisitos/casos/UC07-incorporar-acuerdo-al-consolidado.md
---

# Formato del JSON de normativa

## 0′ · Cuándo cargar este archivo

Antes de leer o escribir el JSON de UC07-HU03, al cambiar el contrato y al
agregar fixtures en `src/test/resources/json/`. El estado de cada regla no se
digita: lo calcula el importador.

Este documento fija el contrato del archivo JSON con el que UC07 incorpora una norma y sus reglas
al consolidado de un programa. Es la referencia para el lector de UC07-HU03-T2 y para los archivos
de prueba de `src/test/resources/json/`.

- Versión del formato: `"1.0"`.
- Un archivo contiene **una norma**, **un programa** y **sus reglas**.
- Los archivos reales no llevan comentarios: el ejemplo comentado de abajo es solo documentación.
- Los datos de ejemplo son sintéticos; no son una transcripción normativa.

## Estructura general

```jsonc
{
  "schemaVersion": "1.0", // versión del contrato; hoy solo existe 1.0
  "programa": {           // el programa al que se incorpora la norma
    "codigoSia": "2541",
    "nombre": "Ingeniería de Sistemas y Computación",
    "facultad": "Facultad de Ingeniería"
  },
  "norma": {
    "tipo": "ACUERDO",
    "numero": "003-A",
    "anio": 2022,
    "organo": "Consejo Académico",
    "tipoDeEfecto": "BASE", // BASE, MODIFICATORIA o EXCEPCION_O_MEDIDA_TEMPORAL
    "fechaExpedicion": "2022-05-25",
    "vigencia": {
      "aplicaDesde": { "tipo": "PERIODO", "periodo": "2022-2" },
      "vigenteHasta": null // null si no tiene fin
    },
    "enlaceRegimenLegal": null,
    "alcance": {
      "tipo": "PROGRAMAS",
      "facultades": [],
      "programas": ["2541"], // códigos SIA
      "exclusiones": [],
      "texto": "El presente acuerdo aplica al programa de Ingeniería de Sistemas y Computación."
    }
  },
  "reglas": [ // al menos una regla; el estado no se escribe
    {
      "categoria": "MATERIA", // debe coincidir con datos.tipo
      "objeto": "2024042 Cálculo diferencial",
      "enunciado": "Cálculo diferencial pertenece a la agrupación Matemáticas.",
      "articulo": "art. 2",
      "vigenciaPropia": null, // si existe, usa la forma de vigencia
      "clasificacion": "DEL_PLAN", // DEL_PLAN o INFORMATIVA
      "afectacion": null, // opcional; ver abajo
      "datos": {
        "tipo": "MATERIA",
        "materia": {
          "codigo": "2024042",
          "nombre": "Cálculo diferencial",
          "creditos": 4,
          "obligatoriedad": "OBLIGATORIA",
          "semestreSugerido": 1
        },
        "contenedor": {
          "componente": "Componente de Fundamentación",
          "agrupacion": "Matemáticas"
        }
      }
    }
  ]
}
```

## Campos de primer nivel

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `schemaVersion` | texto | Sí | Versión del contrato; para este formato es `"1.0"`. |
| `programa` | objeto | Sí | Programa al que se incorpora la norma. |
| `norma` | objeto | Sí | Norma que se incorpora. |
| `reglas` | arreglo | Sí | Al menos una regla; cada elemento es una regla de la norma. |

### `programa`

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `codigoSia` | texto | Sí | Código SIA del programa; identifica el programa en el archivo. |
| `nombre` | texto | Sí | Nombre del programa. |
| `facultad` | texto | Sí | Nombre de la facultad a la que pertenece. |

La importación resuelve el programa por `codigoSia` contra los programas ya cargados. El lector no
crea programas ni identificadores internos: el archivo no lleva UUID y los identificadores los
genera o resuelve RutaUN en pasos posteriores.

### `norma`

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `tipo` | texto | Sí | `ACUERDO`, `RESOLUCION` o `CIRCULAR`. |
| `numero` | texto | Sí | Número tal como lo rotula la norma. |
| `anio` | entero | Sí | Año positivo de expedición. |
| `organo` | texto | Sí | Órgano que la expide. |
| `tipoDeEfecto` | texto | Sí | `BASE`, `MODIFICATORIA` o `EXCEPCION_O_MEDIDA_TEMPORAL`. |
| `fechaExpedicion` | fecha | Sí | Formato `AAAA-MM-DD`. |
| `vigencia` | objeto | Sí | Vigencia de la norma. |
| `enlaceRegimenLegal` | texto o `null` | No | Enlace a Régimen Legal, si existe. |
| `alcance` | objeto | Sí | Programas a los que aplica la norma. |

### `vigencia` y `momento`

La vigencia se representa con dos momentos: `aplicaDesde` y `vigenteHasta`. El segundo puede ser
`null` si la norma no tiene fin. Un momento es uno de estos objetos:

```jsonc
{ "tipo": "PERIODO", "periodo": "2022-2" }
{ "tipo": "FECHA", "fecha": "2022-05-25" }
```

- `PERIODO` usa el formato `AAAA-1` o `AAAA-2`; se ignoran los espacios al inicio y al final.
- `FECHA` usa `AAAA-MM-DD`.
- Si `tipoDeEfecto` es `EXCEPCION_O_MEDIDA_TEMPORAL`, `vigenteHasta` es obligatorio.

La vigencia propia de una regla usa la misma forma. Si es `null`, la regla rige con la vigencia de
su norma.

### `alcance`

| Campo | Tipo | Regla |
|---|---|---|
| `tipo` | texto | `TODOS`, `FACULTADES` o `PROGRAMAS`. |
| `facultades` | arreglo de textos | Nombres de facultades; solo si `tipo` es `FACULTADES`. |
| `programas` | arreglo de textos | Códigos SIA; solo si `tipo` es `PROGRAMAS`. |
| `exclusiones` | arreglo de textos | Códigos SIA excluidos; deben ser programas del alcance. |
| `texto` | texto | Texto con el que la norma fija el alcance. |

Las listas que no correspondan al tipo deben ir vacías. Para `PROGRAMAS`, `exclusiones` debe ser
subconjunto de `programas`.

## Reglas

Cada elemento de `reglas` tiene esta forma común:

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `categoria` | texto | Sí | Debe coincidir con `datos.tipo`. |
| `objeto` | texto | Sí | Objeto sobre el que fija el dato la regla. |
| `enunciado` | texto | Sí | Texto o resumen fiel de la norma. |
| `articulo` | texto | Sí | Artículo o numeral tal como lo rotula la norma. |
| `vigenciaPropia` | objeto o `null` | No | Vigencia propia de la regla. |
| `clasificacion` | texto | Sí | `DEL_PLAN` o `INFORMATIVA`. |
| `afectacion` | objeto o `null` | No | Regla o artículo que esta regla modifica, deroga o exceptúa. |
| `datos` | objeto | Sí | Datos propios de la categoría. |

El **estado** de la regla no se digita: lo calcula el importador a partir de la vigencia y del
periodo en curso. En el archivo no se escribe `estado`.

### `afectacion` opcional

```jsonc
{
  "tipo": "MODIFICA",
  "destino": {
    "tipo": "ARTICULO",
    "norma": {
      "tipo": "ACUERDO",
      "numero": "008",
      "anio": 2008,
      "organo": "Consejo Superior Universitario"
    },
    "articulo": "art. 10"
  },
  "desde": { "tipo": "PERIODO", "periodo": "2026-2" }
}
```

- `tipo`: `MODIFICA`, `DEROGA` o `EXCEPTUA`.
- `destino.tipo = "ARTICULO"`: afecta todas las reglas de ese artículo de la norma indicada.
- `destino.tipo = "REGLA"`: afecta una regla concreta; añade `objeto` a la referencia.
- `desde`: momento opcional desde el que surte efecto.

### Datos por categoría

El campo `datos.tipo` es el discriminante y debe coincidir con `categoria`.

#### `MATERIA`

```jsonc
{
  "tipo": "MATERIA",
  "materia": {
    "codigo": "2024042",
    "nombre": "Cálculo diferencial",
    "creditos": 4,
    "obligatoriedad": "OBLIGATORIA",
    "semestreSugerido": 1
  },
  "contenedor": {
    "componente": "Componente de Fundamentación",
    "agrupacion": "Matemáticas"
  }
}
```

`obligatoriedad`: `OBLIGATORIA` u `OPTATIVA`. `semestreSugerido` puede ser `null`. `contenedor`
indica el componente y, si aplica, la agrupación más interna que contiene la materia.

El código identifica la materia en el catálogo global y sus créditos son fijos. La importación
rechaza un archivo que le dé a un código otros créditos que los del catálogo, o dos valores
distintos dentro de la misma norma (UC07 ext. 6d).

#### `PRERREQUISITO` y `CORREQUISITO`

```jsonc
{
  "tipo": "PRERREQUISITO",
  "materia": "2024043",
  "expresion": {
    "tipo": "Y",
    "izquierda": { "tipo": "MATERIA_DEL_PROGRAMA", "codigo": "2024042" },
    "derecha": {
      "tipo": "MATERIA_EXTERNA",
      "nombre": "Matemáticas Básicas",
      "codigo": null,
      "origen": "NIVELACION"
    }
  }
}
```

Una expresión es:

```jsonc
{ "tipo": "MATERIA_DEL_PROGRAMA", "codigo": "2024042" }
{ "tipo": "MATERIA_EXTERNA", "nombre": "Matemáticas Básicas", "codigo": null, "origen": "NIVELACION" }
{ "tipo": "Y", "izquierda": { }, "derecha": { } }
{ "tipo": "O", "izquierda": { }, "derecha": { } }
```

`origen` de una materia externa: `NIVELACION` u `OTRO_PROGRAMA`.

#### `HABILITACION`

```jsonc
{
  "tipo": "HABILITACION",
  "materia": "2024045",
  "medida": { "tipo": "COMPONENTE", "componente": "FUNDAMENTACION" },
  "umbral": 40
}
```

`medida` es `{ "tipo": "PROGRAMA" }` o `{ "tipo": "COMPONENTE", "componente": "..." }`, donde
`componente` es el tipo del componente: `FUNDAMENTACION` o `DISCIPLINAR`. Es el mismo tipo con el que
el contexto de evaluación informa los créditos aprobados de un componente.

#### `AGRUPACION`

```jsonc
{
  "tipo": "AGRUPACION",
  "agrupacion": {
    "nombre": "Matemáticas",
    "creditosExigidos": 16,
    "desglose": null
  },
  "contenedor": { "componente": "Componente de Fundamentación", "agrupacion": null }
}
```

`desglose` puede ser `null` o `{ "obligatorios": 8, "optativos": 8 }`.

#### `COMPONENTE`

```jsonc
{
  "tipo": "COMPONENTE",
  "componente": {
    "nombre": "Componente de Fundamentación",
    "tipo": "FUNDAMENTACION",
    "creditosExigidos": 51,
    "desglose": { "obligatorios": 15, "optativos": 36 }
  }
}
```

`componente.tipo`: `FUNDAMENTACION`, `DISCIPLINAR` o `LIBRE_ELECCION`. `LIBRE_ELECCION` representa
el componente de libre elección: se modela como `ComponenteDeLibreEleccion` y solo lleva créditos;
para ese tipo, `desglose` debe ser `null`.

#### `PROGRAMA`

```jsonc
{ "tipo": "PROGRAMA", "creditosExigidos": 160 }
```

#### `EQUIVALENCIA`

```jsonc
{ "tipo": "EQUIVALENCIA", "origen": ["2024042"], "destino": "2024046" }
```

`origen` debe tener al menos un código.

#### `CARGA_POR_PERIODO`

```jsonc
{ "tipo": "CARGA_POR_PERIODO", "creditos": 10, "tipoCarga": "MINIMO" }
```

`tipoCarga`: `MINIMO` o `MAXIMO`.

#### `REQUISITO_DE_GRADO` y `OTRA`

```jsonc
{ "tipo": "REQUISITO_DE_GRADO" }
{ "tipo": "OTRA" }
```

No llevan más datos. Son siempre informativas.

## Datos provisionales de la semilla

Las fechas de `fechaExpedicion` y `aplicaDesde` de la semilla real
(`src/main/resources/semilla/`) se toman de Régimen Legal, que las escribe como **día/mes/año**:
«11/01/2023» es el 11 de enero de 2023. En el JSON van como `AAAA-MM-DD`.

| Norma | Dato en la semilla | Lo que dice la norma | Por qué es provisional | Cuándo se corrige |
|---|---|---|---|---|
| Acuerdo 008 de 2008, CSU | `aplicaDesde`: fecha `2009-01-01` («fecha de entrada en vigencia» de Régimen Legal) | Art. 61: rige «a partir del primer período académico de 2009» | La tabla `norma` todavía no guarda periodos académicos | Con #141 pasa a `{ "tipo": "PERIODO", "periodo": "2009-1" }` |

El Acuerdo 003-A de 2022 tiene como fecha de aplicación la de su publicación (art. 13): 11 de
enero de 2023. Su art. 12 (régimen de transición) dice además que el plan empieza a regir «a
partir de la cohorte que ingresa en el período 2023-2S». La semilla no registra ese periodo; se
revisa cuando el esquema guarde periodos (#141), donde queda anotado junto con la
actualización del Acuerdo 008 y de la prueba de fechas.

La importación omite una norma ya registrada (por tipo, número, año y órgano). Para que una base
de desarrollo tome un dato corregido de la semilla hay que borrar `data/` y volver a arrancar con
el perfil `dev`.

## Datos de prueba de la semilla

La semilla trae dos normas ficticias para cubrir los casos que pide ENT-04 y que la normativa real
todavía no puede aportar:

| Archivo | Norma | Casos que cubre |
|---|---|---|
| `datos-de-prueba-vigentes.json` | Acuerdo 901 de 2026, Consejo de Facultad de Ingeniería (datos de prueba) | Un máximo de créditos por periodo, un correquisito entre dos materias de la malla y una regla informativa con «vigencia hasta» |
| `datos-de-prueba-programados.json` | Acuerdo 902 de 2026, Consejo de Facultad de Ingeniería (datos de prueba) | Una norma que aplica desde una fecha futura, para que sus reglas queden `PROGRAMADA`; es toda informativa y provoca el aviso de UC07 ext. 5a |

**Efecto en el cálculo.** Con el perfil `dev`, el máximo de 25 créditos y el correquisito se cargan
como reglas del plan que UC01, UC03 y UC05 deben aplicar; hoy el código principal todavía no las
aplica. El glosario dice que RutaUN no supone ningún máximo permanente: este tope es un dato cargado
de prueba (regla de oro 2), no un supuesto del código. Quien haga la demostración con `dev` verá
normas que no existen, distinguibles solo por la marca. La decisión está en
[`0002`](../diseno/decisiones/0002-datos-de-prueba-en-la-semilla.md).

**Convención de marcado.** Un archivo es de datos de prueba si su nombre empieza por
`datos-de-prueba-`. En esos archivos, el órgano de la norma y el enunciado y el objeto de cada regla
contienen el texto exacto «(datos de prueba)»; en los demás archivos de normas ese texto no aparece
en ningún campo. `DatosDePruebaDeLaSemillaTest` vigila la convención.

**Por qué no se usan las normas reales.** La Circular 11 de 2026 (máximo de 23 créditos) y la
Resolución 057 de 2026 rigen por periodo académico, y las tablas `norma` y `regla` solo guardan
fechas; los periodos llegan con [#141](https://github.com/SamuelGomezG/RutaUN/issues/141).
Además, el importador todavía no aplica afectaciones
([#175](https://github.com/SamuelGomezG/RutaUN/issues/175)). Las fechas futuras (2099) son
deliberadas: no se vencen durante la vida del proyecto y delatan que los datos son ficticios. Las
dos normas tienen fechas de expedición distintas (14 y 15 de enero de 2026) para que el orden por
fecha de #146 y el de UC06 no dependan de un desempate.

**Cuándo se borran.** Cada dato ficticio cubre un caso distinto: se quita solo el que pase a estar
cubierto por una norma real y se conservan los demás, aunque otro caso ya lo esté. Si al quitar
reglas una norma ficticia se queda sin ninguna, se borra su archivo, porque el validador rechaza
una incorporación sin reglas. Las cifras de `SemillaDeSistemasTest` y de «Qué deja la semilla» en
[`esquema-de-la-base.md`](esquema-de-la-base.md) se recalculan con los datos que queden, no con la
suposición de que desaparecen los dos archivos completos; `DatosDePruebaDeLaSemillaTest` y
`SoporteDeSemilla` descubren los archivos por su nombre y no fijan cifras. Retirar un archivo del
repositorio no lo saca de una base ya cargada: hay que borrar `data/` y volver a arrancar con el
perfil `dev`.

Los datos de prueba no traen reglas de estructura (`MATERIA`, `AGRUPACION`, `COMPONENTE` ni
`PROGRAMA`) ni afectaciones: no deben mover la versión del plan de estudios ni depender de #175.

## Errores que los archivos inválidos deben provocar

| Archivo | Caso | Resultado esperado |
|---|---|---|
| `regla-sin-enunciado.json` | Regla sin `enunciado`. | Rechazo por el paso 6a: falta el enunciado. |
| `regla-sin-articulo.json` | Regla sin `articulo`. | Rechazo por GM 4: toda regla necesita fuente. |
| `datos-incompletos.json` | Regla `MATERIA` sin `creditos`. | Rechazo por el paso 6a: datos incompletos para su categoría. |
| `temporal-del-plan.json` | Regla con `vigenciaPropia` temporal y `clasificacion: "DEL_PLAN"`. | Rechazo por la extensión 6f: una regla temporal solo puede ser informativa. |

Los cuatro archivos son JSON sintácticamente válidos: el rechazo es semántico y lo hará el lector de
UC07-HU03-T2.

## Archivos de prueba y cómo ejecutarlos

- Válido: `src/test/resources/json/validos/consolidado-prueba.json`.
- Válido, con todas sus reglas informativas (UC07 ext. 5a):
  `src/test/resources/json/validos/norma-toda-informativa.json`.
- Inválidos: `src/test/resources/json/invalidos/`.

La prueba de contrato `FormatoNormativaTest` lee `consolidado-prueba.json` desde el classpath y
comprueba que tiene `schemaVersion`, programa, norma y reglas, y que los inválidos son JSON
parseables con el defecto esperado. El archivo `norma-toda-informativa.json` lo ejercita
`ImportacionDeNormasServiceTest`, que lo importa con el lector real.

```bash
./mvnw test -Dtest=FormatoNormativaTest
```

Estos archivos **no se cargan solos en la aplicación**: son fixtures de prueba. La carga real de la
semilla la hace el runner de UC07-HU03-T4 (#96) desde `src/main/resources/semilla/`. Para probar esa
carga, se reinicia la base H2 local:

```bash
rm -f data/rutaun.mv.db
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No se deben usar datos personales en estos archivos (Ley 1581 de 2012). Los datos de ejemplo son
sintéticos y están marcados como tales en este documento.
