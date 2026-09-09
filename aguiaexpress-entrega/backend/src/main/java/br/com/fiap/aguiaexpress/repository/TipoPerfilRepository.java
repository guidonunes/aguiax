package br.com.fiap.aguiaexpress.repository;


import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoPerfilRepository extends JpaRepository<TipoPerfil, Long> {

    Optional<TipoPerfil> findByNomePerfil(NomePerfil nomePerfil);
}