---
title: "Glosario 2 · Normativa, reglas y consolidado"
doc_type: "glosario"
srs_ids:
  - Acuerdo (o norma)
  - Alcance
  - Carga máxima por periodo
  - Consolidado calculable
  - Consolidado normativo
  - Cupo de créditos
  - Datos de una regla
  - Fecha de aplicación
  - Fuente
  - Mínimo por periodo
  - Órganos
  - Programa
  - Regla
  - Reglas del plan y reglas informativas
  - Requisitos de grado
  - Tipo de efecto
  - Versión del consolidado
  - Vigencia
depends_on:
  - ../00-contexto.md
  - ../01-invariantes.md
  - ../04-supuestos.md
---
# Glosario 2 · Normativa, reglas y consolidado

> **Cuándo cargar:** antes de tocar UC07, la clasificación de reglas del plan vs informativas, la vigencia, la fuente, el alcance o el consolidado.

> Términos: **18** de 71 del glosario; texto verbatim (solo se renivela el encabezado `###` → `##`). Índice completo en [README.md](README.md).

## Acuerdo (o norma)

Término genérico para cualquier norma que se incorpora al consolidado: un acuerdo, una resolución o una circular. Se identifica por su tipo (acuerdo, resolución o circular), número, año y el órgano que la expide (por ejemplo, «Acuerdo 008 de 2008 del Consejo Superior Universitario»). Una norma puede contener varias reglas y tener efectos distintos (ver «Tipo de efecto»). En los siete casos de uso (UC01 a UC07), «acuerdo» y «norma» significan lo mismo e incluyen siempre a las resoluciones y a las circulares; cuando se habla del documento de tipo acuerdo, se dice «de tipo acuerdo».

## Alcance

Programas a los que aplica una norma según la propia norma: todos los programas de pregrado de la Sede, los de una o varias facultades, o uno o varios programas, con las exclusiones que ella establezca (la Circular 11 de 2026, por ejemplo, exime a los programas de Medicina y de Medicina Veterinaria). La granularidad la fija cada norma y el administrador la verifica en su texto: un Consejo de Facultad, por ejemplo, puede expedir una norma para un solo programa de su facultad. Si la norma no dice a qué programas aplica (como el Acuerdo 008 de 2008 del Consejo Superior Universitario), RutaUN supone que aplica a todos los programas sobre los que tiene competencia el órgano que la expide: un órgano del nivel nacional o de la Sede, a todos; un Consejo de Facultad, a los de su facultad (supuesto del equipo). RutaUN registra el alcance con su tipo (todos, facultades o programas) y con el texto con que la norma lo fija, y aplica la norma solo a los programas cargados que ese alcance comprende: cada uno de ellos es un «programa del alcance» y recibe la norma en su consolidado. Los programas que la norma comprende y que no están cargados no se ven afectados; si después se cargan, el sistema le lista la norma al administrador como pendiente (ver «Programa»). Una norma se registra una sola vez en RutaUN, con su identificación, su alcance y su enlace a Régimen Legal, si existe: incorporarla después a otro programa usa ese mismo registro, sin volver a digitarla (UC07, extensión 4c), y lo ya registrado no se cambia en UC07 (un error de la norma se corrige incorporando una norma que la modifique; uno de digitación o un enlace, como dice «Administrador de normativa»). Una norma posterior que cambia reglas de un programa es otra norma, de efecto modificatorio, aunque la expida el mismo órgano y para el mismo programa. Una regla que otra modifica, deroga o exceptúa se identifica por su norma, su artículo o numeral y su objeto (ver «Regla»); cuando la norma posterior sustituye un artículo o numeral completo («el artículo X quedará así»), remite a todas sus reglas. El sistema las ubica en el consolidado de cada programa del alcance (si no las encuentra en alguno, extensión 6b de UC07).

## Carga máxima por periodo

Máximo de créditos por periodo que fija una norma. La Circular 11 de 2026 fijó 23 créditos como máximo para la inscripción inicial de 2026-2, pero solo hasta la primera semana del periodo, y dice que se estudia una regulación permanente; por eso es informativa y RutaUN no supone ningún máximo permanente. Si una regla del plan fija una carga máxima por periodo, ningún semestre del plan, desde el 1, la supera.

## Consolidado calculable

