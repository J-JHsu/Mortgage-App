package edu.cs213.mortgage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseCon {
    private static final String URL = System.getenv("DB_URL");       // Database URL
    private static final String USER = System.getenv("DB_USER");     // Database User
    private static final String PASSWORD = System.getenv("DB_PASSWORD");  // Database Password

    /**
     * Connects to the PostgreSQL database using environment variables.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully.");
            return conn;
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            throw e;
        }
    }
}
