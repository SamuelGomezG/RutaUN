---
title: "UC05 · Entender restricciones de una materia"
doc_type: "caso-de-uso"
srs_ids:
  - UC05
  - UC05-HU01..HU07
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
# UC05 · Entender restricciones de una materia

> **Cuándo cargar:** para implementar o revisar **UC05**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Visitante (con o sin sesión) · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Visitante** — *Quiere:* saber qué exige una materia para cursarla —sus prerrequisitos y sus reglas de habilitación, y la cadena de requisitos que conduce a ella— en cualquier programa cargado, sin necesidad de iniciar sesión. *Por qué:* el entrevistado cuenta que en Diseño Industrial, su segundo programa, donde solo existe la lista de materias, "es por el voz a voz que uno se entera qué materia es prerrequisito de qué" (en Ingeniería de Sistemas, aclaró, "la malla curricular es muy clara"). Sin una explicación por materia, la restricción se descubre tarde, al planear o al inscribir; y consultar la normativa de un programa no debería exigir una cuenta, porque es información de la Universidad, no del estudiante.
- **I2 · Visitante** — *Quiere:* que la explicación sea confiable y verificable: cada restricción con su fuente (ver «Fuente»), su fecha de aplicación y su vigencia. *Por qué:* el acta ZEUS-01 (sección 3) registra que la información de voz a voz a veces se contradice entre compañeros; necesita algo que pueda comprobar en el documento oficial.
- **I3 · Estudiante (con sesión, en su programa)** — *Quiere:* ver, además, en qué estado está cada restricción según su avance (cumplida, en curso o pendiente), aunque ya curse o haya aprobado la materia; en una optativa, cuántos créditos lleva su agrupación y cuáles son las demás optativas, con su estado, pudiendo consultar cualquiera de ellas; y, como dato adicional, dónde ubica la materia su plan vigente. *Por qué:* elegir qué optativa cursar exige ver el conjunto; el entrevistado dice, sobre Diseño Industrial (su segundo programa), donde no hay un documento equivalente a la malla: "En Diseño yo desconozco qué materias son optativas o cuáles son obligatorias". Y un estudiante puede preferir consultar aquí los requisitos de una materia que ya cursa antes que en otra herramienta.
- **I4 · Visitante** — *Quiere:* que la explicación deje claro lo que RutaUN no verifica (oferta, cupos, sobrecupos, horarios, cupo de créditos, reconocimientos, autorizaciones del Consejo de Facultad y cumplimiento de los requisitos de grado) y que «sin restricciones pendientes» no se lea como «puedo inscribirla ya». *Por qué:* no debe confundir que una materia no tenga restricciones en su programa con que esté abierta y tenga cupo; el entrevistado, en su doble titulación, ya vio cómo un sobrecupo que no le aprobaron le cambió los planes (acta ZEUS-01, sección 2).
- **I5 · Estudiante** — *Quiere:* que consultar una restricción no modifique su avance ni su plan y que nadie más vea su avance. *Por qué:* su avance es un dato personal protegido por la Ley 1581 de 2012 y consultar debe ser una operación inocua.
- **I6 · Administrador de normativa** — *Quiere:* que toda explicación se construya solo con reglas de la versión vigente del consolidado, que incluye las reglas programadas y las que ya no rigen, cada una con su estado y con su fuente. *Por qué:* para que lo que ve el visitante sea exactamente lo que incorporó y pueda corregirse en un solo lugar.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando una regla no tiene fuente, cuando el consolidado no tiene una malla utilizable, cuando un prerrequisito apunta a una materia que no rige o forma un ciclo, cuando hay reglas en revisión, cuando un estudiante con sesión reporta un enlace que no abre o cuando hay un error interno. *Por qué:* solo él puede corregir el consolidado y las fuentes y, como responsable de operar RutaUN, atiende los errores internos; si no se entera, la explicación seguirá siendo incompleta para todos los que consulten el programa.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que la explicación reproduzca fielmente lo que la norma exige, con su identificación, su artículo o numeral y su vigencia, y que RutaUN no interprete excepciones que les corresponden. *Por qué:* una explicación que reinterprete o dé por cumplida una restricción sustituiría la decisión del órgano que expidió la norma y haría que el visitante confiara en RutaUN y no en la norma, cuyo texto oficial se publica en Régimen Legal.

