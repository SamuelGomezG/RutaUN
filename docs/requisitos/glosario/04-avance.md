---
title: "Glosario 4 · Avance y marcas"
doc_type: "glosario"
srs_ids:
  - Aprobada
  - Atraso, adelanto y cambio de alternativa
  - Avance
  - En curso
  - Marca del avance
  - Materia que ya no rige
  - Pendiente
  - Semestres cursados
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
---
# Glosario 4 · Avance y marcas

> **Cuándo cargar:** antes de implementar el guardado del avance, sus marcas y sus conflictos con la versión del consolidado.

> Términos: **8** de 71 del glosario; texto verbatim (solo se renivela el encabezado `###` → `##`). Índice completo en [README.md](README.md).

## Aprobada

Estado de una materia que el estudiante cursó y aprobó, o que la Universidad le reconoció como aprobada por validación, homologación o convalidación (Acuerdo 008 de 2008, arts. 29 y 38). RutaUN no verifica esos reconocimientos ni sus límites: registra lo que el estudiante declara.

## Atraso, adelanto y cambio de alternativa

Desfases del avance del estudiante respecto a una referencia. RutaUN los muestra para que el estudiante sepa dónde está; **ninguno cambia el cálculo de un plan**, que siempre parte del avance actual. Hay dos referencias:

**Respecto a la malla estándar** (solo si el programa tiene la malla estándar registrada y el estudiante registró sus semestres cursados; UC02, Replanificar plan de carrera, y UC03, Registrar avance en la malla).
- *Atraso:* materia obligatoria pendiente cuyo semestre sugerido es menor o igual que los semestres cursados, o agrupación o libre elección con menos créditos aprobados o en curso que los que la malla estándar sugiere en ella hasta ese semestre (una optativa que la malla muestra cuenta como créditos sugeridos de su agrupación; ver «Malla estándar»).
- *Adelanto:* materia obligatoria aprobada o en curso cuyo semestre sugerido es mayor que los semestres cursados, o agrupación o libre elección con más créditos aprobados o en curso que los sugeridos hasta ese semestre.

**Respecto al plan vigente** (UC02, Replanificar plan de carrera, y UC03, Registrar avance en la malla). Solo cuentan como atraso los semestres del plan cuyo periodo ya terminó (anteriores al periodo en curso): lo del semestre actual del plan que aún figura pendiente no es atraso mientras su periodo no termine; el plan lo muestra «sin registrar en curso» y recuerda actualizar el avance.
- *Atraso:* (a) materia obligatoria u optativa fijada que el plan ubicaba en un semestre cuyo periodo ya terminó y que hoy figura pendiente; (b) espacio de optativa de uno de esos semestres que ninguna optativa ocupa (ver «Espacio de optativa»); (c) libre elección con menos créditos aprobados o en curso que los que el plan ubicaba en bloques hasta esos semestres; o (d) materia que figuraba aprobada o en curso en el avance con que se calculó el plan (por ejemplo, en su semestre 0) y hoy figura pendiente, porque el estudiante la perdió o la canceló.
- *Adelanto:* materia que el plan ubicaba en un semestre cuyo periodo aún no empieza, u optativa que ocupa un espacio de uno de esos semestres, y que hoy figura aprobada o en curso; o libre elección con más créditos aprobados o en curso que los que el plan ubicaba hasta el semestre actual.
- *Cambio de alternativa:* optativa aprobada o en curso que no ocupa ningún espacio del plan ni estaba fijada en él (por ejemplo, una de las que el plan mostraba aparte porque exigía algo más).

## Avance

Estado de cada materia del programa (aprobada, en curso o pendiente) más los créditos de libre elección aprobados o en curso, tal como el estudiante los registra a mano; cada registro es una «marca del avance» y puede tocar materias de versiones anteriores del plan de estudios o materias reconocidas (S7); cada marca guarda de qué versión viene. Junto con el avance, el estudiante puede registrar sus semestres cursados (ver «Semestres cursados»). Cada guardado del avance registra el periodo en curso en que se hizo. RutaUN no lo lee del Sistema de Información Académica (SIA) de la Universidad. Cuando cambia el consolidado, el cálculo cuenta cada marca con los créditos, el componente y la agrupación que la materia tiene en la versión del consolidado que usa (si figura en varias agrupaciones, su agrupación asignada; ver «Agrupación asignada»); si la materia ya no rige, como dice «Materia que ya no rige» (supuesto del equipo). Es un dato personal del estudiante, protegido por la Ley 1581 de 2012.

