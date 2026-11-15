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

<!-- Para quien revisa. Un conteo de pruebas o una mutación no son «Qué se ve», salvo cuando el
     cambio son las pruebas mismas: ahí la mutación que las rompe es el flujo. En los demás casos
     el flujo se demuestra con el código real (docs/proceso/definicion-de-hecho.md, «Cómo se
     demuestra un cambio»). Si es solo documentación, qué abrir y qué comprobar. -->

**Flujo que resuelve:**

<!-- Una frase. Si el cambio no tiene flujo de usuario, di cuál es el flujo. -->

**Pasos:**

<!-- Lista numerada, con ./mvnw y, si difiere, el comando de Windows. -->

1.
2.

**Qué se ve cuando funciona:**

<!-- Lo que se observa, con las cifras o las líneas exactas. Si es un defecto, también lo que se veía antes. -->

**Pruebas:**

<!-- ./mvnw verify y las pruebas nuevas, con su conteo. Es evidencia del tercer criterio, no del segundo. -->

## Definición de Hecho

<!-- Copia de docs/proceso/definicion-de-hecho.md. Si cambia una, cambia la otra. -->

1. [ ] Está integrado en `develop` y lo revisó otra persona del equipo.
2. [ ] Si incluye código, se puede ejecutar —por quien lo escribió y por quien lo revisa— para recrear el flujo que resuelve y obtener un resultado observable que muestre que hace lo que debe: la aplicación, una prueba de aceptación, un comando o un script, con los pasos en el PR.
3. [ ] Tiene pruebas unitarias en verde, y las pruebas de arquitectura pasan en la integración continua.
4. [ ] Cumple Google Java Style (el build lo verifica).
5. [ ] Si es una historia, sus criterios de aceptación se demostraron funcionando y su issue quedó cerrado en el tablero.

## Antes de pedir revisión

- [ ] Los commits son atómicos y cada mensaje cita su ID (`<ID>: resumen`, `Refs #n`).
- [ ] Cada prueba nueva lleva en su nombre el ID del SRS que verifica.
- [ ] El diff no trae credenciales, datos personales ni archivos locales.
- [ ] Si cambia un comportamiento, actualicé el documento que lo describe: <!-- cuál --> (o este PR no cambia ninguno).

<!-- Quien revisa: sigue la lista de CONTRIBUTING.md, sección «Revisión». -->
