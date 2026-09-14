package br.com.fiap.aguiaexpress.config.observability;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        businessMetrics = new BusinessMetrics(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
        meterRegistry.close();
    }

    @Test
    void recordsSuccessfulAndFailedAiEvaluations() {
        Timer.Sample successfulEvaluation = businessMetrics.startAiEvaluation();
        businessMetrics.recordAiEvaluationSuccess(successfulEvaluation);

        Timer.Sample failedEvaluation = businessMetrics.startAiEvaluation();
        businessMetrics.recordAiEvaluationFailure(failedEvaluation);

        assertAll(
                () -> assertEquals(
                        1L,
                        aiEvaluationTimer("success").count()
                ),
                () -> assertEquals(
                        1L,
                        aiEvaluationTimer("failure").count()
                ),
                () -> assertEquals(
                        1.0,
                        meterRegistry
                                .get(BusinessMetrics.AI_EVALUATION_FAILURES)
                                .counter()
                                .count()
                )
        );
    }

    @Test
    void recordsBusinessCountersImmediatelyWithoutAnActiveTransaction() {
        businessMetrics.recordIdeaApproved();
        businessMetrics.recordProjectCreated();

        assertAll(
                () -> assertEquals(
                        1.0,
                        counterValue(BusinessMetrics.IDEAS_APPROVED)
                ),
                () -> assertEquals(
                        1.0,
                        counterValue(BusinessMetrics.PROJECTS_CREATED)
                )
        );
    }

    @Test
    void recordsProjectCreationOnlyAfterTransactionCommit() {
        beginTransactionSynchronization();

        businessMetrics.recordProjectCreated();

        assertEquals(0.0, counterValue(BusinessMetrics.PROJECTS_CREATED));

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        assertEquals(1.0, counterValue(BusinessMetrics.PROJECTS_CREATED));
    }

    @Test
    void doesNotRecordProjectCreationAfterTransactionRollback() {
        beginTransactionSynchronization();

        businessMetrics.recordProjectCreated();

        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK
                )
        );

        assertEquals(0.0, counterValue(BusinessMetrics.PROJECTS_CREATED));
    }

    private Timer aiEvaluationTimer(String outcome) {
        return meterRegistry
                .get(BusinessMetrics.AI_EVALUATION_DURATION)
                .tag("outcome", outcome)
                .timer();
    }

    private double counterValue(String metricName) {
        return meterRegistry.get(metricName).counter().count();
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }
}
