package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.Badge;
import br.com.fiap.aguiax.model.Usuario;
import br.com.fiap.aguiax.model.UsuarioBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioBadgeRepository extends JpaRepository<UsuarioBadge, Long> {

    List<UsuarioBadge> findByUsuario(Usuario usuario);

    List<UsuarioBadge> findByBadge(Badge badge);

    Optional<UsuarioBadge> findByUsuarioAndBadge(Usuario usuario, Badge badge);

    boolean existsByUsuarioAndBadge(Usuario usuario, Badge badge);

    // Unique recipient/badge pairs make concurrent awards a no-op, preserving dates.
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO t_usuario_badges (id_usuario, id_badge, dt_conquista)
            SELECT u.id_usuario, b.id_badge, CURRENT_TIMESTAMP
            FROM t_usuarios u
            JOIN t_tipos_perfis p ON p.id_perfil = u.id_perfil
            JOIN t_badges b ON b.nm_badge = :nomeBadge AND b.st_status = 1
            WHERE u.id_usuario = :usuarioId AND p.nm_perfil = 'OPERACIONAL'
            ON DUPLICATE KEY UPDATE dt_conquista = t_usuario_badges.dt_conquista
            """, nativeQuery = true)
    void concederMarcoSeAusente(@Param("usuarioId") Long usuarioId,
                              @Param("nomeBadge") String nomeBadge);
}
