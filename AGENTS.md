# AGENTS.md

## Project purpose

This repository is being completed and cleaned up as a small local portfolio/resume project based on an existing Rutgers database course project.

The application works with 2017 New Jersey HMDA mortgage data.

The finished application should demonstrate:

- PostgreSQL
- relational database design
- SQL
- Java
- JDBC
- Spring Boot
- REST APIs
- React
- mortgage filtering
- weighted portfolio-rate calculations
- database transactions
- focused backend testing

The objective is NOT to create a production banking platform.

The objective is a small, complete, understandable full-stack application.

---

# Engineering principles

Follow a simplicity-first approach.

1. Make the smallest reasonable change that satisfies a requirement.
2. Reuse existing working code instead of rewriting it unnecessarily.
3. Do not add features that were not explicitly requested.
4. Do not introduce abstractions without a concrete need.
5. Do not perform unrelated refactors.
6. Prefer readable straightforward code over clever architecture.
7. Preserve existing behavior unless it is clearly broken, unsafe, or incompatible with the target architecture.
8. Do not add technologies merely because they are common in production.
9. Verify each meaningful change.
10. Stop when the requirements are satisfied.

If implementation behavior is unclear, inspect the existing code before making a decision.

Do not silently redesign product behavior.

---

# Hard scope boundary

This is a LOCAL application only.

Do NOT add:

- deployment
- AWS
- Azure
- GCP
- Vercel
- Render
- Docker unless it becomes necessary for local functionality
- Kubernetes
- Terraform
- microservices
- authentication
- authorization
- users
- manager accounts
- OAuth
- payment processing
- machine learning
- mortgage approval prediction
- credit scoring
- Redis
- Kafka
- message queues
- WebSockets
- GraphQL
- native mobile apps
- SEO functionality
- complex analytics
- large charting systems
- extensive frontend animation
- marketing pages
- multiple HMDA years
- nationwide HMDA support

Do not suggest or implement these as "nice to have" improvements.

---

# Target architecture

The target application is:

```text
React + Vite
     |
     | HTTP / JSON
     v
Spring Boot REST API
     |
     v
Java service/business logic
     |
     v
JDBC repository layer
     |
     v
PostgreSQL
     |
     v
2017 New Jersey HMDA data
```

The browser interface is the primary user interface.

The old command-line application does not need to remain an actively supported interface.

However, reuse useful existing CLI logic wherever practical.

Do not maintain duplicate CLI and web implementations of the same business rules.

---

# Existing code

Before changing anything, inspect the entire repository.

Existing useful code may include:

- Mortgage model
- MortgageDAO
- MortgageCalculator
- Filter
- FilterManager
- DatabaseCon
- TransactionManager
- CLI mortgage workflow
- React frontend
- frontend mortgage-search components
- frontend packaging components
- partially implemented API calls

Do not discard these automatically.

Reuse correct working logic.

---

# Backend

Convert the current Java/Maven backend into a minimal Spring Boot application.

Use:

- Java
- Maven
- Spring Boot
- Spring Web
- PostgreSQL JDBC
- JUnit 5

Continue using JDBC.

Do not introduce Hibernate or JPA unless there is a clear technical requirement that cannot reasonably be handled with the existing JDBC approach.

Prefer a simple organization such as:

```text
controller/
service/
repository/
model/
config/
```

Do not create unnecessary interface/implementation pairs.

For example, prefer one concrete:

```text
MortgageService
```

rather than:

```text
MortgageService
MortgageServiceImpl
```

unless multiple implementations actually exist.

---

# Core application workflow

Preserve the original application's fundamental workflow:

1. search/filter eligible mortgages
2. inspect the matching mortgage set
3. calculate the weighted portfolio rate
4. review loan count, total loan amount, and rate
5. confirm mortgage securitization
6. update the relevant mortgages transactionally

Do not invent a different mortgage workflow.

---

# Mortgage filtering