Un consolidado permite calcular un plan cuando se cumple todo lo siguiente: (1) la malla es utilizable (ver «Malla utilizable»); (2) toda agrupación puede cubrirse: sus obligatorias y los créditos exigidos de sus subagrupaciones no superan lo que exige, y sus optativas, sin contar las no cursables desde el programa (ver «Materia no cursable desde el programa»), alcanzan para lo que falta; (3) ningún prerrequisito ni correquisito es circular ni apunta a una materia del programa que no rige (las materias externas se evalúan como dice «Materia externa»); (4) todo componente con créditos exigidos puede cubrirse del mismo modo (el de libre elección, siempre); (5) ninguna regla del plan está «en revisión» (ver «Regla»); (6) el periodo en curso está registrado (ver «Periodo en curso»). Las condiciones 1 a 5 se evalúan con las reglas vigentes en la versión del consolidado; UC07 (Incorporar acuerdo al consolidado del programa) las comprueba además como quedarían cuando empiecen a regir las reglas programadas. Si alguna condición falla, los casos de uso que calculan un plan (UC01, Generar plan de carrera; UC02, Replanificar plan de carrera; y UC04, Comparar alternativas de carga) no calculan, informan al estudiante y registran un incidente.

## Consolidado normativo

Conjunto de las reglas de un programa (entre ellas, las que definen sus componentes, agrupaciones y materias), cada una con el acuerdo del que sale, más la malla estándar del programa (ver «Malla estándar»). Tiene una versión y una fecha de última actualización. Fija el **orden del consolidado**, que el cálculo usa para desempatar: el de la malla estándar, por semestre sugerido (para una optativa que la malla no muestra, el semestre más temprano en que la malla muestra una optativa de su agrupación); a igual semestre, primero las materias que la malla muestra; luego por código, comparado como texto carácter a carácter; todo con los valores vigentes en la versión que se usa. Las materias que quedan sin semestre (las de agrupaciones de las que la malla no muestra ninguna optativa) van al final, por código. Lo carga el administrador de normativa con UC07 (Incorporar acuerdo al consolidado del programa); RutaUN no consulta Régimen Legal ni el SIA.

## Cupo de créditos

Créditos que cada estudiante de pregrado puede inscribir durante su permanencia: los del plan de estudios más un cupo adicional (Acuerdo 008 de 2008, art. 11). Quien no dispone de cupo suficiente para inscribir lo que le falta pierde la calidad de estudiante (art. 44, numeral 4). RutaUN no lo calcula ni lo verifica.

## Datos de una regla

Lo que el administrador registra de cada regla en UC07 (Incorporar acuerdo al consolidado del programa) para que el cálculo pueda aplicarla y para que los visitantes puedan consultarla.
- **De toda regla:** su enunciado (el texto de la norma o un resumen fiel de él, que es el que muestran UC05, Entender restricciones de una materia, y UC06, Consultar regla del programa y su acuerdo), su categoría, el objeto al que se refiere, su artículo o numeral de la norma (ver «Fuente») y, si la norma le fija una vigencia propia, su fecha de aplicación y su «vigencia hasta» (ver «Vigencia»).
- **Además, según su categoría:**
  - **Materia:** código, nombre, créditos, si es obligatoria u optativa, el componente o la agrupación que la contiene (la más interna) y, si la malla estándar la muestra, su semestre sugerido (también para una optativa); si la norma ubica la materia en varias agrupaciones, se registra una regla de materia por cada una, con el mismo código, nombre y créditos.
  - **Prerrequisito:** la materia y una expresión con «y» y «o» sobre materias del programa y, si la norma las menciona, materias externas, cada una con su nombre (y su código, si la norma lo da) y la indicación de si es de nivelación o de otro programa (ver «Materia externa»).
  - **Correquisito:** la materia y una expresión con «y» y «o» sobre materias que deben cursarse en el mismo semestre que ella o antes; comparte con el prerrequisito la forma de la expresión y la regla de las materias externas (ver «Correquisito» y «Materia externa»).
  - **Agrupación:** nombre, el componente o la agrupación que la contiene, créditos exigidos y, si la norma lo fija, su desglose en créditos obligatorios y optativos (como el art. 5 del Acuerdo 003-A de 2022 del Consejo Académico); si la malla estándar muestra créditos de la agrupación sin nombrar una materia, los créditos sugeridos en cada semestre.
  - **Componente y libre elección:** créditos exigidos y, si la norma lo fija, su desglose en créditos obligatorios y optativos; para la libre elección, los créditos sugeridos en cada semestre por la malla estándar, si los indica (incluidos los de una materia concreta que la malla muestre como libre elección).
  - **Programa** (categoría «componentes y créditos»): créditos exigidos del plan de estudios.
  - **Equivalencia:** la materia o las materias de origen y la materia de destino (ver «Equivalencia»).
  - **Mínimo o máximo por periodo:** número de créditos.
  - **Regla de habilitación:** materia, medida (créditos aprobados de un componente o del programa) y umbral en créditos (ver «Regla de habilitación»).
  - **Requisitos de grado y categoría «otra»:** ningún dato más; son siempre informativas.
