package edu.cs213.mortgage.repository;

import edu.cs213.mortgage.config.DatabaseCon;
import edu.cs213.mortgage.model.Filter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MortgageDAOTest {
    private DatabaseCon database;
    private Connection connection;
    private PreparedStatement statement;
    private MortgageDAO repository;

    @BeforeEach
    void setup() throws SQLException {
        database = mock(DatabaseCon.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(database.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        repository = new MortgageDAO(database);
    }

    @Test
    void searchBindsValuesAndPreservesUnavailableSpread() throws SQLException {
        ResultSet rows = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(rows.wasNull()).thenReturn(true);
        var result = repository.getFilteredMortgages(List.of(new Filter(Filter.Type.MSAMD, 35084)));
        verify(statement).setInt(1, 35084);
        verify(connection).prepareStatement(argThat(sql -> sql.contains("l.msamd = ?") && !sql.contains("35084")));
        assertNull(result.get(0).getRateSpread());
        assertNull(result.get(0).getLoanAmount());
        assertNull(result.get(0).getApplicantIncome());
        assertNull(result.get(0).getMsamd());
        verify(rows).close();
        verify(connection).close();
    }

    @Test
    void searchPropagatesDatabaseFailure() throws SQLException {
        SQLException failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);
        assertSame(failure, assertThrows(SQLException.class, () -> repository.getFilteredMortgages(List.of())));
    }

    @Test
    void commitsOnlyAfterAllRowsAreUpdated() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        assertEquals(2, repository.packageMortgages(List.of(10, 20)));
        var order = inOrder(connection, statement);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).prepareStatement(contains("AND a.action_taken = 1"));
        order.verify(statement).setInt(1, 10);
        order.verify(statement).executeUpdate();
        order.verify(statement).setInt(1, 20);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).commit();
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    void laterUpdateFailureRollsBackEarlierUpdates() throws SQLException {
        SQLException failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenReturn(1).thenThrow(failure);
        assertSame(failure, assertThrows(SQLException.class, () -> repository.packageMortgages(List.of(10, 20))));
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void missingOrIneligibleRowRollsBackSet() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1, 0);
        assertThrows(SQLException.class, () -> repository.packageMortgages(List.of(10, 20)));
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void commitFailureAttemptsRollbackAndPreservesBothFailures() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        SQLException commitFailure = new SQLException("commit failed");
        SQLException rollbackFailure = new SQLException("rollback failed");
        doThrow(commitFailure).when(connection).commit();
        doThrow(rollbackFailure).when(connection).rollback();
        SQLException failure = assertThrows(SQLException.class, () -> repository.packageMortgages(List.of(10)));
        assertSame(commitFailure, failure);
        assertArrayEquals(new Throwable[] {rollbackFailure}, failure.getSuppressed());
        verify(connection).close();
    }

    @Test
    void rejectsEmptyDuplicateOrInvalidIdsBeforeConnecting() {
        for (List<Integer> ids : List.of(List.<Integer>of(), List.of(1, 1), List.of(0))) {
            assertThrows(IllegalArgumentException.class, () -> repository.packageMortgages(ids));
        }
        verifyNoInteractions(database);
    }
}
