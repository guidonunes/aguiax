CREATE TABLE t_status_ideia (
    id_status_ideia BIGINT AUTO_INCREMENT PRIMARY KEY,
    nm_status VARCHAR(50) NOT NULL,
    ds_descricao VARCHAR(255),

    CONSTRAINT uk_status_ideia_nome UNIQUE (nm_status)
);

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('NOVA', 'Ideia cadastrada e aguardando análise inicial');

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('EM_ANALISE', 'Ideia em análise por gestores');

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('PRIORIZADA', 'Ideia priorizada para avaliação ou execução futura');

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('APROVADA', 'Ideia aprovada para virar projeto ou iniciativa');

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('REJEITADA', 'Ideia rejeitada após análise');

INSERT INTO t_status_ideia (nm_status, ds_descricao)
VALUES ('CONVERTIDA_EM_PROJETO', 'Ideia aprovada e convertida em projeto');