---
title: "UC01 · Generar plan de carrera"
doc_type: "caso-de-uso"
srs_ids:
  - UC01
  - UC01-HU01..HU07
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
  - ../glosario/01-producto-actores-tiempo.md
  - ../glosario/02-normativa-consolidado.md
  - ../glosario/03-malla-materias.md
  - ../glosario/04-avance.md
  - ../glosario/05-calculo-plan.md
  - ./README.md
---
# UC01 · Generar plan de carrera

> **Cuándo cargar:** para implementar o revisar **UC01**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Estudiante · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Estudiante** — *Quiere:* un plan semestre a semestre que cumpla las reglas de su programa, los prerrequisitos y su tope de carga, que con ese tope termine en el menor número de semestres posible y con la menor cantidad de semestres que requieran autorización del Consejo de Facultad, que le diga cuántos semestres le faltan para culminar su plan de estudios y que pueda compararlo con la malla estándar de su programa. *Por qué:* el entrevistado lleva su avance rayando el PDF de la malla ("lo rayo para saber qué me queda, qué me falta"); alguna vez, en su doble titulación, cuadró las dos mallas en Paint ("tomaba pedazos de cada una de las mallas y las iba cuadrando para contar los créditos"), y con las dos mallas a la mano planea al menos dos semestres a la vez ("normalmente meto las materias pensando por lo menos en este semestre y en el que viene después"). Un plan que incumpla una regla o un prerrequisito puede costarle un semestre, y la Entrega 0 planteó como hipótesis que el estudiante decide su carga según «Capacidad personal, otras obligaciones, qué tan rápido quiere graduarse».
- **I2 · Estudiante** — *Quiere:* ver en el plan todo lo que le falta, sin importar el tipo de materia: las obligatorias; las optativas, como espacios de optativa con las opciones que puede cursar en cada semestre, para consultarlas y fijar la que prefiera; la libre elección, como bloques de créditos repartidos entre semestres; y, en el semestre 0, lo que ya está cursando. *Por qué:* necesita saber qué le falta de verdad para decidir qué cursar. El entrevistado cuenta que en Diseño Industrial, su segundo programa, donde solo existe la lista de materias, "yo desconozco qué materias son optativas o cuáles son obligatorias"; y la Entrega 0 ya describía que, si no sigue la malla al pie de la letra, el estudiante «tiene que construir manualmente, semestre a semestre, una secuencia alternativa que respete los prerrequisitos». La experiencia directa del equipo como estudiantes de la UNAL apunta a que el estudiante necesita ver todo lo que le falta, optativas incluidas, para decidir qué cursar; y elegir una optativa depende de información que RutaUN no tiene, como de qué trata cada materia, por lo que el plan no la elige por él.
- **I3 · Estudiante** — *Quiere:* que su plan vigente solo cambie cuando él lo confirma y que nunca quede perdido ni a medias si algo falla. *Por qué:* el plan es el resultado de su planeación; si un fallo o un clic accidental lo pierde, tiene que rehacerla, y al entrevistado hoy incluso el horario de un solo semestre le cuesta: usa un Excel en el que registra "todas las materias que voy a ver y las opciones de grupos que hay" (este semestre, BetterCampus) y "organizar el horario siempre me toma como unas tres, cuatro horas".
- **I4 · Estudiante** — *Quiere:* saber con claridad qué no garantiza el plan: oferta, cupos, sobrecupos, horarios, cupo de créditos, reconocimientos, autorizaciones del Consejo de Facultad y cumplimiento de los requisitos de grado. *Por qué:* un plan puede cumplir prerrequisitos y carga y aun así fallar por algo que la malla no captura: al entrevistado, que cursa doble titulación, "no me aprobaron el sobrecupo, y eso me cambió los planes". Si lo tomara por una inscripción, decidiría con información falsa.
- **I5 · Estudiante** — *Quiere:* que su avance y sus planes sean privados: solo él los ve y los modifica. *Por qué:* son datos personales académicos protegidos por la Ley 1581 de 2012.
- **I6 · Administrador de normativa** — *Quiere:* que cada plan quede asociado a la versión del consolidado con que se calculó. *Por qué:* para que el plan se pueda explicar con la versión registrada cuando el propio estudiante lo consulta, y para saber cuántos planes (sin identificar a sus dueños) quedan desactualizados por una norma nueva.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando el consolidado impide calcular un plan (por ejemplo, un prerrequisito circular) o falta registrar el periodo en curso, cuando una regla que el plan aplica o muestra no tiene fuente registrada o está en revisión, cuando una materia que ya no rige no se puede contar, o cuando el cálculo falla. *Por qué:* solo él puede corregir el consolidado y, como responsable de operar RutaUN, atiende los errores internos; si no se entera, cada estudiante que lo intente recibirá el mismo error.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que sus normas se apliquen como fueron expedidas (el plan usa las que rigen al calcularlo y avisa en qué semestre empiezan o dejan de regir las temporales y las programadas) y con su fuente citada, y que RutaUN no se presente como fuente oficial ni resuelva por ellos lo que les corresponde (por ejemplo, autorizar una carga inferior al mínimo). *Por qué:* la validez de una norma y la decisión sobre los casos particulares son de ellos (en especial, de los Consejos de Facultad, que autorizan cargas inferiores al mínimo según el parágrafo del art. 10 del Acuerdo 008 de 2008, y de quien verifica la carga mínima, como DINARA en 2026-2); una versión distorsionada haría que los estudiantes decidieran con base en RutaUN y no en la norma.

