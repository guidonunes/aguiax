CREATE TABLE t_categorias (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nm_categoria VARCHAR(80) NOT NULL,
    ds_descricao VARCHAR(255),

    CONSTRAINT uk_categorias_nome UNIQUE (nm_categoria)
);

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('INOVACAO', 'Ideias relacionadas à inovação de processos, produtos ou serviços');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('OPERACOES', 'Ideias relacionadas à melhoria das operações internas');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('SUSTENTABILIDADE', 'Ideias relacionadas à redução de impacto ambiental e uso eficiente de recursos');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('ATENDIMENTO', 'Ideias relacionadas à melhoria da experiência do cliente');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('TECNOLOGIA', 'Ideias relacionadas à automação, sistemas e soluções digitais');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('SEGURANCA', 'Ideias relacionadas à segurança operacional, do usuário ou dos colaboradores');

INSERT INTO t_categorias (nm_categoria, ds_descricao)
VALUES ('PRODUTIVIDADE', 'Ideias relacionadas à eficiência, redução de retrabalho e aumento de produtividade');