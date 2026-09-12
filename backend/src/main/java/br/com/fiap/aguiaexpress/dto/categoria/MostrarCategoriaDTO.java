package br.com.fiap.aguiaexpress.dto.categoria;

import br.com.fiap.aguiaexpress.model.Categoria;

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