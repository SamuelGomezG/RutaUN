# 0001 · Motor del cálculo del plan (TT-05)

Fecha: 2026-11-03 · Estado: **aceptada con reservas**

## Pregunta

¿Una búsqueda exacta sobre un modelo compacto calcula el plan óptimo según los
seis criterios del «Cálculo del plan» sobre la malla real de Ingeniería de
Sistemas y Computación en menos de 30 s (S3), también cinco veces seguidas
(UC04)?

## Respuesta

**No para una búsqueda exacta sin límite.** En los casos curados la búsqueda en
profundidad no expandió ningún nodo (`nodos=0`): el plan lo encontró el
constructor testigo. Y como único oráculo del criterio 4 tampoco sirve:
demostrar que una optativa no cabe obliga a agotar el espacio, y E1/T2, que
calcula en ~20 ms, agotaba el plazo (3,5 millones de nodos sin terminar).

**Sí para un híbrido de cotas, constructor testigo y búsqueda acotada.** En 20
de los 25 pares escenario–tope calcula en menos de 1,1 s (mediana) un plan que
pasa el verificador: T1–T4 para E0–E3, T2–T4 para E4 y T5 solo en E3. k, c2 y c3
están demostrados (k coincide con la cota inferior en todos); c4–c6 salen de
heurísticas deterministas sin prueba de optimalidad. Quedan sin calcular
E0/T5, E1/T5, E2/T5, E4/T1, E4/T5 y la variante F (tiempo agotado). El estrés de
malla duplicada no se hizo.

## Enfoques comparados

- **Búsqueda exacta propia:** profundización por semestres, cotas, simetrías,
  memoización de fallidos y fijación sucesiva. Implementada en `Buscador` y
  `CalculoDelPlan`.
- **Constructor testigo:** plan codicioso en orden del consolidado, con
  prerrequisitos, correquisitos, habilitaciones, agrupaciones y bloques. Se usa
  como prueba de existencia y como punto de partida de la fijación sucesiva.
- **Mejora local verificada** para c5 y c6 (`PlanOptimizador`).
- **CP-SAT por etapas:** no se probó; no se agregó la dependencia nativa.

## Qué hizo cada parte

El modelo compacto (índices, bitsets, CNF y máscaras de agrupaciones) y el
verificador independiente funcionan. La fijación sucesiva usa como oráculo de
existencia `buscar`: el constructor testigo primero y, solo si falla, la
búsqueda en profundidad con cancelación. Con él se fijan k, c2 y c3, que son
exactos mientras la búsqueda termine. Después:

- **c4, vía rápida:** decide el constructor cada inclusión de optativa y el plan
  es su reconstrucción final. Resuelve los casos curados, con las mismas huellas
  que antes de la segunda ronda.
- **c4, vía de rescate:** si el constructor no reconstruye el plan (antes era un
  falso «sin plan»), decide `buscar` con un tope determinista de 1000 nodos por
  inclusión y el plan es el último que devolvió. Una exclusión por rescate
  significa «no se halló plan con ese tope», no «no existe»: **c4 por rescate no
  está demostrado mínimo.**
- **c5 y c6:** mejora local verificada.

La búsqueda sí aporta en la vía de rescate. Inclusiones de optativas por cada
camino y exclusiones (todas por agotar el tope; ninguna demostrada):

| Caso | Por el constructor | Por la búsqueda | Exclusiones por tope |
| --- | ---: | ---: | ---: |
| E0/T1 | 15 | 8 | 48 |
| E0/T3 | 0 | 2 | 69 |
| E0/T4 | 7 | 0 | 64 |
| E1/T3 | 10 | 13 | 48 |
| E2/T3 | 0 | 24 | 47 |
| E4/T3 | 22 | 1 | 48 |

Los rescates exitosos necesitaron entre 13 y 79 nodos, salvo uno de E0/T3 con
17.543, que el tope de 1000 deja fuera (por eso E0/T3 incluye 2 y no 3 por la
búsqueda). El plan final siempre pasa `VerificadorDePlan`.

## Datos usados

- Malla: Acuerdo 003-A de 2022 (CA) y Acuerdo 11 de 2023 (CFI), transcritos el
  2026-11-03: 90 materias (17 obligatorias, 54 créditos), 12 agrupaciones con
  créditos y 19 subagrupaciones, 83 prerrequisitos, 14 habilitaciones, 0
  correquisitos, 3 externas, 2 no cursables. Cadena más larga: 7 materias.
