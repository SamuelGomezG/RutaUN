-- TT-04-T6 (#145): los datos variables pasan a columnas JSON.
--
-- Las columnas obligatorias se agregan primero como NOT NULL: si la tabla tiene filas, la
-- migración falla antes de borrar nada. Una base de desarrollo cargada antes de esta versión no
-- se convierte: se borra data/ y se vuelve a cargar la semilla.
ALTER TABLE regla ADD COLUMN datos_json JSON NOT NULL;
ALTER TABLE regla DROP COLUMN datos;
ALTER TABLE regla ALTER COLUMN datos_json RENAME TO datos;

ALTER TABLE norma ADD COLUMN alcance_detalle_json JSON NOT NULL;
ALTER TABLE norma DROP COLUMN alcance_detalle;
ALTER TABLE norma ALTER COLUMN alcance_detalle_json RENAME TO alcance_detalle;

ALTER TABLE incidente ADD COLUMN elementos_json JSON NOT NULL;
ALTER TABLE incidente DROP COLUMN elementos;
ALTER TABLE incidente ALTER COLUMN elementos_json RENAME TO elementos;

-- Los cambios respecto a la versión anterior todavía no tienen contraparte en el dominio y la
-- columna siempre va nula: se cambia el tipo sin exigirla.
ALTER TABLE version_consolidado DROP COLUMN cambios;
ALTER TABLE version_consolidado ADD COLUMN cambios JSON;
