---
title: "UC03 · Registrar avance en la malla"
doc_type: "caso-de-uso"
srs_ids:
  - UC03
  - UC03-HU01..HU09
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
# UC03 · Registrar avance en la malla

> **Cuándo cargar:** para implementar o revisar **UC03**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Estudiante · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Estudiante** — *Quiere:* marcar en la malla lo que ya aprobó y lo que está cursando —incluidas las optativas dentro de sus agrupaciones y subagrupaciones, y los créditos de libre elección— y ver de inmediato, en cada componente y cada agrupación, cuántos créditos le faltan y qué obligatorias, optativas y subagrupaciones le quedan pendientes y, si registra sus semestres cursados, dónde está respecto a la malla estándar de su programa. *Por qué:* el entrevistado lo hace hoy sobre un PDF ("Tengo el PDF y yo lo rayo para saber qué me queda, qué me falta") o, alguna vez, en su doble titulación, cuadrando las dos mallas en Paint ("tomaba pedazos de cada una de las mallas y las iba cuadrando para contar los créditos"). Él mismo describió la función: una malla simulada donde se pueda "cliquear y poner cuáles son las que le faltan, cuáles ya vio". Ver solo un total de créditos por agrupación no le basta: necesita saber qué materias componen lo que le falta.
- **I2 · Estudiante** — *Quiere:* que su avance refleje lo que la Universidad ya le reconoció (validaciones, homologaciones y convalidaciones) sin tener que justificarlo ante el sistema, y que el sistema le avise de las inconsistencias sin impedirle registrar su realidad. *Por qué:* RutaUN no lee la historia académica del SIA; el avance es lo que el estudiante declara. Si el sistema rechazara un reconocimiento que la Universidad ya hizo (Acuerdo 008 de 2008, arts. 29 y 38), el avance dejaría de servir para planear.
- **I3 · Estudiante** — *Quiere:* que sus cambios se guarden completos o no se guarden, y que no pisen cambios hechos desde otra sesión. *Por qué:* un avance a medias o sobrescrito produciría planes incorrectos sin que él lo note.
- **I4 · Estudiante** — *Quiere:* saber si, al cambiar su avance, su plan vigente queda desactualizado (porque se atrasó, se adelantó o cambió de optativa) o sigue al día y en qué semestre de él está, y que RutaUN no recalcule el plan por su cuenta. *Por qué:* no quiere confiar en un plan que ya no refleja su avance, pero tampoco perder el plan que tiene antes de decidir replanificar, ni recibir avisos cuando solo registra lo que el plan preveía.
- **I5 · Estudiante** — *Quiere:* que su avance sea privado: solo él lo ve y lo modifica. *Por qué:* es un dato personal académico protegido por la Ley 1581 de 2012.
- **I6 · Administrador de normativa** — *Quiere:* que las marcas queden registradas contra la versión vigente del consolidado y que una materia aprobada que deja de regir siga contando como la Universidad la reconoció. *Por qué:* para no perder el avance de los estudiantes cuando cambia una malla y para saber cuántos avances, sin identificar a sus dueños, tienen materias que una norma elimina.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando el consolidado impide mostrar la malla, tiene un prerrequisito circular o que apunta a una materia que no rige o no permite contar una materia que ya no rige, o cuando ocurre un error interno. *Por qué:* solo él puede corregir el consolidado y, como responsable de operar RutaUN, atiende los errores internos; sin el aviso, ningún estudiante de ese programa podría registrar su avance.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que la malla mostrada (componentes, agrupaciones, subagrupaciones y créditos exigidos) sea la de la versión vigente del consolidado y que RutaUN no verifique ni decida por su cuenta validaciones, homologaciones o convalidaciones, que les corresponden. *Por qué:* son decisiones suyas (las homologaciones, en especial, de los Consejos de Facultad; las validaciones, de la Unidad Académica que ofrece la materia, art. 29) y tienen límites (el artículo 38 del Acuerdo 008 de 2008 limita el total de homologaciones, convalidaciones y validaciones al 50% del mínimo de créditos del plan de estudios; solo cuando la reciprocidad de un convenio de doble titulación con una universidad extranjera lo exige puede autorizarse más, sin superar el 75%); una malla distorsionada llevaría al estudiante a contar mal lo que le falta.