- En cualquier regla informativa bastan el enunciado, la categoría, el objeto y el artículo o numeral; los datos numéricos son opcionales (el enunciado de la Circular 11 de 2026, por ejemplo, dice que el máximo de 23 créditos se aplica solo a la inscripción, hasta la primera semana de 2026-2).

## Fecha de aplicación

Desde cuándo rige una norma según ella misma: un periodo académico (por ejemplo, «a partir del primer período académico de 2009», en el Acuerdo 008 de 2008, art. 61) o una fecha (por ejemplo, su publicación en Régimen Legal). Si es un periodo, la norma empieza a regir cuando el administrador avanza el periodo en curso a ese periodo (ver «Periodo en curso»); si es una fecha, al comenzar ese día (hora de Colombia). Si al incorporarla ya llegó (el periodo es el periodo en curso o uno anterior, o la fecha ya pasó), rige desde que se incorpora. Una norma cuya fecha de aplicación aún no llega está «programada» (ver «Vigencia»).

## Fuente

Referencia con la que se ubica una regla en el documento oficial: identificación de la norma, artículo o numeral y, cuando existe, enlace a Régimen Legal (una norma recién expedida y aún no publicada puede no tenerlo). La fecha de aplicación y la vigencia no forman parte de la fuente: se muestran junto a ella. En UC07 la norma se registra con su identificación y su enlace a Régimen Legal (si existe); el texto oficial expedido por el órgano o publicado en Régimen Legal **no se guarda en RutaUN** (al visitante se le muestra la fuente de cada regla y el enlace, si existe), y cada regla se ubica en la norma con su artículo o numeral, registrado tal como lo rotula la norma y con lo que haga falta para distinguirlo: el Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería tiene dos «Artículo 2», el segundo rotulado «(Transitorio)», así que las reglas de este se ubican en «art. 2 (transitorio)»; también puede precisar el parágrafo, el literal o el cuadro. **Toda regla debe tener fuente** (UC07 no acepta reglas sin ella); si por un error de datos una regla aparece sin fuente, RutaUN la muestra como «sin fuente registrada» y registra un incidente. RutaUN no comprueba que los enlaces funcionen: depende de que los estudiantes con sesión reporten los que no abren («Reportar enlace roto»). RutaUN no es una fuente oficial.

## Mínimo por periodo

Número mínimo de créditos que el estudiante debe inscribir en cada periodo: diez (Acuerdo 008 de 2008, art. 10), salvo autorización del Consejo de Facultad. Es regla del plan, pero no excluye planes: el cálculo procura que cada semestre lo alcance (criterio 2 del «Cálculo del plan») y los semestres que no lo alcanzan (por el tope, por los prerrequisitos o por ser el último con pocos créditos por cursar) llevan marca de autorización; el semestre 0 no se evalúa. El plan usa en todos sus semestres el mínimo vigente al calcularlo. Las excepciones temporales al mínimo, como la Resolución 057 de 2026, son informativas: el plan sigue exigiendo el mínimo vigente.

## Órganos

