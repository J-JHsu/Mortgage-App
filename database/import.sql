\set ON_ERROR_STOP on
\if :{?csv_path}
\else
\echo 'Pass -v csv_path=C:/absolute/path/to/hmda_2017_nationwide_all-records_labels.csv'
\quit 1
\endif
BEGIN;
LOCK TABLE application IN EXCLUSIVE MODE;
DO $$
BEGIN
    IF EXISTS (SELECT FROM application) THEN
        RAISE EXCEPTION 'Import requires an empty application table; existing data will not be overwritten.';
    END IF;
END $$;

-- COPY needs the complete physical header, but WHERE stores only 2017 NJ rows.
-- The source-row default is evaluated before WHERE, preserving file ordinals.
CREATE TEMP TABLE hmda_csv (
    source_row bigint GENERATED ALWAYS AS IDENTITY,
    as_of_year text,
    respondent_id text,
    agency_name text,
    agency_abbr text,
    agency_code text,
    loan_type_name text,
    loan_type text,
    property_type_name text,
    property_type text,
    loan_purpose_name text,
    loan_purpose text,
    owner_occupancy_name text,
    owner_occupancy text,
    loan_amount_000s text,
    preapproval_name text,
    preapproval text,
    action_taken_name text,
    action_taken text,
    msamd_name text,
    msamd text,
    state_name text,
    state_abbr text,
    state_code text,
    county_name text,
    county_code text,
    census_tract_number text,
    applicant_ethnicity_name text,
    applicant_ethnicity text,
    co_applicant_ethnicity_name text,
    co_applicant_ethnicity text,
    applicant_race_name_1 text,
    applicant_race_1 text,
    applicant_race_name_2 text,
    applicant_race_2 text,
    applicant_race_name_3 text,
    applicant_race_3 text,
    applicant_race_name_4 text,
    applicant_race_4 text,
    applicant_race_name_5 text,
    applicant_race_5 text,
    co_applicant_race_name_1 text,
    co_applicant_race_1 text,
    co_applicant_race_name_2 text,
    co_applicant_race_2 text,
    co_applicant_race_name_3 text,
    co_applicant_race_3 text,
    co_applicant_race_name_4 text,
    co_applicant_race_4 text,
    co_applicant_race_name_5 text,
    co_applicant_race_5 text,
    applicant_sex_name text,
    applicant_sex text,
    co_applicant_sex_name text,
    co_applicant_sex text,
    applicant_income_000s text,
    purchaser_type_name text,
    purchaser_type text,
    denial_reason_name_1 text,
    denial_reason_1 text,
    denial_reason_name_2 text,
    denial_reason_2 text,
    denial_reason_name_3 text,
    denial_reason_3 text,
    rate_spread text,
    hoepa_status_name text,
    hoepa_status text,
    lien_status_name text,
    lien_status text,
    edit_status_name text,
    edit_status text,
    sequence_number text,
    population text,
    minority_population text,
    hud_median_family_income text,
    tract_to_msamd_income text,
    number_of_owner_occupied_units text,
    number_of_1_to_4_family_units text,
    application_date_indicator text
) ON COMMIT DROP;
COPY hmda_csv (as_of_year,respondent_id,agency_name,agency_abbr,agency_code,loan_type_name,loan_type,property_type_name,property_type,loan_purpose_name,loan_purpose,owner_occupancy_name,owner_occupancy,loan_amount_000s,preapproval_name,preapproval,action_taken_name,action_taken,msamd_name,msamd,state_name,state_abbr,state_code,county_name,county_code,census_tract_number,applicant_ethnicity_name,applicant_ethnicity,co_applicant_ethnicity_name,co_applicant_ethnicity,applicant_race_name_1,applicant_race_1,applicant_race_name_2,applicant_race_2,applicant_race_name_3,applicant_race_3,applicant_race_name_4,applicant_race_4,applicant_race_name_5,applicant_race_5,co_applicant_race_name_1,co_applicant_race_1,co_applicant_race_name_2,co_applicant_race_2,co_applicant_race_name_3,co_applicant_race_3,co_applicant_race_name_4,co_applicant_race_4,co_applicant_race_name_5,co_applicant_race_5,applicant_sex_name,applicant_sex,co_applicant_sex_name,co_applicant_sex,applicant_income_000s,purchaser_type_name,purchaser_type,denial_reason_name_1,denial_reason_1,denial_reason_name_2,denial_reason_2,denial_reason_name_3,denial_reason_3,rate_spread,hoepa_status_name,hoepa_status,lien_status_name,lien_status,edit_status_name,edit_status,sequence_number,population,minority_population,hud_median_family_income,tract_to_msamd_income,number_of_owner_occupied_units,number_of_1_to_4_family_units,application_date_indicator)
FROM :'csv_path'
WITH (FORMAT csv, HEADER MATCH, ENCODING 'UTF8', FORCE_NULL (as_of_year,respondent_id,agency_name,agency_abbr,agency_code,loan_type_name,loan_type,property_type_name,property_type,loan_purpose_name,loan_purpose,owner_occupancy_name,owner_occupancy,loan_amount_000s,preapproval_name,preapproval,action_taken_name,action_taken,msamd_name,msamd,state_name,state_abbr,state_code,county_name,county_code,census_tract_number,applicant_ethnicity_name,applicant_ethnicity,co_applicant_ethnicity_name,co_applicant_ethnicity,applicant_race_name_1,applicant_race_1,applicant_race_name_2,applicant_race_2,applicant_race_name_3,applicant_race_3,applicant_race_name_4,applicant_race_4,applicant_race_name_5,applicant_race_5,co_applicant_race_name_1,co_applicant_race_1,co_applicant_race_name_2,co_applicant_race_2,co_applicant_race_name_3,co_applicant_race_3,co_applicant_race_name_4,co_applicant_race_4,co_applicant_race_name_5,co_applicant_race_5,applicant_sex_name,applicant_sex,co_applicant_sex_name,co_applicant_sex,applicant_income_000s,purchaser_type_name,purchaser_type,denial_reason_name_1,denial_reason_1,denial_reason_name_2,denial_reason_2,denial_reason_name_3,denial_reason_3,rate_spread,hoepa_status_name,hoepa_status,lien_status_name,lien_status,edit_status_name,edit_status,sequence_number,population,minority_population,hud_median_family_income,tract_to_msamd_income,number_of_owner_occupied_units,number_of_1_to_4_family_units,application_date_indicator))
WHERE as_of_year = '2017' AND state_code = '34';