## Precondiciones
1. El estudiante ha iniciado sesión con su cuenta de RutaUN.
2. El estudiante tiene asociado un programa cargado en el sistema (un solo programa: la doble titulación está fuera de alcance) y ese programa tiene una versión vigente del consolidado.

## Garantía mínima (GM) — se cumple sin importar el resultado
1. El plan vigente del estudiante solo cambia si él confirma y el guardado se completa (paso 10); en cualquier otra salida queda exactamente como estaba (o sin plan, si no tenía). Calcular o ver un plan no modifica el plan vigente ni el avance. [I3]
2. Ningún plan queda guardado a medias: se guarda completo o no se guarda. [I3]
3. Este caso de uso no modifica el avance del estudiante. [I5]
4. Nadie más que el estudiante accede a su avance ni a sus planes. [I5]
5. Si el consolidado o un fallo impiden atender al estudiante, queda un incidente para el administrador y el estudiante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]
6. Siempre que se muestra un plan, se muestra con el aviso de lo que RutaUN no verifica: nunca se presenta como una inscripción ni como una autorización. [I4, I8]

## Garantía de éxito (GE) — cuando la meta se cumple
1. El plan vigente del estudiante es el plan que él revisó y confirmó, y reemplazó al anterior, si existía. [I3]
2. El plan incluye todo lo que falta, sin importar el tipo de materia: las obligatorias; para cada agrupación, las optativas fijadas y los espacios de optativa que cubren los créditos que exige, cada espacio con sus opciones; y toda la libre elección, en bloques. Lo que el estudiante tiene en curso aparece en el semestre 0. [I2]
3. Cada semestre, desde el 1, cumple los prerrequisitos, las reglas de habilitación y el tope, y no supera la carga máxima por periodo que fije una regla del plan; el conjunto cumple los créditos exigidos por componente y por agrupación y las equivalencias; todo según las reglas del plan vigentes en la versión del consolidado cargada en el paso 2, que el plan supone vigentes en todos sus semestres (ver «Vigencia»). [I1, I8]
4. El plan tiene el menor número de semestres posible con ese tope y, entre los planes que lo logran, la menor cantidad de semestres con marca de autorización (criterios 1 y 2 del «Cálculo del plan»); con los mismos datos, el cálculo entrega siempre el mismo plan. [I1, I6]
5. Cada semestre que no llega al mínimo por periodo lleva su marca de autorización; si una norma temporal que cubre ese periodo prevé una exención, el plan la lista como regla informativa junto a la marca, sin quitarla. [I1, I4, I8]
6. El plan guardado registra la fecha, el periodo en curso, el tope, las optativas fijadas, el avance y la versión del consolidado que se usaron, y se calculó con las reglas del plan vigentes en esa versión (ver «Vigencia»). [I6, I8]
7. El estudiante sabe cuántos semestres le faltan para culminar su plan de estudios, en qué periodo lo culminaría (no cuándo se graduaría, que RutaUN no estima; ver «Culminación del plan de estudios») y lo que RutaUN no verifica. [I1, I4]

