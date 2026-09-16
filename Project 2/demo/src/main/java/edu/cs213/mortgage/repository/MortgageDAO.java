package edu.cs213.mortgage.repository;

import edu.cs213.mortgage.config.DatabaseCon;
import edu.cs213.mortgage.model.Filter;
import edu.cs213.mortgage.model.Mortgage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MortgageDAO {
    private final DatabaseCon database;

    public MortgageDAO(DatabaseCon database) {
        this.database = database;
    }

    public List<Mortgage> getFilteredMortgages(List<Filter> filters) throws SQLException {
        String query = """
            SELECT a.application_id, a.respondent_id, a.loan_type, a.loan_amount_000s,
                   a.action_taken, l.msamd, a.applicant_income_000s, a.rate_spread,
                   a.purchaser_type, a.lien_status, a.property_type, a.loan_purpose, a.owner_occupancy
            FROM application a
            JOIN location l ON l.location_id = a.location_id
            """;
        FilterManager.Query conditions = FilterManager.buildWhereClause(filters);
        List<Mortgage> mortgages = new ArrayList<>();
        try (Connection conn = database.connect();
             PreparedStatement stmt = conn.prepareStatement(query + conditions.whereClause())) {
            for (int i = 0; i < conditions.parameters().size(); i++) {
                stmt.setInt(i + 1, conditions.parameters().get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double spread = rs.getDouble("rate_spread");
                    Double rateSpread = rs.wasNull() ? null : spread;
                    mortgages.add(new Mortgage(
                        rs.getInt("application_id"), rs.getString("respondent_id"),
                        rs.getInt("loan_type"), rs.getObject("loan_amount_000s", Integer.class),
                        rs.getInt("action_taken"), rs.getObject("msamd", Integer.class),
                        rs.getObject("applicant_income_000s", Integer.class), rateSpread,
                        rs.getInt("purchaser_type"), rs.getInt("lien_status"),
                        rs.getInt("property_type"), rs.getInt("loan_purpose"),
                        rs.getInt("owner_occupancy")));
                }
            }
        }
        return mortgages;
    }

    /** Packages reviewed IDs atomically; missing or no-longer-eligible rows fail the whole set. */
    public int packageMortgages(List<Integer> applicationIds) throws SQLException {
        List<Integer> ids = List.copyOf(applicationIds);
        if (ids.isEmpty() || ids.stream().anyMatch(id -> id <= 0)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Packaging requires distinct positive application IDs.");
        }
        String update = "UPDATE application a SET purchaser_type = 5 WHERE a.application_id = ? AND "
                + FilterManager.ELIGIBILITY;
        try (Connection conn = database.connect()) {
            TransactionManager.beginTransaction(conn);
            try {
                // Close the statement before commit so statement failures also roll back.
                try (PreparedStatement stmt = conn.prepareStatement(update)) {
                    for (int id : ids) {
                        stmt.setInt(1, id);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("An application is missing or no longer eligible; packaging cancelled.");
                        }
                    }
                }
                TransactionManager.commitTransaction(conn);
                return ids.size();
            } catch (SQLException | RuntimeException failure) {
                try {
                    TransactionManager.rollbackTransaction(conn);
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
            // This operation owns and closes its connection; do not reset autocommit after failure.
        }
    }
}
