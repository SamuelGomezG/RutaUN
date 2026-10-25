---
title: "Supuestos del equipo (S1–S18)"
doc_type: "supuestos"
srs_ids:
  - S1
  - S2
  - S3
  - S4
  - S5
  - S6
  - S7
  - S8
  - S9
  - S10
  - S11
  - S12
  - S13
  - S14
  - S15
  - S16
  - S17
  - S18
depends_on:
  - 00-contexto.md
  - 01-invariantes.md
---
# 8. Supuestos del equipo (S1–S18)

> **Cuándo cargar:** antes de cualquier cálculo o validación. La lista abajo es la fuente de los «supuesto del equipo» citados sin número en glosario y casos (S1 → (1), … S17 → (17); S18 es la decisión del 25 de octubre, ver S18 abajo).

Decisiones que el equipo tomó donde las normas o la entrevista no dicen nada. En el documento original se numeran (1) a (17) y se señalan como «supuesto del equipo» donde se aplican. **S1 y S3 siguen por confirmar: el S1 (que las normas revisadas no tratan) y la cifra del S3 (se ajustará en la implementación; el spike TT-05 la medirá). El S18 es una decisión ya aplicada (25 de octubre de 2026).**

| ID | Supuesto |
|---|---|
| **S1** | Los créditos que excedan lo que exige una agrupación **no reducen lo exigido a otra agrupación, a la agrupación que la contiene ni a la libre elección**, y los créditos de una optativa elegida para cumplir un prerrequisito cuentan en su agrupación. Lo mismo vale para una materia que figura en varias agrupaciones y se marca cuando todas están cubiertas: es un **excedente sin agrupación asignada**, porque no se sabe si la Universidad la cuenta como libre elección o de otro modo («Agrupación», «Cálculo del plan», «Agrupación asignada»). Ni el Acuerdo 003-A de 2022 del Consejo Académico ni el Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería tratan el tema. *(Por confirmar.)* |
| **S2** | Los cursos de libre elección valen, en su mayoría, 3 o 4 créditos. Para que el plan nunca supere el límite de materias del tope, se cuenta el caso más desfavorable (cursos de 3 créditos): **un bloque de N créditos cuenta como ⌈N/3⌉ materias** («Bloque de libre elección»). |
| **S3** | **Todo cálculo se cancela a los treinta segundos**, una cota conservadora dado el tamaño de las mallas, que se validará en la implementación («Cálculo del plan»). *(La cifra se ajustará.)* |
| **S4** | RutaUN **no guarda fechas de los periodos**: el administrador avanza el periodo en curso cuando empieza el periodo nuevo, aunque el calendario se haya corrido. El plan supone que **en todos sus semestres regirán las mismas reglas del plan que rigen al calcularlo**, porque RutaUN no puede saber si la normativa cambiará ni cuándo: las reglas temporales y las programadas no cambian el cálculo, solo se avisan en el semestre en que empiezan o dejan de regir («Periodo en curso» y «Vigencia»). |
| **S5** | **La sesión expira a los treinta minutos sin acciones del usuario**: da tiempo de revisar un plan o una comparación sin perder lo marcado y protege los datos personales del estudiante y el consolidado que modifica el administrador («Sesión»). |
| **S6** | El cálculo **cuenta cada marca con los créditos, el componente y la agrupación que la materia tiene en la versión del consolidado que usa** (si figura en varias agrupaciones, su agrupación asignada). Los créditos de una materia son **fijos por código**: cambiarlos crea otra materia; el nombre vive en cada versión («Materia»). Una materia que ya no rige cuenta como su materia equivalente, si una equivalencia lo establece, o con lo que tenía en la última versión en que rigió («Avance» y «Materia que ya no rige»). |
| **S7** | Una materia que la Universidad le reconoció al estudiante y que no figura en la malla se registra como **materia reconocida**: nombre, código si lo tiene, créditos y agrupación (UC03, extensión 3e). Suma créditos a esa agrupación y no cumple ningún prerrequisito ni correquisito. |
| **S8** | Una **incorporación de normas se hace en una sola sesión**, sin guardarla a medias; las reglas se digitan o se cargan desde un archivo JSON dentro de la misma sesión (UC07, paso 5). |
| **S9** | **Validar las reglas y construir la vista previa de UC07 se cancela a los sesenta segundos** (UC07, extensiones 6g y 7c). |
| **S10** | Las **marcas «en curso» de un periodo anterior al periodo en curso se siguen contando como aprobadas** hasta que el estudiante las actualice; RutaUN solo se lo recuerda («En curso»). |
| **S11** | Si una norma no dice a qué programas aplica, **se supone que aplica a todos los programas sobre los que tiene competencia el órgano que la expide** («Alcance» y UC07, extensión 4h). |
| **S12** | **Quien cambia de programa conserva las marcas de las materias que existen en el programa nuevo** (mismo código), descarta las demás y **pierde su plan vigente**; la derogación o el reemplazo del plan de estudios de un programa quedan fuera de UC01 a UC07 («Estudiante» y «Programa»). |
| **S13** | El administrador de normativa **corrige fuera de UC07 los errores de digitación y los enlaces de lo ya registrado**, y consulta los incidentes y los marca como atendidos; cada corrección crea una versión nueva con motivo «corrección» («Administrador de normativa»). |
| **S14** | Una **regla de habilitación mide créditos aprobados de un componente o del programa**, y el cálculo los cuenta como dice «Regla de habilitación». |
| **S15** | La **versión automática del consolidado que origina una fecha se crea al comenzar ese día (hora de Colombia), o en cuanto el sistema esté disponible, y solo se crea una por programa y día** («Versión del consolidado»). |
| **S16** | Cuando una norma cuenta la vigencia de una regla desde un hecho que su texto no fecha (como su implementación en el SIA), **el administrador registra el periodo que conste en la información oficial con que cuente** («Vigencia»). |
| **S17** | Las **materias externas no se muestran**: las de nivelación se dan por cumplidas, porque solo las cursa quien las debe por su puntaje de admisión, y las de otro programa, por no cumplibles, porque el SIA no deja inscribirlas a los estudiantes del programa («Materia externa»). |
| **S18** | Los estados de una restricción se combinan **por certeza**, en este orden: cumplida > en curso > incierta > pendiente. Una «y» toma el peor de sus términos y una «o», el mejor; una **restricción incierta** se trata como cumplida, con aviso, y una materia obligatoria cuyo reemplazo es incierto se sigue incluyendo, con el aviso de que podría estar cubierta o no (ver «Correquisito», «Restricción (de una materia)» y «Materia que ya no rige»). *(Cambio n.º 8 del diseño, aplicado el 25 de octubre de 2026.)* |

*Nota:* los identificadores S1–S17 corresponden a los números (1)–(17) del glosario original. En el cuerpo de los casos de uso, los supuestos aparecen como «(supuesto del equipo)» sin número; por ejemplo, la regla de que las marcas «en curso» por actualizar cuentan como aprobadas mientras tanto (UC01 ext. 2c, UC02 ext. 2b, UC03 ext. 2c, UC04 ext. 2f) es S10, y que una incorporación se hace en una sola sesión (UC07, precondición 4) es S8. **S18 no viene del glosario:** es la decisión del equipo del 25 de octubre de 2026 que aplica el cambio n.º 8 del diseño (estados por certeza).