## Disparador
El estudiante decide armar su plan de carrera y selecciona «Generar plan de carrera».

## Escenario principal de éxito
1. El estudiante selecciona «Generar plan de carrera».
2. El sistema carga el avance del estudiante, la versión vigente del consolidado de su programa y el periodo en curso (que será el semestre 0 del plan; el semestre 1 será el periodo siguiente), verifica que el consolidado sea calculable (ver «Consolidado calculable») y le muestra un resumen del avance: créditos aprobados, en curso y pendientes por componente y por agrupación. [I1, I5, I6]
3. El estudiante indica su tope de carga, el mismo para todos los semestres: el límite de créditos, el límite de materias, o ambos. [I1]
4. El sistema valida el tope: formato, mínimo por periodo, créditos de la materia obligatoria pendiente más pesada y carga máxima por periodo que fije una regla del plan (extensión 4d). [I1, I4, I8]
5. El sistema calcula el plan con el avance, la versión del consolidado y el periodo en curso cargados en el paso 2, el tope, las optativas fijadas, si las hay (extensión 7b), y las reglas del plan vigentes en esa versión, que supone vigentes en todos los semestres (ver «Vigencia»), y registra esa versión. [I1, I2, I6, I8]
6. El sistema muestra el plan semestre a semestre, desde el semestre 0, con lo que el estudiante tiene en curso; cada semestre con su periodo (por ejemplo, «Semestre 1 (2027-1)»), con las obligatorias, las optativas fijadas, los espacios de optativa con sus opciones (ver «Espacio de optativa»), los bloques de libre elección, las marcas de autorización y, por semestre, las reglas temporales, programadas e informativas que cubren su periodo o que empiezan o dejan de regir en él, cada una con su fuente; muestra también, para todo el plan, las reglas informativas permanentes (requisitos de grado, cupo de créditos), los semestres restantes para culminar el plan de estudios, el periodo estimado de culminación del plan de estudios y el aviso de lo que RutaUN no verifica. [I1, I2, I4, I8]
7. El estudiante revisa el plan y decide guardarlo como su plan vigente. [I3]
8. El sistema solicita confirmación, advirtiendo que reemplazará el plan vigente cuando exista. [I3]
9. El estudiante confirma. [I3]
10. El sistema verifica que el avance y la versión vigente del consolidado sigan siendo los cargados en el paso 2, que el plan vigente no haya cambiado y que el periodo en curso sea el cargado en el paso 2, y guarda el plan de forma atómica como plan vigente. [I3, I5, I6]
11. El sistema confirma el guardado al estudiante. [I3]