## En curso

Estado de una materia, o de una cantidad de créditos de libre elección, que el estudiante está cursando en el periodo en curso o que ya cursó y de la que aún no tiene nota; para calcular el plan se trata como aprobada, y el plan la muestra en su semestre 0 (ver «Semestre del plan»). La marca guarda el periodo en curso en que el estudiante la registró o la modificó (no se vuelve a sellar al guardar otros cambios). Cuando el administrador avanza el periodo en curso, RutaUN señala las marcas «en curso» de periodos anteriores como «por actualizar» y le recuerda al estudiante resolverlas en UC03 (Registrar avance en la malla): una materia, pasándola a aprobada o a pendiente; los créditos de libre elección, pasándolos a aprobados o quitándolos. Mientras tanto, el cálculo las sigue contando como aprobadas (supuesto del equipo). Lo que el estudiante cursará en el periodo siguiente no se marca «en curso» antes de que el administrador avance el periodo en curso: aparece en su plan. Los créditos de libre elección en curso son una cantidad de un solo periodo: para registrar los del periodo en curso, el estudiante debe resolver antes los que están por actualizar (UC03, extensión 2c).

## Marca del avance

Registro que el estudiante hace en su avance: el estado de una materia (aprobada, en curso o pendiente) o una cantidad de créditos de libre elección aprobados o en curso. No debe confundirse con la «marca de autorización» del plan.

## Materia que ya no rige

Materia marcada como aprobada o en curso en el avance que no rige en la versión vigente del consolidado (fue derogada o su vigencia terminó). Lo que la Universidad ya le reconoció al estudiante no se pierde: si una equivalencia vigente la hace equivaler a otra materia, cuenta como esa materia (ver «Equivalencia»); si no, sigue contando con los créditos, el componente y la agrupación que tenía en la última versión en que rigió (supuesto del equipo). Si la materia cumplía prerrequisitos o habilitaciones de otras materias en la versión en que rigió y ninguna equivalencia dice cómo traducir la marca, esas restricciones quedan como una **restricción incierta** (S18): el cálculo las trata como cumplidas, con aviso. El estudiante no tiene que hacer nada: RutaUN la muestra aparte en su avance (UC03, extensión 2e). Si esa agrupación o ese componente tampoco rigen y ninguna equivalencia la ubica, el sistema la muestra sin contarla y registra un incidente para que el administrador cargue la equivalencia o la regla de transición que corresponda; mientras tanto, los casos de uso que calculan un plan (UC01, UC02 y UC04) lo advierten. Una materia pendiente que deja de regir simplemente deja de mostrarse en la malla.

## Pendiente

Estado de una materia que el estudiante no ha aprobado ni está cursando. Es el estado inicial de toda materia. El plan ubica todas las obligatorias pendientes y, para cada agrupación, las optativas fijadas y los espacios de optativa que hagan falta para cubrirla.

## Semestres cursados

Número de periodos académicos en que el estudiante ha estado matriculado en su programa, incluido el periodo en curso si lo está cursando (un periodo con reserva de cupo no cuenta). Lo declara el propio estudiante en UC03 (Registrar avance en la malla); RutaUN no lo lee del SIA ni lo verifica. Es opcional y solo se usa para comparar el avance y los planes con la malla estándar (ver «Atraso, adelanto y cambio de alternativa» y «Comparación con la malla estándar»). RutaUN guarda el periodo en curso en que el estudiante lo registró; si después el administrador avanza el periodo en curso, al usarlo le advierte que puede haber cambiado y le ofrece actualizarlo (UC02, extensión 2i, y UC03, extensión 2d).

