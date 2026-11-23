---
title: "Convención de pruebas (PRU-01)"
doc_type: "proceso"
depends_on:
  - ../requisitos/00-contexto.md
---

# Convención de pruebas (PRU-01)

## 0′ · Cuándo cargar este archivo

Antes de escribir o mover una prueba, al cambiar la pirámide o al tocar las
reglas de arquitectura. La Definición de Hecho está en
`definicion-de-hecho.md`.

Este documento fija cómo se nombran, se marcan y se corren las pruebas de
RutaUN, y dónde están los ejemplos de cada nivel. El build falla si algo de
esto se rompe: los nombres los revisa Checkstyle y el formato, Spotless.

## Cómo correrlas

| Qué | Comando |
| --- | --- |
| Todo: formato, estilo, arquitectura y pruebas | `./mvnw verify` |
| Solo las pruebas de integración (etiqueta `integracion`) | `./mvnw test -Dgroups=integracion` |
| Solo la capa web (MockMvc; no llevan la etiqueta) | ./mvnw test -Dtest="*ControllerTest,ReglasDelProgramaEstadosTest" |
| Todas menos las de integración | `./mvnw test -DexcludedGroups=integracion` |
| Formatear el código | `./mvnw spotless:apply` |
| Cobertura con JaCoCo: reporte en `target/site/jacoco/index.html` | `./mvnw verify`

Las pruebas de integración corren con el perfil `test`
(`application-test.yml`): H2 en memoria y las migraciones de
`src/test/resources/db/humo/`.

`src/test/resources/semilla/` no debe existir: cualquier archivo ahí oculta la
semilla real en las pruebas (`SemillaDeSistemasTest` lo detecta).

El reporte mide lo que corrió. Con `-Dgroups=integracion` solo muestra la cobertura de las pruebas de integración; para el total, corre `./mvnw verify` completo. Para abrirlo en el navegador usa Start-Process target\site\jacoco\index.html en PowerShell (start en CMD), open target/site/jacoco/index.html en macOS o xdg-open target/site/jacoco/index.html en Linux.

## Nombres

1. **Cada prueba lleva en su nombre el ID que verifica** (regla de oro 1):
   inicia en minúscula y separa las partes con `_`
   (`uc01_ext4b_topeBajoElMinimoAdvierte`, `uc03_ext2a_marcaPorActualizarSeSeñala`).
2. **Las pruebas de infraestructura no verifican un ID del SRS.** Proponen este
   documento (hueco que el SRS no cubre) usar el ID del issue en su lugar:
   `tt01_inicioSirveLaPagina`, `tt01_guardaYLeeUnDatoEnH2`.
3. La clase de prueba se llama como la unidad probada + `Test`
   (`ArgumentChecksTest`, `InicioControllerTest`), en el paquete espejo del
   código que prueba.

## Pirámide y ejemplos

| Nivel | Qué se prueba | Herramienta | Ejemplo en el código |
| --- | --- | --- | --- |
| Unitarias de dominio | Valores y restricciones, sin Spring | JUnit 6 y AssertJ | `utilidades/ArgumentChecksTest` |
| Propiedades | Determinismo del cálculo, cumplimiento del tope y de restricciones | jqwik | `utilidades/ArgumentChecksPropiedadesTest` |
| Oráculo de optimalidad | El plan mínimo y los seis criterios del cálculo, contra fuerza bruta | JUnit 6 con mallas sintéticas | *(pendiente, PRU-02 #61)*; el prototipo de `spikes/tt-05-motor/` no demuestra optimalidad de c4–c6 |
| Orquestadores | Flujos y extensiones de cada caso con fachadas falsas | Mockito con `Clock` fijo | `normativa/aplicacion/ConsultaDeReglasOrchestratorTest, ConsultaDeRestriccionesOrchestratorTest` |
| Arquitectura | Reglas de capas, módulos y nombres | Modulith `verify()` y ArchUnit | `arquitectura/ModulosTest`, `CapasTest`, `NombresTest`, `EntidadesTest` |
| Integración | Repositorios, restricción única, eventos, sesión, reserva de UC07 | `@PruebaDeIntegracion` (contexto completo, H2 en memoria) | `PersistenciaHumoTest`, `normativa/aplicacion/ConsultaNormativaServiceIntegracionTest` |
| Web | Controladores, rutas, Post/Redirect/Get, errores de formulario | MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) | `web/InicioControllerTest`, flujo completo hasta H2: `normativa/web/ReglasDelProgramaEstadosTest` |
| Contrato | Formato JSON de importación, válido e inválido | JUnit 6 con archivos de `src/test/resources/json/` | `normativa/aplicacion/FormatoNormativaTest` |
| Aceptación | Criterios de las 47 historias de usuario | Escenarios con datos de la semilla | *(sprint 2)* |

## Dobles de prueba

