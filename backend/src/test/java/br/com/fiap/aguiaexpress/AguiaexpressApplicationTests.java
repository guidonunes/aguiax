package br.com.fiap.aguiaexpress;

import br.com.fiap.aguiaexpress.ai.repository.AiIdeaEvaluationRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "api.security.token.secret=test-secret-with-sufficient-length",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
                + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration"
})
class AguiaexpressApplicationTests {

    @MockitoBean
    private AiIdeaEvaluationRepository aiIdeaEvaluationRepository;

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

}
