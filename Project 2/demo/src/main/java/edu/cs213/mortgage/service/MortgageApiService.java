package edu.cs213.mortgage.service;

import edu.cs213.mortgage.model.Filter;
import edu.cs213.mortgage.model.Mortgage;
import edu.cs213.mortgage.model.MortgageApi.*;
import edu.cs213.mortgage.repository.LookupRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class MortgageApiService {
    private final MortgageService mortgages;
    private final LookupRepository lookups;
    public MortgageApiService(MortgageService mortgages, LookupRepository lookups) {
        this.mortgages = mortgages;
        this.lookups = lookups;
    }
    public Options options() throws SQLException {
        var values = lookups.read();
        return new Options(values.get("loan_type"), values.get("loan_purpose"),
                values.get("property_type"), values.get("county"), values.get("msamd"),
                values.get("owner_occupancy"));
    }
    public SearchResponse search(SearchRequest request) throws SQLException {
        var values = lookups.read();
        var rows = mortgages.search(filters(request, values));
        return new SearchResponse(rows.stream().map(m -> result(m, values)).toList(),
                rows.size(), total(rows));
    }
    public RateResponse rate(SearchRequest request) throws SQLException {
        var portfolio = review(request);
        requireQuote(portfolio);
        return new RateResponse(portfolio.loanCount(), total(portfolio.mortgages()),
                portfolio.rate().getAsDouble(), fingerprint(portfolio.mortgages()));
    }
    public PackageResponse packageMortgages(PackageRequest request) throws SQLException {
        if (request == null || request.filters() == null || request.portfolioFingerprint() == null
                || !request.portfolioFingerprint().matches("[0-9a-f]{64}")) {
            throw invalid("Filters and a portfolio fingerprint from a successful rate review are required.");
        }
        var portfolio = review(request.filters());
        if (!fingerprint(portfolio.mortgages()).equals(request.portfolioFingerprint())) {
            throw new ApiFailure(409, "PORTFOLIO_CHANGED", "The eligible portfolio changed. Search and review its rate again.");
        }
        requireQuote(portfolio);
        int count = mortgages.packageMortgages(portfolio);
        return new PackageResponse(true, count, total(portfolio.mortgages()));
    }
    private MortgageService.Portfolio review(SearchRequest request) throws SQLException {
        return mortgages.review(filters(request, lookups.read()));
    }
    private static void requireQuote(MortgageService.Portfolio portfolio) {
        if (portfolio.loanCount() == 0) {
            throw new ApiFailure(422, "EMPTY_PORTFOLIO", "No eligible mortgages match these filters.");
        }
        if (portfolio.rate().isEmpty()) {
            throw new ApiFailure(422, "UNQUOTABLE_PORTFOLIO",
                    "The portfolio contains missing or invalid loan amounts, or unsupported spread/lien values.");
        }
    }
    static List<Filter> filters(SearchRequest r, Map<String, List<LookupOption>> values) {
        if (r == null) throw invalid("A JSON filter object is required.");
        List<Filter> result = new ArrayList<>();
        codes(result, r.countyCodes(), Filter.Type.COUNTY, "county", values);
        codes(result, r.loanTypes(), Filter.Type.LOAN_TYPE, "loan_type", values);
        codes(result, r.loanPurposes(), Filter.Type.LOAN_PURPOSE, "loan_purpose", values);
        codes(result, r.propertyTypes(), Filter.Type.PROPERTY_TYPE, "property_type", values);
        codes(result, r.msamds(), Filter.Type.MSAMD, "msamd", values);
        codes(result, r.ownerOccupancy(), Filter.Type.OWNER_OCCUPANCY, "owner_occupancy", values);
        range(result, Filter.Type.INCOME_LOAN_RATIO, r.incomeLoanRatioMin(), r.incomeLoanRatioMax());
        range(result, Filter.Type.TRACT_INCOME, r.tractToMsamdIncomeMin(), r.tractToMsamdIncomeMax());
        return List.copyOf(result);
    }
    private static void codes(List<Filter> result, List<Integer> codes, Filter.Type type,
            String domain, Map<String, List<LookupOption>> values) {
        if (codes == null) return;
        Set<Integer> accepted = new HashSet<>();
        values.get(domain).forEach(option -> accepted.add(option.code()));
        for (Integer code : new LinkedHashSet<>(codes)) {
            if (code == null || !accepted.contains(code)) throw invalid("Unknown or null code for " + domain + ".");
            result.add(new Filter(type, code));
        }
    }
    private static void range(List<Filter> result, Filter.Type type, BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return;
        if ((min != null && !Double.isFinite(min.doubleValue()))
                || (max != null && !Double.isFinite(max.doubleValue()))) {
            throw invalid("Range bounds must be finite numbers.");
        }
        try {
            result.add(new Filter(type, min, max));
        } catch (IllegalArgumentException failure) {
            throw invalid("Range bounds must be nonnegative, with minimum no greater than maximum.");
        }
    }
    private static ApiFailure invalid(String message) { return new ApiFailure(400, "INVALID_REQUEST", message); }
    private static Long dollars(Integer thousands) { return thousands == null ? null : thousands * 1000L; }
    private static Long total(List<Mortgage> rows) {
        if (rows.stream().anyMatch(m -> m.getLoanAmount() == null)) return null;
        return rows.stream().mapToLong(m -> dollars(m.getLoanAmount())).sum();
    }
    private static LookupOption option(Map<String, List<LookupOption>> values, String table, Integer code) {
        if (code == null) return null;
        return values.get(table).stream().filter(o -> o.code().equals(code)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing reference data"));
    }
    private static MortgageResult result(Mortgage m, Map<String, List<LookupOption>> values) {
        return new MortgageResult(m.getApplicationId(), m.getRespondentId(), dollars(m.getLoanAmount()),
                dollars(m.getApplicantIncome()), m.getRateSpread(),
                option(values, "loan_type", m.getLoanType()), option(values, "loan_purpose", m.getLoanPurpose()),
                option(values, "property_type", m.getPropertyType()), option(values, "owner_occupancy", m.getOwnerOccupancy()),
                option(values, "county", m.getCountyCode()), option(values, "msamd", m.getMsamd()),
                option(values, "action_type", m.getActionTaken()), option(values, "purchaser_type", m.getPurchaserType()),
                option(values, "lien_status", m.getLienStatus()), m.getCensusTractNumber(), m.getTractToMsamdIncome());
    }
    private static String fingerprint(List<Mortgage> rows) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            rows.stream().sorted(Comparator.comparingInt(Mortgage::getApplicationId)).forEach(m -> {
                String value = m.getApplicationId() + ":" + m.getLoanAmount() + ":" + m.getRateSpread()
                        + ":" + m.getLienStatus() + ":" + m.getPurchaserType() + "\n";
                digest.update(value.getBytes(StandardCharsets.UTF_8));
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
