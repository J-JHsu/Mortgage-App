\set ON_ERROR_STOP on
-- Run only in a dedicated empty development database. No DROP/overwrite behavior.
BEGIN;
CREATE TABLE loan_type (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE loan_purpose (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE property_type (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE owner_occupancy (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE action_type (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE purchaser_type (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE lien_status (
    code smallint PRIMARY KEY CHECK (code >= 0),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE county (
    county_code smallint PRIMARY KEY CHECK (county_code BETWEEN 1 AND 999),
    state_code smallint NOT NULL CHECK (state_code = 34),
    name text NOT NULL CHECK (btrim(name) <> '')
);
CREATE TABLE msamd (
    code integer PRIMARY KEY CHECK (code > 0),
    name text CHECK (name IS NULL OR btrim(name) <> '')
);
CREATE TABLE location (
    location_id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    county_code smallint REFERENCES county(county_code),
    msamd integer REFERENCES msamd(code),
    census_tract_number text CHECK (census_tract_number ~ '^[0-9]{4}[.][0-9]{2}$'),
    tract_to_msamd_income numeric CHECK (tract_to_msamd_income >= 0 AND tract_to_msamd_income < 'Infinity'::numeric),
    UNIQUE NULLS NOT DISTINCT (county_code, census_tract_number, msamd)
);
CREATE TABLE application (
    application_id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_row bigint NOT NULL UNIQUE CHECK (source_row > 0),
    as_of_year smallint NOT NULL CHECK (as_of_year = 2017),
    respondent_id text NOT NULL CHECK (btrim(respondent_id) <> ''),
    loan_amount_000s integer CHECK (loan_amount_000s > 0),
    applicant_income_000s integer CHECK (applicant_income_000s > 0),
    rate_spread numeric CHECK (rate_spread > 0 AND rate_spread < 'Infinity'::numeric),
    loan_type smallint NOT NULL REFERENCES loan_type(code),
    loan_purpose smallint NOT NULL REFERENCES loan_purpose(code),
    property_type smallint NOT NULL REFERENCES property_type(code),
    owner_occupancy smallint NOT NULL REFERENCES owner_occupancy(code),
    action_taken smallint NOT NULL REFERENCES action_type(code),
    purchaser_type smallint NOT NULL REFERENCES purchaser_type(code),
    lien_status smallint NOT NULL REFERENCES lien_status(code),
    location_id integer NOT NULL REFERENCES location(location_id)
);
COMMENT ON COLUMN application.source_row IS '1-based data record ordinal in the inspected CSV; not an HMDA identifier';
COMMENT ON COLUMN application.loan_amount_000s IS 'Thousands of dollars, unchanged from source';
COMMENT ON COLUMN application.applicant_income_000s IS 'Thousands of dollars; unavailable remains NULL';
COMMENT ON COLUMN application.rate_spread IS 'Percentage points; unavailable remains NULL';
COMMENT ON COLUMN location.tract_to_msamd_income IS 'Source tract-to-MSA/MD income percentage, without rounding';
COMMIT;
