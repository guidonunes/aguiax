CREATE TABLE t_usuario_badges (
    id_usuario_badge BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT NOT NULL,
    id_badge BIGINT NOT NULL,

    dt_conquista DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_badges_usuario
        FOREIGN KEY (id_usuario)
            REFERENCES t_usuarios(id_usuario),

    CONSTRAINT fk_usuario_badges_badge
        FOREIGN KEY (id_badge)
            REFERENCES t_badges(id_badge),

    CONSTRAINT uk_usuario_badges_usuario_badge
        UNIQUE (id_usuario, id_badge)
);