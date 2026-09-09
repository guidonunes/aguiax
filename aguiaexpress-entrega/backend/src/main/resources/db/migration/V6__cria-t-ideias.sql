CREATE TABLE t_ideias (
    id_ideia BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    id_status_ideia BIGINT NOT NULL,
    id_orientacao BIGINT NOT NULL,

    ds_titulo VARCHAR(150) NOT NULL,
    ds_descricao VARCHAR(1000) NOT NULL,
    ds_problema VARCHAR(1000),
    ds_solucao_proposta VARCHAR(1000),

    nr_impacto INT DEFAULT 0,
    nr_viabilidade INT DEFAULT 0,
    nr_prioridade INT DEFAULT 0,
    nr_total_votos INT DEFAULT 0,

    dt_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt_data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ideias_usuario
        FOREIGN KEY (id_usuario)
            REFERENCES t_usuarios(id_usuario),

    CONSTRAINT fk_ideias_categoria
        FOREIGN KEY (id_categoria)
            REFERENCES t_categorias(id_categoria),

    CONSTRAINT fk_ideias_status_ideia
        FOREIGN KEY (id_status_ideia)
            REFERENCES t_status_ideia(id_status_ideia),

    CONSTRAINT fk_ideias_orientacao
        FOREIGN KEY (id_orientacao)
            REFERENCES t_orientacoes_estrategicas(id_orientacao)
);