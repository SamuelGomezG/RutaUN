# 0001 · Motor del cálculo del plan (TT-05)

Fecha: 2026-11-03 · Estado: **aceptada con reservas**

## Pregunta

¿Una búsqueda exacta sobre un modelo compacto calcula el plan óptimo según los
seis criterios del «Cálculo del plan» sobre la malla real de Ingeniería de
Sistemas y Computación en menos de 30 s (S3), también cinco veces seguidas
(UC04)?

## Respuesta

**Sí con reservas para los topes realistas T1, T2 y T4**, con tiempos de 2 a 89
ms y el verificador en verde; **no se completó** T3 (12 créditos), T5 (8
créditos), la variante F (optativas fijadas) ni el estrés de cancelación. La
fase que resuelve los criterios 2 a 6 es una fijación sucesiva con un
constructor testigo y una mejora local, no una prueba exhaustiva de optimalidad.

## Enfoques comparados

- Búsqueda exacta propia: profundización por semestres, cotas, simetrías,
  memoización de fallidos y fijación sucesiva de criterios (diseño, «El
  algoritmo»). Implementada en `Buscador` y `CalculoDelPlan`.
- Constructor testigo: plan codicioso en orden del consolidado, con
  prerrequisitos, habilitaciones, agrupaciones, bloques y redistribución de
  libre elección. Se usa como prueba de existencia y como punto de partida de
  la fijación sucesiva.
- Mejora local verificada para los criterios 5 y 6 (`PlanOptimizador`).
- CP-SAT por etapas: no se probó; no se agregó la dependencia nativa.

## Enfoque elegido y por qué

Se conserva el modelo compacto (índices, bitsets de dos `long`, CNF y máscaras
de agrupaciones) y el verificador independiente. Para la malla real, el
constructor testigo encuentra en milisegundos planes que la búsqueda exhaustiva
no encontraba dentro del plazo. La fijación sucesiva usa ese constructor como
oráculo rápido: primero `k`, después c2, c3, c4 (lista de optativas), y una
mejora local verificada para c5 y c6. El plan final siempre pasa
`VerificadorDePlan`; si no pasa, no se reporta.

## Datos usados

- Malla: Acuerdo 003-A de 2022 (CA) y Acuerdo 11 de 2023 (CFI), transcritos el
  2026-11-03: 90 materias (17 obligatorias, 54 créditos), 12 agrupaciones con
  créditos y 19 subagrupaciones, 83 prerrequisitos, 14 habilitaciones, 0
  correquisitos, 3 externas, 2 no cursables. Cadena más larga: 7 materias.
- Orden del consolidado: malla estándar 2023 del programa.
- Decisiones de transcripción: D1 a D12 del plan del spike. Las que tocan la
  semilla se registran en #92 y #100; el formato de normativa en #93/DOC-05.
- Escenarios: E0 a E4, topes T1 a T5, variantes C (correquisito sintético) y D3
  («o» de Sistemas de Información); la variante F no se completó.
- Ambiente: AMD Ryzen 7 5825U, 16 hilos, 13 GiB de RAM, Ubuntu 24.04, OpenJDK
  21.0.12.1.

## Mediciones

CSV completo: `spikes/tt-05-motor/resultados/mediciones-2026-11-03.csv`.
Cada fila es una corrida; la tabla resume la mediana de cinco corridas en
caliente. Tiempos en milisegundos, sin contar el arranque de la JVM.

| Escenario | Tope | k | c2 | c3 | Opt. | Bloques | Mediana | Máx. | Verificador |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| E0 | T2 | 10 | 0 | 78 | 23 | 4 | 40 | 89 | OK |
| E1 | T1 | 7 | 0 | 63 | 19 | 2 | 18 | 18 | OK |
| E1 | T2 | 8 | 0 | 63 | 19 | 2 | 18 | 23 | OK |
| E1 | T4 | 9 | 0 | 63 | 19 | 3 | 22 | 23 | OK |
| E2 | T2 | 4 | 0 | 24 | 7 | 2 | 5 | 6 | OK |
| E3 | T2 | 1 | 0 | 9 | 2 | 1 | 2 | 2 | OK |
| E4 | T2 | 8 | 0 | 61 | 18 | 4 | 12 | 14 | OK |
| V-C (correquisito) | T2 | 8 | 0 | 63 | 19 | 2 | 15 | 15 | OK |
| V-D3 («o») | T2 | 10 | 0 | 78 | 23 | 4 | 21 | 22 | OK |

