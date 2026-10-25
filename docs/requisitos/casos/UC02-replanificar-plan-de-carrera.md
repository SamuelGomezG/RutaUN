---
title: "UC02 · Replanificar plan de carrera"
doc_type: "caso-de-uso"
srs_ids:
  - UC02
  - UC02-HU01..HU05
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
# UC02 · Replanificar plan de carrera

> **Cuándo cargar:** para implementar o revisar **UC02**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Estudiante · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Estudiante** — *Quiere:* que, cuando se atrasa o se adelanta respecto a su plan o a la malla estándar de su programa, cambia de optativa o quiere cambiar su carga o sus optativas fijadas, el plan se recalcule desde su situación real, con el menor número posible de semestres para su tope, y le muestre el efecto en los semestres que le quedan. *Por qué:* los planes se rompen —al entrevistado, que cursa doble titulación, "no me aprobaron el sobrecupo, y eso me cambió los planes"— y hoy rehacerlos depende de sus propias herramientas: un Excel en el que registra "todas las materias que voy a ver y las opciones de grupos que hay" o, este semestre, BetterCampus. La Entrega 0 ya señalaba, sobre la carga del semestre, que «No hay herramienta que muestre el efecto real de esa decisión sobre los semestres restantes» y, al desviarse de la malla, que «No hay validación automática de que la secuencia elegida sea viable o eficiente a largo plazo».
- **I2 · Estudiante** — *Quiere:* ver qué cambió entre su plan vigente y el nuevo (semestres restantes, periodo estimado de culminación del plan de estudios, materias que cambian de periodo, optativas fijadas y espacios de optativa) y qué situación detectó el sistema (en qué semestre de su plan está, atrasos y adelantos respecto a su plan vigente y a la malla estándar, cambios de alternativa, cambios de normativa). *Por qué:* para decidir con conocimiento si le conviene reemplazar su plan; reemplazarlo sin ver la diferencia sería hacerlo a ciegas. Compararse con la malla estándar es lo que hoy hace a mano el entrevistado con el PDF de la malla de su programa ("lo rayo para saber qué me queda, qué me falta").
- **I3 · Estudiante** — *Quiere:* que su plan vigente solo se reemplace cuando él lo confirma, todo o nada, que el sistema nunca lo recalcule por su cuenta y que le avise cuando esté desactualizado, pero no por el solo paso del tiempo. *Por qué:* quiere conservar un plan que ya le funciona mientras evalúa el nuevo; un reemplazo parcial o automático lo dejaría sin un plan coherente y sin control sobre su propia planeación, y un aviso que apareciera cada semestre, aunque vaya al día, terminaría ignorado.
- **I4 · Estudiante** — *Quiere:* saber que el nuevo plan tampoco verifica oferta, cupos, sobrecupos, horarios, cupo de créditos, reconocimientos, autorizaciones del Consejo de Facultad ni el cumplimiento de los requisitos de grado. *Por qué:* una replanificación suele nacer de un imprevisto y el entrevistado ya desconfía de la información de cupos ("los cupos como que no se ven tan la realidad como se ven"); no debe creer que el nuevo plan resuelve lo que la malla no captura.
- **I5 · Estudiante** — *Quiere:* que su avance y sus planes sean privados: solo él los ve y los modifica. *Por qué:* son datos personales académicos protegidos por la Ley 1581 de 2012.
- **I6 · Administrador de normativa** — *Quiere:* que el plan nuevo quede asociado a la versión del consolidado con que se recalculó, y que los planes calculados con una versión anterior se señalen como desactualizados cuando cambió una regla que aplican, sin recalcularse solos. *Por qué:* para saber cuántos planes (sin identificar a sus dueños) quedan desactualizados por una norma nueva y para poder explicar cada plan, cuando el propio estudiante lo consulta, con la versión que lo produjo.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando el consolidado impide recalcular un plan o falta registrar el periodo en curso, cuando una regla que el plan aplica o muestra no tiene fuente registrada o está en revisión, cuando una materia que ya no rige no se puede contar, o cuando el cálculo falla. *Por qué:* solo él puede corregir el consolidado y, como responsable de operar RutaUN, atiende los errores internos; si no se entera, cada estudiante que replanifique recibirá el mismo error.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que el nuevo plan aplique sus normas que rigen al calcularlo, con su fuente citada, y avise en qué semestre empiezan o dejan de regir las temporales y las programadas, y que RutaUN no resuelva por ellos las autorizaciones ni las excepciones que les corresponden. *Por qué:* la validez de la norma y la decisión sobre casos particulares son de ellos (en especial, de los Consejos de Facultad y de quien verifica la carga mínima, como DINARA en 2026-2); un plan calculado con reglas equivocadas o vencidas llevaría al estudiante a decidir con base en RutaUN y no en la norma.

