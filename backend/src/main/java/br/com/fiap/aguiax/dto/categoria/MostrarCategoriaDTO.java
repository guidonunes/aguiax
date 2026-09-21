package br.com.fiap.aguiax.dto.categoria;

import br.com.fiap.aguiax.model.Categoria;

public record MostrarCategoriaDTO(
        Long id,
        String nomeCategoria,
        String descricao
) {
    public MostrarCategoriaDTO(Categoria categoria) {
        this(
                categoria.getId(),
                categoria.getNomeCategoria(),
                categoria.getDescricao()
        );
    }
}