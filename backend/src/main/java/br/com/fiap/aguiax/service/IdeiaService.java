package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.service.AuditService;
import br.com.fiap.aguiax.config.observability.BusinessMetrics;
import br.com.fiap.aguiax.dto.ideia.AtualizarAvaliacaoIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.RegistrarIdeiaDTO;
import br.com.fiap.aguiax.exception.DuplicateResourceException;
import br.com.fiap.aguiax.exception.InvalidRequestException;
import br.com.fiap.aguiax.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.exception.UnauthorizedActionException;
import br.com.fiap.aguiax.model.*;
import br.com.fiap.aguiax.repository.IdeiaRepository;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdeiaService {

    private static final Map<NomeStatusIdeia, Set<NomeStatusIdeia>>
            ALLOWED_STATUS_TRANSITIONS = Map.of(
                    NomeStatusIdeia.NOVA,
                    Set.of(NomeStatusIdeia.EM_ANALISE),
                    NomeStatusIdeia.EM_ANALISE,
                    Set.of(
                            NomeStatusIdeia.PRIORIZADA,
                            NomeStatusIdeia.APROVADA,
                            NomeStatusIdeia.REJEITADA
                    ),
                    NomeStatusIdeia.PRIORIZADA,
                    Set.of(
                            NomeStatusIdeia.APROVADA,
                            NomeStatusIdeia.REJEITADA
                    ),
                    NomeStatusIdeia.APROVADA,
                    Set.of(NomeStatusIdeia.CONVERTIDA_EM_PROJETO),
                    NomeStatusIdeia.REJEITADA,
                    Set.of(),
                    NomeStatusIdeia.CONVERTIDA_EM_PROJETO,
                    Set.of()
            );

    private final AuditService auditService;
    private final IdeiaRepository ideiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final StatusIdeiaService statusIdeiaService;
    private final OrientacaoEstrategicaService orientacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final IdeaPriorityService ideaPriorityService;
    private final BusinessMetrics businessMetrics;
    private final MilestoneBadgeService milestoneBadgeService;

    @Transactional
    public MostrarIdeiaDTO criarIdeia(RegistrarIdeiaDTO ideiaDTO) {
        if (ideiaRepository.existsByTitulo(ideiaDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma ideia cadastrada com esse título.");
        }

        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        Categoria categoria = categoriaService.buscarEntidadePorId(ideiaDTO.categoriaId());

        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(ideiaDTO.orientacaoId());

        StatusIdeia statusInicial =
                statusIdeiaService.buscarEntidadePorStatus(NomeStatusIdeia.NOVA);

        Ideia ideia = new Ideia();

        ideia.setUsuario(usuario);
        ideia.setCategoria(categoria);
        ideia.setOrientacaoEstrategica(orientacao);
        ideia.setStatusIdeia(statusInicial);

        ideia.setTitulo(ideiaDTO.titulo());
        ideia.setDescricao(ideiaDTO.descricao());
        ideia.setProblema(ideiaDTO.problema());
        ideia.setSolucaoProposta(ideiaDTO.solucaoProposta());

        ideia.setImpacto(0);
        ideia.setViabilidade(0);
        ideia.setPrioridade(new BigDecimal("0.00"));
        ideia.setTotalVotos(0);

        ideia.setDataCriacao(LocalDateTime.now());
        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaSalva = ideiaRepository.save(ideia);
        milestoneBadgeService.awardForIdea(ideiaSalva);

        auditService.registrarEvento(
                AuditEventType.IDEA_CREATED,
                usuario.getId(),
                AuditResourceType.IDEA,
                ideiaSalva.getId().toString(),
                Map.of(
                        "status", ideiaSalva.getStatusIdeia().getStatus().name(),
                        "categoryId", categoria.getId(),
                        "orientationId", orientacao.getId()
                )
        );

        return new MostrarIdeiaDTO(ideiaSalva);
    }

    public List<MostrarIdeiaDTO> listarIdeias() {
        return ideiaRepository.findAll()
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public MostrarIdeiaDTO buscarIdeiaPorId(Long id) {
        return new MostrarIdeiaDTO(buscarEntidadePorId(id));
    }

    public MostrarIdeiaDTO atualizarIdeia(Long id, RegistrarIdeiaDTO ideiaDTO) {
        Ideia ideia = buscarEntidadePorId(id);
        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        validarProprietario(ideia, usuario);

        if (!ideia.getTitulo().equals(ideiaDTO.titulo())
                && ideiaRepository.existsByTitulo(ideiaDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma ideia cadastrada com esse título.");
        }

        Categoria categoria = categoriaService.buscarEntidadePorId(ideiaDTO.categoriaId());

        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(ideiaDTO.orientacaoId());

        ideia.setCategoria(categoria);
        ideia.setOrientacaoEstrategica(orientacao);

        ideia.setTitulo(ideiaDTO.titulo());
        ideia.setDescricao(ideiaDTO.descricao());
        ideia.setProblema(ideiaDTO.problema());
        ideia.setSolucaoProposta(ideiaDTO.solucaoProposta());

        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaAtualizada = ideiaRepository.save(ideia);

        auditService.registrarEvento(
                AuditEventType.IDEA_UPDATED,
                usuario.getId(),
                AuditResourceType.IDEA,
                ideiaAtualizada.getId().toString(),
                Map.of(
                        "categoryId", categoria.getId(),
                        "orientationId", orientacao.getId()
                )
        );

        return new MostrarIdeiaDTO(ideiaAtualizada);
    }

    public void deletarIdeia(Long id) {
        Ideia ideia = buscarEntidadePorId(id);
        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        validarProprietario(ideia, usuario);

        NomeStatusIdeia previousStatus = ideia.getStatusIdeia().getStatus();

        ideiaRepository.delete(ideia);

        auditService.registrarEvento(
                AuditEventType.IDEA_DELETED,
                usuario.getId(),
                AuditResourceType.IDEA,
                ideia.getId().toString(),
                Map.of("previousStatus", previousStatus.name())
        );
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return ideiaRepository.findByUsuario(usuario)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaService.buscarEntidadePorId(categoriaId);

        return ideiaRepository.findByCategoria(categoria)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorStatus(NomeStatusIdeia nomeStatusIdeia) {
        StatusIdeia statusIdeia = statusIdeiaService.buscarEntidadePorStatus(nomeStatusIdeia);

        return ideiaRepository.findByStatusIdeia(statusIdeia)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorOrientacao(Long orientacaoId) {
        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(orientacaoId);

        return ideiaRepository.findByOrientacaoEstrategica(orientacao)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> buscarIdeiasPorTitulo(String titulo) {
        return ideiaRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public Ideia buscarEntidadePorId(Long id) {
        return ideiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ideia não encontrada."));
    }

    @Transactional
    public MostrarIdeiaDTO atualizarStatusIdeia(
            Long id,
            AtualizarStatusIdeiaDTO statusDTO
    ) {
        if (statusDTO == null || statusDTO.status() == null) {
            throw new InvalidRequestException("O novo status da ideia é obrigatório.");
        }

        Ideia ideia = buscarEntidadePorId(id);
        NomeStatusIdeia previousStatus = ideia.getStatusIdeia().getStatus();
        NomeStatusIdeia newStatus = statusDTO.status();

        validarTransicaoSolicitadaPorGestor(previousStatus, newStatus);

        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();
        StatusIdeia novoStatus =
                statusIdeiaService.buscarEntidadePorStatus(newStatus);

        ideia.setStatusIdeia(novoStatus);
        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaAtualizada = ideiaRepository.save(ideia);

        milestoneBadgeService.awardForIdea(ideiaAtualizada);

        auditService.registrarEvento(
                eventTypeForIdeaStatus(newStatus),
                actor.getId(),
                AuditResourceType.IDEA,
                ideiaAtualizada.getId().toString(),
                Map.of(
                        "previousStatus", previousStatus.name(),
                        "newStatus", newStatus.name()
                )
        );

        if (newStatus == NomeStatusIdeia.APROVADA) {
            businessMetrics.recordIdeaApproved();
        }

        return new MostrarIdeiaDTO(ideiaAtualizada);
    }

    void validarConversaoEmProjeto(Ideia ideia) {
        if (ideia == null
                || ideia.getStatusIdeia() == null
                || ideia.getStatusIdeia().getStatus() == null) {
            throw new InvalidRequestException(
                    "A ideia deve possuir um status válido para ser convertida."
            );
        }

        validarTransicao(
                ideia.getStatusIdeia().getStatus(),
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO
        );
    }

    void converterEmProjeto(Ideia ideia) {
        validarConversaoEmProjeto(ideia);

        StatusIdeia convertedStatus = statusIdeiaService.buscarEntidadePorStatus(
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO
        );

        ideia.setStatusIdeia(convertedStatus);
        ideia.setDataAtualizacao(LocalDateTime.now());
        ideiaRepository.save(ideia);
        milestoneBadgeService.awardForIdea(ideia);
    }

    public MostrarIdeiaDTO atualizarAvaliacaoIdeia(
            Long id,
            AtualizarAvaliacaoIdeiaDTO avaliacaoDTO
    ) {
        validarAvaliacao(avaliacaoDTO);

        Ideia ideia = buscarEntidadePorId(id);
        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();

        int previousImpact = Objects.requireNonNullElse(ideia.getImpacto(), 0);
        int previousViability = Objects.requireNonNullElse(ideia.getViabilidade(), 0);

        ideia.setImpacto(avaliacaoDTO.impacto());
        ideia.setViabilidade(avaliacaoDTO.viabilidade());

        ideaPriorityService.recalculateWithLatestEvaluation(ideia);

        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaAtualizada = ideiaRepository.save(ideia);

        auditService.registrarEvento(
                AuditEventType.IDEA_UPDATED,
                actor.getId(),
                AuditResourceType.IDEA,
                ideiaAtualizada.getId().toString(),
                Map.of(
                        "previousImpact", previousImpact,
                        "newImpact", avaliacaoDTO.impacto(),
                        "previousViability", previousViability,
                        "newViability", avaliacaoDTO.viabilidade()
                )
        );

        return new MostrarIdeiaDTO(ideiaAtualizada);
    }

    private void validarProprietario(Ideia ideia, Usuario usuarioAutenticado) {
        Long proprietarioId = ideia.getUsuario().getId();
        Long usuarioAutenticadoId = usuarioAutenticado.getId();

        if (!Objects.equals(proprietarioId, usuarioAutenticadoId)) {
            throw new UnauthorizedActionException(
                    "Você só pode editar ou excluir suas próprias ideias."
            );
        }
    }

    private void validarTransicaoSolicitadaPorGestor(
            NomeStatusIdeia currentStatus,
            NomeStatusIdeia newStatus
    ) {
        if (newStatus == NomeStatusIdeia.CONVERTIDA_EM_PROJETO) {
            throw new InvalidRequestException(
                    "A conversão da ideia deve ocorrer durante a criação do projeto."
            );
        }

        validarTransicao(currentStatus, newStatus);
    }

    private void validarTransicao(
            NomeStatusIdeia currentStatus,
            NomeStatusIdeia newStatus
    ) {

        Set<NomeStatusIdeia> allowedTargets =
                ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedTargets.contains(newStatus)) {
            throw new InvalidRequestException(
                    "Transição de status não permitida: %s -> %s."
                            .formatted(currentStatus, newStatus)
            );
        }
    }

    private AuditEventType eventTypeForIdeaStatus(NomeStatusIdeia status) {
        return switch (status) {
            case PRIORIZADA -> AuditEventType.IDEA_PRIORITIZED;
            case APROVADA -> AuditEventType.IDEA_APPROVED;
            case REJEITADA -> AuditEventType.IDEA_REJECTED;
            default -> AuditEventType.IDEA_UPDATED;
        };
    }

    private void validarAvaliacao(AtualizarAvaliacaoIdeiaDTO avaliacaoDTO) {
        if (avaliacaoDTO == null
                || avaliacaoDTO.impacto() == null
                || avaliacaoDTO.viabilidade() == null
                || avaliacaoDTO.impacto() < 0
                || avaliacaoDTO.impacto() > 10
                || avaliacaoDTO.viabilidade() < 0
                || avaliacaoDTO.viabilidade() > 10) {
            throw new InvalidRequestException(
                    "Impacto e viabilidade devem estar entre 0 e 10."
            );
        }
    }

    BigDecimal atualizarPrioridadeComPontuacaoAi(
            Ideia ideia,
            BigDecimal aiScore
    ) {
        BigDecimal priority = ideaPriorityService.recalculate(ideia, aiScore);

        ideia.setDataAtualizacao(LocalDateTime.now());
        Ideia updatedIdea;
        try {
            updatedIdea = ideiaRepository.save(ideia);
        } catch (DataAccessException exception) {
            throw new PriorityCalculationUnavailableException(
                    "Não foi possível persistir a prioridade da ideia.",
                    exception
            );
        }

        return updatedIdea.getPrioridade();
    }

}
