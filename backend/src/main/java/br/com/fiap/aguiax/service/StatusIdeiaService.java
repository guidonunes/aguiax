package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.dto.ideia.MostrarStatusIdeiaDTO;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.NomeStatusIdeia;
import br.com.fiap.aguiax.model.StatusIdeia;
import br.com.fiap.aguiax.repository.StatusIdeiaRepository;
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