CREATE TEMP TABLE nj ON COMMIT DROP AS
SELECT source_row,
    NULLIF(btrim(as_of_year), '')::smallint AS as_of_year,
    NULLIF(btrim(state_code), '')::smallint AS state_code,
    NULLIF(btrim(respondent_id), '') AS respondent_id,
    NULLIF(btrim(loan_amount_000s), '')::integer AS loan_amount_000s,
    NULLIF(btrim(applicant_income_000s), '')::integer AS applicant_income_000s,
    NULLIF(btrim(rate_spread), '')::numeric AS rate_spread,
    NULLIF(btrim(loan_type), '')::smallint AS loan_type,
    NULLIF(btrim(loan_type_name), '') AS loan_type_name,
    NULLIF(btrim(loan_purpose), '')::smallint AS loan_purpose,
    NULLIF(btrim(loan_purpose_name), '') AS loan_purpose_name,
    NULLIF(btrim(property_type), '')::smallint AS property_type,
    NULLIF(btrim(property_type_name), '') AS property_type_name,
    NULLIF(btrim(owner_occupancy), '')::smallint AS owner_occupancy,
    NULLIF(btrim(owner_occupancy_name), '') AS owner_occupancy_name,
    NULLIF(btrim(action_taken), '')::smallint AS action_taken,
    NULLIF(btrim(action_taken_name), '') AS action_taken_name,
    NULLIF(btrim(purchaser_type), '')::smallint AS purchaser_type,
    NULLIF(btrim(purchaser_type_name), '') AS purchaser_type_name,
    NULLIF(btrim(lien_status), '')::smallint AS lien_status,
    NULLIF(btrim(lien_status_name), '') AS lien_status_name,
    NULLIF(btrim(county_code), '')::smallint AS county_code,
    NULLIF(btrim(county_name), '') AS county_name,
    NULLIF(btrim(msamd), '')::integer AS msamd,
    NULLIF(btrim(msamd_name), '') AS msamd_name,
    NULLIF(btrim(census_tract_number), '') AS census_tract_number,
    NULLIF(btrim(tract_to_msamd_income), '')::numeric AS tract_to_msamd_income
