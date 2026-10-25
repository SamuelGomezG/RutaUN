---
title: "Glosario 3 · Malla, agrupaciones y materias"
doc_type: "glosario"
srs_ids:
  - Agrupación
  - Agrupación asignada
  - Cadena de requisitos
  - Componente
  - Correquisito
  - Crédito
  - Cuadro de prerrequisitos
  - Equivalencia
  - Libre elección
  - Malla
  - Malla estándar
  - Malla utilizable
  - Materia
  - Materia externa
  - Materia no cursable desde el programa
  - Optativa
  - Prerrequisito
  - Regla de habilitación
  - Restricción (de una materia)
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
---
# Glosario 3 · Malla, agrupaciones y materias

> **Cuándo cargar:** antes de modelar la malla o evaluar restricciones (prerrequisitos, correquisitos y habilitaciones); alimenta UC03/UC05/UC06.

> Términos: **19** de 71 del glosario; texto verbatim (solo se renivela el encabezado `###` → `##`). Índice completo en [README.md](README.md).

## Agrupación

Conjunto de materias de un componente, con un eje temático y unos créditos exigidos que fija el plan de estudios (por ejemplo, «Matemáticas», con 16 créditos, en el componente de fundamentación de Ingeniería de Sistemas y Computación; Acuerdo 003-A de 2022 del Consejo Académico, art. 5). Puede contener, en cualquier combinación y sin límite de niveles: materias obligatorias, que deben aprobarse todas; materias optativas, alternativas entre sí; y otras agrupaciones, que la norma llama subagrupaciones (por ejemplo, «Matemáticas» contiene la subagrupación «Cálculo diferencial», de 4 créditos, cuyas optativas son «Cálculo diferencial» y «Cálculo diferencial en una variable», según el Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería). Así, una agrupación puede tener solo obligatorias, solo optativas (como «Computación Aplicada») o ambas, con subagrupaciones o sin ellas. Sus créditos exigidos incluyen los de sus obligatorias y los de sus subagrupaciones; lo que falta se cubre con sus optativas. Pertenece a un solo componente o a una sola agrupación.
**Cubierta:** una agrupación está cubierta cuando sus obligatorias figuran aprobadas o en curso, sus subagrupaciones están cubiertas y lo aprobado o en curso dentro de ella (de una materia que figura en varias agrupaciones, solo si esta es su agrupación asignada; ver «Agrupación asignada») suma los créditos que exige, contando de cada subagrupación como máximo lo que exige (como cuenta el cálculo del plan).
**Supuesto del equipo (1):** los créditos que excedan lo que exige una agrupación no reducen lo exigido a otra agrupación, a la agrupación que la contiene ni a la libre elección; las normas revisadas no tratan el tema (ver «Supuestos del equipo»).

## Agrupación asignada

Agrupación para la que cuenta una materia que figura en dos o más agrupaciones del programa (ver «Materia»), sin contar las agrupaciones de libre elección, cuyas electivas no se marcan en el avance (ver «Libre elección»). Se decide cuando el estudiante la marca como aprobada o en curso en UC03 (Registrar avance en la malla), con el avance que tiene en pantalla: si solo una de esas agrupaciones no está cubierta, se le asigna esa; si dos o más no lo están, el estudiante elige entre ellas (UC03, extensión 3f); si todas están cubiertas, no se le asigna ninguna: es un excedente, que no cuenta para ninguna agrupación ni para la libre elección (supuesto del equipo (1), por confirmar: no se sabe si la Universidad la cuenta como libre elección o de otro modo). La asignación se guarda con la marca y no cambia sola, aunque después cambie la cobertura de las agrupaciones; se conserva si la marca pasa de en curso a aprobada, se borra si pasa a pendiente y se decide de nuevo si el estudiante vuelve a marcarla. En el plan, una materia así es opción del espacio de optativa de cada agrupación en que figura y cuenta para la del espacio que ocupa (ver «Espacio de optativa»).

## Cadena de requisitos

Parte del cuadro de prerrequisitos que conduce a una materia: sus prerrequisitos, correquisitos y reglas de habilitación, los de cada uno de ellos, y así hasta las materias que no exigen nada, con cada rama de las alternativas «o». Si la materia no tiene restricciones, se muestra sola. La ofrece UC05 (Entender restricciones de una materia) con «Ver cadena de requisitos»; cuando un estudiante consulta su propio programa, cada nodo muestra su estado (cumplido, en curso o pendiente). No muestra materias externas (ver «Materia externa»); una materia no cursable desde el programa aparece con esa señal.

