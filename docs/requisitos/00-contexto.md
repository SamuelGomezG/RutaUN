---
title: "RutaUN · Contexto del producto y reglas de trabajo"
doc_type: "orientación"
srs_ids:
  - UC01
  - UC02
  - UC03
  - UC04
  - UC05
  - UC06
  - UC07
depends_on:
  - 01-invariantes.md
  - 04-supuestos.md
  - 07-lagunas.md
---
## 0′ · Cuándo cargar este archivo

> Siempre, primero. Toda tarea en este repo empieza aquí: resumen del producto, alcance, actores y reglas de trabajo del agente. Los puntos 1–7 del bloque «Reglas de trabajo» (§0) son obligatorios en cada tarea.

## 0″ · Ficha del documento original

> Ficha ("front matter") del documento consolidado, reproducida verbatim:
> ---
> proyecto: RutaUN (nombre provisional)
> equipo: Zeus
> curso: Ingeniería de Software I · SIA 2016701 · Grupo 4 · 2026-II
> institucion: Universidad Nacional de Colombia · Sede Bogotá · Facultad de Ingeniería · Departamento de Ingeniería de Sistemas e Industrial
> entrega: "Entrega 2 · Especificación de requisitos (fecha de entrega: 7 de octubre de 2026)"
> fuente: "Zeus_Entrega2_Especificacion_de_requisitos-{1,2,3}.pdf (101 páginas en total)"
> idioma: es
> tipo_de_documento: "contexto de proyecto para agentes de IA (consolidación fiel de la especificación de requisitos; NO es un diseño técnico)"
> estado_del_sistema: sin desarrollar (este documento describe cómo se usará)
> ---
>

---

# RutaUN — Contexto de proyecto para agentes de IA

> Consolidación en un solo archivo de los tres PDF de la **Entrega 2 · Especificación de requisitos** del equipo Zeus. Sustituye a los PDF como fuente de contexto: contiene la nota de cambio de alcance, el Actor-Goal List, el glosario, los 7 casos de uso *fully dressed*, el diagrama de casos de uso (en texto), las 47 historias de usuario, el backlog y el acta de la segunda entrevista (ZEUS-02).
> Se quitó lo puramente editorial de los PDF (portada, encabezados y pies, índice paginado, capturas de pantalla). Se conserva el español original y la numeración original de pasos, extensiones, garantías e interesados, para que cualquier referencia cruzada siga valiendo.

# 0. Cómo usar este documento (instrucciones para el agente)

**Estado del proyecto.** Existe un **diseño vigente** (diseño de clases del núcleo, 20 de octubre de 2026; su documentación vive en `docs/diseno/`) que fija el stack y la estructura: Spring Boot con MVC y Thymeleaf, H2 en modo archivo con Flyway, cuatro capas con dependencias solo hacia abajo, y los contextos Normativa, Avance y Plan de carrera, con Periodo en curso, Identidad e Incidentes como soporte. Este documento sigue siendo la fuente de los requisitos; el diseño, de las decisiones técnicas. Lo que el diseño no fija se propone y se confirma; no se asume. (Detalle y decisiones del 25 de octubre: `srs/08-deriva-documental.md`.)

**Lectura por capas** (lee solo hasta donde necesites):

| Capa | Secciones | Para qué |
|---|---|---|
| 1 · Orientación | §1 Resumen, §2 Alcance, §3 Actores y metas, §4 Invariantes | Entender el producto y no violar sus reglas de oro. Suficiente para tareas pequeñas. |
| 2 · Dominio | §5 Mapa de entidades, §6 Marco normativo y siglas, §7 Glosario, §8 Supuestos del equipo | Las reglas de negocio exactas (cálculo del plan, vigencia, consolidado). **Léelo antes de implementar cualquier cálculo o validación.** |
| 3 · Comportamiento | §9 Casos de uso UC01–UC07 | Flujos, garantías y excepciones: la especificación testeable. |
| 4 · Unidades de trabajo | §10 Diagrama UML, §11 Historias de usuario (47), §12 Backlog | Cada historia (`UCnn-HUmm`) es una unidad de trabajo con criterios de aceptación. |
| 5 · Evidencia | §13 Acta ZEUS-02, §14 Lagunas y preguntas abiertas | Origen de requisitos, feedback de UX y lo que quedó sin resolver. |

*Tamaño aproximado (tokens, estimado): §0–§6 ≈ 9 k · glosario y supuestos (§7–§8) ≈ 22 k · casos de uso (§9) ≈ 46 k · diagrama, historias, backlog, acta y lagunas (§10–§14) ≈ 16 k. Total ≈ 90–100 k. Si el contexto es limitado, carga §0–§6 siempre y trae el resto por demanda (busca por ID: `UC03`, `UC03-HU05`, «Plan vigente»…).*