Support the applicable existing mortgage filters, including:

- MSAMD
- applicant income / loan amount ratio
- county
- loan type
- tract-to-MSA/MD income
- loan purpose
- property type
- owner occupied

Filters must be combinable.

Different categories combine using AND.

Multiple accepted values within one category combine using OR.

Example:

```text
(County = A OR County = B)
AND
Loan Type = Conventional
```

The frontend should show the currently active filters.

Do not build a generic query-builder framework.

---

# SQL safety

Where practical, replace raw dynamic value concatenation with JDBC `PreparedStatement` parameter binding.

User/frontend-supplied values must not be concatenated directly into SQL.

Static SQL fragments representing known columns/operators may be constructed by application logic when necessary.

Do not add an ORM just to avoid SQL.

---

# Mortgage-rate calculation

Preserve the existing MortgageCalculator business logic unless a clear bug is found.

The original application uses:

- base rate of 2.33%
- known rate spread when available
- lien-status-specific assumptions for unavailable rate spread
- loan amount as the weighting value
- weighted-average portfolio rate

This calculation belongs in the Java backend.

Do not calculate portfolio rates independently in React.

---

# Transactions

Mortgage securitization must be transactional.

At minimum:

1. begin transaction
2. perform required database update
3. commit on success
4. rollback on failure

A failure must never result in a partially securitized mortgage set.

Reuse existing TransactionManager behavior where practical.

---

# REST API

Create only the endpoints required by the frontend.

A reasonable API is:

```text
GET  /api/mortgages/options
POST /api/mortgages/search
POST /api/mortgages/rate
POST /api/mortgages/package
```

Preserve an add-mortgage endpoint only if the existing functionality remains part of the completed application and implementing it does not enlarge scope unnecessarily.

Do not create unrelated endpoints.

---

# Database reconstruction

The original normalized SQL scripts are no longer available.

Do NOT recreate every unused field from the original Rutgers database assignment.

Create a normalized PostgreSQL schema containing the data required by this application.

The schema should demonstrate proper relational design using:

- primary keys
- foreign keys
- lookup/reference tables
- NOT NULL constraints where appropriate
- CHECK constraints where appropriate
- sensible numeric types
- useful indexes for application queries

Possible conceptual tables include:

```text
application
location
loan_type
loan_purpose
property_type
action_type
purchaser_type
lien_status
county/geographic lookup information
MSAMD/geographic lookup information
```

The main application/mortgage record should contain the financial and reference fields required by the application.

Do not finalize source-column mappings by guessing.

---

# HMDA source data

The user has the original 2017 New Jersey HMDA CSV/ZIP locally.

Before implementing the final import:

1. locate or request access to the local HMDA CSV
2. inspect the actual CSV header
3. identify the exact fields required by this application
4. map those fields into the normalized schema
5. document that mapping

Do not guess HMDA source column names when the actual file can be inspected.

The large HMDA dataset must NOT be committed to Git.

Use something similar to:

```text
data/
    README.md
    .gitkeep
```

and ignore:

```text
data/*.csv
data/*.zip
```

A very small test fixture may be committed if it is specifically useful for tests.

---

# Database scripts

Create reproducible database setup files.

Prefer a small structure such as:

```text
database/
    schema.sql
    import.sql
    indexes.sql
```

Use fewer files if that is simpler.

The scripts should make it possible to:

1. create the normalized schema
2. load the required HMDA data
3. populate lookup tables
4. establish constraints and indexes
5. run the application

Do not reproduce unused historical fields merely to make the SQL larger.

---

# Frontend

Keep the existing React + Vite frontend.

Do NOT migrate to:

- Next.js
- Angular
- Vue
- another frontend framework

Inspect existing components first.

Reuse useful components.

Remove:

- active hard-coded mortgage examples
- fake rate results
- fake packaging success values
- duplicate search pages
- dead frontend code
- obsolete components

The frontend is intentionally simple.

It only needs to make the database/application workflow easy to use.

