package br.com.fiap.aguiaexpress.ai.client;

import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightResult;

public interface AiDashboardInsightClient {

    DashboardInsightResult generateInsight(DashboardInsightContext context);
}
