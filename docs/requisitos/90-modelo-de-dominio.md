---
title: "Mapa de entidades (derivado, orientativo)"
doc_type: "anexo-derivado"
srs_ids:
  - §5
depends_on:
  - 01-invariantes.md
  - glosario/01-producto-actores-tiempo.md
  - glosario/02-normativa-consolidado.md
  - glosario/03-malla-materias.md
  - glosario/04-avance.md
  - glosario/05-calculo-plan.md
---
# 5. Mapa de entidades (derivado del glosario; orientativo, no es diseño definitivo)

> **Nota de segmentación:** prefijo `90` = anexo derivado de la segmentación. Este mapa **no aparece en los PDF** y **no es diseño**: es orientativo y **manda el glosario** si algo lo contradice. No modelar a partir de aquí sin releer el glosario.

> Esta sección **no** aparece en los PDF: es un resumen de las entidades y atributos que el glosario y los casos de uso mencionan, para orientar el modelado. Si algo contradice al glosario (§7), manda el glosario.

| Entidad | Atributos / relaciones que el documento menciona |
|---|---|
| **Programa** | nombre, código, facultad; creado por el administrador («cargado»); un consolidado; norma base = acuerdo de su plan de estudios (primera norma incorporada, debe dejar una malla utilizable) |
| **Estudiante / Cuenta** | programa asociado (uno solo); sesión; avance; plan vigente |
| **Administrador de normativa** | rol; crea programas; registra y avanza el periodo en curso; consulta historial e incidentes |
| **Norma** (acuerdo, resolución o circular) | tipo, número, año, órgano; tipo de efecto (base / modificatoria / excepción o medida temporal); fecha de expedición; fecha de aplicación (periodo o fecha); «vigencia hasta» (periodo o fecha; obligatoria en excepciones y medidas temporales); enlace a Régimen Legal (si existe); **no se guarda el documento fuente**; alcance (tipo: todos / facultades / programas, exclusiones, texto) |
| **Regla** | categoría, objeto, enunciado, artículo o numeral, datos según categoría, vigencia propia (opcional), clasificación (plan / informativa), temporal (sí/no), estado (vigente / programada / ya no rige / en revisión), relación con otra regla (modifica / deroga / exceptúa) |
| **Consolidado (versión)** | número, fecha de creación/actualización, motivo (norma incorporada, «corrección» o automático por periodo/fecha), administrador (si lo creó uno), reglas con su estado, malla estándar |
| **Versión del plan de estudios** | por programa; solo cambia cuando una norma modifica reglas de estructura; la más reciente rige para todos los estudiantes |
| **Componente** | créditos exigidos; materias obligatorias y optativas; agrupaciones |
| **Agrupación / subagrupación** | créditos exigidos (y desglose obligatorios/optativos si la norma lo fija); materias obligatorias, optativas y subagrupaciones; pertenece a un solo componente o agrupación |
| **Materia** | catálogo global: código (identidad) y créditos fijos; el nombre vive en cada versión; componente/agrupación(es), obligatoria u optativa; semestre sugerido (malla estándar) |
| **Materia reconocida** | fuera de la malla (S7): nombre, código, créditos, agrupación; suma créditos y no cumple prerrequisitos |
| **Prerrequisito** | materia + expresión «y»/«o» sobre materias; puede mencionar **materias externas** (nivelación u otro programa) |
| **Correquisito** | materia + expresión «y»/«o»; se cumple si el término está aprobado/en curso o el plan lo ubica en el mismo semestre o antes |
| **Regla de habilitación** | medida (créditos aprobados de un componente o del programa) + umbral en créditos |
| **Equivalencia** | materia(s) de origen → materia de destino |
| **Malla / Malla estándar** | estructura del plan de estudios; semestre sugerido por materia y créditos de libre elección sugeridos por semestre |
| **Avance** | marca por materia (aprobada / en curso / pendiente) con el periodo en curso en que se registró cada marca «en curso»; créditos de libre elección aprobados y en curso; semestres cursados (con su periodo); agrupación asignada por marca; fecha y periodo en curso del guardado; versión del consolidado |
| **Plan vigente** | fecha, periodo en curso, tope, optativas fijadas, avance y versión del consolidado usados; semestres (0, 1, 2…) con obligatorias, optativas fijadas, espacios de optativa, bloques de libre elección, marcas de autorización y reglas temporales/programadas/informativas |
| **Incidente** | primera y última fecha, conteo, caso de uso y paso, programa, versión, causa, elementos, atendido (sí/no) |
| **Periodo en curso** | único dato de tiempo (AAAA-1 / AAAA-2) |
| **Sesión** | cuenta de RutaUN, expira a los 30 min sin acciones |
