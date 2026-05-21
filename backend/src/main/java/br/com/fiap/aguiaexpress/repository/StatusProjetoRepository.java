package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusProjetoRepository extends JpaRepository<StatusProjeto, Long> {

    Optional<StatusProjeto> findByStatus(NomeStatusProjeto status);
}