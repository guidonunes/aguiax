package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.Badge;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.model.UsuarioBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioBadgeRepository extends JpaRepository<UsuarioBadge, Long> {

    List<UsuarioBadge> findByUsuario(Usuario usuario);

    List<UsuarioBadge> findByBadge(Badge badge);

    Optional<UsuarioBadge> findByUsuarioAndBadge(Usuario usuario, Badge badge);

    boolean existsByUsuarioAndBadge(Usuario usuario, Badge badge);
}