# Mortgage REST API (Phase 4)

## Local setup

Set DB_URL, DB_USERNAME and DB_PASSWORD in the server process environment.
Use the existing [database setup](../../database/README.md); startup does not
create/import tables or open a connection. Spring does not auto-load .env.example.
Never commit credentials.

From this backend directory with JDK 17+:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
# Or: java -jar target/demo-1.0-SNAPSHOT.jar
```

Server: http://127.0.0.1:8080. CORS allows only http://localhost:5173 and
http://127.0.0.1:5173. POST requests require application/json.

## Endpoints

| Method | Path | Response |
| --- | --- | --- |
| GET | /api/mortgages/options | loanTypes, loanPurposes, propertyTypes, counties, msamds, ownerOccupancy |
| POST | /api/mortgages/search | mortgages, eligibleMortgageCount, totalLoanAmountDollars |
| POST | /api/mortgages/rate | eligibleMortgageCount, totalLoanAmountDollars, expectedRatePercent, portfolioFingerprint |
| POST | /api/mortgages/package | success, packagedMortgageCount, packagedTotalLoanAmountDollars |

Options are code-sorted arrays of code/label objects. MSAMD entries include
`{"code":35620,"label":null}` and `{"code":37980,"label":null}`. They remain usable.

## Filter contract

Search and rate take the same object:

```json
{
  "countyCodes": [13,25],
  "loanTypes": [1],
  "loanPurposes": [1,3],
  "propertyTypes": [1],
  "msamds": [35084,35614],
  "ownerOccupancy": [1],
  "incomeLoanRatioMin": 0.5,
  "incomeLoanRatioMax": null,
  "tractToMsamdIncomeMin": 100,
  "tractToMsamdIncomeMax": null
}
```

Categories combine with AND; values within each category with OR. Arrays accept
only integer codes from lookup tables. Missing/null/empty arrays impose no filter;
duplicates are collapsed; null elements are invalid. Bounds are inclusive,
nonnegative finite JSON numbers. Either bound may be absent/null, and min must
not exceed max. Ratio 0.5 means income is half of loan amount. Tract income 100
means 100% of MSA/MD income. Missing data does not match an active filter.

An empty object searches all eligible rows. Unknown fields, string numbers,
fractional codes, invalid codes/ranges and malformed JSON return 400.
No raw IDs, SQL, operators or column names are accepted.

## Search results and units

All API money fields are actual dollars, using integer/long conversion.
Database/calculator units remain thousands. Rates are percentages: 3.83 means
3.83%, not 0.0383. Spread fields are percentage points.

Each mortgage has applicationId, respondentId, loanAmountDollars,
applicantIncomeDollars, rateSpreadPercentPoints, censusTractNumber,
tractToMsamdIncomePercent, and code/label objects for loanType, loanPurpose,
propertyType, ownerOccupancy, county, msamd, actionTaken, purchaserType and lienStatus.

Missing values remain JSON null. Missing geography is a null object; a known code
with a missing name is an object with a null label. A missing loan amount makes
the whole search total null rather than returning a partial total.

Empty search response:

```json
{"mortgages":[],"eligibleMortgageCount":0,"totalLoanAmountDollars":0}
```

There is no pagination: totals/rate cover the complete matching set, and broad
searches can produce large responses.

## Review and packaging

Real verification filter:

```json
{"countyCodes":[1],"tractToMsamdIncomeMin":58.25,"tractToMsamdIncomeMax":58.25}
```

Rate response before packaging:

```json
{
  "eligibleMortgageCount":1,
  "totalLoanAmountDollars":45000,
  "expectedRatePercent":3.83,
  "portfolioFingerprint":"3b5104394adde03e317a280ee3805ab57007c09c18f681482751f974e3ea9854"
}
```

After user confirmation, package with the same filters and returned fingerprint:

```json
{
  "filters":{"countyCodes":[1],"tractToMsamdIncomeMin":58.25,"tractToMsamdIncomeMax":58.25},
  "portfolioFingerprint":"3b5104394adde03e317a280ee3805ab57007c09c18f681482751f974e3ea9854"
}
```

Success:

```json
{"success":true,"packagedMortgageCount":1,"packagedTotalLoanAmountDollars":45000}
```

The server re-queries the filtered set, checks the fingerprint and quotability,
and sends only server-selected IDs to the existing transactional service.
The fingerprint detects changed IDs/quote-relevant values; it is not authentication.
Each update rechecks eligibility, and all updates must succeed before commit.
Failures trigger rollback. Purchaser type becomes 5 (Private securitization).
A repeat request conflicts; search and review again.

Eligibility remains action_taken=1 and purchaser_type IN (0,1,2,3,4,8).
The calculator still adds known spread to 2.33%, or assumes 1.5 percentage points
for first liens / 3.5 for subordinate liens when spread is unavailable, weighted
by loan amount. Empty/invalid/unsupported portfolios cannot be quoted or packaged;
loans are never silently dropped from the calculation.

## Error contract

```json
{"code":"EMPTY_PORTFOLIO","message":"No eligible mortgages match these filters."}
```

| Status | Code | Meaning |
| --- | --- | --- |
| 400 | INVALID_REQUEST | Bad JSON, codes, ranges, or package request |
| 409 | PORTFOLIO_CHANGED | Search/review again |
| 409 | PACKAGING_CONFLICT | Transaction/eligibility conflict, rollback attempted |
| 422 | EMPTY_PORTFOLIO | No matching loans to quote |
| 422 | UNQUOTABLE_PORTFOLIO | Missing/unsupported loan/spread/lien data |
| 503 | DATABASE_ERROR | Database connection/query/transaction failure |
| 500 | INTERNAL_ERROR | Unexpected backend error |
| 405 / 415 | METHOD_NOT_ALLOWED / UNSUPPORTED_MEDIA_TYPE | Wrong method/content type |

Responses never contain SQL, credentials, paths or stack traces. Server logs retain
diagnostics. Database failures never become successful empty responses. Connection
loss during commit can make its outcome uncertain: search/review before repackaging.

## Verification

37 JUnit tests pass, including prior calculator/JDBC rollback/startup tests plus
API options/nulls, filters, validation, dollars, empty/unquotable portfolios,
packaging/conflicts/errors and CORS.

Real isolated PostgreSQL HTTP checks confirmed 21 counties, 11 MSAMD codes, both
missing labels preserved; combined example filters returned 3,720 loans.
The one-loan example quoted 3.83% and packaged $45,000. An injected database failure
verified the rollback error path. The temporary trigger was removed and original
purchaser restored; eligible count returned to 123,036. Local preflight passed.

Frontend, database scripts and dependencies remain unchanged. Phase 5 is not started.