## Precondiciones
1. El estudiante ha iniciado sesión con su cuenta de RutaUN.
2. El estudiante tiene un plan vigente.
3. Su programa tiene una versión vigente del consolidado.

## Garantía mínima (GM)
1. El plan vigente solo se reemplaza si el estudiante confirma y el guardado se completa (paso 11); en cualquier otra salida permanece exactamente igual. Calcular o ver un plan nuevo no modifica el plan vigente ni el avance. [I3]
2. Nunca hay dos planes vigentes ni un plan a medias: el plan nuevo se guarda completo o no se guarda. [I3]
3. El sistema nunca recalcula un plan sin que el estudiante lo solicite; un plan desactualizado solo se señala. [I3, I6]
4. Este caso de uso no modifica el avance; para cambiarlo, el estudiante usa «Registrar avance en la malla» (UC03). [I5]
5. Nadie más que el estudiante accede a su avance ni a sus planes. [I5]
6. Si el consolidado o un fallo impiden replanificar, queda un incidente para el administrador y el estudiante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]
7. Siempre que se muestra un plan, se muestra con el aviso de lo que RutaUN no verifica. [I4, I8]

## Garantía de éxito (GE)
1. El plan vigente es el plan nuevo que el estudiante revisó y confirmó, y reemplazó al anterior. [I3]
2. El plan nuevo se calculó desde el avance actual, con las optativas fijadas que el estudiante mantuvo, con las garantías de éxito 2 a 5 de «Generar plan de carrera» (UC01). [I1, I8]
3. Antes de confirmar, el estudiante vio las diferencias con el plan vigente, comparadas por periodo académico: semestres restantes, periodo estimado de culminación del plan de estudios, materias que cambian de periodo, optativas fijadas y espacios de optativa. [I1, I2]
4. El estudiante sabe qué situación detectó el sistema: en qué semestre de su plan vigente está; atrasos y adelantos respecto a ese plan y, si registró sus semestres cursados y su programa tiene la malla estándar registrada, respecto a la malla estándar; cambios de alternativa de optativas; y cambios de normativa que afectan su plan. [I2]
5. El plan queda registrado con la fecha, el periodo en curso, el tope, las optativas fijadas, el avance y la versión del consolidado usados, y deja de figurar como desactualizado. [I6]
6. El estudiante sabe lo que RutaUN no verifica. [I4]

## Disparador
El estudiante abre su plan vigente y selecciona «Replanificar plan de carrera» porque se atrasó, adelantó materias, cambió de optativa, quiere cambiar su carga o sus optativas fijadas, o RutaUN le mostró que su plan está desactualizado.