## Extensiones
- **2a** · *Condición:* El estudiante no tiene avance registrado (todas las materias están pendientes y no hay créditos de libre elección registrados). → *Resolución:* El sistema lo informa y ofrece registrar el avance antes («Registrar avance en la malla», UC03). Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, el plan se calcula con todo pendiente, que es lo correcto para quien inicia el programa, y el caso sigue en el paso 3. [I1]
- **2b** · *Condición:* No queda nada por cubrir: todas las obligatorias, las agrupaciones y los créditos de libre elección figuran aprobados o en curso (las marcas «en curso» por actualizar cuentan como aprobadas; ver «En curso»). Solo se evalúa si el consolidado es calculable; si no lo es, se atiende la extensión 2e. → *Resolución:* El sistema informa que no hay nada por planear: con lo registrado, el estudiante ya habría culminado su plan de estudios. Si hay marcas «en curso» por actualizar, recuerda que eso depende de que las apruebe y ofrece actualizarlas en «Registrar avance en la malla» (UC03). Recuerda también que los requisitos de grado son informativos y RutaUN no los verifica. El caso termina sin plan nuevo; el plan vigente, si existe, queda como estaba y, como no queda nada por cubrir, se muestra como «plan cumplido» (ver «Plan desactualizado»). [I1, I3, I4, I8]
- **2c** · *Condición:* El avance tiene marcas «en curso» por actualizar (de materias o de créditos de libre elección), registradas en un periodo anterior al periodo en curso. → *Resolución:* El sistema las lista, recuerda que conviene actualizarlas cuando el estudiante tenga sus notas, explica que el plan las cuenta como aprobadas mientras tanto (supuesto del equipo) y ofrece actualizarlas antes de continuar («Registrar avance en la malla», UC03). Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, el caso sigue en el paso 3. [I1]
- **2d** · *Condición:* El avance marca como aprobada una materia cuyos prerrequisitos figuran pendientes. → *Resolución:* El sistema avisa la inconsistencia (puede deberse a validaciones, homologaciones o convalidaciones, que RutaUN no verifica) y continúa con el avance tal como está registrado; el caso sigue en el paso 3. [I1, I4]
- **2e** · *Condición:* El consolidado vigente no es calculable (ver «Consolidado calculable»): por ejemplo, una malla que no es utilizable, una agrupación cuyas optativas no alcanzan para lo que exige, un prerrequisito circular, reglas del plan en revisión o el periodo en curso sin registrar. → *Resolución:* El sistema no calcula e informa al estudiante qué lo impide: si es el consolidado, que el consolidado normativo de su programa tiene un problema; si es el periodo en curso, que el sistema todavía no lo tiene registrado; en ambos casos, que ya se informó al administrador de normativa. Registra un incidente con el programa, la versión y la causa. El caso termina sin cambios. [I1, I7]
- **2f** · *Condición:* El estudiante ya tiene un plan vigente. → *Resolución:* El sistema lo informa y sugiere «Replanificar plan de carrera» (UC02), que muestra las diferencias con el plan vigente y conserva sus optativas fijadas. Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, el caso sigue en el paso 3 y la confirmación se hace como en la extensión 8a. [I3]
- **2g** · *Condición:* El avance tiene materias que ya no rigen y que no se pueden contar, porque su agrupación o su componente tampoco rigen y ninguna equivalencia las ubica (ver «Materia que ya no rige»). → *Resolución:* El sistema las lista, explica que el plan no las cuenta, por lo que puede pedir créditos que la Universidad ya le reconoció, y que ya se informó al administrador de normativa para que cargue la regla que corresponda. El caso sigue en el paso 3. [I1, I2, I7]
- **4a** · *Condición:* El tope es inválido: ambos límites vacíos o ambos «sin límite», o un valor igual a cero, negativo, decimal o no numérico. → *Resolución:* El sistema señala el campo, explica el formato válido (un entero mayor o igual a 1 o «sin límite», con al menos un límite entero) y pide corregirlo; el caso vuelve al paso 3. [I1]
- **4b** · *Condición:* El tope no permite alcanzar el mínimo por periodo que rige: el límite de créditos es menor que ese mínimo (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10) o el límite de materias es tan bajo que, aun con las materias pendientes de más créditos (un bloque de libre elección cuenta como una materia de 3 créditos), ningún semestre llegaría a ese mínimo. → *Resolución:* El sistema advierte qué mínimo rige y qué norma lo fija (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10), y que con ese tope cada semestre afectado requeriría autorización del Consejo de Facultad, que RutaUN no tramita. El estudiante ajusta el tope (el caso vuelve al paso 3) o lo mantiene (el caso continúa en el paso 5 y el plan marca cada semestre afectado). [I1, I4, I8]
- **4c** · *Condición:* El límite de créditos es menor que los créditos de alguna materia obligatoria pendiente o de alguna optativa fijada, o alguna agrupación no puede cubrirse solo con optativas que quepan en el límite, o el límite es menor que 3 créditos y aún hay créditos de libre elección por cubrir (si faltan menos de 3, el límite es menor que los que faltan; ver «Bloque de libre elección»). → *Resolución:* El sistema lista las materias, las agrupaciones o la libre elección que no caben y el límite mínimo que las haría caber, y no calcula. El estudiante ajusta el límite (el caso vuelve al paso 3), quita la optativa fijada que no cabe (el caso vuelve al paso 4) o abandona el caso, que termina sin cambios. [I1]
- **4d** · *Condición:* El límite de créditos supera la carga máxima por periodo que fija una regla del plan. → *Resolución:* El sistema muestra el máximo y la norma que lo fija, y explica que en todos los semestres el plan usará el máximo en lugar del límite del estudiante. El estudiante continúa (el caso sigue en el paso 5) o ajusta el límite (el caso vuelve al paso 3). [I1, I8]
- **5a** · *Condición:* El cálculo no termina en treinta segundos (supuesto del equipo). → *Resolución:* El sistema lo cancela, no muestra ningún plan, informa al estudiante y le sugiere reintentar o probar otro tope (paso 3), y registra un incidente. Si el estudiante no reintenta, el caso termina sin cambios. [I1, I7]
- **5b** · *Condición:* El cálculo determina que no existe ningún plan que cumpla las reglas del plan, el tope y las optativas fijadas. Las validaciones del paso 4 y de UC07 (extensiones 6d y 6e) lo impiden, así que solo ocurre por un error del consolidado que esas validaciones no detectaron. → *Resolución:* El sistema no muestra un plan parcial: informa que no pudo calcular un plan, con la causa que detectó (la materia o la agrupación, y la norma), y que ya se informó al administrador de normativa, y registra un incidente. El caso termina sin cambios. [I1, I7]
- **6a** · *Condición:* Algún semestre, desde el 1, suma menos créditos que el mínimo por periodo, porque el tope o los prerrequisitos lo impiden o porque es el último y faltan pocos créditos. → *Resolución:* El plan lo muestra con la marca de autorización (rótulo «requiere autorización del Consejo de Facultad»; base: Acuerdo 008 de 2008, art. 10, parágrafo) sin tramitarla. Si una norma temporal que cubre el periodo de ese semestre prevé una exención (como la que la Resolución 057 de 2026 previó, solo para 2026-2, para quien inscribe únicamente los créditos que le faltan para culminar su plan de estudios), el plan la lista como regla informativa junto a la marca, sin quitarla, porque RutaUN no verifica que el estudiante cumpla la condición. [I1, I4, I8]
- **6b** · *Condición:* El plan cubre periodos con reglas temporales (por ejemplo, una carga máxima o una excepción) o con reglas programadas que aún no rigen. → *Resolución:* En cada semestre afectado el plan indica la regla, su fuente y si es temporal o programada, para que el estudiante esté pendiente de ese semestre; si la norma fija una fecha y no un periodo, el plan la muestra para todo el plan, con esa fecha. Ninguna modifica el cálculo: el plan aplica solo las reglas del plan vigentes en la versión cargada en el paso 2 (ver «Vigencia»); las programadas que cambian lo que el plan aplica se atienden además con la extensión 6d. [I1, I8]
- **6c** · *Condición:* Una regla que el plan aplica o muestra no tiene fuente registrada. → *Resolución:* El plan la muestra con la señal «sin fuente registrada», no la presenta como respaldada por una norma y el sistema registra un incidente. El caso continúa en el paso 7. [I7, I8]
- **6d** · *Condición:* Una regla programada empieza a regir en el periodo de uno de los semestres del plan (o en una fecha, si su fecha de aplicación no es un periodo), y con ello cambiará lo que el plan aplica (las materias del programa, sus créditos, su componente o su agrupación, si son obligatorias u optativas, sus prerrequisitos, las reglas de habilitación, las equivalencias, los créditos exigidos por el programa, por un componente o por una agrupación, las optativas de una agrupación, los créditos de libre elección, o el mínimo o el máximo por periodo). → *Resolución:* El plan se calculó con las reglas del plan vigentes y supone que siguen rigiendo en todos sus semestres (ver «Vigencia»). El sistema señala la regla, con su fuente, en el semestre en que empieza a regir (o para todo el plan, con su fecha), y advierte que, cuando empiece a regir, el sistema creará una versión nueva del consolidado, el plan quedará desactualizado y convendrá replanificarlo («Replanificar plan de carrera», UC02). El caso continúa en el paso 7. [I1, I8]
- **6e** · *Condición:* Una regla informativa que el plan muestra está «en revisión» (ver «Regla»). → *Resolución:* El plan la muestra con esa señal y con las dos fuentes que se contradicen, no afirma cuál rige y el sistema registra un incidente. El caso continúa en el paso 7. [I7, I8]
- **7a** · *Condición:* El estudiante quiere probar otro tope. → *Resolución:* El caso vuelve al paso 3 conservando el resumen del avance y las optativas fijadas, y el plan mostrado se descarta. Si prefiere ver varias opciones a la vez, este caso termina sin cambios y el estudiante puede usar «Comparar alternativas de carga» (UC04), que también permite guardar la que prefiera. [I1]
- **7b** · *Condición:* El estudiante quiere ver las opciones de un espacio de optativa, fijar una optativa o quitar una que fijó. → *Resolución:* El sistema muestra las opciones del espacio, con sus créditos y las marcas del tope o del mínimo (ver «Espacio de optativa»), y aparte las demás optativas de la agrupación, cada una con lo que le falta para cursarse en ese semestre y, si el plan lo cumpliría más adelante, desde qué semestre. Si el estudiante fija una optativa o quita una que fijó, el caso vuelve al paso 5 con el mismo tope: el sistema recalcula el plan con las optativas fijadas que queden y, al mostrarlo (paso 6), indica si cambió el número de semestres y por qué (ver «Optativa fijada»). Si solo consulta, el caso sigue en el paso 7. [I1, I2]
- **7c** · *Condición:* El estudiante decide no guardar el plan. → *Resolución:* No se guarda nada y el plan vigente, si existía, permanece igual. El caso termina sin cambios. [I3]
- **7d** · *Condición:* El estudiante quiere comparar el plan con la malla estándar de su programa. → *Resolución:* El sistema muestra la comparación (ver «Comparación con la malla estándar»): la malla estándar junto al plan y, si el estudiante registró sus semestres cursados, los semestres restantes del plan frente a los de la malla estándar y las obligatorias que el plan ubica antes o después de su semestre sugerido. Si el programa no tiene la malla estándar registrada, o le falta algún dato, muestra lo que pueda y dice qué falta; si faltan los semestres cursados, indica que puede registrarlos en «Registrar avance en la malla» (UC03). La comparación no cambia el plan; el caso sigue en el paso 7. [I1]
- **8a** · *Condición:* El estudiante ya tiene un plan vigente. → *Resolución:* La confirmación muestra el plan vigente (semestres restantes y periodo estimado de culminación del plan de estudios) junto al nuevo y deja claro que el nuevo lo reemplaza y que el anterior no se conserva. [I3]
- **9a** · *Condición:* El estudiante no confirma. → *Resolución:* No se guarda nada y el estudiante vuelve al plan calculado (paso 7). [I3]
- **10a** · *Condición:* Desde el paso 2 cambió el avance del estudiante (por ejemplo, desde otra sesión) o la versión vigente del consolidado (porque el administrador incorporó una norma o porque el sistema creó una versión al empezar o dejar de regir una regla). → *Resolución:* El sistema no guarda, informa cuál de los dos cambió y ofrece recalcular desde el paso 2 con el tope y las optativas fijadas que había indicado; el plan mostrado se descarta. Si el estudiante no acepta, el caso termina sin cambios. [I3, I6]
- **10b** · *Condición:* Falla el guardado (error de persistencia). → *Resolución:* Como el guardado es atómico, el plan vigente queda como estaba y no queda un plan a medias. El sistema informa el fallo, conserva el plan calculado en pantalla mientras dure la sesión para que el estudiante confirme de nuevo (paso 9), y registra un incidente. [I3, I7]
- **10c** · *Condición:* Desde el paso 2 cambió el plan vigente (se guardó otro desde otra sesión) o el administrador avanzó el periodo en curso, con lo que el semestre 0 del plan calculado ya no es el periodo en curso. → *Resolución:* El sistema no guarda, informa qué cambió y ofrece recalcular desde el paso 2 con el tope y las optativas fijadas que había indicado; el plan mostrado se descarta. Si el estudiante no acepta, el caso termina sin cambios. [I3]
- **11a** · *Condición:* La sesión expira, el estudiante abandona la aplicación u ocurre un error interno después de que el plan se guardó y antes de ver la confirmación. → *Resolución:* El plan ya quedó guardado como plan vigente; el estudiante lo encuentra actualizado al volver a entrar. No se requiere ninguna acción; si fue un error interno, el sistema registra un incidente con el paso y la causa. [I3, I7]
- **Cualquiera** · *Condición:* El estudiante abandona el caso o la sesión expira antes de que el guardado se complete. → *Resolución:* No se guarda nada: lo calculado o editado hasta ese momento se descarta y los datos guardados quedan como estaban. [I3, I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso anterior al guardado (el fallo del propio guardado lo trata la extensión 10b; si el error ocurre después, lo guardado ya quedó guardado, como en la extensión 11a). → *Resolución:* El sistema no modifica los datos guardados, informa que ocurrió un error y que se puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I3, I7]

