package edu.cs213.mortgage;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public static void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public static void rollbackTransaction(Connection conn) throws SQLException {
        conn.rollback();
        conn.setAutoCommit(true);
    }
}
