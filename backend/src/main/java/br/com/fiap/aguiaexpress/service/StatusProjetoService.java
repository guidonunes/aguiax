package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.MostrarStatusProjetoDTO;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import br.com.fiap.aguiaexpress.repository.StatusProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusProjetoService {

    private final StatusProjetoRepository statusProjetoRepository;

    public List<MostrarStatusProjetoDTO> listarStatusProjetos() {
        return statusProjetoRepository.findAll()
                .stream()
                .map(MostrarStatusProjetoDTO::new)
                .toList();
    }

    public MostrarStatusProjetoDTO buscarStatusProjetoPorId(Long id) {
        return new MostrarStatusProjetoDTO(buscarEntidadePorId(id));
    }

    public StatusProjeto buscarEntidadePorId(Long id) {
        return statusProjetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status do projeto não encontrado."));
    }

    public StatusProjeto buscarEntidadePorStatus(NomeStatusProjeto status) {
        return statusProjetoRepository.findByStatus(status)
                .orElseThrow(() -> new ResourceNotFoundException("Status do projeto não encontrado."));
    }
}