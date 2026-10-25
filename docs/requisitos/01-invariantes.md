---
title: "Invariantes y reglas críticas"
doc_type: "invariantes"
srs_ids:
  - UC01..UC07 (GM/GE)
  - §4
depends_on:
  - 00-contexto.md
  - 04-supuestos.md
---
# 4. Invariantes y reglas críticas (hoja de referencia rápida)

> **Cuándo cargar:** siempre, junto con 00. Si una implementación contradice esta sección, la implementación está mal.

Resumen de lo que **nunca** debe violar una implementación. Cada punto remite al glosario (§7), a los supuestos (§8) o a los casos de uso (§9), que son la fuente exacta.

## 4.1 Naturaleza del producto
- **El plan es una guía calculada, no una inscripción ni una autorización.** Con todo plan, comparación o explicación se muestra el «Aviso de lo que RutaUN no verifica» (UC01 GM 6, UC04 GM 7, UC05 GM 3…).
- **RutaUN no verifica:** oferta de materias, cupos y sobrecupos, horarios y cruces de clases, cupo de créditos (Acuerdo 008 de 2008, art. 11), reconocimientos (validaciones, homologaciones, convalidaciones), autorizaciones del Consejo de Facultad, si el estudiante debe cursar nivelación, cumplimiento de los requisitos de grado. Tampoco describe de qué trata cada materia.
- **Nunca afirmar que alguien «puede» o «no puede» inscribir una materia** (UC05 GM 3): «sin restricciones pendientes» no significa «puedo inscribirla ya».
- **Culminación del plan de estudios ≠ grado.** El plan estima cuándo el estudiante culminaría su plan de estudios, no cuándo se graduaría.
- RutaUN **no es fuente oficial**; la fuente es el documento publicado en Régimen Legal. Cada regla se muestra con su fuente.

## 4.2 Normativa, consolidado y reglas
- **Programa** = unidad que tiene un consolidado. Cada estudiante tiene **un solo programa** a la vez. Un programa con más de un plan de estudios vigente (p. ej. régimen de transición) se carga como programas distintos.
- **Consolidado normativo versionado, con historial inmutable.** Se crea una **versión nueva** (a) por cada incorporación confirmada en UC07, (b) por cada corrección del administrador (motivo «corrección») y (c) automáticamente cuando una regla empieza o deja de regir (al avanzar el periodo en curso, o al llegar su fecha, hora de Colombia; una sola por programa y día — S15). Las versiones anteriores se conservan; una incorporación nunca borra historia.
- **Toda regla tiene fuente** (norma + artículo o numeral). UC07 no acepta reglas sin fuente. Si una regla aparece sin fuente, se muestra como «sin fuente registrada», nunca como respaldada por una norma, y se registra un incidente.
- **Una regla fija un solo tipo de dato sobre un solo objeto.** Un mismo artículo suele contener muchas reglas.
- **Reglas del plan vs. informativas:** el cálculo solo aplica las reglas del plan; las informativas se muestran junto a planes y consultas. **Toda regla temporal (con «vigencia hasta») es informativa**; el plan no puede suponerla vigente en todos sus semestres.
- **Contradicciones:** dos reglas sobre el mismo objeto, con vigencias solapadas y valores distintos, sin que una modifique/derogue/exceptúe a la otra → ambas «en revisión», con sus dos fuentes; no se afirma cuál rige; incidente. Si una es regla del plan, el consolidado deja de ser calculable.
- **Una norma se registra una sola vez** (identificada por tipo, número, año y órgano) y se incorpora a varios programas por su **alcance**; lo ya registrado no se cambia en UC07.
- **Periodo en curso** = único dato de tiempo que guarda RutaUN (no guarda fechas de periodos — S4). Lo avanza el administrador, fuera de UC01–UC07, solo de uno en uno.

