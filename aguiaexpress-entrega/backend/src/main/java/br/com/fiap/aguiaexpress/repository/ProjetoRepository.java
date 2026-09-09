package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.Projeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import br.com.fiap.aguiaexpress.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    Optional<Projeto> findByIdeia(Ideia ideia);

    boolean existsByIdeia(Ideia ideia);

    List<Projeto> findByUsuario(Usuario usuario);

    List<Projeto> findByStatusProjeto(StatusProjeto statusProjeto);

    List<Projeto> findByTituloContainingIgnoreCase(String titulo);
}