Órganos y dependencias de la Universidad que expiden las normas, proponen sus cambios, deciden casos particulares o verifican su cumplimiento.
- **Expiden normas:** el Consejo Superior Universitario (CSU) y su Comisión Delegataria (normas generales, como el Acuerdo 008 de 2008 y la Resolución 057 de 2026) y la Vicerrectoría Académica (medidas académicas, como la Circular 11 de 2026).
- **Intervienen en el acuerdo de cada plan de estudios** (lo proponen, aprueban y expiden): los Comités Asesores, las Direcciones de Área Curricular de los programas, los Consejos de Facultad y el Consejo Académico.
- **Deciden casos particulares:** los Consejos de Facultad autorizan cargas inferiores al mínimo (Acuerdo 008 de 2008, art. 10, parágrafo) y deciden las homologaciones (acta ZEUS-01, sección 3); las pruebas de validación las programa la Unidad Académica que ofrece la materia y las califica un jurado de sus profesores (art. 29).
- **Verifican su cumplimiento:** por ejemplo, para 2026-2, la Resolución 057 de 2026 encargó a la Dirección Nacional de Información Académica, Registro y Matrícula (DINARA) verificar la carga mínima (art. 1, numeral 4).
- Las normas se publican en el Sistema de Información Normativa, Jurisprudencial y de Conceptos «Régimen Legal» de la Universidad. Son interesados fuera de escena: no usan RutaUN, pero sus decisiones deben quedar bien representadas.

## Programa

Programa curricular de pregrado de la Sede Bogotá, con su plan de estudios (por ejemplo, Ingeniería de Sistemas y Computación, cuyo plan de estudios fija el Acuerdo 003-A de 2022 del Consejo Académico). En RutaUN, un programa es la unidad que tiene un consolidado y una **versión del plan de estudios** (un número que solo cambia cuando una norma toca reglas de estructura): una reforma del plan crea una versión nueva del mismo programa y la más reciente rige para todos sus estudiantes; la convivencia de versiones por cohorte queda fuera de alcance. Lo crea el administrador de normativa fuera de UC01 a UC07 (nombre, código y facultad) y queda «cargado» desde entonces. Al crearlo, el sistema le lista las normas ya registradas cuyo alcance lo comprende (por ejemplo, el Acuerdo 008 de 2008), que el administrador le incorpora con UC07 (extensión 4c); mientras falte alguna, la sigue listando como pendiente. Solo puede usarse en UC01 a UC06 desde que se le incorpora su norma base: el acuerdo de su plan de estudios (no debe confundirse con el tipo de efecto «base»), que debe ser la primera norma que se le incorpore, porque UC07 no acepta una incorporación que deje al programa sin una malla utilizable. Como esta versión no guarda incorporaciones a medias, cada incorporación se hace en una sola sesión (supuesto del equipo); importar reglas desde un archivo queda como posible mejora. Es un dato del sistema, no un actor. El uso concurrente de dos versiones del plan de estudios por cohortes distintas queda fuera de alcance (supuesto del equipo). Cada estudiante tiene asociado un solo programa a la vez: la doble titulación queda fuera de alcance.

## Regla

Enunciado del consolidado sobre un programa (por ejemplo, los créditos exigidos por componente, un prerrequisito, una agrupación o el mínimo por periodo), con su fuente, su vigencia y su alcance. Es una regla del plan o una regla informativa. Toda regla tiene una **categoría** (componentes y créditos, materias, agrupaciones, prerrequisitos y habilitación, equivalencias, libre elección, carga mínima o máxima por periodo, requisitos de grado u otra) y un **objeto**: la materia, el componente, la agrupación o el parámetro por periodo al que se refiere (en una regla de materia, la materia en la agrupación o el componente que la contiene, porque una materia puede figurar en varias agrupaciones; en una equivalencia, sus materias de origen); los datos que se registran de cada una están en «Datos de una regla». **Una regla fija un solo tipo de dato sobre un solo objeto**, así que un mismo artículo o numeral suele contener muchas reglas: el art. 2 del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería fija en un solo cuadro las materias de Ingeniería de Sistemas y Computación, sus prerrequisitos, sus reglas de habilitación y los créditos exigidos de sus agrupaciones y subagrupaciones, y una sola celda de ese cuadro puede contener dos reglas («Haber aprobado 40 créditos del Componente de Formación Disciplinar o Profesional; y; Gerencia y Gestión de Proyectos o Diseño, Gestión y Evaluación de Proyectos» es una regla de habilitación y una de prerrequisito de la misma materia). Una regla se identifica por su norma, su artículo o numeral y su objeto con su tipo de dato (por ejemplo, «Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería, art. 2, prerrequisito de 2016701 Ingeniería de Software I»).
**Mismo objeto y contradicción:** dos reglas son sobre el mismo objeto si se refieren a la misma materia (en las de tipo materia, en la misma agrupación o componente), componente, agrupación o parámetro por periodo y son del mismo tipo de dato (materia, prerrequisito, habilitación, agrupación, equivalencia, créditos de componente o de libre elección, mínimo o máximo por periodo). Dos reglas sobre el mismo objeto, con vigencias que se solapan y valores distintos, **se contradicen** si ninguna modifica, deroga o exceptúa a la otra (una norma puede hacerlo con reglas de otra norma o de la misma, por ejemplo en un régimen de transición); las reglas de habilitación de una materia no se contradicen entre sí (se exigen todas) y, en un mínimo o máximo por periodo, el solape se mide con los periodos a los que aplica cada una dentro de su vigencia. UC07 (Incorporar acuerdo al consolidado del programa) no permite incorporar reglas que se contradigan (extensión 6c); la señal **«en revisión»** cubre las contradicciones que lleguen al consolidado fuera de UC07 o por un error del sistema. RutaUN no supone cuál rige: las señala «en revisión», con sus dos fuentes, y registra un incidente. Si una de ellas es regla del plan, el consolidado deja de ser calculable hasta que el administrador la corrija fuera de UC01 a UC07 (ver «Administrador de normativa»); si las dos son informativas, no impide calcular.