FROM hmda_csv;
DROP TABLE hmda_csv;

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM nj) THEN
        RAISE EXCEPTION 'The source contains no 2017 NJ records.';
    END IF;
    IF EXISTS (
        SELECT FROM nj GROUP BY county_code, census_tract_number, msamd
        HAVING count(DISTINCT coalesce(tract_to_msamd_income::text, 'missing')) > 1
    ) THEN
        RAISE EXCEPTION 'Conflicting tract income for a location: inspect source before mapping.';
    END IF;
END $$;
INSERT INTO loan_type (code, name) SELECT DISTINCT loan_type, loan_type_name FROM nj;
INSERT INTO loan_purpose (code, name) SELECT DISTINCT loan_purpose, loan_purpose_name FROM nj;
INSERT INTO property_type (code, name) SELECT DISTINCT property_type, property_type_name FROM nj;
INSERT INTO owner_occupancy (code, name) SELECT DISTINCT owner_occupancy, owner_occupancy_name FROM nj;
INSERT INTO action_type (code, name) SELECT DISTINCT action_taken, action_taken_name FROM nj;
INSERT INTO purchaser_type (code, name) SELECT DISTINCT purchaser_type, purchaser_type_name FROM nj;
INSERT INTO lien_status (code, name) SELECT DISTINCT lien_status, lien_status_name FROM nj;
INSERT INTO county (county_code, state_code, name)
SELECT DISTINCT county_code, state_code, county_name FROM nj WHERE county_code IS NOT NULL;
INSERT INTO msamd (code, name)
SELECT DISTINCT msamd, msamd_name FROM nj WHERE msamd IS NOT NULL;
INSERT INTO location (county_code, census_tract_number, msamd, tract_to_msamd_income)
SELECT DISTINCT county_code, census_tract_number, msamd, tract_to_msamd_income FROM nj
ORDER BY county_code, census_tract_number, msamd;
INSERT INTO application (source_row, as_of_year, respondent_id, loan_amount_000s,
    applicant_income_000s, rate_spread, loan_type, loan_purpose, property_type,
    owner_occupancy, action_taken, purchaser_type, lien_status, location_id)
SELECT n.source_row, n.as_of_year, n.respondent_id, n.loan_amount_000s,
       n.applicant_income_000s, n.rate_spread, n.loan_type, n.loan_purpose, n.property_type,
       n.owner_occupancy, n.action_taken, n.purchaser_type, n.lien_status, l.location_id
FROM nj n JOIN location l
  ON l.county_code IS NOT DISTINCT FROM n.county_code
 AND l.census_tract_number IS NOT DISTINCT FROM n.census_tract_number
 AND l.msamd IS NOT DISTINCT FROM n.msamd
ORDER BY n.source_row;

-- Compare every imported financial/reference/geographic value before commit.
DO $$
BEGIN
    IF (SELECT count(*) FROM application) <> (SELECT count(*) FROM nj) THEN
        RAISE EXCEPTION 'Source/import row count mismatch';
    END IF;
    IF EXISTS (
        SELECT source_row, as_of_year, respondent_id, loan_amount_000s, applicant_income_000s,
               rate_spread, loan_type, loan_purpose, property_type, owner_occupancy,
               action_taken, purchaser_type, lien_status, county_code, census_tract_number,
               msamd, tract_to_msamd_income FROM nj
        EXCEPT
        SELECT a.source_row, a.as_of_year, a.respondent_id, a.loan_amount_000s, a.applicant_income_000s,
               a.rate_spread, a.loan_type, a.loan_purpose, a.property_type, a.owner_occupancy,
               a.action_taken, a.purchaser_type, a.lien_status, l.county_code, l.census_tract_number,
               l.msamd, l.tract_to_msamd_income
        FROM application a JOIN location l USING (location_id)
    ) THEN
        RAISE EXCEPTION 'Source/import field mismatch';
    END IF;
    IF NOT EXISTS (SELECT FROM purchaser_type WHERE code = 5 AND name = 'Private securitization')
       OR NOT EXISTS (SELECT FROM action_type WHERE code = 1 AND name = 'Loan originated')
       OR NOT EXISTS (SELECT FROM lien_status WHERE code = 1 AND name = 'Secured by a first lien')
       OR NOT EXISTS (SELECT FROM lien_status WHERE code = 2 AND name = 'Secured by a subordinate lien') THEN
        RAISE EXCEPTION 'Source labels contradict application business codes';
    END IF;
END $$;
\ir indexes.sql
SELECT count(*) AS imported_nj_records FROM application;
COMMIT;
ANALYZE application;
ANALYZE location;
