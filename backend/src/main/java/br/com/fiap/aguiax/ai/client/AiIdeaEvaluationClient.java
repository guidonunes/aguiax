package br.com.fiap.aguiax.ai.client;

import br.com.fiap.aguiax.dto.ai.AiIdeaEvaluationContext;
import br.com.fiap.aguiax.dto.ai.AiIdeaEvaluationResult;

public interface AiIdeaEvaluationClient {

    AiIdeaEvaluationResult evaluate(AiIdeaEvaluationContext context);
}
