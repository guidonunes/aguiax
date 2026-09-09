CREATE TABLE t_projetos (
    id_projeto BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_ideia BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    id_status_projeto BIGINT NOT NULL,

    ds_titulo VARCHAR(150) NOT NULL,
    ds_descricao VARCHAR(1000),
    ds_objetivo VARCHAR(1000),

    vl_investimento DECIMAL(12, 2) DEFAULT 0.00,
    vl_roi DECIMAL(10, 2) DEFAULT 0.00,
    vl_retorno_financeiro DECIMAL(12, 2) DEFAULT 0.00,
    vl_reducao_custos DECIMAL(12, 2) DEFAULT 0.00,
    vl_ganho_produtividade DECIMAL(10, 2) DEFAULT 0.00,

    dt_inicio DATE,
    dt_previsao_fim DATE,
    dt_conclusao DATE,

    dt_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt_data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_projetos_ideia UNIQUE (id_ideia),

    CONSTRAINT fk_projetos_ideia
        FOREIGN KEY (id_ideia)
            REFERENCES t_ideias(id_ideia),

    CONSTRAINT fk_projetos_usuario
        FOREIGN KEY (id_usuario)
            REFERENCES t_usuarios(id_usuario),

    CONSTRAINT fk_projetos_status_projeto
        FOREIGN KEY (id_status_projeto)
            REFERENCES t_status_projeto(id_status_projeto)
);