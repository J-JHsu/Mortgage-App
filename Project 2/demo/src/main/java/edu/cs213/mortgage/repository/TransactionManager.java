package edu.cs213.mortgage.repository;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public static void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
    }

    public static void rollbackTransaction(Connection conn) throws SQLException {
        conn.rollback();
    }
}