---

# Frontend pages

Prefer a very small navigation structure.

Likely pages:

```text
Dashboard
Mortgage Search
```

Mortgage packaging may be a separate page or a clear step/action originating from Mortgage Search.

If an existing Home page adds no value beyond Dashboard, remove or simplify it rather than keeping redundant navigation.

---

# Mortgage search UI

The Mortgage Search page should allow the user to:

- select filter types
- select filter values
- add filters
- see active filters
- remove an individual filter
- clear all filters
- search eligible mortgages
- see the number of matching mortgages
- see the total matching loan amount
- inspect useful mortgage rows in a table
- calculate the portfolio rate
- proceed to securitization

Do not add advanced enterprise table functionality.

No need for complex:

- column customization
- drag-and-drop
- saved views
- user preferences
- export systems
- advanced table frameworks

unless an existing dependency already provides something useful with essentially no additional complexity.

---

# Dashboard

Create a small real dashboard backed by the database.

Approximately three summary values are enough:

- eligible mortgage count
- total eligible loan value
- average eligible loan amount

Optionally include one simple textual/table breakdown such as:

```text
Purchaser Type | Mortgage Count
```

or:

```text
Loan Type | Mortgage Count
```

Do not add a charting library unless one already exists and using it is clearly simpler than a table/list.

The dashboard exists to demonstrate SQL aggregation and API integration, not advanced visualization.

---

# Design reference

Use:

```text
docs/stripe-DESIGN.md
```

as a visual-language reference.

Use it selectively.

This application is an internal mortgage-data tool, NOT a Stripe marketing-site clone.

Prioritize these characteristics from the design reference:

- white and soft-gray surfaces
- deep navy body text
- restrained indigo primary accent
- subtle hairline borders
- compact form inputs
- simple transactional buttons
- small/light cards
- readable dense tables
- tabular figures for financial values
- restrained shadows
- consistent spacing

Use the documented financial-data styling, especially tabular numerals for:

- currency
- percentages
- mortgage counts
- loan amounts

Use Inter as the practical open-source font.

Do NOT attempt to use or distribute proprietary Sohne font files.

---

# Design elements explicitly out of scope

Do NOT reproduce the Stripe marketing-site characteristics from the design reference.

Do not add:

- gradient-mesh hero sections
- giant 48–56px marketing headlines
- landing-page sections
- pricing cards
- promotional content
- testimonial sections
- marketing footers
- decorative product mockups
- orange/pink/ruby gradient decoration
- excessive visual effects

This should look like a restrained internal financial application.

Functionality and readability matter more than visual novelty.

---

# Suggested frontend typography

Use approximately:

```text
Page title:      28px / weight 500
Section title:   20px / weight 500
Body:            15px / weight 400
Tables:          14px / weight 400
Labels/helper:   13px / weight 500
```

Financial/numeric cells should use tabular figures.

Do not blindly copy the design reference's weight-300 marketing typography.

---

# Frontend responsiveness

This is desktop-first because it is a local data tool.

However, avoid layouts that completely break on smaller browser widths.

Basic responsive behavior is enough.

Do not build a specialized mobile interface.

---

# Authentication

There is no authentication.

Do not add:

- login page
- user accounts
- manager accounts
- sessions
- roles
- permissions

---

# Configuration

Do not commit database credentials.

Use environment/configuration values for:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Provide an example configuration such as:

```text
.env.example
```

or the appropriate Spring Boot example properties.

---

# Testing

Use JUnit 5.

Focus testing on business logic.

At minimum cover MortgageCalculator cases such as:

- known rate spread
- unavailable rate spread with first lien
- unavailable rate spread with second lien
- weighted average across several mortgages
- empty mortgage set / invalid calculation case

Test important filtering behavior:

- one filter
- multiple different categories using AND
- multiple values within one category using OR
- `(A OR B) AND C`

Add service tests when useful.

Repository integration tests are optional and should only be added if straightforward.