## Precondiciones
1. El programa que se consulta está cargado en el sistema y tiene una versión vigente del consolidado.
2. No se requiere sesión. Si el visitante es un estudiante que inició sesión y consulta su propio programa, la explicación incluye su avance y, como dato adicional, su plan vigente.

## Garantía mínima (GM)
1. Este caso de uso no modifica el avance, el plan vigente ni el consolidado; solo puede registrar incidentes. [I5]
2. Toda regla mostrada cita su fuente o, si no la tiene, se muestra con la señal «sin fuente registrada», sin presentarse como respaldada por una norma, y queda un incidente. [I2, I6, I7, I8]
3. La explicación nunca afirma que alguien pueda o no inscribir la materia: solo dice qué exigen las reglas del programa y, para un estudiante en su programa, cómo está según el avance que él registró. [I4, I8]
4. Nadie ve el avance ni el plan de otro: solo el propio estudiante, con sesión y en su programa, ve los suyos en la explicación. Las consultas no se guardan. [I5]
5. Si el consolidado o un fallo impiden explicar, queda un incidente para el administrador y el visitante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]

## Garantía de éxito (GE)
1. El visitante sabe qué restricciones tiene la materia y, de cada una, qué es, por qué aplica y qué la satisface; un estudiante en su programa sabe además en qué estado está cada una según su avance. [I1, I3]
2. Cada restricción cita su fuente (ver «Fuente»), su fecha de aplicación y su vigencia. [I2, I8]
3. Cuando la materia es una optativa, el visitante ve su agrupación (y las que la contienen), los créditos que exige y las demás optativas; un estudiante en su programa ve además los créditos cubiertos y el estado de cada optativa. [I3]
4. Si el estudiante tiene plan vigente, sabe, como dato adicional, en qué semestre su plan ubica la materia (como materia o como opción de un espacio de optativa) o que el plan no la incluye, y si el plan está desactualizado. [I3]
5. El visitante sabe lo que RutaUN no verifica: oferta, cupos, sobrecupos, horarios, cupo de créditos, reconocimientos, autorizaciones del Consejo de Facultad y cumplimiento de los requisitos de grado. [I4]
6. Ningún dato guardado cambió. [I5]

## Disparador
El visitante quiere saber qué exige una materia de un programa para cursarla —y, si es un estudiante en su programa, qué le falta— y selecciona sobre ella «Ver restricciones»: en la malla o en el cuadro de prerrequisitos del programa que consulta («Consultar regla del programa y su acuerdo», UC06) o, si es estudiante con sesión, en su malla («Registrar avance en la malla», UC03) o en su plan vigente.

## Escenario principal de éxito
1. El visitante selecciona una materia y, sobre ella, «Ver restricciones».
2. El sistema identifica la materia en la versión vigente del consolidado del programa consultado y, si es optativa, su agrupación y las agrupaciones que la contienen; si el visitante es un estudiante con sesión que consulta su propio programa, identifica también el estado de la materia en su avance. [I1, I3, I5, I6]
3. El sistema evalúa qué exigen para cursarla las reglas del programa vigentes en la versión vigente del consolidado (las mismas con que se calcularía un plan hoy): sus prerrequisitos, correquisitos y reglas de habilitación; para un estudiante en su programa, clasifica cada uno **por certeza** según su avance (cumplida, en curso, incierta o pendiente; S18): una «y» toma el peor de sus términos y una «o», el mejor. [I1, I3, I6]
4. El sistema presenta la explicación: cada restricción (qué es, por qué aplica y qué la satisface), con su estado si se evaluó con el avance, su fuente (ver «Fuente»), su fecha de aplicación y su vigencia; si es optativa, su agrupación con los créditos que exige (y, para el estudiante, los cubiertos) y las demás optativas (con su estado, para el estudiante); para un estudiante con plan vigente, como dato adicional, el semestre en que el plan la ubica (extensiones 4h a 4j); y el aviso de lo que RutaUN no verifica. [I1, I2, I3, I4, I8]
5. El visitante selecciona una restricción para ver su fuente. [I2]
6. El sistema muestra la fuente completa (identificación de la norma, artículo o numeral y enlace), con su fecha de aplicación, vigencia y alcance, y el recordatorio de que RutaUN no reemplaza el documento oficial. [I2, I8]

