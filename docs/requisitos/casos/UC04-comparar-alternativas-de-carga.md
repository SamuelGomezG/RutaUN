---
title: "UC04 · Comparar alternativas de carga"
doc_type: "caso-de-uso"
srs_ids:
  - UC04
  - UC04-HU01..HU05
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
# UC04 · Comparar alternativas de carga

> **Cuándo cargar:** para implementar o revisar **UC04**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Estudiante · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Estudiante** — *Quiere:* comparar entre dos y cuatro alternativas de carga (y contra su plan vigente, si lo tiene) y ver, para cada una, cuántos semestres le faltan para culminar su plan de estudios, en qué periodo lo culminaría y cómo queda la carga por semestre. *Por qué:* la carga es una decisión suya y hoy se toma sin ver su efecto. La Entrega 0 planteó como hipótesis que el estudiante decide según «Capacidad personal, otras obligaciones, qué tan rápido quiere graduarse» y señaló que lo que queda oculto es que «No hay herramienta que muestre el efecto real de esa decisión sobre los semestres restantes». El entrevistado, además, trabaja mientras estudia (acta ZEUS-01, sección 1).
- **I2 · Estudiante** — *Quiere:* que la comparación sea justa y transparente: todas las alternativas calculadas con el mismo avance, la misma versión del consolidado, las mismas reglas y las mismas optativas fijadas, y que se entienda por qué una alternativa no es viable o por qué dos dan lo mismo. *Por qué:* para no elegir con cifras que no son comparables: la diferencia debe venir del tope y no de los datos.
- **I3 · Estudiante** — *Quiere:* poder guardar como su plan vigente la alternativa que prefiera, viendo su plan completo, sin que comparar modifique nada y con un guardado de todo o nada. *Por qué:* así pasa de decidir a tener un plan sin recalcularlo aparte, y no arriesga su plan vigente por explorar.
- **I4 · Estudiante** — *Quiere:* saber qué no garantizan las alternativas: oferta, cupos, sobrecupos, horarios, cupo de créditos, reconocimientos, autorizaciones del Consejo de Facultad y cumplimiento de los requisitos de grado. *Por qué:* una carga mayor se ve mejor en semestres, pero depende de cupos que el sistema no ve; el entrevistado, que cursa doble titulación ("uno siempre tiene que meter las materias por sobrecupos"), cuenta que "nunca pregunté qué tan factible es que a uno le den sobrecupo", y una alternativa que necesita más cupos no es igual de segura que otra.
- **I5 · Estudiante** — *Quiere:* que su avance sea privado (solo él lo ve y lo modifica) y que nadie más vea lo que compara. *Por qué:* son datos personales académicos protegidos por la Ley 1581 de 2012.
- **I6 · Administrador de normativa** — *Quiere:* que todas las alternativas se calculen con la versión vigente del consolidado y que el plan guardado registre el avance y la versión usados. *Por qué:* para que el plan que se guarde desde la comparación se pueda explicar con la versión registrada cuando el propio estudiante lo consulta, y para saber cuántos planes (sin identificar a sus dueños) quedan desactualizados por una norma nueva.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando una alternativa o la referencia no se puede calcular por el consolidado o porque falta registrar el periodo en curso, cuando una regla que la comparación aplica o muestra no tiene fuente registrada o está en revisión, cuando una materia que ya no rige no se puede contar, o cuando el cálculo o el guardado fallan. *Por qué:* solo él puede corregir el consolidado y, como responsable de operar RutaUN, atiende los errores internos; si no se entera, las comparaciones de ese programa seguirán fallando.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que cada alternativa aplique sus normas vigentes al calcularla (mínimo y máximo) y que la comparación no presente como resuelta una autorización del Consejo de Facultad. *Por qué:* la carga mínima (art. 10 del Acuerdo 008 de 2008) y las cargas máximas que fijen normas posteriores son decisiones institucionales (en especial, de los Consejos de Facultad, que autorizan cargas inferiores al mínimo según el parágrafo de ese artículo, y de quien verifica la carga mínima, como DINARA en 2026-2); una alternativa que depende de una autorización no debe verse como la mejor sin decirlo.

