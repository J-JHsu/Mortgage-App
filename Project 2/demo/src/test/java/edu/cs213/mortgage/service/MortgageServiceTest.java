package edu.cs213.mortgage.service;

import edu.cs213.mortgage.repository.MortgageDAO;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MortgageServiceTest {
    @Test
    void missingLoanAmountRemainsUnquotableWithoutBreakingReview() throws SQLException {
        MortgageDAO repository = mock(MortgageDAO.class);
        when(repository.getFilteredMortgages(List.of())).thenReturn(List.of(
                MortgageCalculatorTest.mortgage(10, null, null, 1)));
        var review = new MortgageService(repository).review(List.of());
        assertEquals(1, review.loanCount());
        assertEquals(0, review.totalLoanAmountThousands());
        assertTrue(review.rate().isEmpty());
    }

    @Test
    void reviewsDatabaseRowsAndPackagesExactlyTheReviewedIds() throws SQLException {
        MortgageDAO repository = mock(MortgageDAO.class);
        when(repository.getFilteredMortgages(List.of())).thenReturn(List.of(
                MortgageCalculatorTest.mortgage(10, 100, 0.5, 1),
                MortgageCalculatorTest.mortgage(20, 300, 2.5, 1)));
        when(repository.packageMortgages(List.of(10, 20))).thenReturn(2);
        MortgageService service = new MortgageService(repository);
        var review = service.review(List.of());
        assertEquals(2, review.loanCount());
        assertEquals(400, review.totalLoanAmountThousands());
        assertEquals(4.33, review.rate().orElseThrow(), 0.000001);
        assertEquals(2, service.packageMortgages(review));
        verify(repository, times(1)).getFilteredMortgages(List.of());
    }

    @Test
    void noQuoteCannotBePackagedAndFailuresPropagate() throws SQLException {
        MortgageDAO repository = mock(MortgageDAO.class);
        when(repository.getFilteredMortgages(List.of())).thenReturn(List.of());
        MortgageService service = new MortgageService(repository);
        var review = service.review(List.of());
        assertTrue(review.rate().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.packageMortgages(review));
        verify(repository, never()).packageMortgages(anyList());
        SQLException failure = new SQLException("database unavailable");
        when(repository.getFilteredMortgages(List.of())).thenThrow(failure);
        assertSame(failure, assertThrows(SQLException.class, () -> service.review(List.of())));
    }
}
