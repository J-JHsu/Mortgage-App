# 2017 New Jersey HMDA database

This portfolio version normalizes only the fields needed for mortgage search,
portfolio-rate calculation and securitization. It does not reconstruct the entire
HMDA dataset or the original course schema. No REST endpoints are implemented here.

## Source and mapping

Use `data/raw/hmda_2017_nationwide_all-records_labels.csv`.
The source is nationwide, but the import stores only year 2017 and state code 34.
See [source-mapping.md](source-mapping.md) for exact source columns, observed
values, missing-value counts and representative records.
The complete original header is preserved in [source-header.txt](source-header.txt).

The 11,237,068,086-byte file contains 14,285,496 data records. It is comma-delimited,
uses double quotes, has no UTF-8 BOM, and parsed successfully in full as UTF-8.
Quoted empty strings become SQL NULL. Labels are trimmed; no missing labels are
invented. The source and local verification files remain under Git-ignored `data/`.

## Initialize a dedicated local database

Requires PostgreSQL 15 or newer for `HEADER MATCH` and `UNIQUE NULLS NOT DISTINCT`.
Verified with PostgreSQL 18.6. Use an empty database devoted to this project.
The scripts never drop or overwrite existing application data.

From the repository root, with PostgreSQL tools on PATH:

```powershell
createdb -h localhost -U postgres mortgage_2017_nj
psql -X -h localhost -U postgres -d mortgage_2017_nj -f database/schema.sql
$csvPath = (Resolve-Path 'data/raw/hmda_2017_nationwide_all-records_labels.csv').Path.Replace('\', '/')
psql -X -h localhost -U postgres -d mortgage_2017_nj -v "csv_path=$csvPath" -f database/import.sql
psql -X -h localhost -U postgres -d mortgage_2017_nj -f database/validate.sql
```

On this Windows installation the executables are in
`C:/Program Files/PostgreSQL/18/bin`; use their full paths if they are not on PATH.
Let the tools prompt for your local password or use your existing local PostgreSQL
credential configuration. Do not put passwords in these scripts or Git.

`COPY` reads the CSV from the **database server's filesystem**, so supply an
absolute path readable by the PostgreSQL service account. Run the import with a
local administrative role that can read server files. No shell programs or extra
data-processing dependencies are invoked. PostgreSQL's CSV reader handles quoted
commas and validates the exact header.

The import scans the nationwide file once, but its COPY WHERE clause stores only
NJ rows in a temporary table. Other-state records are not normalized or retained.
Allow time for reading the 11.2 GB source. Index creation is included by
`import.sql`; do not run `indexes.sql` a second time.

## Import transaction

1. Refuse to run if application records already exist.
2. Parse the full physical CSV header into a temporary staging structure, filtering
   to 2017 NJ during COPY.
3. Convert the 26 required source columns to typed values; preserve NULL.
4. Reject conflicting tract-income values for the same location.
5. Populate source-derived code/name lookups and deduplicated locations.
6. Insert applications in source order with generated database IDs.
7. Compare every retained financial/reference/geographic value and record count
   against staging, and confirm critical action/purchaser/lien meanings.
8. Create indexes and commit. Temporary staging is removed.

Any failure before commit rolls back normalized inserts and indexes. Re-running
against populated tables intentionally fails rather than duplicating loans or
undoing securitization. For another independent reproduction use a new empty
database and the same commands. Source-row ordinals are tied to this exact file
order, not universal HMDA identifiers.

The temporary table has all 78 CSV columns solely to parse the physical format.
Demographics and other unused data do not survive the import.

## Tables and normalization

| Table | Purpose / imported rows |
| --- | --- |
| application | 349,563 records with financial values, scope year and reference keys |
| location | 2,135 shared county/tract/MSAMD combinations and tract-income values |
| county | 21 NJ county code/name pairs, state constrained to 34 |
| msamd | 11 MSAMD codes; names may be NULL |
| loan_type | 4 code/name pairs |
| loan_purpose | 3 code/name pairs |
| property_type | 3 code/name pairs |
| owner_occupancy | 3 code/name pairs |
| action_type | 8 code/name pairs |
| purchaser_type | 10 code/name pairs |
| lien_status | 4 code/name pairs |

Applications reference locations and the seven categorical tables through foreign
keys. Locations reference county and MSAMD. Lookups eliminate repeated labels and
reject invalid codes. No institution table is needed: the source respondent
identifier is retained as text without assuming that it uniquely identifies a loan.