## Componente

Parte del plan de estudios con unos créditos exigidos (por ejemplo, fundamentación, disciplinar o profesional, o libre elección), tal como la fija el acuerdo del plan de estudios (por ejemplo, el componente de fundamentación de Ingeniería de Sistemas y Computación exige 51 créditos, 15 de obligatorias y 36 de optativas; Acuerdo 003-A de 2022 del Consejo Académico, art. 4). Tiene la misma estructura que una agrupación: puede contener materias obligatorias, optativas y agrupaciones, y sus créditos exigidos incluyen los de todo lo que contiene (ver «Agrupación»). El de libre elección tiene créditos exigidos y, si la norma las lista, agrupaciones de electivas (ver «Libre elección»).

## Correquisito

Restricción de una materia distinta del prerrequisito: conjunto de materias combinadas con «y» y «o» que deben cursarse **en el mismo semestre que ella o antes**. Cada término se cumple si la materia está aprobada o en curso, o si el plan lo ubica en ese semestre o en uno anterior (en el avance, un término que ni está cursado ni ubicado queda pendiente). Un correquisito no exige haber aprobado antes: acompaña, no precede. Sus datos (ver «Datos de una regla») usan la misma expresión de materias que un prerrequisito, y comparten la regla de las materias externas (ver «Materia externa»). Se evalúa por certeza como toda restricción (S18); el cálculo lo aplica igual que un prerrequisito y el plan ubica siempre los correquisitos de las materias que programa.

## Crédito

Unidad con la que el plan de estudios mide la carga de una materia. Los créditos se suman por semestre, por componente y por agrupación.

## Cuadro de prerrequisitos

Representación visual de todas las materias de un programa y de sus prerrequisitos, correquisitos y reglas de habilitación, como un grafo: cada materia es un nodo y cada prerrequisito o correquisito, una flecha; las alternativas «o» y las reglas de habilitación (por ejemplo, un mínimo de créditos aprobados) se distinguen. No tiene por qué verse igual que la malla estándar. Lo ofrece UC06 (Consultar regla del programa y su acuerdo) para el programa consultado; desde él se pueden ver las restricciones de una materia (UC05). Cuando un estudiante consulta su propio programa, cada materia muestra además su estado en el avance. No muestra materias externas (ver «Materia externa»); las materias no cursables desde el programa aparecen con esa señal.

## Equivalencia

Regla del plan por la que quien aprobó una materia (o un conjunto de materias) se entiende que aprobó otra: por ejemplo, las que reglamente el Consejo de Facultad en el plan de transición que ordena el Acuerdo 003-A de 2022 del Consejo Académico (art. 12). El administrador la registra con UC07 (ver «Datos de una regla»). El cálculo y el avance la aplican sin que el estudiante haga nada: una marca de la materia de origen, aprobada o en curso, cuenta como la materia de destino con ese mismo estado (si el origen son varias materias, cuando todas lo están), y la de origen no se cuenta por separado. RutaUN aplica solo las equivalencias que una norma establece; no decide equivalencias por su cuenta.

## Libre elección

Componente del plan de estudios cuyos créditos el estudiante cubre con materias de su elección. RutaUN no individualiza esas materias en el avance ni en el plan: registra créditos aprobados o en curso (UC03, Registrar avance en la malla) y los reparte en bloques (ver «Bloque de libre elección»). Si la norma lista electivas en una agrupación del componente —el art. 2 del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería, por ejemplo, lista en la agrupación Profundización, que exige cero créditos, «Práctica estudiantil I» y «Teoría de grafos», entre otras—, el administrador las registra con UC07 como cualquier materia, con sus prerrequisitos y reglas de habilitación, y RutaUN las muestra en la malla (UC03 y UC06) y explica sus restricciones (UC05). En UC03 son solo de consulta: sus créditos se registran como créditos de libre elección. El plan tampoco las distingue: sigue reservando bloques de créditos. Si la malla estándar muestra una materia concreta como libre elección (la de Ingeniería de Sistemas y Computación muestra la «Cátedra nacional de inducción y preparación para la vida universitaria» en el semestre I), cuenta como créditos de libre elección sugeridos en ese semestre. No recomienda qué cursar. Un programa puede no tener componente de libre elección: en ese caso no hay bloques ni se registran créditos de libre elección.