| Doble | Qué es | Dónde está |
| --- | --- | --- |
| Dummy | Objeto que solo llena un parámetro obligatorio; si se usa, la prueba falla | `soporte/PublicadorDeEventosDeRelleno` (prueba `uc06_paso2_listarLosProgramasNoNecesitaPublicarEventos`) |
| Fake | Implementación que funciona, pero simplificada: datos sintéticos fijos, sin persistencia | `soporte/ConsultaNormativaFalsa` (filtra reglas por estado, categoría y texto) |
| Stub | Responde con datos fijos que se arman antes de la prueba | `soporte/ContextoDeEvaluacionFijo` |
| Espía | Guarda lo que recibe para comprobarlo después | `RegistroDeEventos` en `ConsultaDeReglasOrchestratorTest` |
| Mock | Doble de Mockito con respuestas y verificaciones programadas | `ConsultaDeRestriccionesOrchestratorTest`, `SemillaDeDesarrolloRunnerTest` |

`ConsultaNormativaFalsa` no guarda estado: sus datos son fijos. Se
clasifica como Fake y no como Stub porque implementa el comportamiento de la
fachada (filtros y programa inexistente), no solo devuelve respuestas
enlatadas.

## Reglas de arquitectura que se verifican

`CapasTest` (AGENTS.md, «Capas y módulos»):

- El dominio no importa `aplicacion`, la presentación ni persistencia, y no
  conoce Spring.
- La persistencia no importa clases de negocio (`aplicacion` ni `dominio`) ni
  la presentación.
- La aplicación no depende de la presentación; la presentación no toca
  persistencia ni Spring Data (los repositorios los usa solo `aplicacion`;
  desde que TT-04 creó los repositorios, la regla se verifica sobre las clases
  reales). Los selectores de `web` quedan acotados a
  `co.edu.unal.rutaun..web..` para cubrir los `web/` de cada módulo sin
  matchear paquetes de frameworks.

`ModulosTest`: `ApplicationModules.of(RutaUnApplication.class).verify()`
falla si un módulo usa las clases internas de otro.

`NombresTest`: ninguna clase termina en `Impl` y los identificadores son
ASCII. Las reglas sobre paquetes que aún no tienen clases no fallan
(`archRule.failOnEmptyShould=false` en `archunit.properties`).

`EntidadesTest`: toda entidad JPA implementa `Persistable` o tiene un campo
`@Version`, para que guardar una entidad nueva con una llave existente falle
en vez de sobrescribir la fila (#140).

### Un ejemplo de cada nivel

Las rutas son relativas a `src/test/java/co/edu/unal/rutaun/`.

| Nivel | Ejemplo |
| --- | --- |
| Unitaria de dominio, sin Spring | `utilidades/ArgumentChecksTest` |
| Propiedades (jqwik) | `utilidades/ArgumentChecksPropiedadesTest` |
| Arquitectura | `arquitectura/CapasTest` |
| Integración (contexto completo, H2 en memoria, etiqueta `integracion`) | `normativa/aplicacion/ConsultaNormativaServiceIntegracionTest` |
| Capa web: MockMvc con `@SpringBootTest` y `@AutoConfigureMockMvc` | `normativa/web/ReglasDelProgramaEstadosTest`: la petición HTTP recorre controlador, orquestador y publicador real de eventos, y la prueba lee el incidente guardado en H2. Solo la fachada de normativa es falsa. |
| Contrato (formato JSON) | `normativa/aplicacion/FormatoNormativaTest` |

### Dobles de prueba

| Doble | Ejemplo |
| --- | --- |
| Dummy | `soporte/PublicadorDeEventosDeRelleno`, usado en `uc06_paso2_listarLosProgramasNoNecesitaPublicarEventos` |
| Fake | `soporte/ConsultaNormativaFalsa`: filtra reglas por estado, categoría y texto, con datos sintéticos fijos |
| Stub | `soporte/ContextoDeEvaluacionFijo` |
| Espía | `RegistroDeEventos`, dentro de `normativa/aplicacion/ConsultaDeReglasOrchestratorTest` |
| Mock (Mockito) | `normativa/aplicacion/ConsultaDeRestriccionesOrchestratorTest` |

## Pendientes (se completan al llegar)

- **jqwik (decisión D8, resuelta):** el humo con JUnit 6 corrió en el PR 2:
  jqwik 1.10.2 descubre y ejecuta propiedades junto a JUnit 6.0.3 sin
  conflicto de motores. Las propiedades reales del cálculo llegan con TT-08
  (#174) y su oráculo, PRU-02 (#61).
- **Datos de prueba:** mallas sintéticas en `soporte/` y archivos JSON en
  `src/test/resources/json/`; nunca datos personales reales (regla de oro 7).
  Las demostraciones del dominio viven en `soporte/demostraciones/` y no son
  pruebas: muestran el flujo con el código real (segundo criterio de la
  Definición de Hecho, `definicion-de-hecho.md`).
