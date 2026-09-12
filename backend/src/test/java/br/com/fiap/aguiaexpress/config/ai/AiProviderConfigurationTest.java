package br.com.fiap.aguiaexpress.config.ai;

import br.com.fiap.aguiaexpress.ai.client.AiIdeaEvaluationClient;
import br.com.fiap.aguiaexpress.ai.client.GeminiAiIdeaEvaluationClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiProviderConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withInitializer(
                            new ConfigDataApplicationContextInitializer()
                    )
                    .withBean(ObjectMapper.class, ObjectMapper::new)
                    .withUserConfiguration(AiClientConfiguration.class);

    @Test
    void shouldStartWithoutApiKeyWhenAiIsDisabled() {
        contextRunner
                .withPropertyValues("AI_ENABLED=false")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(aiClients(context).isEmpty());

                    AiProviderProperties properties =
                            context.getBean(AiProviderProperties.class);

                    assertAllDefaultValues(properties);
                });
    }

    @Test
    void shouldIgnoreUnusedProviderSettingsWhenAiIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=false",
                        "AI_PROVIDER=unsupported",
                        "AI_API_KEY=",
                        "AI_BASE_URL=http://insecure.example.test",
                        "AI_CONNECT_TIMEOUT_MS=0",
                        "AI_READ_TIMEOUT_MS=0"
                )
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(aiClients(context).isEmpty());
                });
    }

    @Test
    void shouldCreateGeminiClientAndBindEnvironmentOverridesWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=true",
                        "AI_PROVIDER=gemini",
                        "AI_API_KEY=test-api-key",
                        "AI_MODEL=gemini-3.6-flash",
                        "AI_BASE_URL="
                                + "https://generativelanguage.googleapis.com/v1beta",
                        "AI_CONNECT_TIMEOUT_MS=2500",
                        "AI_READ_TIMEOUT_MS=12000"
                )
                .run(context -> {
                    assertNull(context.getStartupFailure());

                    Map<String, AiIdeaEvaluationClient> clients =
                            aiClients(context);

                    assertEquals(1, clients.size());
                    assertInstanceOf(
                            GeminiAiIdeaEvaluationClient.class,
                            clients.values().iterator().next()
                    );

                    AiProviderProperties properties =
                            context.getBean(AiProviderProperties.class);

                    assertTrue(properties.enabled());
                    assertEquals("gemini", properties.provider());
                    assertEquals("test-api-key", properties.apiKey());
                    assertEquals(
                            "gemini-3.6-flash",
                            properties.model()
                    );
                    assertEquals(
                            Duration.ofMillis(2500),
                            properties.connectTimeout()
                    );
                    assertEquals(
                            Duration.ofMillis(12000),
                            properties.readTimeout()
                    );
                });
    }

    @Test
    void shouldFailSafelyWhenEnabledWithoutApiKey() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=true",
                        "AI_API_KEY="
                )
                .run(context -> {
                    Throwable rootCause = rootCause(
                            context.getStartupFailure()
                    );

                    assertNotNull(rootCause);
                    assertInstanceOf(
                            IllegalStateException.class,
                            rootCause
                    );
                    assertEquals(
                            "AI_API_KEY é obrigatória quando AI_ENABLED=true",
                            rootCause.getMessage()
                    );
                });
    }

    @Test
    void shouldRejectUnsupportedProviderWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=true",
                        "AI_PROVIDER=unsupported",
                        "AI_API_KEY=test-api-key"
                )
                .run(context -> {
                    Throwable rootCause = rootCause(
                            context.getStartupFailure()
                    );

                    assertNotNull(rootCause);
                    assertInstanceOf(
                            IllegalStateException.class,
                            rootCause
                    );
                    assertEquals(
                            "AI_PROVIDER não suportado: unsupported",
                            rootCause.getMessage()
                    );
                });
    }

    @Test
    void shouldRejectInvalidTimeoutWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=true",
                        "AI_API_KEY=test-api-key",
                        "AI_CONNECT_TIMEOUT_MS=0"
                )
                .run(context -> {
                    Throwable rootCause = rootCause(
                            context.getStartupFailure()
                    );

                    assertNotNull(rootCause);
                    assertInstanceOf(
                            IllegalArgumentException.class,
                            rootCause
                    );
                    assertEquals(
                            "O timeout de conexão deve ser positivo",
                            rootCause.getMessage()
                    );
                });
    }

    @Test
    void shouldRejectInsecureProviderUrlWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "AI_ENABLED=true",
                        "AI_API_KEY=test-api-key",
                        "AI_BASE_URL=http://insecure.example.test"
                )
                .run(context -> {
                    Throwable rootCause = rootCause(
                            context.getStartupFailure()
                    );

                    assertNotNull(rootCause);
                    assertInstanceOf(
                            IllegalArgumentException.class,
                            rootCause
                    );
                    assertEquals(
                            "A URL base do provedor de IA deve usar HTTPS",
                            rootCause.getMessage()
                    );
                });
    }

    private Map<String, AiIdeaEvaluationClient> aiClients(
            org.springframework.context.ApplicationContext context
    ) {
        return context.getBeansOfType(AiIdeaEvaluationClient.class);
    }

    private void assertAllDefaultValues(
            AiProviderProperties properties
    ) {
        assertFalse(properties.enabled());
        assertEquals("gemini", properties.provider());
        assertNull(properties.apiKey());
        assertEquals("gemini-3.6-flash", properties.model());
        assertEquals(
                "https://generativelanguage.googleapis.com/v1beta",
                properties.baseUrl()
        );
        assertEquals(
                Duration.ofSeconds(3),
                properties.connectTimeout()
        );
        assertEquals(
                Duration.ofSeconds(15),
                properties.readTimeout()
        );
    }

    private Throwable rootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }
}