Do not create a large testing infrastructure.

Frontend automated tests are not required.

Frontend verification should include successful build and manual functional checks.

---

# README

Replace the current minimal README with a useful project README.

Include:

- project overview
- features
- architecture
- technologies
- database design
- rate-calculation explanation
- local setup
- HMDA dataset setup
- backend startup
- frontend startup
- testing
- screenshots
- known limitations/project background

Only claim functionality that actually exists.

Be clear that this is a local portfolio/course-derived application.

---

# Repository cleanup

Clean obvious issues only.

Examples:

- remove hard-coded credentials
- remove fake active data
- remove dead code
- remove duplicate pages
- remove unused imports
- update `.gitignore`
- remove unnecessary committed build artifacts
- improve misleading comments
- perform low-risk naming cleanup

Do not mass-reformat or rename unrelated working code.

---

# Comments

Avoid comments that simply repeat what code already says.

Comments should explain things such as:

- HMDA-specific assumptions
- unusual mortgage rules
- rate-spread behavior
- transaction reasoning
- non-obvious data mappings

---

# Implementation phases

Follow this sequence.

## Phase 1: Audit

Inspect the repository completely.

Document:

- current structure
- reusable code
- duplicate/mock code
- current build status
- database assumptions
- frontend routes/components

Do not perform broad implementation work yet.

## Phase 2: Spring Boot backend

Convert the existing Java/Maven backend to minimal Spring Boot while preserving reusable logic.

Verify backend compilation/startup.

## Phase 3: Database

Inspect the real HMDA CSV header.

Create the minimal normalized schema and import process required by this application.

Verify representative SQL queries.

## Phase 4: REST API

Implement the minimum API required by the frontend.

Verify endpoints independently.

## Phase 5: React integration

Connect the existing frontend to the real API.

Remove fake active data and duplicate pages.

## Phase 6: Dashboard

Add the small database-backed dashboard.

## Phase 7: Tests

Add focused JUnit tests.

## Phase 8: Cleanup and README

Perform limited cleanup.

Document setup and architecture.

Verify the complete workflow.

Do not skip foundational failures in order to work on later visual features.

---

# Verification before completion

Backend:

- Maven build succeeds
- Spring Boot starts
- JUnit tests pass
- PostgreSQL connection works
- search works
- rate calculation works
- securitization works
- transaction rollback works on failure
- no secrets are committed

Database:

- schema is reproducible from repository scripts
- required HMDA fields import correctly
- PK/FK relationships work
- constraints work
- important search fields have reasonable indexes

Frontend:

- npm installation succeeds
- production build succeeds
- Vite development app starts
- active flows contain no hard-coded mortgage records
- filters use backend data
- results come from PostgreSQL
- portfolio values come from backend calculations
- securitization calls the backend
- errors are understandable
- dashboard statistics are real database values

Repository:

- README explains setup
- raw HMDA data is ignored by Git
- unnecessary dependencies were not added
- unrelated features were not added

---

# Completion report

When all requested work is finished, provide:

## What changed

Meaningful architectural and functional changes.

## Existing code reused

Identify original logic/components that were preserved.

## Code removed or replaced

Identify mock, duplicate, obsolete, or unsafe code that was removed.

## Database

Describe the final schema and exact HMDA fields used.

## API

List final endpoints.

## Tests

List important tests and results.

## Verification

Show commands run and whether they passed.

## Remaining limitations

Be explicit about anything incomplete.

Do not hide failures.

---

# Stop rule

Once the requested workflow works end-to-end, STOP.

Do not continue adding improvements merely because they are possible.

The project is complete when it demonstrates:

```text
2017 NJ HMDA data
        ↓
normalized PostgreSQL database
        ↓
Java / Spring Boot / JDBC
        ↓
mortgage filtering and calculations
        ↓
transactional securitization
        ↓
simple React interface
```

A small complete system is preferred over a larger partially finished system.