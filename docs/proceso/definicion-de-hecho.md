# Definición de Hecho

Un elemento (historia, tarea técnica, spike o documento) cuenta como terminado solo si cumple todo lo siguiente:

- Está integrado en `develop` y lo revisó otra persona del equipo.
- Si incluye código, se puede ejecutar —por quien lo escribió y por quien lo revisa— para recrear el flujo que resuelve y obtener un resultado observable que muestre que hace lo que debe: la aplicación, una prueba de aceptación, un comando o un script, con los pasos en el PR.
- Tiene pruebas unitarias en verde, y las pruebas de arquitectura pasan en la integración continua.
- Cumple Google Java Style (el build lo verifica).
- Si es una historia, sus criterios de aceptación se demostraron funcionando y su issue quedó cerrado en el tablero.

Esta es la definición del criterio de terminado del plan del sprint 1, ampliada el 25 de octubre de 2026 con la ejecución del código. Pasar a `main` no es parte de ella: ocurre una vez por sprint, cuando la demostración lo da por completo y satisfactorio (ver `CONTRIBUTING.md`).
