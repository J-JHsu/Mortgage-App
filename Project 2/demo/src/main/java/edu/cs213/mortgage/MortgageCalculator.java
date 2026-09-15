package edu.cs213.mortgage;

import java.util.List;

public class MortgageCalculator {
    private static final double BASE_RATE = 2.33;

    /**
     * Calculates the expected weighted average rate from the selected mortgages.
     *
     * @param mortgages List of filtered mortgages
     * @return Expected average rate based on loan amounts and rate spread
     */
    public static double calculateRate(List<Mortgage> mortgages) {
        double weightedSum = 0;
        double totalLoanAmount = 0;

        for (Mortgage m : mortgages) {
            double finalRate = determineFinalRate(m);

            // Loan Amount from DB is in thousands
            double loanAmount = m.getLoanAmount();

            // Calculate weighted sum and total loan amount
            weightedSum += finalRate * loanAmount;
            totalLoanAmount += loanAmount;
        }
        System.out.printf("Total Loan Amount: $%.2f%n", totalLoanAmount * 1000);
        return totalLoanAmount > 0 ? weightedSum / totalLoanAmount : BASE_RATE;
    }

    /**
     * Determines the final rate based on lien status and known rate spread.
     * 
     * @param m Current mortgage record
     * @return Calculated interest rate
     */
    private static double determineFinalRate(Mortgage m) {
        double rateSpread = m.getRateSpread();
        int lienStatus = m.getLienStatus();

        // If rate spread is unknown or less than required, assign max spread
        if (rateSpread <= 0 || 
            (lienStatus == 1 && rateSpread < 1.5) || 
            (lienStatus == 2 && rateSpread < 3.5)) {
            
            if (lienStatus == 1) {
                return BASE_RATE + 1.5;
            } else if (lienStatus == 2) {
                return BASE_RATE + 3.5;
            }
        }

        // Use known rate spread if it's valid
        return rateSpread + BASE_RATE;
    }
}