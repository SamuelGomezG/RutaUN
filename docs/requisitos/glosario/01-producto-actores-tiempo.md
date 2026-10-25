---
title: "Glosario 1 · Producto, actores, operación y tiempo"
doc_type: "glosario"
srs_ids:
  - Administrador de normativa
  - Aviso de lo que RutaUN no verifica
  - Estudiante
  - Historial del consolidado
  - Incidente
  - Oferta, cupos y sobrecupos
  - Periodo académico
  - Periodo en curso
  - Sesión
  - Supuestos del equipo
  - Visitante
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
---
# Glosario 1 · Producto, actores, operación y tiempo

> **Cuándo cargar:** tareas de actores, sesión, periodo en curso, incidentes, historial del consolidado y aviso del sistema.

> Términos: **11** de 71 del glosario; texto verbatim (solo se renivela el encabezado `###` → `##`). Índice completo en [README.md](README.md).

## Administrador de normativa

Rol que carga y mantiene el consolidado normativo de los programas y opera RutaUN; en la primera versión lo asumirá el equipo Zeus. Es el actor principal de UC07 (Incorporar acuerdo al consolidado del programa) y el interesado que recibe los incidentes, tanto los del consolidado como los de errores internos. **Fuera de UC01 a UC07:** crea los programas (al crear uno, el sistema le lista las normas ya registradas cuyo alcance lo comprende, que él le incorpora con UC07; ver «Programa»); registra el periodo en curso y lo avanza cuando empieza un periodo nuevo (ver «Periodo en curso»); consulta el historial del consolidado; y consulta los incidentes y los marca como atendidos (si uno atendido vuelve a ocurrir, se registra de nuevo). También corrige los errores de digitación y completa o corrige los enlaces de lo ya registrado, con las mismas validaciones del paso 6 de UC07; cada corrección crea una versión nueva de los consolidados afectados, con motivo «corrección», que queda en el historial del consolidado (supuesto del equipo).

## Aviso de lo que RutaUN no verifica

Texto fijo que RutaUN muestra con todo plan, comparación o explicación: el plan es una guía, no una inscripción ni una autorización, y RutaUN no verifica la oferta de materias, los cupos y sobrecupos, los horarios y cruces de clases, el cupo de créditos (Acuerdo 008 de 2008, art. 11), los reconocimientos (validaciones, homologaciones y convalidaciones), las autorizaciones del Consejo de Facultad, si el estudiante debe cursar materias de nivelación ni el cumplimiento de los requisitos de grado, ni describe de qué trata cada materia. Aclara que el plan estima cuándo el estudiante culminaría su plan de estudios, no cuándo se graduaría (ver «Culminación del plan de estudios»), y recuerda que la fuente oficial de las normas es Régimen Legal.

## Estudiante

Persona que cursa un programa de pregrado de la Sede Bogotá cargado en el sistema y usa RutaUN, con su cuenta, para planear su carrera. Es el actor principal de UC01 a UC04; como visitante con sesión, usa también UC05 y UC06, y cuando consulta su propio programa ve además su avance (ver «Visitante»). **Fuera de UC01 a UC07:** crea su cuenta de RutaUN y elige su programa entre los que ya tienen consolidado. También puede **cambiar de programa** en su cuenta (por ejemplo, por un traslado), con confirmación previa: el sistema conserva las marcas de las materias que existen en el programa nuevo (mismo código), lista y descarta las demás (lo que la Universidad le reconozca lo registra él en UC03, extensiones 3d y 3e), elimina su plan vigente, que se calculó para otro programa, y le pide actualizar sus semestres cursados (supuesto del equipo). Tiene un solo programa a la vez: la doble titulación queda fuera de alcance.

## Historial del consolidado

Registro de las versiones del consolidado de un programa: número, fecha de creación, motivo y administrador que la creó, si la creó un administrador. El motivo es la norma incorporada, una «corrección» o un motivo automático: empezó o dejó de regir una regla, por el avance del periodo en curso o por la llegada de una fecha, con el periodo o la fecha que lo originó. El administrador de normativa lo consulta fuera de UC01 a UC07.

## Incidente

