package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.model.VotoIdeia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VotoIdeiaRepository extends JpaRepository<VotoIdeia, Long> {

    List<VotoIdeia> findByIdeia(Ideia ideia);

    List<VotoIdeia> findByUsuario(Usuario usuario);

    Optional<VotoIdeia> findByUsuarioAndIdeia(Usuario usuario, Ideia ideia);

    boolean existsByUsuarioAndIdeia(Usuario usuario, Ideia ideia);

    long countByIdeia(Ideia ideia);
}