- Orden del consolidado: malla estándar 2023 del programa.
- Escenarios: E0 a E4, topes T1 a T5 y las variantes F (materias fijadas), C
  (correquisito trivial), C2 (correquisito de un semestre posterior: 1000017
  exige 2016699), CC (cadena de correquisitos: 1000017 exige 2016699 y 2016699
  exige 2016697), D3 («o» de Sistemas de Información), IMP (ciclo de
  prerrequisitos: 2016699 exige 2025967) y ESC (ciclo con salida por «o»:
  2016699 exige 2025967 o 2016353). Todas, salvo F y D3, son sintéticas.
- Ambiente: AMD Ryzen 7 5825U, 16 hilos, 13 GiB de RAM, Ubuntu 24.04, OpenJDK
  21.0.12.1, con el equipo en corriente.

### Decisiones de transcripción

| # | Decisión del spike |
| --- | --- |
| D1 | Computación paralela y distribuida usa 2016722 (manda el Acuerdo 11 de 2023), no 2025968; conserva el semestre VIII de la malla |
| D2 | El programa vale 165 créditos (Acuerdo 003-A de 2022), no los 167 que imprime la malla |
| D3 | «Sistemas de Información» se lee como la materia 2025982, no como la subagrupación; la variante D3 prueba la lectura con «o» |
| D4 | Se aplica el prerrequisito Pensamiento sistémico; la suspensión transitoria no cambia las mediciones |
| D5 | «Trabajo de grado» de la malla son créditos de la agrupación; sus opciones quedan sin semestre y el criterio 4 elige 2016843 |
| D6 | 2028837, que está en dos agrupaciones, toma el semestre más temprano (VII) |
| D7 | La subagrupación del Taller usa el nombre del encabezado, no el de la línea de créditos |
| D8 | Los prerrequisitos se resuelven por nombre a código con los anexos; el JSON guarda códigos |
| D9 | «Profundización» no se modela; 2027313 entra solo en Criptografía. La libre elección son bloques de 3 créditos por materia, registrado como supuesto |
| D10 | Matemáticas Básicas es externa de nivelación y no suma a los 165 |
| D11 | La malla estándar se cita por el nombre del PDF y la fecha de transcripción; no es una norma |
| D12 | La puntuación de la regla de Ingeniería económica se lee como «Cálculo integral y una de las tres probabilidades» |

Las decisiones que tocan la semilla se registran en #92 y #100; el formato de
normativa en #93/DOC-05.

## Mediciones

CSV completo: `spikes/tt-05-motor/resultados/mediciones-2026-11-03T18-41-05.csv`
(`todos`). La corrida `curado` dejó además
`spikes/tt-05-motor/resultados/mediciones-2026-11-03T18-35-39.csv`. Cada caso
hizo 3 calentamientos y 20 medidas; los tiempos son milisegundos sin arranque
de la JVM, y la mediana de 20 medidas es el promedio de la décima y la
undécima. La huella del plan coincide entre las dos corridas para los 23 casos
comunes y es única dentro de cada caso. La columna «Vía c4» es `rápida` (decide
el constructor) o `rescate`.

Nota sobre los tiempos: la primera versión de la cancelación multiplicó el
tiempo por 5,5 al consultar el reloj en cada iteración de los bucles internos
(E1/T2: de 18 a 100 ms); ahora se consulta cada 256 llamadas y E1/T2 vuelve a
~17 ms. Los tiempos de la ronda anterior no son comparables con estos.

