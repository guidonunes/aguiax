UPDATE t_ideias
SET nr_prioridade = 0
WHERE nr_prioridade IS NULL;

ALTER TABLE t_ideias
    MODIFY COLUMN nr_prioridade DECIMAL(4, 2) NOT NULL DEFAULT 0.00;
