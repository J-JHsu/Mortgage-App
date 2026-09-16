# Mortgage backend — Phase 2

The existing Java/Maven module now starts as a local Spring Boot application.
It retains JDBC and the original mortgage workflow in reusable backend classes.
There are no application REST endpoints yet. Database schema/import and frontend
integration belong to later phases.

## Build and run

Use a JDK 17 or newer (verified with JDK 21). Spring Boot 3.5.16 is used;
see its [system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
Run these commands from this directory:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Alternatively, after building:

```powershell
java -jar target/demo-1.0-SNAPSHOT.jar
```

On Unix-like systems use `sh ./mvnw clean verify` and
`sh ./mvnw spring-boot:run`. The official Apache wrapper scripts download
Maven 3.9.11 on first use; no global Maven installation is required.
The first build requires internet access for Maven and dependencies.

The server binds to `127.0.0.1:8080`. A request to `/` returns HTTP 404 because
no application controller is implemented in this phase. Startup is confirmed by
the Spring Boot/Tomcat startup log, not by a mortgage endpoint. Stop with Ctrl+C.

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the process environment
before performing database operations. See `.env.example` for the format.
Spring Boot does not automatically load that file.

Startup does not open a database connection. Missing configuration causes a clear
`SQLException` when a repository operation is invoked. Connection/query failures
propagate through the service; they are not converted to successful empty results.
No schema is created automatically.

## Structure

- `Main`: Spring Boot entry point.
- `config/DatabaseCon`: environment-configured JDBC connections.
- `model/Mortgage`, `model/Filter`: existing mortgage fields and typed filters.
- `repository/MortgageDAO`: bound JDBC search and transactional packaging.
- `repository/FilterManager`: fixed filter columns, AND between categories, OR within each category.
- `repository/TransactionManager`: explicit begin/commit/rollback operations.
- `service/MortgageCalculator`: portfolio-rate calculation.
- `service/MortgageService`: search, review totals/rate, and confirmed packaging.

The CLI menu and manual mortgage creation have been removed. Filters are
request-local; no shared mutable filter list is retained in the service.

## Preserved rules and corrections

Eligible rows have `action_taken = 1` and purchaser type in `(0, 1, 2, 3, 4, 8)`.
Packaging updates purchaser type to `5`.
The six existing filters remain: MSAMD, loan type, loan purpose, property type,
applicant-income range, and owner occupancy. Geographic and ratio filters await
the actual dataset/schema mapping.

Loan amounts and review totals remain in thousands. Rates use loan-amount
weighting and a 2.33% base:

- Known positive spread: use the actual spread plus 2.33%, with no minimum floor.
- Unavailable spread: add 1.5 percentage points for first liens or 3.5 for second liens.
- SQL NULL spreads remain null. The legacy nonpositive-spread unknown marker is
  retained until the actual CSV representation is inspected in Phase 3.
- Empty portfolios, nonpositive loan amounts, nonfinite spreads/results, or
  unavailable spreads without a supported lien assumption return
  `OptionalDouble.empty()`. No partial portfolio is silently quoted.
- A review without a calculable rate cannot be packaged.

Review and packaging use the reviewed application IDs. Packaging owns one JDBC
connection, disables autocommit, rechecks eligibility in each bound update, and
requires exactly one affected row per ID. It commits only after all updates
succeed. Any update/count/commit failure triggers rollback; rollback failures
are preserved as suppressed exceptions. The connection is then closed.

## Verification and limits

`clean verify` runs JUnit 5 tests for the rate correction, invalid portfolios,
filter grouping/parameter ordering, service orchestration, database error
propagation, JDBC transaction paths, and database-free web-server startup.

Transaction tests use mocked JDBC connections. They verify application control
flow, not PostgreSQL behavior. The current SQL still assumes the original
`application` table/columns. Real queries, source null/sentinel mappings, and
database rollback must be verified once Phase 3 supplies the database.

The review object is internal Java orchestration, not a finalized HTTP contract.
No schema/import scripts, frontend changes, or new product features are included.
