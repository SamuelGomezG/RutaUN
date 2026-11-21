ALTER TABLE programa ADD COLUMN facultad_codigo VARCHAR(20) NOT NULL;

ALTER TABLE norma DROP CONSTRAINT uq_norma_identificacion;
ALTER TABLE norma RENAME COLUMN organo TO organo_nombre;
ALTER TABLE norma ADD COLUMN organo_nivel VARCHAR(20) NOT NULL;
ALTER TABLE norma ADD COLUMN organo_facultad_codigo VARCHAR(20);
ALTER TABLE norma ADD COLUMN organo_clave VARCHAR(125) NOT NULL;

ALTER TABLE norma ADD CONSTRAINT ck_norma_organo_nivel CHECK (
    organo_nivel IN ('NACIONAL', 'SEDE', 'FACULTAD')
);

ALTER TABLE norma ADD CONSTRAINT ck_norma_organo_facultad CHECK (
    (organo_nivel = 'FACULTAD' AND organo_facultad_codigo IS NOT NULL)
    OR (organo_nivel <> 'FACULTAD' AND organo_facultad_codigo IS NULL)
);

ALTER TABLE norma ADD CONSTRAINT uq_norma_identificacion
    UNIQUE (tipo, numero, anio, organo_clave);
