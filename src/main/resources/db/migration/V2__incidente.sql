CREATE TABLE incidente (
    id                    UUID          PRIMARY KEY,
    caso_de_uso           VARCHAR(40)   NOT NULL,
    paso                  VARCHAR(40)   NOT NULL,
    programa_id           UUID,
    version_consolidado   INT,
    causa                 VARCHAR(500)  NOT NULL,
    elementos             CLOB,
    primera_fecha         TIMESTAMP     NOT NULL,
    ultima_fecha          TIMESTAMP     NOT NULL,
    conteo                INT           NOT NULL,
    atendido              BOOLEAN       NOT NULL
);