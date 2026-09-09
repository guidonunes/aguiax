package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.AtualizarCategoriaDTO;
import br.com.fiap.aguiaexpress.dto.MostrarCategoriaDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarCategoriaDTO;
import br.com.fiap.aguiaexpress.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<MostrarCategoriaDTO> criarCategoria(
            @RequestBody @Valid RegistrarCategoriaDTO categoriaDTO
    ) {
        MostrarCategoriaDTO categoriaCriada = categoriaService.criarCategoria(categoriaDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaCriada);
    }

    @GetMapping
    public ResponseEntity<List<MostrarCategoriaDTO>> listarCategorias() {
        List<MostrarCategoriaDTO> categorias = categoriaService.listarCategorias();

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarCategoriaDTO> buscarCategoriaPorId(
            @PathVariable Long id
    ) {
        MostrarCategoriaDTO categoria = categoriaService.buscarCategoriaPorId(id);

        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<MostrarCategoriaDTO> atualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarCategoriaDTO categoriaDTO
    ) {
        MostrarCategoriaDTO categoriaAtualizada = categoriaService.atualizarCategoria(id, categoriaDTO);

        return ResponseEntity.ok(categoriaAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<Void> deletarCategoria(
            @PathVariable Long id
    ) {
        categoriaService.deletarCategoria(id);

        return ResponseEntity.noContent().build();
    }
}
