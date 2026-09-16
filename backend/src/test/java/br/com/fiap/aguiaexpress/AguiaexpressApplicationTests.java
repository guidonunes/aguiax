package br.com.fiap.aguiaexpress;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AguiaexpressApplicationTests {

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
    void contextLoadsWithoutExternalDatabasesOrDotenv() {
        // Spring successfully creates the application graph with persistence mocked.
    }

    @Test
    void openApiEndpointsAreDisabledOutsideLocalProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .with(user("leader").roles("LIDER")))
                .andExpect(status().isNotFound());
    }

}
