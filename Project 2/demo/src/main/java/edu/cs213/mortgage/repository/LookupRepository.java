package edu.cs213.mortgage.repository;

import edu.cs213.mortgage.config.DatabaseCon;
import edu.cs213.mortgage.model.MortgageApi.LookupOption;
import java.sql.SQLException;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class LookupRepository {
    private final DatabaseCon database;
    public LookupRepository(DatabaseCon database) { this.database = database; }

    public Map<String, List<LookupOption>> read() throws SQLException {
        Map<String, List<LookupOption>> result = new HashMap<>();
        // Table/column identifiers are exclusively this fixed whitelist.
        try (var connection = database.connect()) {
            for (String table : List.of("loan_type", "loan_purpose", "property_type",
                    "owner_occupancy", "county", "msamd", "action_type", "purchaser_type", "lien_status")) {
                String code = table.equals("county") ? "county_code" : "code";
                List<LookupOption> options = new ArrayList<>();
                try (var statement = connection.prepareStatement(
                        "SELECT " + code + ", name FROM " + table + " ORDER BY " + code);
                     var rows = statement.executeQuery()) {
                    while (rows.next()) options.add(new LookupOption(rows.getInt(1), rows.getString(2)));
                }
                result.put(table, List.copyOf(options));
            }
        }
        return Map.copyOf(result);
    }
}