## Precondiciones
1. El estudiante ha iniciado sesión con su cuenta de RutaUN.
2. El estudiante tiene asociado un programa cargado en el sistema (un solo programa) y ese programa tiene una versión vigente del consolidado.

## Garantía mínima (GM)
1. El avance guardado queda exactamente como estaba hasta que el estudiante guarde; marcar o editar en pantalla no cambia nada guardado. [I3]
2. Los cambios se guardan todos o ninguno. [I3]
3. Ningún cambio del estudiante sobrescribe cambios más recientes hechos desde otra sesión. [I3]
4. El sistema nunca marca una materia por su cuenta: solo registra lo que el estudiante declara y nunca lo lee del SIA. [I1, I2, I5]
5. El plan vigente no se modifica ni se recalcula en este caso de uso. [I4]
6. Solo el estudiante accede a su avance. [I5]
7. Si el consolidado impide mostrar la malla o hay un fallo, queda un incidente para el administrador y el estudiante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]

## Garantía de éxito (GE)
1. El avance guardado refleja exactamente los estados de las materias y los créditos de libre elección que el estudiante guardó, incluido lo que la Universidad ya le reconoció. [I1, I2]
2. El estudiante ve, por componente y por agrupación (desplegables hasta sus subagrupaciones y materias), los créditos aprobados, en curso y pendientes, qué obligatorias, optativas y subagrupaciones le faltan, y los créditos de libre elección. [I1]
3. Si registró sus semestres cursados y su programa tiene la malla estándar registrada, el estudiante ve sus atrasos y adelantos respecto a ella (ver «Atraso, adelanto y cambio de alternativa»); los semestres cursados quedan guardados con el periodo en curso en que los registró. [I1, I6]
4. Las marcas quedan registradas contra la versión vigente del consolidado, el guardado registra el periodo en curso, y cada marca «en curso» registrada o modificada en este caso, incluidos los créditos de libre elección en curso, guarda ese periodo. [I6]
5. Las inconsistencias (prerrequisitos, marcas «en curso» por actualizar, materias que ya no rigen, excedentes de créditos) quedaron señaladas al estudiante sin impedirle guardar. [I2, I8]
6. Si el estudiante tiene plan vigente, sabe si el avance guardado lo dejó desactualizado y cómo actualizarlo, o en qué semestre de su plan está, o, si ya no queda nada por cubrir, que su plan figura como «plan cumplido». [I4]

## Disparador
El estudiante quiere dejar al día lo que ya aprobó o está cursando, o RutaUN le recordó hacerlo porque empezó un periodo nuevo, y selecciona «Registrar avance en la malla».

## Escenario principal de éxito
1. El estudiante selecciona «Registrar avance en la malla».
2. El sistema muestra la malla de su programa según las reglas vigentes en la versión vigente del consolidado —sus componentes y, dentro de cada uno, sus agrupaciones y subagrupaciones con sus materias obligatorias y optativas, cada nivel desplegable, las materias que muestra la malla estándar con su semestre sugerido, y la libre elección, con las electivas de sus agrupaciones si la norma las lista (solo de consulta; ver «Libre elección»)—, el estado actual de cada materia, los créditos aprobados, en curso y pendientes por componente y por agrupación y, si el estudiante registró sus semestres cursados, sus atrasos y adelantos respecto a la malla estándar. [I1, I5, I6, I8]
3. El estudiante marca una o más materias como aprobada, en curso o pendiente y, si el programa tiene libre elección, registra los créditos de libre elección aprobados y en curso; también puede registrar o actualizar sus semestres cursados (ver «Semestres cursados»). [I1, I2]
4. El sistema valida cada cambio (coherencia con los prerrequisitos, estado «en curso» permitido, créditos de libre elección y semestres cursados válidos) y avisa las inconsistencias sin impedir el registro, salvo los valores inválidos (extensiones 4a y 4d). [I2, I8]
5. El sistema recalcula y muestra de inmediato los créditos aprobados, en curso y pendientes por componente y por agrupación, lo que falta en cada una y, si corresponde, los atrasos y adelantos respecto a la malla estándar, sin guardar todavía. [I1]
6. El estudiante revisa el resultado y selecciona «Guardar avance». [I3]
7. El sistema verifica que el avance guardado no haya cambiado desde que se cargó, que la versión del consolidado siga siendo la misma y, si entre los cambios hay marcas «en curso» nuevas o modificadas o semestres cursados, que el periodo en curso siga siendo el mismo. [I3, I6]
8. El sistema guarda todos los cambios de forma atómica, con la fecha y el periodo en curso y asociados a la versión del consolidado, y guarda el periodo en curso en cada marca «en curso» nueva o modificada y, si cambiaron, en los semestres cursados; las demás marcas conservan el periodo que tenían. [I3, I5, I6]
9. El sistema confirma el guardado y muestra los créditos por componente. Si el estudiante tiene plan vigente: si el avance guardado lo deja desactualizado (atrasos, adelantos o cambios de alternativa respecto a él; ver «Plan desactualizado»), avisa y ofrece «Replanificar plan de carrera» (UC02) sin recalcularlo; si no, le indica en qué semestre de su plan está (ver «Semestre actual del plan»); y si ya no queda nada por cubrir, le informa que su plan figura como «plan cumplido» (ver «Plan desactualizado»). [I1, I4]