**Reglas de trabajo para el agente**

1. **Traza todo a IDs.** Cita en código, tests, commits y PRs los identificadores del documento: `UC03-HU05`, `UC03 ext. 7a`, `UC01 paso 10`, `UC01 GM 1` / `GE 4` (garantía mínima / de éxito), `[I3]` (interesado), `S3` (supuesto del equipo).
2. **No inventes normas ni reglas académicas.** La normativa entra al sistema como *datos* del consolidado de cada programa (la carga el administrador con UC07), no como constantes del código. Las cifras que aparecen en el documento (mínimo de 10 créditos por periodo, máximo temporal de 23 créditos, etc.) son **ejemplos de datos** de normas reales, citadas en §6.
3. **Cuando el documento calla, no decidas en silencio.** Busca primero en §8 (Supuestos S1–S17). Si tampoco está allí, trátalo como un hueco: pregunta al usuario o regístralo como supuesto nuevo y márcalo como tal.
4. **Respeta el fuera de alcance** (§2.2). Varias metas están descartadas a propósito (doble titulación, oferta/cupos, integración con el SIA, etc.).
5. **Nunca presentes un plan como inscripción ni como autorización.** Todo plan, comparación o explicación lleva el *Aviso de lo que RutaUN no verifica* (ver §4 y glosario).
6. **Vocabulario.** El dominio está en español; usa los términos del glosario (§7) tal cual en la UI y en la documentación. Si el proyecto necesita identificadores en inglés en el código, la correspondencia es una decisión del equipo: propón una y confírmala. Las frases entre «…» del documento son, según el caso, nombres de casos, términos definidos, rótulos de interfaz, ejemplos o citas textuales de normas.
7. **Privacidad por diseño.** Avance y planes son datos personales (Ley 1581 de 2012): solo los ve su dueño con sesión; el administrador solo ve cantidades.
8. **Procedencia del documento.** El texto describe intenciones de producto y decisiones del equipo (incluidos supuestos aún por confirmar). Es la mejor fuente disponible, pero no es normativa oficial.

**Rótulos de interfaz que el documento cita como texto de acción o de señal** (úsalos literalmente salvo que el equipo decida otra cosa):
«Generar plan de carrera» · «Replanificar plan de carrera» · «Registrar avance en la malla» · «Guardar avance» · «Comparar alternativas de carga» · «Ver restricciones» · «Ver cadena de requisitos» · «Consultar regla del programa y su acuerdo» · «Incorporar acuerdo al consolidado del programa» · «Mi programa» / «Cualquier programa» · «Reportar enlace roto» · «Avanzar periodo» · «requiere autorización del Consejo de Facultad» (marca de autorización) · «sin fuente registrada» · «en revisión» · «plan cumplido» · «no se puede cursar desde este programa» · «solo informativa: el plan no la aplica» · «temporal» · estados de regla: «vigente», «programada», «ya no rige» · «Materias que ya no rigen» · «en curso» (marca por actualizar) · rótulo de semestre «Semestre 1 (2027-1)» · mensaje «Estás en el semestre 2 (2027-2) de tu plan».

**Fuentes citadas pero no incluidas aquí:** Entrega 0 (hipótesis del problema), Entrega 1 (alcance previo), acta de la primera entrevista **ZEUS-01** (se cita como «acta ZEUS-01, sección n» y en citas textuales) y los textos de las normas (Acuerdos, Resolución, Circular). No asumas su contenido más allá de lo que este documento reproduce.

# 1. Resumen del producto

**RutaUN** (nombre provisional) es una herramienta para que los **estudiantes de pregrado de la Sede Bogotá de la Universidad Nacional de Colombia (UNAL)** planeen su carrera semestre a semestre sobre la normativa **consolidada** de su programa.

