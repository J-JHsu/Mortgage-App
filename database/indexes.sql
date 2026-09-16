-- Eligibility predicate matches the existing JDBC search and packaging rules.
CREATE INDEX application_eligible_location_idx ON application (location_id)
    WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8);
CREATE INDEX application_eligible_loan_type_idx ON application (loan_type)
    WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8);
-- The location natural-key unique index already starts with county_code.
CREATE INDEX location_msamd_idx ON location (msamd);
