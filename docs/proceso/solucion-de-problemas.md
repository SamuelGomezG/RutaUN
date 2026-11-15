---
title: "Solución de problemas"
doc_type: "proceso"
depends_on:
  - entorno-y-configuracion.md
---

# Solución de problemas

## 0′ · Cuándo cargar este archivo

Cuando el build, el arranque, la aplicación o la CI fallan y el mensaje no basta. Cada entrada va del síntoma a la causa y a lo que hay que hacer. Cómo funciona cada pieza está en [`entorno-y-configuracion.md`](entorno-y-configuracion.md).

Los mensajes entre comillas son los que imprime la herramienta. Los marcados con ✔ se reprodujeron el 12 de noviembre de 2026; los demás salen de leer el código y la configuración.

Si resuelves un fallo que no está aquí y le puede pasar a otra persona, agrégalo.

## Construir

| Síntoma | Causa | Qué hacer |
| --- | --- | --- |
| «RutaUN se compila y prueba con Java 21. Instala un JDK 21 o usa SDKMAN…» | Enforcer: el JDK activo no es un 21. Suele pasar al construir desde un IDE o un hook con otro `JAVA_HOME` | `sdk env install`, o apunta `JAVA_HOME` a un JDK 21. Comprueba con `./mvnw -v` |
| `verify` corre todas las pruebas y al final falla en `spotless:check` | El formato se revisa después de las pruebas | `./mvnw spotless:apply`. Para saberlo antes: `./mvnw spotless:check checkstyle:check` ✔ |
| `verify` falla en `checkstyle:check` y el detalle está en líneas `[WARNING] …java` | Checkstyle falla con cualquier aviso, también en las pruebas. Spotless no arregla nombres ni líneas de más de 100 caracteres | Corrige cada aviso a mano. Los `package-info.java` no se revisan |
| `ModulosTest` falla y nombra un módulo que depende de otro | Un módulo importa uno que no declaró, o usa una clase de un subpaquete de otro | Si la dependencia es legítima, agrégala a `allowedDependencies` en el `package-info.java` del módulo y justifícala en la solicitud de integración. Si es una clase interna, usa lo que el otro módulo publica en su raíz, o un evento (`AGENTS.md`, «Capas y módulos») |
| `CapasTest` falla | Una capa depende de una superior: el dominio importa Spring, `aplicacion`, persistencia o `puertos`; persistencia importa negocio; `web` toca un repositorio | Mueve la clase a su capa o invierte la dependencia. Las reglas, una por una, están en [`pruebas.md`](pruebas.md) |
| `NombresTest` falla | Una clase termina en `Impl`, o un identificador lleva tilde o eñe | Nómbrala por lo que la distingue, en ASCII (`ParteDelAnio`) |
| Una regla de ArchUnit nueva pasa aunque no debería | `archRule.failOnEmptyShould=false`: una regla sobre un paquete sin clases no falla | Comprueba el selector del paquete con una clase que la viole a propósito |
| `.jqwik-database` aparece modificado tras correr las pruebas | jqwik guarda ahí las propiedades que fallaron | No lo incluyas en el commit: `git restore .jqwik-database` |
| Las pruebas de la semilla fallan con «semilla/ debe resolverse solo desde la salida de producción», o el runner intenta importar un archivo que ya no existe | Quedó una carpeta `semilla/` copiada en `target/test-classes` de una compilación anterior: Maven no retira de `target/` los recursos eliminados | `./mvnw clean` una vez. `SemillaDeSistemasTest` es la guarda |

## Arrancar

