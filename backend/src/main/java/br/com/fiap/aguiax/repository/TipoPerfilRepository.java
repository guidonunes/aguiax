package br.com.fiap.aguiax.repository;


import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.TipoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoPerfilRepository extends JpaRepository<TipoPerfil, Long> {

    Optional<TipoPerfil> findByNomePerfil(NomePerfil nomePerfil);
}