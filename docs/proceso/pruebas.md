# Convención de pruebas (PRU-01)

Este documento fija cómo se nombran, se marcan y se corren las pruebas de
RutaUN, y dónde están los ejemplos de cada nivel. El build falla si algo de
esto se rompe: los nombres los revisa Checkstyle y el formato, Spotless.

## Cómo correrlas

| Qué | Comando |
| --- | --- |
| Todo: formato, estilo, arquitectura y pruebas | `./mvnw verify` |
| Solo las pruebas de integración (etiqueta `integracion`) | `./mvnw test -Dgroups=integracion` |
| Todas menos las de integración | `./mvnw test -DexcludedGroups=integracion` |
| Formatear el código | `./mvnw spotless:apply` |

Las pruebas de integración corren con el perfil `test`
(`application-test.yml`): H2 en memoria y las migraciones de
`src/test/resources/db/humo/`.

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

## Pirámide y ejemplos que ya corren

| Nivel | Qué se prueba | Herramienta | Ejemplo en el código |
| --- | --- | --- | --- |
| Unitarias de dominio | Valores y restricciones, sin Spring | JUnit 6 y AssertJ | `utilidades/ArgumentChecksTest` |
| Propiedades | Determinismo del cálculo, cumplimiento del tope y de restricciones | jqwik | *(se retoma cuando jqwik corra con JUnit 6; ver «Pendientes»)* |
| Oráculo de optimalidad | El plan mínimo y los seis criterios del cálculo, contra fuerza bruta | JUnit 6 con mallas sintéticas | *(sprint 2, con TT-05)* |
| Orquestadores | Flujos y extensiones de cada caso con fachadas falsas | Mockito con `Clock` fijo | *(sprint 2)* |
| Arquitectura | Reglas de capas, módulos y nombres | Modulith `verify()` y ArchUnit | `arquitectura/ModulosTest`, `CapasTest`, `NombresTest` |
| Integración | Repositorios, restricción única, eventos, sesión, reserva de UC07 | `@PruebaDeIntegracion` (contexto completo, H2 en memoria) | `PersistenciaHumoTest` |
| Web | Controladores, rutas, Post/Redirect/Get, errores de formulario | MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) | `web/InicioControllerTest` |
| Contrato | Formato JSON de importación, válido e inválido | JUnit 6 con archivos de `src/test/resources/json/` | *(con UC07-HU03, #41)* |
| Aceptación | Criterios de las 47 historias de usuario | Escenarios con datos de la semilla | *(sprint 2)* |

## Reglas de arquitectura que se encuestan

`CapasTest` (AGENTS.md, «Capas y módulos»):

- El dominio no importa persistencia ni Spring.
- La persistencia no importa clases de negocio (`aplicacion` ni `dominio`).
- La presentación no toca persistencia ni Spring Data (los repositorios los
  usa solo `aplicacion`; la regla explícita «solo `aplicacion` usa los
  repositorios» se encuesta con firmeza cuando TT-04 los cree).

`ModulosTest`: `ApplicationModules.of(RutaUnApplication.class).verify()`
falla si un módulo usa las clases internas de otro.

`NombresTest`: ninguna clase termina en `Impl` y los identificadores son
ASCII. Las reglas sobre paquetes que aún no tienen clases no fallan
(`archRule.failOnEmptyShould=false` en `archunit.properties`).

## Pendientes (se completan al llegar)

- **jqwik (D8):** 1.10.2 compila contra JUnit Platform 1.14.4 y el BOM de
  Boot usa la serie 6. Si el humo del PR 2 no corre, se aplaza a que el
  stack lo soporte y se anota aquí.
- **Datos de prueba:** mallas sintéticas en `soporte/` y archivos JSON en
  `src/test/resources/json/`; nunca datos personales reales (regla de oro 7).
