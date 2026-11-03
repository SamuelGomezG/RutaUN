# 0001 · Motor del cálculo del plan (TT-05)

Fecha: 2026-11-03 · Estado: **aceptada con reservas**

## Pregunta

¿Una búsqueda exacta sobre un modelo compacto calcula el plan óptimo según los
seis criterios del «Cálculo del plan» sobre la malla real de Ingeniería de
Sistemas y Computación en menos de 30 s (S3), también cinco veces seguidas
(UC04)?

## Respuesta

**No para una búsqueda exacta.** En los casos reportados la búsqueda en
profundidad no expandió ningún nodo (`nodos=0`): el plan lo encontró el
constructor testigo. Cuando la búsqueda corrió de respaldo, agotó el plazo.

**Sí para un híbrido de cotas y constructor testigo** en T2 para E0–E4 y en
T1/T4 para E1–E4: calcula en milisegundos un plan que demuestra c1 (k coincide
con la cota inferior), c2 y c3; c4–c6 salen de una heurística determinista sin
prueba exhaustiva de optimalidad. T3 y T5 solo se completan en E3; la variante
F y el estrés de malla duplicada no se completaron.

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

El modelo compacto (índices, bitsets de dos `long`, CNF y máscaras de
agrupaciones) y el verificador independiente funcionan. La búsqueda exacta
queda como respaldo y como oráculo de pruebas, pero no es la que produce los
planes medidos: por eso el CSV muestra `nodos=0`. La fijación sucesiva usa el
constructor testigo como oráculo rápido: primero k, después c2, c3, c4 y una
mejora local para c5 y c6. El plan final siempre pasa `VerificadorDePlan`.

## Datos usados

- Malla: Acuerdo 003-A de 2022 (CA) y Acuerdo 11 de 2023 (CFI), transcritos el
  2026-11-03: 90 materias (17 obligatorias, 54 créditos), 12 agrupaciones con
  créditos y 19 subagrupaciones, 83 prerrequisitos, 14 habilitaciones, 0
  correquisitos, 3 externas, 2 no cursables. Cadena más larga: 7 materias.
- Orden del consolidado: malla estándar 2023 del programa.
- Escenarios: E0 a E4, topes T1 a T5, variantes C (correquisito trivial), C2
  (correquisito pendiente) y D3 («o» de Sistemas de Información).
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

CSV completo: `spikes/tt-05-motor/resultados/mediciones-2026-11-03T17-13-41.csv`.
La corrida `curado` dejó además
`spikes/tt-05-motor/resultados/mediciones-2026-11-03T17-05-46.csv`. Cada caso
hizo 3 calentamientos y 20 medidas; los tiempos son milisegundos sin arranque
de la JVM. La huella del plan coincide entre las dos corridas para todos los
casos comunes.

| Escenario | Tope | k | c2 | c3 | Opt. | Bloques | Mediana | p95 | Máx. |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| E0 | T2 | 10 | 0 | 78 | 23 | 4 | 124 | 139 | 140 |
| E1 | T1 | 7 | 0 | 63 | 19 | 2 | 91 | 92 | 93 |
| E1 | T2 | 8 | 0 | 63 | 19 | 2 | 98 | 99 | 100 |
| E1 | T4 | 9 | 0 | 63 | 19 | 3 | 111 | 112 | 112 |
| E2 | T1 | 4 | 0 | 24 | 7 | 3 | 49 | 51 | 51 |
| E2 | T2 | 4 | 0 | 24 | 7 | 2 | 49 | 50 | 51 |
| E2 | T4 | 5 | 0 | 24 | 7 | 3 | 60 | 60 | 61 |
| E3 | T1 | 1 | 0 | 9 | 2 | 1 | 12 | 13 | 13 |
| E3 | T2 | 1 | 0 | 9 | 2 | 1 | 12 | 12 | 12 |
| E3 | T3 | 2 | 1 | 9 | 2 | 1 | 36 | 37 | 38 |
| E3 | T5 | 3 | 3 | 9 | 2 | 1 | 107 | 110 | 111 |
| E4 | T2 | 8 | 0 | 61 | 18 | 4 | 94 | 97 | 99 |
| E4 | T4 | 8 | 0 | 61 | 18 | 3 | 97 | 99 | 99 |
| V-C | T2 | 8 | 0 | 63 | 19 | 2 | 98 | 104 | 104 |
| V-C2 | T2 | 8 | 0 | 63 | 19 | 2 | 99 | 100 | 101 |
| V-D3 | T2 | 10 | 0 | 78 | 23 | 4 | 124 | 125 | 129 |

Todos los planes calculados pasan el verificador. En todos, k coincide con la
cota inferior. E3/T3 con T3 tiene `c2=1`, la prueba de UC01 ext. 6a; E3/T5 con
T5 tiene `c2=k=3`.

