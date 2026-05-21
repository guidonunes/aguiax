package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.MostrarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.repository.StatusIdeiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusIdeiaService {

    private final StatusIdeiaRepository statusIdeiaRepository;

    public List<MostrarStatusIdeiaDTO> listarStatusIdeias() {
        return statusIdeiaRepository.findAll()
                .stream()
                .map(MostrarStatusIdeiaDTO::new)
                .toList();
    }

    public MostrarStatusIdeiaDTO buscarStatusIdeiaPorId(Long id) {
        return new MostrarStatusIdeiaDTO(buscarEntidadePorId(id));
    }

    public StatusIdeia buscarEntidadePorId(Long id) {
        return statusIdeiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status da ideia não encontrado."));
    }

    public StatusIdeia buscarEntidadePorStatus(NomeStatusIdeia status) {
        return statusIdeiaRepository.findByStatus(status)
                .orElseThrow(() -> new ResourceNotFoundException("Status da ideia não encontrado."));
    }
}