## Escenario principal de éxito
1. El estudiante abre su plan vigente y selecciona «Replanificar plan de carrera».
2. El sistema carga el avance actual, la versión vigente del consolidado y el periodo en curso (que será el semestre 0 del plan nuevo; el semestre 1 será el periodo siguiente), verifica que el consolidado sea calculable y muestra en qué semestre de su plan vigente está el estudiante (ver «Semestre actual del plan») y qué cambió: atrasos, adelantos y cambios de alternativa respecto al plan vigente; atrasos y adelantos respecto a la malla estándar, según los semestres cursados del estudiante (ver «Atraso, adelanto y cambio de alternativa»); y cambios de normativa que afectan el plan. [I1, I2, I6]
3. El sistema propone como punto de partida el tope y las optativas fijadas del plan vigente o, si el caso volvió desde el paso 11, los que el estudiante había indicado. [I1]
4. El estudiante confirma el tope y las optativas fijadas propuestos o los modifica (puede quitar optativas fijadas; para fijar otras, extensión 8b). [I1]
5. El sistema valida el tope: formato, mínimo por periodo, créditos de la materia obligatoria pendiente más pesada y carga máxima por periodo que fije una regla del plan (extensión 5d). [I1, I4, I8]
6. El sistema recalcula el plan con el avance, la versión del consolidado y el periodo en curso cargados en el paso 2, el tope, las optativas fijadas y las reglas del plan vigentes en esa versión, que supone vigentes en todos los semestres (ver «Vigencia»), y registra esa versión. [I1, I6, I8]
7. El sistema muestra el plan nuevo, desde su semestre 0, junto al vigente, con sus diferencias comparadas por periodo académico y no por número de semestre (semestres restantes; periodo estimado de culminación del plan de estudios; materias que cambian de periodo; optativas fijadas y espacios de optativa; marcas de autorización); las reglas temporales, programadas e informativas que cubren el periodo de cada semestre nuevo o que empiezan o dejan de regir en él, cada una con su fuente, las reglas informativas permanentes y el aviso de lo que RutaUN no verifica. [I1, I2, I4, I8]
8. El estudiante revisa las diferencias y decide reemplazar su plan vigente por el nuevo. [I2, I3]
9. El sistema solicita confirmación, advirtiendo que el plan vigente se reemplaza y no se conserva. [I3]
10. El estudiante confirma. [I3]
11. El sistema verifica que el avance y la versión vigente del consolidado sigan siendo los cargados en el paso 2, que el plan vigente no haya cambiado y que el periodo en curso sea el cargado en el paso 2, y guarda el plan nuevo de forma atómica como plan vigente. [I3, I5, I6]
12. El sistema confirma al estudiante que su plan quedó al día y le muestra la fecha, el tope, las optativas fijadas y la versión del consolidado usados. [I3, I6]