## Extensiones
- **2a** · *Condición:* La materia seleccionada no rige en la versión vigente del consolidado (por ejemplo, porque el estudiante la tomó de un plan guardado antes de un cambio de malla). → *Resolución:* El sistema lo informa y no explica restricciones de una materia que ya no rige; si es un estudiante que la tiene aprobada o en curso, le indica cómo cuenta en su avance (ver «Materia que ya no rige»). El caso termina sin cambios. [I1, I6]
- **2b** · *Condición:* Un estudiante en su programa ya tiene la materia aprobada o en curso. → *Resolución:* El sistema lo indica junto a la materia y muestra igual sus restricciones, con su estado (pasos 3 y 4). Si la marca «en curso» está por actualizar (registrada en un periodo anterior al periodo en curso), lo avisa. El caso continúa en el paso 3. [I1, I3]
- **2c** · *Condición:* El consolidado vigente no tiene una malla utilizable (ver «Malla utilizable»); se atiende antes que 2a, 2b y 2d. → *Resolución:* El sistema no explica, informa que el consolidado normativo del programa tiene un problema que ya se informó al administrador de normativa, y registra un incidente con el programa, la versión y la causa. El caso termina sin cambios. [I1, I7]
- **2d** · *Condición:* El visitante no ha iniciado sesión, o es un estudiante que consulta un programa distinto del suyo. → *Resolución:* La explicación no usa ningún avance: muestra qué exige cada restricción, sin estados, y no muestra información de planes. Si el visitante quiere ver su propio estado, puede iniciar sesión y consultar su programa. El caso continúa en el paso 3. [I1, I5]
- **3a** · *Condición:* La materia no tiene restricciones o, para un estudiante en su programa, ninguna está pendiente (las que están en curso se explican con la extensión 4b). → *Resolución:* El sistema lo informa (que el programa no le exige requisitos, o que, según el avance registrado, no tiene restricciones pendientes) y recuerda que eso no significa que esté ofrecida ni que haya cupo. El caso continúa en el paso 4, que muestra igual las restricciones que tenga, con su estado. [I1, I4]
- **3b** · *Condición:* El consolidado tiene un prerrequisito que apunta a una materia que no rige o que forma un ciclo. → *Resolución:* El sistema no evalúa esa restricción, explica las demás, avisa que una regla no se pudo evaluar y registra un incidente con la materia, el programa y la versión. El caso continúa en el paso 4. [I1, I6, I7]
- **3c** · *Condición:* Una restricción está «en revisión» (ver «Regla»). → *Resolución:* El sistema la muestra con esa señal y con las dos fuentes que se contradicen, no afirma cuál rige y registra un incidente. El caso continúa en el paso 4. [I2, I7, I8]
- **4a** · *Condición:* El prerrequisito admite alternativas («esta o aquella»). → *Resolución:* El sistema muestra las alternativas y, para un estudiante en su programa, cuál de ellas cumple y cuáles le faltan. [I1, I2]
- **4b** · *Condición:* Para un estudiante en su programa, un prerrequisito figura «en curso». → *Resolución:* Para el plan se cuenta como cumplido, pero el sistema avisa que solo se confirma cuando el estudiante lo apruebe y que RutaUN no verifica si el SIA permite inscribir con el prerrequisito en curso. Si la marca «en curso» está por actualizar (registrada en un periodo anterior al periodo en curso), lo avisa. [I1, I4]
- **4c** · *Condición:* La restricción es una regla de habilitación (por ejemplo, un mínimo de créditos aprobados de un componente). → *Resolución:* El sistema muestra cuántos créditos exige la regla y de qué componente, o si es del programa; para un estudiante en su programa, muestra también cuántos lleva aprobados y cuántos en curso, según la medida de la regla (sin excedentes; ver «Regla de habilitación»), y avisa si hay créditos «en curso» por actualizar. [I1, I2]
- **4d** · *Condición:* Una restricción no tiene fuente registrada. → *Resolución:* El sistema la muestra con la señal «sin fuente registrada», no la presenta como respaldada por una norma y registra un incidente para el administrador. [I2, I6, I7, I8]
- **4e** · *Condición:* La restricción es temporal (ella o su norma tiene «vigencia hasta»; ver «Vigencia») o está programada. → *Resolución:* El sistema la muestra con su vigencia y los periodos que cubre. Si es temporal y está vigente, la explica (y, para un estudiante en su programa, la evalúa con el avance), pero advierte que es temporal y que el plan no la aplica (ver «Reglas del plan y reglas informativas»); si es programada, la muestra como tal, con el periodo o la fecha en que empieza a regir y sin evaluarla, y, si el plan vigente del estudiante ubica la materia en un semestre cuyo periodo esa regla ya cubriría, lo advierte para que esté pendiente. [I2, I8]
- **4f** · *Condición:* Para un estudiante en su programa, la materia es una optativa y su agrupación ya está cubierta (ver «Agrupación»). → *Resolución:* El sistema lo dice: la materia no es necesaria para cubrir la agrupación, y cursarla sería un excedente que, según el supuesto del equipo (1), no reduce lo exigido a otra agrupación ni a la libre elección. Si la cobertura depende de optativas «en curso», lo advierte, porque el estudiante puede no aprobarlas. [I3, I4]
- **4g** · *Condición:* Para un estudiante en su programa, la materia es una optativa y su agrupación no está cubierta. → *Resolución:* El sistema indica cuántos créditos le faltan a la agrupación y que, en el plan, las optativas que faltan aparecen como espacios de optativa, entre cuyas opciones el estudiante elige o fija la que prefiera (ver «Espacio de optativa»). [I3]
- **4h** · *Condición:* El estudiante no tiene plan vigente. → *Resolución:* El sistema no muestra información de plan. El caso continúa en el paso 5. [I3]
- **4i** · *Condición:* El plan vigente está desactualizado. → *Resolución:* El sistema muestra el semestre en que el plan ubica la materia, si la ubica, con la advertencia de que el plan está desactualizado. El caso continúa en el paso 5. [I3]
- **4j** · *Condición:* El plan vigente no ubica la materia (es una optativa que no figura en él como fijada ni como opción de ningún espacio, o el plan se calculó antes de que la materia formara parte del programa). → *Resolución:* El sistema informa que el plan vigente no la incluye y por qué; si además está desactualizado, se atiende la extensión 4i. El caso continúa en el paso 5. [I3]
- **4k** · *Condición:* Un prerrequisito de la materia menciona materias externas (ver «Materia externa»), o la materia es no cursable desde el programa. → *Resolución:* El sistema no muestra las materias externas: un término que es una materia de nivelación se da por cumplido y no se lista, y una rama «o» con una materia de otro programa no aparece. Si la materia no puede cursarse sin una materia de otro programa, la muestra con la señal «no se puede cursar desde este programa», explica que su prerrequisito exige una materia que los estudiantes del programa no pueden inscribir, sin nombrarla, y recuerda que el plan no la ubica. Para un estudiante que la tiene aprobada o en curso, lo indica igual (extensión 2b) y la cuenta en su avance. El caso continúa en el paso 5. [I1, I2, I3]
- **4l** · *Condición:* La materia es una electiva de una agrupación de libre elección (ver «Libre elección»). → *Resolución:* El sistema explica sus restricciones como las de cualquier materia (para un estudiante en su programa, evaluadas con su avance), muestra su agrupación y aclara que el plan no la individualiza: la cuenta dentro de los créditos de libre elección. No muestra su estado en el avance, porque las electivas no se marcan. El caso continúa en el paso 5. [I1, I3]
- **5a** · *Condición:* El visitante no necesita ver la fuente completa. → *Resolución:* El caso termina con éxito en el paso 4. [I1]
- **5b** · *Condición:* El visitante selecciona «Ver cadena de requisitos». → *Resolución:* El sistema muestra la cadena de requisitos de la materia (ver «Cadena de requisitos»): el subgrafo de sus prerrequisitos y reglas de habilitación, con los de cada uno, hasta las materias que no exigen nada y con cada rama de las alternativas «o»; o la materia sola, si no tiene restricciones. Para un estudiante en su programa, cada nodo muestra su estado. Si el visitante selecciona una materia de la cadena, el caso vuelve al paso 2 con ella; si no, sigue en el paso 5. [I1, I3]
- **5c** · *Condición:* El visitante selecciona otra optativa de la agrupación. → *Resolución:* El caso vuelve al paso 2 con esa optativa. [I1, I3]
- **6a** · *Condición:* La fuente no tiene enlace registrado, o el enlace no abre. → *Resolución:* El sistema muestra la identificación completa (norma, artículo o numeral y fecha) para ubicarla en Régimen Legal y avisa que el enlace no está disponible. Si el visitante quiere reportar que un enlace registrado no abre («Reportar enlace roto»), el sistema le pide iniciar sesión, si no lo ha hecho, y registra un incidente con ese enlace. El caso termina con éxito. [I2, I7, I8]
- **6b** · *Condición:* La norma fuente fue modificada por otra posterior. → *Resolución:* El sistema muestra la cadena (la norma original y sus modificatorias, cada una con su vigencia) y marca cuál rige hoy, que es la que usa la explicación (paso 3). [I2, I8]
- **Cualquiera** · *Condición:* Mientras el visitante consultaba cambió la versión vigente del consolidado. → *Resolución:* El sistema avisa que hay una versión más reciente y ofrece recargar la explicación: si el visitante acepta, el caso vuelve al paso 2 con la misma materia; si no, sigue donde estaba, y lo mostrado queda identificado con su versión, que ya no se presenta como la vigente. [I2, I6]
- **Cualquiera** · *Condición:* El visitante abandona el caso o, si tenía sesión, esta expira. → *Resolución:* No se modifica ningún dato. [I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso. → *Resolución:* El sistema no modifica ningún dato, informa que ocurrió un error y que puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I1, I5, I7]

