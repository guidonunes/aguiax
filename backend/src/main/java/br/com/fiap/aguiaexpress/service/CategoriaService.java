package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.AtualizarCategoriaDTO;
import br.com.fiap.aguiaexpress.dto.MostrarCategoriaDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarCategoriaDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public MostrarCategoriaDTO criarCategoria(RegistrarCategoriaDTO categoriaDTO) {
        if (categoriaRepository.existsByNomeCategoria(categoriaDTO.nomeCategoria())) {
            throw new DuplicateResourceException("Categoria já cadastrada.");
        }

        Categoria categoria = new Categoria();
        categoria.setNomeCategoria(categoriaDTO.nomeCategoria());
        categoria.setDescricao(categoriaDTO.descricao());

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return new MostrarCategoriaDTO(categoriaSalva);
    }

    public List<MostrarCategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(MostrarCategoriaDTO::new)
                .toList();
    }

    public MostrarCategoriaDTO buscarCategoriaPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));

        return new MostrarCategoriaDTO(categoria);
    }

    public Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    public MostrarCategoriaDTO atualizarCategoria(Long id, AtualizarCategoriaDTO categoriaDTO) {
        Categoria categoria = buscarEntidadePorId(id);

        if (!categoria.getNomeCategoria().equals(categoriaDTO.nomeCategoria())
                && categoriaRepository.existsByNomeCategoria(categoriaDTO.nomeCategoria())) {
            throw new DuplicateResourceException("Categoria já cadastrada.");
        }

        categoria.setNomeCategoria(categoriaDTO.nomeCategoria());
        categoria.setDescricao(categoriaDTO.descricao());

        Categoria categoriaAtualizada = categoriaRepository.save(categoria);

        return new MostrarCategoriaDTO(categoriaAtualizada);
    }

    public void deletarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada.");
        }

        categoriaRepository.deleteById(id);
    }
}