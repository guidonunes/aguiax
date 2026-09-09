CREATE TABLE t_status_projeto (
    id_status_projeto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nm_status VARCHAR(50) NOT NULL,
    ds_descricao VARCHAR(255),

    CONSTRAINT uk_status_projeto_nome UNIQUE (nm_status)
);

INSERT INTO t_status_projeto (nm_status, ds_descricao)
VALUES ('PLANEJADO', 'Projeto criado, mas ainda não iniciado');

INSERT INTO t_status_projeto (nm_status, ds_descricao)
VALUES ('EM_ANDAMENTO', 'Projeto em execução');

INSERT INTO t_status_projeto (nm_status, ds_descricao)
VALUES ('CONCLUIDO', 'Projeto finalizado');

INSERT INTO t_status_projeto (nm_status, ds_descricao)
VALUES ('CANCELADO', 'Projeto interrompido ou cancelado');