package edu.cs213.mortgage.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCon {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseCon(@Value("${DB_URL:}") String url,
                       @Value("${DB_USERNAME:}") String username,
                       @Value("${DB_PASSWORD:}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection connect() throws SQLException {
        // Phase 2 can start without a database; operations still require configuration.
        if (url.isBlank() || username.isBlank() || password.isBlank()) {
            throw new SQLException("Set DB_URL, DB_USERNAME and DB_PASSWORD before using the database.");
        }
        return DriverManager.getConnection(url, username, password);
    }
}
