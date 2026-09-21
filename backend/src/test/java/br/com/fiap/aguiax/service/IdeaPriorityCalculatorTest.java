package br.com.fiap.aguiax.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdeaPriorityCalculatorTest {

    private final IdeaPriorityCalculator calculator =
            new IdeaPriorityCalculator();

    @Test
    void shouldCalculateTheDocumentedWeightedFormulaExactly() {
        BigDecimal result = calculator.calculate(
                5,
                8,
                6,
                new BigDecimal("9.0")
        );

        assertEquals(new BigDecimal("7.35"), result);
    }

    @Test
    void shouldTreatEveryMissingSignalAsZero() {
        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculate(null, null, null, null)
        );
    }

    @Test
    void shouldClampVotesToTheInclusiveZeroToTenRange() {
        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculate(-1, 0, 0, BigDecimal.ZERO)
        );
        assertEquals(
                new BigDecimal("2.00"),
                calculator.calculate(10, 0, 0, BigDecimal.ZERO)
        );
        assertEquals(
                new BigDecimal("2.00"),
                calculator.calculate(100, 0, 0, BigDecimal.ZERO)
        );
    }

    @Test
    void shouldProduceTenForMaximumInputs() {
        assertEquals(
                new BigDecimal("10.00"),
                calculator.calculate(10, 10, 10, BigDecimal.TEN)
        );
    }

    @Test
    void shouldRoundOnlyTheFinalResultUsingHalfUp() {
        assertEquals(
                new BigDecimal("2.84"),
                calculator.calculate(
                        0,
                        0,
                        0,
                        new BigDecimal("8.10")
                )
        );
    }

    @Test
    void shouldReturnTheSameValueForTheSameInputs() {
        BigDecimal first = calculator.calculate(
                7,
                9,
                4,
                new BigDecimal("8.25")
        );
        BigDecimal second = calculator.calculate(
                7,
                9,
                4,
                new BigDecimal("8.25")
        );

        assertEquals(first, second);
        assertEquals(new BigDecimal("7.34"), first);
    }

    @Test
    void shouldRejectHumanOrAiScoresOutsideZeroToTen() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(0, -1, 0, BigDecimal.ZERO)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(0, 0, 11, BigDecimal.ZERO)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        0,
                        0,
                        0,
                        new BigDecimal("10.01")
                )
        );
    }
}
