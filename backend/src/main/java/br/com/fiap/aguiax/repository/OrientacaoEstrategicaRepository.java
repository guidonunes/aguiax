package br.com.fiap.aguiax.repository;

import br.com.fiap.aguiax.model.OrientacaoEstrategica;
import br.com.fiap.aguiax.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrientacaoEstrategicaRepository extends JpaRepository<OrientacaoEstrategica, Long> {

    List<OrientacaoEstrategica> findByUsuario(Usuario usuario);

    List<OrientacaoEstrategica> findByStatus(Integer status);

    List<OrientacaoEstrategica> findByAreaEstrategicaContainingIgnoreCase(String areaEstrategica);

    boolean existsByTitulo(String titulo);
}