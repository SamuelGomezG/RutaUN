---
title: "UC07 · Incorporar acuerdo al consolidado del programa"
doc_type: "caso-de-uso"
srs_ids:
  - UC07
  - UC07-HU01..HU09
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
# UC07 · Incorporar acuerdo al consolidado del programa

> **Cuándo cargar:** para implementar o revisar **UC07**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Administrador de normativa · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Administrador de normativa** — *Quiere:* incorporar una norma al consolidado de uno o varios programas, con su fuente, su fecha de aplicación, su vigencia y su alcance —verificado en el texto de la norma— y, si es el acuerdo del plan de estudios, con la malla estándar del programa, de modo que sus reglas queden estructuradas, trazables y sin contradicciones. *Por qué:* es quien mantiene el consolidado (en la primera versión, el equipo Zeus, según el Actor-Goal List); de esa carga depende que todos los planes y explicaciones sean correctos. Las normas reales lo exigen: la Resolución 057 y la Circular 11 de 2026 son normas temporales (una excepción y una medida temporal) con alcance propio, no reglas permanentes del programa; y la granularidad del alcance varía de una norma a otra: un Consejo de Facultad puede expedir una norma para un solo programa de su facultad.
- **I2 · Administrador de normativa** — *Quiere:* ver antes de confirmar qué cambiará en cada programa (reglas nuevas, modificadas, derogadas o vencidas, desde qué fecha, cuántos planes vigentes quedarían desactualizados porque cambia una regla que aplican y cuántos avances tendrían materias que dejan de regir) y que la incorporación sea todo o nada, conservando las versiones anteriores. *Por qué:* una versión equivocada del consolidado afectaría a todos los estudiantes del programa, y deshacerla no corrige los planes que ya se calcularon con ella.
- **I3 · Administrador de normativa** — *Quiere:* que cada regla rija solo en su vigencia (fecha de aplicación, «vigencia hasta», normas programadas y temporales) y que el sistema lo haga cumplir cuando él avance el periodo en curso o llegue la fecha, sin tener que volver a incorporar la norma. *Por qué:* las normas reales tienen vigencias específicas: el Acuerdo 008 de 2008 rige «a partir del primer período académico de 2009» (art. 61); la Resolución 057 de 2026 exceptúa el artículo 10 del Acuerdo 008 de 2008 solo durante el segundo periodo académico de 2026 (art. 1) y rige desde su publicación en Régimen Legal (art. 3); y la Circular 11 de 2026 aplica su medida solo al periodo 2026-2 y rige desde su publicación en Régimen Legal.
- **I4 · Estudiante** — *Quiere:* que lo que RutaUN le muestra y calcula refleje las normas vigentes de su programa, que la malla estándar con la que se comparan su avance y sus planes sea la que publica la Universidad y que, al abrir su plan, vea que quedó desactualizado si una norma nueva cambia una regla que el plan aplica, sin que RutaUN lo cambie por su cuenta. *Por qué:* decide su carrera con esas reglas; un cambio silencioso rompería un plan en el que confiaba, y una norma que RutaUN no conoce lo haría planear con reglas viejas.
- **I5 · Estudiante** — *Quiere:* que ninguna norma que no le aplica, por su programa o por su periodo, afecte su plan ni su consulta. *Por qué:* las normas tienen alcance: la Circular 11 de 2026, por ejemplo, exime a los programas de Medicina y de Medicina Veterinaria; aplicarla a quien no corresponde produciría planes incorrectos.
- **I6 · Estudiante** — *Quiere:* que el administrador vea solo cantidades de planes y de avances afectados, sin datos que identifiquen a los estudiantes ni el contenido de sus avances o sus planes. *Por qué:* su avance y sus planes son datos personales protegidos por la Ley 1581 de 2012, y para decidir la incorporación al administrador le basta saber cuántos se afectan.
- **I7 · Órganos y Régimen Legal** — *Quiere:* que cada norma se incorpore tal como fue expedida y publicada (identificación, órgano, artículos, fecha de aplicación, vigencia, alcance y, si existe, enlace a Régimen Legal), sin reinterpretarla. *Por qué:* la norma publicada en Régimen Legal es la fuente oficial y cada órgano expide la suya; RutaUN solo la consolida, y una incorporación infiel llevaría a los estudiantes a decidir con una versión distorsionada de lo que ellos expidieron.

## Precondiciones
1. El administrador ha iniciado sesión con su cuenta de RutaUN.
2. El administrador tiene a la mano el texto oficial de la norma (expedido por el órgano o publicado en Régimen Legal) y, si carga las reglas por archivo, el archivo JSON con ellas (S8).
3. Hay al menos un programa cargado en el sistema.
4. El administrador puede registrar todas las reglas de la norma en la misma sesión: RutaUN no guarda incorporaciones a medias (supuesto del equipo).