## Extensiones
- **2a** · *Condición:* El plan vigente no está desactualizado (ver «Plan desactualizado»): no hay atrasos, adelantos ni cambios de alternativa respecto a él, ni cambió una regla que aplica, y no hay marcas «en curso» por actualizar (si las hay, se atiende la extensión 2b). → *Resolución:* El sistema informa que el plan está al día y que no hace falta replanificarlo (si hay atrasos o adelantos respecto a la malla estándar, los muestra igual). El estudiante puede terminar (el caso termina sin cambios) o continuar para cambiar su tope o sus optativas fijadas (el caso sigue en el paso 3). [I1, I3]
- **2b** · *Condición:* El avance tiene marcas «en curso» por actualizar (de materias o de créditos de libre elección), registradas en un periodo anterior al periodo en curso. → *Resolución:* El sistema las lista y propone actualizarlas antes de replanificar («Registrar avance en la malla», UC03), porque una materia que el estudiante perdió o canceló es un atraso que el plan no vería mientras siga «en curso». Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa sin actualizar, el plan nuevo las cuenta como aprobadas (supuesto del equipo) y el caso sigue en el paso 3. [I1, I2, I3]
- **2c** · *Condición:* El avance marca como aprobada una materia cuyos prerrequisitos figuran pendientes. → *Resolución:* El sistema avisa la inconsistencia (puede deberse a validaciones, homologaciones o convalidaciones, que RutaUN no verifica) y continúa con el avance tal como está registrado; el caso sigue en el paso 3. [I1, I4]
- **2d** · *Condición:* La versión vigente del consolidado cambió desde que se calculó el plan: se incorporaron o modificaron reglas, o alguna empezó o dejó de regir (por el avance del periodo en curso o por la llegada de una fecha). → *Resolución:* El sistema lista las reglas que cambiaron o cuyo estado cambió (de programada a vigente, o de vigente a ya no rige), con su fuente y su vigencia, y señala cuáles afectan al plan, es decir, cuáles lo dejan desactualizado (ver «Plan desactualizado»); si ninguna lo afecta, lo dice. El caso continúa con la versión vigente y sigue en el paso 3. [I2, I6, I8]
- **2e** · *Condición:* El consolidado vigente no es calculable (ver «Consolidado calculable»): por ejemplo, una malla que no es utilizable, una agrupación cuyas optativas no alcanzan para lo que exige, un prerrequisito circular, reglas del plan en revisión o el periodo en curso sin registrar. → *Resolución:* El sistema no calcula e informa al estudiante qué lo impide: si es el consolidado, que el consolidado normativo de su programa tiene un problema; si es el periodo en curso, que el sistema todavía no lo tiene registrado; en ambos casos, que ya se informó al administrador de normativa. Registra un incidente con el programa, la versión y la causa. El plan vigente queda igual y el caso termina sin cambios. [I1, I7]
- **2f** · *Condición:* El sistema muestra atrasos (respecto al plan vigente o a la malla estándar), pero el estudiante indica que ya aprobó esas materias, o que las está cursando, y aún no lo registró en su avance. → *Resolución:* El estudiante sale a «Registrar avance en la malla» (UC03), lo actualiza y vuelve a iniciar este caso desde el paso 1; este caso termina sin cambios, y el sistema no corrige el avance por su cuenta porque no lo lee del SIA. [I1, I5]
- **2g** · *Condición:* No queda nada por cubrir: todas las obligatorias, las agrupaciones y los créditos de libre elección figuran aprobados o en curso (las marcas «en curso» por actualizar cuentan como aprobadas; ver «En curso»). → *Resolución:* El sistema informa que no hay nada por planear: con lo registrado, el estudiante ya habría culminado su plan de estudios. Si hay marcas «en curso» por actualizar, recuerda que eso depende de que las apruebe y ofrece actualizarlas en «Registrar avance en la malla» (UC03). Recuerda también que los requisitos de grado son informativos y RutaUN no los verifica. El plan vigente queda igual y, como no queda nada por cubrir, se muestra como «plan cumplido» (ver «Plan desactualizado»). El caso termina sin cambios. [I1, I3, I4, I8]
- **2h** · *Condición:* El avance tiene materias que ya no rigen y que no se pueden contar, porque su agrupación o su componente tampoco rigen y ninguna equivalencia las ubica (ver «Materia que ya no rige»). → *Resolución:* El sistema las lista, explica que el plan no las cuenta, por lo que puede pedir créditos que la Universidad ya le reconoció, y que ya se informó al administrador de normativa para que cargue la regla que corresponda. El caso sigue en el paso 3. [I1, I2, I7]
- **2i** · *Condición:* Los semestres cursados del estudiante se registraron en un periodo anterior al periodo en curso (ver «Semestres cursados»). → *Resolución:* El sistema muestra la comparación con la malla estándar con ese número, advierte en qué periodo se registró y que puede haber cambiado, y ofrece actualizarlo en «Registrar avance en la malla» (UC03). Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, el caso sigue en el paso 3. [I1, I2]
- **2j** · *Condición:* No se puede comparar con la malla estándar: el programa no tiene completa la malla estándar o el estudiante no ha registrado sus semestres cursados. → *Resolución:* El sistema no muestra atrasos ni adelantos respecto a la malla estándar y dice por qué; si faltan los semestres cursados, indica que puede registrarlos en «Registrar avance en la malla» (UC03). Lo demás se muestra igual y el caso sigue en el paso 3. [I2]
- **2k** · *Condición:* Una optativa fijada del plan vigente ya no aplica: figura aprobada o en curso, ya no rige o su agrupación ya quedó cubierta con otras optativas (ver «Optativa fijada»). → *Resolución:* El sistema lo informa y no la propone en el paso 3; si ya no rige, lo señala como un cambio de normativa. El caso sigue en el paso 3. [I1, I2]
- **5a** · *Condición:* El tope es inválido: ambos límites vacíos o ambos «sin límite», o un valor igual a cero, negativo, decimal o no numérico. → *Resolución:* El sistema señala el campo, explica el formato válido (un entero mayor o igual a 1 o «sin límite», con al menos un límite entero) y pide corregirlo; el caso vuelve al paso 4. [I1]
- **5b** · *Condición:* El tope no permite alcanzar el mínimo por periodo que rige: el límite de créditos es menor que ese mínimo (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10) o el límite de materias es tan bajo que, aun con las materias pendientes de más créditos (un bloque de libre elección cuenta como una materia de 3 créditos), ningún semestre llegaría a ese mínimo. → *Resolución:* El sistema advierte qué mínimo rige y qué norma lo fija (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10), y que con ese tope cada semestre afectado requeriría autorización del Consejo de Facultad, que RutaUN no tramita. El estudiante ajusta el tope (el caso vuelve al paso 4) o lo mantiene (el caso continúa en el paso 6 y el plan marca cada semestre afectado). [I1, I4, I8]
- **5c** · *Condición:* El límite de créditos es menor que los créditos de alguna materia obligatoria pendiente o de alguna optativa fijada, o alguna agrupación no puede cubrirse solo con optativas que quepan en el límite, o el límite es menor que 3 créditos y aún hay créditos de libre elección por cubrir (si faltan menos de 3, el límite es menor que los que faltan; ver «Bloque de libre elección»). → *Resolución:* El sistema lista las materias, las agrupaciones o la libre elección que no caben y el límite mínimo que las haría caber, y no calcula. El estudiante ajusta el límite o quita la optativa fijada que no cabe (el caso vuelve al paso 4) o abandona el caso, que termina sin cambios y con el plan vigente igual. [I1]
- **5d** · *Condición:* El límite de créditos supera la carga máxima por periodo que fija una regla del plan. → *Resolución:* El sistema muestra el máximo y la norma que lo fija, y explica que en todos los semestres el plan usará el máximo en lugar del límite del estudiante. El estudiante continúa (el caso sigue en el paso 6) o ajusta el límite (el caso vuelve al paso 4). [I1, I8]
- **6a** · *Condición:* El cálculo no termina en treinta segundos (supuesto del equipo). → *Resolución:* El sistema lo cancela, no muestra ningún plan nuevo, informa al estudiante y le sugiere reintentar o probar otro tope (paso 4), y registra un incidente. El plan vigente queda igual y, si el estudiante no reintenta, el caso termina sin cambios. [I1, I7]
- **6b** · *Condición:* El cálculo determina que no existe ningún plan que cumpla las reglas del plan, el tope y las optativas fijadas. Las validaciones del paso 5 y de UC07 (extensiones 6d y 6e) lo impiden, así que solo ocurre por un error del consolidado que esas validaciones no detectaron. → *Resolución:* El sistema no muestra un plan parcial: informa que no pudo calcular un plan, con la causa que detectó (la materia o la agrupación, y la norma), y que ya se informó al administrador de normativa, y registra un incidente. El plan vigente queda igual y el caso termina sin cambios. [I1, I7]
- **7a** · *Condición:* Algún semestre del plan nuevo, desde el 1, suma menos créditos que el mínimo por periodo, porque el tope o los prerrequisitos lo impiden o porque es el último y faltan pocos créditos. → *Resolución:* El plan lo muestra con la marca de autorización (rótulo «requiere autorización del Consejo de Facultad»; base: Acuerdo 008 de 2008, art. 10, parágrafo) sin tramitarla. Si una norma temporal que cubre el periodo de ese semestre prevé una exención (como la que la Resolución 057 de 2026 previó, solo para 2026-2, para quien inscribe únicamente los créditos que le faltan para culminar su plan de estudios), el plan la lista como regla informativa junto a la marca, sin quitarla, porque RutaUN no verifica que el estudiante cumpla la condición. [I1, I4, I8]
- **7b** · *Condición:* El plan nuevo culmina el plan de estudios en un periodo posterior al del plan vigente (por ejemplo, por un atraso, por un tope menor o por una optativa fijada que exige algo más). → *Resolución:* El sistema muestra la diferencia sin ocultarla y la causa detectada; el estudiante decide si reemplaza (paso 8). [I2]
- **7c** · *Condición:* El plan nuevo tiene los mismos periodos, con las mismas materias, espacios de optativa y bloques en cada uno, que el vigente. → *Resolución:* El sistema lo informa. El estudiante puede terminar sin reemplazar (el caso termina sin cambios) o reemplazar el plan para dejar registrados el avance actual y la versión vigente del consolidado cargados en el paso 2, con lo que el plan deja de figurar como desactualizado (el caso sigue en el paso 8). [I2, I3, I6]
- **7d** · *Condición:* Una regla que el plan aplica o muestra no tiene fuente registrada. → *Resolución:* El plan la muestra con la señal «sin fuente registrada», no la presenta como respaldada por una norma y el sistema registra un incidente. El caso continúa en el paso 8. [I7, I8]
- **7e** · *Condición:* Una regla programada empieza a regir en el periodo de uno de los semestres del plan nuevo (o en una fecha, si su fecha de aplicación no es un periodo), y con ello cambiará lo que el plan aplica (las materias del programa, sus créditos, su componente o su agrupación, si son obligatorias u optativas, sus prerrequisitos, las reglas de habilitación, las equivalencias, los créditos exigidos por el programa, por un componente o por una agrupación, las optativas de una agrupación, los créditos de libre elección, o el mínimo o el máximo por periodo). → *Resolución:* El plan nuevo se calculó con las reglas del plan vigentes y supone que siguen rigiendo en todos sus semestres (ver «Vigencia»). El sistema señala la regla, con su fuente, en el semestre en que empieza a regir (o para todo el plan, con su fecha), y advierte que, cuando empiece a regir, el sistema creará una versión nueva del consolidado, el plan quedará desactualizado y convendrá replanificarlo de nuevo. El caso continúa en el paso 8. [I1, I8]
- **7f** · *Condición:* Una regla informativa que el plan nuevo muestra está «en revisión» (ver «Regla»). → *Resolución:* El plan la muestra con esa señal y con las dos fuentes que se contradicen, no afirma cuál rige y el sistema registra un incidente. El caso continúa en el paso 8. [I7, I8]
- **8a** · *Condición:* El estudiante quiere probar otro tope. → *Resolución:* El caso vuelve al paso 4 y el plan nuevo mostrado se descarta. Si prefiere ver varias opciones a la vez, este caso termina sin cambios y el estudiante puede usar «Comparar alternativas de carga» (UC04), que también permite guardar la que prefiera. [I1]
- **8b** · *Condición:* El estudiante quiere ver las opciones de un espacio de optativa del plan nuevo, fijar una optativa o quitar una que fijó. → *Resolución:* El sistema muestra las opciones del espacio, con sus créditos y las marcas del tope o del mínimo (ver «Espacio de optativa»), y aparte las demás optativas de la agrupación, cada una con lo que le falta para cursarse en ese semestre y, si el plan lo cumpliría más adelante, desde qué semestre. Si el estudiante fija una optativa o quita una que fijó, el caso vuelve al paso 6 con el mismo tope: el sistema recalcula el plan nuevo con las optativas fijadas que queden y, al mostrarlo (paso 7), indica si cambió el número de semestres y por qué (ver «Optativa fijada»). Si solo consulta, el caso sigue en el paso 8. [I1, I2]
- **8c** · *Condición:* El estudiante decide no reemplazar su plan vigente. → *Resolución:* No se guarda nada. El plan vigente permanece igual y, si estaba desactualizado, sigue señalado como tal. El caso termina sin cambios. [I3]
- **8d** · *Condición:* El estudiante quiere comparar el plan nuevo con la malla estándar de su programa. → *Resolución:* El sistema muestra la comparación (ver «Comparación con la malla estándar»), como en la extensión 7d de «Generar plan de carrera» (UC01). La comparación no cambia el plan; el caso sigue en el paso 8. [I1, I2]
- **10a** · *Condición:* El estudiante no confirma. → *Resolución:* No se guarda nada y el estudiante vuelve a la comparación del paso 7. [I3]
- **11a** · *Condición:* Desde el paso 2 cambió el avance del estudiante (por ejemplo, desde otra sesión) o la versión vigente del consolidado (porque el administrador incorporó una norma o porque el sistema creó una versión al empezar o dejar de regir una regla). → *Resolución:* El sistema no guarda, informa cuál de los dos cambió y ofrece recalcular desde el paso 2 con el tope y las optativas fijadas que había indicado; el plan nuevo mostrado se descarta. Si el estudiante no acepta, el caso termina sin cambios. [I3, I6]
- **11b** · *Condición:* Falla el guardado (error de persistencia). → *Resolución:* Como el guardado es atómico, el plan vigente queda como estaba. El sistema informa el fallo, conserva el plan nuevo en pantalla mientras dure la sesión para que el estudiante confirme de nuevo (paso 10), y registra un incidente. [I3, I7]
- **11c** · *Condición:* Desde el paso 2 cambió el plan vigente (se guardó otro desde otra sesión) o el administrador avanzó el periodo en curso, con lo que el semestre 0 del plan nuevo ya no es el periodo en curso. → *Resolución:* El sistema no guarda, informa qué cambió y ofrece recalcular desde el paso 2 con el tope y las optativas fijadas que había indicado; el plan nuevo mostrado se descarta. Si el estudiante no acepta, el caso termina sin cambios. [I3]
- **12a** · *Condición:* La sesión expira, el estudiante abandona la aplicación u ocurre un error interno después de que el plan se guardó y antes de ver la confirmación. → *Resolución:* El plan ya quedó guardado como plan vigente; el estudiante lo encuentra actualizado al volver a entrar. No se requiere ninguna acción; si fue un error interno, el sistema registra un incidente con el paso y la causa. [I3, I7]
- **Cualquiera** · *Condición:* El estudiante abandona el caso o la sesión expira antes de que el guardado se complete. → *Resolución:* No se guarda nada: lo calculado o editado hasta ese momento se descarta y los datos guardados quedan como estaban. [I3, I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso anterior al guardado (el fallo del propio guardado lo trata la extensión 11b; si el error ocurre después, lo guardado ya quedó guardado, como en la extensión 12a). → *Resolución:* El sistema no modifica los datos guardados, informa que ocurrió un error y que se puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I3, I7]