| Escenario | Tope | k | c2 | c3 | Opt. | Bloques | Vía c4 | Mediana | p95 | Máx. |
| --- | --- | ---: | ---: | ---: | ---: | ---: | --- | ---: | ---: | ---: |
| E0 | T1 | 8 | 0 | 78 | 23 | 4 | rescate | 165 | 180 | 185 |
| E0 | T2 | 10 | 0 | 78 | 23 | 4 | rápida | 26,5 | 31 | 37 |
| E0 | T3 | 14 | 0 | 78 | 23 | 10 | rescate | 1052 | 1076 | 1083 |
| E0 | T4 | 11 | 0 | 78 | 23 | 6 | rescate | 664,5 | 702 | 705 |
| E1 | T1 | 7 | 0 | 63 | 19 | 2 | rápida | 19 | 19 | 21 |
| E1 | T2 | 8 | 0 | 63 | 19 | 2 | rápida | 17 | 18 | 20 |
| E1 | T3 | 12 | 0 | 63 | 19 | 6 | rescate | 304,5 | 315 | 325 |
| E1 | T4 | 9 | 0 | 63 | 19 | 3 | rápida | 14 | 15 | 16 |
| E2 | T1 | 4 | 0 | 24 | 7 | 3 | rápida | 4 | 4 | 4 |
| E2 | T2 | 4 | 0 | 24 | 7 | 2 | rápida | 4 | 4 | 4 |
| E2 | T3 | 6 | 0 | 24 | 7 | 3 | rescate | 273 | 286 | 287 |
| E2 | T4 | 5 | 0 | 24 | 7 | 3 | rápida | 5 | 5 | 5 |
| E3 | T1 | 1 | 0 | 9 | 2 | 1 | rápida | 1 | 1 | 1 |
| E3 | T2 | 1 | 0 | 9 | 2 | 1 | rápida | 1 | 1 | 1 |
| E3 | T3 | 2 | 1 | 9 | 2 | 1 | rápida | 3 | 3 | 3 |
| E3 | T4 | 1 | 0 | 9 | 2 | 1 | rápida | 1 | 1 | 1 |
| E3 | T5 | 3 | 3 | 9 | 2 | 1 | rápida | 7 | 7 | 7 |
| E4 | T2 | 8 | 0 | 61 | 18 | 4 | rápida | 13 | 14 | 15 |
| E4 | T3 | 11 | 0 | 61 | 18 | 4 | rescate | 256 | 260 | 261 |
| E4 | T4 | 8 | 0 | 61 | 18 | 3 | rápida | 11 | 12 | 13 |
| V-C | T2 | 8 | 0 | 63 | 19 | 2 | rápida | 15 | 15 | 15 |
| V-C2 | T2 | 8 | 0 | 63 | 19 | 4 | rápida | 19 | 19 | 19 |
| V-CC | T2 | 8 | 0 | 63 | 19 | 4 | rápida | 15 | 16 | 16 |
| V-ESC | T2 | 8 | 0 | 63 | 19 | 2 | rápida | 13 | 14 | 16 |
| V-D3 | T2 | 10 | 0 | 78 | 23 | 4 | rápida | 19 | 20 | 20 |

Todos los planes calculados pasan el verificador. En todos, k coincide con la
cota inferior. E3/T3 con T3 tiene `c2=1`, la prueba de UC01 ext. 6a; E3/T5 con
T5 tiene `c2=k=3`. Los E0/T1, E0/T3, E0/T4, E1/T3, E2/T3 y E4/T3 son los casos
de rescate. Los tiempos de rescate (0,17 a 1,1 s) son los más altos porque cada
exclusión por tope agota sus 1000 nodos; con el oráculo sin tope esos casos
agotaban el plazo.

Casos que no terminan en calculado:

| Fase | Escenario | Tope | Resultado | Corridas | Mediana | Máx. |
| --- | --- | --- | --- | ---: | ---: | ---: |
| calentamiento | E0 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| calentamiento | E1 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| calentamiento | E2 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| calentamiento | E4 | T1 | tiempo agotado | 1 | 30000 | 30000 |
| calentamiento | E4 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| calentamiento | V-F | T2 | tiempo agotado | 1 | 30000 | 30000 |
| medida | V-IMP | T2 | sin plan (esperado, ver «Ciclos de prerrequisitos») | 20 | 0 | 0 |

La fase `calentamiento` indica que el primer calentamiento agotó el plazo y el
caso no se repitió; ninguno de esos seis llegó a medirse. En todos el plazo se
agotó dentro de la primera búsqueda de existencia (k igual a la cota), tras 2,6
a 6,7 millones de nodos sin hallar plan ni terminar de agotar el espacio.

**Por qué antes salían «sin plan».** E0/T1, E0/T3, E2/T3 y E4/T3 devolvían
`SinPlanPosible("No se pudo reconstruir el plan inicial")` después de haber
hallado un plan con k semestres; en UC01 eso es la ext. 5b y culpa al
consolidado. Había dos causas:

- **El constructor no resuelve la instancia** (E0/T1, E0/T3, E2/T3): k, c2 y c3
  los fijaba la búsqueda (entre 61 y 218 nodos), pero en c4 el único oráculo era
  el constructor, que falla con todas las inclusiones, así que se excluían las 71
  optativas y ya nada era factible.
