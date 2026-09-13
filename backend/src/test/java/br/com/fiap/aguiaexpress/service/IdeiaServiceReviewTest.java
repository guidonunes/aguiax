package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.ideia.AtualizarAvaliacaoIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IdeiaServiceReviewTest {

    private AuditService auditService;
    private IdeiaRepository ideiaRepository;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private IdeaPriorityService ideaPriorityService;
    private IdeiaService ideiaService;

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        ideiaRepository = mock(IdeiaRepository.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        ideaPriorityService = mock(IdeaPriorityService.class);

        ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                mock(UsuarioRepository.class),
                mock(CategoriaService.class),
                mock(StatusIdeiaService.class),
                mock(OrientacaoEstrategicaService.class),
                usuarioAutenticadoService,
                ideaPriorityService
        );
    }

    @Test
    void shouldPersistBoundaryScoresAndAuditAfterSave() {
        Ideia ideia = idea();
        Usuario actor = user(17L);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(ideaPriorityService.recalculateWithLatestEvaluation(ideia))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("2.90"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        MostrarIdeiaDTO result = ideiaService.atualizarAvaliacaoIdeia(
                42L,
                new AtualizarAvaliacaoIdeiaDTO(10, 0)
        );

        assertEquals(10, result.impacto());
        assertEquals(0, result.viabilidade());
        assertEquals(new BigDecimal("2.90"), result.prioridade());

        InOrder order = inOrder(
                ideaPriorityService,
                ideiaRepository,
                auditService
        );
        order.verify(
                ideaPriorityService
        ).recalculateWithLatestEvaluation(ideia);
        order.verify(ideiaRepository).save(ideia);
        order.verify(auditService).registrarEvento(
                AuditEventType.IDEA_UPDATED,
                17L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "previousImpact", 2,
                        "newImpact", 10,
                        "previousViability", 3,
                        "newViability", 0
                )
        );
    }

    @Test
    void shouldRejectMissingScoresBeforePersistence() {
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(null, 5));
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(5, null));
    }

    @Test
    void shouldRejectScoresBelowRangeBeforePersistence() {
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(-1, 5));
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(5, -1));
    }

    @Test
    void shouldRejectScoresAboveRangeBeforePersistence() {
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(11, 5));
        assertInvalidReview(new AtualizarAvaliacaoIdeiaDTO(5, 11));
    }

    @Test
    void shouldNotAuditWhenRelationalSaveFails() {
        Ideia ideia = idea();
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(ideaPriorityService.recalculateWithLatestEvaluation(ideia))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("6.15"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia))
                .thenThrow(new IllegalStateException("MySQL unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> ideiaService.atualizarAvaliacaoIdeia(
                        42L,
                        new AtualizarAvaliacaoIdeiaDTO(8, 7)
                )
        );

        verifyNoInteractions(auditService);
        verify(ideaPriorityService).recalculateWithLatestEvaluation(ideia);
    }

    @Test
    void shouldPersistPriorityFromSuccessfulAiEvaluationWithoutChangingStatus() {
        Ideia ideia = idea();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        BigDecimal aiScore = new BigDecimal("8.10");
        when(ideaPriorityService.recalculate(ideia, aiScore))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("4.34"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        BigDecimal result = ideiaService.atualizarPrioridadeComPontuacaoAi(
                ideia,
                aiScore
        );

        assertEquals(new BigDecimal("4.34"), result);
        assertSame(originalStatus, ideia.getStatusIdeia());
        InOrder order = inOrder(ideaPriorityService, ideiaRepository);
        order.verify(ideaPriorityService).recalculate(ideia, aiScore);
        order.verify(ideiaRepository).save(ideia);
    }

    @Test
    void shouldTranslateAiPriorityPersistenceFailure() {
        Ideia ideia = idea();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        BigDecimal aiScore = new BigDecimal("8.10");
        when(ideaPriorityService.recalculate(ideia, aiScore))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("4.34"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia)).thenThrow(
                new DataAccessResourceFailureException("mysql unavailable")
        );

        PriorityCalculationUnavailableException exception = assertThrows(
                PriorityCalculationUnavailableException.class,
                () -> ideiaService.atualizarPrioridadeComPontuacaoAi(
                        ideia,
                        aiScore
                )
        );

        assertEquals(
                "Não foi possível persistir a prioridade da ideia.",
                exception.getMessage()
        );
        assertSame(originalStatus, ideia.getStatusIdeia());
    }

    private void assertInvalidReview(AtualizarAvaliacaoIdeiaDTO request) {
        assertThrows(
                InvalidRequestException.class,
                () -> ideiaService.atualizarAvaliacaoIdeia(42L, request)
        );

        verifyNoInteractions(
                ideiaRepository,
                usuarioAutenticadoService,
                ideaPriorityService,
                auditService
        );
    }

    private Ideia idea() {
        Usuario owner = user(7L);
        Categoria categoria = new Categoria(3L, "Tecnologia", "Descrição");
        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();
        orientacao.setId(4L);
        orientacao.setTitulo("Orientação");
        orientacao.setAreaEstrategica("Operações");

        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setUsuario(owner);
        ideia.setCategoria(categoria);
        ideia.setOrientacaoEstrategica(orientacao);
        ideia.setStatusIdeia(new StatusIdeia(2L, NomeStatusIdeia.EM_ANALISE, "Em análise"));
        ideia.setTitulo("Ideia");
        ideia.setDescricao("Descrição");
        ideia.setImpacto(2);
        ideia.setViabilidade(3);
        ideia.setPrioridade(new BigDecimal("7.00"));
        ideia.setTotalVotos(2);
        return ideia;
    }

    private Usuario user(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário");
        return usuario;
    }
}