## Garantía mínima (GM)
1. Ningún consolidado cambia hasta que el administrador confirme la incorporación: validar o ver la vista previa no modifica nada. Antes de confirmar solo se registran la reserva de los programas del alcance y de la identificación de la norma (paso 4) y los incidentes de las extensiones 6g y 7c o de un error interno. La reserva es de esta incorporación, no del administrador, y se libera cuando el caso termina, por cualquier salida (se guarde, termine sin cambios, se abandone, expire la sesión u ocurra un error interno). [I2]
2. La incorporación es todo o nada: si falla para un programa del alcance, no se incorpora en ninguno. [I2]
3. Las versiones anteriores del consolidado se conservan: una incorporación nunca borra historia. [I2, I3]
4. Ninguna regla queda sin fuente: toda regla cita su norma y su artículo o numeral (ver «Fuente») y queda con la fecha de aplicación de su norma. [I1, I7]
5. Los planes y los avances de los estudiantes no se modifican ni se recalculan por incorporar una norma: un plan figura como desactualizado porque ese estado se calcula al mostrarlo (ver «Plan desactualizado»). [I4]
6. Solo un administrador de normativa puede incorporar normas. [I1, I4, I7]
7. Si algo falla, el administrador recibe un mensaje que dice qué falló y qué corregir; si el fallo es un error interno, queda además un incidente. [I1]
8. El administrador solo ve cantidades de planes y de avances afectados: nunca datos que identifiquen a un estudiante ni el contenido de sus avances o sus planes. [I6]

## Garantía de éxito (GE)
1. La norma queda incorporada con su identificación completa, órgano, tipo de efecto, fecha de aplicación, vigencia, alcance y, si existe, el enlace a Régimen Legal. Lo cargado por archivo JSON no queda guardado como archivo: lo que se incorpora son las reglas. [I1, I7]
2. Cada programa del alcance tiene una nueva versión del consolidado con las reglas nuevas, modificadas, derogadas o vencidas, cada una con su fuente y clasificada como regla del plan o informativa, y con su fecha de actualización. [I1, I2]
3. Las reglas de la norma rigen solo en su vigencia: cada una figura «vigente», «programada» o «ya no rige» y, cuando una programada empieza a regir o una temporal deja de regir (al avanzar el periodo en curso o al llegar una fecha), el sistema crea una versión nueva sin que el administrador tenga que volver a incorporar la norma. [I3]
4. Las reglas de la norma solo aplican a los programas de su alcance y a los periodos de su vigencia. [I3, I5]
5. Si la norma define materias, agrupaciones o la libre elección de un programa, la malla estándar queda registrada con sus reglas o la vista previa advirtió qué le falta (extensión 7d). [I1, I4]
6. Los planes calculados con una versión anterior que aplican una regla que la norma cambió figuran como desactualizados desde que la nueva versión es la vigente, sin recalcularse ni cambiarse: solo el estudiante los replanifica («Replanificar plan de carrera», UC02). [I4]
7. El administrador vio la vista previa antes de confirmar, sabe qué versión se creó en cada programa y desde cuándo rige cada regla nueva, y el historial del consolidado registra quién incorporó la norma y cuándo. [I1, I2]

## Disparador
Se expidió una norma (acuerdo, resolución o circular) que afecta a uno o más programas, hay que cargar una norma vigente que aún no está en RutaUN (por ejemplo, la norma base de un programa nuevo) o hay que incorporar a un programa una norma ya registrada que el sistema le lista como pendiente (ver «Programa»), y el administrador selecciona «Incorporar acuerdo al consolidado del programa».

