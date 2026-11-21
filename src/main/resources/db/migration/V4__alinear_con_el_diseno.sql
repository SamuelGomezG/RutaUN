ALTER TABLE programa ADD CONSTRAINT uq_programa_codigo_sia UNIQUE (codigo_sia);

ALTER TABLE regla ADD CONSTRAINT fk_regla_afectada FOREIGN KEY (regla_afectada_id) REFERENCES regla (id);

ALTER TABLE version_consolidado DROP CONSTRAINT fk_version_consolidado;
ALTER TABLE version_consolidado DROP COLUMN consolidado_id;
DROP TABLE consolidado;
