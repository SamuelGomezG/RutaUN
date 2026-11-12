# Decisiones de diseño

Una decisión por archivo, `NNNN-resumen-corto.md`, numeradas en orden. Una decisión aceptada no se reescribe: si cambia, se escribe otra que la reemplaza.

## Frontmatter

Sigue la convención de `docs/requisitos/` (`title`, `doc_type`, `depends_on`, `srs_ids`) y agrega tres claves:

```yaml
---
title: "0002 · Resumen de la decisión"
doc_type: "decisión"
estado: "aceptada"       # propuesta · aceptada · aceptada con reservas · rechazada · reemplazada
fecha: "2026-11-20"
srs_ids:                 # los IDs que la decisión gobierna, no todos los que menciona
  - S3
  - UC01 ext. 5a
reemplaza: [0001]        # vacío si no reemplaza a ninguna
depends_on:
  - ../../requisitos/casos/UC01-generar-plan-de-carrera.md
---
```

- **`estado`** dice si la decisión rige. Al escribir una que reemplaza a otra, cambia el estado de la anterior a `reemplazada` en el mismo PR.
- **`srs_ids`** usa los IDs tal como se escriben en el SRS (`UC01 ext. 5a`, `S3`, `UC06-HU01`) o en el tablero (`TT-05`).
- Una decisión sin estas claves sigue valiendo: el índice toma el estado y la fecha de la línea «Fecha: … · Estado: **…**» y, como IDs, los que el texto cita.

## Consultarlas

```
herramientas/indice_ids.py --decisiones    # todas, con su estado y cuál reemplaza a cuál
herramientas/indice_ids.py 0001            # una decisión y los IDs que gobierna
herramientas/indice_ids.py S3              # la ficha de un ID nombra las decisiones que lo tocan
herramientas/indice_ids.py --verificar     # falla si una decisión no tiene estado, reemplaza a una
                                           # que no existe o la reemplazada no lo dice
```