## Precondiciones
1. El estudiante ha iniciado sesión con su cuenta de RutaUN.
2. El estudiante tiene asociado un programa cargado en el sistema (un solo programa) y ese programa tiene una versión vigente del consolidado.

## Garantía mínima (GM)
1. Comparar no modifica ningún dato guardado (ni el avance ni el plan vigente): nada cambia hasta que el estudiante confirma guardar una alternativa y el guardado se completa (paso 10). [I3]
2. Ninguna alternativa se guarda a medias: el plan elegido se guarda completo o no se guarda. [I3]
3. Todas las alternativas de una comparación, y su referencia, se calculan con el mismo avance, la misma versión del consolidado, el mismo periodo en curso y las mismas optativas fijadas. [I2, I6]
4. Una alternativa que no se puede calcular no se oculta: aparece como no viable o no calculada, con su causa; si es la referencia la que no se puede calcular, la comparación se muestra sin ella y con la causa. [I2]
5. Solo el estudiante accede a su avance y a sus comparaciones; las comparaciones no se guardan. [I5]
6. Si el consolidado o un fallo impiden comparar, queda un incidente para el administrador y el estudiante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]
7. Siempre que se muestra una comparación o un plan, se muestra con el aviso de lo que RutaUN no verifica. [I4, I8]

## Garantía de éxito (GE)
1. Para cada alternativa viable, el estudiante ve cuántos semestres le faltan para culminar su plan de estudios, el periodo estimado de culminación del plan de estudios, los créditos y materias por semestre y los semestres con marca de autorización, junto con la referencia del plan vigente cuando existe. [I1, I2]
2. Cada alternativa se calculó con las garantías de éxito 2 a 5 de «Generar plan de carrera» (UC01). [I1, I8]
3. El estudiante entiende por qué una alternativa no es viable o por qué dos alternativas dan el mismo resultado. [I2]
4. Si el estudiante eligió guardar una alternativa, esa alternativa es su plan vigente, reemplazó al anterior y quedó registrada con la fecha, el tope, las optativas fijadas, el avance y la versión del consolidado usados. [I3, I6]
5. Si el estudiante no eligió guardar ninguna, los datos guardados quedaron como estaban. [I3]
6. El estudiante sabe lo que RutaUN no verifica. [I4]

## Disparador
El estudiante quiere saber cuántos semestres le faltarían con distintas cargas y selecciona «Comparar alternativas de carga».

## Escenario principal de éxito
1. El estudiante selecciona «Comparar alternativas de carga».
2. El sistema carga el avance, la versión vigente del consolidado, el periodo en curso (que será el semestre 0 de cada alternativa; el semestre 1 será el periodo siguiente) y, si el estudiante tiene plan vigente, sus optativas fijadas, y verifica que el consolidado sea calculable (ver «Consolidado calculable»). [I1, I5, I6]
3. El estudiante define entre dos y cuatro alternativas de carga (A a D), cada una con su tope, el mismo para todos los semestres: el límite de créditos, el límite de materias, o ambos. [I1]
4. El sistema valida cada alternativa: formato, mínimo por periodo, créditos de la materia obligatoria pendiente más pesada y carga máxima por periodo que fije una regla del plan (extensión 4d). [I1, I4, I8]
5. El sistema calcula el plan de cada alternativa y, si el estudiante tiene plan vigente, la referencia, todos con el mismo avance, la misma versión del consolidado, el mismo periodo en curso y las mismas optativas fijadas cargados en el paso 2, y con las reglas del plan vigentes en esa versión, que supone vigentes en todos los semestres (ver «Vigencia»). [I1, I2, I6, I8]
6. El sistema muestra la comparación de las alternativas: por alternativa, los semestres restantes para culminar el plan de estudios, el periodo estimado de culminación del plan de estudios, los créditos y materias por semestre, los semestres con marca de autorización y la diferencia frente a la referencia (semestres restantes de la referencia menos semestres restantes de la alternativa: un valor positivo significa que la alternativa termina antes); además, las reglas temporales, programadas e informativas que cubren los periodos comparados o que empiezan o dejan de regir en ellos, cada una con su fuente, y el aviso de lo que RutaUN no verifica. [I1, I2, I4, I8]
7. El estudiante elige una alternativa viable para guardarla como su plan vigente. [I3]
8. El sistema muestra el plan completo de la alternativa elegida y solicita confirmación, advirtiendo que reemplazará el plan vigente cuando exista. [I3, I4]
9. El estudiante confirma. [I3]
10. El sistema verifica que el avance y la versión vigente del consolidado sigan siendo los cargados en el paso 2, que el plan vigente no haya cambiado y que el periodo en curso sea el cargado en el paso 2, y guarda el plan de forma atómica como plan vigente. [I3, I5, I6]
11. El sistema confirma el guardado al estudiante. [I3, I6]

