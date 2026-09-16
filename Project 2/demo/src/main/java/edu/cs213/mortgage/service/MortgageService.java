package edu.cs213.mortgage.service;

import edu.cs213.mortgage.model.Filter;
import edu.cs213.mortgage.model.Mortgage;
import edu.cs213.mortgage.repository.MortgageDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.OptionalDouble;
import org.springframework.stereotype.Service;

@Service
public class MortgageService {
    private final MortgageDAO mortgageDAO;

    public MortgageService(MortgageDAO mortgageDAO) {
        this.mortgageDAO = mortgageDAO;
    }

    public List<Mortgage> search(List<Filter> filters) throws SQLException {
        return mortgageDAO.getFilteredMortgages(List.copyOf(filters));
    }

    public Portfolio review(List<Filter> filters) throws SQLException {
        List<Mortgage> mortgages = search(filters);
        return new Portfolio(mortgages, mortgages.stream().map(Mortgage::getLoanAmount)
                .filter(java.util.Objects::nonNull).mapToDouble(Integer::doubleValue).sum(),
                MortgageCalculator.calculateRate(mortgages));
    }

    /** Call only after confirmation of the portfolio returned by review. */
    public int packageMortgages(Portfolio portfolio) throws SQLException {
        if (portfolio.rate().isEmpty() || MortgageCalculator.calculateRate(portfolio.mortgages()).isEmpty()) {
            throw new IllegalArgumentException("No calculable eligible portfolio to package.");
        }
        return mortgageDAO.packageMortgages(
                portfolio.mortgages().stream().map(Mortgage::getApplicationId).toList());
    }

    public record Portfolio(List<Mortgage> mortgages, double totalLoanAmountThousands, OptionalDouble rate) {
        public Portfolio {
            mortgages = List.copyOf(mortgages);
        }

        public int loanCount() {
            return mortgages.size();
        }
    }
}