- **Exclusiones posteriores al último testigo válido** (E4/T3): hubo un testigo
  válido (k=11, c3=61), pero se excluyeron 59 materias después y la
  reconstrucción final fallaba.

La vía de rescate resuelve ambas. E0/T4 y E1/T3, que antes agotaban el plazo,
también calculan ahora. Los fallos internos que aún puedan darse se devuelven
como `NoCalculado` (ver «Resultados del cálculo»); ninguno apareció.

UC04 sobre E1 con T1–T5, una corrida por tope. Duración de cada tarea, medida
dentro de la tarea:

| Tope | Secuencial (ms) | En paralelo (ms) | Resultado |
| --- | ---: | ---: | --- |
| T1 | 12 | 13 | calculado, k=7 |
| T2 | 12 | 14 | calculado, k=8 |
| T3 | 313 | 315 | calculado, k=12 (rescate) |
| T4 | 14 | 15 | calculado, k=9 |
| T5 | 30000 | 29999 | tiempo agotado |

El total secuencial fue de 30 354 ms y el paralelo, con hilos virtuales y
`CompletableFuture.orTimeout`, de 30 007 ms: ambos los fija el plazo de T5, y el
paralelo no gana nada mientras T5 siga sin terminar. El trabajo útil de T1–T4
suma unos 0,35 s. En la ronda
anterior el informe atribuía el tiempo de T4 en paralelo a una contención con
T3/T5; era un error de medición (`msTotal` se tomaba tras cada `join()` en
orden, de modo que T4 heredaba la espera de T3).

La cancelación se midió: E0/T5 termina en 30 s, no en 300 s. La prueba con plazo
de 2 s devuelve `TiempoAgotado` en menos de 2,5 s.

## Corrección

- **C1:** la transcripción pasa las comprobaciones del plan del spike: 165
  créditos, 15 + 36 y 39 + 42, 90 materias, 83 prerrequisitos, 14
  habilitaciones, 0 correquisitos, 3 externas, 2 no cursables y cadena de 7.
  La malla estándar adaptada pasa el verificador con T2; las tres mutaciones se
  rechazan por prerrequisito, habilitación y cobertura/libre elección.
- **Verificador:** se ejecutó sobre todos los planes calculados; no hay filas
  con `calculado` que tengan errores.
- **Pruebas:** 48 pruebas en verde con `verify`. La prueba de determinismo
  compara los vectores completos y construye un modelo nuevo en cada vuelta; la
  anterior usaba un centinela negativo y no comparaba nada. La huella del plan
  coincide entre procesos. Las pruebas de esta ronda se comprobaron por
  mutación: fallan contra el código que corrigen (ver abajo).
- **Cancelación:** la primera versión consultaba `System.nanoTime()` en cada
  iteración de los bucles internos y multiplicó por 5,5 el tiempo del cálculo
  (E1/T2: de 18 a 100 ms). Ahora `Cancelacion` lee el reloj en la primera
  llamada y luego cada 256; E1/T2 vuelve a ~20 ms y la latencia de cancelación
  sigue por debajo de 0,5 s (la prueba de 2 s termina en menos de 2,5 s).
- **Correquisitos:**
  - V-C era trivial porque 2016353 ya estaba aprobada en E1.
  - La primera V-C2 (2016353 con 2016698) también pasaba con el código anterior
    a la corrección: el codicioso viejo las dejaba juntas en el semestre 1 sin
    emparejarlas, con la misma huella que E1/T2 sin correquisito. La nueva V-C2
    es «1000017 exige 2016699» (semestres sugeridos 3 y 4; las dos se pueden
    cursar con lo aprobado en E1). Mutación: sobre `d27c516` (sin la corrección)
    la prueba `tt05_correquisitoDeUnSemestrePosteriorNoQuedaDespues` falla, y el
    motor de entonces respondía `SinPlanPosible`; sobre el código vigente da
    `calculado k=8` con el verificador en OK.
  - **Ciclo del constructor:** con correquisitos encadenados (M exige P y P
    exige Q, con Q todavía no cursable) el bucle de `ConstructorTestigo`
    agregaba P, lo quitaba y lo volvía a agregar sin fin; solo lo detenía el
    plazo. Ahora cada semestre lleva las materias descartadas y
    `buscarCorrequisito` no vuelve a proponerlas. Termina porque, en un semestre,
    cada materia entra como pareja a lo sumo una vez (al quitarla queda
    descartada) y se quita a lo sumo una vez: el bucle da como mucho 2·n vueltas.
  - **Variante CC.** El plan de correcciones proponía la cadena 2016353 →
    2016698 → 2025967, pero no tiene plan: 2025967 (obligatoria) exige 2016697,
    esta exige 2016698 (2016498 no es cursable) y la cadena obligaría a 2016698
    a cursarse con 2025967 o después, así que el ciclo es mixto
    (prerrequisito–correquisito). Se usa 1000017 → 2016699 → 2016697, que es
    realizable. Mutación: con el código anterior CC agota el plazo con `nodos=0`
    (atascado en el constructor); con el vigente calcula `k=8` en ~22 ms.