## Extensiones
- **2a** · *Condición:* El estudiante no ha registrado avance: todas las materias están pendientes y no hay créditos de libre elección registrados. → *Resolución:* El sistema muestra la malla completa en pendiente y una guía breve para empezar; el caso continúa en el paso 3. [I1]
- **2b** · *Condición:* El consolidado vigente no tiene una malla utilizable (ver «Malla utilizable»). → *Resolución:* El sistema no muestra la malla, informa que el consolidado normativo de su programa tiene un problema que ya se informó al administrador de normativa, y registra un incidente con el programa, la versión y la causa. El caso termina sin cambios. [I1, I7]
- **2c** · *Condición:* El avance guardado tiene marcas «en curso» por actualizar (de materias o de créditos de libre elección), registradas en un periodo anterior al periodo en curso. → *Resolución:* El sistema las destaca y pide al estudiante resolverlas cuando tenga sus notas: una materia, pasándola a aprobada o a pendiente; los créditos de libre elección, pasándolos a aprobados o quitándolos. Puede guardar otros cambios sin resolverlas; los totales las siguen mostrando como «en curso» y el cálculo de un plan las cuenta como aprobadas (supuesto del equipo). Los créditos de libre elección «en curso» son una cantidad de un solo periodo: mientras no resuelva los que están por actualizar, no puede registrar los del periodo en curso. El caso continúa en el paso 3. [I1, I2]
- **2d** · *Condición:* Los semestres cursados del estudiante se registraron en un periodo anterior al periodo en curso (ver «Semestres cursados»). → *Resolución:* El sistema muestra la comparación con la malla estándar con ese número, advierte en qué periodo se registró y que puede haber cambiado (por ejemplo, de 5 a 6 si se matriculó en el periodo en curso) y le pide confirmarlo o actualizarlo en el paso 3. El caso continúa en el paso 3. [I1]
- **2e** · *Condición:* Una materia marcada como aprobada o en curso rige en una **versión anterior del plan de estudios** y no figura en la versión vigente (fue derogada o modificada por una reforma) o ya no rige en la versión vigente (ver «Materia que ya no rige»). → *Resolución:* El sistema mantiene la marca y muestra la materia aparte, en «Materias que ya no rigen», con su estado y con cómo cuenta: como la materia a la que la hace equivaler una equivalencia vigente, con los créditos, el componente y la agrupación que tenía en su versión, o como **incierta** cuando ninguna equivalencia dice cómo traducirla (S18). El estudiante no tiene que hacer nada; puede cambiar su estado si la marcó por error. Si su agrupación o su componente tampoco rigen y ninguna equivalencia la ubica, el sistema la muestra sin contarla, avisa que el cálculo de un plan tampoco la contará y registra un incidente para que el administrador cargue la regla que corresponda. RutaUN no decide equivalencias por su cuenta. El caso continúa en el paso 3. [I1, I6, I7, I8]
- **2f** · *Condición:* No se puede comparar con la malla estándar: el programa no tiene completa la malla estándar o el estudiante no ha registrado sus semestres cursados. → *Resolución:* El sistema muestra la malla sin atrasos ni adelantos respecto a la malla estándar y dice por qué; si faltan los semestres cursados, invita a registrarlos en el paso 3. El caso continúa en el paso 3. [I1]
- **2g** · *Condición:* El periodo en curso no está registrado (ver «Periodo en curso»). → *Resolución:* El sistema no ofrece el estado «en curso» ni permite registrar semestres cursados, porque no tiene periodo que guardar con ellos; informa que el sistema aún no tiene registrado el periodo en curso y registra un incidente. El estudiante puede registrar materias aprobadas o pendientes y créditos de libre elección aprobados; el caso continúa en el paso 3. [I1, I7]
- **2h** · *Condición:* El administrador avanzó el periodo en curso después del último guardado del avance. → *Resolución:* El sistema le recuerda registrar lo que inscribió en el periodo en curso (marcarlo «en curso») y resolver las marcas por actualizar (extensión 2c), porque sin eso RutaUN no puede saber si su plan se está cumpliendo; si tiene plan vigente, le muestra lo que el plan ubicaba en el semestre actual (materias, espacios de optativa con sus opciones y bloque de libre elección), para que marque lo que inscribió. El caso continúa en el paso 3. [I1, I4]
- **3a** · *Condición:* El estudiante marca como aprobada o en curso una materia cuyos prerrequisitos figuran pendientes. → *Resolución:* El sistema avisa la inconsistencia (puede deberse a validaciones, homologaciones o convalidaciones, o a un error de digitación) y permite registrarla tal como el estudiante la declara; queda señalada. Si la materia es no cursable desde el programa (ver «Materia no cursable desde el programa»), no avisa nada: su prerrequisito exige una materia que RutaUN no muestra. [I2]
- **3b** · *Condición:* El estudiante pasa a pendiente una materia que es prerrequisito de otras marcadas como aprobadas o en curso. → *Resolución:* El sistema lo permite, señala qué materias quedan con el prerrequisito pendiente y no cambia esas marcas por su cuenta. [I2]
- **3c** · *Condición:* El estudiante marca optativas de una agrupación cuyos créditos ya están cubiertos, o supera los créditos exigidos de un componente. → *Resolución:* El sistema muestra el excedente y avisa que, según el supuesto del equipo (1), los créditos que exceden lo exigido a una agrupación no reducen lo exigido a otra agrupación, a la agrupación que la contiene ni a la libre elección. Permite registrarlo. [I1, I2, I8]
- **3d** · *Condición:* El estudiante marca como aprobada una materia que la Universidad le reconoció por validación, homologación o convalidación. → *Resolución:* Se registra como aprobada, igual que una cursada. RutaUN no verifica el reconocimiento ni el límite del artículo 38 del Acuerdo 008 de 2008 (50% del mínimo de créditos del plan de estudios; solo puede autorizarse más, hasta el 75%, cuando la reciprocidad de un convenio de doble titulación con una universidad extranjera lo exige). [I2, I8]
- **3e** · *Condición:* El estudiante cursó una materia que no figura en la malla (por ejemplo, una electiva de otro programa). → *Resolución:* Si la Universidad se la reconoció como una materia de la malla, el estudiante marca esa materia como aprobada (extensión 3d). Si la contó como libre elección, suma sus créditos a la libre elección. Si la contó dentro de una agrupación sin equivalencia con ninguna de sus optativas, el estudiante la registra como **materia reconocida** (S7): nombre, código si lo tiene, créditos y agrupación; los créditos suman a esa agrupación y la materia no cumple ningún prerrequisito ni correquisito. [I1, I2, I8]
- **3f** · *Condición:* El estudiante marca como aprobada o en curso una materia que figura en dos o más agrupaciones (ver «Agrupación asignada»). → *Resolución:* Si solo una de ellas no está cubierta, el sistema le asigna esa y lo indica junto a la materia. Si dos o más no lo están, le pide elegir entre ellas, mostrando cuántos créditos le faltan a cada una; mientras no elija, los totales no la cuentan en ninguna y no se puede guardar el avance. Si todas están cubiertas, no le asigna ninguna: la muestra como excedente (extensión 3c) y avisa que RutaUN no sabe si la Universidad la cuenta como libre elección o de otro modo (supuesto del equipo). La asignación se guarda con la marca en el paso 8 y no cambia sola después. [I1, I2]
- **4a** · *Condición:* Los créditos de libre elección son inválidos: negativos, decimales o no numéricos. → *Resolución:* El sistema señala el campo y pide un entero mayor o igual a 0; no toma ese valor, el resto del registro no se pierde y el caso vuelve al paso 3. [I1, I3]
- **4b** · *Condición:* Los créditos de libre elección registrados (aprobados más en curso) superan los que exige el componente. → *Resolución:* El sistema muestra el excedente y avisa que para el plan solo se cuentan los créditos exigidos; permite registrarlo tal cual. [I1, I2]
- **4c** · *Condición:* Un prerrequisito de la materia marcada es circular o apunta a una materia que no rige. → *Resolución:* El sistema no evalúa esa coherencia, permite registrar la marca y registra un incidente con las materias involucradas. [I2, I7]
- **4d** · *Condición:* Los semestres cursados son inválidos: cero, negativos, decimales o no numéricos. → *Resolución:* El sistema señala el campo y pide un entero mayor o igual a 1, o dejarlo vacío; no toma ese valor, el resto del registro no se pierde y el caso vuelve al paso 3. [I1, I3]
- **6a** · *Condición:* El estudiante selecciona «Guardar avance» sin haber cambiado nada. → *Resolución:* El sistema informa que no hay cambios y no guarda nada; el plan vigente no cambia de estado por este caso. El caso termina sin cambios. [I3, I4]
- **7a** · *Condición:* Otra sesión modificó el avance guardado desde que el estudiante lo cargó. → *Resolución:* El sistema no guarda, muestra las diferencias y ofrece recargar el avance actual; si el estudiante acepta, el sistema recarga el avance y muestra aparte sus cambios sin guardar, para que decida cuáles vuelve a aplicar (el caso vuelve al paso 2); si no acepta, el caso termina sin cambios. [I3]
- **7b** · *Condición:* La versión vigente del consolidado cambió desde que el estudiante cargó la malla. → *Resolución:* El sistema no guarda, recarga la malla con la versión vigente, conserva los cambios del estudiante sobre materias que sigan rigiendo, descarta y señala los que recaen en materias que ya no rigen, señala las materias marcadas que dejaron de regir (extensión 2e) y el caso vuelve al paso 4, que valida de nuevo los cambios con la versión nueva. [I3, I6]
- **7c** · *Condición:* Entre la carga y el guardado el administrador avanzó el periodo en curso y entre los cambios hay marcas «en curso» nuevas o modificadas o semestres cursados. → *Resolución:* El sistema no guarda, señala esas marcas «en curso» (de materias o de créditos de libre elección) y pide al estudiante volver a marcarlas: como aprobadas, como pendientes (los créditos de libre elección, quitándolos) o como «en curso» en el periodo en curso nuevo; y le pide confirmar sus semestres cursados. Las demás marcas y cambios se conservan, el sistema toma como periodo en curso de referencia el actual (el que el paso 8 guardará) y el caso vuelve al paso 3. [I3, I6]
- **8a** · *Condición:* Falla el guardado (error de persistencia). → *Resolución:* Como el guardado es atómico, el avance guardado queda como estaba. El sistema informa el fallo, conserva los cambios en pantalla mientras dure la sesión para reintentar el paso 6, y registra un incidente. [I3, I7]
- **9a** · *Condición:* El estudiante no tiene plan vigente. → *Resolución:* El sistema no muestra nada sobre el plan y ofrece «Generar plan de carrera» (UC01). [I4]
- **9b** · *Condición:* El avance guardado no deja desactualizado el plan vigente (por ejemplo, el estudiante marcó en curso lo que el plan ubicaba en el semestre actual, o aprobado lo que ubicaba en un semestre ya terminado). → *Resolución:* El sistema no muestra el aviso de plan desactualizado: le indica en qué semestre de su plan está. [I4]
- **9c** · *Condición:* La sesión expira, el estudiante abandona la aplicación u ocurre un error interno después de que el avance se guardó y antes de ver la confirmación. → *Resolución:* El avance ya quedó guardado; el estudiante lo encuentra al volver a entrar. No se requiere ninguna acción; si fue un error interno, el sistema registra un incidente con el paso y la causa. [I3, I7]
- **Cualquiera** · *Condición:* El estudiante abandona el caso o la sesión expira antes de que el avance se guarde. → *Resolución:* No se guarda nada: lo calculado o editado hasta ese momento se descarta y los datos guardados quedan como estaban. [I3, I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso anterior al guardado (el fallo del propio guardado lo trata la extensión 8a; si el error ocurre después, lo guardado ya quedó guardado, como en la extensión 9c). → *Resolución:* El sistema no modifica los datos guardados, informa que ocurrió un error y que se puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I3, I7]

