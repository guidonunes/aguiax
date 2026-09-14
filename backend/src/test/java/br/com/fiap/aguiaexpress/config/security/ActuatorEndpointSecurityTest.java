package br.com.fiap.aguiaexpress.config.security;

import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = ActuatorEndpointSecurityTest.TestApplication.class,
        properties = "api.security.token.secret=jwt-test-secret-with-sufficient-length"
)
@AutoConfigureMockMvc
class ActuatorEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void healthIsPublicAndDoesNotExposeComponents() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void infoAndMetricsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonLeaderCannotReadOperationalInformation() throws Exception {
        String managerToken = tokenFor(NomePerfil.GESTOR, 17L);

        mockMvc.perform(get("/actuator/info")
                        .header(HttpHeaders.AUTHORIZATION, bearer(managerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/metrics")
                        .header(HttpHeaders.AUTHORIZATION, bearer(managerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void leaderCanReadSafeInfoAndBuiltInMetrics() throws Exception {
        String leaderToken = tokenFor(NomePerfil.LIDER, 18L);

        mockMvc.perform(get("/actuator/info")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("aguiaexpress"))
                .andExpect(jsonPath("$.app.description")
                        .value("AguiaExpress innovation management API"))
                .andExpect(jsonPath("$.app.version").value("0.0.1-SNAPSHOT"));

        mockMvc.perform(get("/actuator/metrics")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());

        mockMvc.perform(get("/actuator/metrics/jvm.memory.used")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jvm.memory.used"));
    }

    @Test
    void healthDetailsAndUnlistedActuatorEndpointsRemainUnavailable() throws Exception {
        String leaderToken = tokenFor(NomePerfil.LIDER, 19L);

        mockMvc.perform(get("/actuator/health/ping")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/env")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/actuator")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken)))
                .andExpect(status().isNotFound());
    }

    private String tokenFor(NomePerfil profile, Long userId) {
        Usuario user = new Usuario();
        user.setId(userId);
        user.setNome("Usuário de teste");
        user.setEmail(profile.name().toLowerCase() + "@example.com");
        user.setStatus(1);
        user.setTipoPerfil(new TipoPerfil(userId, profile, profile.name()));

        when(usuarioRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        return tokenService.gerarToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class,
            MongoAutoConfiguration.class,
            DataMongoAutoConfiguration.class
    })
    @Import({SecurityConfig.class, TokenVerification.class, TokenService.class})
    static class TestApplication {
    }
}
