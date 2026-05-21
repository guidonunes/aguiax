CREATE TABLE t_tipos_perfis (
    id_perfil BIGINT AUTO_INCREMENT PRIMARY KEY,
    nm_perfil VARCHAR(50) NOT NULL,
    ds_descricao VARCHAR(255),

    CONSTRAINT uk_tipos_perfis_nome UNIQUE (nm_perfil)
);

INSERT INTO t_tipos_perfis (nm_perfil, ds_descricao)
VALUES ('OPERACIONAL', 'Usuário operacional que cadastra e acompanha ideias');

INSERT INTO t_tipos_perfis (nm_perfil, ds_descricao)
VALUES ('GESTOR', 'Usuário gestor que prioriza ideias e acompanha projetos');

INSERT INTO t_tipos_perfis (nm_perfil, ds_descricao)
VALUES ('LIDER', 'Usuário líder que gerencia orientações estratégicas e visualiza resultados');
