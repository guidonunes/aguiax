package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.dto.projeto.MostrarStatusProjetoDTO;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.model.StatusProjeto;
import br.com.fiap.aguiax.repository.StatusProjetoRepository;
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