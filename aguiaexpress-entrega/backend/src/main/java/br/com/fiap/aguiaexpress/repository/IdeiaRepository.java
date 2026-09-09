package br.com.fiap.aguiaexpress.repository;

import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdeiaRepository extends JpaRepository<Ideia, Long> {

    List<Ideia> findByUsuario(Usuario usuario);

    List<Ideia> findByCategoria(Categoria categoria);

    List<Ideia> findByStatusIdeia(StatusIdeia statusIdeia);

    List<Ideia> findByOrientacaoEstrategica(OrientacaoEstrategica orientacaoEstrategica);

    List<Ideia> findByTituloContainingIgnoreCase(String titulo);

    boolean existsByTitulo(String titulo);
}