- **Ciclos de prerrequisitos:** `ModeloDeCalculo.profundidad` no marcaba las
  materias que estaba visitando, así que IMP (2016699 exige 2025967, que ya
  exige 2016699) terminaba en `StackOverflowError` en vez de informar la causa
  que pide la ext. 5b de UC01. Ahora marca la materia «en curso» y, si la
  recursión vuelve a una en curso, ese camino vale infinito (un camino circular
  nunca es el más corto). **No memoriza los valores contaminados por el
  ciclo:** si el valor de una materia dependió de otra que seguía en curso, se
  devuelve pero no se guarda; con «A exige (B o C)» y «B exige A», memorizar B
  como infinita dejaría la cota inferior por encima del mínimo real aunque A se
  alcance por C, y volverían los falsos «sin plan». ESC (2016699 exige 2025967
  o 2016353) lo prueba: calcula con el verificador en OK; memorizando los
  contaminados daría «sin plan». Con la cota infinita, el resultado nombra la
  primera materia requerida inalcanzable en el orden del consolidado: «La
  materia 2016699 (Estructuras de datos) no se puede alcanzar: prerrequisito
  circular o no cursable». En la malla real no hay ciclos; el costo extra solo
  aparece si los hay.
- **El 3 como dato:** los créditos por materia de libre elección salen de
  `libreEleccion.creditosPorMateria`; ya no quedan literales en el constructor,
  el optimizador ni los mensajes del verificador. Si el campo falta, la carga
  falla con un mensaje claro (un dato que falta no se inventa). Con el dato en 4,
  E1/T2 calcula y el verificador acepta el plan.
- **UC04 en paralelo:** `msTotal` se tomaba después de cada `join()` en orden, así
  que T4 heredaba la espera de T3, y el `catch` registraba cualquier excepción
  como tiempo agotado. Ahora cada tarea mide su duración dentro de la tarea, solo
  la `TimeoutException` se registra como `TiempoAgotado` y cualquier otra falla
  se relanza.
- **Revisión automática del PR (15 comentarios):** se verificó cada uno contra el
  código. Se corrigieron: `claveClase` (no incluía los correquisitos propios de la
  materia), `kMaximo` (no contaba los prerrequisitos opcionales), el Javadoc de
  `CalculoDelPlan` (prometía optimalidad), la validación de `Tope`, el doble
  conteo de una obligatoria fijada, el tamaño fijo de las máscaras (128 materias),
  la mediana de un número par de muestras, la etiqueta de las filas de
  calentamiento, el CSV único por corrida y las rutas del README. Ya estaban
  resueltos los dos de `Medir` sobre UC04 (`6429041`). No se aplicó el orden de
  `RevisarMalla` (desde `05d0500`, `profundidad` tolera ciclos) ni los dos que
  piden decisiones de diseño (asignación de agrupación guardada y
  `maximoPorPeriodo`); quedan en «Limitaciones».
- **Tercera revisión automática (6 comentarios):** se corrigieron cinco y se
  comprobó que no cambian ninguna de las 25 filas escenario–tope ni de las 5
  variantes publicadas (k, huella y vía de c4 idénticas). El reparto de bloques
  de libre elección ya no acepta un donante que deje a un receptor vacío con
  menos de un bloque válido; una obligatoria que está en varias agrupaciones
  suma sus créditos en la primera y no en todas (en la malla real no hay
  ninguna); la carga rechaza `creditosPorMateria` menor o igual a cero; el CSV
  reemplaza las comas y los saltos de línea del resultado; y la prueba de 150
  materias también cubre `todoCubierto`. No se corrigió `clausulasUtiles` (ver
  «Limitaciones»). El arreglo del reparto de bloques no tiene prueba dedicada: la
  función es privada y ningún plan medido cambió.
