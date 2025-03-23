package edu.cs213.mortgage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MortgageDAO {

    /**
     * Retrieves filtered mortgages from the database.
     * Uses dynamic query building and prepared statements.
     * 
     * @param filters Active filters provided by FilterManager.
     * @return List of matching Mortgage records.
     */
    public List<Mortgage> getFilteredMortgages(List<Filter> filters) {
        List<Mortgage> mortgages = new ArrayList<>();

        // Base SQL Query
        String query = """
            SELECT a.application_id, a.respondent_id, a.loan_type, a.loan_amount_000s, 
                    a.action_taken, a.msamd, 
                    a.applicant_income_000s, a.rate_spread, a.purchaser_type, 
                   a.lien_status, a.property_type, a.loan_purpose, a.owner_occupancy
            FROM application a
        """;
     
        // Use FilterManager for Query Building
        FilterManager filterManager = new FilterManager(filters);
        String whereClause = filterManager.buildWhereClause();  
        
        try (Connection conn = DatabaseCon.connect();
             var stmt = conn.createStatement()) {

            // Execute Query and Map Results to Mortgage Objects
            ResultSet rs = stmt.executeQuery(query + " " + whereClause);
            while (rs.next()) {
                Mortgage mortgage = new Mortgage(
                    rs.getInt("application_id"),
                    rs.getString("respondent_id"),
                    rs.getInt("loan_type"),
                    rs.getInt("loan_amount_000s"),
                    rs.getInt("action_taken"),
                    rs.getInt("msamd"),
                    rs.getInt("applicant_income_000s"),
                    rs.getDouble("rate_spread"),
                    rs.getInt("purchaser_type"),
                    rs.getInt("lien_status"),
                    rs.getInt("property_type"),
                    rs.getInt("loan_purpose"),
                    rs.getInt("owner_occupancy")
                );
                mortgages.add(mortgage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mortgages;
    }
}
