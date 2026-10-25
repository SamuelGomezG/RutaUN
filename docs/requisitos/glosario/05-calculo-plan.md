---
title: "Glosario 5 · Cálculo y planes"
doc_type: "glosario"
srs_ids:
  - Alternativa de carga
  - Bloque de libre elección
  - Cálculo del plan
  - Comparación con la malla estándar
  - Culminación del plan de estudios
  - Espacio de optativa
  - Marca de autorización
  - Optativa fijada
  - Plan de carrera
  - Plan desactualizado
  - Plan vigente
  - Semestre actual del plan
  - Semestre del plan
  - Semestres restantes
  - Tope de carga
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
---
# Glosario 5 · Cálculo y planes

> **Cuándo cargar:** antes de tocar el motor (UC01/UC02/UC04), el tope, las marcas de autorización y las comparaciones.

> Términos: **15** de 71 del glosario; texto verbatim (solo se renivela el encabezado `###` → `##`). Índice completo en [README.md](README.md).

## Alternativa de carga

Cada tope de carga que el estudiante propone comparar en UC04 (Comparar alternativas de carga). Se rotulan con letras (A, B, C, D) y se comparan entre dos y cuatro. Si el estudiante tiene plan vigente, la comparación incluye además una **referencia**: el tope del plan vigente calculado con el avance actual; la referencia no cuenta entre las alternativas. Todas, y la referencia, se calculan con las optativas fijadas del plan vigente, si las tiene. No debe confundirse con las optativas de una agrupación.

## Bloque de libre elección

Porción de los créditos de libre elección que el plan asigna a un semestre, desde el 1 (el semestre 0 muestra los créditos de libre elección en curso). Un semestre tiene como máximo un bloque; cada bloque tiene al menos 3 créditos (salvo que falten menos de 3 créditos de libre elección en total) y los créditos de libre elección se reparten en tantos bloques como resulten necesarios. Para el límite de materias del tope, un bloque de N créditos cuenta como ⌈N/3⌉ materias *(el PDF lo escribe «[N/3]»; es el redondeo hacia arriba)* (supuesto del equipo: los cursos de libre elección valen en su mayoría 3 o 4 créditos y se cuenta el caso más desfavorable, el de 3 créditos).

## Cálculo del plan

Procedimiento con el que RutaUN arma un plan a partir del avance, el tope, las optativas fijadas y las reglas del plan vigentes en la versión del consolidado que el caso de uso que calcula (UC01, UC02 o UC04) cargó en su paso 2, y que supone vigentes en todos los semestres (ver «Vigencia»). El **semestre 0** es el periodo en curso cargado en ese mismo paso y solo muestra lo que figura en curso; el cálculo ubica lo que falta desde el **semestre 1**, el periodo siguiente. Considera los planes que cumplen el tope, las reglas del plan (que desde el supuesto S18 combinan prerrequisitos, correquisitos y habilitaciones por certeza: una restricción incierta cuenta como cumplida, con aviso, y una materia obligatoria cuyo reemplazo es incierto se sigue incluyendo, con el aviso de que podría estar cubierta o no) (salvo el mínimo por periodo, que se trata en el criterio 2), que incluyen las optativas fijadas y en los que ninguna agrupación recibe más optativas de las necesarias: si se quita cualquiera de las elegidas que no esté fijada, la agrupación deja de cubrirse o alguna materia del plan deja de cumplir un prerrequisito o una regla de habilitación; los créditos de una optativa elegida para cumplir un prerrequisito cuentan en su agrupación y no reducen lo exigido a otra (supuesto del equipo (1)). Entre ellos entrega el que, en este orden:
1. tiene menos semestres;
2. tiene menos semestres con marca de autorización;
3. suma menos créditos en las optativas elegidas que no están fijadas;
4. tiene, con esas optativas ordenadas según el orden del consolidado, la lista lexicográficamente menor;
5. tiene el vector de semestres (el semestre de cada materia, en el orden del consolidado) lexicográficamente menor;
6. tiene el vector de créditos de libre elección por semestre (semestre 1, 2, …) lexicográficamente **mayor**.