---

---

## UC05 · Entender restricciones de una materia (7 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC05 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC05-HU01 (#27) · Ver las restricciones de una materia sin iniciar sesión
**Como** visitante, **quiero** ver qué exige una materia de cualquier programa cargado para cursarla, sin iniciar sesión, **para** no enterarme por el voz a voz de qué es prerrequisito de qué.
Criterios de aceptación:
- [ ] De cada restricción se muestra qué es, por qué aplica y qué la satisface, con su fuente, su fecha de aplicación y su vigencia (GE 1, GE 2).
- [ ] Si el prerrequisito admite alternativas («esta o aquella»), el sistema las muestra todas (ext. 4a).
- [ ] Si la materia no tiene restricciones, el sistema lo dice y recuerda que eso no significa que esté ofrecida ni que haya cupo; nunca afirma que alguien pueda o no inscribirla (ext. 3a, GM 3).
- [ ] Una restricción sin fuente se muestra con la señal «sin fuente registrada» y se registra un incidente (ext. 4d, GM 2).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU02 (#28) · Ver el estado de cada restricción según mi avance
**Como** estudiante con sesión que consulta su propio programa, **quiero** ver en qué estado está cada restricción de una materia según mi avance, **para** saber qué me falta para poder cursarla.
Criterios de aceptación:
- [ ] Cada restricción se clasifica como cumplida, en curso o pendiente según mi avance; si ya aprobé o estoy cursando la materia, el sistema lo indica y muestra igual sus restricciones (paso 3, ext. 2b).
- [ ] Un prerrequisito «en curso» se cuenta como cumplido para el plan, con el aviso de que solo se confirma al aprobarlo (ext. 4b).
- [ ] En una regla de habilitación, el sistema muestra cuántos créditos exige y de qué componente y cuántos llevo aprobados y en curso (ext. 4c).
- [ ] Si no inicié sesión o consulto otro programa, la explicación no usa ningún avance y nadie más ve el mío (ext. 2d, GM 4).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU03 (#29) · Ver la agrupación de una optativa y sus demás optativas
**Como** visitante, **quiero** ver, cuando la materia es optativa, su agrupación, los créditos que exige y las demás optativas, **para** elegir una optativa viendo el conjunto.
Criterios de aceptación:
- [ ] Se muestran la agrupación (y las que la contienen), sus créditos exigidos y las demás optativas; un estudiante en su programa ve además los créditos cubiertos y el estado de cada optativa (GE 3).
- [ ] Para un estudiante en su programa, si la agrupación ya está cubierta, el sistema dice que la materia sería un excedente; si no, cuántos créditos le faltan (ext. 4f, 4g).
- [ ] Puedo seleccionar otra optativa de la agrupación y ver sus restricciones (ext. 5c).
- [ ] Si la materia es una electiva de una agrupación de libre elección, el sistema lo dice y aclara que el plan la cuenta dentro de los créditos de libre elección (ext. 4l).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU04 (#30) · Ver la cadena de requisitos de una materia
**Como** visitante, **quiero** ver la cadena de requisitos que conduce a una materia, **para** entender qué tengo que cursar antes, y en qué orden.
Criterios de aceptación:
- [ ] La cadena muestra los prerrequisitos y reglas de habilitación de la materia, los de cada uno, hasta las materias que no exigen nada, con cada rama de las alternativas «o»; si la materia no tiene restricciones, se muestra sola (ext. 5b).
- [ ] Para un estudiante en su programa, cada nodo muestra su estado (cumplido, en curso o pendiente) (ext. 5b).
- [ ] Al seleccionar una materia de la cadena, el sistema explica sus restricciones (ext. 5b).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU05 (#31) · Ver la fuente completa de una restricción
**Como** visitante, **quiero** ver la fuente completa de una restricción, con su vigencia, **para** comprobarla en el documento oficial.
Criterios de aceptación:
- [ ] El sistema muestra la norma, el artículo o numeral, el enlace, la fecha de aplicación, la vigencia y el alcance, y recuerda que RutaUN no reemplaza el documento oficial (paso 6).
- [ ] Si la norma fue modificada, muestra la cadena de normas con su vigencia y marca la que rige hoy (ext. 6b).
- [ ] Una restricción temporal se muestra con su vigencia y la advertencia de que el plan no la aplica; una programada, con el periodo o la fecha en que empieza a regir y sin evaluarla (ext. 4e).
- [ ] Si no hay enlace o no abre, el sistema muestra la identificación completa; reportar el enlace roto pide iniciar sesión y registra un incidente (ext. 6a).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU06 (#32) · Ver dónde ubica mi plan vigente una materia
**Como** estudiante con plan vigente, **quiero** ver, al consultar una materia, en qué semestre la ubica mi plan vigente, **para** relacionar la restricción con mi planeación.
Criterios de aceptación:
- [ ] Si el plan la ubica, el sistema dice en qué semestre, como materia o como opción de un espacio de optativa (GE 4).
- [ ] Si el plan no la incluye, el sistema lo dice y explica por qué; si no tengo plan vigente, no muestra información de plan (ext. 4j, 4h).
- [ ] Si el plan vigente está desactualizado, la ubicación se muestra con esa advertencia (ext. 4i).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

### UC05-HU07 (#33) · Saber que una materia no se puede cursar desde mi programa
**Como** visitante, **quiero** saber cuándo una materia de la malla no se puede cursar desde el programa y no ver materias que el programa no ofrece, **para** no contar con una optativa que no podré inscribir.
Criterios de aceptación:
- [ ] Las materias externas que menciona un prerrequisito no se muestran: un término de nivelación se da por cumplido y una rama «o» con una materia de otro programa no aparece (ext. 4k).
- [ ] Si la materia no se puede cursar sin una materia de otro programa, se muestra con la señal «no se puede cursar desde este programa», sin nombrar la materia externa, y con el recordatorio de que el plan no la ubica (ext. 4k).
- [ ] Si soy estudiante y la tengo aprobada o en curso, el sistema lo indica y la cuenta en mi avance como cualquier otra (ext. 2b, ext. 4k).

Caso de uso relacionado: UC05 · Entender restricciones de una materia

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #27–#33 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
