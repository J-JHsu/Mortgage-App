package edu.cs213.mortgage.service;

import edu.cs213.mortgage.model.Mortgage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class MortgageCalculatorTest {
    static Mortgage mortgage(int id, Integer amount, Double spread, int lien) {
        return new Mortgage(id, "respondent", 1, amount, 1, 35084, 100, spread, 0, lien, 1, 1, 1);
    }

    @ParameterizedTest
    @CsvSource({"0.5,1,2.83", "0.5,2,2.83", "4.0,1,6.33", "0.0,1,3.83", "0.0,2,5.83"})
    void knownSpreadIsNotFlooredAndLegacyUnknownUsesLien(double spread, int lien, double expected) {
        assertEquals(expected, MortgageCalculator.calculateRate(List.of(mortgage(1, 100, spread, lien)))
                .orElseThrow(), 0.000001);
    }

    @Test
    void nullSpreadUsesLienAssumption() {
        assertEquals(3.83, MortgageCalculator.calculateRate(List.of(mortgage(1, 100, null, 1)))
                .orElseThrow(), 0.000001);
        assertEquals(5.83, MortgageCalculator.calculateRate(List.of(mortgage(1, 100, null, 2)))
                .orElseThrow(), 0.000001);
    }

    @Test
    void weightsByLoanAmountInThousands() {
        assertEquals(4.58, MortgageCalculator.calculateRate(List.of(
                mortgage(1, 100, 0.0, 1), mortgage(2, 300, 2.5, 1))).orElseThrow(), 0.000001);
    }

    @Test
    void emptyAndInvalidPortfoliosHaveNoQuote() {
        assertTrue(MortgageCalculator.calculateRate(List.of()).isEmpty());
        assertTrue(MortgageCalculator.calculateRate(null).isEmpty());
        for (Mortgage invalid : List.of(mortgage(1, null, 1.0, 1), mortgage(1, 0, 1.0, 1), mortgage(1, -1, 1.0, 1),
                mortgage(1, 100, Double.NaN, 1), mortgage(1, 100, Double.POSITIVE_INFINITY, 1),
                mortgage(1, 100, null, 3))) {
            assertTrue(MortgageCalculator.calculateRate(List.of(mortgage(2, 100, 1.0, 1), invalid)).isEmpty());
        }
    }
}
