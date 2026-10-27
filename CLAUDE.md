# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Notas para Claude Code

- **Requisitos.** Empieza por `docs/requisitos/README.md` y carga solo los archivos que tu tarea necesita (el router tarea→archivos ya los lista). Cada archivo es corto y no hay un documento único gigante; busca por ID (`UC06-HU01`, `S18`, `ext. 4a`).
- **Diseño.** El diseño de clases completo no está en este repositorio. Lo vigente para programar está en `AGENTS.md`; si necesitas un detalle que no está ahí ni en `docs/diseno/`, pregunta en vez de inventarlo (regla de oro 3).
- **Antes de dar algo por terminado**, corre `./mvnw verify` (formato, estilo, arquitectura y pruebas). Si algo falla, dilo con la salida; no lo ocultes. Ejecuta además el flujo que cambiaste y comprueba que el resultado sea el esperado: la Definición de Hecho lo exige.
- **Cambios pequeños.** Un issue, un cambio, y cita los IDs que cubre (`#n`, `UCnn-HUmm`).
- **Idioma.** Escribe y responde en español, con el vocabulario del glosario tal cual (`docs/requisitos/glosario/`). Los identificadores de Java van en ASCII.
- **Nunca** pongas credenciales, el contenido de `data/` ni datos personales en el repositorio, en los logs ni en las pruebas.