---

---

## UC02 · Replanificar plan de carrera (5 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC02 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC02-HU01 (#8) · Ver qué cambió desde mi plan vigente
**Como** estudiante, **quiero** ver en qué semestre de mi plan vigente estoy y qué atrasos, adelantos, cambios de alternativa y cambios de normativa detectó el sistema, **para** decidir si me conviene replanificar.
Criterios de aceptación:
- [ ] El sistema muestra el semestre actual del plan y los atrasos, adelantos y cambios de alternativa respecto al plan vigente (paso 2, GE 4).
- [ ] Si registré mis semestres cursados y el programa tiene la malla estándar, muestra también atrasos y adelantos respecto a ella; si no, dice por qué no puede (GE 4, ext. 2j).
- [ ] Si cambió el consolidado, lista las reglas que cambiaron, con su fuente, y señala cuáles dejan desactualizado el plan (ext. 2d).
- [ ] Si el plan está al día, el sistema lo dice y me deja terminar o continuar para cambiar el tope o las optativas; nunca recalcula un plan sin que yo lo pida (ext. 2a, GM 3).

Caso de uso relacionado: UC02 · Replanificar plan de carrera

### UC02-HU02 (#9) · Partir del tope y las optativas del plan vigente
**Como** estudiante, **quiero** que el sistema me proponga el tope y las optativas fijadas de mi plan vigente y poder modificarlos, **para** replanificar sin volver a configurar todo desde cero.
Criterios de aceptación:
- [ ] El sistema propone el tope y las optativas fijadas del plan vigente como punto de partida (paso 3).
- [ ] Una optativa fijada que ya no aplica (aprobada o en curso, que ya no rige o cuya agrupación ya quedó cubierta) no se propone y el sistema explica por qué (ext. 2k).
- [ ] El tope modificado se valida igual que al generar un plan: formato, mínimo por periodo, materias que no caben y carga máxima por periodo (ext. 5a–5d).
- [ ] Puedo fijar o quitar optativas en el plan nuevo y el sistema recalcula con el mismo tope e indica si cambió el número de semestres (ext. 8b).

