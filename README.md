# Loan Processing Assistant

A GenAI-powered loan processing application built with Spring Boot. Customers submit
loan applications and supporting documents; a configurable, auditable rules engine
makes the automated credit decision; Claude (via the Anthropic API) assists with
document extraction, summarization, gap/risk analysis, and customer communication —
but **never makes the final approval/rejection decision**.

## Decision-control model

| Step | Performed by |
|---|---|
| Document data extraction | Claude (advisory) |
| Application summary | Claude (advisory) |
| Missing/inconsistent info flags | Claude (advisory) |
| Risk highlights | Claude (advisory) |
| **Automated decision** | **Deterministic `RulesEngine`** (never the model) |
| **Manual review decision** | **Authorized loan officer** (`ROLE_OFFICER`) |
| Decision explanation / customer letter | Claude (drafted *after* the decision) |

Protected characteristics (race, religion, gender, disability, marital status, age,
national origin, …) are not modeled anywhere in the domain, are never requested in
the UI, and free-text sent to the model is additionally passed through
`ProtectedDataSanitizer` as defence-in-depth.

Every submission, evaluation, document upload, and officer decision is persisted to
`audit_logs`, and every rule outcome (with the threshold applied) is persisted to
`rule_evaluations` for full auditability.

## Business rules

Configured under `lending.rules` in `application.yml`:

| Rule | Hard reject | Manual review |
|---|---|---|
| `MIN_CREDIT_SCORE` | < `review-credit-score` (580) | < `min-credit-score` (640) |
| `MAX_DEBT_TO_INCOME` | > `review-debt-to-income` (0.43) | > `max-debt-to-income` (0.36) |
| `MIN_ANNUAL_INCOME` | < `min-annual-income` (25,000) | — |
| `EMPLOYMENT_STABILITY` | unemployed | retired or tenure < `min-years-employed` (1) |
| `MAX_LOAN_TO_INCOME` | — | amount > `max-loan-to-income-multiple` (5× income) |

Any REJECT-severity failure → auto-rejected. Otherwise, any REVIEW failure (or
`auto-approve-enabled: false`) → `MANUAL_REVIEW` queue for a loan officer.
All rules passing → auto-approved.

## Tech stack

Java 21 · Spring Boot 3.5 · Spring MVC · Spring Data JPA · Spring Security ·
Thymeleaf + Bootstrap 5 · MySQL 8 · Flyway · Anthropic Claude API · Maven · Lombok ·
JUnit 5 / Mockito · Testcontainers · Docker / Docker Compose

## Running

### Docker Compose (recommended)

```bash
export ANTHROPIC_API_KEY=sk-ant-...   # optional; app runs on a local stub without it
docker compose up --build
```

App: http://localhost:8080 — officer login `officer` / `officer123!`
(override with `SEED_OFFICER_USERNAME` / `SEED_OFFICER_PASSWORD`).

### Local (needs a MySQL 8 instance)

```bash
export DB_HOST=localhost DB_USER=root DB_PASSWORD=root
mvn spring-boot:run
```

Flyway creates/migrates the schema automatically.

### Tests

```bash
mvn test
```

Unit tests run without Docker. `LoanApplicationRepositoryIT` uses Testcontainers +
MySQL 8 and is skipped automatically when Docker is unavailable.

## Roles

- `CUSTOMER` — register, submit applications, upload documents, view status/letters
- `OFFICER` — manual review queue, AI advisory panel, approve/reject with justification
- `ADMIN` — same as officer

## Configuration highlights

| Property / env var | Purpose |
|---|---|
| `ANTHROPIC_API_KEY` | Enables the real Claude client (otherwise deterministic stub) |
| `ANTHROPIC_MODEL` | Claude model id (default `claude-sonnet-4-5-20250929`) |
| `lending.rules.*` | All policy thresholds + `auto-approve-enabled` |
| `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` | MySQL connection |
