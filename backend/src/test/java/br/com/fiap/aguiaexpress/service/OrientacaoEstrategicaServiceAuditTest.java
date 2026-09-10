package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.RegistrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrientacaoEstrategicaServiceAuditTest {

    private AuditService auditService;
    private OrientacaoEstrategicaRepository orientacaoRepository;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private OrientacaoEstrategicaService orientacaoService;

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        orientacaoRepository = mock(OrientacaoEstrategicaRepository.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);

        orientacaoService = new OrientacaoEstrategicaService(
                auditService,
                orientacaoRepository,
                mock(UsuarioRepository.class),
                usuarioAutenticadoService
        );
    }

    @Test
    void shouldAuditOrientationCreationAfterRelationalSave() {
        Usuario actor = leader(17L);
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(orientacaoRepository.save(any(OrientacaoEstrategica.class)))
                .thenAnswer(invocation -> {
                    OrientacaoEstrategica orientacao = invocation.getArgument(0);
                    orientacao.setId(12L);
                    return orientacao;
                });

        orientacaoService.criarOrientacao(request("Nova orientação", "Operações"));

        InOrder order = inOrder(orientacaoRepository, auditService);
        order.verify(orientacaoRepository).save(any(OrientacaoEstrategica.class));
        order.verify(auditService).registrarEvento(
                AuditEventType.ORIENTATION_CREATED,
                17L,
                AuditResourceType.ORIENTATION,
                "12",
                Map.of("status", 1)
        );
    }

    @Test
    void shouldAuditOrientationUpdateWithoutPersistingSensitiveValues() {
        OrientacaoEstrategica orientacao = orientation(12L, "Título anterior", "Operações", 1);
        when(orientacaoRepository.findById(12L)).thenReturn(Optional.of(orientacao));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(leader(17L));
        when(orientacaoRepository.save(orientacao)).thenReturn(orientacao);

        orientacaoService.atualizarOrientacao(
                12L,
                request("Novo título", "Sustentabilidade")
        );

        verify(auditService).registrarEvento(
                AuditEventType.ORIENTATION_UPDATED,
                17L,
                AuditResourceType.ORIENTATION,
                "12",
                Map.of(
                        "titleChanged", true,
                        "strategicAreaChanged", true
                )
        );
    }

    @Test
    void shouldAuditOrientationDeactivationAsUpdate() {
        OrientacaoEstrategica orientacao = orientation(12L, "Orientação", "Operações", 1);
        when(orientacaoRepository.findById(12L)).thenReturn(Optional.of(orientacao));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(leader(17L));
        when(orientacaoRepository.save(orientacao)).thenReturn(orientacao);

        orientacaoService.deletarOrientacao(12L);

        verify(auditService).registrarEvento(
                AuditEventType.ORIENTATION_UPDATED,
                17L,
                AuditResourceType.ORIENTATION,
                "12",
                Map.of(
                        "previousStatus", "1",
                        "newStatus", "0"
                )
        );
    }

    @Test
    void shouldNotAuditWhenOrientationSaveFails() {
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(leader(17L));
        when(orientacaoRepository.save(any(OrientacaoEstrategica.class)))
                .thenThrow(new IllegalStateException("MySQL unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> orientacaoService.criarOrientacao(
                        request("Nova orientação", "Operações")
                )
        );

        verifyNoInteractions(auditService);
    }

    private RegistrarOrientacaoDTO request(String title, String strategicArea) {
        return new RegistrarOrientacaoDTO(title, "Descrição", strategicArea);
    }

    private OrientacaoEstrategica orientation(
            Long id,
            String title,
            String strategicArea,
            Integer status
    ) {
        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();
        orientacao.setId(id);
        orientacao.setUsuario(leader(7L));
        orientacao.setTitulo(title);
        orientacao.setDescricao("Descrição");
        orientacao.setAreaEstrategica(strategicArea);
        orientacao.setStatus(status);
        return orientacao;
    }

    private Usuario leader(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Líder " + id);
        usuario.setTipoPerfil(new TipoPerfil(3L, NomePerfil.LIDER, "Líder"));
        return usuario;
    }
}