- El estudiante **registra su avance** en la malla (qué aprobó, qué cursa, qué le falta por componente y agrupación), **genera un plan de carrera** dentro de un tope de carga (créditos y/o materias por semestre), **replanifica** cuando se atrasa o cambia algo y **compara alternativas de carga** para ver cuántos semestres le faltan con cada una.
- La normativa de cada programa vive en un **consolidado normativo versionado**: reglas con su fuente (norma, artículo o numeral), su vigencia y su alcance, más la **malla estándar** del programa. Lo carga y mantiene el **Administrador de normativa** (en la primera versión, el equipo Zeus) con UC07.
- **Cualquier visitante, con o sin sesión,** puede consultar la normativa y las restricciones de las materias de cualquier programa cargado (UC05, UC06). Un estudiante con sesión que consulta su propio programa ve además su avance.
- RutaUN **es una guía, no una inscripción ni una autorización, y no es fuente oficial** (la fuente oficial es *Régimen Legal*). No se integra con el SIA ni consulta Régimen Legal en vivo.
- **Evidencia de origen del problema** (entrevistas): los estudiantes llevan su avance rayando el PDF de la malla (con doble titulación, cuadrando las dos mallas en Paint) y organizan materias y horarios en un Excel; la normativa de un programa no está reunida en un solo lugar (ni siquiera dentro de un mismo programa, p. ej. Ingeniería Industrial); la carga se decide "sin ver su efecto".

**Prioridad de producto** (orden de los casos de uso): UC01 es el más relevante y UC07 el último.

| UC | Caso de uso | Actor principal |
|---|---|---|
| UC01 | Generar plan de carrera | Estudiante |
| UC02 | Replanificar plan de carrera | Estudiante |
| UC03 | Registrar avance en la malla | Estudiante |
| UC04 | Comparar alternativas de carga | Estudiante |
| UC05 | Entender restricciones de una materia | Visitante (con o sin sesión) |
| UC06 | Consultar regla del programa y su acuerdo | Visitante (con o sin sesión) |
| UC07 | Incorporar acuerdo al consolidado del programa | Administrador de normativa |

Todos los casos de uso son de **nivel «Objetivo de usuario»**, **sistema: RutaUN**, y son **independientes del programa**: describen lo que hace el sistema con cualquier programa de pregrado cargado. El equipo prevé empezar por **Ingeniería de Sistemas y Computación** e **Ingeniería Industrial**; cargar otros programas no cambia los casos de uso.

# 2. Alcance

## 2.1 Nota de cambio de alcance respecto a la Entrega 1

*Qué decía la Entrega 1:* definía el consolidado de «la normativa del plan de estudios de Ingeniería de Sistemas y Computación (un solo plan)» y dejaba fuera la «cobertura de otros programas y sedes de la universidad».

*Qué cambia en la Entrega 2:*

1. **RutaUN no se limita a un programa.** Ningún caso de uso depende del programa: cada estudiante tendrá asociado un programa cargado en el sistema, y el administrador de normativa cargará y actualizará los consolidados de uno o varios programas (UC07). Se prevé empezar por Ingeniería de Sistemas y Computación e Ingeniería Industrial; cargar otros programas no cambia los casos de uso.
2. **«Un solo plan» queda aclarado.** En la Entrega 1 significaba un solo plan de estudios (un programa). En los casos de uso, cada estudiante tiene **un solo programa a la vez** —puede cambiarlo en su cuenta, por ejemplo por un traslado— y **un plan de carrera vigente**, que se reemplaza cuando guarda uno nuevo.
3. **La causa «falta de normativa consolidada» tiene más evidencia.** La Entrega 1 la dejó matizada porque la evidencia venía del segundo programa del entrevistado. Un integrante del equipo verificó después, buscando los acuerdos que rigen Ingeniería Industrial, que también dentro de un solo programa esas reglas no están reunidas en un solo lugar. Se refleja en UC06.
4. **Quién carga el consolidado.** Lo que la Entrega 1 llamaba «lo carga el equipo» es ahora el caso de uso UC07, con el rol de administrador de normativa, que en la primera versión asumirá el equipo.
5. **La normativa se consulta sin cuenta.** La normativa de cualquier programa cargado y las restricciones de sus materias se pueden consultar sin iniciar sesión (UC05 y UC06), con el actor Visitante. Un estudiante con sesión ve además su avance cuando consulta su propio programa.

## 2.2 Qué sigue fuera de alcance (igual que en la Entrega 1)

- Otras sedes (RutaUN es para estudiantes de **pregrado de la Sede Bogotá**).
- Integración con el **SIA** (Sistema de Información Académica); no se lee la historia académica: el avance es lo que el estudiante declara.
- **Oferta de materias, cupos y sobrecupos, horarios**, cruces de clases.
- **Doble titulación** (planear con dos programas simultáneos; cada estudiante tiene un solo programa a la vez).
- **Trámites del reglamento estudiantil** (reintegros, homologaciones, movilidad).
- Describir **de qué trata cada materia**: el estudiante elige sus optativas con información que consigue por fuera de RutaUN.
- **Actualización automática del consolidado cuando cambian los acuerdos o la malla**: el consolidado cambia solo cuando el administrador incorpora un acuerdo. Lo único automático es que, cuando una regla ya cargada empieza o deja de regir (porque el administrador avanza el periodo en curso o porque llega su fecha), el sistema crea una versión nueva con las mismas reglas.
- No se verifica: cupo de créditos (Acuerdo 008 de 2008, art. 11), reconocimientos (validaciones, homologaciones, convalidaciones), autorizaciones del Consejo de Facultad, materias de nivelación, requisitos de grado.
- La convivencia de versiones del plan de estudios por cohorte: una reforma del plan crea una **versión nueva del mismo programa** y la más reciente rige para todos sus estudiantes (ver «Programa»).

