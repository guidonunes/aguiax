package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByNomeBadge(String nomeBadge);

    boolean existsByNomeBadge(String nomeBadge);

    List<Badge> findByStatus(Integer status);
}