The location natural key treats NULLs as equal so missing geographic combinations
are deduplicated consistently. Tract identifiers remain text to preserve leading
zeros. Their income measure is numeric without artificial rounding.

Financial amounts remain **thousands of dollars**. Rate spread is numeric percentage
points. Empty income, amount, spread and geography values remain NULL. Positive
amount/income/spread checks are supported by the inspected nonmissing values;
tract income can be zero. Primary keys, foreign keys, required-field constraints,
scope checks and source-row uniqueness protect relational integrity.

Income/loan ratio is derived rather than stored:

```sql
applicant_income_000s::numeric / NULLIF(loan_amount_000s, 0)
```

MSAMD codes 35620 and 37980 have NULL names because their source labels are empty.
Lien 2 retains its actual source label, "Secured by a subordinate lien"; the existing
code-2 calculation assumption remains unchanged.

## Eligibility and packaging

```sql
SELECT count(*), sum(loan_amount_000s)
FROM application
WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8);
```

Expected immediately after this source import: **123,036 loans**, totaling
**41,280,637 thousands of dollars**. Purchaser 5 is Private securitization and
contains 1,096 source records. Updating eligible records to purchaser 5 removes
them from the eligible set, without deleting the records.

There are 2,449 eligible rows with unavailable spread and lien other than 1 or 2.
The Phase 2 calculator intentionally cannot quote portfolios containing these
rows. This phase neither invents a rate nor narrows eligibility.

## Indexes

In addition to primary-key and unique-constraint indexes:

- `application_eligible_location_idx`: partial index for eligible geographic searches.
- `application_eligible_loan_type_idx`: partial index for eligible loan-type searches.
- `location_msamd_idx`: MSAMD lookup before joining eligible applications.

The location natural-key index already starts with county code. No additional
index is added for every low-cardinality category or numeric range. EXPLAIN on an
eligible MSAMD query used the MSAMD index and eligible-location index.

## Validation and backend compatibility

`validate.sql` prints totals, reference labels, source examples, constraints,
indexes and a combined filter query. Its foreign-key/check/purchaser-change probes
run inside a transaction that is rolled back.

The combined example (counties 13/25, conventional, MSAMDs 35084/35614, purposes
1/3, property/occupancy 1, income/loan >= 0.5 and tract income >= 100) returns
**3,720** eligible records.

Missing values preserved: **340,747 spreads**, **154 loan amounts** and
**50,515 applicant incomes**. Source examples 352, 844 and 1216 match the normalized
records. Import assertions compare all retained numeric/reference/geographic values.

Java's MSAMD query/filter now joins location. Nullable integer fields use
`Integer` and JDBC `getObject` rather than turning database NULL into zero.
Review sums known amounts; a missing amount still prevents a portfolio quote.
No currency conversion, eligibility change or rate-rule change was introduced.

Configure the backend using `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` for your
project database, then run from `Project 2/demo`:

```powershell
.\mvnw.cmd clean verify
```

The JDBC classes can query this schema. County, ratio and tract-income filters
are verified in SQL; exposing them through the service/API remains later work.

## Local verification instance

### Results on 2026-09-16

- Reproduced schema, full-source NJ-only import and validation in a second empty
  database: 349,563 applications, 2,135 locations, all 9 lookup tables populated.
- All 10 foreign keys validated; invalid loan-type reference and zero loan amount
  rejected. Purchaser-5 transition removed the selected row from eligibility.
- Repeat import refused before reading the source; application count unchanged.
- Real Java/JDBC search returned 123,036 eligible records and the expected total;
  Newark MSAMD 35084 plus conventional loan type returned 27,657.
- A failed second JDBC packaging update rolled back the first; a successful
  packaging update committed purchaser 5. The test restored the source purchaser.
- `mvnw.cmd clean verify`: 25 tests, zero failures/errors/skips.
- Frontend tracked/untracked checks found no changes; raw source and verification
  cluster remain ignored by Git. No application REST endpoints were added.

Verification used an isolated PostgreSQL cluster under ignored
`data/phase3-postgres`, bound to `127.0.0.1:55437`, with databases
`mortgage_phase3` and `mortgage_phase3_clean`. The normal PostgreSQL service and
unrelated databases were not modified. This disposable verification cluster uses
local trust authentication and is stopped after verification. Its files are not
part of the reproducible repository setup; the commands above create a regular
local development database.