## 4.3 Cálculo del plan (determinista)
- **Entradas:** avance, tope de carga, optativas fijadas, reglas del plan vigentes en la versión del consolidado cargada en el paso 2 del caso de uso, periodo en curso. **Semestre 0** = periodo en curso (solo muestra lo que está «en curso»); **semestre 1** = periodo siguiente. El plan supone vigentes las reglas del plan **en todos sus semestres** (S4).
- **Tope** = (límite de créditos, límite de materias), cada uno un entero ≥ 1 o «sin límite», **al menos uno entero**. Un bloque de N créditos de libre elección cuenta como ⌈N/3⌉ materias (S2).
- **Mínimo por periodo** (hoy 10 créditos, Acuerdo 008 de 2008, art. 10): el cálculo procura alcanzarlo pero no excluye planes; el semestre que no lo alcanza lleva la **marca de autorización**; el semestre 0 no se evalúa. **Carga máxima por periodo:** si una regla del plan la fija, ningún semestre la supera.
- **Desempate, en este orden** (ver «Cálculo del plan»): (1) menos semestres; (2) menos semestres con marca de autorización; (3) menos créditos en optativas elegidas no fijadas; (4) lista de optativas elegidas, ordenadas según el orden del consolidado, lexicográficamente menor; (5) vector de semestres (semestre de cada materia en el orden del consolidado) lexicográficamente menor; (6) vector de créditos de libre elección por semestre lexicográficamente **mayor**. Mismos datos ⇒ mismo plan.
- **Orden del consolidado** (para desempatar): por semestre sugerido de la malla estándar; a igual semestre, primero las materias que la malla muestra; luego por código (comparado carácter a carácter).
- **Cancelar todo cálculo a los 30 segundos** (S3; la cifra se validará en la implementación). Validar reglas / construir la vista previa de UC07: **60 segundos** (S9).
- **El plan vigente se marca como desactualizado solo al mostrarse** (nunca se guarda ese estado ni se recalcula solo). Si no queda nada por cubrir: «plan cumplido».
- Todo plan incluye: obligatorias pendientes, optativas fijadas, **espacios de optativa** con opciones (nunca una recomendada), bloques de libre elección, marcas de autorización, reglas temporales/programadas/informativas por semestre con su fuente, semestres restantes, periodo estimado de culminación y el aviso.

## 4.4 Persistencia, concurrencia y errores
- **Guardado atómico, todo o nada, con confirmación explícita.** Un solo **plan vigente** por estudiante; **no hay borradores**; guardar uno nuevo lo reemplaza (se advierte que el anterior no se conserva).
- **Comprobación previa al guardar** (UC01 paso 10, UC02 paso 11, UC04 paso 10, UC03 paso 7): el avance, la versión vigente del consolidado, el plan vigente y el periodo en curso deben seguir siendo los cargados en el paso 2. Si cambiaron: no guardar, informar cuál cambió y ofrecer recalcular/recargar.
- **Fallo de persistencia:** el estado previo queda intacto; se conserva lo calculado/editado en pantalla mientras dure la sesión para reintentar; se registra incidente.
- **Error interno:** no se modifica ningún dato guardado; se informa que ocurrió un error y que se puede reintentar; se registra incidente con paso, programa, versión del consolidado y causa.
- **Sesión** (cuenta de RutaUN, no la institucional): expira tras **30 minutos sin acciones** (clics, teclas, envíos; las consultas automáticas no cuentan) — S5. Lo no guardado se pierde. Si ya se guardó, queda guardado.
- **UC07 reserva** (bloqueo) los programas del alcance y la identificación de la norma mientras dura el caso; la reserva se libera en **cualquier** salida (guardado, sin cambios, abandono, sesión expirada, error interno). La incorporación es **todo o nada** entre los programas del alcance.

## 4.5 Privacidad y roles
- Avance y planes = **datos personales** (Ley 1581 de 2012). Solo su dueño, con sesión y en su programa, los ve y modifica. **Las consultas no se guardan.** El administrador solo ve **cantidades** (planes desactualizados, avances afectados), nunca datos que identifiquen estudiantes.
- **Visitante sin sesión:** puede UC05 y UC06 sobre cualquier programa cargado con consolidado vigente. **Reportar un enlace roto exige sesión.**
- Solo el rol **administrador de normativa** accede a UC07 (UC07 ext. 1a).

## 4.6 Incidentes
- Registro que el sistema deja al administrador cuando el consolidado impide atender a un usuario o ocurre un error. Incluye fecha, caso de uso y paso, programa, versión del consolidado, causa y elementos involucrados. **Se agrupan** los incidentes con mismo programa, versión, causa y elementos: un solo registro con primera y última fecha y cuántas veces ocurrió. Los visitantes no los ven; el administrador los consulta y los marca como atendidos fuera de UC01–UC07; si uno atendido vuelve a ocurrir, se registra de nuevo.