*Lexicográficamente* significa que se comparan los elementos uno a uno, en orden, hasta la primera posición en que las listas difieren; decide el elemento de esa posición (el menor en los criterios 4 y 5, el mayor en el 6). El plan muestra cada optativa elegida que no está fijada como un **espacio de optativa**, sin nombrarla como recomendada (ver «Espacio de optativa»). Con los mismos datos entrega siempre el mismo plan. **Cada cálculo (en UC04, el de cada alternativa y el de su referencia) se cancela si tarda más de treinta segundos (supuesto del equipo).**

## Comparación con la malla estándar

Lo que RutaUN muestra al comparar un plan (generado, guardado o no, o una alternativa de carga) con la malla estándar del programa, en UC01 (Generar plan de carrera), UC02 (Replanificar plan de carrera) y UC04 (Comparar alternativas de carga): (1) la malla estándar junto al plan, para compararlos a simple vista; (2) los semestres restantes del plan frente a los que le quedarían al estudiante según la malla estándar (los semestres de la malla menos sus semestres cursados); y (3) por cada materia obligatoria, si el plan la ubica antes o después de su semestre sugerido, sabiendo que el semestre k del plan corresponde al semestre (semestres cursados + k) del estudiante. La (1) requiere que el programa tenga la malla estándar registrada; la (2) y la (3), además, que esté completa y que el estudiante haya registrado sus semestres cursados. Es informativa: no cambia el plan.

## Culminación del plan de estudios

Momento en que el estudiante habrá aprobado todo lo que su plan de estudios exige en materias y créditos: el último semestre de su plan de carrera, si lo cumple. El plan dice cuántos semestres le faltan para culminarlo (semestres restantes) y en qué periodo lo culminaría (periodo estimado de culminación del plan de estudios). **No es el grado:** en la UNAL el grado suele ser en un periodo posterior al de la culminación del plan de estudios y depende de los requisitos de grado y de los trámites de cada estudiante, que RutaUN no verifica ni estima.

## Espacio de optativa

Lugar que el plan reserva en un semestre para una optativa de una agrupación, en lugar de nombrar una sola, porque elegirla le corresponde al estudiante. Se rotula con su agrupación (y la que la contiene, si es una subagrupación) y los créditos que exige. El plan muestra como **opciones** del espacio las optativas de la agrupación que el estudiante podría cursar en ese semestre sin cambiar el resto del plan: pendientes, no ubicadas en otro semestre del plan, con sus prerrequisitos y reglas de habilitación cumplidos en ese semestre, y tales que, con cualquiera de ellas, la agrupación sigue cubierta y todo prerrequisito del plan sigue cumplido. **No presenta ninguna como recomendada.** Para el tope, el total de créditos del semestre y el mínimo por periodo, el espacio cuenta los créditos de la opción más pequeña que mantiene el plan (criterio 3 del «Cálculo del plan») y marca las opciones con las que el semestre pasaría del tope o de la carga máxima por periodo, o quedaría por debajo del mínimo. Aparte, muestra las demás optativas de la agrupación con lo que les falta para cursarse en ese semestre (por ejemplo, un prerrequisito adicional) y, si el plan lo cumpliría más adelante, desde qué semestre. El estudiante puede consultar las opciones de cualquier espacio y fijar la que prefiera (ver «Optativa fijada»); la elección final la hace al inscribir, con información que consigue por fuera de RutaUN, porque la descripción de las materias está fuera del alcance. Una materia que figura en varias agrupaciones es opción de los espacios de todas ellas y, en el plan, cuenta para la agrupación del espacio que ocupa. Las materias no cursables desde el programa no son opciones de ningún espacio; entre las demás optativas que se muestran aparte, figuran con la señal «no se puede cursar desde este programa» (ver «Materia no cursable desde el programa»). Una optativa aprobada o en curso **ocupa** un espacio si es una de sus opciones (si figura en varias agrupaciones, solo un espacio de su agrupación asignada), sin importar en qué periodo la cursó; cada optativa ocupa a lo sumo un espacio: el del semestre más temprano en que figura como opción.