## Escenario principal de éxito
1. El administrador selecciona «Incorporar acuerdo al consolidado del programa».
2. El sistema solicita los datos de la norma y su alcance. [I1]
3. El administrador registra la identificación de la norma —tipo (acuerdo, resolución o circular), número, año, órgano que la expide, tipo de efecto (base; modificatoria; o excepción o medida temporal), fecha de expedición, fecha de aplicación (una fecha o un periodo), «vigencia hasta» (una fecha o un periodo; obligatoria en las excepciones y medidas temporales) y, si existe, el enlace a Régimen Legal— y su alcance: su tipo (todos los programas de pregrado de la Sede, los de una o varias facultades, o uno o varios programas), con las exclusiones que la norma establezca, el texto con que la norma lo fija y, entre los programas cargados que ese alcance comprende, aquellos en los que la incorpora ahora (los programas del alcance; puede dejar otros para incorporarla después). [I1, I3, I7]
4. El sistema busca primero la norma por su tipo, número, año y órgano: si ya está registrada, se atiende la extensión 4c y los demás datos digitados no se validan (las extensiones 4a, 4b, 4f, 4g y 4h no aplican); si no, valida los datos de identificación, la vigencia y el alcance (incluido que los programas elegidos estén comprendidos en él) y reserva, todos a la vez, los programas del alcance y la identificación de la norma, para que ninguna otra incorporación (de otro administrador o de otra sesión) incorpore una norma en esos programas ni registre la misma norma mientras dure este caso. [I1, I2, I3, I5, I7]
5. El administrador registra las reglas de la norma, digitadas o cargadas desde un **archivo JSON** en la misma sesión (S8): de cada una, su categoría (ver «Regla»), su enunciado, sus datos según esa categoría (ver «Datos de una regla»; en las de materia, de agrupación, de correquisito y de libre elección, el componente o la agrupación que la contiene), el artículo o numeral (tal como lo rotula la norma; ver «Fuente»), su fecha de aplicación y su «vigencia hasta» si la norma le fija una vigencia propia (ver «Vigencia»), si es regla del plan o informativa (según el criterio de «Reglas del plan y reglas informativas») y, en cualquier norma, la regla que modifica, deroga o exceptúa (de otra norma o de la misma, como en un régimen de transición), identificada por su norma, su artículo o numeral y su objeto, o todas las reglas de un artículo o numeral si la norma lo sustituye completo, y el periodo o la fecha desde la que lo hace, si los hay. En las reglas de materia, de agrupación y de libre elección registra además los datos de la malla estándar (ver «Malla estándar»). Un mismo artículo o numeral suele dar lugar a muchas reglas (ver «Regla»): una regla de materia por cada agrupación en que la norma ubica la materia, una de prerrequisito, una de habilitación, una por cada agrupación; y, en los prerrequisitos, las materias que no figuran en la malla se registran como externas (ver «Materia externa»). [I1, I4, I7]
6. El sistema valida las reglas (entre ellas, los correquisitos): que cada una tenga su enunciado, sus datos completos y su artículo o numeral, y que, junto con el consolidado de cada programa del alcance, el consolidado resultante no tenga reglas que se contradigan y cumpla las condiciones 1 a 5 de «Consolidado calculable», con las reglas vigentes y también como quedaría cada vez que empiece a regir una regla programada o deje de regir una temporal. [I1, I2, I3]
7. El sistema construye la versión resultante de cada programa y muestra la vista previa, solo con cantidades: reglas nuevas, modificadas, derogadas o vencidas, el periodo o la fecha desde la que rigen, cuántos planes vigentes que hoy no figuran desactualizados ni como «plan cumplido» pasarían a figurar desactualizados porque cambia una regla que aplican (ver «Plan desactualizado»), y cuántos avances tendrían materias que dejarían de regir (ver «Materia que ya no rige») y, de ellos, cuántos con alguna que no se podría contar. [I2, I3, I4, I6]
8. El administrador revisa la vista previa y confirma la incorporación. [I2]
9. El sistema verifica que la versión vigente de cada programa del alcance no haya cambiado desde la vista previa; guarda, de forma atómica y para todos los programas del alcance, la norma (si no estaba registrada antes de este caso), sus reglas y las nuevas versiones del consolidado con su fecha de actualización y el administrador que la incorporó, en el historial del consolidado, conservando las versiones anteriores; y libera las reservas. [I2, I3, I7]
10. El sistema confirma la incorporación e informa la versión creada en cada programa y el estado de cada regla nueva (vigente o programada) con el periodo o la fecha en que empieza a regir. [I1, I3]

