CREATE TABLE t_orientacoes_estrategicas (
        id_orientacao BIGINT AUTO_INCREMENT PRIMARY KEY,

        id_usuario BIGINT NOT NULL,

        ds_titulo VARCHAR(150) NOT NULL,
        ds_descricao VARCHAR(1000) NOT NULL,
        nm_area_estrategica VARCHAR(100),
        st_status INT DEFAULT 1,

        dt_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
        dt_data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

        CONSTRAINT fk_orientacoes_usuario
            FOREIGN KEY (id_usuario)
                REFERENCES t_usuarios(id_usuario)
);