## Extensiones
- **2a** · *Condición:* El estudiante no tiene avance registrado (todas las materias están pendientes y no hay créditos de libre elección registrados). → *Resolución:* El sistema lo informa y ofrece registrarlo antes («Registrar avance en la malla», UC03). Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, se calcula con todo pendiente y el caso sigue en el paso 3. [I1]
- **2b** · *Condición:* El estudiante no tiene plan vigente. → *Resolución:* No hay referencia ni optativas fijadas: la comparación es solo entre las alternativas y, al guardar (paso 10), no hay plan que se reemplace. El caso continúa en el paso 3. [I1, I3]
- **2c** · *Condición:* El plan vigente del estudiante está desactualizado. → *Resolución:* La referencia se calcula con el avance actual y la versión vigente del consolidado, no con los del plan guardado, y el sistema lo dice para que no se confunda con el plan guardado. El caso continúa en el paso 3. [I2]
- **2d** · *Condición:* El consolidado vigente no es calculable (ver «Consolidado calculable»): por ejemplo, una malla que no es utilizable, una agrupación cuyas optativas no alcanzan para lo que exige, un prerrequisito circular, reglas del plan en revisión o el periodo en curso sin registrar. → *Resolución:* El sistema no calcula e informa al estudiante qué lo impide: si es el consolidado, que el consolidado normativo de su programa tiene un problema; si es el periodo en curso, que el sistema todavía no lo tiene registrado; en ambos casos, que ya se informó al administrador de normativa. Registra un incidente con el programa, la versión y la causa. El caso termina sin cambios. [I1, I7]
- **2e** · *Condición:* No queda nada por cubrir: todas las obligatorias, las agrupaciones y los créditos de libre elección figuran aprobados o en curso (las marcas «en curso» por actualizar cuentan como aprobadas; ver «En curso»). → *Resolución:* El sistema informa que no hay nada que comparar: con lo registrado, el estudiante ya habría culminado su plan de estudios. Si hay marcas «en curso» por actualizar, recuerda que eso depende de que las apruebe y ofrece actualizarlas en «Registrar avance en la malla» (UC03). Recuerda también que los requisitos de grado son informativos y RutaUN no los verifica; el plan vigente, si existe, se muestra como «plan cumplido» (ver «Plan desactualizado»). El caso termina sin cambios. [I1, I4, I8]
- **2f** · *Condición:* El avance tiene marcas «en curso» por actualizar (de materias o de créditos de libre elección), registradas en un periodo anterior al periodo en curso. → *Resolución:* El sistema las lista y ofrece actualizarlas antes de comparar («Registrar avance en la malla», UC03). Si el estudiante acepta, este caso termina sin cambios y el estudiante pasa a ese caso; si continúa, se cuentan como aprobadas en todas las alternativas (supuesto del equipo) y el caso sigue en el paso 3. [I1, I2]
- **2g** · *Condición:* El avance tiene materias que ya no rigen y que no se pueden contar, porque su agrupación o su componente tampoco rigen y ninguna equivalencia las ubica (ver «Materia que ya no rige»). → *Resolución:* El sistema las lista, explica que ninguna alternativa las cuenta, por lo que pueden pedir créditos que la Universidad ya le reconoció, y que ya se informó al administrador de normativa para que cargue la regla que corresponda. El caso sigue en el paso 3. [I1, I2, I7]
- **2h** · *Condición:* El plan vigente tiene optativas fijadas. → *Resolución:* Todas las alternativas y la referencia se calculan con ellas (salvo las que ya no aplican; ver «Optativa fijada»), para que la diferencia venga solo del tope, y el sistema lo dice. El plan que se guarde las conserva. Para cambiarlas, el estudiante usa «Replanificar plan de carrera» (UC02). El caso continúa en el paso 3. [I2]
- **3a** · *Condición:* El estudiante define menos de dos o más de cuatro alternativas. → *Resolución:* El sistema no calcula y pide definir entre dos y cuatro; el caso vuelve al paso 3. [I1]
- **3b** · *Condición:* Dos alternativas, o una alternativa y la referencia, tienen exactamente el mismo tope. → *Resolución:* El sistema señala la repetición y pide modificar una de las alternativas (la referencia no se modifica; el caso vuelve al paso 3); no calcula duplicados. Topes distintos que producen el mismo plan sí se aceptan y se informan en el paso 6. [I2]
- **4a** · *Condición:* El tope de una alternativa es inválido: ambos límites vacíos o ambos «sin límite», o un valor igual a cero, negativo, decimal o no numérico. → *Resolución:* El sistema señala cuál alternativa y por qué, y pide corregirla (el caso vuelve al paso 3); las demás alternativas se conservan. [I1]
- **4b** · *Condición:* El tope de una alternativa no permite alcanzar el mínimo por periodo que rige: el límite de créditos es menor que ese mínimo (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10) o el límite de materias es tan bajo que, aun con las materias pendientes de más créditos (un bloque de libre elección cuenta como una materia de 3 créditos), ningún semestre llegaría a ese mínimo. → *Resolución:* El sistema advierte qué mínimo rige y qué norma lo fija (hoy, 10 créditos, Acuerdo 008 de 2008, art. 10), y que con ese tope cada semestre afectado requeriría autorización del Consejo de Facultad, que RutaUN no tramita. El estudiante la ajusta (el caso vuelve al paso 3) o la mantiene (el caso continúa en el paso 5; la comparación cuenta y lista los semestres marcados). [I1, I4, I8]
- **4c** · *Condición:* El límite de créditos de una alternativa es menor que los créditos de alguna materia obligatoria pendiente o de alguna optativa fijada, o alguna agrupación no puede cubrirse solo con optativas que quepan en el límite, o el límite es menor que 3 créditos y aún hay créditos de libre elección por cubrir (si faltan menos de 3, el límite es menor que los que faltan; ver «Bloque de libre elección»). → *Resolución:* Esa alternativa aparece como no viable con la materia, la agrupación o la libre elección que no cabe y el límite mínimo que la haría caber; las demás se calculan. Si ninguna alternativa quedó calculada y viable, se resuelve como en la extensión 5a. [I1, I2]
- **4d** · *Condición:* El límite de créditos de una alternativa supera la carga máxima por periodo que fija una regla del plan. → *Resolución:* El sistema calcula esa alternativa con el máximo en todos los semestres y lo indica para esa alternativa: el máximo y la norma que lo fija. [I1, I2, I8]
- **5a** · *Condición:* El cálculo de una alternativa no termina en treinta segundos (supuesto del equipo). → *Resolución:* Esa alternativa aparece como no calculada y las demás se muestran; el sistema registra un incidente. Si, entre esta extensión y las extensiones 4c y 5b, ninguna alternativa quedó calculada y viable, no hay comparación: el estudiante puede reintentar o cambiar los topes (el caso vuelve al paso 3 con las alternativas cargadas y editables) o abandonar (el caso termina sin cambios). [I1, I2, I7]
- **5b** · *Condición:* El cálculo determina que no existe ningún plan para una alternativa. Las validaciones del paso 4 y de UC07 (extensiones 6d y 6e) lo impiden, así que solo ocurre por un error del consolidado que esas validaciones no detectaron. → *Resolución:* Esa alternativa aparece como no calculada, con la causa que detectó (la materia o la agrupación, y la norma), y el sistema registra un incidente. Si ninguna alternativa quedó calculada y viable, se resuelve como en la extensión 5a. [I2, I7]
- **5c** · *Condición:* No se puede calcular la referencia: su tope ya no es viable con el avance actual y la versión vigente del consolidado, el cálculo no termina en treinta segundos (supuesto del equipo) o no existe plan. → *Resolución:* La comparación se hace sin referencia; el sistema lo informa con la causa y registra un incidente si la causa es el consolidado o el tiempo. El caso continúa en el paso 6. [I1, I2, I7]
- **6a** · *Condición:* Dos o más alternativas, o una alternativa y la referencia, producen exactamente el mismo plan. → *Resolución:* El sistema lo informa (por ejemplo, porque los prerrequisitos y no el tope son el límite) para que el estudiante no elija por una diferencia que no existe. [I2]
- **6b** · *Condición:* Ninguna alternativa mejora a la referencia. → *Resolución:* El sistema lo dice con claridad, mostrando que ninguna alternativa tiene menos semestres restantes que la referencia (diferencia cero o negativa), y no sugiere ninguna como mejor. [I1, I2]
- **6c** · *Condición:* Una alternativa deja semestres con marca de autorización. → *Resolución:* La comparación cuenta y lista esos semestres y no presenta esa alternativa como mejor sin señalarlo; la autorización no se tramita ni se verifica. [I1, I4, I8]
- **6d** · *Condición:* Una regla que la comparación aplica o muestra no tiene fuente registrada. → *Resolución:* La comparación la muestra con la señal «sin fuente registrada», no la presenta como respaldada por una norma y el sistema registra un incidente. El caso continúa en el paso 7. [I7, I8]
- **6e** · *Condición:* Una regla programada empieza a regir en el periodo de uno de los semestres de alguna alternativa o de la referencia (o en una fecha, si su fecha de aplicación no es un periodo), y con ello cambiará lo que el plan aplica (las materias del programa, sus créditos, su componente o su agrupación, si son obligatorias u optativas, sus prerrequisitos, las reglas de habilitación, las equivalencias, los créditos exigidos por el programa, por un componente o por una agrupación, las optativas de una agrupación, los créditos de libre elección, o el mínimo o el máximo por periodo). → *Resolución:* Las alternativas y la referencia se calcularon con las reglas del plan vigentes, que se suponen vigentes en todos sus semestres (ver «Vigencia»). El sistema señala la regla, con su fuente, en el semestre en que empieza a regir en cada alternativa afectada (o para toda la comparación, con su fecha), y advierte que, cuando empiece a regir, el sistema creará una versión nueva del consolidado, el plan que se guarde quedará desactualizado y convendrá replanificarlo («Replanificar plan de carrera», UC02). El caso continúa en el paso 7. [I1, I8]
- **6f** · *Condición:* Una regla informativa que la comparación muestra está «en revisión» (ver «Regla»). → *Resolución:* La comparación la muestra con esa señal y con las dos fuentes que se contradicen, no afirma cuál rige y el sistema registra un incidente. El caso continúa en el paso 7. [I7, I8]
- **7a** · *Condición:* El estudiante no quiere guardar ninguna alternativa. → *Resolución:* El caso termina con éxito; no se guarda nada y los datos guardados quedan como estaban. [I3]
- **7b** · *Condición:* El estudiante intenta elegir una alternativa no viable o no calculada. → *Resolución:* El sistema no lo permite y muestra la causa; el caso vuelve al paso 7. [I2, I3]
- **7c** · *Condición:* El estudiante quiere ajustar una alternativa o probar otras. → *Resolución:* El caso vuelve al paso 3 con las alternativas cargadas y editables; las cifras mostradas se descartan. [I1]
- **7d** · *Condición:* El estudiante quiere comparar las alternativas con la malla estándar de su programa. → *Resolución:* El sistema muestra, para cada alternativa viable, sus semestres restantes frente a los que le quedarían según la malla estándar y, para la alternativa que el estudiante elija, la comparación completa (ver «Comparación con la malla estándar»); si falta la malla estándar, algún dato de ella o los semestres cursados, muestra lo que pueda y dice qué falta. La comparación no cambia las alternativas; el caso sigue en el paso 7. [I1]
- **8a** · *Condición:* El estudiante ya tiene un plan vigente. → *Resolución:* La confirmación muestra el plan vigente (semestres restantes y periodo estimado de culminación del plan de estudios) junto al de la alternativa elegida, deja claro que el nuevo lo reemplaza y que el anterior no se conserva, y avisa que «Replanificar plan de carrera» (UC02) muestra además qué materias cambian de periodo. [I3]
- **9a** · *Condición:* El estudiante no confirma. → *Resolución:* No se guarda nada y el estudiante vuelve a la comparación del paso 6. [I3]
- **10a** · *Condición:* Desde el paso 2 cambió el avance del estudiante (por ejemplo, desde otra sesión) o la versión vigente del consolidado (porque el administrador incorporó una norma o porque el sistema creó una versión al empezar o dejar de regir una regla). → *Resolución:* El sistema no guarda, informa cuál de los dos cambió y ofrece recalcular la comparación desde el paso 2, conservando las alternativas definidas; como las cifras mostradas dejaron de valer, no se guarda ninguna. Si el estudiante no acepta, el caso termina sin cambios. [I2, I3, I6]
- **10b** · *Condición:* Falla el guardado (error de persistencia). → *Resolución:* Como el guardado es atómico, el plan vigente queda como estaba. El sistema informa el fallo, conserva la comparación en pantalla mientras dure la sesión para que el estudiante confirme de nuevo (paso 9), y registra un incidente. [I3, I7]
- **10c** · *Condición:* Desde el paso 2 cambió el plan vigente (se guardó otro desde otra sesión) o el administrador avanzó el periodo en curso, con lo que el semestre 0 de las alternativas ya no es el periodo en curso. → *Resolución:* El sistema no guarda, informa qué cambió y ofrece recalcular la comparación desde el paso 2, conservando las alternativas definidas; las cifras mostradas se descartan. Si el estudiante no acepta, el caso termina sin cambios. [I2, I3]
- **11a** · *Condición:* La sesión expira, el estudiante abandona la aplicación u ocurre un error interno después de que el plan se guardó y antes de ver la confirmación. → *Resolución:* El plan ya quedó guardado como plan vigente; el estudiante lo encuentra actualizado al volver a entrar. No se requiere ninguna acción; si fue un error interno, el sistema registra un incidente con el paso y la causa. [I3, I7]
- **Cualquiera** · *Condición:* El estudiante abandona el caso o la sesión expira antes de que el guardado se complete. → *Resolución:* No se guarda nada: lo calculado o editado hasta ese momento se descarta y los datos guardados quedan como estaban. Las alternativas y su comparación no se conservan. [I3, I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso anterior al guardado (el fallo del propio guardado lo trata la extensión 10b; si el error ocurre después, lo guardado ya quedó guardado, como en la extensión 11a). → *Resolución:* El sistema no modifica los datos guardados, informa que ocurrió un error y que se puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I3, I7]