Registro que el sistema deja al administrador de normativa cuando el consolidado impide atender a un usuario (por ejemplo, un prerrequisito circular o una malla que no es utilizable) o cuando falta registrar el periodo en curso, cuando una regla que se muestra o se aplica no tiene fuente o está en revisión, cuando una materia que ya no rige no se puede contar («Materia que ya no rige»), cuando un estudiante con sesión reporta un enlace que no abre o cuando ocurre un error interno. Incluye fecha, caso de uso y paso en que ocurrió (por ejemplo, «UC01, paso 5»), programa, versión del consolidado, causa y elementos involucrados (materia, agrupación, regla o enlace); los incidentes con el mismo programa, la misma versión, la misma causa y los mismos elementos involucrados se agrupan en un solo registro, que guarda la primera y la última fecha y cuenta cuántas veces ocurrieron. **Los visitantes no lo ven**; el administrador de normativa los consulta y los marca como atendidos fuera de UC01 a UC07 (si uno atendido vuelve a ocurrir, se registra de nuevo).

## Oferta, cupos y sobrecupos

Qué materias se abren en un periodo, cuántos cupos tiene cada grupo y si se aprueba un sobrecupo. **RutaUN no verifica nada de esto** ni los horarios o cruces de clases: el plan es una guía, no una inscripción.

## Periodo académico

Semestre de la Universidad, identificado como AAAA-1 o AAAA-2 (por ejemplo, 2027-1). Los periodos se suceden en orden: después de 2026-2 viene 2027-1, y después, 2027-2. RutaUN no guarda sus fechas de inicio ni de fin, porque se conocen tarde y cambian (por ejemplo, con un paro): solo sabe cuál es el periodo en curso (ver «Periodo en curso»).

## Periodo en curso

Periodo académico que el administrador de normativa tiene registrado como el último que empezó (por ejemplo, 2026-2); entre dos periodos, durante las vacaciones, sigue siendo el último que empezó. **Es el único dato de tiempo académico que RutaUN guarda.** Es el semestre 0 de todo plan que se calcule mientras rige, y el periodo siguiente es su semestre 1. El administrador lo avanza al periodo siguiente cuando ese periodo empieza, sin importar si el calendario de la Universidad se corrió (supuesto del equipo); lo hace fuera de UC01 a UC07 («Avanzar periodo») y **solo puede avanzarlo de uno en uno**. Al avanzarlo:
- (a) las reglas programadas para el periodo nuevo empiezan a regir y las normas temporales cuya «vigencia hasta» era el periodo anterior dejan de regir, con lo que el sistema crea una versión nueva del consolidado de cada programa afectado (ver «Versión del consolidado»);
- (b) los planes vigentes no se recalculan ni quedan desactualizados «por eso»: su semestre actual pasa a ser el del periodo nuevo (ver «Semestre actual del plan»), y solo quedan desactualizados si cambia una regla que aplican (ver «Plan desactualizado»);
- (c) las marcas «en curso» de periodos anteriores quedan «por actualizar» (ver «En curso»);
- (d) los semestres cursados registrados en un periodo anterior se señalan para que el estudiante los actualice (ver «Semestres cursados»); y
- (e) cada estudiante cuyo avance se guardó por última vez en un periodo anterior ve, al entrar, un recordatorio para actualizarlo (ver «Plan vigente» y UC03, extensión 2h).

## Sesión

Acceso autenticado del estudiante o del administrador a RutaUN con su cuenta de RutaUN (no se usa la cuenta institucional de la Universidad). UC05 (Entender restricciones de una materia) y UC06 (Consultar regla del programa y su acuerdo) no la requieren (ver «Visitante»); reportar un enlace roto sí. **La sesión expira tras treinta minutos sin acciones del usuario**: clics, teclas o envíos; las consultas automáticas de la página no cuentan como actividad (supuesto del equipo). Lo que no se guardó al expirar se pierde.

## Supuestos del equipo

Decisiones que el equipo tomó donde las normas o la entrevista no dicen nada; cada una se señala como «supuesto del equipo» donde se aplica. Siguen por confirmar el (1), que las normas revisadas no tratan, y la cifra de (3), que se ajustará en la implementación. **La lista completa S1–S17 está en §8.**

## Visitante

Cualquier persona que consulta RutaUN, haya iniciado sesión o no. Es el actor principal de UC05 (Entender restricciones de una materia) y UC06 (Consultar regla del programa y su acuerdo), que no requieren sesión: puede consultar la normativa, la malla, el cuadro de prerrequisitos y las restricciones de las materias de cualquier programa cargado con consolidado vigente («Cualquier programa»). Un estudiante con sesión es un visitante que además tiene cuenta y programa: cuando consulta su propio programa («Mi programa»), UC05 y UC06 le muestran también el estado de su avance y, como dato adicional, lo que dice su plan vigente. Reportar un enlace roto requiere sesión. RutaUN no guarda las consultas.

---

