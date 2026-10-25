---
title: "UC06 · Consultar regla del programa y su acuerdo"
doc_type: "caso-de-uso"
srs_ids:
  - UC06
  - UC06-HU01..HU05
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
# UC06 · Consultar regla del programa y su acuerdo

> **Cuándo cargar:** para implementar o revisar **UC06**. Antes de codificar carga `../01-invariantes.md`, `../04-supuestos.md` y los archivos del glosario que este caso cita.

## Cómo leer este caso

**Cómo leer cada caso:** *Interesados e intereses* (I1…In: qué quiere y por qué; las etiquetas `[I-n]` en pasos, extensiones y garantías remiten a ellos) → *Precondiciones* → *Garantía mínima (GM)*: se cumple sin importar el resultado → *Garantía de éxito (GE)*: cuando la meta se cumple → *Disparador* → *Escenario principal de éxito* (el «camino feliz») → *Extensiones*: las excepciones que pueden surgir, identificadas por el número del paso en que ocurren más una letra (p. ej. `4b`); «Cualquiera» = puede ocurrir en cualquier paso.

Las extensiones **vuelven a un paso, continúan en el paso siguiente (si no dicen otra cosa) o terminan**, con éxito o sin cambios (ver las convenciones al inicio del glosario, §7).

**Actor principal:** Visitante (con o sin sesión) · **Sistema:** RutaUN · **Nivel:** Objetivo de usuario

## Interesados e intereses
- **I1 · Visitante** — *Quiere:* consultar en un solo lugar, sin necesidad de iniciar sesión, las reglas de cualquier programa cargado (créditos por componente y por agrupación, prerrequisitos, agrupaciones, equivalencias, libre elección, carga mínima y máxima por periodo, requisitos de grado), incluidas las que son temporales, el acuerdo del que sale cada una y una representación visual de la malla y de los prerrequisitos del programa. *Por qué:* el entrevistado, sobre los acuerdos legales de Diseño Industrial, su segundo programa, dice "siento que no hay información tan central sobre eso" (no habló de si a Ingeniería de Sistemas le falta algo así), y se enteró de que podía usar créditos de electiva para su doble titulación "en Cátedra de Inducción. Esa materia que todo el mundo cancela en primer semestre". Además, un integrante del equipo verificó, buscando los acuerdos que rigen Ingeniería Industrial, que también dentro de un solo programa esas reglas no están reunidas en un solo lugar; la Entrega 1 había dejado esta causa abierta porque la evidencia del entrevistado venía de su segundo programa. Que la normativa sea fácil de consultar y de entender visualmente es la razón de ser de este caso; atarla a una cuenta la haría menos accesible.
- **I2 · Visitante** — *Quiere:* distinguir qué rige hoy (y, dentro de eso, qué es temporal), qué está programado y qué ya no rige, y saber a qué programas aplica cada norma, con su vigencia y su alcance a la vista. *Por qué:* las medidas temporales son reales y puntuales: la Resolución 057 de 2026 (Comisión Delegataria del CSU) exceptúa el artículo 10 del Acuerdo 008 de 2008 solo durante 2026-2, y la Circular 11 de 2026 (Vicerrectoría Académica) fija un máximo de 23 créditos para la inscripción de 2026-2, solo hasta la primera semana del periodo, y exime a los programas de Medicina y de Medicina Veterinaria. Confundir una medida temporal con la regla general lo llevaría a decidir mal.
- **I3 · Visitante** — *Quiere:* saber cuáles de las reglas usa RutaUN para calcular planes y cuáles solo informa. *Por qué:* para entender por qué un plan hace lo que hace y qué debe verificar por su cuenta (por ejemplo, los requisitos de grado o el cupo de créditos).
- **I4 · Visitante** — *Quiere:* que RutaUN sea transparente sobre su límite: no es la fuente oficial, un resumen no reemplaza la norma y la fuente oficial es el documento publicado en Régimen Legal. *Por qué:* decidir con un resumen desactualizado o equivocado tiene costo real: la Resolución 057 de 2026 dispone que a quien, al 14 de septiembre de 2026, no acredite diez o más créditos inscritos (salvo quien inscribió únicamente los créditos que le faltaban para culminar su plan de estudios) ni la autorización del Consejo de Facultad para una carga inferior, se le apliquen los bloqueos correspondientes a la historia académica: pérdida de la calidad de estudiante por no cumplir los requisitos de renovación de la matrícula (Acuerdo 008 de 2008, art. 44, numeral 2) o reserva de cupo automática, si tiene derecho a ella.
- **I5 · Estudiante** — *Quiere:* que, cuando consulta su propio programa con sesión, la malla y el cuadro de prerrequisitos muestren el estado de su avance y le permitan pasar a registrarlo («Registrar avance en la malla», UC03) o a entender las restricciones de una materia (UC05); y que consultar no modifique su avance ni su plan ni deje que nadie más vea su avance. *Por qué:* así consulta la normativa y su situación sin cambiar de herramienta; su avance es un dato personal protegido por la Ley 1581 de 2012.
- **I6 · Administrador de normativa** — *Quiere:* que cada regla mostrada corresponda a la versión vigente del consolidado (que incluye las reglas programadas y las que ya no rigen, cada una con su estado) y a su fuente. *Por qué:* es su responsabilidad que lo que se muestra sea fiel al consolidado que incorporó y que cada regla pueda contrastarse con su fuente.
- **I7 · Administrador de normativa** — *Quiere:* enterarse, mediante un incidente, cuando una regla no tiene fuente, cuando un estudiante con sesión reporta que un enlace registrado no abre, cuando hay reglas en revisión, cuando el consolidado no tiene reglas cargadas o una malla utilizable para el programa, o cuando ocurre un error interno. *Por qué:* solo él puede corregir el consolidado y las fuentes y, como responsable de operar RutaUN, atiende los errores internos.
- **I8 · Órganos y Régimen Legal** — *Quiere:* que cada regla se muestre con la identificación completa de su norma (tipo, número, año y órgano), artículo o numeral, fecha de aplicación, vigencia y alcance, y con el enlace a Régimen Legal, y que RutaUN no se presente como fuente oficial ni omita que una norma es temporal. *Por qué:* el órgano que expide cada norma decide su alcance y su vigencia, y Régimen Legal es donde se publica; una regla separada de su fuente, de su vigencia o de su alcance se aplicaría mal.

