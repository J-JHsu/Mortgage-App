package edu.cs213.mortgage.model;

import java.math.BigDecimal;
import java.util.List;

/** Explicit HTTP models. Money is in dollars; rates are percentages. */
public final class MortgageApi {
    private MortgageApi() {}
    public record LookupOption(Integer code, String label) {}
    public record Options(List<LookupOption> loanTypes, List<LookupOption> loanPurposes,
            List<LookupOption> propertyTypes, List<LookupOption> counties,
            List<LookupOption> msamds, List<LookupOption> ownerOccupancy) {}
    public record SearchRequest(List<Integer> countyCodes, List<Integer> loanTypes,
            List<Integer> loanPurposes, List<Integer> propertyTypes, List<Integer> msamds,
            List<Integer> ownerOccupancy, BigDecimal incomeLoanRatioMin,
            BigDecimal incomeLoanRatioMax, BigDecimal tractToMsamdIncomeMin,
            BigDecimal tractToMsamdIncomeMax) {}
    public record MortgageResult(int applicationId, String respondentId, Long loanAmountDollars,
            Long applicantIncomeDollars, Double rateSpreadPercentPoints,
            LookupOption loanType, LookupOption loanPurpose, LookupOption propertyType,
            LookupOption ownerOccupancy, LookupOption county, LookupOption msamd,
            LookupOption actionTaken, LookupOption purchaserType, LookupOption lienStatus,
            String censusTractNumber, BigDecimal tractToMsamdIncomePercent) {}
    public record SearchResponse(List<MortgageResult> mortgages, int eligibleMortgageCount,
            Long totalLoanAmountDollars) {}
    public record RateResponse(int eligibleMortgageCount, long totalLoanAmountDollars,
            double expectedRatePercent, String portfolioFingerprint) {}
    public record PackageRequest(SearchRequest filters, String portfolioFingerprint) {}
    public record PackageResponse(boolean success, int packagedMortgageCount,
            long packagedTotalLoanAmountDollars) {}
    public record ApiError(String code, String message) {}
}