## Malla

Representación del plan de estudios de un programa: sus componentes y, dentro de ellos, sus agrupaciones y subagrupaciones con sus materias obligatorias y optativas (y, en el de libre elección, las electivas que liste la norma), sus créditos, sus prerrequisitos y, si lo tienen, su semestre sugerido en la malla estándar. Es lo que el estudiante ve al registrar su avance en UC03 (Registrar avance en la malla) y lo que cualquier visitante puede ver al consultar un programa en UC06 (Consultar regla del programa y su acuerdo).

## Malla estándar

Distribución por semestres que sugiere el plan de estudios de un programa, la que la Universidad publica como su malla curricular (la que el entrevistado raya en PDF): el semestre sugerido de cada materia que muestra y los créditos de libre elección sugeridos en cada semestre. Muestra todas las obligatorias y, por cada optativa que sugiere cursar, una optativa concreta en lugar de un hueco genérico: la de Ingeniería de Sistemas y Computación, por ejemplo, muestra «Cálculo diferencial» en el semestre I, aunque su subagrupación admite otra optativa. Esa optativa representa a su agrupación (la más interna que la contiene): el avance y los planes se comparan con los créditos que la malla sugiere en esa agrupación hasta cada semestre, no con esa optativa en particular. Si una malla muestra créditos de una agrupación sin nombrar una materia, se registran como créditos sugeridos de esa agrupación en ese semestre. El administrador de normativa la registra con UC07 (Incorporar acuerdo al consolidado del programa), como parte de los datos de las reglas de materia, de agrupación y de libre elección del acuerdo del plan de estudios (ver «Datos de una regla»). **No es una regla del plan:** no obliga al estudiante ni cambia el cálculo del plan, salvo para fijar el orden del consolidado (ver «Consolidado normativo»). Sirve para comparar el avance (ver «Atraso, adelanto y cambio de alternativa») y los planes (ver «Comparación con la malla estándar») con lo que la Universidad sugiere. Si al programa le falta algún dato de la malla estándar, RutaUN no hace las comparaciones que dependen de él y lo dice.

## Malla utilizable

Malla que tiene al menos un componente con materias y en la que toda materia tiene código, créditos y un componente o una agrupación que la contiene. Si no lo es, RutaUN no la muestra (UC03, Registrar avance en la malla; UC05, Entender restricciones de una materia; y UC06, Consultar regla del programa y su acuerdo) ni calcula planes (ver «Consolidado calculable»).

## Materia

Unidad del plan de estudios con nombre, código, créditos y restricciones (prerrequisitos y habilitaciones; en las normas se llama «asignatura»). El **catálogo** de materias es global a RutaUN: el código es la identidad y los créditos son **fijos**; cambiarlos crea otra materia, y el nombre vive en la regla de materia de cada versión del consolidado (puede cambiar sin cambiar el código). La coherencia del catálogo vale entre programas: cada código es una sola materia con unos mismos créditos. En cada agrupación (o componente) en que figura es obligatoria u optativa, y la contiene la agrupación más interna. Por lo general figura en una sola, pero una norma puede ubicarla en varias: el art. 2 del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería ubica «Matemáticas del aprendizaje de máquinas» (2028837) en las agrupaciones Computación Aplicada y Sistemas Inteligentes. Aun así cuenta para una sola: la que le asigna el avance (ver «Agrupación asignada»). Las materias que un prerrequisito menciona y que no figuran en la malla del programa son materias externas (ver «Materia externa»). Las materias de libre elección no se individualizan en el avance ni en el plan: se cuentan como créditos, aunque la norma liste algunas en una agrupación del componente (ver «Libre elección»).

## Materia externa