| Síntoma | Causa | Qué hacer |
| --- | --- | --- |
| «Migration checksum mismatch for migration version N» ✔ | Una migración ya aplicada cambió. Pasa al editar un `V*.sql` o al cambiar a una rama que trae otra versión del mismo archivo | En una base de desarrollo desechable, borra `data/` y arranca de nuevo. Una migración aplicada no se edita: se agrega una nueva ([`recetas.md`](recetas.md)). El mensaje sugiere `repair`; no lo uses para tapar una migración editada |
| El arranque falla con «Schema-validation: …» | Una entidad JPA no coincide con su tabla (`ddl-auto: validate`) | Falta la migración del cambio, o la entidad y la migración no dicen lo mismo. Compara la entidad con [`esquema-de-la-base.md`](../datos/esquema-de-la-base.md) |
| «Database may be already in use: …/data/rutaun.mv.db» ✔ | Otro proceso tiene abierta la base: la aplicación, la consola de H2 o el cliente de base de datos del IDE | Cierra el otro proceso. H2 en archivo admite uno solo |
| «Web server failed to start. Port 8080 was already in use» | Otra aplicación usa el puerto, o quedó una instancia de RutaUN | Detén la otra, o arranca con `--server.port=…` (el comando está en [`entorno-y-configuracion.md`](entorno-y-configuracion.md)) |
| WARN de Flyway: «Using H2 2.4.240 which is newer than the version Flyway has been verified with» ✔ | Flyway 12 todavía no declara soporte para esa versión de H2 | Nada: no bloquea ([`cambios-de-boot-4.md`](cambios-de-boot-4.md)) |
| El arranque falla con `ArchivoDeProgramasInvalidoException` o `ArchivoDeNormativaInvalidoException` | Un archivo de `semilla/` no pasa la validación. El mensaje trae los motivos | Corrige el archivo según [`formato-normativa.md`](../datos/formato-normativa.md). Dos motivos frecuentes: «No hay un programa cargado con el código SIA…» (falta en `programas.json`) y «La norma no comprende ningún programa cargado» |
| Arranca, pero el log no dice «Programas cargados…» y la base queda sin normas | Arrancó sin el perfil `dev` | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` |
| El log dice «0 creadas, 3 ya existían» ✔ y un dato corregido de la semilla no aparece | La importación omite una norma que ya está registrada | En una base desechable, borra `data/` y arranca con `dev` |
| Tras cambiar una clase del dominio, falla al leer una regla o una versión guardada | El JSON guardado sigue la forma de las clases del dominio y no tiene versión | En una base desechable, borra `data/`. Si hay datos que conservar, lee antes [`cambios-de-boot-4.md`](cambios-de-boot-4.md), «Verificado en los carriles 2 y 3» |
| Aparece una carpeta `data/` fuera de la raíz del repositorio | La ruta de la base es relativa al directorio desde el que arrancaste | Arranca desde la raíz y borra la carpeta sobrante |

**Antes de borrar `data/`**, comprueba que la base es desechable: que solo tiene la semilla. Si guarda normas incorporadas por fuera de `semilla/` o incidentes que alguien necesita —y, cuando existan, avances y planes—, no la borres.

## Usar la aplicación

| Síntoma | Causa | Qué hacer |
| --- | --- | --- |
| `/programas` responde 503, «Consulta no disponible» ✔ | La aplicación todavía no tiene la implementación de `ConsultaNormativaFacade` | Es lo esperado hasta que entre [#132](https://github.com/SamuelGomezG/RutaUN/issues/132). La semilla sí está cargada: compruébalo con una consulta sobre la base |
| `/programas/{id}/reglas` responde 400 ✔ | `{id}` no es un UUID | Usa el `id` del programa, no su código SIA |
| Esperabas un incidente y no aparece | No hay pantalla de incidentes; o el registro falló | Consulta la tabla `incidente` ([`esquema-de-la-base.md`](../datos/esquema-de-la-base.md)). Si el registro falló, el log dice «No se pudo registrar un incidente: …» con la clase del error y sin el contenido del incidente |
| Dos incidentes distintos quedaron en una sola fila, o uno igual quedó en dos | La agrupación tiene defectos abiertos | [#167](https://github.com/SamuelGomezG/RutaUN/issues/167), [#168](https://github.com/SamuelGomezG/RutaUN/issues/168) y [#169](https://github.com/SamuelGomezG/RutaUN/issues/169) |

## Git y la integración continua

| Síntoma | Causa | Qué hacer |
| --- | --- | --- |
| Los hooks no corren | No están activados en este clon, o la rama no contiene `.githooks/` | `git config core.hooksPath .githooks`; si la rama es vieja, rebásala sobre `develop`. El resto de los casos está en `CONTRIBUTING.md`, «Hooks de git» |
| `./mvnw verify` pasa en tu máquina y la CI falla en «Verificar el spike del motor (TT-05)» | La CI construye también `spikes/tt-05-motor/`, que el `verify` de la raíz no incluye | `./mvnw -B -f spikes/tt-05-motor/pom.xml verify` |
| La CI falla en una prueba y el log no alcanza | — | Descarga el artefacto `informes-de-surefire` de la ejecución, en GitHub Actions |
| La ejecución de la CI aparece cancelada | Un push nuevo a la misma rama cancela la ejecución anterior | Mira la ejecución del último commit |
| Una aprobación de la solicitud de integración desapareció | Un push nuevo la descarta | Pide la revisión otra vez. Los hooks `pre-commit` y `pre-push` existen para no empujar algo que la CI va a rechazar |
