package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusIdeiaRepository extends JpaRepository<StatusIdeia, Long> {
    Optional<StatusIdeia> findByStatus(NomeStatusIdeia status);
}
