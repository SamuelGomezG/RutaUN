CREATE TABLE programa (
    id         UUID         PRIMARY KEY,
    nombre     VARCHAR(200) NOT NULL,
    codigo_sia VARCHAR(20)  NOT NULL,
    facultad   VARCHAR(100) NOT NULL
);

CREATE TABLE materia (
    codigo   VARCHAR(20) PRIMARY KEY,
    creditos INT         NOT NULL
);

CREATE TABLE norma (
    id                   UUID         PRIMARY KEY,
    tipo                 VARCHAR(20)  NOT NULL,
    numero               VARCHAR(20)  NOT NULL,
    anio                 INT          NOT NULL,
    organo               VARCHAR(100) NOT NULL,
    tipo_de_efecto       VARCHAR(40)  NOT NULL,
    fecha_expedicion     DATE         NOT NULL,
    aplica_desde         DATE         NOT NULL,
    vigente_hasta        DATE,
    enlace_regimen_legal VARCHAR(500),
    tipo_alcance         VARCHAR(20)  NOT NULL,
    alcance_detalle      CLOB,
    CONSTRAINT uq_norma_identificacion UNIQUE (tipo, numero, anio, organo)
);

CREATE TABLE regla (
    id                UUID         PRIMARY KEY,
    norma_id          UUID         NOT NULL,
    categoria         VARCHAR(40)  NOT NULL,
    objeto            VARCHAR(200) NOT NULL,
    enunciado         VARCHAR(2000) NOT NULL,
    fuente            VARCHAR(300) NOT NULL,
    vigente_desde     DATE,
    vigente_hasta     DATE,
    clasificacion     VARCHAR(20)  NOT NULL,
    tipo_afectacion   VARCHAR(20),
    regla_afectada_id UUID,
    articulo_afectado VARCHAR(100),
    datos             CLOB,
    CONSTRAINT fk_regla_norma FOREIGN KEY (norma_id) REFERENCES norma (id)
);

CREATE TABLE consolidado (
    id              UUID   PRIMARY KEY,
    programa_id     UUID   NOT NULL,
    version_bloqueo BIGINT NOT NULL,
    CONSTRAINT uq_consolidado_programa UNIQUE (programa_id),
    CONSTRAINT fk_consolidado_programa FOREIGN KEY (programa_id) REFERENCES programa (id)
);

CREATE TABLE version_consolidado (
    id                      UUID         PRIMARY KEY,
    consolidado_id          UUID         NOT NULL,
    programa_id             UUID         NOT NULL,
    numero                  INT          NOT NULL,
    fecha                   DATE         NOT NULL,
    motivo                  VARCHAR(40)  NOT NULL,
    detalle_motivo          VARCHAR(300),
    administrador_id        UUID,
    numero_plan_de_estudios INT          NOT NULL,
    cambios                 CLOB,
    CONSTRAINT uq_version_programa_numero UNIQUE (programa_id, numero),
    CONSTRAINT fk_version_consolidado FOREIGN KEY (consolidado_id) REFERENCES consolidado (id),
    CONSTRAINT fk_version_programa FOREIGN KEY (programa_id) REFERENCES programa (id)
);

CREATE TABLE regla_en_version (
    id         UUID        PRIMARY KEY,
    version_id UUID        NOT NULL,
    regla_id   UUID        NOT NULL,
    estado     VARCHAR(20) NOT NULL,
    CONSTRAINT uq_regla_en_version UNIQUE (version_id, regla_id),
    CONSTRAINT fk_riv_version FOREIGN KEY (version_id) REFERENCES version_consolidado (id),
    CONSTRAINT fk_riv_regla FOREIGN KEY (regla_id) REFERENCES regla (id)
);