## Reglas del plan y reglas informativas

**Reglas del plan:** las que el cálculo aplica. Todo plan cumple los créditos exigidos por componente, los prerrequisitos, las reglas de habilitación, las agrupaciones, las equivalencias, la libre elección y la carga máxima por periodo que fije una regla del plan. Son las vigentes al calcular el plan, y el plan las supone vigentes en todos sus semestres. El mínimo por periodo también es regla del plan, pero no excluye planes: el cálculo procura cumplirlo (criterio 2 del «Cálculo del plan») y marca los semestres que no lo alcanzan.
**Reglas informativas:** el cálculo no las aplica; se muestran junto a los planes (UC01, Generar plan de carrera; UC02, Replanificar plan de carrera; y UC04, Comparar alternativas de carga) y en las consultas (UC05, Entender restricciones de una materia, y UC06, Consultar regla del programa y su acuerdo). Son todas las reglas temporales, por su norma o por su propia «vigencia hasta» (el plan no puede suponerlas vigentes en todos sus semestres), los requisitos de grado, el cupo de créditos y las reglas que el cálculo no puede aplicar a un semestre completo o cuya condición RutaUN no puede verificar (por ejemplo, el máximo de la Circular 11 de 2026 y la exención de la Resolución 057 de 2026, que además son temporales).
**Criterio de clasificación.** El administrador de normativa clasifica cada regla al incorporarla con UC07 (Incorporar acuerdo al consolidado del programa): es **regla del plan** si el cálculo puede aplicarla a un semestre completo y suponerla vigente en todos los semestres (prerrequisitos, habilitación, créditos, agrupaciones, equivalencias, libre elección, o un mínimo o máximo de créditos por periodo) y no es temporal. Es **informativa** si es temporal (ella o su norma tiene «vigencia hasta»), si no se refiere a materias ni créditos del plan (requisitos de grado, cupo de créditos), si lo que exige solo se aplica durante una parte de cada periodo al que se refiere (como el máximo de la Circular 11, que solo limita la inscripción inicial, hasta la primera semana) o si prevé una exención cuya condición RutaUN no puede verificar (como la de la Resolución 057). Que una norma esté programada no la vuelve informativa: sus reglas del plan se aplican desde que empiezan a regir (ver «Vigencia»).

## Requisitos de grado

Condiciones para graduarse distintas de aprobar materias y créditos (por ejemplo, un requisito de idioma, si el programa lo exige). Son reglas informativas: el plan no las verifica. Por eso el plan estima la culminación del plan de estudios, no el grado (ver «Culminación del plan de estudios»).

## Tipo de efecto

Efecto de una norma sobre el consolidado.
- **Base:** establece reglas del programa (por ejemplo, el acuerdo del plan de estudios o el Acuerdo 008 de 2008).
- **Modificatoria:** cambia o deroga reglas de una norma anterior.
- **Excepción o medida temporal:** exceptúa una regla, o añade una regla, solo durante uno o varios periodos o hasta una fecha, por lo que **siempre tiene «vigencia hasta»** (por ejemplo, la Resolución 057 de 2026 exceptúa el artículo 10 del Acuerdo 008 de 2008 durante 2026-2; la Circular 11 de 2026 añade un máximo temporal de créditos); sus reglas son siempre informativas (ver «Reglas del plan y reglas informativas»).

