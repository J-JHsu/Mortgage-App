\set ON_ERROR_STOP on
-- Read-only summaries plus rollback-only integrity checks.
SELECT count(*) AS applications,
       count(*) FILTER (WHERE action_taken = 1 AND purchaser_type IN (0,1,2,3,4,8)) AS eligible,
       count(*) FILTER (WHERE rate_spread IS NULL) AS missing_spread,
       count(*) FILTER (WHERE loan_amount_000s IS NULL) AS missing_amount,
       count(*) FILTER (WHERE applicant_income_000s IS NULL) AS missing_income
FROM application;
SELECT count(*) AS locations FROM location;
SELECT p.code, p.name, count(a.application_id) AS applications
FROM purchaser_type p LEFT JOIN application a ON a.purchaser_type = p.code
GROUP BY p.code, p.name ORDER BY p.code;
SELECT a.application_id, a.source_row, a.respondent_id, a.loan_amount_000s,
       a.applicant_income_000s, a.rate_spread, a.lien_status, a.action_taken,
       a.purchaser_type, l.msamd, c.name AS county, l.census_tract_number,
       l.tract_to_msamd_income
FROM application a JOIN location l USING (location_id)
LEFT JOIN county c USING (county_code) ORDER BY a.source_row LIMIT 5;

-- All requested filter concepts are queryable without a REST API.
SELECT count(*) AS combined_filter_matches
FROM application a JOIN location l USING (location_id)
WHERE a.action_taken = 1 AND a.purchaser_type IN (0,1,2,3,4,8)
  AND l.county_code IN (13,25) AND a.loan_type = 1
  AND l.msamd IN (35084,35614) AND a.loan_purpose IN (1,3)
  AND a.property_type = 1 AND a.owner_occupancy = 1
  AND a.applicant_income_000s::numeric / NULLIF(a.loan_amount_000s,0) >= 0.5
  AND l.tract_to_msamd_income >= 100;
SELECT count(*) AS eligible, sum(loan_amount_000s) AS total_thousands,
       avg(loan_amount_000s) AS average_thousands
FROM application WHERE action_taken = 1 AND purchaser_type IN (0,1,2,3,4,8);
SELECT conname, convalidated FROM pg_constraint
WHERE conrelid = 'application'::regclass AND contype = 'f';
SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE schemaname = 'public' AND tablename IN ('application','location')
ORDER BY tablename, indexname;

BEGIN;
DO $$
DECLARE selected_id integer;
BEGIN
    SELECT min(application_id) INTO STRICT selected_id FROM application
    WHERE action_taken = 1 AND purchaser_type IN (0,1,2,3,4,8);
    IF selected_id IS NULL THEN RAISE EXCEPTION 'No eligible row available for validation'; END IF;
    BEGIN
        UPDATE application SET loan_type = -1 WHERE application_id = selected_id;
        RAISE EXCEPTION 'Invalid lookup reference was accepted';
    EXCEPTION WHEN foreign_key_violation THEN NULL;
    END;
    BEGIN
        UPDATE application SET loan_amount_000s = 0 WHERE application_id = selected_id;
        RAISE EXCEPTION 'Zero loan amount was accepted';
    EXCEPTION WHEN check_violation THEN NULL;
    END;
    UPDATE application SET purchaser_type = 5 WHERE application_id = selected_id;
    IF EXISTS (SELECT FROM application WHERE application_id = selected_id
               AND action_taken = 1 AND purchaser_type IN (0,1,2,3,4,8)) THEN
        RAISE EXCEPTION 'Packaged loan remained eligible';
    END IF;
    RAISE NOTICE 'Foreign key, amount check and purchaser-5 eligibility checks passed; rolling back.';
END $$;
ROLLBACK;