---

---

## UC01 · Generar plan de carrera (7 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC01 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC01-HU01 (#1) · Indicar y validar el tope de carga
**Como** estudiante, **quiero** indicar mi tope de carga (límite de créditos, de materias o ambos) y que el sistema lo valide antes de calcular, **para** no calcular un plan con un tope imposible ni descubrir tarde que me deja semestres que requieren autorización.
Criterios de aceptación:
- [ ] Si el tope es inválido (ambos límites vacíos o «sin límite», o un valor cero, negativo, decimal o no numérico), el sistema señala el campo, explica el formato válido y no calcula (ext. 4a).
- [ ] Si el tope no permite alcanzar el mínimo por periodo, el sistema advierte qué mínimo rige y qué norma lo fija (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10) y deja ajustar el tope o mantenerlo; si lo mantiene, el plan marca cada semestre afectado (ext. 4b).
- [ ] Si alguna materia obligatoria pendiente, optativa fijada, agrupación o la libre elección no cabe en el límite de créditos, el sistema lista lo que no cabe y el límite mínimo que lo haría caber, y no calcula (ext. 4c).
- [ ] Si el límite supera la carga máxima por periodo que fija una regla del plan, el sistema muestra ese máximo y su norma y explica que el plan usará el máximo en todos los semestres (ext. 4d).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU02 (#2) · Ver el plan calculado semestre a semestre
**Como** estudiante, **quiero** ver un plan semestre a semestre, desde el semestre 0, que incluya todo lo que me falta, **para** saber qué me falta de verdad y cuántos semestres me quedan para la culminación de mi plan de estudios.
Criterios de aceptación:
- [ ] El plan incluye las obligatorias, los espacios de optativa con sus opciones, las optativas fijadas y toda la libre elección en bloques; lo que está en curso aparece en el semestre 0 (GE 2).
- [ ] Cada semestre, desde el 1, cumple los prerrequisitos, las reglas de habilitación y el tope, y el plan tiene el menor número de semestres posible con ese tope; con los mismos datos, el cálculo entrega siempre el mismo plan (GE 3, GE 4).
- [ ] Cada semestre con menos créditos que el mínimo por periodo lleva la marca «requiere autorización del Consejo de Facultad», sin tramitarla (GE 5, ext. 6a).
- [ ] El plan muestra los semestres restantes, el periodo estimado de culminación del plan de estudios (no de grado) y el aviso de lo que RutaUN no verifica (GE 7, GM 6).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU03 (#3) · Consultar y fijar optativas en el plan
**Como** estudiante, **quiero** consultar las opciones de un espacio de optativa y fijar la optativa que prefiera, o quitar una que fijé, **para** decidir qué optativa cursar sin que el sistema elija por mí.
Criterios de aceptación:
- [ ] Al abrir un espacio, el sistema muestra sus opciones con sus créditos y las marcas del tope o del mínimo, sin presentar ninguna como recomendada (ext. 7b).
- [ ] Aparte, muestra las demás optativas de la agrupación con lo que les falta para cursarse en ese semestre y, si el plan lo cumpliría más adelante, desde qué semestre (ext. 7b).
- [ ] Al fijar o quitar una optativa, el sistema recalcula con el mismo tope e indica si cambió el número de semestres y por qué; si solo consulto, el plan no cambia (ext. 7b).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU04 (#4) · Guardar el plan como plan vigente
**Como** estudiante, **quiero** guardar el plan calculado como mi plan vigente después de confirmarlo, **para** conservar mi planeación sin arriesgarme a perderla o a dejarla a medias.
Criterios de aceptación:
- [ ] Antes de guardar, el sistema pide confirmación; si ya tengo plan vigente, lo muestra junto al nuevo y aclara que el anterior no se conserva (pasos 8–9, ext. 8a).
- [ ] El plan se guarda de forma atómica y solo si confirmo; en cualquier otra salida el plan vigente queda exactamente como estaba (GM 1, GM 2).
- [ ] Si desde que se cargaron los datos cambió mi avance, la versión del consolidado, mi plan vigente o el periodo en curso, el sistema no guarda, dice qué cambió y ofrece recalcular (ext. 10a, 10c).
- [ ] El plan guardado registra la fecha, el periodo en curso, el tope, las optativas fijadas, el avance y la versión del consolidado usados (GE 6).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU05 (#5) · Comparar el plan con la malla estándar
**Como** estudiante, **quiero** comparar el plan calculado con la malla estándar de mi programa, **para** ver si voy por delante o por detrás de lo que sugiere la Universidad.
Criterios de aceptación:
- [ ] El sistema muestra la malla estándar junto al plan (ext. 7d).
- [ ] Si registré mis semestres cursados, muestra los semestres restantes del plan frente a los de la malla estándar y las obligatorias que el plan ubica antes o después de su semestre sugerido (ext. 7d).
- [ ] Si falta la malla estándar, algún dato de ella o mis semestres cursados, muestra lo que pueda y dice qué falta; la comparación nunca cambia el plan (ext. 7d).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU06 (#6) · Recibir avisos sobre el avance y el consolidado antes de calcular
**Como** estudiante, **quiero** que el sistema me avise antes de calcular si mi avance o el consolidado de mi programa afectan el plan, **para** no planear sobre datos incompletos sin saberlo.
Criterios de aceptación:
- [ ] Si no tengo avance registrado o tengo marcas «en curso» por actualizar, el sistema lo dice y me ofrece ir a «Registrar avance en la malla» (UC03) o continuar; si continúo, las marcas por actualizar cuentan como aprobadas (ext. 2a, 2c).
- [ ] Si ya no queda nada por cubrir, el sistema informa que, con lo registrado, ya habría culminado mi plan de estudios y no calcula un plan nuevo (ext. 2b).
- [ ] Si el consolidado no es calculable o falta el periodo en curso, el sistema no calcula, me dice qué lo impide y que ya se informó al administrador, y registra un incidente (ext. 2e, GM 5).
- [ ] Si ya tengo un plan vigente, el sistema me sugiere «Replanificar plan de carrera» (UC02) y me deja continuar aquí (ext. 2f).

Caso de uso relacionado: UC01 · Generar plan de carrera

### UC01-HU07 (#7) · Ver en el plan las normas temporales y programadas
**Como** estudiante, **quiero** ver en cada semestre del plan las reglas temporales, programadas e informativas que lo afectan, con su fuente, **para** estar pendiente de los semestres en que cambia la normativa.
Criterios de aceptación:
- [ ] En cada semestre afectado, el plan indica la regla, su fuente y si es temporal o programada, sin que eso cambie el cálculo (ext. 6b).
- [ ] Si una regla programada cambiará lo que el plan aplica, el plan la señala en el semestre en que empieza a regir y advierte que el plan quedará desactualizado y convendrá replanificarlo (ext. 6d).
- [ ] Una regla sin fuente se muestra con la señal «sin fuente registrada» y una regla «en revisión» con sus dos fuentes, sin afirmar cuál rige; en ambos casos se registra un incidente (ext. 6c, 6e).

Caso de uso relacionado: UC01 · Generar plan de carrera

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #1–#7 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
