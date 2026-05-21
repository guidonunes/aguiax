package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByNomeBadge(String nomeBadge);

    boolean existsByNomeBadge(String nomeBadge);

    List<Badge> findByStatus(Integer status);
}