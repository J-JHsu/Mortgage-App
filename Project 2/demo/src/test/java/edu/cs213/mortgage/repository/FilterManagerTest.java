package edu.cs213.mortgage.repository;

import edu.cs213.mortgage.model.Filter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static edu.cs213.mortgage.model.Filter.Type.*;
import static org.junit.jupiter.api.Assertions.*;

class FilterManagerTest {
    @Test
    void eligibilityAlwaysApplies() {
        assertEquals("WHERE a.action_taken = 1 AND a.purchaser_type IN (0, 1, 2, 3, 4, 8)",
                FilterManager.buildWhereClause(List.of()).whereClause());
    }

    @Test
    void singleValueIsBound() {
        var query = FilterManager.buildWhereClause(List.of(new Filter(MSAMD, 35084)));
        assertTrue(query.whereClause().endsWith("AND (a.msamd = ?)"));
        assertFalse(query.whereClause().contains("35084"));
        assertEquals(List.of(35084), query.parameters());
    }

    @Test
    void differentCategoriesUseAnd() {
        var query = FilterManager.buildWhereClause(List.of(new Filter(LOAN_TYPE, 1), new Filter(LOAN_PURPOSE, 2)));
        assertTrue(query.whereClause().endsWith("AND (a.loan_type = ?) AND (a.loan_purpose = ?)"));
        assertEquals(List.of(1, 2), query.parameters());
    }

    @Test
    void sameCategoryUsesOr() {
        var query = FilterManager.buildWhereClause(List.of(new Filter(LOAN_TYPE, 1), new Filter(LOAN_TYPE, 2)));
        assertTrue(query.whereClause().endsWith("AND (a.loan_type = ? OR a.loan_type = ?)"));
        assertEquals(List.of(1, 2), query.parameters());
    }

    @Test
    void groupedOrAndRangePreservePrecedenceAndParameterOrder() {
        var filters = new ArrayList<>(List.of(new Filter(APPLICANT_INCOME, 50, 100),
                new Filter(LOAN_TYPE, 1), new Filter(LOAN_TYPE, 2)));
        var original = List.copyOf(filters);
        var query = FilterManager.buildWhereClause(filters);
        assertTrue(query.whereClause().endsWith(
                "AND (a.loan_type = ? OR a.loan_type = ?) AND (a.applicant_income_000s BETWEEN ? AND ?)"));
        assertEquals(List.of(1, 2, 50, 100), query.parameters());
        assertEquals(original, filters);
    }

    @Test
    void invalidRangesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Filter(APPLICANT_INCOME, 100, 50));
        assertThrows(IllegalArgumentException.class, () -> new Filter(APPLICANT_INCOME, 50));
        assertThrows(IllegalArgumentException.class, () -> new Filter(LOAN_TYPE, 1, 2));
    }
}
