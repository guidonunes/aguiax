package br.com.fiap.aguiaexpress.config.openapi;

import br.com.fiap.aguiaexpress.ai.repository.AiIdeaEvaluationRepository;
import br.com.fiap.aguiaexpress.ai.repository.DashboardAiInsightRepository;
import br.com.fiap.aguiaexpress.audit.repository.AuditEventRepository;
import br.com.fiap.aguiaexpress.repository.BadgeRepository;
import br.com.fiap.aguiaexpress.repository.CategoriaRepository;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaexpress.repository.ProjetoRepository;
import br.com.fiap.aguiaexpress.repository.StatusIdeiaRepository;
import br.com.fiap.aguiaexpress.repository.StatusProjetoRepository;
import br.com.fiap.aguiaexpress.repository.TipoPerfilRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioBadgeRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import br.com.fiap.aguiaexpress.repository.VotoIdeiaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
@ActiveProfiles("local")
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void localProfilePublishesDocumentedSprintTwoContractWithoutAuthentication()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AguiaExpress API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type")
                        .value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat")
                        .value("JWT"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$['paths']['/auth/login']['post']['security']")
                        .isEmpty())
                .andExpect(jsonPath("$['paths']['/usuarios']['post']['security']")
                        .isEmpty())
                .andExpect(jsonPath("$['paths']['/ideias/{id}/avaliacao']['patch']['responses']['503']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/ideas/{ideaId}/ai-evaluation']['post']['responses']['502']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/ideas/{ideaId}/ai-evaluation']['post']['responses']['504']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/projetos']['post']['responses']['409']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/summary']['get']['responses']['403']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/insights']['post']['responses']['400']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/insights']['post']['responses']['403']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/insights']['post']['responses']['502']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/insights']['post']['responses']['503']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/dashboard/insights']['post']['responses']['504']")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.AtualizarAvaliacaoIdeiaDTO.properties.impacto.minimum")
                        .value(0))
                .andExpect(jsonPath("$.components.schemas.AtualizarAvaliacaoIdeiaDTO.properties.impacto.maximum")
                        .value(10))
                .andExpect(jsonPath("$.components.schemas.RegistrarProjetoDTO.properties.investimento.minimum")
                        .value(0));
    }

    @Test
    void localProfilePublishesSwaggerUiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
