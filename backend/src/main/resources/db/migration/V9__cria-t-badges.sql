CREATE TABLE t_badges (
    id_badge BIGINT AUTO_INCREMENT PRIMARY KEY,
    nm_badge VARCHAR(80) NOT NULL,
    ds_descricao VARCHAR(255),
    ds_criterio VARCHAR(255),
    ds_icone VARCHAR(100),
    st_status INT DEFAULT 1,

    CONSTRAINT uk_badges_nome UNIQUE (nm_badge)
);

INSERT INTO t_badges (nm_badge, ds_descricao, ds_criterio, ds_icone, st_status)
VALUES (
           'PRIMEIRA_IDEIA',
           'Concedida ao usuário que cadastrou sua primeira ideia.',
           'Cadastrar pelo menos uma ideia no sistema.',
           'primeira_ideia',
           1
       );

INSERT INTO t_badges (nm_badge, ds_descricao, ds_criterio, ds_icone, st_status)
VALUES (
           'IDEIA_APROVADA',
           'Concedida ao usuário que teve uma ideia aprovada.',
           'Ter pelo menos uma ideia com status APROVADA.',
           'ideia_aprovada',
           1
       );

INSERT INTO t_badges (nm_badge, ds_descricao, ds_criterio, ds_icone, st_status)
VALUES (
           'TOP_COLABORADOR',
           'Concedida ao usuário com alta participação no envio de ideias.',
           'Cadastrar múltiplas ideias ou alcançar alta pontuação.',
           'top_colaborador',
           1
       );

INSERT INTO t_badges (nm_badge, ds_descricao, ds_criterio, ds_icone, st_status)
VALUES (
           'PROJETO_IMPLEMENTADO',
           'Concedida quando uma ideia do usuário é convertida em projeto.',
           'Ter uma ideia convertida em projeto.',
           'projeto_implementado',
           1
       );

INSERT INTO t_badges (nm_badge, ds_descricao, ds_criterio, ds_icone, st_status)
VALUES (
           'MESTRE_DA_INOVACAO',
           'Concedida ao usuário com impacto relevante na geração de ideias.',
           'Acumular pontos, votos ou ideias aprovadas de forma consistente.',
           'mestre_inovacao',
           1
       );