## Precondiciones
1. Hay al menos un programa cargado en el sistema con una versión vigente del consolidado.
2. No se requiere sesión. Si el visitante es un estudiante que inició sesión, puede consultar su propio programa con su avance («Mi programa»).

## Garantía mínima (GM)
1. Este caso de uso no modifica el avance, el plan vigente ni el consolidado; solo puede registrar incidentes. [I5]
2. Toda regla mostrada cita su fuente o, si no la tiene, se muestra con la señal «sin fuente registrada», sin presentarse como respaldada por una norma, y queda un incidente. [I4, I6, I7, I8]
3. Solo se muestran las reglas del consolidado del programa consultado, que son las de las normas incorporadas a ese programa. [I2, I6]
4. Una medida temporal nunca se muestra como regla permanente: siempre aparece con su vigencia y su alcance. [I2, I8]
5. RutaUN nunca se presenta como fuente oficial. [I4, I8]
6. Las consultas no se guardan, y el avance de un estudiante solo lo ve él mismo, con sesión y en su programa. [I5]
7. Si el consolidado o un fallo impiden consultar, queda un incidente para el administrador y el visitante recibe un mensaje que dice qué pasó y qué puede hacer. [I1, I7]

## Garantía de éxito (GE)
1. El visitante encontró la regla que buscaba y el acuerdo del que sale. [I1]
2. La regla se muestra con su enunciado, si el plan la aplica o solo la informa, su acuerdo (tipo, número, año y órgano), el artículo o numeral, la fecha de aplicación, la vigencia, el alcance y el enlace a la fuente. [I1, I2, I3, I8]
3. El visitante distingue lo que rige hoy (y, dentro de ello, lo que es temporal) de lo programado y de lo que ya no rige. [I2]
4. El visitante sabe qué versión del consolidado está viendo y cuándo se actualizó. [I6]
5. El visitante sabe que la fuente oficial es el documento publicado en Régimen Legal. [I4, I8]
6. Si lo pidió, el visitante vio la malla y el cuadro de prerrequisitos del programa; un estudiante en su programa, con el estado de su avance. [I1, I5]

## Disparador
El visitante quiere saber qué dice la normativa de un programa —el suyo o cualquier otro— sobre un asunto (por ejemplo, los créditos de un componente, los prerrequisitos de una materia, el mínimo por periodo o una medida temporal del periodo) y selecciona «Consultar regla del programa y su acuerdo».