Casos no completados (la tabla del CSV los conserva):

| Escenario | Tope | Resultado | Corridas | Mediana | Máx. |
| --- | --- | --- | ---: | ---: | ---: |
| E0 | T1 | sin plan | 20 | 110 | 127 |
| E0 | T3 | sin plan | 20 | 99 | 105 |
| E0 | T4 | tiempo agotado | 1 | 30000 | 30000 |
| E0 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| E1 | T3 | tiempo agotado | 1 | 30000 | 30000 |
| E1 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| E2 | T3 | sin plan | 20 | 344 | 356 |
| E2 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| E4 | T1 | tiempo agotado | 1 | 30000 | 30000 |
| E4 | T3 | sin plan | 20 | 3295 | 3325 |
| E4 | T5 | tiempo agotado | 1 | 30000 | 30000 |
| V-F | T2 | tiempo agotado | 1 | 30000 | 30000 |

UC04 sobre E1 con T1–T5: secuencial 60 384 ms; en paralelo con hilos virtuales
y `CompletableFuture.orTimeout`, 30 029 ms. T3 y T5 terminan en tiempo agotado
y se muestran como no calculadas; T1, T2 y T4 calculan en unos 90–110 ms.
En paralelo, T4 quedó en el límite de los 30 s por la contención con T3/T5.

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
- **Pruebas:** 20 pruebas en verde con `verify`. La prueba de determinismo ahora
  compara los vectores completos y construye un modelo nuevo en cada vuelta; la
  anterior usaba un centinela negativo y no comparaba nada. La huella del plan
  coincide entre procesos.
- **Correquisitos:** V-C era trivial porque 2016353 ya estaba aprobada en E1.
  V-C2 exige 2016698 junto con 2016353; el constructor los pone en el mismo
  semestre y el verificador lo acepta.
- **Oráculo de fuerza bruta:** no se implementó; queda para PRU-02 (#61). Por
  eso c4–c6 no tienen prueba exhaustiva de optimalidad.
- **Checkstyle:** se usa la copia local del proyecto que omite solo las reglas de
  Javadoc; se corrigieron nombres (`D`, `L`, `DX`, `n`, `k`), líneas largas y
  distancias de declaración.

## Limitaciones

- La fase c2–c6 usa el constructor testigo y una mejora local; no agota el
  espacio de planes. El resultado es determinista y válido, pero no se probó
  que sea el óptimo lexicográfico en todos los casos.
- No se completaron E0/T1, E0/T3, E0/T4, E0/T5, E1/T3, E1/T5, E2/T3, E2/T5,
  E4/T1, E4/T3, E4/T5 ni V-F. T3 es un tope realista, así que la respuesta
  «escala» no cubre toda la franja T1–T3.
- El estrés de malla duplicada no se implementó.
- No se modelan materias que ya no rigen, restricciones inciertas ni reglas
  temporales, programadas o informativas. Los correquisitos y la habilitación
  con medida del programa solo se ejercitaron con datos sintéticos.
- «Profundización» no se modela: la libre elección son bloques (D9). El 3 de
  créditos por materia de libre elección es un supuesto del prototipo, no una
  norma; en el sprint 2 debe salir de la normativa o de `04-supuestos.md`.
- CP-SAT y la ramificación y acotamiento completa no se probaron.

## Tiempo invertido

- Spike original: unas 7 h (estimación del equipo; el máximo del issue era 6–8 h).
- Correcciones de la revisión: unas 5 h (estimación), incluidas las mediciones
  de 20 corridas y UC04.

Las horas reales deben registrarse en el campo «Horas reales» del tablero.

## Propuesta de S3

- Mantener **30 s por cálculo**.
- Latencia de cancelación objetivo: **≤ 0,5 s**; la prueba de 2 s cumple con
  margen.
- Calcular las alternativas de UC04 **en paralelo** con hilos virtuales y
  `orTimeout`; las que agoten el plazo se muestran como no calculadas.
- El cambio de `04-supuestos.md` y `07-lagunas.md` va en un PR de requisitos
  aparte.

## Qué cambia en el sprint 2

- Implementar `CalculoDelPlan` en `plandecarrera` con el modelo compacto, el
  verificador y el constructor testigo; dejar la búsqueda exacta como respaldo
  y oráculo de pruebas.
- Completar PRU-02 (#61) con el oráculo de fuerza bruta en mallas pequeñas y
  usarlo para validar c4–c6.
- Resolver los casos que hoy no terminan: E0/T1, E0/T3, E1/T3, E1/T5, E2/T3,
  E2/T5, E4/T1, E4/T3, E4/T5 y V-F. Opciones: mejorar el constructor o evaluar
  CP-SAT.
- Implementar el estrés de malla duplicada y la prueba de cancelación a 30 s.
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