# 3. Actores y metas (Actor-Goal List)

Tres actores y trece metas; **siete se desarrollan como caso de uso**.

| Actor | Meta | ¿Caso de uso? |
|---|---|---|
| Estudiante | Registrar mi avance en la malla (marcar lo aprobado y lo que curso, ver los créditos y las materias que me faltan por componente y por agrupación, y ver si voy atrasado o adelantado respecto a la malla estándar) | **SÍ** · UC03 |
| Estudiante | Generar mi plan de carrera semestre a semestre dentro de mi tope de carga y saber en cuántos semestres culmino mi plan de estudios | **SÍ** · UC01 |
| Estudiante | Replanificar mi plan cuando me atraso, adelanto materias, cambio de optativa o cambio mi carga | **SÍ** · UC02 |
| Estudiante | Comparar alternativas de carga y ver cuántos semestres me faltan con cada una | **SÍ** · UC04 |
| Estudiante | Ver qué materias puedo cursar el próximo semestre | NO |
| Estudiante | Ver en qué semestre de mi plan voy | NO |
| Estudiante | Guardar mi plan y retomarlo después | NO |
| Estudiante | Cambiar de programa en mi cuenta | NO |
| Estudiante | Planear mi carrera con doble titulación entre programas | NO |
| Visitante | Entender qué exige una materia para cursarla (y, si soy estudiante, qué me falta) | **SÍ** · UC05 |
| Visitante | Consultar una regla de un programa y el acuerdo del que sale | **SÍ** · UC06 |
| Administrador de normativa | Incorporar un acuerdo (base, modificatorio o de excepción/medida temporal) al consolidado de uno o varios programas, con su fuente, su fecha de aplicación, si la tiene, su «vigencia hasta», su alcance y, si es el acuerdo del plan de estudios, la malla estándar | **SÍ** · UC07 |
| Administrador de normativa | Avanzar el periodo en curso cuando empieza un periodo nuevo | NO |

*(La columna de UC es la correspondencia obvia entre meta y caso de uso; el Actor-Goal List original solo marca SÍ/NO.)*

**Notas del Actor-Goal List**

- Los actores son **roles, no personas**, y **el programa es un dato del sistema, no un actor**. El sistema aún no está desarrollado: las metas describen cómo se usará.
- **Estudiante:** quien cursa un programa de pregrado de la Sede Bogotá cargado en el sistema y tiene cuenta en RutaUN. Tiene un solo programa a la vez y puede cambiarlo en su cuenta.
- **Visitante:** cualquier persona que consulta RutaUN, haya iniciado sesión o no. Consultar la normativa y las restricciones de las materias de cualquier programa no requiere cuenta; **un estudiante con sesión es un visitante** que, cuando consulta su propio programa, ve además su avance.
- **Administrador de normativa:** rol que en la primera versión asumirá el equipo Zeus. «Acuerdo» se usa en sentido genérico: también incluye resoluciones y circulares.
- **Sin actores secundarios:** el sistema no se integrará con el SIA ni consultará Régimen Legal en vivo.
- **Fuera de escena:** los órganos que expiden, proponen o aplican las normas —el Consejo Superior Universitario y su Comisión Delegataria, la Vicerrectoría Académica, el Consejo Académico, los Consejos de Facultad y, en cada programa, la Dirección de Área Curricular y el Comité Asesor— y Régimen Legal, donde se publican. No van en la tabla: sus intereses se registran en «Interesados e intereses» de cada caso de uso (interesado «Órganos y Régimen Legal»).
- **Por qué «No»:** «Ver qué materias puedo cursar el próximo semestre» lo cubre «Generar mi plan…», que es su semestre 1; «Ver en qué semestre de mi plan voy» lo muestra el plan vigente al abrirlo (glosario, «Semestre actual del plan»); «Guardar mi plan…» es una subfunción; «Cambiar de programa en mi cuenta» y «Avanzar el periodo en curso» son acciones de un solo paso, cuyos efectos se describen en el glosario de los casos de uso («Estudiante» y «Periodo en curso»); la doble titulación quedó fuera del alcance en la Entrega 1.

