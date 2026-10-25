---
title: "Lagunas, decisiones pendientes y preguntas abiertas"
doc_type: "lagunas"
srs_ids:
  - §14
  - S1
  - S3
depends_on:
  - 00-contexto.md
  - 01-invariantes.md
  - 03-marco-normativo.md
  - 06-evidencia-y-ux.md
---
# 14. Lagunas, decisiones pendientes y preguntas abiertas

> **Cuándo cargar:** cuando una decisión no esté resuelta. §14.2 son **pautas de UX a respetar** (no «ignorar»); el resto son huecos: si el documento calla, no decidas en silencio (regla 3 de §0), pregunta o registra supuesto nuevo.
> **ADVERTENCIA:** aquí está lo que el SRS **no** define — nombre del producto, algunos NFRs. Las decisiones del 25 de octubre (stack, cuentas, SOP) están en §14.3.

## 14.1 Pendientes explícitos del documento
- **S1** (los créditos excedentes de una agrupación no reducen lo exigido a otra, a la que la contiene ni a la libre elección) está **por confirmar**: las normas revisadas no tratan el tema.
- **S3:** la cota de **30 s** por cálculo se ajustará/validará en la implementación (el spike TT-05 del sprint 1 la medirá); la de **60 s** de UC07, S9, es igualmente un supuesto.
- **S18** es una **decisión ya aplicada** (25 de octubre de 2026): estados de una restricción por certeza. Quedan por confirmar solo S1 y S3.
- Las reformas del plan de estudios ya son **versiones del mismo programa** (ver «Programa»); la convivencia por cohorte queda fuera de alcance.
- Acta ZEUS-02 §6–§7: sin resolver si las normas que el SIA no aplica son un problema general o anecdótico, y si afecta a Sistemas, a Diseño Industrial o a ambas; confirmar con el entrevistado que entiende que doble titulación está fuera de alcance.

## 14.2 Feedback de UX recogido (ZEUS-02), a considerar en el diseño de la interfaz
Evitar saturar con información de golpe (mostrar primero la carrera/malla y desplegar detalle al hacer clic); formularios que se habilitan en el orden en que deben llenarse; tooltips en el registro del avance; la «carga académica» se mide solo en créditos.

## 14.3 Huecos del producto y decisiones adoptadas (25 de octubre de 2026)
Al aplicar el diseño de clases y la evaluación del SRS, el equipo adoptó estas decisiones de la primera versión; el código y el backlog las tratan:
- **Stack y estructura:** fijados por el diseño (ver §0): Spring Boot/MVC/Thymeleaf, H2 en modo archivo con Flyway, cuatro capas.
- **Autenticación y cuentas:** cuenta de RutaUN con registro propio (correo y contraseña con hash), **sin** recuperación automática (la restablece el administrador); roles estudiante y administrador de normativa; la sesión expira a los 30 minutos (S5).
- **Administrador inicial:** lo crea un inicializador al arrancar, con credenciales de variables de entorno; ninguna contraseña queda en el repositorio.
- **Alta de programas:** por carga de datos (JSON); sin pantalla en esta versión.
- **Periodo en curso:** valor único y configurable (formato AAAA-1/AAAA-2); lo cambia un único punto del código; el administrador lo avanza de uno en uno («Avanzar periodo»). El cambio de programa del estudiante queda fuera de la primera entrega.
- **Incidentes:** servicio único de registro; el administrador los consulta y los marca como atendidos.
- **Aviso de lo que RutaUN no verifica:** fragmento reutilizable en todo plan, comparación o explicación (§4.1).
- **Comprobación de versión antes de guardar:** mecanismo compartido de UC01 paso 10, UC02 paso 11, UC03 paso 7 y UC04 paso 10 (§4.4).
- **Ley 1581:** aviso de privacidad y borrado de la cuenta con sus datos; el alcance exacto se confirma con el docente.
- **Documento fuente:** resuelto: **no se guarda** (ver «Fuente»); el enlace a Régimen Legal, si existe, basta.
- **Pantallas de administración fuera de UC01–UC07:** crear programas, registrar/avanzar el periodo en curso, consultar el historial del consolidado, consultar y marcar incidentes como atendidos, corregir errores de digitación/enlaces (S13), cambiar de programa (S12) y crear cuenta. El documento describe sus efectos en el glosario pero no los desarrolla como casos de uso.
- **Requisitos no funcionales** más allá de los tiempos de cancelación (30 s/60 s), la expiración de sesión (30 min) y la privacidad (Ley 1581): rendimiento, disponibilidad e internacionalización siguen sin definirse; accesibilidad con HTML semántico. La forma de la malla estándar y de las reglas va por el esquema JSON (UC07 paso 5).
- **Nombre definitivo** del producto («RutaUN» es provisional): sigue abierto.