Caso de uso relacionado: UC02 · Replanificar plan de carrera

### UC02-HU03 (#10) · Ver las diferencias entre el plan vigente y el nuevo
**Como** estudiante, **quiero** ver el plan nuevo junto al vigente, con sus diferencias comparadas por periodo académico, **para** no reemplazar mi plan a ciegas.
Criterios de aceptación:
- [ ] La comparación muestra, por periodo académico y no por número de semestre, los semestres restantes, el periodo estimado de culminación del plan de estudios, las materias que cambian de periodo, las optativas fijadas, los espacios de optativa y las marcas de autorización (GE 3, paso 7).
- [ ] Si el plan nuevo culmina más tarde que el vigente, el sistema muestra la diferencia sin ocultarla y la causa detectada (ext. 7b).
- [ ] Si el plan nuevo es igual al vigente, el sistema lo informa y me deja terminar sin cambios o reemplazarlo para dejar registrados el avance y la versión actuales (ext. 7c).
- [ ] El plan nuevo se muestra con el aviso de lo que RutaUN no verifica (GM 7).

Caso de uso relacionado: UC02 · Replanificar plan de carrera

### UC02-HU04 (#11) · Reemplazar el plan vigente
**Como** estudiante, **quiero** reemplazar mi plan vigente por el nuevo solo cuando lo confirme, **para** mantener un plan coherente y bajo mi control.
Criterios de aceptación:
- [ ] El sistema pide confirmación advirtiendo que el plan vigente se reemplaza y no se conserva; si no confirmo o decido no reemplazar, no se guarda nada y el plan vigente sigue igual, señalado como desactualizado si lo estaba (paso 9, ext. 8c, 10a).
- [ ] El guardado es atómico: nunca hay dos planes vigentes ni un plan a medias (GM 1, GM 2).
- [ ] Si desde que se cargaron los datos cambió mi avance, la versión del consolidado, mi plan vigente o el periodo en curso, el sistema no guarda y ofrece recalcular (ext. 11a, 11c).
- [ ] Al guardar, el sistema confirma que el plan quedó al día, muestra la fecha, el tope, las optativas fijadas y la versión usados, y el plan deja de figurar como desactualizado (GE 5, paso 12).

Caso de uso relacionado: UC02 · Replanificar plan de carrera

### UC02-HU05 (#12) · Actualizar el avance antes de replanificar
**Como** estudiante, **quiero** que el sistema me proponga actualizar mi avance cuando detecta marcas por actualizar o atrasos que ya resolví, **para** replanificar desde mi situación real.
Criterios de aceptación:
- [ ] Si hay marcas «en curso» por actualizar, el sistema las lista y propone actualizarlas en «Registrar avance en la malla» (UC03); si continúo, el plan nuevo las cuenta como aprobadas (ext. 2b).
- [ ] Si el sistema muestra atrasos de materias que ya aprobé o estoy cursando, puedo ir a UC03 a registrarlas; el sistema no corrige el avance por su cuenta (ext. 2f, GM 4).
- [ ] Si mis semestres cursados se registraron en un periodo anterior, el sistema lo advierte y ofrece actualizarlos (ext. 2i).

Caso de uso relacionado: UC02 · Replanificar plan de carrera

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #8–#12 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
