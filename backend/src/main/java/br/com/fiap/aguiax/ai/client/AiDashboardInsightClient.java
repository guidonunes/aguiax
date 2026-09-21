package br.com.fiap.aguiax.ai.client;

import br.com.fiap.aguiax.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiax.dto.ai.DashboardInsightResult;

public interface AiDashboardInsightClient {

    DashboardInsightResult generateInsight(DashboardInsightContext context);
}
