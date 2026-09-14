package br.com.fiap.aguiaexpress.ai.client;

import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationContext;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResult;
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

class GeminiAiIdeaEvaluationClientTest {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta";
    private static final String MODEL = "gemini-3.6-flash";
    private static final String API_KEY = "test-api-key";
    private static final String ENDPOINT =
            BASE_URL + "/models/" + MODEL + ":generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private GeminiAiIdeaEvaluationClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("x-goog-api-key", API_KEY);

        server = MockRestServiceServer.bindTo(builder).build();
        client = new GeminiAiIdeaEvaluationClient(
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
    void shouldSendStructuredRequestAndMapValidProviderResponse()
            throws Exception {
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(
                        jsonPath("$.contents[0].parts[0].text")
                                .value(containsString(
                                        "Redução de combustível"
                                ))
                )
                .andExpect(
                        jsonPath("$.generationConfig.responseMimeType")
                                .value("application/json")
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema.type"
                        ).value("object")
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.responseJsonSchema"
                                        + ".properties.overallScore.maximum"
                        ).value(10)
                )
                .andExpect(
                        jsonPath("$.generationConfig.maxOutputTokens")
                                .value(4096)
                )
                .andExpect(
                        jsonPath(
                                "$.generationConfig.thinkingConfig.thinkingLevel"
                        ).value("medium")
                )
                .andRespond(withSuccess(
                        providerResponse(validEvaluationJson(), "STOP"),
                        MediaType.APPLICATION_JSON
                ));

        AiIdeaEvaluationResult result = client.evaluate(validContext());

        assertAll(
                () -> assertEquals(9, result.strategicAlignment()),
                () -> assertEquals(8, result.impact()),
                () -> assertEquals(7, result.feasibility()),
                () -> assertEquals(8, result.innovation()),
                () -> assertEquals(
                        0,
                        result.overallScore().compareTo(
                                new BigDecimal("8.1")
                        )
                ),
                () -> assertEquals(
                        "Ideia bem alinhada à estratégia.",
                        result.summary()
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
                () -> client.evaluate(validContext())
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
    void shouldRejectProviderScoreOutsideApplicationBounds()
            throws Exception {
        String invalidEvaluation = """
                {
                  "strategicAlignment": 11,
                  "impact": 8,
                  "feasibility": 7,
                  "innovation": 8,
                  "overallScore": 8.1,
                  "summary": "Pontuação inválida."
                }
                """;

        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        providerResponse(invalidEvaluation, "STOP"),
                        MediaType.APPLICATION_JSON
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.evaluate(validContext())
        );

        assertEquals(
                AiProviderFailure.INVALID_RESPONSE,
                exception.failure()
        );
    }

    @Test
    void shouldRejectIncompleteProviderGeneration() throws Exception {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        providerResponse(
                                validEvaluationJson(),
                                "MAX_TOKENS"
                        ),
                        MediaType.APPLICATION_JSON
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.evaluate(validContext())
        );

        assertEquals(
                AiProviderFailure.INVALID_RESPONSE,
                exception.failure()
        );
    }

    @Test
    void shouldMapRateLimitToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withTooManyRequests());

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.evaluate(validContext())
        );

        assertEquals(
                AiProviderFailure.RATE_LIMITED,
                exception.failure()
        );
    }

    @Test
    void shouldMapServerErrorToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withServerError());

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.evaluate(validContext())
        );

        assertEquals(
                AiProviderFailure.UNAVAILABLE,
                exception.failure()
        );
    }

    @Test
    void shouldMapReadTimeoutToControlledFailure() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(
                        new SocketTimeoutException("simulated timeout")
                ));

        AiProviderException exception = assertThrows(
                AiProviderException.class,
                () -> client.evaluate(validContext())
        );

        assertEquals(
                AiProviderFailure.TIMEOUT,
                exception.failure()
        );
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
                                                Map.of(
                                                        "text",
                                                        generatedText
                                                )
                                        )
                                ),
                                "finishReason", finishReason
                        )
                )
        );

        return objectMapper.writeValueAsString(response);
    }

    private String validEvaluationJson() {
        return """
                {
                  "strategicAlignment": 9,
                  "impact": 8,
                  "feasibility": 7,
                  "innovation": 8,
                  "overallScore": 8.1,
                  "summary": "Ideia bem alinhada à estratégia."
                }
                """;
    }

    private AiIdeaEvaluationContext validContext() {
        return new AiIdeaEvaluationContext(
                "Redução de combustível",
                "Reduzir o consumo da frota",
                "Rotas pouco eficientes",
                "Otimização automática das rotas",
                "Eficiência operacional",
                "Redução de custos",
                8,
                7
        );
    }
}