Materia que un prerrequisito del programa menciona y que no figura en la malla del programa, porque la norma que la fija no la regula: una materia de nivelación, que solo cursa quien la debe por su puntaje de admisión (por ejemplo, «Matemáticas Básicas», prerrequisito de «Cálculo Diferencial» en el art. 2 del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería), o una materia de otro programa, que el SIA no deja inscribir a los estudiantes del programa (por ejemplo, «Electrónica Análoga I», prerrequisito de «Electrónica Digital I», o «Líneas y Antenas», prerrequisito de «Comunicaciones», en el mismo artículo). El administrador la registra dentro de la regla de prerrequisito, indicando si es de nivelación o de otro programa (ver «Datos de una regla»). **RutaUN no la muestra en ninguna parte** —ni en la malla, ni en las restricciones de UC05, ni en la cadena de requisitos ni en el cuadro de prerrequisitos— y no se puede marcar en el avance. Al evaluar un prerrequisito, un término que es una materia de nivelación se da por cumplido y uno que es una materia de otro programa, por no cumplible: en una alternativa «o», esa rama no se muestra; si el prerrequisito no puede cumplirse sin una materia de otro programa, la materia que lo exige es una materia no cursable desde el programa (supuesto del equipo (17)).

## Materia no cursable desde el programa

Materia de la malla cuyo prerrequisito solo puede cumplirse con una materia de otro programa (ver «Materia externa»); en el Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería, por ejemplo, «Electrónica Digital I» y «Comunicaciones». Se muestra en la malla (UC03 y UC06), en el cuadro de prerrequisitos y en UC05 (Entender restricciones de una materia) con la señal «no se puede cursar desde este programa», sin nombrar la materia externa (UC05, extensión 4k). El cálculo del plan nunca la ubica ni la ofrece como opción de un espacio de optativa, y no cuenta para que una agrupación pueda cubrirse (ver «Consolidado calculable»). El estudiante sí puede marcarla como aprobada o en curso (por ejemplo, si la cursó en otro programa antes de un traslado), sin el aviso de prerrequisitos pendientes (UC03, extensión 3a), y entonces cuenta en su avance como cualquier otra.

## Optativa

Materia que el estudiante elige, entre las optativas de una agrupación o de un componente, para cubrir los créditos que exige. Forma parte de la malla. En el plan, las optativas que faltan aparecen como espacios de optativa, con sus opciones (ver «Espacio de optativa»), salvo las que el estudiante fija (ver «Optativa fijada»).

## Prerrequisito

Materia, o conjunto de materias combinadas con «y» y «o», que debe estar aprobada (o en curso, para efectos del plan) antes de cursar otra materia. Un prerrequisito con «y» se cumple si se cumplen todos sus términos; con «o», si se cumple alguno. Está «cumplida» si se cumple con materias aprobadas; «en curso» si solo se cumple contando las que están en curso; «incierta» si algún término ya no rige sin equivalencia que lo traduzca (ver «Materia que ya no rige»); y «pendiente» en otro caso. Los resultados de los términos se combinan por certeza (S18): un «y» toma el peor y una «o», el mejor. La expresión puede mencionar materias externas (de nivelación o de otro programa), que RutaUN no muestra y evalúa como dice «Materia externa». En el cálculo del plan, una materia ubicada en un semestre anterior cuenta como aprobada para los siguientes (el plan supone que el estudiante aprueba lo planeado); lo mismo vale para las reglas de habilitación.

## Regla de habilitación

Condición distinta de un prerrequisito que el programa exige para cursar una materia: haber aprobado un número mínimo de créditos de un componente o del programa. El cálculo cuenta las marcas aprobadas o en curso y lo ubicado en semestres anteriores (incluidos los bloques de libre elección cuando la medida es del programa); de cada agrupación y de la libre elección cuenta solo hasta lo que exigen, y las materias que ya no rigen cuentan como dice «Materia que ya no rige» (supuesto del equipo). Un porcentaje de los créditos del plan de estudios se registra como su equivalente en créditos del programa; una condición de otro tipo (por ejemplo, un promedio mínimo) se registra como regla informativa, que el plan no verifica.

## Restricción (de una materia)

Prerrequisito, correquisito o regla de habilitación que el programa exige para cursar una materia, según la versión vigente del consolidado. Cuando un estudiante consulta su propio programa, cada restricción tiene un estado **por certeza** (S18): cumplida, en curso, incierta o pendiente; una «y» toma el peor de sus términos y una «o», el mejor. UC05 (Entender restricciones de una materia) las muestra todas, sin nombrar las materias externas (ver «Materia externa»), cada una con su estado, y explica las que están «en curso» o «inciertas» con los avisos de sus extensiones. Las agrupaciones y la carga por periodo no son restricciones de una materia.

