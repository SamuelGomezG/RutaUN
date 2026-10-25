<!-- Título del PR: el del issue, por ejemplo «UC06-HU01 · Elegir un programa y ver sus reglas». Los comentarios como este no se muestran. -->

## Issue

<!-- `Closes #n` si este PR termina el issue; `Refs #n` si lo deja abierto. Deja una sola de las dos. -->

Closes #

## Qué cambia y por qué

<!-- Máximo tres líneas. Si no cabe en tres, el PR es demasiado grande: se parte. -->

## Qué cubre del SRS

<!-- Pasos, extensiones, garantías o criterios de aceptación. Ejemplo: UC06 paso 4, ext. 4a, GE 4.
     Si es infraestructura o documentación sin requisito detrás, escribe «No aplica» y di por qué. -->

## Cómo ejecutarlo

<!-- Para quien revisa. Los pasos para recrear el flujo y el resultado que se espera ver: la aplicación,
     una prueba de aceptación, un comando o un script. Si es solo documentación, qué abrir y qué comprobar. -->

1.
2.

**Resultado esperado:**

## Definición de Hecho

<!-- Copia de docs/proceso/definicion-de-hecho.md. Si cambia una, cambia la otra. -->

- [ ] Va contra `develop` y no se fusiona sin la aprobación de otra persona del equipo.
- [ ] Si incluye código, se puede ejecutar —por quien lo escribió y por quien lo revisa— con los pasos de arriba, y el resultado muestra que hace lo que debe.
- [ ] Tiene pruebas unitarias en verde, y las pruebas de arquitectura pasan en la integración continua (`./mvnw verify`).
- [ ] Cumple Google Java Style (`./mvnw spotless:apply`).
- [ ] Si es una historia, sus criterios de aceptación se demostraron funcionando.

## Antes de pedir revisión

- [ ] Los commits son atómicos y cada mensaje cita su ID (`<ID>: resumen`, `Refs #n`).
- [ ] Cada prueba nueva lleva en su nombre el ID del SRS que verifica.
- [ ] El diff no trae credenciales, datos personales ni archivos locales.
- [ ] Si cambia un comportamiento, actualicé el documento que lo describe: <!-- cuál --> (o este PR no cambia ninguno).

<!-- Quien revisa: sigue la lista de CONTRIBUTING.md, sección «Revisión». -->
