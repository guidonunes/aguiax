package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.Ideia;
import br.com.fiap.aguiax.model.Projeto;
import br.com.fiap.aguiax.model.StatusProjeto;
import br.com.fiap.aguiax.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    Optional<Projeto> findByIdeia(Ideia ideia);

    boolean existsByIdeia(Ideia ideia);

    List<Projeto> findByUsuario(Usuario usuario);

    List<Projeto> findByStatusProjeto(StatusProjeto statusProjeto);

    List<Projeto> findByTituloContainingIgnoreCase(String titulo);

    List<Projeto> findByIdeia_OrientacaoEstrategica_Id(Long orientacaoId);
}
