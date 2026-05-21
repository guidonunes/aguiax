CREATE TABLE t_votos_ideia (
    id_voto BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT NOT NULL,
    id_ideia BIGINT NOT NULL,

    nr_valor INT DEFAULT 1,
    dt_voto DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_votos_ideia_usuario
        FOREIGN KEY (id_usuario)
            REFERENCES t_usuarios(id_usuario),

    CONSTRAINT fk_votos_ideia_ideia
        FOREIGN KEY (id_ideia)
            REFERENCES t_ideias(id_ideia),

    CONSTRAINT uk_votos_ideia_usuario_ideia
        UNIQUE (id_usuario, id_ideia)
);