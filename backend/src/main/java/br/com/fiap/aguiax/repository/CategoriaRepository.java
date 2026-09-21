package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNomeCategoria(String nomeCategoria);
    boolean existsByNomeCategoria(String nomeCategoria);
}
