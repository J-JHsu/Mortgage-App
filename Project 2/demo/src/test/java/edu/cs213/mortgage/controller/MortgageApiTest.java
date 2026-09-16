package edu.cs213.mortgage.controller;

import edu.cs213.mortgage.config.JsonConfiguration;
import edu.cs213.mortgage.model.Mortgage;
import edu.cs213.mortgage.model.MortgageApi.LookupOption;
import edu.cs213.mortgage.repository.*;
import edu.cs213.mortgage.service.*;
import java.sql.SQLException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MortgageApiTest {
    MortgageDAO dao;
    LookupRepository lookups;
    MockMvc mvc;
    Mortgage valid = new Mortgage(1, "respondent", 1, 100, 1, 35620, null, null, 0, 1, 1, 1, 1);

    @BeforeEach void setup() throws Exception {
        dao = mock(MortgageDAO.class);
        lookups = mock(LookupRepository.class);
        Map<String, List<LookupOption>> values = new HashMap<>();
        for (String table : List.of("loan_type", "loan_purpose", "property_type", "owner_occupancy",
                "county", "action_type", "lien_status")) {
            values.put(table, List.of(new LookupOption(1, "One"), new LookupOption(2, "Two")));
        }
        values.put("purchaser_type", List.of(new LookupOption(0, "Not purchased")));
        values.put("msamd", List.of(new LookupOption(35620, null)));
        when(lookups.read()).thenReturn(values);
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of(valid));
        var builder = new Jackson2ObjectMapperBuilder();
        new JsonConfiguration().strictRequestTypes().customize(builder);
        mvc = MockMvcBuilders.standaloneSetup(new MortgageController(
                new MortgageApiService(new MortgageService(dao), lookups)))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(builder.build())).build();
    }
    String postJson(String endpoint, String json, int status) throws Exception {
        return mvc.perform(post("/api/mortgages/" + endpoint).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(status)).andReturn().getResponse().getContentAsString();
    }
    @Test void optionsPreserveMissingNames() throws Exception {
        mvc.perform(get("/api/mortgages/options")).andExpect(status().isOk())
                .andExpect(jsonPath("$.msamds[0].code").value(35620))
                .andExpect(jsonPath("$.msamds[0].label").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.counties.length()").value(2));
    }
    @Test void searchUsesDollarsAndPreservesNulls() throws Exception {
        String body = postJson("search", "{\"loanTypes\":[1]}", 200);
        assertTrue(body.contains("\"loanAmountDollars\":100000"));
        assertTrue(body.contains("\"applicantIncomeDollars\":null"));
        verify(dao).getFilteredMortgages(argThat(filters ->
                FilterManager.buildWhereClause(filters).parameters().equals(List.of(1))));
    }
    @Test void categoriesCombineAndValuesAreBound() throws Exception {
        postJson("search", """
                {"countyCodes":[1,2],"loanTypes":[1],"incomeLoanRatioMin":0.5,"tractToMsamdIncomeMax":150}
                """, 200);
        verify(dao).getFilteredMortgages(argThat(filters -> {
            var q = FilterManager.buildWhereClause(filters);
            return q.whereClause().contains("AND (l.county_code = ? OR l.county_code = ?)")
                    && q.whereClause().contains("AND (a.loan_type = ?)")
                    && q.whereClause().contains("NULLIF(a.loan_amount_000s, 0)) >= ?")
                    && q.whereClause().contains("l.tract_to_msamd_income <= ?")
                    && q.parameters().equals(List.of(1, 1, 2, new java.math.BigDecimal("0.5"),
                            new java.math.BigDecimal("150")));
        }));
    }
    @Test void emptyArraysMeanUnfilteredAndNoResultsAreSuccessful() throws Exception {
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of());
        assertTrue(postJson("search", "{\"loanTypes\":[]}", 200).contains("\"totalLoanAmountDollars\":0"));
        verify(dao).getFilteredMortgages(List.of());
    }
    @Test void missingAmountDoesNotBecomePartialTotal() throws Exception {
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of(
                new Mortgage(2, "r", 1, null, 1, null, null, null, 0, 1, 1, 1, 1)));
        assertTrue(postJson("search", "{}", 200).contains("\"totalLoanAmountDollars\":null"));
    }
    @Test void invalidCodesRangesAndJsonNeverReachMortgageQuery() throws Exception {
        for (String body : List.of("{\"loanTypes\":[99]}", "{\"loanTypes\":[null]}",
                "{\"incomeLoanRatioMin\":2,\"incomeLoanRatioMax\":1}",
                "{\"tractToMsamdIncomeMin\":-1}", "{\"loanTypes\":[1.5]}",
                "{\"loanTypes\":[\"1\"]}", "{\"sql\":\"OR 1=1\"}", "{", "null")) {
            postJson("search", body, 400);
        }
        verifyNoInteractions(dao);
    }
    @Test void rateUsesExistingCalculator() throws Exception {
        String body = postJson("rate", "{}", 200);
        assertTrue(body.contains("\"expectedRatePercent\":3.83"));
        assertTrue(body.contains("\"totalLoanAmountDollars\":100000"));
        assertTrue(body.contains("portfolioFingerprint"));
    }
    @Test void emptyAndUnsupportedPortfoliosCannotBeQuoted() throws Exception {
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of());
        assertTrue(postJson("rate", "{}", 422).contains("EMPTY_PORTFOLIO"));
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of(
                new Mortgage(2, "r", 1, 100, 1, null, null, null, 0, 3, 1, 1, 1)));
        assertTrue(postJson("rate", "{}", 422).contains("UNQUOTABLE_PORTFOLIO"));
    }
    String packageBody() throws Exception {
        String quote = postJson("rate", "{}", 200);
        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(quote)
                .get("portfolioFingerprint").asText();
        return "{\"filters\":{},\"portfolioFingerprint\":\"" + token + "\"}";
    }
    @Test void packagingUsesServerSelectedIds() throws Exception {
        when(dao.packageMortgages(List.of(1))).thenReturn(1);
        String body = postJson("package", packageBody(), 200);
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"packagedTotalLoanAmountDollars\":100000"));
        verify(dao).packageMortgages(List.of(1));
    }
    @Test void staleAndInvalidPackageRequestsCannotWrite() throws Exception {
        String body = packageBody();
        when(dao.getFilteredMortgages(anyList())).thenReturn(List.of());
        postJson("package", body, 409);
        postJson("package", "{}", 400);
        postJson("package", "{\"applicationIds\":[1]}", 400);
        verify(dao, never()).packageMortgages(anyList());
    }
    @Test void transactionConflictAndDatabaseFailureAreSafeErrors() throws Exception {
        String body = packageBody();
        when(dao.packageMortgages(anyList())).thenThrow(new SQLException("secret SQL path", "40001"));
        assertTrue(postJson("package", body, 409).contains("PACKAGING_CONFLICT"));
        doThrow(new SQLException("secret SQL path", "08006")).when(dao).packageMortgages(anyList());
        String failure = postJson("package", body, 503);
        assertTrue(failure.contains("DATABASE_ERROR"));
        assertFalse(failure.contains("secret"));
        when(lookups.read()).thenThrow(new SQLException("password secret"));
        mvc.perform(get("/api/mortgages/options")).andExpect(status().isServiceUnavailable());
    }
    @Test void corsOnlyAcceptsLocalViteOrigins() throws Exception {
        mvc.perform(get("/api/mortgages/options").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(get("/api/mortgages/options").header("Origin", "https://example.com"))
                .andExpect(status().isForbidden());
    }
}
