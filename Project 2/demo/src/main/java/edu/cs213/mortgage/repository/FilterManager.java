package edu.cs213.mortgage.repository;

import edu.cs213.mortgage.model.Filter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class FilterManager {
    static final String ELIGIBILITY = "a.action_taken = 1 AND a.purchaser_type IN (0, 1, 2, 3, 4, 8)";

    public record Query(String whereClause, List<Integer> parameters) {
        public Query {
            parameters = List.copyOf(parameters);
        }
    }

    public static Query buildWhereClause(List<Filter> filters) {
        Map<Filter.Type, List<Filter>> groups = new EnumMap<>(Filter.Type.class);
        for (Filter filter : filters) {
            groups.computeIfAbsent(filter.type(), ignored -> new ArrayList<>()).add(filter);
        }
        StringBuilder sql = new StringBuilder("WHERE ").append(ELIGIBILITY);
        List<Integer> parameters = new ArrayList<>();
        for (var group : groups.entrySet()) {
            String column = switch (group.getKey()) {
                case MSAMD -> "l.msamd";
                case LOAN_TYPE -> "a.loan_type";
                case LOAN_PURPOSE -> "a.loan_purpose";
                case PROPERTY_TYPE -> "a.property_type";
                case APPLICANT_INCOME -> "a.applicant_income_000s";
                case OWNER_OCCUPANCY -> "a.owner_occupancy";
            };
            StringJoiner alternatives = new StringJoiner(" OR ", " AND (", ")");
            for (Filter filter : group.getValue()) {
                if (filter.type() == Filter.Type.APPLICANT_INCOME) {
                    alternatives.add(column + " BETWEEN ? AND ?");
                    parameters.add(filter.value());
                    parameters.add(filter.maximum());
                } else {
                    alternatives.add(column + " = ?");
                    parameters.add(filter.value());
                }
            }
            sql.append(alternatives);
        }
        return new Query(sql.toString(), parameters);
    }
}