---

---

## UC04 · Comparar alternativas de carga (5 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC04 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC04-HU01 (#22) · Definir y validar alternativas de carga
**Como** estudiante, **quiero** definir entre dos y cuatro alternativas de carga, cada una con su tope, **para** probar distintas cargas sin calcularlas una por una.
Criterios de aceptación:
- [ ] El sistema exige entre dos y cuatro alternativas y no calcula dos con el mismo tope, ni una con el mismo tope que la referencia (ext. 3a, 3b).
- [ ] Si el tope de una alternativa es inválido, el sistema señala cuál y por qué, y conserva las demás (ext. 4a).
- [ ] Una alternativa que no cabe en su límite de créditos aparece como no viable, con lo que no cabe y el límite mínimo que lo haría caber; no se oculta (ext. 4c, GM 4).
- [ ] Si el límite de una alternativa supera la carga máxima por periodo de una regla del plan, se calcula con el máximo y se indica para esa alternativa (ext. 4d).

Caso de uso relacionado: UC04 · Comparar alternativas de carga

### UC04-HU02 (#23) · Ver la comparación de las alternativas
**Como** estudiante, **quiero** ver, por alternativa, cuántos semestres me faltan, en qué periodo culminaría mi plan de estudios y cómo queda la carga por semestre, **para** decidir mi carga viendo su efecto real.
Criterios de aceptación:
- [ ] Por cada alternativa viable se muestran los semestres restantes, el periodo estimado de culminación del plan de estudios, los créditos y materias por semestre y los semestres con marca de autorización (GE 1).
- [ ] Si dos alternativas producen el mismo plan, el sistema lo informa para que no elija por una diferencia que no existe (ext. 6a, GE 3).
- [ ] Una alternativa con semestres que requieren autorización no se presenta como mejor sin señalarlo (ext. 6c).
- [ ] Una alternativa cuyo cálculo falla o tarda más de treinta segundos aparece como no calculada, con su causa, y las demás se muestran igual (ext. 5a, 5b, GM 4).