---

---

## UC03 · Registrar avance en la malla (9 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC03 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC03-HU01 (#13) · Ver mi malla con el estado de cada materia
**Como** estudiante, **quiero** ver la malla de mi programa con el estado de cada materia y los créditos por componente y por agrupación, **para** saber qué me falta sin rayar el PDF de la malla.
Criterios de aceptación:
- [ ] La malla se muestra por componente, desplegable hasta sus agrupaciones, subagrupaciones y materias, con los créditos aprobados, en curso y pendientes y las obligatorias, optativas y subagrupaciones que faltan (GE 2).
- [ ] Si no he registrado avance, el sistema muestra toda la malla en pendiente y una guía breve para empezar (ext. 2a).
- [ ] Si el consolidado no tiene una malla utilizable, el sistema no la muestra, dice que el problema ya se informó al administrador y registra un incidente (ext. 2b, GM 7).
- [ ] Las electivas que la norma lista en una agrupación de libre elección (como Profundización) se muestran solo de consulta; sus créditos los registro como créditos de libre elección (paso 2).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU02 (#14) · Marcar materias aprobadas, en curso o pendientes
**Como** estudiante, **quiero** marcar materias como aprobadas, en curso o pendientes y ver los totales recalculados al instante, **para** reflejar mi situación real, incluido lo que la Universidad me reconoció.
Criterios de aceptación:
- [ ] Los totales por componente y por agrupación se recalculan de inmediato, sin guardar nada hasta que seleccione «Guardar avance» (paso 5, GM 1).
- [ ] Si marco una materia con prerrequisitos pendientes, o paso a pendiente un prerrequisito de materias aprobadas, el sistema avisa la inconsistencia y me deja registrarla; no cambia otras marcas por su cuenta (ext. 3a, 3b).
- [ ] Una materia validada, homologada o convalidada se registra como aprobada, sin que el sistema verifique el reconocimiento (ext. 3d, GM 4).
- [ ] Si marco optativas que exceden lo que exige una agrupación o un componente, el sistema muestra el excedente y me deja registrarlo (ext. 3c).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU03 (#15) · Registrar créditos de libre elección
**Como** estudiante, **quiero** registrar los créditos de libre elección que tengo aprobados y en curso, **para** que el sistema sepa cuánta libre elección me falta.
Criterios de aceptación:
- [ ] El sistema solo acepta un entero mayor o igual a 0; si el valor es inválido, señala el campo sin perder el resto del registro (ext. 4a).
- [ ] Si los créditos registrados superan lo que exige el componente, el sistema muestra el excedente y avisa que para el plan solo se cuentan los exigidos (ext. 4b).
- [ ] Mientras tenga créditos de libre elección «en curso» por actualizar, no puedo registrar los del periodo en curso (ext. 2c).
- [ ] Si cursé una materia que no está en la malla y la Universidad la contó como libre elección, puedo sumar sus créditos a la libre elección (ext. 3e).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU04 (#16) · Registrar semestres cursados y compararme con la malla estándar
**Como** estudiante, **quiero** registrar mis semestres cursados y ver mis atrasos y adelantos respecto a la malla estándar, **para** saber dónde estoy frente a lo que sugiere la Universidad.
Criterios de aceptación:
- [ ] Con semestres cursados registrados y la malla estándar del programa disponible, el sistema muestra mis atrasos y adelantos respecto a ella (GE 3).
- [ ] El sistema solo acepta un entero mayor o igual a 1, o el campo vacío; un valor inválido se señala sin perder el resto del registro (ext. 4d).
- [ ] Si mis semestres cursados se registraron en un periodo anterior, el sistema lo advierte y me pide confirmarlos o actualizarlos (ext. 2d).
- [ ] Si falta la malla estándar o mis semestres cursados, la malla se muestra sin atrasos ni adelantos y el sistema dice por qué (ext. 2f).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU05 (#17) · Guardar el avance sin pisar otros cambios
**Como** estudiante, **quiero** guardar mi avance de forma completa y sin sobrescribir cambios hechos desde otra sesión, **para** no terminar con un avance a medias o incorrecto sin notarlo.
Criterios de aceptación:
- [ ] Los cambios se guardan todos o ninguno, con la fecha, el periodo en curso y la versión del consolidado; si el guardado falla, el avance queda como estaba, mis cambios siguen en pantalla y se registra un incidente (GM 2, GE 4, ext. 8a).
- [ ] Si otra sesión modificó el avance, el sistema no guarda, muestra las diferencias y ofrece recargar mostrando aparte mis cambios sin guardar (GM 3, ext. 7a).
- [ ] Si cambió la versión del consolidado, el sistema recarga la malla, conserva mis cambios sobre materias que siguen rigiendo y señala los que ya no aplican (ext. 7b).
- [ ] Si selecciono «Guardar avance» sin haber cambiado nada, el sistema lo informa y no guarda (ext. 6a).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU06 (#18) · Saber cómo queda mi plan vigente después de guardar
**Como** estudiante, **quiero** saber, al guardar mi avance, si mi plan vigente quedó desactualizado o en qué semestre de él estoy, **para** decidir si replanifico sin perder el plan que tengo.
Criterios de aceptación:
- [ ] Si el avance guardado deja desactualizado el plan vigente, el sistema lo avisa y ofrece «Replanificar plan de carrera» (UC02), sin recalcularlo (paso 9, GM 5).
- [ ] Si el plan sigue al día, el sistema me indica en qué semestre del plan estoy (ext. 9b).
- [ ] Si ya no queda nada por cubrir, el sistema informa que el plan figura como «plan cumplido» (GE 6).
- [ ] Si no tengo plan vigente, el sistema no muestra nada del plan y me ofrece «Generar plan de carrera» (UC01) (ext. 9a).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU07 (#19) · Resolver marcas «en curso» al empezar un periodo nuevo
**Como** estudiante, **quiero** que, al empezar un periodo nuevo, el sistema me recuerde resolver mis marcas «en curso» anteriores y registrar lo que inscribí, **para** que RutaUN sepa si estoy cumpliendo mi plan.
Criterios de aceptación:
- [ ] El sistema destaca las marcas «en curso» por actualizar y me pide resolverlas: una materia, pasándola a aprobada o a pendiente; los créditos de libre elección, pasándolos a aprobados o quitándolos (ext. 2c).
- [ ] Puedo guardar otros cambios sin resolverlas; mientras tanto, los totales las muestran «en curso» (ext. 2c).
- [ ] Si tengo plan vigente, el sistema me muestra lo que el plan ubicaba en el semestre actual para que marque lo que inscribí (ext. 2h).
- [ ] Si el periodo en curso cambió entre la carga y el guardado, el sistema no guarda las marcas «en curso» afectadas y me pide volver a marcarlas (ext. 7c).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU08 (#20) · Ver aparte las materias que ya no rigen
**Como** estudiante, **quiero** ver aparte las materias que tengo aprobadas o en curso y que ya no rigen, con cómo cuentan, **para** no perder lo que la Universidad ya me reconoció cuando cambia la malla.
Criterios de aceptación:
- [ ] El sistema muestra esas materias en «Materias que ya no rigen», con su estado y cómo cuentan: como la materia de una equivalencia vigente o con los créditos, el componente y la agrupación de la última versión en que rigieron (ext. 2e).
- [ ] No tengo que hacer nada para que sigan contando; solo puedo cambiar su estado si las marqué por error (ext. 2e).
- [ ] Si ninguna regla permite contarlas, el sistema las muestra sin contarlas, avisa que el plan tampoco las contará y registra un incidente para el administrador (ext. 2e).

Caso de uso relacionado: UC03 · Registrar avance en la malla

### UC03-HU09 (#21) · Asignar a una agrupación una materia que figura en varias
**Como** estudiante, **quiero** que una materia que figura en varias agrupaciones cuente para la que todavía me falta cubrir y, si me faltan varias, elegir yo cuál, **para** que sus créditos se cuenten donde los necesito.
Criterios de aceptación:
- [ ] Si solo una de sus agrupaciones no está cubierta, el sistema le asigna esa y lo indica junto a la materia (ext. 3f).
- [ ] Si dos o más no están cubiertas, el sistema me pide elegir, mostrando cuántos créditos le faltan a cada una, y no guarda el avance mientras no elija (ext. 3f).
- [ ] Si todas están cubiertas, la materia figura como excedente, sin agrupación asignada, con el aviso de que RutaUN no sabe si la Universidad la cuenta como libre elección o de otro modo (ext. 3f, ext. 3c).
- [ ] La asignación se guarda con la marca y no cambia sola después, aunque cambie la cobertura de las agrupaciones (paso 8, ext. 3f).

Caso de uso relacionado: UC03 · Registrar avance en la malla

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #13–#21 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
