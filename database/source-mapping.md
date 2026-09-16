# Inspected source and mapping

Inspected before schema design: `data/raw/hmda_2017_nationwide_all-records_labels.csv`,
11,237,068,086 bytes. This is a nationwide source; only records whose
`as_of_year = 2017` and `state_code = 34` are imported.
The exact 78-column header is in [source-header.txt](source-header.txt).

The source uses comma delimiters, double-quoted values, and an unquoted header.
The entire file parsed successfully with PostgreSQL CSV and UTF-8 decoding.
Empty quoted strings represent unavailable values. No other nonnumeric tokens
occurred in the NJ numeric fields inspected. The normalized import converts empty
strings to SQL NULL; it does not substitute zero.

## Exact source fields retained

| Source columns | Destination |
| --- | --- |
| as_of_year | application.as_of_year, constrained to 2017 |
| state_code | NJ selection; county.state_code, constrained to 34 |
| respondent_id | application.respondent_id (text, preserves leading zeros/hyphens) |
| loan_amount_000s | application.loan_amount_000s (nullable integer, thousands) |
| applicant_income_000s | application.applicant_income_000s (nullable integer, thousands) |
| rate_spread | application.rate_spread (nullable numeric, percentage points) |
| loan_type, loan_type_name | loan_type.code/name; application.loan_type foreign key |
| loan_purpose, loan_purpose_name | loan_purpose.code/name; application.loan_purpose foreign key |
| property_type, property_type_name | property_type.code/name; application.property_type foreign key |
| owner_occupancy, owner_occupancy_name | owner_occupancy.code/name; application.owner_occupancy foreign key |
| action_taken, action_taken_name | action_type.code/name; application.action_taken foreign key |
| purchaser_type, purchaser_type_name | purchaser_type.code/name; application.purchaser_type foreign key |
| lien_status, lien_status_name | lien_status.code/name; application.lien_status foreign key |
| county_code, county_name | county.county_code/name; location.county_code foreign key |
| msamd, msamd_name | msamd.code/name; location.msamd foreign key |
| census_tract_number | location.census_tract_number (text, preserves 0122.00) |
| tract_to_msamd_income | location.tract_to_msamd_income (numeric, preserves source precision) |

The income/loan ratio is derived as
`applicant_income_000s::numeric / NULLIF(loan_amount_000s, 0)`; it is not another
source field or stored redundant column. Application IDs are generated locally.
Source record ordinal is retained for reproducible source comparisons; it is not
an original HMDA application identifier. No uniqueness is assumed for respondent_id
or for financial-field combinations.

## Observed source facts

- Nationwide records: 14,285,496; NJ records: 349,563; eligible: 123,036.
- Eligibility remains action 1 and purchaser in (0, 1, 2, 3, 4, 8).
- Purchaser 5 is Private securitization (1,096 source NJ records).
- Lien 1 is Secured by a first lien; lien 2 is Secured by a subordinate lien.
  The existing code-2 rate assumption is preserved; the source label is not
  changed to imply only second liens.
- NJ loan amounts: 1–260,000 thousands; 154 unavailable.
- NJ applicant incomes: 1–7,073,045 thousands; 50,515 unavailable.
- Rate spreads: 1.50–14.00 where populated; 340,747 unavailable.
- County codes: 21, with 676 missing county values.
- MSAMD codes: 11, with 725 missing values. Codes 35620 and 37980 have empty
  source labels; their lookup names remain NULL rather than invented.
- Census tract and tract income: each missing in 697 rows.
- Tract income ranges from 0 to 346.989990234375. Source floating-point-looking
  decimal strings are retained with PostgreSQL numeric rather than rounded.
- 2,135 distinct (county, tract, MSAMD) locations. No conflicting tract-income
  values were observed within those keys, including unavailable values.
- 2,449 eligible records have unavailable rate spread and a lien other than 1/2.
  Per Phase 2, a portfolio containing such rows has no calculable quote;
  eligibility is not silently narrowed.

## Representative source records

| Source record | Respondent | Loan thousands | Income thousands | Spread | MSAMD | County | Tract | Tract income |
| --- | --- | ---: | ---: | --- | ---: | ---: | --- | ---: |
| 352 | 23-2470039 | 526 | 411 | unavailable | 35614 | 25 | 8125.01 | 179.67999267578125 |
| 844 | 0000497404 | 70 | 50 | unavailable | 35614 | 29 | 7312.01 | 62.13999938964844 |
| 1216 | 0000817824 | 75 | 51 | unavailable | 15804 | 7 | 6051.00 | 79.08000183105469 |

The temporary CSV staging structure must match all source columns for PostgreSQL
COPY parsing, but no demographic or unrelated fields survive normalization.