## Escenario principal de éxito
1. El visitante selecciona «Consultar regla del programa y su acuerdo».
2. El sistema ofrece «Mi programa», si el visitante inició sesión como estudiante, y «Cualquier programa», con los programas cargados que tienen una versión vigente del consolidado. [I1, I5]
3. El visitante elige el programa. [I1]
4. El sistema muestra las reglas vigentes y programadas del programa agrupadas por categoría (ver «Regla»), cada una con su estado y, si lo es, la etiqueta «temporal», junto con la versión del consolidado y su fecha de actualización, y ofrece ver la malla del programa y su cuadro de prerrequisitos (ver «Cuadro de prerrequisitos»). [I1, I2, I6]
5. El visitante busca por texto o filtra por categoría y por estado (vigente, programada o ya no rige). [I1, I2]
6. El sistema muestra las reglas que coinciden, cada una con su enunciado resumido, su acuerdo y si el plan la aplica o solo la informa. [I1, I3]
7. El visitante selecciona una regla. [I1]
8. El sistema muestra la regla completa: enunciado, acuerdo (tipo, número, año y órgano), artículo o numeral, fecha de aplicación, vigencia, alcance, si el plan la aplica o solo la informa, y las normas que la modificaron o la reemplazaron; y recuerda que la fuente oficial es el documento en Régimen Legal y no RutaUN. [I1, I2, I3, I4, I8]
9. El visitante abre el acuerdo de origen desde el enlace. [I4, I8]

