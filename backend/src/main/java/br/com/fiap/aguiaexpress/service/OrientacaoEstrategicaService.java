package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.MostrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrientacaoEstrategicaService {

    private final AuditService auditService;
    private final OrientacaoEstrategicaRepository orientacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public MostrarOrientacaoDTO criarOrientacao(RegistrarOrientacaoDTO orientacaoDTO) {
        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        validarUsuarioLider(usuario);

        if (orientacaoRepository.existsByTitulo(orientacaoDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma orientação estratégica com esse título.");
        }

        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();

        orientacao.setUsuario(usuario);
        orientacao.setTitulo(orientacaoDTO.titulo());
        orientacao.setDescricao(orientacaoDTO.descricao());
        orientacao.setAreaEstrategica(orientacaoDTO.areaEstrategica());
        orientacao.setStatus(1);
        orientacao.setDataCriacao(LocalDateTime.now());
        orientacao.setDataAtualizacao(LocalDateTime.now());

        OrientacaoEstrategica orientacaoSalva =
                orientacaoRepository.save(orientacao);

        auditService.registrarEvento(
                AuditEventType.ORIENTATION_CREATED,
                usuario.getId(),
                AuditResourceType.ORIENTATION,
                orientacaoSalva.getId().toString(),
                Map.of("status", orientacaoSalva.getStatus())
        );

        return new MostrarOrientacaoDTO(orientacaoSalva);
    }

    public List<MostrarOrientacaoDTO> listarOrientacoes() {
        return orientacaoRepository.findAll()
                .stream()
                .map(MostrarOrientacaoDTO::new)
                .toList();
    }

    public List<MostrarOrientacaoDTO> listarOrientacoesAtivas() {
        return orientacaoRepository.findByStatus(1)
                .stream()
                .map(MostrarOrientacaoDTO::new)
                .toList();
    }

    public MostrarOrientacaoDTO buscarOrientacaoPorId(Long id) {
        OrientacaoEstrategica orientacao = buscarEntidadePorId(id);

        return new MostrarOrientacaoDTO(orientacao);
    }

    public List<MostrarOrientacaoDTO> buscarOrientacoesPorArea(String areaEstrategica) {
        return orientacaoRepository.findByAreaEstrategicaContainingIgnoreCase(areaEstrategica)
                .stream()
                .map(MostrarOrientacaoDTO::new)
                .toList();
    }

    public MostrarOrientacaoDTO atualizarOrientacao(Long id, RegistrarOrientacaoDTO orientacaoDTO) {
        OrientacaoEstrategica orientacao = buscarEntidadePorId(id);

        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        validarUsuarioLider(usuario);

        if (!orientacao.getTitulo().equals(orientacaoDTO.titulo())
                && orientacaoRepository.existsByTitulo(orientacaoDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma orientação estratégica com esse título.");
        }

        boolean titleChanged = !Objects.equals(
                orientacao.getTitulo(),
                orientacaoDTO.titulo()
        );
        boolean strategicAreaChanged = !Objects.equals(
                orientacao.getAreaEstrategica(),
                orientacaoDTO.areaEstrategica()
        );

        orientacao.setUsuario(usuario);
        orientacao.setTitulo(orientacaoDTO.titulo());
        orientacao.setDescricao(orientacaoDTO.descricao());
        orientacao.setAreaEstrategica(orientacaoDTO.areaEstrategica());
        orientacao.setDataAtualizacao(LocalDateTime.now());

        OrientacaoEstrategica orientacaoAtualizada = orientacaoRepository.save(orientacao);

        auditService.registrarEvento(
                AuditEventType.ORIENTATION_UPDATED,
                usuario.getId(),
                AuditResourceType.ORIENTATION,
                orientacaoAtualizada.getId().toString(),
                Map.of(
                        "titleChanged", titleChanged,
                        "strategicAreaChanged", strategicAreaChanged
                )
        );

        return new MostrarOrientacaoDTO(orientacaoAtualizada);
    }

    public void deletarOrientacao(Long id) {
        OrientacaoEstrategica orientacao = buscarEntidadePorId(id);

        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        validarUsuarioLider(usuario);

        Integer previousStatus = orientacao.getStatus();

        orientacao.setStatus(0);
        orientacao.setDataAtualizacao(LocalDateTime.now());

        orientacaoRepository.save(orientacao);

        auditService.registrarEvento(
                AuditEventType.ORIENTATION_UPDATED,
                usuario.getId(),
                AuditResourceType.ORIENTATION,
                orientacao.getId().toString(),
                Map.of(
                        "previousStatus", Objects.toString(previousStatus, "UNKNOWN"),
                        "newStatus", "0"
                )
        );
    }

    public OrientacaoEstrategica buscarEntidadePorId(Long id) {
        return orientacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orientação estratégica não encontrada."));
    }

    private void validarUsuarioLider(Usuario usuario) {
        if (!usuario.getTipoPerfil().getNomePerfil().equals(NomePerfil.LIDER)) {
            throw new UnauthorizedActionException("Apenas líderes podem gerenciar orientações estratégicas.");
        }
    }
}