No debe confundirse con el «tipo» que identifica a la norma como documento (acuerdo, resolución o circular).

## Versión del consolidado

Número que identifica el estado del consolidado de un programa: todas sus normas incorporadas, con cada regla y su estado (vigente, programada o ya no rige), y su malla estándar. Crean una versión nueva sobre la más reciente: cada incorporación confirmada con UC07 (Incorporar acuerdo al consolidado del programa); cada corrección que el administrador hace fuera de UC01 a UC07; y, automáticamente y con las mismas normas, cada vez que una regla del programa empieza o deja de regir: cuando el administrador avanza el periodo en curso o, si la fecha de aplicación o la «vigencia hasta» es una fecha, al comenzar ese día (hora de Colombia) o en cuanto el sistema esté disponible, una sola por programa y día (supuesto del equipo). Los planes calculados antes que aplican una regla que cambió en la versión nueva se señalan como desactualizados (ver «Plan desactualizado»). La versión automática no incorpora normas ni cambia reglas: la actualización automática del consolidado cuando cambian los acuerdos sigue fuera de alcance. Si al incorporar una norma su fecha de aplicación o su «vigencia hasta» ya llegaron, no se crea ninguna versión adicional por ellas. La versión vigente es la más reciente; las anteriores se conservan. «Consolidado vigente» y «malla vigente» se refieren a la versión vigente.

## Vigencia

Lapso en el que una norma, y con ella sus reglas, rige: desde su fecha de aplicación hasta su «vigencia hasta» (un periodo o una fecha) o sin fin. Cuando la «vigencia hasta» es un periodo, la norma deja de regir cuando el administrador avanza el periodo en curso más allá de ese periodo; cuando es una fecha, al terminar ese día (hora de Colombia). La vigencia de cada regla es la de su norma, salvo que la norma le fije una propia —su propia fecha de aplicación o su propia «vigencia hasta», que funcionan igual que las de una norma; por ejemplo, el art. 2 (transitorio) del Acuerdo 11 de 2023 del Consejo de Facultad de Ingeniería suspende por tres periodos académicos el prerrequisito «Pensamiento Sistémico» de «Ingeniería de Software I», «Sistemas de Información» y «Sistemas de Información Gerencial»— o que otra regla la modifique o la derogue. Si la norma cuenta esa vigencia desde un hecho que su texto no fecha (en ese ejemplo, el periodo en que el acuerdo se implementó en el SIA), el administrador registra el periodo que conste en la información oficial con que cuente y lo indica en el enunciado (supuesto del equipo). Cuando otra regla la modifica o la deroga, deja de regir cuando empieza a regir la que la modifica o la deroga. Una excepción no reemplaza a la regla que exceptúa: la regla exceptuada sigue vigente.
**Uso en el plan.** El plan se calcula con las reglas del plan vigentes en la versión del consolidado que el caso de uso que calcula (UC01, UC02 o UC04) cargó en su paso 2, y supone que seguirán rigiendo, igual, en todos sus semestres, porque RutaUN no puede saber si la normativa cambiará ni cuándo (supuesto del equipo). **Las reglas temporales y las programadas no cambian el cálculo:** el plan las muestra en el semestre cuyo periodo cubren o en el que empiezan o dejan de regir, para que el estudiante esté pendiente de ese semestre; si su fecha de aplicación o su «vigencia hasta» es una fecha y no un periodo, el plan las muestra para todo el plan, con esa fecha, porque RutaUN no sabe en qué periodo cae. Cuando una regla programada empieza a regir, el sistema crea una versión nueva del consolidado y los planes calculados antes quedan desactualizados si aplican esa regla (ver «Plan desactualizado»).
**Estados.** En la consulta, cada regla tiene un estado: «vigente», «programada» (aún no empieza a regir) o «ya no rige» (su vigencia terminó, porque la norma venció o porque otra regla la modificó o la derogó).
**Temporal.** Una regla es temporal si tiene «vigencia hasta», propia o de su norma (a una norma con «vigencia hasta» se la llama norma temporal), sea cual sea el tipo de efecto de su norma: toda excepción o medida temporal la tiene (ver «Tipo de efecto») y una norma de efecto base o modificatorio puede tenerla. Es otro dato, aparte del estado: una regla puede estar vigente y ser temporal. Las reglas temporales nunca son reglas del plan (ver «Reglas del plan y reglas informativas»).