## Extensiones
- **2a** · *Condición:* El visitante no ha iniciado sesión, o no es estudiante (por ejemplo, es el administrador). → *Resolución:* El sistema ofrece solo «Cualquier programa» y la opción de iniciar sesión para consultar el propio. El caso continúa en el paso 3. [I1]
- **3a** · *Condición:* El estudiante elige «Mi programa». → *Resolución:* Además de lo que ve cualquier visitante, la malla y el cuadro de prerrequisitos muestran el estado de cada materia en su avance, y desde ellos puede pasar a «Registrar avance en la malla» (UC03) o a «Entender restricciones de una materia» (UC05); si pasa a otro caso, este termina con éxito. El caso continúa en el paso 4. [I5]
- **4a** · *Condición:* El consolidado vigente no tiene reglas cargadas para el programa. → *Resolución:* El sistema informa que no hay reglas para mostrar y que el consolidado normativo del programa tiene un problema que ya se informó al administrador de normativa, y registra un incidente con el programa, la versión y la causa. El caso termina sin cambios. [I1, I7]
- **4b** · *Condición:* El visitante elige ver la malla o el cuadro de prerrequisitos. → *Resolución:* El sistema muestra la malla del programa (componentes, agrupaciones y subagrupaciones con sus materias y créditos) y, si está registrada, la malla estándar; o su cuadro de prerrequisitos (todas sus materias, con sus prerrequisitos y reglas de habilitación, como un grafo; ver «Cuadro de prerrequisitos»). Si el visitante selecciona una materia, puede pasar a «Entender restricciones de una materia» (UC05) con ella, y este caso termina con éxito; si no, el caso sigue en el paso 5. Si la malla no es utilizable (ver «Malla utilizable»), el sistema no muestra la malla ni el cuadro, lo informa y registra un incidente; las reglas se siguen pudiendo consultar. [I1, I5, I7]
- **5a** · *Condición:* La búsqueda no encuentra ninguna regla. → *Resolución:* El sistema lo dice, sugiere revisar el texto o quitar filtros y aclara que, si la regla no está cargada, RutaUN no puede confirmar que no exista: debe consultarse Régimen Legal. El caso vuelve al paso 5. [I1, I4]
- **5b** · *Condición:* El visitante filtra por el estado «ya no rige». → *Resolución:* El sistema muestra las reglas cuya vigencia terminó (porque la norma venció o porque otra la derogó o la modificó), con la señal «ya no rige» y con su vigencia, sin mezclarlas con las vigentes. El caso continúa en el paso 6. [I2]
- **6a** · *Condición:* Una regla es temporal (ella o su norma tiene «vigencia hasta»; por ejemplo, el art. 2 (transitorio) del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería, la Resolución 057 y la Circular 11 de 2026, cuyas medidas aplican solo a 2026-2). → *Resolución:* La regla se lista con su estado y con la etiqueta «temporal», con su «vigencia hasta» y los periodos que cubre. Si aún no rige, aparece como «programada» con el periodo o la fecha desde la que regirá; si ya no rige, solo aparece al filtrar por «ya no rige». [I2, I8]
- **6b** · *Condición:* Una norma existe en RutaUN, pero no está incorporada al consolidado del programa consultado (porque su alcance no lo comprende, como la Circular 11 de 2026 a los programas de Medicina y de Medicina Veterinaria, o porque aún no se le ha incorporado). → *Resolución:* El sistema no lista sus reglas entre las del programa. Si el visitante busca la norma por su número o su nombre, le informa que existe, que no forma parte del consolidado de ese programa y por qué (su alcance no lo comprende, con el texto de su alcance, o aún no se ha incorporado), sin mostrar sus reglas. [I2, I8]
- **6c** · *Condición:* La regla es informativa (por ejemplo, un requisito de grado, el cupo de créditos o el máximo de la Circular 11 de 2026). → *Resolución:* Se muestra con la etiqueta «solo informativa: el plan no la aplica», para que el visitante sepa que debe verificarla por su cuenta. [I3, I4]
- **6d** · *Condición:* Dos reglas sobre el mismo objeto, con vigencias que se solapan y valores distintos, se contradicen sin que una modifique, derogue o exceptúe a la otra (ver «Regla»). → *Resolución:* El sistema no las presenta como vigentes a la vez: las muestra con la señal «en revisión» y con sus dos fuentes, no afirma cuál rige y registra un incidente para el administrador. Una excepción vigente sobre la regla que exceptúa no es una contradicción: se muestran juntas, cada una con su vigencia. [I2, I6, I7, I8]
- **6e** · *Condición:* Una regla no tiene fuente registrada. → *Resolución:* En cualquier listado, la regla se muestra con la señal «sin fuente registrada», no se presenta como respaldada por una norma y queda un incidente para el administrador. [I4, I6, I7, I8]
- **8a** · *Condición:* La regla vigente es el resultado de varias normas (una base y una o más modificatorias, o una excepción). → *Resolución:* El sistema muestra la cadena en orden cronológico, marca cuál rige hoy y muestra una excepción vigente junto a la regla que exceptúa. [I2, I8]
- **9a** · *Condición:* La fuente no tiene enlace registrado, o el enlace no abre. → *Resolución:* El sistema muestra la identificación completa (norma, artículo o numeral y fecha) para ubicarla en Régimen Legal y avisa que el enlace no está disponible. Si el visitante quiere reportar que un enlace registrado no abre («Reportar enlace roto»), el sistema le pide iniciar sesión, si no lo ha hecho, y registra un incidente con ese enlace. El caso termina con éxito. [I4, I7, I8]
- **9b** · *Condición:* El visitante no abre el acuerdo de origen. → *Resolución:* El caso termina con éxito en el paso 8. [I1]
- **Cualquiera** · *Condición:* Mientras el visitante consultaba cambió la versión vigente del consolidado. → *Resolución:* El sistema avisa que hay una versión más reciente y ofrece recargar: si el visitante acepta, el caso vuelve al paso 4 conservando el programa, la búsqueda y los filtros; si no, sigue donde estaba, y lo mostrado queda identificado con su versión, que ya no se presenta como la vigente. [I1, I6]
- **Cualquiera** · *Condición:* El visitante abandona el caso o, si tenía sesión, esta expira. → *Resolución:* No se modifica ningún dato. [I5]
- **Cualquiera** · *Condición:* Ocurre un error interno del sistema en cualquier paso. → *Resolución:* El sistema no modifica ningún dato, informa que ocurrió un error y que puede reintentar, y registra un incidente con el paso, el programa, la versión del consolidado y la causa. [I1, I5, I7]

---

---

## UC06 · Consultar regla del programa y su acuerdo (5 historias)

Los criterios de aceptación remiten a pasos, extensiones y garantías de UC06 (este archivo). El número entre paréntesis es el issue GitHub del backlog (§12).

### UC06-HU01 (#34) · Elegir un programa y ver sus reglas
**Como** visitante, **quiero** elegir un programa cargado y ver sus reglas agrupadas por categoría, sin iniciar sesión, **para** consultar la normativa en un solo lugar.
Criterios de aceptación:
- [ ] El sistema ofrece «Cualquier programa» y, si inicié sesión como estudiante, también «Mi programa»; sin sesión, ofrece iniciarla para consultar el propio (paso 2, ext. 2a).
- [ ] Las reglas vigentes y programadas se muestran por categoría, cada una con su estado y, si lo es, la etiqueta «temporal» (paso 4).
- [ ] Se muestran la versión del consolidado y su fecha de actualización (GE 4).
- [ ] Si el programa no tiene reglas cargadas, el sistema lo dice, avisa que ya se informó al administrador y registra un incidente (ext. 4a).

