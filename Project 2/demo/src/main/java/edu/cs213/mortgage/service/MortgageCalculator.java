package edu.cs213.mortgage.service;

import edu.cs213.mortgage.model.Mortgage;
import java.util.List;
import java.util.OptionalDouble;

public class MortgageCalculator {
    private static final double BASE_RATE = 2.33;

    /** Empty means the complete portfolio cannot be quoted, not a base-rate quote. */
    public static OptionalDouble calculateRate(List<Mortgage> mortgages) {
        if (mortgages == null || mortgages.isEmpty()) {
            return OptionalDouble.empty();
        }
        double weightedSum = 0;
        double totalLoanAmount = 0;
        for (Mortgage mortgage : mortgages) {
            if (mortgage == null || mortgage.getLoanAmount() <= 0) {
                return OptionalDouble.empty();
            }
            OptionalDouble rate = determineFinalRate(mortgage);
            if (rate.isEmpty()) {
                return OptionalDouble.empty();
            }
            // Existing HMDA loan amounts remain in thousands.
            weightedSum += rate.getAsDouble() * mortgage.getLoanAmount();
            totalLoanAmount += mortgage.getLoanAmount();
        }
        double rate = weightedSum / totalLoanAmount;
        return Double.isFinite(rate) ? OptionalDouble.of(rate) : OptionalDouble.empty();
    }

    private static OptionalDouble determineFinalRate(Mortgage mortgage) {
        Double spread = mortgage.getRateSpread();
        if (spread != null && !Double.isFinite(spread)) {
            return OptionalDouble.empty();
        }
        // Preserve the legacy nonpositive unknown marker until the real CSV is mapped.
        if (spread == null || spread <= 0) {
            return switch (mortgage.getLienStatus()) {
                case 1 -> OptionalDouble.of(BASE_RATE + 1.5);
                case 2 -> OptionalDouble.of(BASE_RATE + 3.5);
                default -> OptionalDouble.empty();
            };
        }
        return OptionalDouble.of(BASE_RATE + spread);
    }
}
