# TT-05 · Motor de cálculo del plan (spike)

Prototipo del cálculo de UC01 sobre la malla real de Ingeniería de Sistemas y
Computación. **No es parte de la aplicación**: vive en `spikes/`, no se incluye
en el `pom.xml` raíz y no toca `normativa`, `plandecarrera` ni la base de datos.

Responde una sola pregunta: ¿una búsqueda exacta sobre un modelo compacto
calcula el plan óptimo según los seis criterios del «Cálculo del plan» en menos
de 30 s (S3), también cinco veces seguidas (UC04)?

## Cómo correrlo

Desde la raíz del repositorio:

```sh
./mvnw -f spikes/tt-05-motor/pom.xml verify
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="curado"
./mvnw -f spikes/tt-05-motor/pom.xml exec:java -Dexec.args="E0 T2"
```

Por omisión, las mediciones se escriben en `target/mediciones/` con la fecha y
la hora en el nombre. Para publicar una corrida en `resultados/` (la evidencia
que cita el informe), agrega `-Dtt05.publicar=true`.

El informe de la decisión está en
[`docs/diseno/decisiones/0001-motor-del-calculo-del-plan.md`](../../docs/diseno/decisiones/0001-motor-del-calculo-del-plan.md).
La corrida `curado` hace 3 calentamientos y 20 medidas por caso y resume
mediana, p95 y máximo; `todos` recorre además los casos extremos. Cada fila
lleva la fase y la huella del plan para comprobar el determinismo.

El proyecto usa su propia copia de la configuración de Checkstyle,
`config/checkstyle.xml`, sin las reglas de Javadoc. `config/checkstyle-suppressions.xml`
limita las supresiones a esas mismas reglas (`MissingJavadocType` y
`MissingJavadocMethod`); las demás reglas del proyecto se aplican tal cual.

## Datos

- `datos/malla-sistemas.json`: transcripción del Acuerdo 003-A de 2022 del
  Consejo Académico y del Acuerdo 11 de 2023 del Consejo de Facultad de
  Ingeniería, con la malla estándar 2023 para el orden del consolidado.
- `datos/escenarios.json`: avances sintéticos (sin datos personales), topes y
  variantes de medición.

Los PDF no se versionan. Fuentes:

- [Acuerdo 003-A de 2022 (Régimen Legal, d_i=103388)](https://legal.unal.edu.co/rlunal/home/doc.jsp?d_i=103388)
- [Acuerdo 11 de 2023 (Régimen Legal, d_i=106142)](https://legal.unal.edu.co/rlunal/home/doc.jsp?d_i=106142)
- [Acuerdo 008 de 2008 (Régimen Legal, d_i=34983)](https://legal.unal.edu.co/rlunal/home/doc.jsp?d_i=34983)
- [Acuerdo 033 de 2007 (Régimen Legal, d_i=34245)](https://legal.unal.edu.co/rlunal/home/doc.jsp?d_i=34245)

La malla estándar es el PDF «Plan de estudios estándar 2023» del programa. No
es una norma; solo fija el orden del consolidado y los semestres sugeridos.

## Decisiones de transcripción

D1 a D12 del plan del spike. Las que afectan a la semilla se registran en el
informe y en los issues #92 y #100; no se deciden en silencio para la
aplicación.
