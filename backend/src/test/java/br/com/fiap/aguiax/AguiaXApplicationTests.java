package br.com.fiap.aguiax;

import br.com.fiap.aguiax.ai.repository.AiIdeaEvaluationRepository;
import br.com.fiap.aguiax.ai.repository.DashboardAiInsightRepository;
import br.com.fiap.aguiax.audit.repository.AuditEventRepository;
import br.com.fiap.aguiax.repository.BadgeRepository;
import br.com.fiap.aguiax.repository.CategoriaRepository;
import br.com.fiap.aguiax.repository.IdeiaRepository;
import br.com.fiap.aguiax.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiax.repository.ProjetoRepository;
import br.com.fiap.aguiax.repository.StatusIdeiaRepository;
import br.com.fiap.aguiax.repository.StatusProjetoRepository;
import br.com.fiap.aguiax.repository.TipoPerfilRepository;
import br.com.fiap.aguiax.repository.UsuarioBadgeRepository;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import br.com.fiap.aguiax.repository.VotoIdeiaRepository;
import br.com.fiap.aguiax.service.DashboardService;
import br.com.fiap.aguiax.service.IdeiaService;
import br.com.fiap.aguiax.service.UsuarioBadgeService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "api.security.token.secret=test-secret-with-sufficient-length",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
                + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration"
})
@AutoConfigureMockMvc
class AguiaXApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private IdeiaService ideiaService;

    @MockitoBean
    private UsuarioBadgeService usuarioBadgeService;

    @BeforeEach
    void simulateBuildWithoutJavaParameterNames() {
        // IDE builds may omit -parameters; URL binding must still work.
        handlerAdapter.setParameterNameDiscoverer(null);
    }

    @AfterEach
    void restoreParameterNameDiscovery() {
        handlerAdapter.setParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
    }

    @MockitoBean
    private AiIdeaEvaluationRepository aiIdeaEvaluationRepository;

    @MockitoBean
    private DashboardAiInsightRepository dashboardAiInsightRepository;

    @MockitoBean
    private AuditEventRepository auditEventRepository;

    @MockitoBean
    private BadgeRepository badgeRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private IdeiaRepository ideiaRepository;

    @MockitoBean
    private OrientacaoEstrategicaRepository orientacaoEstrategicaRepository;

    @MockitoBean
    private ProjetoRepository projetoRepository;

    @MockitoBean
    private StatusIdeiaRepository statusIdeiaRepository;

    @MockitoBean
    private StatusProjetoRepository statusProjetoRepository;

    @MockitoBean
    private TipoPerfilRepository tipoPerfilRepository;

    @MockitoBean
    private UsuarioBadgeRepository usuarioBadgeRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private VotoIdeiaRepository votoIdeiaRepository;

    @Test
    void contextLoadsWithoutExternalDatabasesOrDotenv() {
        // Spring successfully creates the application graph with persistence mocked.
    }

    @Test
    void openApiEndpointsAreDisabledOutsideLocalProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .with(user("leader").roles("LIDER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void operationalHomeLoadsPersonalIdeasWithoutCompilerParameterNames() throws Exception {
        when(ideiaService.listarIdeiasPorUsuario(11L)).thenReturn(List.of());

        mockMvc.perform(get("/ideias/usuario/11")
                        .with(user("ana.martins@aguiax.com").roles("OPERACIONAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(ideiaService).listarIdeiasPorUsuario(11L);
    }

    @Test
    void operationalHomeLoadsPersonalBadgesWithoutCompilerParameterNames() throws Exception {
        when(usuarioBadgeService.listarBadgesPorUsuario(11L)).thenReturn(List.of());

        mockMvc.perform(get("/usuario-badges/usuario/11")
                        .with(user("ana.martins@aguiax.com").roles("OPERACIONAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(usuarioBadgeService).listarBadgesPorUsuario(11L);
    }

    @Test
    void internalErrorDispatchPreservesStatusInsteadOfClaimingSessionExpired() throws Exception {
        for (int originalStatus : new int[]{403, 404, 500}) {
            mockMvc.perform(get("/error")
                            .with(request -> {
                                request.setDispatcherType(DispatcherType.ERROR);
                                return request;
                            })
                            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, originalStatus)
                            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/ideias/usuario/11"))
                    .andExpect(status().is(originalStatus))
                    .andExpect(jsonPath("$.status").value(originalStatus));
        }
    }

    @Test
    void ordinaryRequestsStillRequireAuthentication() throws Exception {
        for (String path : new String[]{"/error", "/ideias/usuario/11", "/usuario-badges/usuario/11"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void leaderCanLoadGlobalAndFilteredDashboardWithoutCompilerParameterNames() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user("leader").roles("LIDER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/summary").param("orientationId", "7")
                        .with(user("leader").roles("LIDER")))
                .andExpect(status().isOk());

        verify(dashboardService).buscarResumo(null);
        verify(dashboardService).buscarResumo(7L);
    }

    @Test
    void operationalUserStillCannotAccessExecutiveDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user("ana.martins@aguiax.com").roles("OPERACIONAL")))
                .andExpect(status().isForbidden());
    }

}
