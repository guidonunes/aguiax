package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.model.StatusProjeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusProjetoRepository extends JpaRepository<StatusProjeto, Long> {

    Optional<StatusProjeto> findByStatus(NomeStatusProjeto status);
}