## Marca de autorización

Aviso que el plan muestra en un semestre, desde el 1, con menos créditos que el mínimo por periodo: «requiere autorización del Consejo de Facultad» (rótulo de RutaUN; base: Acuerdo 008 de 2008, art. 10, parágrafo). RutaUN solo lo muestra: no tramita ni verifica la autorización. Si una norma temporal que cubre el periodo de ese semestre prevé una exención (por ejemplo, la Resolución 057 de 2026, solo para 2026-2, para quien inscribe únicamente los créditos que le faltan para culminar su plan de estudios), el plan la lista como regla informativa junto a la marca, sin quitarla, porque RutaUN no puede verificar que el estudiante cumpla la condición de la exención.

## Optativa fijada

Optativa que el estudiante elige, desde un espacio de optativa o entre las que el plan mostraba aparte, para que el plan la incluya (UC01, extensión 7b, y UC02, extensión 8b). El cálculo la trata como obligatoria para ese plan: la ubica, con lo que exija, y cubre con ella su agrupación. Si era una opción del espacio que no excedía el tope, el plan conserva su número de semestres; si no, puede sumar semestres, y el sistema lo muestra. Se guarda con el plan, y «Replanificar plan de carrera» (UC02) la propone de nuevo, igual que el tope; el estudiante puede quitarla. Deja de aplicarse cuando figura aprobada o en curso (ya cuenta en el avance), cuando deja de regir o cuando su agrupación ya quedó cubierta con otras optativas. Si las optativas fijadas de una agrupación exceden lo que exige, el sistema advierte el excedente (supuesto del equipo (1)) y las incluye igual.

## Plan de carrera

Secuencia de semestres, desde el semestre 0, que cubre todo lo que le falta al estudiante para culminar su plan de estudios (ver «Culminación del plan de estudios»). Por semestre muestra su número y su periodo (por ejemplo, «Semestre 1 (2027-1)»): en el semestre 0, lo que el estudiante tiene en curso; desde el semestre 1, las materias obligatorias, las optativas fijadas y los espacios de optativa con sus opciones (ver «Espacio de optativa»), los bloques de libre elección, el total de créditos y de materias, las marcas de autorización y las reglas temporales, programadas e informativas que cubren su periodo o que empiezan o dejan de regir en él, cada una con su fuente. Para todo el plan muestra las reglas informativas permanentes (requisitos de grado, cupo de créditos), las reglas temporales o programadas cuya fecha no es un periodo (con esa fecha), los semestres restantes y el periodo estimado de culminación del plan de estudios, y permite compararlo con la malla estándar (ver «Comparación con la malla estándar»). Cómo se dibuja (por ejemplo, una tabla por semestre) se define en el diseño de la interfaz. Es una guía calculada con el avance, el tope, las optativas fijadas y las reglas del plan vigentes al calcularlo, que supone vigentes en todos sus semestres (ver «Vigencia»); **no es una inscripción**. Siempre tiene al menos un semestre después del 0: si no queda nada por cubrir, RutaUN no calcula un plan (ver «Plan desactualizado»).

## Plan desactualizado

Plan vigente que ya no refleja la situación del estudiante o las reglas que aplica: (a) hay atrasos, adelantos o cambios de alternativa respecto a él (ver «Atraso, adelanto y cambio de alternativa»); o (b) en la versión vigente del consolidado cambió, respecto a la versión con que se calculó, algo que el plan aplica: las materias del programa, sus créditos, su componente o su agrupación, si son obligatorias u optativas, sus prerrequisitos, las reglas de habilitación, las equivalencias, los créditos exigidos por el programa, por un componente o por una agrupación, las optativas de una agrupación, los créditos de libre elección, o el mínimo o el máximo por periodo. **No queda desactualizado por el solo paso del tiempo** (el avance del periodo en curso solo cambia su semestre actual), por cambios del avance que siguen el plan (por ejemplo, marcar en curso lo que el plan ubicaba en el semestre actual) ni por normas que solo cambian reglas informativas. Es un estado que **se calcula cada vez que se muestra el plan; no se guarda**. RutaUN lo señala, pero **nunca lo recalcula por su cuenta: solo el estudiante lo replanifica con UC02** (Replanificar plan de carrera). Si no queda nada por cubrir (todo lo que exige el programa figura aprobado o en curso, contando como aprobadas las marcas «en curso» por actualizar), el plan vigente no se señala como desactualizado: se muestra como «plan cumplido», no se ofrece replanificarlo (UC02, extensión 2g) y, si hay marcas por actualizar, se recuerda actualizarlas.

