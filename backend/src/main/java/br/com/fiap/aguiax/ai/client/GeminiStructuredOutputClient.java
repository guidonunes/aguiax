package br.com.fiap.aguiax.ai.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

final class GeminiStructuredOutputClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            GeminiStructuredOutputClient.class
    );
    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String THINKING_LEVEL = "low";
    private static final int MAX_OUTPUT_TOKENS = 4096;
    private static final Pattern MODEL_NAME_PATTERN =
            Pattern.compile("[a-zA-Z0-9._-]+");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    GeminiStructuredOutputClient(
            String baseUrl,
            String apiKey,
            String model,
            Duration connectTimeout,
            Duration readTimeout,
            ObjectMapper objectMapper
    ) {
        this(
                createRestClient(
                        baseUrl,
                        apiKey,
                        connectTimeout,
                        readTimeout
                ),
                objectMapper,
                model
        );
    }

    GeminiStructuredOutputClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            String model
    ) {
        this.restClient = Objects.requireNonNull(
                restClient,
                "RestClient é obrigatório"
        );
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "ObjectMapper é obrigatório"
        );
        this.model = requireValidModel(model);
    }

    <T> T generate(
            Object context,
            String promptTemplate,
            Map<String, Object> responseSchema,
            Class<T> responseType,
            String operation,
            String requestFailureMessage,
            String invalidResponseMessage
    ) {
        Objects.requireNonNull(context, "O contexto da solicitação de IA é obrigatório");
        Objects.requireNonNull(responseSchema, "O schema da resposta de IA é obrigatório");
        Objects.requireNonNull(responseType, "O tipo da resposta de IA é obrigatório");

        GeminiGenerateContentRequest request = buildRequest(
                context,
                requireNonBlank(
                        promptTemplate,
                        "O prompt da solicitação de IA é obrigatório"
                ),
                responseSchema,
                requestFailureMessage
        );
        String providerResponse = invokeProvider(request);

        return parseProviderResponse(
                providerResponse,
                responseType,
                requireNonBlank(operation, "A operação de IA é obrigatória"),
                invalidResponseMessage
        );
    }

    private GeminiGenerateContentRequest buildRequest(
            Object context,
            String promptTemplate,
            Map<String, Object> responseSchema,
            String requestFailureMessage
    ) {
        try {
            String contextJson = objectMapper.writeValueAsString(context);
            String prompt = promptTemplate.formatted(contextJson);

            GeminiContent content = new GeminiContent(
                    "user",
                    List.of(new GeminiPart(prompt))
            );
            GeminiGenerationConfig generationConfig =
                    new GeminiGenerationConfig(
                            "application/json",
                            responseSchema,
                            0.2,
                            MAX_OUTPUT_TOKENS,
                            new GeminiThinkingConfig(THINKING_LEVEL)
                    );

            return new GeminiGenerateContentRequest(
                    List.of(content),
                    generationConfig
            );
        } catch (JacksonException exception) {
            throw new AiProviderException(
                    AiProviderFailure.REQUEST_BUILD_FAILED,
                    requestFailureMessage,
                    exception
            );
        }
    }

    private String invokeProvider(GeminiGenerateContentRequest request) {
        try {
            return restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 408) {
                throw timeoutException(exception);
            }

            if (exception.getStatusCode().value() == 429) {
                throw new AiProviderException(
                        AiProviderFailure.RATE_LIMITED,
                        "O limite temporário do provedor de IA foi atingido",
                        exception
                );
            }

            throw new AiProviderException(
                    AiProviderFailure.REJECTED_REQUEST,
                    "O provedor de IA rejeitou a solicitação",
                    exception
            );
        } catch (HttpServerErrorException exception) {
            if (exception.getStatusCode().value() == 504) {
                throw timeoutException(exception);
            }

            throw new AiProviderException(
                    AiProviderFailure.UNAVAILABLE,
                    "O provedor de IA está temporariamente indisponível",
                    exception
            );
        } catch (ResourceAccessException exception) {
            if (containsTimeout(exception)) {
                throw timeoutException(exception);
            }

            throw new AiProviderException(
                    AiProviderFailure.UNAVAILABLE,
                    "Não foi possível acessar o provedor de IA",
                    exception
            );
        } catch (RestClientException exception) {
            throw new AiProviderException(
                    AiProviderFailure.UNAVAILABLE,
                    "Falha inesperada ao acessar o provedor de IA",
                    exception
            );
        }
    }

    private <T> T parseProviderResponse(
            String providerResponse,
            Class<T> responseType,
            String operation,
            String invalidResponseMessage
    ) {
        if (providerResponse == null || providerResponse.isBlank()) {
            LOGGER.warn(
                    "Resposta inválida do provedor de IA: operation={} reason=EMPTY_RESPONSE",
                    operation
            );
            throw invalidResponse(invalidResponseMessage, null);
        }

        try {
            JsonNode root = objectMapper.readTree(providerResponse);
            JsonNode candidate = root.at("/candidates/0");

            if (candidate.isMissingNode()) {
                LOGGER.warn(
                        "Resposta inválida do provedor de IA: operation={} reason=MISSING_CANDIDATE",
                        operation
                );
                throw invalidResponse(invalidResponseMessage, null);
            }

            String finishReason = candidate.path("finishReason").asString("");
            if (!"STOP".equals(finishReason)) {
                LOGGER.warn(
                        "Resposta incompleta do provedor de IA: operation={} finishReason={}",
                        operation,
                        finishReason.isBlank() ? "UNSPECIFIED" : finishReason
                );
                throw invalidResponse(invalidResponseMessage, null);
            }

            JsonNode parts = candidate.at("/content/parts");
            if (!parts.isArray() || parts.isEmpty()) {
                LOGGER.warn(
                        "Resposta inválida do provedor de IA: operation={} reason=MISSING_PARTS",
                        operation
                );
                throw invalidResponse(invalidResponseMessage, null);
            }

            StringBuilder generatedJson = new StringBuilder();
            for (JsonNode part : parts) {
                JsonNode text = part.get("text");
                if (text != null && text.isString()) {
                    generatedJson.append(text.stringValue());
                }
            }

            if (generatedJson.isEmpty()) {
                LOGGER.warn(
                        "Resposta inválida do provedor de IA: operation={} reason=EMPTY_CONTENT",
                        operation
                );
                throw invalidResponse(invalidResponseMessage, null);
            }

            return objectMapper.readValue(
                    generatedJson.toString(),
                    responseType
            );
        } catch (AiProviderException exception) {
            throw exception;
        } catch (JacksonException | IllegalArgumentException exception) {
            LOGGER.warn(
                    "Conteúdo inválido do provedor de IA: operation={} validationFailureType={}",
                    operation,
                    exception.getClass().getSimpleName()
            );
            throw invalidResponse(invalidResponseMessage, exception);
        }
    }

    private static RestClient createRestClient(
            String baseUrl,
            String apiKey,
            Duration connectTimeout,
            Duration readTimeout
    ) {
        String validatedBaseUrl = requireSecureBaseUrl(baseUrl);
        String validatedApiKey = requireNonBlank(
                apiKey,
                "A chave da API de IA é obrigatória"
        );
        Duration validatedConnectTimeout = requirePositiveDuration(
                connectTimeout,
                "O timeout de conexão deve ser positivo"
        );
        Duration validatedReadTimeout = requirePositiveDuration(
                readTimeout,
                "O timeout de leitura deve ser positivo"
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(validatedConnectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(validatedReadTimeout);

        return RestClient.builder()
                .baseUrl(validatedBaseUrl)
                .defaultHeader(API_KEY_HEADER, validatedApiKey)
                .requestFactory(requestFactory)
                .build();
    }

    private static boolean containsTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof HttpTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static AiProviderException timeoutException(Throwable cause) {
        return new AiProviderException(
                AiProviderFailure.TIMEOUT,
                "O provedor de IA excedeu o tempo limite de resposta",
                cause
        );
    }

    private static AiProviderException invalidResponse(
            String message,
            Throwable cause
    ) {
        return new AiProviderException(
                AiProviderFailure.INVALID_RESPONSE,
                message,
                cause
        );
    }

    private static String requireSecureBaseUrl(String baseUrl) {
        String value = requireNonBlank(
                baseUrl,
                "A URL base do provedor de IA é obrigatória"
        );
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "A URL base do provedor de IA é inválida",
                    exception
            );
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null) {
            throw new IllegalArgumentException(
                    "A URL base do provedor de IA deve usar HTTPS"
            );
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

    private static String requireValidModel(String model) {
        String value = requireNonBlank(model, "O modelo de IA é obrigatório");
        if (!MODEL_NAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "O identificador do modelo de IA é inválido"
            );
        }
        return value;
    }

    private static Duration requirePositiveDuration(
            Duration duration,
            String message
    ) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(message);
        }
        return duration;
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private record GeminiGenerateContentRequest(
            List<GeminiContent> contents,
            GeminiGenerationConfig generationConfig
    ) {
    }

    private record GeminiContent(String role, List<GeminiPart> parts) {
    }

    private record GeminiPart(String text) {
    }

    private record GeminiGenerationConfig(
            String responseMimeType,
            Map<String, Object> responseJsonSchema,
            double temperature,
            int maxOutputTokens,
            GeminiThinkingConfig thinkingConfig
    ) {
    }

    private record GeminiThinkingConfig(String thinkingLevel) {
    }
}
