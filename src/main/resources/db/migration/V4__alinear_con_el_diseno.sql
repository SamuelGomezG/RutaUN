ALTER TABLE programa ADD CONSTRAINT uq_programa_codigo_sia UNIQUE (codigo_sia);

ALTER TABLE regla ADD CONSTRAINT fk_regla_afectada FOREIGN KEY (regla_afectada_id) REFERENCES regla (id);