## Extensiones
- **1a** · *Condición:* El usuario no tiene el rol de administrador de normativa. → *Resolución:* El sistema no permite el acceso al caso y no muestra datos del consolidado. [I4, I7]
- **4a** · *Condición:* Faltan datos obligatorios o son inválidos: número o año ausentes o no numéricos, formato incorrecto de una fecha o de un periodo (un periodo se escribe AAAA-1 o AAAA-2), o algún programa elegido no está comprendido en el alcance registrado. → *Resolución:* El sistema señala cada campo y pide corregirlo; el caso vuelve al paso 3. [I1, I5, I7]
- **4b** · *Condición:* Una excepción o medida temporal no tiene «vigencia hasta», o la «vigencia hasta» de la norma (sea cual sea su tipo de efecto) es anterior a su fecha de aplicación (las dos son periodos, o las dos son fechas). → *Resolución:* El sistema exige una vigencia coherente (un periodo o una fecha) para las excepciones y medidas temporales, y, en cualquier norma, una «vigencia hasta» posterior a la fecha de aplicación; el caso vuelve al paso 3. Si una es un periodo y la otra una fecha, el sistema no puede compararlas, porque no guarda las fechas de los periodos, y lo advierte para que el administrador las revise. [I3]
- **4c** · *Condición:* La norma ya está registrada en RutaUN (mismo tipo, número, año y órgano) porque se incorporó antes en otros programas. → *Resolución:* El sistema informa que la norma ya existe, carga su registro (identificación, alcance, enlace a Régimen Legal y reglas) y lo usa en lugar de los datos digitados: lo ya registrado no se cambia en este caso de uso (un error de la norma se corrige incorporando una norma que la modifique; uno de digitación o un enlace, como dice «Administrador de normativa») y solo se eligen, entre los programas cargados que su alcance comprende, aquellos en los que se incorpora ahora. Quita del alcance de esta incorporación los programas en que ya está incorporada, e informa en cuáles; si la norma ya está incorporada en todos los programas del alcance, no incorpora nada y el caso termina sin cambios. Si no, reserva los programas que quedan, como en el paso 4 (si alguno está reservado por otra incorporación, se atiende la extensión 4d), y el caso continúa en el paso 6 con las reglas ya registradas, sin volver a digitarlas (paso 5). Si la validación del paso 6 rechaza la norma para algún programa (extensiones 6b a 6e), el caso no vuelve al paso 5, porque esas reglas no se pueden cambiar: el administrador quita ese programa del alcance (el caso vuelve al paso 3) o termina el caso sin cambios, por ejemplo para incorporar antes a ese programa la norma a la que remite la regla. [I1, I2, I7]
- **4d** · *Condición:* Algún programa del alcance, o la identificación de la norma, está reservado por otra incorporación en curso (de otro administrador o de otra sesión del mismo). → *Resolución:* El sistema no reserva ningún programa nuevo (la reserva de los programas que faltan es de todos o de ninguno), informa qué está reservado y no permite avanzar hasta que esa otra incorporación termine, por cualquier salida. El administrador puede esperar y repetir el paso 4 (si lo reservado es la identificación de la norma, solo puede esperar: cuando la otra incorporación termine, si guardó la norma se atenderá la extensión 4c) o, si lo reservado es un programa, reducir el alcance (el caso vuelve al paso 3). Los programas que este caso ya tenía reservados (extensión 8b) se conservan mientras espera. [I2]
- **4e** · *Condición:* Ningún programa cargado queda comprendido en el alcance (por las exclusiones o porque los programas que la norma comprende no están cargados). → *Resolución:* El sistema no incorpora la norma y pide corregir el alcance (el caso vuelve al paso 3); los programas que la norma comprende y que no están cargados se informan y quedan fuera. [I1, I5]
- **4f** · *Condición:* La norma dice que rige desde su publicación y todavía no se ha publicado, por lo que no existe una fecha de aplicación verificable. → *Resolución:* El sistema no permite incorporarla hasta que exista una fecha de aplicación con su fuente (por ejemplo, la Resolución 057 de 2026 rige desde su publicación en Régimen Legal, art. 3); el caso termina sin cambios y el administrador puede volver a incorporarla cuando se publique. [I1, I3, I7]
- **4g** · *Condición:* La fecha de aplicación es una fecha anterior a la fecha de expedición de la norma (por ejemplo, una norma expedida el 15 de febrero que dice regir desde el 8 de febrero). → *Resolución:* El sistema advierte que la norma pide regir antes de su expedición y pide confirmar que esa es la fecha de aplicación que la norma establece: si el administrador confirma, la incorpora tal como fue expedida y el caso continúa en el paso 5; si no, el caso vuelve al paso 3 para corregir la fecha. [I1, I7]
- **4h** · *Condición:* El administrador indica que la norma no dice a qué programas aplica. → *Resolución:* El sistema propone como alcance el de la competencia del órgano que la expide (supuesto del equipo; ver «Alcance»): todos los programas de pregrado de la Sede, si es un órgano del nivel nacional o de la Sede; los de su facultad, si es un Consejo de Facultad. El administrador lo confirma, después de verificar en el texto que la norma no fija otro (un Consejo de Facultad, por ejemplo, puede expedir una norma para un solo programa), o lo corrige; el caso vuelve al paso 3 con el alcance propuesto. [I1, I5, I7]
- **5a** · *Condición:* Todas las reglas de la norma son informativas (ninguna cambia lo que el plan exige), como ocurre con toda norma temporal, por ejemplo la Resolución 057 y la Circular 11 de 2026. → *Resolución:* Se acepta: la norma se registra con sus reglas informativas, su vigencia y su alcance (para la Circular 11: «vigencia hasta» 2026-2; los estudiantes de pregrado, con los programas de Medicina y de Medicina Veterinaria exentos), y con un enunciado que dice a qué parte del periodo se refiere cada regla. Quedan visibles en «Consultar regla del programa y su acuerdo» (UC06) y junto a los planes, pero no cambian el cálculo; como no cambian reglas que los planes aplican, ningún plan vigente queda desactualizado por ella, y la vista previa (paso 7) lo informa. [I1, I3, I4, I5, I7]
- **6a** · *Condición:* Una regla tiene datos incompletos o inválidos según su categoría (ver «Datos de una regla»), o no tiene enunciado, o no tiene artículo o numeral. → *Resolución:* El sistema no la acepta e indica qué falta: toda regla necesita su enunciado, sus datos y su artículo o numeral de la norma (ver «Fuente»); el caso vuelve al paso 5. [I1, I7]
- **6b** · *Condición:* Una regla que modifica, deroga o exceptúa a otra remite a una regla que no existe en el consolidado resultante de un programa del alcance (o, si remite a todas las reglas de un artículo o numeral, ese artículo no tiene reglas en él), o cuya vigencia ya terminó (una regla aún programada sí puede modificarse). → *Resolución:* El sistema no la acepta e informa cuál regla no encontró y en qué programa. Si la remisión está mal digitada, el caso vuelve al paso 5 para corregirla (salvo que la norma ya estuviera registrada; extensión 4c). Si al programa le falta la norma que contiene la regla remitida, el sistema lo dice y el administrador quita ese programa del alcance de esta incorporación (el caso vuelve al paso 3) para incorporarle primero esa norma y después esta (extensión 4c). [I1, I2]
- **6c** · *Condición:* Una regla contradice otra regla, vigente o programada, cuya vigencia se solapa con la suya, sin que la norma la modifique, la derogue ni la exceptúe (dos reglas sobre el mismo objeto con valores distintos; ver «Regla»). → *Resolución:* El sistema informa el conflicto citando las dos fuentes; el administrador indica cuál regla se modifica, se deroga o se exceptúa, si la norma lo establece, o corrige un error de digitación (el caso vuelve al paso 5, salvo que la norma ya estuviera registrada; extensión 4c). Si las normas se contradicen de verdad, no se incorpora la norma hasta aclararlo con el órgano que la expidió (el caso termina sin cambios). [I1, I2, I3, I7]
- **6d** · *Condición:* La malla resultante no es utilizable (ver «Malla utilizable») o las cuentas de créditos no cierran: en algún componente o agrupación, sus obligatorias y los créditos exigidos de las agrupaciones que contiene suman más de lo que exige, o sus optativas no alcanzan para lo que falta; los créditos exigidos del programa no coinciden con la suma de sus componentes; o el desglose en créditos obligatorios y optativos que fija la norma no coincide con los créditos de las obligatorias registradas. También si el catálogo es incoherente: una misma materia (mismo código) registrada con nombres o créditos distintos, o dos veces en la misma agrupación —una norma sí puede ubicarla en varias agrupaciones (ver «Materia»), pero no con datos distintos—, una agrupación que lista una materia de otro componente, o una materia con más créditos que la carga máxima por periodo que fije una regla del plan. → *Resolución:* El sistema muestra qué falta en la malla o la cuenta que no cierra, con los créditos de cada parte, y no permite continuar; el caso vuelve al paso 5 (salvo que la norma ya estuviera registrada; extensión 4c), o al paso 3 si la causa es que al programa todavía no se le incorporó el acuerdo de su plan de estudios. Si la causa es que la norma deroga el plan de estudios del programa, el sistema informa que eso queda fuera de alcance (supuesto del equipo) y el caso vuelve al paso 3 para quitar ese programa del alcance. [I1, I2]
- **6e** · *Condición:* Un prerrequisito apunta a una materia del programa que no rige en alguno de los momentos que valida el paso 6, o a una materia que no figura en la malla y que el administrador no registró como externa (ver «Materia externa»), o forma un ciclo. → *Resolución:* El sistema no acepta la regla, indica las materias involucradas y, si alguna no figura en la malla, pide registrarla como externa (de nivelación o de otro programa) o corregir su nombre o su código; el caso vuelve al paso 5 (salvo que la norma ya estuviera registrada; extensión 4c). [I1, I2]
- **6f** · *Condición:* Una regla es temporal (su norma tiene «vigencia hasta» o el administrador le registró una propia) y el administrador la clasificó como regla del plan. → *Resolución:* El sistema no la acepta: el plan supone vigentes en todos sus semestres las reglas del plan que rigen al calcularlo, por lo que las reglas temporales solo pueden ser informativas (ver «Reglas del plan y reglas informativas»). Indica cuáles reglas reclasificar y el caso vuelve al paso 5. [I1, I5]
- **6g** · *Condición:* La validación de las reglas no termina en sesenta segundos (supuesto del equipo). → *Resolución:* El sistema la cancela, no guarda nada, informa al administrador y registra un incidente con el paso, los programas y la causa; el administrador puede reintentar (el caso vuelve al paso 6) o reducir el alcance (el caso vuelve al paso 3). [I1, I2]
- **7a** · *Condición:* La norma está programada: su fecha de aplicación es un periodo posterior al periodo en curso o una fecha futura. → *Resolución:* La vista previa muestra las reglas nuevas como «programadas» (ver «Vigencia»), con su periodo o su fecha, y advierte que mientras tanto rigen las reglas actuales, que el cálculo de los planes no las aplica hasta que rijan y que los planes las avisan en el semestre en que empiezan a regir. Al confirmar se crea la versión nueva, que ya las contiene; cuando el administrador avance el periodo en curso a ese periodo, o al llegar la fecha, el sistema crea otra, con las mismas reglas ya vigentes, sin que haya que volver a incorporar la norma, y los planes que apliquen las reglas que cambian quedan desactualizados. [I3, I4]
- **7b** · *Condición:* La norma tiene una «vigencia hasta» que aún no llega. → *Resolución:* La vista previa muestra hasta qué periodo o qué fecha rigen sus reglas y advierte que, cuando el administrador avance el periodo en curso más allá de ese periodo, o al terminar esa fecha, el sistema creará una versión nueva, con las mismas reglas y las de esta norma como «ya no rige», sin que haya que volver a incorporar nada. [I3, I4]
- **7c** · *Condición:* La construcción de la vista previa no termina en sesenta segundos (supuesto del equipo). → *Resolución:* El sistema la cancela, no guarda nada, informa al administrador y registra un incidente con el paso, los programas y la causa; el administrador puede reintentar (el caso vuelve al paso 7) o reducir el alcance (el caso vuelve al paso 3). [I1, I2]
- **7d** · *Condición:* La norma define materias, agrupaciones o la libre elección de un programa (como el acuerdo de su plan de estudios) y la malla estándar queda incompleta en el consolidado resultante (falta el semestre sugerido de alguna materia obligatoria) o no alcanza (en alguna agrupación, las optativas que muestra más los créditos que sugiere sin una materia concreta no alcanzan los créditos optativos que exige, o los créditos de libre elección que sugiere no suman los que exige la libre elección). → *Resolución:* La vista previa advierte qué falta o qué no alcanza y explica que, mientras la malla estándar esté incompleta, RutaUN calcula planes pero no compara con ella el avance ni los planes de los estudiantes (UC01 a UC04). El administrador puede volver al paso 5 para completarla (salvo que la norma ya estuviera registrada; extensión 4c) o continuar (paso 8). [I1, I4]
- **8a** · *Condición:* El administrador no confirma. → *Resolución:* No se guarda nada y el administrador vuelve a la vista previa; la reserva se mantiene mientras siga el caso. [I2]
- **8b** · *Condición:* El administrador quiere cambiar los datos o las reglas después de ver la vista previa. → *Resolución:* El caso vuelve al paso 3 (datos de la norma o alcance) o al paso 5 (reglas; si la norma ya estaba registrada, sus reglas no se pueden cambiar) con lo ingresado conservado y la vista previa se descarta; al validar de nuevo el alcance (paso 4), el sistema ajusta la reserva a los programas que queden (extensión 4d, si otra incorporación reservó alguno). [I1, I2]
- **9a** · *Condición:* Entre la vista previa y la confirmación cambió la versión vigente del consolidado de algún programa del alcance (por ejemplo, porque el sistema creó una versión nueva al empezar o dejar de regir una regla, por el avance del periodo en curso o por la llegada de una fecha). → *Resolución:* El sistema no guarda, informa qué programa cambió y ofrece rehacer la validación y la vista previa desde el paso 6, conservando los datos ingresados; si el administrador no acepta, el caso termina sin cambios y se libera la reserva. [I2, I3]
- **9b** · *Condición:* Falla el guardado para alguno de los programas (error de persistencia). → *Resolución:* Como la incorporación es atómica, no se incorpora en ningún programa. El sistema informa qué falló, conserva lo ingresado y la reserva mientras siga el caso y registra un incidente con el paso, los programas y la causa; el administrador puede reintentar la confirmación (paso 8). [I1, I2]
- **10a** · *Condición:* La sesión expira, el administrador abandona la aplicación u ocurre un error interno después de que se guardó y antes de ver la confirmación. → *Resolución:* La incorporación ya quedó guardada y la reserva se liberó; el administrador ve la nueva versión de cada programa en el historial del consolidado. No se requiere ninguna acción; si fue un error interno, el sistema registra un incidente con el paso y la causa. [I1]
- **Cualquiera** · *Condición:* El administrador abandona el caso o la sesión expira antes de que la incorporación se guarde. → *Resolución:* No se guarda nada: lo ingresado y la vista previa se descartan, los consolidados quedan como estaban y se liberan las reservas de los programas del alcance y de la identificación de la norma (paso 4). [I2]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en un paso anterior al guardado (el fallo del propio guardado lo trata la extensión 9b; si el error ocurre después, la incorporación ya quedó guardada, como en la extensión 10a). → *Resolución:* El sistema no modifica ningún consolidado, libera las reservas, informa al administrador que ocurrió un error y que puede reintentar (el caso vuelve al paso 3, con lo ingresado conservado mientras dure la sesión), y registra un incidente con el paso, los programas involucrados y la causa. [I1, I2]