## Plan vigente

Único plan de carrera guardado de un estudiante (también «plan guardado»), con su tope y sus optativas fijadas. Guardar un plan nuevo con UC01 (Generar plan de carrera), UC02 (Replanificar plan de carrera) o UC04 (Comparar alternativas de carga) lo reemplaza, siempre con confirmación previa del estudiante. **No hay borradores.** Sus semestres no se renumeran con el paso del tiempo. Al abrirlo, RutaUN lo muestra con el aviso de lo que no verifica; indica el semestre actual del plan y, en ese semestre y en los anteriores, el estado que hoy tiene en el avance cada materia, espacio y bloque (ver «Semestre actual del plan»); toma las reglas informativas, temporales y programadas de la versión vigente del consolidado (las reglas del plan son las de la versión con que se calculó); y ofrece replanificarlo con UC02, salvo que sea un «plan cumplido» (ver «Plan desactualizado»). Si está desactualizado, muestra además una señal con el motivo: los atrasos, adelantos o cambios de alternativa, o la regla que cambió, con la norma que la cambió. Si el administrador avanzó el periodo en curso después del último guardado del avance, le recuerda al estudiante actualizarlo (resolver las marcas «en curso» por actualizar y marcar en curso lo que inscribió en el periodo nuevo), porque sin eso RutaUN no puede saber si el plan se está cumpliendo.

## Semestre actual del plan

Semestre del plan vigente cuyo periodo es el periodo en curso. Al mostrar el plan vigente, RutaUN indica cuál es (por ejemplo, «Estás en el semestre 2 (2027-2) de tu plan») y muestra, en ese semestre y en los anteriores, el estado que hoy tiene en el avance cada materia, espacio de optativa y bloque, para que el estudiante vea qué ya cursó y qué le falta. En un plan recién calculado es el semestre 0. Si el periodo en curso ya pasó el último semestre del plan, lo dice.

## Semestre del plan

Cada periodo académico que cubre el plan. El **semestre 0** es el periodo en curso al calcularlo: muestra lo que el estudiante tiene registrado «en curso» (materias y créditos de libre elección), si registró algo; el cálculo no ubica nada en él, no le aplica el tope ni le pone marcas de autorización, y no cuenta en los semestres restantes. El **semestre 1** es el periodo siguiente, y así. Se rotula con su número y su periodo (por ejemplo, «Semestre 1 (2027-1)»). Los semestres de un plan no se renumeran con el paso del tiempo: mientras el plan no se reemplace, su semestre 1 sigue siendo 2027-1 (ver «Semestre actual del plan»). El plan no supone fechas: si un periodo se corre (por ejemplo, por un paro), el plan sigue siendo válido.

## Semestres restantes

Número de semestres de un plan cuyo periodo aún no empieza (posteriores al periodo en curso). En un plan recién calculado son todos menos el semestre 0. Es lo que le falta al estudiante para culminar su plan de estudios si cumple el plan (ver «Culminación del plan de estudios»).

## Tope de carga

Par (límite de créditos, límite de materias) que el estudiante fija una vez y que se aplica igual a todos los semestres del plan desde el 1 (el semestre 0 muestra lo que ya cursa). Cada límite es un entero mayor o igual a 1 o «sin límite», y al menos uno de los dos es un entero. Ambos límites se cumplen a la vez: el plan nunca los excede; un bloque de libre elección de N créditos cuenta como ⌈N/3⌉ materias (supuesto del equipo) y un espacio de optativa, como una materia con los créditos que cuenta (ver «Espacio de optativa»).

