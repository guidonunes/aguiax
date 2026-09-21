package br.com.fiap.aguiax.config.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class BusinessMetrics {

    public static final String AI_EVALUATION_DURATION =
            "aguiax.ai.evaluation.duration";

    public static final String AI_EVALUATION_FAILURES =
            "aguiax.ai.evaluation.failures";

    public static final String IDEAS_APPROVED =
            "aguiax.ideas.approved";

    public static final String PROJECTS_CREATED =
            "aguiax.projects.created";

    private final MeterRegistry meterRegistry;

    private final Timer successfulAiEvaluations;
    private final Timer failedAiEvaluations;

    private final Counter aiEvaluationFailures;
    private final Counter ideasApproved;
    private final Counter projectsCreated;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.successfulAiEvaluations = Timer.builder(AI_EVALUATION_DURATION)
                .description("Tempo das avaliações de ideias por IA")
                .tag("outcome", "success")
                .register(meterRegistry);

        this.failedAiEvaluations = Timer.builder(AI_EVALUATION_DURATION)
                .description("Tempo das avaliações de ideias por IA")
                .tag("outcome", "failure")
                .register(meterRegistry);

        this.aiEvaluationFailures = Counter.builder(AI_EVALUATION_FAILURES)
                .description("Quantidade de avaliações de IA que falharam")
                .register(meterRegistry);

        this.ideasApproved = Counter.builder(IDEAS_APPROVED)
                .description("Quantidade de ideias aprovadas")
                .register(meterRegistry);

        this.projectsCreated = Counter.builder(PROJECTS_CREATED)
                .description("Quantidade de projetos criados")
                .register(meterRegistry);
    }

    public Timer.Sample startAiEvaluation() {
        return Timer.start(meterRegistry);
    }

    public void recordAiEvaluationSuccess(Timer.Sample sample) {
        sample.stop(successfulAiEvaluations);
    }

    public void recordAiEvaluationFailure(Timer.Sample sample) {
        aiEvaluationFailures.increment();
        sample.stop(failedAiEvaluations);
    }

    public void recordIdeaApproved() {
        recordAfterCommit(ideasApproved::increment);
    }

    public void recordProjectCreated() {
        recordAfterCommit(projectsCreated::increment);
    }

    private void recordAfterCommit(Runnable recordingAction) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            recordingAction.run();
                        }
                    }
            );

            return;
        }

        recordingAction.run();
    }
}
