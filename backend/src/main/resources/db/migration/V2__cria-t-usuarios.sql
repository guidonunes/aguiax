CREATE TABLE t_usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_perfil BIGINT NOT NULL,
    nm_nome VARCHAR(100) NOT NULL,
    ds_email VARCHAR(150) NOT NULL,
    ds_senha VARCHAR(255) NOT NULL,
    ds_cargo VARCHAR(100),
    ds_departamento VARCHAR(100),
    nr_pontos INT DEFAULT 0,
    st_status INT DEFAULT 1,
    dt_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt_data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_usuarios_email UNIQUE (ds_email),
    CONSTRAINT fk_usuarios_perfil
        FOREIGN KEY (id_perfil)
        REFERENCES t_tipos_perfis(id_perfil)
);