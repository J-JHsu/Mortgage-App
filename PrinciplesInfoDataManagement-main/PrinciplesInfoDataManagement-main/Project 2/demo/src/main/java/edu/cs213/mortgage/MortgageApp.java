package edu.cs213.mortgage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import static java.util.Comparator.comparing;


public class MortgageApp {
    private final MortgageDAO mortgageDAO = new MortgageDAO();
    private final FilterManager filterManager = new FilterManager();
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;

    /**
     * Main Program Execution Loop
     */
    public void run() {
        while (running) {
            List<Filter> displayFilters = filterManager.getFilters();
            System.out.println("\nACTIVE FILTERS: ");
            if (displayFilters.size() == 0)
            {
                System.out.println("-\tNone");
            }
            displayFilters.sort(comparing(Filter::getChoice));
            StringBuilder display = new StringBuilder("");
            for (int i = 0; i < displayFilters.size(); i++) 
            {
                if (i == 0)
                {
                    if (i + 1 < displayFilters.size())
                    {
                        if (displayFilters.get(i+1).getChoice() == displayFilters.get(i).getChoice())
                        {
                            display.append("(").append(displayFilters.get(i).getName());
                        }
                        else
                        {
                            display.append(displayFilters.get(i).getName());
                        }
                    }
                    else
                    {
                
                        display.append(displayFilters.get(i).getName());
                    }
                }
                else
                {
                    if (displayFilters.get(i).getChoice() == displayFilters.get(i-1).getChoice())
                    {
                        if (i + 1 < displayFilters.size())
                        {
                            if (displayFilters.get(i+1).getChoice() == displayFilters.get(i).getChoice())
                            {
                                display.append(" OR ").append(displayFilters.get(i).getName());
                            }
                            else
                            {
                                display.append(" OR ").append(displayFilters.get(i).getName()).append(")");
                            }
                        }
                        else
                        {
                           
                            display.append(" OR ").append(displayFilters.get(i).getName()).append(")");
                        }
                    }
                    else
                    {
                        if (i + 1 < displayFilters.size())
                        {
                            if (displayFilters.get(i+1).getChoice() == displayFilters.get(i).getChoice())
                            {
                                display.append(" AND (").append(displayFilters.get(i).getName());
                            }
                            else
                            {
                                display.append(" AND ").append(displayFilters.get(i).getName());
                            }
                        }
                        else
                        {
                            
                            display.append(" AND ").append(displayFilters.get(i).getName());
                        }
                    }
                }
            } 
            System.out.println(display);
            System.out.println("\n--- Mortgage Backed Securities System ---");
            System.out.println("1. Add Filter");
            System.out.println("2. Delete Filter");
            System.out.println("3. View Matching Mortgages");
            System.out.println("4. Calculate and Package Mortgages");
            System.out.println("5. Add New Mortgage");
            System.out.println("6. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println("\n");
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1 -> addFilter();
                case 2 -> deleteFilter();
                case 3 -> viewMatchingMortgages();
                case 4 -> calculateAndPackage();
                case 5 -> addMortgage();
                case 6 -> running = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        System.out.println("Thank you for using the system!");
    }

    /**
     * Allows the user to define a filter for mortgage search.
     */
    private void addFilter() {
        System.out.println("Available Filters:");
        System.out.println("1. MSAMD ");
        System.out.println("2. Loan Type ");
        System.out.println("3. Loan Purpose ");
        System.out.println("4. Property Type ");
        System.out.println("5. Applicant Income Range");
        System.out.println("6. Owner Occupancy");


        System.out.print("Enter filter number: ");
        int filterChoice = scanner.nextInt();
        System.out.println("\n");
        scanner.nextLine();  // Consume newline

        String filterName, sqlCondition;

        switch (filterChoice) {
            case 1 -> {
                System.out.println("""
                        ACCEPTABLE MSAMDs:

                        1.  Allentown, Bethlehem, Easton - PA, NJ
                        2.  Atlantic City, Hammonton - NJ
                        3.  Camden - NJ
                        4.  Newark - NJ, PA
                        5.  New York, Jersey City, White Plains - NY, NJ
                        6.  35620
                        7.  Ocean City - NJ
                        8.  37980
                        9.  Trenton - NJ
                        10. Vineland, Bridgeton - NJ
                        11. Wilmington - DE, MD, NJ
                        """);
                System.out.print("Enter choice #: ");
                int choice = scanner.nextInt();
                System.out.println("\n");
                String msamd = "";
                int msamdVal = 0;
                switch (choice) 
                {
                    case 1 -> 
                    {
                        msamd = "Allentown, Bethlehem, Easton - PA, NJ";
                        msamdVal = 10900;
                    }
                    case 2 -> 
                    {
                        msamd = "Atlantic City, Hammonton - NJ";
                        msamdVal = 12100;
                    }
                    case 3 -> 
                    {
                        msamd = "Camden - NJ";
                        msamdVal = 15804;

                    }
                    case 4 -> 
                    {
                        msamd = "Newark - NJ, PA";
                        msamdVal = 35084;

                    }
                    case 5 -> 
                    {
                        msamd = "New York, Jersey City, White Plains - NY, NJ";
                        msamdVal = 35614;

                    }
                    case 6 -> 
                    {
                        msamdVal = 35620;
                    }
                    case 7 -> 
                    {
                        msamd = "Ocean City - NJ";
                        msamdVal = 36140;

                    }
                    case 8 -> 
                    {
                        msamdVal = 37980;
                    }
                    case 9 -> 
                    {
                        msamd = "Trenton - NJ";
                        msamdVal = 45940;

                    }
                    case 10 -> 
                    {
                        msamd = "Vineland, Bridgeton - NJ";
                        msamdVal = 47220;

                    }
                    case 11 -> 
                    {
                        msamd = "Wilmington - DE, MD, NJ";
                        msamdVal = 48864;

                    }    
                    default -> 
                    {
                        System.out.println("Invalid choice.");
                        return;    
                    }
                }
                if (choice == 6 || choice == 8)
                {
                    filterName = "MSAMD = " + msamdVal;
                    sqlCondition = "a.msamd = " + msamdVal;
                }
                else
                {
                    filterName = "MSAMD = " + msamd;
                    sqlCondition = "a.msamd = " + msamdVal;
                }
            }

            case 2 -> {
                System.out.println("""
                        ACCEPTABLE LOAN TYPES:

                        1.  Conventional
                        2.  FHA-insured
                        3.  VA-guaranteed
                        4.  FSA/RHS-guaranteed
                        """);
                System.out.print("Enter choice #: ");
                int choice = scanner.nextInt();
                System.out.println("\n");
                String loanType = "";
                switch (choice) 
                {
                    case 1 -> 
                    {
                        loanType = "Conventional";
                    }
                    case 2 -> 
                    {
                        loanType = "FHA-insured";

                    }
                    case 3 -> 
                    {
                        loanType = "VA-guaranteed";

                    }
                    case 4 -> 
                    {
                        loanType = "FSA/RHS-guaranteed";
                    }
                    default -> 
                    {
                        System.out.println("Invalid choice.");
                        return;    
                    }
                }
                filterName = "Loan Type = " + loanType;
                sqlCondition = "a.loan_type = " + choice;
            }
            case 3 -> {
                System.out.println("""
                        ACCEPTABLE LOAN PURPOSES:

                        1.  Home purchase
                        2.  Home improvement
                        3.  Refinancing
                        """);
                System.out.print("Enter choice #: ");
                int choice = scanner.nextInt();
                System.out.println("\n");
                String loanPurpose = "";
                switch (choice) 
                {
                    case 1 -> 
                    {
                        loanPurpose = "Home purchase";
                    }
                    case 2 -> 
                    {
                        loanPurpose = "Home improvement";

                    }
                    case 3 -> 
                    {
                        loanPurpose = "Refinancing";

                    }
                    default -> 
                    {
                        System.out.println("Invalid choice.");
                        return;    
                    }
                }
                filterName = "Loan Purpose = " + loanPurpose;
                sqlCondition = "a.loan_purpose = " + choice;
            }
            case 4 -> {
                System.out.println("""
                        ACCEPTABLE PROPERTY TYPES:

                        1.  One-to-four family dwelling (other than manufactured housing)
                        2.  Manufactured housing
                        3.  Multifamily dwelling
                        """);
                System.out.print("Enter choice #: ");
                int choice = scanner.nextInt();
                System.out.println("\n");
                String propertyType = "";
                switch (choice) 
                {
                    case 1 -> 
                    {
                        propertyType = "One-to-four family dwelling (other than manufactured housing)";
                    }
                    case 2 -> 
                    {
                        propertyType = "Manufactured housing";

                    }
                    case 3 -> 
                    {
                        propertyType = "Multifamily dwelling";

                    }
                    default -> 
                    {
                        System.out.println("Invalid choice.");
                        return;    
                    }
                }
                filterName = "Property Type = " + propertyType;
                sqlCondition = "a.property_type = " + choice;
            }
            case 5 -> {
                System.out.print("Enter Minimum Applicant Income: ");
                int minIncome = scanner.nextInt();
                System.out.print("Enter Maximum Applicant Income: ");
                int maxIncome = scanner.nextInt();
                filterName = "Applicant Income Range: " + minIncome + " - " + maxIncome;
                sqlCondition = "(a.applicant_income_000s BETWEEN " + minIncome + " AND " + maxIncome + ")";
            }
            case 6 -> {
                System.out.println("""
                        ACCEPTABLE OWNER OCCUPANCIES:

                        1.  Owner-occupied as a principal dwelling
                        2.  Not owner-occupied as a principal dwelling
                        3.  Not applicable
                        """);
                System.out.print("Enter choice #: ");
                int choice = scanner.nextInt();
                System.out.println("\n");
                String ownerOccupancy = "";
                switch (choice) 
                {
                    case 1 -> 
                    {
                        ownerOccupancy = "Owner-occupied as a principal dwelling";
                    }
                    case 2 -> 
                    {
                        ownerOccupancy = "Not owner-occupied as a principal dwelling";

                    }
                    case 3 -> 
                    {
                        ownerOccupancy = "Not applicable";

                    }
                    default -> 
                    {
                        System.out.println("Invalid choice.");
                        return;    
                    }
                }
                filterName = "Owner Occupancy = " + ownerOccupancy;
                sqlCondition = "a.owner_occupancy = " + choice;
            }
            default -> {
                System.out.println("Invalid filter choice.");
                return;
            }
        }

        filterManager.addFilter(new Filter(filterName, sqlCondition, filterChoice));
        System.out.println("Filter added: " + filterName);
        System.out.println("\n");
    }

    /**
     * Deletes an existing filter or clears all.
     */
    private void deleteFilter() {
        List<Filter> filters = filterManager.getFilters();
        if (filters.isEmpty()) {
            System.out.println("No active filters.");
            return;
        }

        System.out.println("Current Filters:");
        for (int i = 0; i < filters.size(); i++) {
            System.out.println((i + 1) + ". " + filters.get(i).getName());
        }

        System.out.print("Enter filter number to delete or 0 to clear all: ");
        int choice = scanner.nextInt();

        if (choice == 0) {
            filterManager.clearFilters();
            System.out.println("All filters cleared.");
        } else if (choice > 0 && choice <= filters.size()) {
            filterManager.removeFilter(filters.get(choice - 1));
            System.out.println("Filter removed.");
        } else {
            System.out.println("Invalid choice.");
        }
    }

    /**
     * Displays matching mortgage records based on filters.
     */
    private void viewMatchingMortgages() {
        List<Mortgage> mortgages = mortgageDAO.getFilteredMortgages(filterManager.getFilters());

        if (mortgages.isEmpty()) {
            System.out.println("No matching mortgages found.");
            return;
        }

        double totalAmount = mortgages.stream().mapToDouble(Mortgage::getLoanAmount).sum();

        System.out.println("Number of matching mortgages: " + mortgages.size());
        System.out.printf("Total Loan Amount: $%.2f%n", totalAmount * 1000);  // Amount in dollars
    }

    /**
     * Calculates the rate and packages mortgages if accepted.
     */
    private void calculateAndPackage() {
        List<Mortgage> mortgages = mortgageDAO.getFilteredMortgages(filterManager.getFilters());

        if (mortgages.isEmpty()) {
            System.out.println("No mortgages found.");
            return;
        }

        double expectedRate = MortgageCalculator.calculateRate(mortgages);
        System.out.printf("Calculated Expected Rate: %.2f%%%n", expectedRate);

        System.out.print("Accept this rate and package loans? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(response)) {
            try (Connection conn = DatabaseCon.connect()) {
                String updateQuery = "UPDATE application SET purchaser_type = 5 WHERE application_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    for (Mortgage mortgage : mortgages) {
                        stmt.setInt(1, mortgage.getApplicationId());
                        stmt.executeUpdate();
                    }
                    System.out.println("Mortgages successfully packaged!");
                    running = false;
                }
            } catch (SQLException e) {
                System.out.println("Error packaging mortgages.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Rate rejected. Returning to the main menu.");
        }
    }

    private void addMortgage() {
        System.out.println("\n--- Add New Mortgage ---");
    
        try (Connection conn = DatabaseCon.connect()) {
            // Step 1: Input income
            System.out.print("Enter Applicant Income (in thousands): ");
            double income = Double.parseDouble(scanner.nextLine());
    
            // Step 2: Input loan amount
            System.out.print("Enter Loan Amount (in thousands): ");
            double loanAmount = Double.parseDouble(scanner.nextLine());
    
            // Step 3: Input MSAMD
            System.out.print("Enter MSAMD (Metropolitan Statistical Area/Metropolitan Division): ");
            int msamd = Integer.parseInt(scanner.nextLine());
    
            // Step 4: Select Applicant Sex
            System.out.print("Enter Applicant Sex (1 = Male, 2 = Female, 3 = Information Not Provided, 4 = Not Applicable): ");
            int applicantSex = Integer.parseInt(scanner.nextLine());
    
            // Step 5: Select Loan Type
            System.out.print("Enter Loan Type (1 = Conventional, 2 = FHA, 3 = VA, 4 = USDA-RHS): ");
            int loanType = Integer.parseInt(scanner.nextLine());
    
            // Step 6: Select Ethnicity
            System.out.print("Enter Applicant Ethnicity (1 = Hispanic or Latino, 2 = Not Hispanic or Latino, 3 = Information Not Provided, 4 = Not Applicable): ");
            int ethnicity = Integer.parseInt(scanner.nextLine());
    
            // Insert the mortgage into the database with default values for all required fields
            String insertQuery = "INSERT INTO application (as_of_year, respondent_id, agency_code, loan_type, property_type, loan_purpose, " +
                                 "loan_amount_000s, preapproval, action_taken, msamd, location_id, applicant_ethnicity, co_applicant_ethnicity, " +
                                 "applicant_sex, co_applicant_sex, applicant_income_000s, purchaser_type, denial_reason_1, denial_reason_2, " +
                                 "denial_reason_3, rate_spread, hoepa_status, lien_status, edit_status, sequence_number, application_date_indicator) " +
                                 "VALUES (2024, 'UNKNOWN', 1, ?, 1, 1, ?, 0, 1, ?, " +
                                 "(SELECT location_id FROM location WHERE msamd = ? LIMIT 1), ?, 0, ?, 0, ?, 0, 0, 0, 0, 0.0, 0, 0, 0, DEFAULT, 0)";
    
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, loanType);
                stmt.setDouble(2, loanAmount);
                stmt.setInt(3, msamd);
                stmt.setInt(4, msamd);
                stmt.setInt(5, ethnicity);
                stmt.setInt(6, applicantSex);
                stmt.setDouble(7, income);
    
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("New mortgage successfully added!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding new mortgage.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numbers where required.");
        }
    }
}
