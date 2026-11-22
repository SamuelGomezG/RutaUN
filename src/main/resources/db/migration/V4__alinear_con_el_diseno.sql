ALTER TABLE programa ADD CONSTRAINT uq_programa_codigo_sia UNIQUE (codigo_sia);

ALTER TABLE regla ADD CONSTRAINT fk_regla_afectada FOREIGN KEY (regla_afectada_id) REFERENCES regla (id);

ALTER TABLE version_consolidado DROP CONSTRAINT fk_version_consolidado;
ALTER TABLE version_consolidado DROP COLUMN consolidado_id;
DROP TABLE consolidado;

ALTER TABLE version_consolidado ADD COLUMN dia_automatico DATE;

UPDATE version_consolidado SET dia_automatico = fecha WHERE motivo = 'AUTOMATICO_POR_FECHA';

ALTER TABLE version_consolidado ADD CONSTRAINT ck_version_dia_automatico CHECK (
    (motivo = 'AUTOMATICO_POR_FECHA' AND dia_automatico IS NOT NULL AND dia_automatico = fecha)
    OR (motivo <> 'AUTOMATICO_POR_FECHA' AND dia_automatico IS NULL)
);

ALTER TABLE version_consolidado ADD CONSTRAINT uq_version_automatica_por_dia UNIQUE (programa_id, dia_automatico);