Caso de uso relacionado: UC04 · Comparar alternativas de carga

### UC04-HU03 (#24) · Comparar contra mi plan vigente
**Como** estudiante con plan vigente, **quiero** que la comparación incluya como referencia mi plan vigente recalculado con mi avance actual, **para** saber si alguna alternativa me hace culminar antes que mi plan actual.
Criterios de aceptación:
- [ ] Todas las alternativas y la referencia se calculan con el mismo avance, la misma versión del consolidado, el mismo periodo en curso y las mismas optativas fijadas, y el sistema lo dice (GM 3, ext. 2h).
- [ ] Cada alternativa muestra su diferencia frente a la referencia en semestres restantes; si ninguna la mejora, el sistema lo dice y no sugiere ninguna como mejor (paso 6, ext. 6b).
- [ ] Si mi plan vigente está desactualizado, el sistema aclara que la referencia usa el avance actual y no el del plan guardado (ext. 2c).
- [ ] Si la referencia no se puede calcular, la comparación se muestra sin ella y con la causa (ext. 5c, GM 4).

Caso de uso relacionado: UC04 · Comparar alternativas de carga

### UC04-HU04 (#25) · Guardar una alternativa como plan vigente
**Como** estudiante, **quiero** guardar como mi plan vigente la alternativa que prefiera, viendo antes su plan completo, **para** pasar de decidir a tener un plan sin recalcularlo aparte.
Criterios de aceptación:
- [ ] Solo puedo elegir una alternativa viable y calculada; si intento otra, el sistema no lo permite y muestra la causa (ext. 7b).
- [ ] Antes de guardar, el sistema muestra el plan completo de la alternativa y, si tengo plan vigente, ambos juntos, aclarando que el anterior no se conserva (paso 8, ext. 8a).
- [ ] El guardado es atómico; si cambió mi avance, la versión del consolidado, mi plan vigente o el periodo en curso, no se guarda y el sistema ofrece recalcular conservando las alternativas (GM 2, ext. 10a, 10c).
- [ ] Si no guardo ninguna, nada cambia y la comparación no se conserva (GE 5, GM 5).

Caso de uso relacionado: UC04 · Comparar alternativas de carga

### UC04-HU05 (#26) · Comparar las alternativas con la malla estándar
**Como** estudiante, **quiero** comparar las alternativas con la malla estándar de mi programa, **para** ver cuánto me alejo de lo que sugiere la Universidad con cada carga.
Criterios de aceptación:
- [ ] Para cada alternativa viable, el sistema muestra sus semestres restantes frente a los que me quedarían según la malla estándar (ext. 7d).
- [ ] Para la alternativa que elija, muestra la comparación completa con la malla estándar (ext. 7d).
- [ ] Si falta la malla estándar, algún dato de ella o mis semestres cursados, muestra lo que pueda y dice qué falta; la comparación no cambia las alternativas (ext. 7d).

Caso de uso relacionado: UC04 · Comparar alternativas de carga

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #22–#26 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
