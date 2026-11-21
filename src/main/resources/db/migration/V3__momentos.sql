ALTER TABLE norma ADD COLUMN aplica_desde_tipo VARCHAR(7);
ALTER TABLE norma ADD COLUMN aplica_desde_fecha DATE;
ALTER TABLE norma ADD COLUMN aplica_desde_periodo VARCHAR(6);
UPDATE norma SET aplica_desde_tipo = 'FECHA', aplica_desde_fecha = aplica_desde;
ALTER TABLE norma ALTER COLUMN aplica_desde_tipo SET NOT NULL;
ALTER TABLE norma DROP COLUMN aplica_desde;
ALTER TABLE norma ADD CONSTRAINT ck_norma_aplica_desde CHECK (
    (aplica_desde_tipo = 'FECHA' AND aplica_desde_fecha IS NOT NULL AND aplica_desde_periodo IS NULL)
    OR (aplica_desde_tipo = 'PERIODO' AND aplica_desde_periodo IS NOT NULL AND aplica_desde_fecha IS NULL)
);

ALTER TABLE norma ADD COLUMN vigente_hasta_tipo VARCHAR(7);
ALTER TABLE norma ADD COLUMN vigente_hasta_fecha DATE;
ALTER TABLE norma ADD COLUMN vigente_hasta_periodo VARCHAR(6);
UPDATE norma SET vigente_hasta_tipo = 'FECHA', vigente_hasta_fecha = vigente_hasta
    WHERE vigente_hasta IS NOT NULL;
ALTER TABLE norma DROP COLUMN vigente_hasta;
ALTER TABLE norma ADD CONSTRAINT ck_norma_vigente_hasta CHECK (
    (vigente_hasta_tipo IS NULL AND vigente_hasta_fecha IS NULL AND vigente_hasta_periodo IS NULL)
    OR (vigente_hasta_tipo IS NOT NULL AND vigente_hasta_tipo = 'FECHA' AND vigente_hasta_fecha IS NOT NULL AND vigente_hasta_periodo IS NULL)
    OR (vigente_hasta_tipo IS NOT NULL AND vigente_hasta_tipo = 'PERIODO' AND vigente_hasta_periodo IS NOT NULL AND vigente_hasta_fecha IS NULL)
);

ALTER TABLE regla ADD COLUMN vigente_desde_tipo VARCHAR(7);
ALTER TABLE regla ADD COLUMN vigente_desde_fecha DATE;
ALTER TABLE regla ADD COLUMN vigente_desde_periodo VARCHAR(6);
UPDATE regla SET vigente_desde_tipo = 'FECHA', vigente_desde_fecha = vigente_desde
    WHERE vigente_desde IS NOT NULL;
ALTER TABLE regla DROP COLUMN vigente_desde;
ALTER TABLE regla ADD CONSTRAINT ck_regla_vigente_desde CHECK (
    (vigente_desde_tipo IS NULL AND vigente_desde_fecha IS NULL AND vigente_desde_periodo IS NULL)
    OR (vigente_desde_tipo IS NOT NULL AND vigente_desde_tipo = 'FECHA' AND vigente_desde_fecha IS NOT NULL AND vigente_desde_periodo IS NULL)
    OR (vigente_desde_tipo IS NOT NULL AND vigente_desde_tipo = 'PERIODO' AND vigente_desde_periodo IS NOT NULL AND vigente_desde_fecha IS NULL)
);

ALTER TABLE regla ADD COLUMN vigente_hasta_tipo VARCHAR(7);
ALTER TABLE regla ADD COLUMN vigente_hasta_fecha DATE;
ALTER TABLE regla ADD COLUMN vigente_hasta_periodo VARCHAR(6);
UPDATE regla SET vigente_hasta_tipo = 'FECHA', vigente_hasta_fecha = vigente_hasta
    WHERE vigente_hasta IS NOT NULL;
ALTER TABLE regla DROP COLUMN vigente_hasta;
ALTER TABLE regla ADD CONSTRAINT ck_regla_vigente_hasta CHECK (
    (vigente_hasta_tipo IS NULL AND vigente_hasta_fecha IS NULL AND vigente_hasta_periodo IS NULL)
    OR (vigente_hasta_tipo IS NOT NULL AND vigente_hasta_tipo = 'FECHA' AND vigente_hasta_fecha IS NOT NULL AND vigente_hasta_periodo IS NULL)
    OR (vigente_hasta_tipo IS NOT NULL AND vigente_hasta_tipo = 'PERIODO' AND vigente_hasta_periodo IS NOT NULL AND vigente_hasta_fecha IS NULL)
);

ALTER TABLE regla ADD COLUMN afectacion_desde_tipo VARCHAR(7);
ALTER TABLE regla ADD COLUMN afectacion_desde_fecha DATE;
ALTER TABLE regla ADD COLUMN afectacion_desde_periodo VARCHAR(6);
ALTER TABLE regla ADD CONSTRAINT ck_regla_afectacion_desde CHECK (
    (afectacion_desde_tipo IS NULL AND afectacion_desde_fecha IS NULL AND afectacion_desde_periodo IS NULL)
    OR (afectacion_desde_tipo IS NOT NULL AND afectacion_desde_tipo = 'FECHA' AND afectacion_desde_fecha IS NOT NULL AND afectacion_desde_periodo IS NULL)
    OR (afectacion_desde_tipo IS NOT NULL AND afectacion_desde_tipo = 'PERIODO' AND afectacion_desde_periodo IS NOT NULL AND afectacion_desde_fecha IS NULL)
);

ALTER TABLE regla ADD COLUMN norma_afectada_id UUID;
ALTER TABLE regla ADD CONSTRAINT fk_regla_norma_afectada
    FOREIGN KEY (norma_afectada_id) REFERENCES norma (id);
ALTER TABLE regla ALTER COLUMN articulo_afectado SET DATA TYPE VARCHAR(300);

ALTER TABLE regla ADD CONSTRAINT ck_regla_hasta_con_desde CHECK (
    vigente_hasta_tipo IS NULL OR vigente_desde_tipo IS NOT NULL
);

ALTER TABLE regla ADD CONSTRAINT ck_regla_destino_afectacion CHECK (
    (tipo_afectacion IS NULL
        AND regla_afectada_id IS NULL
        AND articulo_afectado IS NULL
        AND norma_afectada_id IS NULL
        AND afectacion_desde_tipo IS NULL)
    OR (tipo_afectacion IS NOT NULL
        AND regla_afectada_id IS NOT NULL
        AND articulo_afectado IS NULL
        AND norma_afectada_id IS NULL)
    OR (tipo_afectacion IS NOT NULL
        AND regla_afectada_id IS NULL
        AND articulo_afectado IS NOT NULL
        AND norma_afectada_id IS NOT NULL)
);