- **Oráculo de fuerza bruta:** no se implementó; queda para PRU-02 (#61). Por
  eso c4–c6 no tienen prueba exhaustiva de optimalidad.
- **Checkstyle:** se usa la copia local del proyecto, que omite las reglas de
  Javadoc; las supresiones también se limitan a Javadoc (0 violaciones con las
  demás reglas). Se corrigieron nombres (`D`, `L`, `DX`, `n`, `k`, `creditosX`,
  `materiasX`), líneas largas y distancias de declaración.

## Resultados del cálculo

`CalculoDelPlan` devuelve un resultado sellado de cuatro formas. Ninguna debe
confundirse con otra: cada una pide una respuesta distinta al estudiante.

| Resultado | Cuándo aparece | Extensión de UC01 |
| --- | --- | --- |
| `Calculado` | Hay plan: k, c2 y c3 demostrados y c4–c6 por la vía descrita arriba. Lleva `c4Aplicado` y `c4PorRescate` | Flujo normal |
| `SinPlanPosible` | De verdad no hay plan: la cota inferior es infinita (el mensaje nombra la materia inalcanzable, p. ej. un ciclo de prerrequisitos) o no existe plan hasta el máximo de semestres | 5b |
| `TiempoAgotado` | El plazo venció (30 s); el cálculo se cancela y no se muestra ningún plan | 5a |
| `NoCalculado` | Ya se demostró que existe un plan con k semestres, pero un paso posterior del motor no pudo fijar c2 o c3 o reconstruir el plan. Es un fallo del motor, no una conclusión sobre la normativa | Propuesta |

`NoCalculado` no aparece en ninguno de los casos medidos. El SRS no lo
contempla: **propuesta para el sprint 2** (laguna que va al PR de requisitos
aparte, regla de oro 3): tratarlo como la ext. 5a, sin plan parcial, con la
sugerencia de reintentar o probar otro tope, pero con un incidente técnico y no
de normativa, porque la causa es del motor y no del consolidado. Antes de esta
ronda esos fallos se devolvían como `SinPlanPosible` y producían falsos 5b: el
estudiante habría leído que el consolidado no tiene plan cuando sí lo tenía.

## Limitaciones

- k, c2 y c3 son exactos mientras la búsqueda termine; **c4–c6 no están probados
  óptimos.** El resultado es determinista y válido, pero no se probó que sea el
  óptimo lexicográfico. En particular, c4 por rescate excluye optativas por
  agotar un tope de 1000 nodos (hasta 69 exclusiones en un caso), no por haber
  demostrado que no caben. Por eso la lista de optativas elegidas puede no ser
  la lexicográficamente menor.
- No se completaron E0/T5, E1/T5, E2/T5, E4/T1, E4/T5 ni V-F (tiempo agotado en
  la primera búsqueda de existencia). T5 (8 créditos y 3 materias) y E4/T1 son
  topes estrechos; T1–T4 se completan en casi todo E0–E4, pero la respuesta
  «escala» no cubre los topes estrechos.
- `NoCalculado` no apareció en ninguna medición, así que su manejo solo está
  razonado, no ejercitado.
- El estrés de malla duplicada no se implementó (las máscaras ya escalan con la
  cantidad de materias: hay una prueba con 150).
- No se modelan materias que ya no rigen, restricciones inciertas ni reglas
  temporales, programadas o informativas. Los correquisitos y la habilitación
  con medida del programa solo se ejercitaron con datos sintéticos.
- Una materia aprobada que está en más de una agrupación toma la primera
  (`grupos[0]`). En la aplicación debe salir de la asignación que el estudiante
  guardó en su avance, y no elegirse una si no hay ninguna guardada; el esquema
  de escenarios del spike no la trae.
- **Completitud de la búsqueda.** `Seleccion.clausulasUtiles` solo marca como
  útiles los prerrequisitos de las materias *requeridas*, no los de una optativa
  que se elige para cubrir una agrupación. Si el único camino a los créditos de una
  agrupación pasa por un prerrequisito optativo cuya propia agrupación ya está
  llena, ni el constructor ni la búsqueda lo consideran y el motor podría
  responder que no hay plan (o no hallarlo). No se buscó un caso así de forma
  sistemática: los que calculan no lo necesitaron, pero eso no demuestra que
  no exista. No se corrigió porque el arreglo directo (marcar como
  útil todo prerrequisito de cualquier optativa pendiente) haría que el
  constructor eligiera optativas que luego el verificador rechaza por innecesarias
  y agrandaría el espacio de la búsqueda; un arreglo seguro debe razonar sobre las
  optativas *efectivamente elegidas*. Queda para el sprint 2 con PRU-02, que lo
  detectaría con mallas pequeñas.
- `maximoPorPeriodo` de la malla es `null` en la transcripción y ni el motor ni
  el verificador lo aplican. Si una norma lo fija, debe entrar a la vez en el
  tope efectivo de la búsqueda, del constructor y del verificador.
- «Profundización» no se modela: la libre elección son bloques (D9). El 3 de
  créditos por materia de libre elección es un supuesto del prototipo, no una
  norma: ya es un dato obligatorio de la malla (`libreEleccion.creditosPorMateria`)
  y en el sprint 2 debe salir de la normativa o de `04-supuestos.md`.
- CP-SAT y la ramificación y acotamiento completa no se probaron.

## Tiempo invertido

- Spike original: unas 7 h (estimación del equipo; el máximo del issue era 6–8 h).
- Correcciones de la primera revisión: unas 5 h (estimación), incluidas las
  mediciones de 20 corridas y UC04.
- Segunda ronda de correcciones: unas 5 h previstas en el plan, más el triaje de
  los 15 comentarios de la revisión automática. Es una estimación del plan, no
  un registro: quien cierra el spike debe corregirla con su tiempo real.

Las horas reales deben registrarse en el campo «Horas reales» del tablero.

## Propuesta de S3

- Mantener **30 s por cálculo**. El caso más lento que calcula tarda 1,1 s; los
  que no calculan agotan el plazo.
- Latencia de cancelación objetivo: **≤ 0,5 s**; la prueba de 2 s cumple con
  margen.
- Calcular las alternativas de UC04 **en paralelo** con hilos virtuales y
  `orTimeout`; las que agoten el plazo se muestran como no calculadas. Con una
  alternativa que no termina el total lo fija el plazo; el paralelo evita que
  esa espera se sume a la de las demás.
- Resultado `NoCalculado`: tratarlo como la ext. 5a (sin plan parcial, con
  sugerencia de reintentar o probar otro tope) pero con un incidente técnico y
  no de normativa. No está en el SRS: va como laguna en el PR de requisitos.
- El cambio de `04-supuestos.md` y `07-lagunas.md` va en un PR de requisitos
  aparte.

## Qué cambia en el sprint 2

- Implementar `CalculoDelPlan` en `plandecarrera` con el modelo compacto, el
  verificador y el constructor testigo; dejar la búsqueda exacta como respaldo
  y oráculo de pruebas.
- Completar PRU-02 (#61) con el oráculo de fuerza bruta en mallas pequeñas y
  usarlo para validar c4–c6 y medir cuánto se aleja c4 por rescate del mínimo.
- Resolver los casos que hoy no terminan: E0/T5, E1/T5, E2/T5, E4/T1, E4/T5 y
  V-F. Opciones: mejorar el constructor para topes estrechos o evaluar CP-SAT.
  Para c4, sustituir el tope de nodos por una poda que demuestre las exclusiones.
- Implementar el estrés de malla duplicada y la prueba de cancelación a 30 s.
- Hacer completo el criterio de «útil» de `clausulasUtiles` para los
  prerrequisitos de las optativas elegidas (ver «Limitaciones»).
- Decidir con la normativa dos puntos que el spike deja abiertos: la asignación
  de agrupación de las materias aprobadas y `maximoPorPeriodo`.
- Llevar a la semilla D1, D3, D4, D5, D6 y D9 (#92, #100) y documentar el
  formato en DOC-05 (#93).

## Cómo reproducir

```sh
./mvnw -f spikes/tt-05-motor/pom.xml verify
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="curado" -Dtt05.publicar=true
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="todos" -Dtt05.publicar=true
```

La revisión de la transcripción (C1):

```sh
./mvnw -f spikes/tt-05-motor/pom.xml exec:java \
  -Dexec.mainClass=co.edu.unal.rutaun.spikes.motor.malla.RevisarMalla
```
