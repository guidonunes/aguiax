package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.projeto.AtualizarStatusProjetoDTO;
import br.com.fiap.aguiaexpress.dto.projeto.MostrarProjetoDTO;
import br.com.fiap.aguiaexpress.dto.projeto.RegistrarProjetoDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import br.com.fiap.aguiaexpress.model.*;
import br.com.fiap.aguiaexpress.repository.ProjetoRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final AuditService auditService;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdeiaService ideiaService;
    private final StatusProjetoService statusProjetoService;

    @Transactional
    public MostrarProjetoDTO criarProjeto(RegistrarProjetoDTO projetoDTO) {
        Ideia ideia = ideiaService.buscarEntidadePorId(projetoDTO.ideiaId());

        if (projetoRepository.existsByIdeia(ideia)) {
            throw new DuplicateResourceException("Essa ideia já foi convertida em projeto.");
        }

        ideiaService.validarConversaoEmProjeto(ideia);

        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();
        Usuario usuario = usuarioRepository.findById(projetoDTO.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));

        validarUsuarioGestor(usuario);

        StatusProjeto statusInicial = statusProjetoService
                .buscarEntidadePorStatus(NomeStatusProjeto.PLANEJADO);

        Projeto projeto = new Projeto();

        projeto.setIdeia(ideia);
        projeto.setUsuario(usuario);
        projeto.setStatusProjeto(statusInicial);

        projeto.setTitulo(projetoDTO.titulo());
        projeto.setDescricao(projetoDTO.descricao());
        projeto.setObjetivo(projetoDTO.objetivo());

        projeto.setInvestimento(valorOuZero(projetoDTO.investimento()));
        projeto.setRoi(valorOuZero(projetoDTO.roi()));
        projeto.setRetornoFinanceiro(valorOuZero(projetoDTO.retornoFinanceiro()));
        projeto.setReducaoCustos(valorOuZero(projetoDTO.reducaoCustos()));
        projeto.setGanhoProdutividade(valorOuZero(projetoDTO.ganhoProdutividade()));

        projeto.setDataInicio(projetoDTO.dataInicio());
        projeto.setDataPrevisaoFim(projetoDTO.dataPrevisaoFim());
        projeto.setDataConclusao(projetoDTO.dataConclusao());

        projeto.setDataCriacao(LocalDateTime.now());
        projeto.setDataAtualizacao(LocalDateTime.now());

        Projeto projetoSalvo = projetoRepository.save(projeto);

        ideiaService.converterEmProjeto(ideia);
        projetoRepository.flush();

        auditService.registrarEvento(
                AuditEventType.PROJECT_CREATED,
                actor.getId(),
                AuditResourceType.PROJECT,
                projetoSalvo.getId().toString(),
                Map.of(
                        "ideaId", ideia.getId(),
                        "responsibleUserId", usuario.getId(),
                        "status", statusInicial.getStatus().name(),
                        "previousIdeaStatus", NomeStatusIdeia.APROVADA.name(),
                        "newIdeaStatus", NomeStatusIdeia.CONVERTIDA_EM_PROJETO.name()
                )
        );

        return new MostrarProjetoDTO(projetoSalvo);
    }

    public List<MostrarProjetoDTO> listarProjetos() {
        return projetoRepository.findAll()
                .stream()
                .map(MostrarProjetoDTO::new)
                .toList();
    }

    public MostrarProjetoDTO buscarProjetoPorId(Long id) {
        return new MostrarProjetoDTO(buscarEntidadePorId(id));
    }

    public MostrarProjetoDTO atualizarProjeto(Long id, RegistrarProjetoDTO projetoDTO) {
        Projeto projeto = buscarEntidadePorId(id);
        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();
        Long previousIdeaId = projeto.getIdeia().getId();
        Long previousResponsibleUserId = projeto.getUsuario().getId();

        Ideia ideia = ideiaService.buscarEntidadePorId(projetoDTO.ideiaId());

        if (!projeto.getIdeia().getId().equals(ideia.getId())
                && projetoRepository.existsByIdeia(ideia)) {
            throw new DuplicateResourceException("Essa ideia já está vinculada a outro projeto.");
        }

        Usuario usuario = usuarioRepository.findById(projetoDTO.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));

        validarUsuarioGestor(usuario);

        projeto.setIdeia(ideia);
        projeto.setUsuario(usuario);

        projeto.setTitulo(projetoDTO.titulo());
        projeto.setDescricao(projetoDTO.descricao());
        projeto.setObjetivo(projetoDTO.objetivo());

        projeto.setInvestimento(valorOuZero(projetoDTO.investimento()));
        projeto.setRoi(valorOuZero(projetoDTO.roi()));
        projeto.setRetornoFinanceiro(valorOuZero(projetoDTO.retornoFinanceiro()));
        projeto.setReducaoCustos(valorOuZero(projetoDTO.reducaoCustos()));
        projeto.setGanhoProdutividade(valorOuZero(projetoDTO.ganhoProdutividade()));

        projeto.setDataInicio(projetoDTO.dataInicio());
        projeto.setDataPrevisaoFim(projetoDTO.dataPrevisaoFim());
        projeto.setDataConclusao(projetoDTO.dataConclusao());

        projeto.setDataAtualizacao(LocalDateTime.now());

        Projeto projetoAtualizado = projetoRepository.save(projeto);

        auditService.registrarEvento(
                AuditEventType.PROJECT_UPDATED,
                actor.getId(),
                AuditResourceType.PROJECT,
                projetoAtualizado.getId().toString(),
                Map.of(
                        "previousIdeaId", previousIdeaId,
                        "newIdeaId", ideia.getId(),
                        "previousResponsibleUserId", previousResponsibleUserId,
                        "newResponsibleUserId", usuario.getId()
                )
        );

        return new MostrarProjetoDTO(projetoAtualizado);
    }

    public void deletarProjeto(Long id) {
        Projeto projeto = buscarEntidadePorId(id);

        projetoRepository.delete(projeto);
    }

    public List<MostrarProjetoDTO> listarProjetosPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));

        return projetoRepository.findByUsuario(usuario)
                .stream()
                .map(MostrarProjetoDTO::new)
                .toList();
    }

    public List<MostrarProjetoDTO> listarProjetosPorStatus(NomeStatusProjeto nomeStatusProjeto) {
        StatusProjeto statusProjeto = statusProjetoService.buscarEntidadePorStatus(nomeStatusProjeto);

        return projetoRepository.findByStatusProjeto(statusProjeto)
                .stream()
                .map(MostrarProjetoDTO::new)
                .toList();
    }

    public List<MostrarProjetoDTO> buscarProjetosPorTitulo(String titulo) {
        return projetoRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(MostrarProjetoDTO::new)
                .toList();
    }

    public MostrarProjetoDTO buscarProjetoPorIdeia(Long ideiaId) {
        Ideia ideia = ideiaService.buscarEntidadePorId(ideiaId);

        Projeto projeto = projetoRepository.findByIdeia(ideia)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado para essa ideia."));

        return new MostrarProjetoDTO(projeto);
    }

    public Projeto buscarEntidadePorId(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
    }

    private void validarUsuarioGestor(Usuario usuario) {
        if (!usuario.getTipoPerfil().getNomePerfil().equals(NomePerfil.GESTOR)) {
            throw new UnauthorizedActionException("Apenas gestores podem cadastrar ou atualizar projetos.");
        }
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    public MostrarProjetoDTO atualizarStatusProjeto(
            Long id,
            AtualizarStatusProjetoDTO statusDTO
    ) {
        Projeto projeto = buscarEntidadePorId(id);
        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();

        NomeStatusProjeto previousStatus =
                projeto.getStatusProjeto().getStatus();

        StatusProjeto novoStatus =
                statusProjetoService.buscarEntidadePorStatus(statusDTO.status());

        projeto.setStatusProjeto(novoStatus);

        if (statusDTO.status() == NomeStatusProjeto.CONCLUIDO
                && projeto.getDataConclusao() == null) {
            projeto.setDataConclusao(LocalDate.now());
        }

        projeto.setDataAtualizacao(LocalDateTime.now());

        Projeto projetoAtualizado = projetoRepository.save(projeto);

        AuditEventType eventType =
                statusDTO.status() == NomeStatusProjeto.CONCLUIDO
                        ? AuditEventType.PROJECT_COMPLETED
                        : AuditEventType.PROJECT_UPDATED;

        auditService.registrarEvento(
                eventType,
                actor.getId(),
                AuditResourceType.PROJECT,
                projetoAtualizado.getId().toString(),
                Map.of(
                        "previousStatus", previousStatus.name(),
                        "newStatus", statusDTO.status().name()
                )
        );

        return new MostrarProjetoDTO(projetoAtualizado);
    }
}
