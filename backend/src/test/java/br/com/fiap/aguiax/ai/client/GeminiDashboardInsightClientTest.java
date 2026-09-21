package br.com.fiap.aguiax.ai.client;

import br.com.fiap.aguiax.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiax.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

class GeminiDashboardInsightClientTest {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta";
    private static final String MODEL = "gemini-3.6-flash";
    private static final String API_KEY = "test-api-key";
    private static final String ENDPOINT =
            BASE_URL + "/models/" + MODEL + ":generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private GeminiDashboardInsightClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("x-goog-api-key", API_KEY);

        server = MockRestServiceServer.bindTo(builder).build();
        client = new GeminiDashboardInsightClient(
                builder.build(),
                objectMapper,
                MODEL
        );
    }

    @AfterEach
    void verifyProviderCall() {
        server.verify();
    }

    @Test
    void shouldSendTrustedContextAndMapValidStructuredResponse()
            throws Exception {
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.generationConfig.temperature").doesNotExist())
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(
                        jsonPath("$.contents[0].parts[0].text")
                                .value(containsString("ORIENTATION"))
                )
                .andExpect(
                        jsonPath("$.contents[0].parts[0].text")
                                .value(containsString("não invente causas"))
                )
                .andExpect(
                        jsonPath("$.generationConfig.responseMimeType")
                                .value("application/json")
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.language.enum[0]"
                        ).value("pt-BR")
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.executiveSummary.description"
                        ).value(containsString("800 caracteres"))
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.highlights.minItems"
                        ).value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.attentionPoints.minItems"
                        ).value(0)
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.recommendations.maxItems"
                        ).value(3)
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.recommendations.items.description"
                        ).value(containsString("300 caracteres"))
                )
                .andExpect(
                        jsonPath("$.generationConfig.maxOutputTokens")
                                .value(4096)
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.thinkingConfig.thinkingLevel"
                        ).value("low")
                )
                .andRespond(withSuccess(
                        providerResponse(validInsightJson(), "STOP"),
                        MediaType.APPLICATION_JSON
                ));

        DashboardInsightResult result = client.generateInsight(validContext());

        assertAll(
                () -> assertEquals("pt-BR", result.language()),
                () -> assertEquals(
                        "O portfólio apresenta resultado positivo.",
                        result.executiveSummary()
                ),
                () -> assertEquals(
                        List.of("Retorno superior ao investimento."),
                        result.highlights()
                ),
                () -> assertEquals(
                        List.of("Projetos em andamento exigem acompanhamento."),
                        result.attentionPoints()
                ),
                () -> assertEquals(
                        List.of("Acompanhar a realização dos retornos."),
                        result.recommendations()
                )
        );
    }

    @Test
    void shouldRejectMalformedGeneratedJsonWithoutExposingIt()
            throws Exception {
        String malformedResponse = "provider-secret-{not-json";

        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        providerResponse(malformedResponse, "STOP"),
                        MediaType.APPLICATION_JSON
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertAll(
                () -> assertEquals(
                        AiProviderFailure.INVALID_RESPONSE,
                        exception.failure()
                ),
                () -> assertFalse(
                        exception.getMessage().contains(malformedResponse)
                )
        );
    }

    @Test
    void shouldRejectInvalidLanguageAndListBounds() throws Exception {
        String invalidInsight = """
                {
                  "language": "en-US",
                  "executiveSummary": "Resumo",
                  "highlights": [],
                  "attentionPoints": [],
                  "recommendations": ["Sugestão"]
                }
                """;

        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        providerResponse(invalidInsight, "STOP"),
                        MediaType.APPLICATION_JSON
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertEquals(AiProviderFailure.INVALID_RESPONSE, exception.failure());
    }

    @Test
    void shouldRejectIncompleteProviderGeneration() throws Exception {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        providerResponse(validInsightJson(), "MAX_TOKENS"),
                        MediaType.APPLICATION_JSON
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertEquals(AiProviderFailure.INVALID_RESPONSE, exception.failure());
    }

    @Test
    void shouldMapRateLimitToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withTooManyRequests());

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertEquals(AiProviderFailure.RATE_LIMITED, exception.failure());
    }

    @Test
    void shouldMapServerErrorToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withServerError());

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertEquals(AiProviderFailure.UNAVAILABLE, exception.failure());
    }

    @Test
    void shouldMapReadTimeoutToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(
                        new SocketTimeoutException("simulated timeout")
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.generateInsight(validContext())
        );

        assertEquals(AiProviderFailure.TIMEOUT, exception.failure());
    }

    private String providerResponse(
            String generatedText,
            String finishReason
    ) throws Exception {
        Map<String, Object> response = Map.of(
                "candidates", List.of(
                        Map.of(
                                "content", Map.of(
                                        "role", "model",
                                        "parts", List.of(
                                                Map.of("text", generatedText)
                                        )
                                ),
                                "finishReason", finishReason
                        )
                )
        );

        return objectMapper.writeValueAsString(response);
    }

    private String validInsightJson() {
        return """
                {
                  "language": "pt-BR",
                  "executiveSummary": "O portfólio apresenta resultado positivo.",
                  "highlights": ["Retorno superior ao investimento."],
                  "attentionPoints": ["Projetos em andamento exigem acompanhamento."],
                  "recommendations": ["Acompanhar a realização dos retornos."]
                }
                """;
    }

    private DashboardInsightContext validContext() {
        return DashboardInsightContext.orientation(
                7L,
                "Eficiência operacional",
                new DashboardSummaryResponse(
                        2,
                        Map.of(NomeStatusProjeto.EM_ANDAMENTO, 2L),
                        new BigDecimal("850000.00"),
                        new BigDecimal("1200000.00"),
                        new BigDecimal("210000.00"),
                        new BigDecimal("560000.00"),
                        new BigDecimal("32.70"),
                        new BigDecimal("11.40")
                )
        );
    }
}
