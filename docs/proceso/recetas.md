---
title: "Recetas para tareas frecuentes"
doc_type: "proceso"
depends_on:
  - ../../AGENTS.md
  - pruebas.md
  - ../datos/esquema-de-la-base.md
---

# Recetas para tareas frecuentes

## 0′ · Cuándo cargar este archivo

Al crear el módulo de un contexto, agregar una migración, agregar una norma a la semilla o publicar un incidente. Cada receta lista los pasos, los archivos que se tocan y cómo comprobar el resultado.

Las recetas no fijan reglas: las reglas de capas, módulos y nombres están en `AGENTS.md` y, cuando se publique, en `docs/diseno/` ([#176](https://github.com/SamuelGomezG/RutaUN/issues/176)). Aquí está el orden de los pasos y lo que no se ve leyendo esas reglas. Cada receta sigue un ejemplo que ya existe en el código.

## Crear el módulo de un contexto

Ejemplos: `normativa` (con las cuatro capas) e `incidentes` (estilo simple, sin `dominio` ni `web`).

1. **Crea el paquete** `co.edu.unal.rutaun.<contexto>`, en minúsculas y sin separadores (`plandecarrera`), solo cuando tengas la primera clase que va en él. No se crean módulos vacíos.
2. **Declara el módulo** en su `package-info.java`:

   ```java
   /** Avance: … qué es el contexto y de quién depende. */
   @ApplicationModule(
       displayName = "Avance",
       allowedDependencies = {"compartido", "utilidades"})
   package co.edu.unal.rutaun.avance;

   import org.springframework.modulith.ApplicationModule;
   ```

   - `allowedDependencies` lleva los nombres de los paquetes de primer nivel, **también los módulos abiertos** (`compartido`, `utilidades`, `puertos`, `web`). Lo que no esté en la lista hace fallar a `ModulosTest`.
   - Empieza con lo mínimo y agrega cada dependencia cuando la necesites, justificada en la solicitud de integración.
   - Spotless y Checkstyle no revisan los `package-info.java`: fórmalo a mano, como los que ya existen.
3. **Pon las capas como subpaquetes**, solo las que el contexto use: `web`, `aplicacion`, `dominio` y `persistencia`. Con esos nombres exactos, `CapasTest` las cubre sin tocarla.
4. **Publica en la raíz del paquete** lo que otros módulos pueden usar: fachadas, vistas y los eventos que el contexto ofrece. Todo lo demás va en los subpaquetes, que son internos.
5. **Si guarda datos**, agrega su migración (receta siguiente).
6. **Pon las pruebas en el paquete espejo** de `src/test/java`.

**Comprobación:** `./mvnw test -Dtest='ModulosTest,CapasTest,NombresTest'`. Para ver que las fronteras están activas, importa a propósito una clase de un subpaquete de otro módulo: `ModulosTest` debe fallar. Deshaz la prueba antes del commit.

## Agregar una migración

Ejemplo: `V2__incidente.sql`.

1. **Crea** `src/main/resources/db/migration/V<n>__<descripcion>.sql`, con el número siguiente al último y dos guiones bajos. `V0` está ocupado por la migración de humo de las pruebas.
2. **Escribe SQL estándar**, sin funciones propias de H2, para poder cambiar de motor sin reescribirla.
3. **Nunca edites una migración ya fusionada en `develop`.** Flyway guarda su suma de comprobación, y toda base que ya la aplicó deja de arrancar («Migration checksum mismatch»). Un cambio sobre una tabla existente es una migración nueva.
4. **Ajusta la entidad JPA** en `persistencia` del módulo. Hibernate valida al arrancar que la entidad y la tabla coincidan, y el arranque falla si no.
5. **Actualiza** [`esquema-de-la-base.md`](../datos/esquema-de-la-base.md) en el mismo commit.

Mientras la migración vive solo en tu rama, puedes editarla; después de cada cambio borra tu `data/` (si es desechable).

**Comprobación:** `./mvnw verify`: las pruebas de integración aplican todas las migraciones sobre una base en memoria. Después arranca con una base nueva (borra `data/` si es desechable) y mira en el log «Successfully applied … migrations». Si dos ramas agregan el mismo número, la segunda en fusionarse renumera la suya.

## Agregar una norma a la semilla

Ejemplo: `src/main/resources/semilla/acuerdo-11-2023.json`.

1. **Escribe el archivo** según [`formato-normativa.md`](../datos/formato-normativa.md). Los datos son los de la norma publicada en Régimen Legal, nunca cifras inventadas (regla de oro 2), y cada regla lleva su fuente (UC07 GM 4). La excepción son los archivos `datos-de-prueba-*.json`, que llevan datos ficticios mínimos marcados «(datos de prueba)» en el órgano y en el enunciado y el objeto de cada regla (ver «Datos de prueba de la semilla» en `formato-normativa.md`); no se usan para completar una norma real.
2. **Comprueba que su programa esté en** `semilla/programas.json`. El alcance cita los programas por su código SIA; si alguno no está cargado, el arranque falla.
3. **Elige el nombre pensando en el orden.** Los archivos se cargan en orden alfabético y cada uno crea una versión del consolidado, así que el nombre decide qué versión queda con qué reglas ([#146](https://github.com/SamuelGomezG/RutaUN/issues/146)).
4. **Anota lo provisional.** Si un dato no puede quedar como lo dice la norma porque el esquema todavía no lo admite, regístralo en «Datos provisionales de la semilla» de `formato-normativa.md`, con el issue que lo corrige.
5. **Actualiza** «Qué deja la semilla» en [`esquema-de-la-base.md`](../datos/esquema-de-la-base.md).
6. **Actualiza las pruebas que fijan las cifras.** Las constantes de `normativa/aplicacion/SemillaDeSistemasTest` (hoy 20, 27, 276, 277 y 280 reglas por versión; 880 en `regla_en_version`). La prueba de #130 de `ImportacionDeNormasServiceTest` solo cambia si cambia uno de los tres acuerdos, porque importa esos tres archivos por su nombre; el conteo de `SemillaDeDesarrolloRunnerTest` se calcula a partir de los archivos y no se toca. Si #146 reordena la carga, `REGLAS_POR_VERSION` pasa a `[7, 27, 276, 277, 280]` y `REGLAS_EN_VERSION` (la suma, que también documenta «Qué deja la semilla») de 880 a 867, aunque las 280 reglas de la tabla `regla` se conservan.

La semilla es normativa pública: nunca lleva datos personales (regla de oro 7). Los archivos para probar el formato, válidos e inválidos, no van aquí: van en `src/test/resources/json/`. `src/test/resources/semilla/` no debe existir: ocultaría esta semilla en las pruebas (`SemillaDeSistemasTest` lo detecta).

**Comprobación:** borra `data/` (si es desechable), arranca con el perfil `dev` y lee «Normas cargadas: N creadas…» en el log. Detén la aplicación y corre la consulta de versiones de `esquema-de-la-base.md`: debe haber una versión más, con las reglas de la anterior más las de la norma nueva. `./mvnw test -Dtest='SemillaDeSistemasTest,SemillaDeDesarrolloRunnerTest'` comprueba las cifras y que el conteo de normas se calcula solo.

## Publicar un incidente desde un caso de uso

Ejemplo: `ConsultaDeReglasOrchestrator`, que publica el de UC06 ext. 4a.

1. **Declara las dependencias** `incidentes` y `puertos` en `allowedDependencies` del módulo, si no están.
2. **Elige quién lo publica:** el orquestador del caso de uso si no hay un agregado que guardar; el servicio que guarda el agregado, si el incidente nace al guardar.
3. **Inyecta** `DomainEventPublisher` por constructor y publica uno de los dos tipos de `IncidenteDetectado`:

   ```java
   eventos.publicar(
       new IncidenteDePrograma("UC06", "ext. 4a", programaId, versionDelConsolidado, causa, List.of()));
   ```

   - `IncidenteDePrograma`, si hay un programa y una versión de su consolidado;
   - `IncidenteInterno`, si no los hay (por ejemplo, falta registrar el periodo en curso).
4. **Escribe el caso de uso y el paso con los IDs del SRS** (`UC06`, `ext. 4a`): son los que lee quien atiende el incidente.
5. **Deja la causa fija.** Los incidentes iguales se agrupan en una fila con su conteo; si la causa lleva un dato que cambia en cada ocurrencia, nunca se agrupan. Lo que varía —materias, agrupaciones, reglas, enlaces— va en `elementos`.
6. **Sin datos personales** en la causa ni en los elementos (regla de oro 7): un incidente lo lee el administrador de normativa, no el dueño del avance.

Lo que pasa después no lo programas tú: `IncidentesEventListener` registra el incidente en una transacción propia, para que quede guardado aunque tu operación falle. Si el registro falla, lo anota en el log y no interrumpe tu caso de uso. La fecha la pone Incidentes con su reloj.

**Comprobación:** en la prueba del orquestador, un `DomainEventPublisher` de prueba recibe el evento esperado (`ConsultaDeReglasOrchestratorTest`). De punta a punta, provoca el caso en la aplicación y corre la consulta de incidentes de [`esquema-de-la-base.md`](../datos/esquema-de-la-base.md).
