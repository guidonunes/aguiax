package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.NomeStatusIdeia;
import br.com.fiap.aguiax.model.StatusIdeia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusIdeiaRepository extends JpaRepository<StatusIdeia, Long> {
    Optional<StatusIdeia> findByStatus(NomeStatusIdeia status);
}
