-- Backfill defined milestones without replacing manual awards or conquest dates.
-- TOP_COLABORADOR and MESTRE_DA_INOVACAO still require explicit thresholds.
INSERT INTO t_usuario_badges (id_usuario, id_badge, dt_conquista)
SELECT DISTINCT u.id_usuario, b.id_badge, CURRENT_TIMESTAMP
FROM t_usuarios u
JOIN t_tipos_perfis p ON p.id_perfil = u.id_perfil
JOIN t_ideias i ON i.id_usuario = u.id_usuario
JOIN t_status_ideia s ON s.id_status_ideia = i.id_status_ideia
JOIN t_badges b ON b.st_status = 1 AND (
    b.nm_badge = 'PRIMEIRA_IDEIA'
    OR (b.nm_badge = 'IDEIA_APROVADA'
        AND s.nm_status IN ('APROVADA', 'CONVERTIDA_EM_PROJETO'))
    OR (b.nm_badge = 'PROJETO_IMPLEMENTADO'
        AND s.nm_status = 'CONVERTIDA_EM_PROJETO')
)
WHERE p.nm_perfil = 'OPERACIONAL'
ON DUPLICATE KEY UPDATE dt_conquista = t_usuario_badges.dt_conquista;