Caso de uso relacionado: UC06 · Consultar regla del programa y su acuerdo

### UC06-HU02 (#35) · Buscar y filtrar reglas
**Como** visitante, **quiero** buscar reglas por texto y filtrarlas por categoría y por estado, **para** encontrar rápido la regla que necesito.
Criterios de aceptación:
- [ ] Cada resultado muestra su enunciado resumido, su acuerdo y si el plan la aplica o solo la informa (paso 6, ext. 6c).
- [ ] Al filtrar por «ya no rige», las reglas vencidas o derogadas se muestran con esa señal y su vigencia, sin mezclarse con las vigentes (ext. 5b).
- [ ] Si no hay resultados, el sistema sugiere revisar el texto o quitar filtros y aclara que RutaUN no puede confirmar que la regla no exista: debe consultarse Régimen Legal (ext. 5a).

Caso de uso relacionado: UC06 · Consultar regla del programa y su acuerdo

### UC06-HU03 (#36) · Ver una regla completa y abrir su acuerdo
**Como** visitante, **quiero** ver una regla completa y abrir el acuerdo del que sale, **para** verificarla en la fuente oficial.
Criterios de aceptación:
- [ ] La regla se muestra con su enunciado, su acuerdo (tipo, número, año y órgano), el artículo o numeral, la fecha de aplicación, la vigencia, el alcance, si el plan la aplica o solo la informa y las normas que la modificaron (GE 2, paso 8).
- [ ] Si la regla resulta de varias normas, se muestra la cadena en orden cronológico y se marca cuál rige hoy (ext. 8a).
- [ ] El sistema recuerda que la fuente oficial es el documento en Régimen Legal y nunca se presenta como fuente oficial (GE 5, GM 5).
- [ ] Si el enlace no existe o no abre, se muestra la identificación completa; reportarlo pide iniciar sesión y registra un incidente (ext. 9a).

Caso de uso relacionado: UC06 · Consultar regla del programa y su acuerdo

### UC06-HU04 (#37) · Distinguir lo temporal, lo programado y lo que está en revisión
**Como** visitante, **quiero** distinguir qué rige hoy, qué es temporal, qué está programado y qué está en revisión, **para** no confundir una medida temporal con la regla general.
Criterios de aceptación:
- [ ] Una regla temporal aparece con la etiqueta «temporal», su «vigencia hasta» y los periodos que cubre; una programada, con el periodo o la fecha desde la que regirá (ext. 6a, GM 4).
- [ ] Dos reglas que se contradicen se muestran con la señal «en revisión» y sus dos fuentes, sin afirmar cuál rige, y se registra un incidente (ext. 6d).
- [ ] Si busco una norma que existe pero no está incorporada al programa consultado, el sistema dice que existe, que no forma parte de su consolidado y por qué, sin mostrar sus reglas (ext. 6b).

Caso de uso relacionado: UC06 · Consultar regla del programa y su acuerdo

### UC06-HU05 (#38) · Ver la malla y el cuadro de prerrequisitos
**Como** visitante, **quiero** ver la malla y el cuadro de prerrequisitos de un programa, **para** entender visualmente su estructura.
Criterios de aceptación:
- [ ] La malla muestra componentes, agrupaciones y subagrupaciones con sus materias y créditos y, si está registrada, la malla estándar; el cuadro muestra todas las materias como un grafo, distinguiendo las alternativas «o» y las reglas de habilitación (ext. 4b).
- [ ] Al seleccionar una materia, puedo pasar a «Entender restricciones de una materia» (UC05) (ext. 4b).
- [ ] Si elijo «Mi programa» con sesión, la malla y el cuadro muestran el estado de mi avance y me dejan pasar a «Registrar avance en la malla» (UC03) (ext. 3a, GE 6).
- [ ] Si la malla no es utilizable, el sistema no muestra la malla ni el cuadro, lo dice y registra un incidente; las reglas se siguen pudiendo consultar (ext. 4b).

Caso de uso relacionado: UC06 · Consultar regla del programa y su acuerdo

---

## Trazabilidad

| Elemento | Valor |
|---|---|
| Issues GitHub | #34–#38 |
| Interesados propios | etiquetas `[I-n]` locales de este caso |