---

---

## UC07 · Incorporar acuerdo al consolidado del programa (9 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC07 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC07-HU01 (#39) · Registrar la identificación, la vigencia y el alcance de una norma
**Como** administrador de normativa, **quiero** registrar la identificación de una norma, su vigencia y su alcance verificado en su texto, **para** que sus reglas apliquen solo a los programas y periodos que corresponden.
Criterios de aceptación:
- [ ] Solo un usuario con el rol de administrador de normativa puede entrar al caso (ext. 1a, GM 6).
- [ ] Si faltan datos obligatorios o son inválidos (número, año, formato de fecha o de periodo AAAA-1/AAAA-2 o un programa fuera del alcance), el sistema señala cada campo (ext. 4a).
- [ ] Una excepción o medida temporal exige «vigencia hasta», y en cualquier norma la «vigencia hasta» debe ser posterior a la fecha de aplicación (ext. 4b).
- [ ] Si la norma no dice a qué programas aplica, el sistema propone el alcance según la competencia del órgano que la expide y yo lo confirmo o lo corrijo (ext. 4h).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU02 (#40) · Incorporar a otros programas una norma ya registrada
**Como** administrador de normativa, **quiero** incorporar a otros programas una norma que ya está registrada, sin volver a digitarla, **para** mantener un solo registro por norma.
Criterios de aceptación:
- [ ] Si la norma ya existe (mismo tipo, número, año y órgano), el sistema carga su registro y lo usa en lugar de lo digitado; lo ya registrado no se cambia (ext. 4c).
- [ ] Los programas en que ya está incorporada se quitan del alcance y el sistema informa cuáles; si ya está en todos, el caso termina sin cambios (ext. 4c).
- [ ] Si la validación rechaza la norma para un programa, puedo quitar ese programa del alcance o terminar sin cambios (ext. 4c).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU03 (#41) · Registrar las reglas de la norma
**Como** administrador de normativa, **quiero** registrar cada regla de la norma con su categoría, sus datos, su artículo o numeral y si es regla del plan o informativa, **para** que el cálculo pueda aplicarla y los visitantes consultarla con su fuente.
Criterios de aceptación:
- [ ] Una regla sin enunciado, sin artículo o numeral o con datos incompletos según su categoría no se acepta y el sistema indica qué falta; ninguna regla queda sin fuente (ext. 6a, GM 4).
- [ ] Una regla temporal (por su norma o por una «vigencia hasta» propia) solo puede ser informativa; si clasifico alguna como regla del plan, el sistema indica cuáles reclasificar (ext. 6f).
- [ ] Una norma con todas sus reglas informativas se acepta y la vista previa informa que ningún plan queda desactualizado por ella (ext. 5a).
- [ ] En las reglas de materia, de agrupación y de libre elección puedo registrar los datos de la malla estándar (paso 5, GE 5).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU04 (#42) · Validar la coherencia del consolidado resultante
**Como** administrador de normativa, **quiero** que el sistema valide que el consolidado resultante de cada programa sea coherente y calculable, **para** no publicar una versión que rompa los planes o las consultas.
Criterios de aceptación:
- [ ] Una regla que modifica, deroga o exceptúa a otra inexistente o ya vencida no se acepta y el sistema dice cuál y en qué programa (ext. 6b).
- [ ] Una contradicción con otra regla de vigencia solapada se informa citando las dos fuentes (ext. 6c).
- [ ] Si la malla no es utilizable, las cuentas de créditos no cierran o el catálogo es incoherente, el sistema muestra qué falla con los créditos de cada parte y no deja continuar (ext. 6d).
- [ ] Un prerrequisito circular, que apunta a una materia del programa que no rige o que menciona una materia fuera de la malla sin registrarla como externa no se acepta; todo se valida también como quedaría cuando empiece a regir una regla programada o deje de regir una temporal (ext. 6e, paso 6).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU05 (#43) · Revisar la vista previa del impacto
**Como** administrador de normativa, **quiero** ver antes de confirmar qué cambiará en cada programa y cuántos planes y avances se afectan, **para** decidir la incorporación sabiendo su impacto.
Criterios de aceptación:
- [ ] La vista previa muestra, por programa, las reglas nuevas, modificadas, derogadas o vencidas y desde cuándo rigen (paso 7, GE 7).
- [ ] Muestra cuántos planes vigentes quedarían desactualizados y cuántos avances tendrían materias que dejan de regir, solo como cantidades, sin identificar a ningún estudiante (paso 7, GM 8).
- [ ] Si la norma es programada o tiene «vigencia hasta», la vista previa lo advierte con su periodo o su fecha (ext. 7a, 7b).
- [ ] Si la malla estándar queda incompleta, la vista previa advierte qué falta y que, mientras tanto, no se comparan avances ni planes con ella (ext. 7d).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU06 (#44) · Confirmar la incorporación de forma atómica
**Como** administrador de normativa, **quiero** confirmar la incorporación y que se guarde completa en todos los programas del alcance o en ninguno, **para** no dejar consolidados a medias.
Criterios de aceptación:
- [ ] Ningún consolidado cambia hasta que confirmo; validar o ver la vista previa no modifica nada (GM 1).
- [ ] La incorporación es todo o nada; si falla para un programa no se incorpora en ninguno, lo ingresado se conserva y se registra un incidente (GM 2, ext. 9b).
- [ ] Si la versión vigente de algún programa cambió desde la vista previa, el sistema no guarda y ofrece rehacer la validación (ext. 9a).
- [ ] Al confirmar, el sistema informa la versión creada en cada programa y el historial registra quién incorporó la norma y cuándo, conservando las versiones anteriores (paso 10, GE 7, GM 3).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU07 (#45) · Evitar incorporaciones simultáneas sobre los mismos programas
**Como** administrador de normativa, **quiero** que, mientras incorporo una norma, ninguna otra incorporación toque los mismos programas ni registre la misma norma, **para** no crear versiones del consolidado en conflicto.
Criterios de aceptación:
- [ ] Al validar el alcance, el sistema reserva a la vez los programas del alcance y la identificación de la norma (paso 4).
- [ ] Si algo ya está reservado por otra incorporación, el sistema no reserva nada nuevo, dice qué está reservado y me deja esperar o reducir el alcance (ext. 4d).
- [ ] La reserva se libera cuando el caso termina, por cualquier salida: guardado, sin cambios, abandono, sesión expirada o error interno (GM 1).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU08 (#46) · Aplicar la vigencia de las reglas sin volver a incorporarlas
**Como** administrador de normativa, **quiero** que las reglas programadas empiecen a regir y las temporales dejen de regir solas, al avanzar el periodo en curso o al llegar su fecha, **para** no tener que volver a incorporar la norma cada vez que cambia su vigencia.
Criterios de aceptación:
- [ ] Cada regla figura «vigente», «programada» o «ya no rige» y solo aplica a los programas de su alcance y a los periodos de su vigencia (GE 3, GE 4).
- [ ] Cuando una regla programada empieza a regir o una temporal deja de regir, el sistema crea una versión nueva del consolidado con ese motivo (GE 3, ext. 7a, 7b).
- [ ] Los planes que aplican una regla que cambió figuran como desactualizados sin recalcularse; solo el estudiante los replanifica (GE 6, GM 5).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

### UC07-HU09 (#47) · Registrar las reglas de un artículo que fija muchas a la vez
**Como** administrador de normativa, **quiero** registrar por separado cada regla de un artículo que fija muchas a la vez (como el cuadro de materias de un plan de estudios), con la vigencia propia que la norma le dé, **para** que cada una tenga su categoría, su objeto y su fuente exactos.
Criterios de aceptación:
- [ ] Puedo registrar muchas reglas con el mismo artículo o numeral, rotulado como en la norma (por ejemplo, «art. 2 (transitorio)»), y una regla de materia por cada agrupación en que la norma ubica la materia (paso 5).
- [ ] Si la norma le fija a una regla una vigencia propia, la registro con su fecha de aplicación y su «vigencia hasta»; si tiene «vigencia hasta», queda temporal y solo puede ser informativa (paso 5, ext. 6f).
- [ ] Una materia que un prerrequisito menciona y que no está en la malla la registro como externa, de nivelación o de otro programa; si no lo hago, el sistema no acepta la regla (paso 5, ext. 6e).
- [ ] Para modificar, derogar o exceptuar una regla la identifico por su norma, su artículo o numeral y su objeto, o remito a todas las reglas de un artículo si la norma lo sustituye completo; si no la encuentra, el sistema no acepta la remisión (paso 5, ext. 6b).

Caso de uso relacionado: UC07 · Incorporar acuerdo al consolidado del programa

---

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #39–#47 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
