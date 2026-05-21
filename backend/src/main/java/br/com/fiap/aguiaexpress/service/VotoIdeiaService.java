package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.MostrarVotoIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.model.VotoIdeia;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import br.com.fiap.aguiaexpress.repository.VotoIdeiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VotoIdeiaService {

    private final VotoIdeiaRepository votoIdeiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdeiaService ideiaService;
    private final IdeiaRepository ideiaRepository;

    public MostrarVotoIdeiaDTO votarIdeia(Long usuarioId, Long ideiaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Ideia ideia = ideiaService.buscarEntidadePorId(ideiaId);

        if (votoIdeiaRepository.existsByUsuarioAndIdeia(usuario, ideia)) {
            throw new DuplicateResourceException("Usuário já votou nessa ideia.");
        }

        VotoIdeia votoIdeia = new VotoIdeia();

        votoIdeia.setUsuario(usuario);
        votoIdeia.setIdeia(ideia);
        votoIdeia.setValor(1);
        votoIdeia.setDataVoto(LocalDateTime.now());

        VotoIdeia votoSalvo = votoIdeiaRepository.save(votoIdeia);

        atualizarTotalVotosEPrioridade(ideia);

        return new MostrarVotoIdeiaDTO(votoSalvo);
    }

    public List<MostrarVotoIdeiaDTO> listarVotos() {
        return votoIdeiaRepository.findAll()
                .stream()
                .map(MostrarVotoIdeiaDTO::new)
                .toList();
    }

    public MostrarVotoIdeiaDTO buscarVotoPorId(Long id) {
        VotoIdeia votoIdeia = buscarEntidadePorId(id);

        return new MostrarVotoIdeiaDTO(votoIdeia);
    }

    public List<MostrarVotoIdeiaDTO> listarVotosPorIdeia(Long ideiaId) {
        Ideia ideia = ideiaService.buscarEntidadePorId(ideiaId);

        return votoIdeiaRepository.findByIdeia(ideia)
                .stream()
                .map(MostrarVotoIdeiaDTO::new)
                .toList();
    }

    public List<MostrarVotoIdeiaDTO> listarVotosPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return votoIdeiaRepository.findByUsuario(usuario)
                .stream()
                .map(MostrarVotoIdeiaDTO::new)
                .toList();
    }

    public void removerVoto(Long usuarioId, Long ideiaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Ideia ideia = ideiaService.buscarEntidadePorId(ideiaId);

        VotoIdeia votoIdeia = votoIdeiaRepository.findByUsuarioAndIdeia(usuario, ideia)
                .orElseThrow(() -> new ResourceNotFoundException("Voto não encontrado para esse usuário e ideia."));

        votoIdeiaRepository.delete(votoIdeia);

        atualizarTotalVotosEPrioridade(ideia);
    }

    public VotoIdeia buscarEntidadePorId(Long id) {
        return votoIdeiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voto não encontrado."));
    }

    private void atualizarTotalVotosEPrioridade(Ideia ideia) {
        long totalVotos = votoIdeiaRepository.countByIdeia(ideia);

        ideia.setTotalVotos((int) totalVotos);

        int impacto = ideia.getImpacto() != null ? ideia.getImpacto() : 0;
        int viabilidade = ideia.getViabilidade() != null ? ideia.getViabilidade() : 0;
        int votos = ideia.getTotalVotos() != null ? ideia.getTotalVotos() : 0;

        ideia.setPrioridade(impacto + viabilidade + votos);
        ideia.setDataAtualizacao(LocalDateTime.now());

        ideiaRepository.save(ideia);
    }
}