Las cotas de §7.1 se respetan en todos los casos medidos: E0/T2 = 10, E1/T1 = 7,
E1/T2 = 8, E1/T4 = 9, E2/T2 = 4, E3/T2 = 1 y E4/T2 = 8.

UC04 sobre E1: T1, T2 y T4 seguidos, 64 ms en total; en paralelo con hilos
virtuales, 53 ms en total. No se incluyeron T3 ni T5 porque el constructor no
los completó.

Casos no completados:

| Caso | Resultado |
| --- | --- |
| E0 T1 y E0 T3 | El constructor no reconstruye el plan con las optativas de c4; no se mide como plan válido |
| E0 T4 | Búsqueda de respaldo: 3,3 M de nodos y tiempo agotado (~43 s) |
| E0 T5 | Tiempo agotado (~300 s); la cancelación no se respetó en el caso extremo |
| E1 T3 | Sin plan tras la fase constructiva (~8,9 s) |
| Variante F (fijadas 2028837 y 2016748) | No terminó; no se reporta plan |
| Estrés de malla duplicada | No implementado en este corte |

## Corrección

- **C1:** la transcripción pasa las comprobaciones de §3.4: 165 créditos,
  15 + 36 y 39 + 42, 90 materias, 83 prerrequisitos, 14 habilitaciones, 0
  correquisitos, 3 externas, 2 no cursables y cadena de 7. La malla estándar
  adaptada pasa el verificador con T2; las tres mutaciones se rechazan por
  prerrequisito, habilitación y cobertura/libre elección, respectivamente.
- **Verificador:** se ejecutó sobre todos los planes reportados; ninguna fila
  con `calculado` tiene errores.
- **Pruebas:** 9 pruebas en verde (`verify`): bitsets, malla real (E0/T2 y
  E1/T2), determinismo 20 corridas y verificador con la malla estándar y sus
  mutaciones.
- **Oráculo de fuerza bruta:** no se implementó en este corte; queda para
  PRU-02 (#61). Por eso la optimalidad de c4, c5 y c6 no está demostrada
  exhaustivamente.

## Limitaciones

- La fase de c2 a c6 usa el constructor testigo y una mejora local; no agota el
  espacio de planes. El resultado es determinista y válido, pero no se probó
  que sea el óptimo lexicográfico en todos los casos.
- T3 y T5 no se completaron. T3 es un tope realista (12 créditos), así que la
  respuesta «escala» no está demostrada para toda la franja T1–T3.
- La variante F y el estrés de cancelación no se completaron. La cancelación
  solo se comprobó de forma indirecta en los tiempos agotados.
- No se modelan materias que ya no rigen, restricciones inciertas ni reglas
  temporales, programadas o informativas. Los correquisitos y la habilitación
  con medida del programa solo se ejercitaron con datos sintéticos (V-C).
- «Profundización» no se modela: la libre elección se planifica como bloques
  (D9).
- CP-SAT y la ramificación y acotamiento completa no se probaron.

## Qué cambia en el sprint 2

- Implementar `CalculoDelPlan` en `plandecarrera` con el modelo compacto, el
  verificador y el constructor testigo; dejar la búsqueda exacta como respaldo
  y como oráculo de pruebas.
- Completar PRU-02 (#61) con el oráculo de fuerza bruta en mallas pequeñas y
  usarlo para validar c4, c5 y c6.
- Resolver T3, T5 y la variante F: mejorar el constructor (o adoptar CP-SAT si
  el equipo acepta la dependencia) y corregir la cancelación del caso extremo.
- Implementar el estrés de malla duplicada y la prueba de cancelación a 30 s.
- Llevar a la semilla las decisiones D1, D3, D4, D5, D6 y D9 (#92, #100) y
  documentar el formato en DOC-05 (#93).

## Cómo reproducir

```sh
./mvnw -f spikes/tt-05-motor/pom.xml verify
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="curado"
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="E0 T2"
```

La revisión de la transcripción (C1) se ejecuta con:

```sh
./mvnw -f spikes/tt-05-motor/pom.xml exec:java \
  -Dexec.mainClass=co.edu.unal.rutaun.spikes.motor.malla.RevisarMalla
```
