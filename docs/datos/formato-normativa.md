# Formato del JSON de normativa

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

- `PERIODO` usa el formato `AAAA-1` o `AAAA-2`.
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
  "medida": { "tipo": "COMPONENTE", "componente": "Componente de Fundamentación" },
  "umbral": 40
}
```

`medida` es `{ "tipo": "PROGRAMA" }` o
`{ "tipo": "COMPONENTE", "componente": "..." }`.

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
{ "tipo": "CARGA_POR_PERIODO", "creditos": 10, "tipoCarga": "MINIMA" }
```

`tipoCarga`: `MINIMA` o `MAXIMA`.

#### `REQUISITO_DE_GRADO` y `OTRA`

```jsonc
{ "tipo": "REQUISITO_DE_GRADO" }
{ "tipo": "OTRA" }
```

No llevan más datos. Son siempre informativas.

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

La prueba de contrato `FormatoNormativaTest` lee los archivos desde el classpath y comprueba que el
válido tiene `schemaVersion`, programa, norma y reglas, y que los inválidos son JSON parseables con
el defecto esperado.

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
