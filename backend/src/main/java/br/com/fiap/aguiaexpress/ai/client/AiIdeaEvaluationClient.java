package br.com.fiap.aguiaexpress.ai.client;

import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationContext;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResult;

public interface AiIdeaEvaluationClient {

    AiIdeaEvaluationResult evaluate(AiIdeaEvaluationContext context);
}
