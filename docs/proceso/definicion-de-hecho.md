---
title: "Definición de Hecho"
doc_type: "proceso"
depends_on:
  - entorno-y-configuracion.md
  - pruebas.md
  - ../datos/esquema-de-la-base.md
---

# Definición de Hecho

## 0′ · Cuándo cargar este archivo

Antes de abrir o revisar una solicitud de integración y al preparar la demostración de un elemento. Dice cuándo un elemento cuenta como terminado y cómo se demuestra un cambio. Los comandos de cada medio están en [`entorno-y-configuracion.md`](entorno-y-configuracion.md) y las consultas sobre la base, en [`../datos/esquema-de-la-base.md`](../datos/esquema-de-la-base.md).

Un elemento (historia, tarea técnica, spike o documento) cuenta como terminado solo si cumple todo lo siguiente:

1. Está integrado en `develop` y lo revisó otra persona del equipo.
2. Si incluye código, se puede ejecutar —por quien lo escribió y por quien lo revisa— para recrear el flujo que resuelve y obtener un resultado observable que muestre que hace lo que debe: la aplicación, una prueba de aceptación, un comando o un script, con los pasos en el PR.
3. Tiene pruebas unitarias en verde, y las pruebas de arquitectura pasan en la integración continua.
4. Cumple Google Java Style (el build lo verifica).
5. Si es una historia, sus criterios de aceptación se demostraron funcionando y su issue quedó cerrado en el tablero.

## Cómo se demuestra un cambio

El segundo criterio tiene cuatro partes: los pasos están en la PR; recrean el flujo que el elemento resuelve; dan un resultado observable que muestra que hace lo que debe; y los puede seguir quien escribió y quien revisa.

Con eso, una PR queda en uno de tres niveles:

| Nivel | Qué traen los pasos | Qué describe el resultado |
| --- | --- | --- |
| **Cumple** | Ejecutan el flujo concreto con el código real | Lo que se ve: una página, una salida, un log, filas en la base |
| **Parcial** (es no cumplir) | Corren las pruebas del cambio, o una mutación que las rompe, salvo cuando el cambio son las pruebas mismas | Lo que esas pruebas comprueban; el flujo no se ve funcionando |
| **No cumple** | Solo corren `./mvnw verify` o las pruebas | Un conteo, o nada |

Sirven cinco medios:

| | Medio | Cuándo | Cómo se corre |
| --- | --- | --- | --- |
| **A** | La aplicación | Siempre que el flujo llegue a una pantalla o al log del arranque | Genera una base desechable nueva en cada demostración, sin tocar `data/`: `base=$(mktemp -d)/rutaun` y `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:file:$base --server.port=18080"`. En Windows, crea la carpeta y define `$base` antes de arrancar, con los dos `-D` entre comillas porque PowerShell 5.1 parte el argumento en el punto: `$base = Join-Path (New-Item -ItemType Directory -Path "$env:TEMP\rutaun-$(New-Guid)").FullName 'rutaun'` y `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:file:$base --server.port=18080"`. Se lee el log y se abre la URL con ese puerto, en la misma terminal |
| **B** | La base | Cuando lo que se ve son filas | Con la aplicación detenida, `scripts/consultar-base.sh "<SQL>"` (o con la ruta de A como segundo argumento). En Windows, `scripts\consultar-base.ps1` |
| **C** | Una demostración del dominio | Cambios de dominio, que no tienen pantalla | Una clase con `main` en `src/test/java/co/edu/unal/rutaun/soporte/demostraciones/`, sin Spring ni base: `./mvnw -q test-compile` y `java -cp target/classes:target/test-classes co.edu.unal.rutaun.soporte.demostraciones.<Clase>`. En Windows, el classpath va entre comillas: `java -cp "target/classes;target/test-classes" co.edu.unal.rutaun.soporte.demostraciones.<Clase>`; el `;` separa comandos en PowerShell |
| **D** | Una prueba de aceptación | Cuando el resultado se observe en el log de la prueba (por ejemplo, la concurrencia) | Una prueba con los componentes reales, sin dobles, que la PR pide correr sola y que escribe en el log lo que se observa: `./mvnw test -Dtest=<Prueba>` |
| **E** | Un comando o un script | Cambios de proceso o de herramientas, sin pantalla ni filas (por ejemplo, los hooks) | El comando o el script del cambio, con lo que imprime: `.githooks/probar.sh`, `herramientas/indice_ids.py <ID> --contexto` |

La base de `data/` no se borra para demostrar. Si guarda normas incorporadas por fuera de `semilla/`, incidentes, avances o planes, no la elimines: respáldala y migra esos datos, como dicen [`cambios-de-boot-4.md`](cambios-de-boot-4.md) y [`solucion-de-problemas.md`](solucion-de-problemas.md) («Antes de borrar `data/`»). Si el comando A usó otra ruta, pásala al comando B como segundo argumento: `scripts/consultar-base.sh "<SQL>" "$base"`.

Un conteo de pruebas o una mutación no bastan: son evidencia del tercer criterio, no del segundo. La excepción es el cambio que trata de las pruebas mismas: ahí la mutación es el flujo y cuenta como **Cumple**.

Cuando el cambio no tiene flujo de usuario, el flujo es otro y la PR dice cuál:

- en un renombre, que nada cambie (`git grep` sin resultados y la aplicación igual que antes);
- en una regla de calidad, que el build rechace la violación;
- en un cambio sobre las pruebas, que la prueba detecte el error.

Lo que se pega en una PR nunca lleva datos personales (regla de oro 7): las demostraciones usan datos sintéticos.

Esta es la definición del criterio de terminado del plan del sprint 1, ampliada el 25 de octubre de 2026 con la ejecución del código y el 15 de noviembre de 2026 con la lectura del segundo criterio. Pasar a `main` no es parte de ella: ocurre una vez por sprint, cuando la demostración lo da por completo y satisfactorio (ver `CONTRIBUTING.md`).
