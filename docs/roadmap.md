# Sprint 2 Roadmap — AguiaX

## Goal

Complete Sprint 2 by evolving the existing AguiaX Sprint 1 implementation into a secure, observable, API-driven platform with:

- Java Spring Boot backend;
- full Android/backend integration;
- server-side role restrictions;
- NoSQL usage;
- external service consumption;
- non-chatbot AI integration;
- backend analytics/dashboard;
- audit, logs and metrics;
- meaningful automated tests.

The roadmap is intentionally ordered to reduce rework.

---

# Current baseline

Already implemented:

- Java Spring Boot backend;
- Spring MVC;
- Spring Data JPA / Hibernate;
- Spring Security;
- JWT authentication;
- BCrypt;
- MySQL + Flyway;
- Swagger/OpenAPI;
- Android integration with Retrofit;
- three roles: `OPERACIONAL`, `GESTOR`, `LIDER`;
- strategic orientations;
- ideas;
- votes;
- projects;
- badges;
- dashboard UI;
- approximately complete core CRUD APIs.

Sprint 2 work should build on this baseline rather than replace it.

Roadmap tasks apply only to the active `backend/` and `frontend/` directories.
The `aguiaexpress-entrega/` directory is a previous-semester historical snapshot
and must be excluded from implementation audits, builds, tests, and Sprint 2 changes.

---

# Phase 0 — Sprint 2 compliance audit

## Task 0.1 — Create requirement matrix

Map every Sprint 2 requirement to:

- backend feature;
- endpoint;
- role;
- Android screen;
- database;
- test.

Include at minimum:

- complete APIs;
- app integration;
- access restrictions;
- external service;
- MongoDB/NoSQL;
- AI;
- dashboard;
- audit;
- logs;
- metrics.

### Exit criterion

Every Sprint 2 requirement has a concrete implementation location and verification method.

### Requirement matrix

The implementation audit was completed on 2026-09-08 and is recorded in
[`docs/sprint2-requirement-matrix.md`](sprint2-requirement-matrix.md). The matrix
classifies current behavior and maps every gap to its roadmap task and verification
method.

---

## Task 0.2 — Establish regression baseline

Before new work:

- compile backend;
- run existing backend tests;
- compile Android app;
- run existing Android tests;
- verify local MySQL startup;
- verify login;
- verify one protected endpoint;
- record any existing failures separately from Sprint 2 changes.

### Exit criterion

A known pre-Sprint-2 baseline exists so new regressions can be distinguished from existing defects.

### Baseline record

The baseline was executed on 2026-09-08 and is recorded in
[`docs/sprint2-regression-baseline.md`](sprint2-regression-baseline.md). All required
checks were run. The record includes one pre-existing backend test failure caused by
test database configuration; it is not a Sprint 2 regression.

---

# Phase 1 — Security hardening

## Task 1.1 — Define authorization matrix in code/docs

Implement or document the target role matrix:

| Operation | OPERACIONAL | GESTOR | LIDER |
|---|---:|---:|---:|
| View orientations | yes | yes | yes |
| Manage orientations | no | no | yes |
| Create idea | yes | no | no |
| Edit/delete own idea | yes | no | no |
| Review/prioritize idea | no | yes | no |
| Approve/reject idea | no | yes | no |
| Manage projects | no | yes | no |
| View projects | yes | yes | yes |
| Executive dashboard | no | no | yes |

### Exit criterion

Every protected business operation has an explicit intended role policy.

### Task 1.1 record — 2026-09-08

The authorization specification was completed by Codex in
[`docs/architecture.md`, sections 9.1–9.4](architecture.md#91-task-11-policy-decisions).
It inventories all 62 current controller operations, assigns explicit role/ownership
policies, and separately covers planned dashboard, AI, provisioning, audit, and
observability access. The requirement matrix links the decisions to enforcement work.

Previously undefined policies are now specified: `LIDER` manages users/categories;
users can read/update their own profiles; `GESTOR` grants/removes badges; badge
recipients can read their own awards and `GESTOR`/`LIDER` can inspect all awards.
Catalog definitions remain migration-managed, with authenticated read APIs.
Public registration is specified as `OPERACIONAL`-only; privileged provisioning
uses a separate leader-only route and a controlled first-leader bootstrap procedure.

The Task 1.1 documentation exit criterion is satisfied. Verification compares the
inventory against controller mappings and checks document consistency; no runtime
authorization change is delivered by this task. Tasks 1.2–1.5 were subsequently
implemented and verified, completing Phase 1. The pre-existing backend test
configuration failure remains recorded
in the Task 0.2 baseline. This record does not claim that Phase 1 passes.

---

## Task 1.2 — Enable method-level authorization

Use Spring Security method authorization where appropriate.

Examples:

```java
@PreAuthorize("hasRole('GESTOR')")
```

Apply it to role-specific operations.

Use [architecture section 9](architecture.md#9-authorization-architecture) as the
policy source. `@EnableMethodSecurity` already exists; fill gaps such as
operational-only idea creation/update/deletion and verify existing orientation and
project annotations. Resource ownership belongs to Task 1.3; administrative
enforcement belongs to Task 1.4. Include role tests with the relevant implementation.

### Exit criterion

Role-restricted endpoints return `403` when called by an authenticated user with the wrong profile.

### Task 1.2 record — 2026-09-08

Implemented by the developer and verified by Codex. Idea creation, update, and
deletion are restricted to `OPERACIONAL`; idea status changes remain `GESTOR`-only.
Existing project and orientation role policies match the authorization matrix.
Two policy tests and seven focused MVC security tests pass, covering method-security
activation, annotation policy, `401`, `403`, and permitted-role delegation.

---

## Task 1.3 — Enforce idea ownership

Fix the known issue where operational users can potentially edit/delete ideas they do not own.

Rules:

- `OPERACIONAL` may edit/delete only their own idea;
- ownership is checked server-side;
- UI visibility is secondary and not trusted.

### Tests

- owner can update;
- owner can delete;
- non-owner receives `403`.

### Exit criterion

Ownership bypass through Postman/API calls is impossible.

### Task 1.3 record — 2026-09-08

Implemented by Codex after reviewing the developer's partial Android change.
`IdeiaService` now compares the authenticated user's ID with the persisted idea
owner before update/delete, rejects mismatches with the existing controlled `403`
exception, and preserves the original owner during updates. Android passes the
authenticated user ID to the idea list and shows edit/delete actions only for owned
ideas.

Five focused ownership tests pass, including owner update/delete, non-owner denial,
owner preservation, and validation ordering. The combined Task 1.2–1.3 backend suite
passes 14 tests, and Android compilation plus unit tests pass. The complete backend
suite still has the single pre-existing `${DB_URL}` context-test error recorded in
the Task 0.2 baseline; all 14 new security/ownership tests pass in that run.

---

## Task 1.4 — Harden administrative endpoints

Review:

- users;
- categories;
- badges;
- strategic orientations;
- any catalog/status administration endpoint.

Apply the intended role restrictions.

Implement the decisions in [architecture sections 9.1–9.4](architecture.md#91-task-11-policy-decisions):

- restrict user listing/deletion and category mutations to `LIDER`;
- enforce self-or-leader user detail/update access in the service and preserve
  protected profile fields;
- constrain public `POST /usuarios` to `OPERACIONAL` (`400` and no account created
  for a privileged profile); add leader-only `POST /usuarios/admin` for privileged
  provisioning and document a controlled first-leader bootstrap procedure;
- restrict both badge-grant variants and removal to `GESTOR`, global award reads
  to `GESTOR`/`LIDER`, and individual/user award reads to self or those roles;
- preserve authenticated catalog reads and migration-owned definitions; do not
  add status, profile, or badge-definition CRUD;
- verify the existing leader-only orientation restrictions and add integration
  tests for permitted/forbidden calls, cross-user access, and privilege escalation.

### Exit criterion

No sensitive administrative operation is protected only by "authenticated user".

### Task 1.4 record — 2026-09-09

Implemented by the developer and corrected/verified by Codex. User listing/deletion,
category mutations, privileged provisioning, badge mutations, and global badge-award
reads now use the roles defined in the authorization matrix. User detail/update and
individual badge-award reads enforce resource ownership in their services, with
`LIDER` or `GESTOR`/`LIDER` overrides as applicable.

Codex fixed the incomplete public-registration/admin-provisioning service flow,
added the missing user-update ownership check, replaced an uncontrolled duplicate
email error, and protected the global badge-award listing. Public registration now
rejects `GESTOR`/`LIDER` before persistence, while `POST /usuarios/admin` is
leader-only. The first-leader procedure is documented in the README.

Twenty-three new Task 1.4 tests pass (eleven MVC authorization/catalog tests,
eight user-service tests, and four badge-ownership tests). The combined focused
Phase 1 suite passes 37 tests. The full backend context test still has the separate
`${DB_URL}` baseline defect, so this record does not claim the complete Maven suite
is green. The Task 1.4 exit criterion is satisfied.

---

## Task 1.5 — Remove sensitive client logging

Remove logging of the complete JWT authorization header from Android/network interceptors.

Review backend logs for similar leakage.

### Exit criterion

JWTs, passwords and secrets are not emitted into normal logs.

### Task 1.5 record — 2026-09-09

Implemented by the developer and reviewed/verified by Codex. `AuthInterceptor`
no longer prints request URLs, the session token, or the complete Authorization
header. It still adds `Bearer <token>` to authenticated requests and now replaces
an existing Authorization value instead of creating duplicate headers.

Codex replaced the unused OkHttp logging-interceptor dependency with the same-version
OkHttp core dependency and added three interceptor regression tests. They verify
unauthenticated requests, authenticated header attachment without token output, and
replacement of stale Authorization headers. `testDebugUnitTest` and `assembleDebug`
pass, and an active-source scan found no application-owned console/Android/HTTP
logging path that exposes credentials. The exit criterion and Phase 1 are complete.

---

# Phase 2 — NoSQL foundation

## Task 2.1 — Add MongoDB dependency/configuration

Add Spring Data MongoDB.

Configuration must use environment variables.

Suggested variables:

```text
MONGODB_URI
MONGODB_DATABASE
```

Do not commit real credentials.

Update `.env.example` if the repository uses one.

### Exit criterion

Spring Boot starts successfully with both MySQL and MongoDB configured.

### Task 2.1 record — 2026-09-09

Implemented by the developer and corrected/verified by Codex. The backend now
includes Spring Data MongoDB and reads `MONGODB_URI` and `MONGODB_DATABASE` from
the same environment/`.env` bridge used by the existing MySQL and JWT settings.
The committed `.env.example` contains development placeholders only, and `.env`
remains ignored by Git.

Codex normalized the configuration example, aligned the documented MySQL variable
name with the active `DB_USER` property, and added native MongoDB startup and
Compass guidance to the README. `mongosh` returned a successful ping, and
`./mvnw spring-boot:run` connected to MySQL, validated all 11 Flyway migrations,
connected to MongoDB, and started Tomcat successfully. The application was then
stopped gracefully. The Task 2.1 exit criterion is satisfied; MongoDB write/read
proof remains intentionally scoped to Task 2.2. The full backend suite ran 38 tests:
37 passed and the existing `AguiaXApplicationTests.contextLoads` test retained
its previously documented `${DB_URL}` setup error.

---

## Task 2.2 — Add MongoDB integration test

Create a minimal persistence test using the project's preferred testing approach.

Use Testcontainers if practical.

### Exit criterion

The test suite proves a Mongo document can be written and read.

### Task 2.2 record — 2026-09-09

Implemented and verified by Codex. A focused `@DataMongoTest` uses `MongoTemplate`
to save and retrieve a test-only document with a unique ID in the dedicated
`aguiax_task_2_2_<unique-id>` database. The test asserts the persisted fields
and drops its isolated database after execution, so concurrent runs do not share
state and the test does not introduce a throwaway production model or modify the
relational domain.

The local native MongoDB service is used because Docker is not currently available
to the developer account; `mongodb.test.uri` allows an isolated alternative test
server to be supplied. Running
`./mvnw -Dtest=MongoPersistenceIntegrationTest test` completed with one test,
zero failures, zero errors, and `BUILD SUCCESS`. The Task 2.2 exit criterion and
Phase 2 are complete.

---

# Phase 3 — Audit and governance

## Task 3.1 — Create AuditEvent document

Suggested fields:

```text
id
eventType
actorUserId
resourceType
resourceId
timestamp
metadata
```

Create:

- document/model;
- repository;
- audit service.

### Exit criterion

Audit events can be persisted independently from relational domain data.

### Task 3.1 record — 2026-09-09

Implemented and verified by Codex. The new feature-focused `audit` package contains
an immutable `AuditEvent` MongoDB document, typed event and resource enums,
`AuditEventRepository`, and `AuditService`. Events receive a client-generated UUID,
UTC `Instant`, actor and resource identifiers, and a defensive metadata copy. The
document maps exclusively to `audit_events` and has no JPA relationship.

The architecture now records a synchronous best-effort failure policy: required
fields fail validation before persistence, while MongoDB `DataAccessException`
failures are logged without exposing metadata or failing an otherwise successful
relational operation. Four unit tests cover construction, metadata handling,
validation, and the failure policy. One isolated `@DataMongoTest` proves repository
write/read behavior independently from MySQL. The focused run completed five tests
with zero failures and zero errors. The Task 3.1 exit criterion is satisfied;
business-operation hooks remain Task 3.2.

---

## Task 3.2 — Audit critical domain operations

Start with:

- idea created;
- idea updated;
- idea prioritized;
- idea approved;
- idea rejected;
- project created;
- project updated/completed;
- strategic orientation created/updated;
- AI evaluation requested/completed/failed.

Do not make audit persistence the system of record.

### Exit criterion

Critical Sprint 2 actions leave a traceable audit document.

### Task 3.2 record — 2026-09-10

Implemented for every critical workflow that currently exists. Successful login,
idea creation/update/deletion and status decisions, project creation/update/status
completion, and strategic-orientation creation/update/deactivation now request an
audit event after the corresponding relational repository operation succeeds. The
authenticated caller is recorded as the actor; a project's assigned responsible
user is retained only as resource metadata.

Codex completed the missing login, project-update, orientation-update, and
orientation-deactivation hooks and retained the developer's idea, project-create,
project-complete, and orientation-create work. Focused unit tests verify event
selection, actor/resource identity, metadata, ordering after relational persistence,
no audit request after failed relational writes, and no event for rejected login or
forbidden idea mutations. Together with the Task 3.1 MongoDB repository test, the
focused run completed 25 tests with zero failures and zero errors. The complete
backend run completed 59 tests: 58 passed, while the pre-existing
`AguiaXApplicationTests.contextLoads` retained its documented unresolved
`${DB_URL}` setup error because `@SpringBootTest` does not execute the `.env`
bootstrap in `main`.

Task 8.1 subsequently connected the AI workflow's requested, completed, and failed
events to `AiIdeaEvaluationService`. Their focused orchestration tests pass; Task
11.6 retains responsibility for end-to-end audit verification across the completed
Sprint workflow. With those hooks connected, the Task 3.2 event inventory is
implemented.

---

# Phase 4 — Correct idea and project workflows

## Task 4.1 — Add manager review scores

Create a complete manager workflow for:

- impact score;
- viability score.

Add API support and validation.

Decide the numeric range explicitly, e.g. `0..10`.

### Exit criterion

A manager can persist impact and viability scores through the backend API.

### Task 4.1 record — 2026-09-10

Implemented and verified by Codex. `GESTOR` can call
`PATCH /ideias/{id}/avaliacao` with integer `impacto` and `viabilidade` values in
the explicit inclusive range `0..10`. Jakarta Validation rejects invalid HTTP
payloads, while the service repeats the business-range check so non-controller
callers cannot bypass the invariant. A successful review updates both existing
MySQL fields and the idea timestamp, returns `MostrarIdeiaDTO`, and records an
`IDEA_UPDATED` audit event after relational persistence with previous/new scores.

No Flyway migration was required because `nr_impacto` and `nr_viabilidade` already
exist. At Task 4.1 completion the method deliberately left `prioridade` unchanged;
Task 8.3 subsequently connected the final deterministic calculation. Five service
tests cover both valid boundaries, null/out-of-range rejection, audit ordering,
failed-save behavior, and the later recalculation trigger. Controller tests cover
`GESTOR` success, `OPERACIONAL` and
`LIDER` denial, and invalid-payload `400`; the authorization inventory also covers
the new endpoint. The focused run completed 23 tests with zero failures and zero
errors. The complete backend run completed 68 tests: 67 passed, and the pre-existing
`AguiaXApplicationTests.contextLoads` retained its documented unresolved
`${DB_URL}` test-configuration error. The Task 4.1 exit criterion is satisfied;
Android review controls remain Task 9.1.

---

## Task 4.2 — Define allowed idea status transitions

Prevent arbitrary transitions.

Suggested model:

```text
NOVA -> EM_ANALISE
EM_ANALISE -> PRIORIZADA
EM_ANALISE -> APROVADA
EM_ANALISE -> REJEITADA
PRIORIZADA -> APROVADA
PRIORIZADA -> REJEITADA
APROVADA -> CONVERTIDA_EM_PROJETO
```

Adjust only if current business rules require a different flow.

### Exit criterion

Invalid transitions are rejected with a controlled domain/API error.

### Task 4.2 record — 2026-09-10

Implemented and verified by Codex. `IdeiaService` now owns an explicit immutable
transition map. The manager-facing `PATCH /ideias/{id}/status` route accepts only
`NOVA -> EM_ANALISE`, `EM_ANALISE -> PRIORIZADA/APROVADA/REJEITADA`, and
`PRIORIZADA -> APROVADA/REJEITADA`. Repeating the current status, moving backward,
or moving from a terminal state is rejected before status lookup, authentication
lookup, MySQL mutation, or audit recording.

`APROVADA -> CONVERTIDA_EM_PROJETO` remains part of the domain lifecycle, but the
public status route deliberately rejects direct conversion. Task 4.3 must perform
that transition only while creating the corresponding project transactionally.
Invalid requests use `InvalidRequestException`, which the existing global advice
maps to a structured `400 Bad Request` response.

Sixteen new service test cases cover every manager-allowed transition,
representative invalid and repeated transitions, terminal states, missing input,
direct-conversion rejection, persistence, and audit metadata. A controller test
verifies the structured `400` contract. The focused run completed 35 tests with
zero failures and zero errors. The complete backend run completed 85 tests: 84
passed, while the pre-existing `AguiaXApplicationTests.contextLoads` retained
its documented unresolved `${DB_URL}` test-configuration error. The Task 4.2 exit
criterion is satisfied; Task 4.3 remains responsible for transactional conversion.

---

## Task 4.3 — Enforce approved-idea project creation

When creating a project from an idea:

- validate idea;
- validate allowed status;
- validate one-project-per-idea rule;
- create project;
- change idea status to `CONVERTIDA_EM_PROJETO`;
- audit operation.

Use a transaction for relational changes.

### Tests

- approved idea → project created;
- ineligible idea → rejected;
- second project for same idea → rejected;
- idea status updated after successful project creation.

### Exit criterion

Idea-to-project conversion is a consistent business transaction.

### Task 4.3 record — 2026-09-10

Implemented and verified by Codex. `ProjetoService.criarProjeto` now declares a
Spring `@Transactional` boundary for the two MySQL changes. Creation first resolves
the idea, preserves the existing service and database uniqueness checks, and uses
the Task 4.2 transition policy to accept only `APROVADA ->
CONVERTIDA_EM_PROJETO`. It then creates the `PLANEJADO` project, updates the idea
status and timestamp through `IdeiaService`, and flushes the relational persistence
context before requesting the success audit event.

The existing Flyway unique constraint on `t_projetos.id_ideia` and the matching JPA
one-to-one mapping already enforce one project per idea, so no migration was
required. The `PROJECT_CREATED` audit metadata now includes the previous and new
idea statuses in addition to the idea, responsible user, and initial project
status. If project insertion, idea update, or relational flush fails, the runtime
exception rolls back the MySQL transaction and no success audit is requested.
MongoDB audit persistence remains best-effort and outside the relational transaction
as defined in the architecture.

Eleven new conversion tests cover successful creation/status conversion, all five
ineligible idea states, duplicate conversion, project-save failure, idea-save
failure, flush failure, transaction declaration, operation ordering, and audit
metadata. Together with the existing project-audit and Task 4.2 tests, the focused
run completed 31 tests with zero failures and zero errors. The complete backend run
completed 96 tests: 95 passed, while the pre-existing
`AguiaXApplicationTests.contextLoads` retained its documented unresolved
`${DB_URL}` test-configuration error. The Task 4.3 exit criterion is satisfied.

---

# Phase 5 — Fix known integration defects

## Task 5.1 — Fix idea status DTO mismatch

Resolve backend/client inconsistency between:

```text
statusIdeia
```

and:

```text
status
```

Prefer one stable API contract.

Update Android only if required by the chosen contract.

### Exit criterion

All idea statuses render correctly in Android with no fallback to `NOVA` caused by field mismatch.

### Task 5.1 record — 2026-09-10

Implemented and verified by Codex. The existing backend response field
`statusIdeia` remains the canonical public contract, avoiding a breaking backend
rename. Android's `IdeiaDto` now reads that exact field and maps it to the domain
model's `status` property. The previous silent `status ?: "NOVA"` substitution was
removed. Because Gson can represent an absent response field as null, the mapping
boundary now rejects a missing `statusIdeia` explicitly instead of displaying
incorrect domain state.

Two Android contract tests deserialize and map all six lifecycle values (`NOVA`,
`EM_ANALISE`, `PRIORIZADA`, `APROVADA`, `REJEITADA`, and
`CONVERTIDA_EM_PROJETO`) and verify that a response without `statusIdeia` fails
without fallback. The complete Android unit run passed 6 of 6 tests, and
`assembleDebug` completed successfully. No backend or database change was required.
The Task 5.1 exit criterion is satisfied.

---

## Task 5.2 — Fix dashboard "projects in progress" bug

Ensure "projects in progress" counts only the intended status, not all projects.

### Exit criterion

Displayed value matches backend/domain status semantics.

### Task 5.2 record — 2026-09-10

Implemented and verified by Codex. `DashboardUiState` now keeps
`projetosEmAndamento` separate from `totalProjetos`. The current Android summary
counts only projects whose domain status is exactly `EM_ANDAMENTO`, and
`DashboardScreen` passes that dedicated value to the card labelled "Projetos em
andamento". Other project statuses no longer inflate the displayed metric.

Three focused unit tests cover mixed statuses, zero projects in progress, and a
collection in which every project is in progress. The complete Android unit suite
passed 9 of 9 tests, and `assembleDebug` completed successfully. This is the
required tactical defect correction; Phase 6 still owns moving authoritative
dashboard aggregation and financial calculations to the backend. The Task 5.2
exit criterion is satisfied.

---

## Task 5.3 — Fix earned/available badge overlap

If retained for Sprint 2 delivery, ensure badges already earned are not incorrectly presented as available.

### Exit criterion

Earned and available badge sections are logically consistent.

### Task 5.3 record — 2026-09-10

Implemented and verified by Codex. The retained Android badge screen now compares
the active catalog's `Badge.id` values with the authenticated user's earned
`UsuarioBadge.badgeId` values. `BadgeViewModel` exposes only unearned active badges
as `badgesDisponiveis`, so an earned badge cannot also appear in the available
section.

Progress values are stored separately from the remaining list: the denominator is
the complete active catalog and the numerator is the number of distinct active
badge IDs earned by the user. Historical awards for inactive badges remain visible
under "Minhas conquistas" but do not inflate active-catalog progress. Four new unit
tests cover partial overlap, all badges earned, no badges earned, and an inactive
historical award. The complete Android unit suite passed 13 of 13 tests, and
`assembleDebug` completed successfully. No backend or database change was required.
The Task 5.3 exit criterion is satisfied, completing Phase 5.

---

# Phase 6 — Backend dashboard

## Task 6.1 — Create dashboard response DTO

Suggested metrics:

```text
totalProjects
projectsByStatus
totalInvestment
totalFinancialReturn
totalCostReduction
estimatedResult
averageRoi
averageProductivityGain
```

Use `BigDecimal` for monetary values.

### Exit criterion

A stable dashboard response contract exists.

### Task 6.1 record — 2026-09-11

Implemented and verified by Codex. `DashboardSummaryResponse` is a Java record in
the existing `dto.dashboard` package with the eight documented JSON fields.
Project totals and status counts use `long`; all monetary and average decimal
metrics use `BigDecimal`.

The response defensively copies its status map, exposes it as immutable, and
zero-fills all values of `NomeStatusProjeto` so clients receive a predictable
shape even when a status has no projects. Construction rejects negative counts,
missing decimal metrics, and a `totalProjects` value that differs from the sum of
the status counts.

Four focused tests verify JSON field names and values, complete/immutable status
counts, and invalid-contract rejection. The focused suite passed 4 of 4 tests.
The complete backend suite passed 99 of 100 tests; only the previously documented
`AguiaXApplicationTests.contextLoads` error remains because `${DB_URL}` is
not populated by the application's main-method `.env` bootstrap during
`@SpringBootTest`. No aggregation service, HTTP endpoint, authorization rule, or
Android integration was added early. The Task 6.1 exit criterion is satisfied.

---

## Task 6.2 — Implement global dashboard aggregation

Create:

```http
GET /api/dashboard/summary
```

Move calculation logic from Android into the backend.

### Exit criterion

The backend returns all global dashboard metrics from persisted project data.

### Task 6.2 record — 2026-09-11

Implemented and verified by Codex. `DashboardService.buscarResumo(...)` loads
the persisted projects through `ProjetoRepository` inside a read-only transaction
and returns the Task 6.1 contract. The backend now owns status counts, investment,
financial return, cost reduction, estimated result, average ROI, and average
productivity gain. Estimated result is `return + reduction - investment`;
averages include every project and use two-decimal `HALF_UP` rounding. Legacy
`null` numeric fields are treated as zero, and an empty database returns a complete
zero-valued summary.

`DashboardController` exposes `GET /api/dashboard/summary`. It is leader-only from
its introduction so the executive endpoint never has an insecure intermediate
state; Task 6.5 remains responsible for confirming the same policy across every
dashboard route introduced by later tasks. No orientation filter or
project-specific endpoint was added early.

Three service tests cover exact mixed-project aggregates, the empty dataset, null
legacy values, a negative estimated result, and average rounding. Three MVC tests
cover the response contract plus `401`, `403`, and leader access. The combined
dashboard suite passed 10 of 10 tests. The complete backend suite passed 105 of
106 tests; only the previously documented `contextLoads` `${DB_URL}` setup error
remains. The Task 6.2 exit criterion is satisfied.

---

## Task 6.3 — Add strategic-orientation filter

Support a request such as:

```http
GET /api/dashboard/summary?orientationId={id}
```

### Exit criterion

A leader can view metrics scoped to one strategic orientation.

### Task 6.3 record — 2026-09-11

Implemented and verified by Codex. `GET /api/dashboard/summary` now accepts the
optional `orientationId` query parameter. Omitting it preserves the Task 6.2
global response. Supplying it uses the existing project-to-idea-to-orientation
relationship and a Spring Data nested-property query to load only projects for
the requested orientation before applying the same authoritative formulas.

The service verifies that the orientation exists: a valid orientation without
projects returns the normal complete zero summary, while an unknown ID returns the
centralized `404` response. Deactivated orientations remain available for
historical reporting. The existing `LIDER` method-security rule protects both
global and filtered calls.

Three added service tests cover exact filtered aggregates, an existing orientation
with no projects, and an unknown orientation without a project query. Two added
MVC tests verify query-parameter delegation and the controlled `404` contract.
The combined dashboard suite passed 15 of 15 tests. The complete backend suite
passed 110 of 111 tests; only the previously documented `contextLoads` `${DB_URL}`
setup error remains. Repository-backed fixture coverage is intentionally retained
for the broader Task 11.4 dashboard integration suite. The Task 6.3 exit criterion
is satisfied.

---

## Task 6.4 — Add project-specific result endpoint if needed

Example:

```http
GET /api/dashboard/projects/{projectId}
```

Avoid duplicating existing project endpoints unless a dashboard-specific DTO adds value.

### Exit criterion

Sprint requirement for project-specific result visualization is supported by an API contract.

### Task 6.4 record — 2026-09-11

Implemented and verified by Codex through the task's explicit conditional path.
No new dashboard endpoint or DTO was added because the existing authenticated
`GET /projetos/{id}` contract already returns the complete project-specific result:
linked idea, responsible user, status, descriptive fields, investment, ROI,
financial return, cost reduction, productivity gain, delivery dates, and audit
timestamps. A second `GET /api/dashboard/projects/{projectId}` route would only
duplicate that contract.

The Android client already consumes the endpoint through `ProjetoApi`,
`ProjetoRepository`, and `ProjetoDto`, and the project screen renders its result
metrics. The existing policy also permits a `LIDER` to inspect project results
while requiring authentication for all project reads.

Three focused MVC tests now lock down unauthenticated `401`, a leader's complete
`200` response contract, and the centralized `404` response for an unknown project.
Together with the dashboard tests, the focused suite passed 18 of 18 tests. The
complete backend suite passed 113 of 114 tests; only the previously documented
`contextLoads` `${DB_URL}` setup error remains. The Task 6.4 exit criterion is
satisfied without duplicating production code.

---

## Task 6.5 — Restrict dashboard access

Dashboard endpoints should be accessible only to the intended role, preferably `LIDER`.

### Exit criterion

Unauthorized roles receive `403`.

### Task 6.5 record — 2026-09-11

Implemented by Codex. The existing `LIDER` rule was promoted from the summary
method to a class-level `@PreAuthorize` rule on `DashboardController`. This keeps
the current behavior and makes every future handler under `/api/dashboard`
leader-only by default. Global authentication in `SecurityConfig` continues to
produce `401` before method authorization for callers without a valid token.

The MVC suite now explicitly verifies that both `OPERACIONAL` and `GESTOR`
receive `403` for the global summary and the orientation-filtered summary, that
unauthenticated access receives `401`, and that `LIDER` receives `200`. It also
verifies that denied calls never reach `DashboardService`. A structural test pins
the controller-level role expression to `hasRole('LIDER')`. The focused dashboard
and authorization-policy suite passed 9 of 9 tests. The complete backend suite
passed 115 of 116 tests; only the previously documented `contextLoads` `${DB_URL}`
setup error remains. The Task 6.5 exit criterion is satisfied, completing Phase 6.

---

# Phase 7 — AI foundation

## Task 7.1 — Choose provider

Preferred options:

- Google Gemini API;
- OpenAI API.

Selection criteria:

- free/academic availability;
- structured JSON response support;
- simple REST/Java integration;
- reliability;
- documentation.

Do not build a chatbot.

### Exit criterion

One provider is chosen and its configuration contract is documented.

### Task 7.1 record — 2026-09-11

Implemented by Codex as an architecture and configuration-contract decision. The
selected provider is the Google Gemini Developer API free tier. The pinned model
is the supported stable `gemini-3.6-flash`, chosen for free-tier availability,
Flash latency/cost characteristics, structured JSON support, and a context window
that is more than sufficient for idea evaluation.

The developer's original `gemini-1.5-flash` preference could not be used because
Google shut that model down on 2025-09-29. The documented context capacity is the
current model's 1,048,576-token input limit, not “2 million parameters.” Likewise,
15 RPM is not treated as a fixed application guarantee: Gemini limits vary by
model and project and must be checked in Google AI Studio.

Architecture and operational documentation now define the provider, explicit
model ID, REST base URL and operation, API-key header, safe disabled default, and
planned environment variables/timeouts. No provider client, dependency, live key,
or application configuration was added early; those remain Tasks 7.3–7.4. The
Task 7.1 exit criterion is satisfied.

---

## Task 7.2 — Define AI evaluation contract

Input context should include relevant idea data:

```text
title
problem
proposed solution
category
strategic orientation
manager impact score if available
manager viability score if available
```

Output should be structured:

```text
strategicAlignment: 0..10
impact: 0..10
feasibility: 0..10
innovation: 0..10
overallScore: 0..10
summary
```

### Exit criterion

AI input/output schema is explicit before provider code is written.

### Task 7.2 record — 2026-09-12

Implemented by the developer and reviewed/fixed by Codex. Three immutable,
provider-independent Java records now define the boundary under `dto.ai`:
`AiIdeaEvaluationContext` for trusted idea context,
`AiIdeaEvaluationResult` for validated model scores, and
`AiIdeaEvaluationResponse` for the future flattened HTTP response.

Required context strings are trimmed and rejected when blank. Problem, proposed
solution, and manager scores remain optional for compatibility with current and
legacy ideas; present manager scores must be within `0..10`. Model dimension
scores are required `Integer` values in `0..10`, `overallScore` is a required
`BigDecimal` in `0..10`, and the summary is required and trimmed. The response
accepts only a positive application-owned `ideaId`, so Gemini cannot control the
resource identity. Review normalized a null-result factory call to the same
`IllegalArgumentException` contract used by other validation failures.

Eight focused tests cover normalization, nullable legacy context, all required
context fields, manager-score boundaries, valid structured results, missing and
out-of-range model fields, the exact seven-field JSON response, and trusted-ID/
null-result rejection. The focused suite passed 8 of 8 tests. No Gemini envelope,
HTTP client, persistence, controller, or Android code was added early. The Task
7.2 exit criterion is satisfied. The complete backend suite passed 123 of 124
tests; only the previously documented `contextLoads` `${DB_URL}` setup error
remains.

---

## Task 7.3 — Implement AI provider client

Create an abstraction such as:

```text
AiIdeaEvaluationClient
```

and a concrete implementation for the chosen provider.

Responsibilities:

- create provider request;
- call external API;
- apply timeout;
- parse structured response;
- validate numeric bounds;
- map into application DTO.

Do not expose raw provider response directly to Android.

### Exit criterion

A service test can obtain a valid mapped evaluation using a mocked provider response.

### Task 7.3 record — 2026-09-12

Implemented by the developer and reviewed by Codex. Application code now depends
on `AiIdeaEvaluationClient`, while `GeminiAiIdeaEvaluationClient` owns Gemini's
request envelope, HTTPS call, API-key header, connection/read timeouts, structured
JSON schema, provider-response parsing, and mapping to the validated
`AiIdeaEvaluationResult`. Provider payloads remain internal, and timeout, rate
limit, rejected request, unavailable provider, and invalid response conditions
are represented by controlled integration failures.

Seven mocked-provider tests pass. They verify the endpoint, authentication header,
structured request/schema, valid application mapping, invalid numeric bounds,
malformed output without raw-response disclosure, incomplete generation, `429`,
`5xx`, and timeout behavior. Together with the Task 7.2 contract suite, 15 focused
AI tests pass. Runtime bean/property binding remains deliberately assigned to
Task 7.4, and endpoint orchestration and persistence remain Phase 8 work. The
complete backend run passed 130 of 131 tests; only the pre-existing `contextLoads`
`${DB_URL}` configuration error recorded in the regression baseline remains.

### Task 7.3 runtime correction — 2026-09-12

A real Task 8.2 endpoint request exposed `INVALID_RESPONSE` because the Gemini 3.6
Flash request capped combined thinking and visible output at 512 tokens. The
client now requests `thinkingLevel=medium`, a deliberate product choice that gives
the idea evaluation more reasoning room, and raises `maxOutputTokens` to 4096.
The ceiling does not force consumption of 4096 tokens; it prevents reasoning from
exhausting the response budget before the small JSON result is produced.

Provider-response rejection logs now contain only safe structural diagnostics:
finish reason, missing response section, or validation exception type. They never
contain the API key, prompt, raw provider body, or generated summary. The existing
seven provider tests verify the new request fields and retain the `MAX_TOKENS`
regression case.

---

## Task 7.4 — Secure provider configuration

Use environment variables such as:

```text
AI_API_KEY
AI_MODEL
AI_BASE_URL
```

where applicable.

Never commit API keys.

### Exit criterion

Repository contains no real AI credentials and application can load configuration safely.

### Task 7.4 record — 2026-09-12

Implemented by the developer and reviewed by Codex. `AiProviderProperties` binds
the external `AI_*` contract through `app.ai`, and `AiClientConfiguration`
registers `GeminiAiIdeaEvaluationClient` only when `AI_ENABLED=true`. AI remains
disabled by default, so ordinary startup does not require a Gemini credential.
Enabled configuration fails fast for a missing key, unsupported provider,
non-positive timeout, or non-HTTPS provider URL. The existing `.env` bridge loads
all AI variables, `.env.example` documents only safe placeholders, and the real
`.env` remains ignored.

Seven isolated configuration tests pass, covering disabled startup without a key,
disabled isolation from unused invalid settings, environment-variable overrides,
Gemini bean creation, and enabled-mode validation. The combined Task 7.2–7.4 AI
suite passes 22 tests. The complete backend run passes 137 of 138 tests; only the
pre-existing `contextLoads` `${DB_URL}` configuration error remains.

---

# Phase 8 — AI-assisted analysis and idea prioritization

## Task 8.1 — Add AI evaluation endpoint

Preferred endpoint:

```http
POST /api/ideas/{ideaId}/ai-evaluation
```

Recommended authorization:

- `GESTOR`.

Behavior:

1. load idea;
2. build structured context;
3. call provider;
4. validate output;
5. calculate/store normalized application score;
6. persist evaluation history in MongoDB;
7. audit request/result;
8. return application DTO.

### Exit criterion

A manager can request AI analysis for an idea and receive structured scores.

### Task 8.1 record — 2026-09-12

Implemented the callable AI-evaluation slice. `GESTOR` can invoke
`POST /api/ideas/{ideaId}/ai-evaluation`; the application loads the trusted MySQL
idea, category, strategic orientation, and manager scores, invokes the
conditionally configured provider client, and returns the validated application
DTO. `OPERACIONAL` and `LIDER` receive `403`, unauthenticated requests receive
`401`, and a missing idea receives `404`.

The orchestration writes `AI_EVALUATION_REQUESTED`,
`AI_EVALUATION_COMPLETED`, or `AI_EVALUATION_FAILED` audit events with the
authenticated manager as actor. Disabled AI and classified provider failures map
to controlled `502`, `503`, or `504` responses as appropriate. Failure logs and
audit metadata contain only idea/provider/model/failure classification data; the
API key and raw provider body are not exposed. Neither success nor failure changes
the idea status or creates a project.

The behavior list above spans the Tasks 8.1–8.4 idea-evaluation pipeline. MongoDB
evaluation history is owned by Task 8.2 and was subsequently connected to this orchestration;
calculation/persistence of the deterministic application priority was subsequently
implemented by Task 8.3. Keeping score calculation in its dedicated task avoided
storing an undocumented formula. The Task 8.1 exit criterion is met by the endpoint and
structured response.

Five service tests and eleven MVC/security test invocations pass. The complete
backend run completed 154 tests: 153 passed and only the pre-existing
`AguiaXApplicationTests.contextLoads` unresolved `${DB_URL}` setup error
remained.

---

## Task 8.2 — Persist AI evaluation history in MongoDB

Suggested document:

```text
id
ideaId
requestedByUserId
provider
model
strategicAlignment
impact
feasibility
innovation
overallScore
summary
createdAt
```

Optionally store prompt/version metadata if useful for traceability.

### Exit criterion

Multiple evaluations of the same idea can be retained historically.

### Task 8.2 record — 2026-09-12

Implemented immutable MongoDB evaluation history in the
`idea_ai_evaluations` collection. Each successful provider result receives a new
UUID and stores the trusted idea ID, authenticated requesting user ID,
provider/model, four dimension scores, overall `BigDecimal` score, summary, and
UTC `Instant`. The document constructor reuses the application result validation,
so invalid scores or blank traceability fields cannot be persisted.

`AiIdeaEvaluationService` now persists the history after provider validation and
before emitting `AI_EVALUATION_COMPLETED`. Each request inserts a new document;
the repository can retrieve all evaluations for an idea ordered newest-first. No
history-read HTTP endpoint was added because Sprint 2 does not require one and the
authorization matrix specifies a policy only if such a route is exposed.

MongoDB data-access failure is translated into a controlled `503 Service
Unavailable`, logged without provider content or credentials, and followed by the
`AI_EVALUATION_FAILED` audit path. The service does not return a successful
evaluation when its required history cannot be stored.

Six service tests, twelve MVC/security invocations, and one real MongoDB repository
integration test pass in the focused run (19 total). The repository test persists
two evaluations for idea `42`, verifies that both remain present in newest-first
order, and drops its isolated test database afterward. The complete backend run
completed 157 tests: 156 passed and only the pre-existing
`AguiaXApplicationTests.contextLoads` unresolved `${DB_URL}` setup error
remained.

---

## Task 8.3 — Define deterministic priority formula

Combine human/business signals using this exact application-owned rule:

```text
normalizedVotes = min(max(totalVotes, 0), 10)

priorityScore =
    normalizedVotes * 0.20
  + impactScore     * 0.25
  + viabilityScore * 0.20
  + aiScore         * 0.35
```

Impact, viability, and AI inputs are constrained to the `0..10` scale. Missing
impact, viability, or AI values contribute zero. `aiScore` comes from the most
recently completed AI evaluation for the idea. The fixed ten-vote cap keeps an
idea's result independent from voting activity on unrelated ideas.

Round only the final result to two decimal places using `HALF_UP`. Recalculate
after vote creation, vote removal, manager review, and successful AI evaluation.
Keep the formula in application code/config, not in the LLM prompt, and never use
the calculated value to approve, reject, create a project, or change workflow
status.

### Exit criterion

The same stored inputs always produce the same `priorityScore`.

### Task 8.3 scoring decision — 2026-09-12

The formula contract was documented before implementation: weights, fixed vote
normalization, missing-value behavior, final rounding, latest completed AI-score
selection, recalculation triggers, and the prohibition on workflow mutation are
explicit.

### Task 8.3 implementation record — 2026-09-12

Implemented and reviewed. `IdeaPriorityCalculator` owns the exact `BigDecimal`
formula and rounds only its final result to two decimals with `HALF_UP`.
`IdeaPriorityService` retrieves the latest successful MongoDB evaluation, treats
an absent evaluation as zero, applies the score without touching workflow state,
and converts MongoDB read failures into a controlled `503` condition.

Flyway migration `V12__make_idea_priority_decimal.sql` changes the existing MySQL
priority column to `DECIMAL(4,2)` without editing an applied migration. Manager
review, vote creation, vote removal, and successful AI evaluation all recalculate
the score. Vote writes and their derived idea update are transactional. AI history
is stored before the MySQL score; priority-persistence failure produces a
controlled `503`, a failed audit classification, and no completed audit.

Seven calculator tests cover the exact weights, missing inputs, both vote bounds,
maximum score, final `HALF_UP` rounding, repeatability, and invalid score ranges.
Service tests cover latest/absent AI history, MongoDB failure, both vote triggers,
manager review, the AI trigger, persistence ordering, controlled HTTP failure, and
unchanged workflow status. A MongoDB integration test also verifies stable ID
tie-breaking when evaluation timestamps are equal. The focused Task 8.3 suite
passed 44 of 44 tests. The
complete backend run executed 177 tests: 175 passed, including every Task 8.3
test; the unrelated
Gemini thinking-level source/test mismatch produced one failure, and the previously
documented unresolved `${DB_URL}` `contextLoads` defect produced one error. The
Task 8.3 exit criterion is satisfied.

---

## Task 8.4 — Keep approval human-controlled

The AI endpoint must never autonomously:

- approve;
- reject;
- create a project;
- change workflow status.

### Exit criterion

All workflow status decisions remain explicit manager actions.

### Task 8.4 implementation record — 2026-09-12

Implemented. The AI endpoint remains an advisory operation: it returns structured
scores and a summary, stores the evaluation history, and updates only the derived
priority. Its response exposes no approval, rejection, workflow-status, or project
field. Approval/rejection still requires the separate manager-only
`PATCH /ideias/{id}/status` command, while project creation still requires the
separate manager-only `POST /projetos` command and an already `APROVADA` idea.

The AI orchestration tests now allow-list `IdeiaService` interactions: lookup is
always permitted and priority update is permitted only after a successfully stored
evaluation. They verify unchanged status on success and every controlled failure
path. The MVC contract test also proves that governance and project fields are not
returned by the AI endpoint. Existing idea-transition and project-conversion tests
continue to verify that those decisions occur through their explicit manager
workflows. The focused Task 8.4 verification passed 54 of 54 tests.

---

## Task 8.5 — Generate executive dashboard insights with AI

Add a leader-only, non-chatbot endpoint:

```http
POST /api/dashboard/insights[?orientationId={id}]
```

The request has no body. The backend must load the authoritative global or
orientation-filtered `DashboardSummaryResponse`, reject a zero-project snapshot,
ask the configured Gemini adapter for structured pt-BR analysis, validate the
result, and return the exact metrics snapshot alongside:

- executive summary;
- one to three highlights;
- zero to three attention points;
- one to three actionable improvement suggestions;
- application-owned scope and generation timestamp.

Persist every successful result in MongoDB collection
`dashboard_ai_insights`, including requester, scope/filter, complete metrics
snapshot, validated output, provider/model, and UTC timestamp. Audit requested,
completed, and failed outcomes without storing prompts, provider bodies,
generated text, credentials, or authorization headers. Reuse the existing AI
configuration and `thinkingLevel=low`; do not add a second provider key.

The AI is advisory. It must not update ideas, projects, orientations, users,
scores, or statuses. It must use only backend-supplied metrics, identify missing
business context as something to investigate, and never invent causal
explanations or forecasts. The controlled error contract is `400` for an empty
snapshot, `404` for an unknown orientation, `502` for rejected/malformed output,
`503` for disabled/rate-limited/unavailable provider or MongoDB failure, and
`504` for provider timeout; normal security behavior remains `401`/`403`.

### Exit criterion

A `LIDER` can request a validated, persisted, traceable pt-BR executive analysis
of the exact backend dashboard snapshot. Other roles are denied, failure paths
are controlled, and no business state is changed.

### Task 8.5 contract record — 2026-09-16

Contract documented in architecture section 17.9.

### Task 8.5 contract-model implementation record — 2026-09-16

Implemented the provider-independent backend foundation: validated global and
orientation contexts, the exact `pt-BR` structured result with bounded immutable
lists, the application-owned HTTP response with its trusted metrics snapshot, and
the traceable `DashboardAiInsight` MongoDB document/repository for collection
`dashboard_ai_insights`. Nine focused tests verify scope invariants, zero-project
rejection, normalization, size/length limits, defensive copies, stable response
serialization, traceability fields, and the collection name.

After this contract-model step, Task 8.5 remained incomplete. The provider
adapter, orchestration service, secured endpoint, audit integration, controlled
provider/persistence failures, and broader integration tests were still pending.

### Task 8.5 provider-client implementation record — 2026-09-16

Implemented `AiDashboardInsightClient` and its Gemini adapter. The adapter sends
only the validated dashboard context, requests the documented structured JSON,
uses `thinkingLevel=low`, validates the application result, and classifies
malformed/incomplete output, rate limits, outages, rejected requests, and timeouts.
It reuses a new internal `GeminiStructuredOutputClient` with the existing idea
adapter, preserving the idea-evaluation contract while removing duplicate Gemini
transport and envelope parsing. Both adapters are conditionally registered from
the same `AI_*` configuration; no new environment variable was introduced.

The provider schema uses only supported JSON Schema keywords for enum and array
bounds. The application record remains authoritative for the 800/300-character
limits. Thirty focused contract, configuration, existing idea-client regression,
and dashboard-client tests pass. The complete backend suite passes 235 tests with
the opt-in MySQL milestone test skipped. At this provider-client step, Task 8.5
was still partial: service orchestration, the secured endpoint, persistence
execution, audit events, HTTP error mapping, and integration tests remained.

### Task 8.5 backend-orchestration implementation record — 2026-09-16

Implemented `DashboardInsightService` and the LIDER-only
`POST /api/dashboard/insights[?orientationId={id}]` route. The service always
loads `DashboardService.buscarResumo(orientationId)`, rejects a zero-project
snapshot before authentication/provider work, sends the trusted snapshot to the
conditional dashboard AI client, persists the validated result in
`dashboard_ai_insights`, and only then returns the application-owned response.
The operation is read-only with respect to MySQL business state.

Added typed requested/completed/failed audit events using one analysis UUID and
safe scope/provider metadata. Disabled AI and MongoDB history failures map to
controlled `503`; the shared provider classification preserves `502`, `503`, and
`504`, while unknown orientations remain `404`. No prompt, provider body,
generated text, credential, JWT, or authorization header is placed in logs or
audit metadata.

Seven service tests, six new MVC/security cases, the enhanced OpenAPI contract
test, and one real MongoDB repository integration test verify trusted context,
persistence/audit ordering, global and filtered responses, role enforcement,
empty/unknown filters, controlled failures, safe diagnostics, collection naming,
and complete history data. The 37-test focused contract/client/service/MVC/OpenAPI
suite and the isolated MongoDB persistence test pass. The Task 8.5 backend exit
criterion is satisfied; the complete backend suite passes 249 tests with the
existing opt-in MySQL milestone test skipped. Android presentation remains Task
9.5.

---

# Phase 9 — Android Sprint 2 integration

## Task 9.1 — Integrate manager review scores

Add/update Android UI to let a `GESTOR` enter:

- impact;
- viability.

Use real backend API calls.

### Exit criterion

Scores survive app restart because they are persisted on the backend.

### Task 9.1 implementation record — 2026-09-13

Implemented by the developer and reviewed/fixed by Codex. Android now maps the
backend `impacto`, `viabilidade`, and decimal `prioridade` fields, sends the exact
`PATCH /ideias/{id}/avaliacao` contract through Retrofit and the repository, and
shows a manager-only review dialog with prefilled values, `0..10` validation,
submission state, and controlled errors. A successful response replaces the idea
in the current list; a later application load reads the same MySQL-backed values
through the normal ideas endpoint. Review does not invoke a status transition.

The review found and fixed an unmatched Compose block that prevented compilation
and made the dialog helper local to the idea card. It also connected the previously
unrendered dialog to its ViewModel state. Nine Task 9.1 tests now cover request
field names, repository request/response mapping, nullable legacy scores, decimal
priority mapping, repository failure propagation, inclusive boundaries, invalid
input, and the GESTOR-only UI capability. The complete Android unit suite passed
22 of 22 tests and
`assembleDebug` completed successfully. The Task 9.1 exit criterion is satisfied;
the final live-device journey remains part of Phases 13 and 14.

---

## Task 9.2 — Integrate AI evaluation

On the appropriate idea detail/review screen:

```text
Analyze with AI
    |
    v
loading
    |
    v
scores + summary
```

Display:

- strategic alignment;
- impact;
- feasibility;
- innovation;
- overall score;
- short explanation.

Avoid chat UI.

### Exit criterion

Android invokes the real external-AI-backed endpoint and displays the structured response.

### Task 9.2 implementation record — 2026-09-13

Implemented by Codex. The manager-only `Analisar com IA` action is available in
the expanded idea details and invokes the real
`POST /api/ideas/{ideaId}/ai-evaluation` backend route through Retrofit and the
idea repository. Android validates and maps the complete structured response into
a provider-independent domain model, then displays strategic alignment, impact,
feasibility, innovation, overall score, and summary in a bounded result dialog.
The flow has explicit loading, retry, and controlled error states and is not a
chat interface.

The client never contacts Gemini directly and never derives or sends a workflow
decision. A successful evaluation refreshes the selected idea from the backend so
the newly calculated priority is visible without treating a refresh failure as an
AI-evaluation failure. Android's HTTP read/write timeout is 30 seconds, which is
longer than the backend's configured 15-second provider timeout and allows the
backend to return its controlled error response first.

Ten Task 9.2 tests cover the structured DTO contract and ranges, absence of
workflow fields, request routing and response identity, repository failure
propagation, GESTOR-only capability, and safe timeout/HTTP/malformed-response
messages. The complete Android unit suite passed 32 of 32 tests and
`assembleDebug` completed successfully. The exit criterion is satisfied at the
implemented and automated-contract level; the final real-key/device journey
remains part of Phases 13 and 14.

---

## Task 9.3 — Integrate backend dashboard

Replace client-side aggregate calculation with backend dashboard calls.

Support:

- global view;
- strategic orientation filter;
- project-specific view where required.

### Exit criterion

Dashboard values come from backend analytics endpoints.

### Task 9.3 implementation record — 2026-09-13

Implemented by Codex. Android no longer downloads the project collection or
calculates executive totals, averages, status counts, or financial results.
`DashboardApi` calls the real `GET /api/dashboard/summary` endpoint,
`DashboardSummaryDto` validates and preserves the backend's `long` counts and
`BigDecimal` values, and `DashboardRepository` exposes the mapped summary to
`DashboardViewModel`.

The dashboard initially requests the global summary. Its strategic-orientation
selector loads the complete orientation catalog and sends the selected ID as the
optional `orientationId` query parameter; selecting “Todas as orientações” omits
that parameter. Empty global and filtered results, stale concurrent responses,
invalid contracts, `401`, `403`, and an unknown-orientation `404` have controlled
UI behavior. The screen displays total projects, the backend-provided
`EM_ANDAMENTO` count, all four status counts, financial totals, estimated result,
average ROI, and average productivity gain without recalculating them.

Project-specific results continue through the existing authenticated
`GET /projetos/{projectId}` Android flow, as decided in Task 6.4; no duplicate
dashboard endpoint was introduced. Thirteen Android dashboard tests cover DTO
mapping and rejection, global and filtered request routing, exact status-count
consumption, safe errors, and repository failure propagation. The complete
Android unit suite passed 42 of 42 tests and `assembleDebug` completed
successfully. The Task 9.3 exit criterion is satisfied; device-level journeys
remain in Phases 13 and 14.

---

## Task 9.4 — Verify role navigation and controls

UI should reflect backend authorization for:

- operational user;
- manager;
- leader.

Remember: UI restrictions are convenience only; backend remains authoritative.

### Exit criterion

Each role sees an appropriate experience and forbidden API calls still fail server-side.

### Task 9.4 implementation record — 2026-09-14

Implemented by Codex. Android now derives navigation and visible controls from one
fail-closed `RoleUiPolicy` instead of repeating independent role comparisons.
Every authenticated role can view orientations, ideas, projects, badges, and vote.
`OPERACIONAL` alone can create ideas and receives edit/delete controls only for
its own ideas; `GESTOR` alone receives idea review, status, AI-evaluation, and
project-management controls; `LIDER` alone receives orientation-management and
executive-dashboard controls. Login now opens the shared role-adaptive Home
(the follow-up below replaces the original workflow-specific start routes).
Unknown roles and sessions without a token receive no protected capability.

The same policy now guards the actual Compose destinations for idea,
orientation, and project creation/editing and for the dashboard. A forbidden
destination does not instantiate its screen or ViewModel, so hidden buttons are
not the sole client-side restriction. These guards remain usability controls:
Spring Security and ownership checks continue to be authoritative.

Fourteen new Android tests cover the three complete role policies, shared
navigation, primary start routes, privileged destination denial,
unauthenticated/unknown-role fail-closed behavior, and own-idea controls. The
complete Android suite passed 56 of 56 tests and `assembleDebug` succeeded. The
focused backend authorization suites passed 47 of 47 checks, including real MVC
`401`, `403`, and allowed responses for project and strategic-orientation routes,
plus the existing idea, AI, dashboard, catalog, user, category, and badge
boundaries. The Task 9.4 exit criterion and the previously scoped Phase 9 work
were complete at this review point. Task 9.5 was implemented in the later record
below, while the broader real-backend device journeys remain in Phases 13 and 14.

## Task 9.5 — Display executive dashboard insights

Extend the existing leader Dashboard with a reusable **"Análise executiva com
IA"** card. A deliberate **"Gerar análise"** action invokes Task 8.5 for the
current global/orientation scope. Display its structured fields under **"Resumo
executivo"**, **"Destaques"**, **"Pontos de atenção"**, and **"Sugestões de
melhoria"**; do not add a prompt field, messages, or chat history.

Keep dashboard-summary loading separate from insight loading/error state. Adopt
the response `metricsSnapshot` as the visible summary, clear a generated insight
when the orientation changes, and prevent stale responses from a prior filter
from replacing current state. Errors, retry labels, loading text, and content
descriptions must be in pt-BR. Continue to guard the destination and controls with
the centralized `LIDER` role policy while treating backend authorization as
authoritative.

### Exit criterion

On the existing Dashboard, a `LIDER` can explicitly generate and read an insight
that matches the displayed metrics and selected orientation. Loading, retry,
filter changes, stale responses, and forbidden roles behave safely without
turning the feature into a chatbot.

### Task 9.5 contract record — 2026-09-16

Implemented by Codex. The existing leader Dashboard now has a reusable
**"Análise executiva com IA"** card backed by
`POST /api/dashboard/insights[?orientationId={id}]`. Generation is deliberate,
uses the current global/orientation scope, and renders only the structured pt-BR
summary, highlights, attention points, and recommendations; no prompt, messages,
or chat history were introduced.

`DashboardViewModel` keeps summary and insight state independent. A generated
response replaces the visible summary with its authoritative `metricsSnapshot`;
provider/application failures preserve the last valid summary. Filter changes
clear the previous insight immediately, and a sequence guard prevents a response
from an older scope from replacing the current selection. The existing
centralized leader policy is checked before the request and remains subordinate
to backend authorization.

The Android client validates scope, orientation identity, complete metrics,
bounded structured text, list sizes, and the generation timestamp before showing
the result. Controlled pt-BR messages cover validation, authorization, empty
scope, provider, availability, and timeout failures, with retry available in the
same card.

Verification on 2026-09-16: the complete Android unit suite passed **124/124**,
the debug APK assembled successfully, Android instrumentation tests compiled,
and the three focused Compose states passed **3/3** on the connected Pixel 6 /
Android 12 emulator. The Task 9.5 exit criterion is satisfied.

### Role-adaptive Home follow-up — 2026-09-15

Implemented by Codex at the developer's request. A shared Home destination now
provides personalized greetings, real role-specific metrics, bounded recent-record
updates, operational idea submission, manager review/project shortcuts, and a
leader executive snapshot linking to the existing full Dashboard. The shared
bottom bar has five destinations; Dashboard remains an independently guarded
leader-only destination accessible from Home. Existing CRUD workflows and backend
authorization are unchanged. Definitions, data sources, unavailable-value policy,
and the current-record activity limitation are documented in architecture section
9.4. This addition does not mark Phase 13's real-backend journeys complete.

Verification: the complete Android unit suite passed **87/87**, the eight Home
Compose tests passed **8/8** on the connected Pixel 6 / Android 12 emulator,
and `assembleDebug` succeeded. Coverage includes personal ownership filtering,
real versus unavailable/empty values, manager counts and attention ordering,
backend-owned executive decimals, role-scoped API requests, expired sessions,
cancellation, five-item timestamp ordering, guarded navigation, role shortcuts,
loading/retry/partial states, and narrow/large-font metric layouts. Generated
operational, manager, and leader layouts were visually inspected. The test APKs
are temporary; the debug app is reinstalled after emulator verification.

### AI thinking and operational milestone awards — 2026-09-15

Implemented by Codex at the developer's request. The Gemini adapter now sends
`thinkingLevel=low`, superseding the earlier medium product choice in Tasks 7.3
and 11.5. The 4096-token ceiling, structured response, validation, timeout handling,
and prohibition on AI status changes remain unchanged; no new environment key is
needed.

This request also revises the earlier manual-only award decision. The backend now
automatically grants the three defined active catalog milestones to the idea's
`OPERACIONAL` author: `PRIMEIRA_IDEIA`, `IDEIA_APROVADA`, and
`PROJETO_IMPLEMENTADO`. Award writes share the successful idea/project transaction;
an atomic MySQL upsert respects unique recipient/badge pairs and original dates.
V13 backfills existing eligible authors without modifying applied migrations.
Manual grants/removal remain `GESTOR`-only and reads retain recipient ownership.
`TOP_COLABORADOR` and `MESTRE_DA_INOVACAO` are not automatically awarded until the
developer supplies explicit numeric thresholds for their ambiguous catalog text.

The retained Android Medals destination now loads on resume rather than only
ViewModel creation. Superseded loads are cancelled and cannot overwrite newer
awards. The existing earned/available filtering and Home refresh behavior remain.
Architecture's badge policy and README record the revised behavior; this follow-up
does not mark the final real-backend Android journeys complete.

Verification: the focused AI/workflow/authorization selection passed 69 tests.
`bash ./mvnw -o test` passed 218 tests with only the new opt-in MySQL test skipped;
that test separately passed with `-Dtest=MilestoneBadgeMySqlIntegrationTest
-Dmysql.test.enabled=true`. It migrated an isolated UUID-scoped MySQL 8 schema
through V12, seeded existing milestones/manual awards, applied V13, and verified
backfill, original dates, duplicate/concurrent grants, inactive/missing definitions,
role filtering, and rollback. Only the newly created test schema was removed.
Android's full unit suite passed 106 tests, `:app:assembleDebug` succeeded, and both
`MedalhasRefreshTest` emulator tests passed (resume refresh and stale-load
suppression). The debug app was reinstalled with `:app:installDebug --offline`.

### Manager workflow/Home regression fix — 2026-09-15

Implemented by Codex at the developer's request. The previous Android card
offered approval/prioritization regardless of current status, even though Task
4.2 correctly rejects direct `NOVA -> APROVADA/PRIORIZADA`. The card now offers
"Iniciar análise" first, appropriate decision actions during analysis, only
approval after prioritization, and no repeated/terminal decision actions.
Per-idea pending state blocks duplicate updates, and the persisted response is
applied before list refresh. Status failures have safe pt-BR feedback; backend
authorization, allowed transitions, and audit behavior are not weakened.

Shared tab navigation now avoids restoring saved child-tab state when Home is
selected, while retaining state restoration for other tabs. This fixes manager
returns from other main tabs and direct Home shortcuts without changing role
policy or creating a separate navigation graph. Architecture sections 9.4 and 12
record these constraints. This targeted regression fix does not mark Phase 13's
real-backend device journeys complete.

Verification: `bash ./gradlew :app:testDebugUnitTest :app:assembleDebug --offline`
passed all 106 Android unit tests and built the debug APK. Eight targeted emulator
tests (`MainRouteNavigationTest`, `IdeaStatusActionsUiTest`) passed on Pixel 6 /
Android 12 via `:app:connectedDebugAndroidTest`. Navigation coverage reproduces
the old failure after the manager's "Revisar ideias" Home shortcut, then verifies
that the corrected options return to Home; other tabs and repeated switches are
also covered. Idea coverage exercises the analysis/prioritization/approval
pipeline, direct approval during analysis, unavailable terminal actions, and
pending-button disabling. Retrofit contract tests preserve the documented PATCH
payload and backend rejection of direct approval from `NOVA`. The existing
backend `IdeiaServiceStatusTransitionTest` and `IdeiaStatusJwtIntegrationTest`
passed all 20 service/JWT checks with
`bash ./mvnw -o -Dtest=IdeiaServiceStatusTransitionTest,IdeiaStatusJwtIntegrationTest test`.
The verified debug app was reinstalled on the emulator with
`bash ./gradlew :app:installDebug --offline` after instrumentation cleanup.

### Android appearance follow-up — 2026-09-15

Implemented by Codex at the developer's request. The existing Material 3 theme
now provides the supplied coral/blue/gold light and dark palettes, layered
surfaces, and shared success/warning/information tokens. Home metrics, actions,
status/profile tags, medals, feedback, buttons, and bottom navigation use these
tokens rather than light-only colors. Home's business structure, API data sources,
and role permissions remain unchanged. The metric grid also checks rendered value
width before using two columns, preventing long financial values from splitting
between digits. Medal progress/earned accents are gold, with mostly neutral
available cards and explicit earned/available labels.

An app-level Claro/Escuro/Sistema menu is available in the shared top bar and
login bar. Preferences DataStore persists the choice across activity recreation
and app restart; explicit choices override Android's system mode. The accessible
darker light-mode coral action tone and theme architecture are documented in
architecture section 9.4 and the README.

Verification: **96/96** Android unit tests and **19/19** connected Compose/device
tests passed on Pixel 6 / Android 12; `assembleDebug` succeeded. Coverage includes
reopening the preference file, explicit/system modes, actual app-bar switching,
activity recreation and system-bar appearance, semantic mappings, text contrast, medal progress bounds,
and Home/medal/status/feedback rendering. Generated layouts were visually reviewed
in both themes. The debug app is reinstalled after test-runner cleanup. This UI
follow-up does not substitute for Phase 13's real-backend device journeys.

### Login visual refinement follow-up — 2026-09-15

Implemented by Codex at the developer's request. Login keeps its existing
authentication wiring while adding a balanced AguiaX hero/welcome area, restrained
theme-token background tint, a 20dp bordered surface card, layered mail/lock
fields, and a 56dp primary action. Password visibility is local/non-persisted,
starts masked, and uses pt-BR accessibility descriptions. The existing loading
state disables submission and displays a readable progress indicator with
"Entrando..."; errors use a polite live region and a safe pt-BR presentation
fallback rather than exposing raw HTTP/network exception messages. Scroll/IME
insets preserve reachability on short screens and with enlarged text.

Dark is now the centralized default for unset/unknown preferences, IO fallback,
initial Compose rendering, and the native launch-window background. Persisted
Light/Dark/System choices are still respected, and the existing theme menu remains
available. No backend, authentication API, session, role, or navigation-flow
changes are introduced. README and architecture section 9.4 describe the updated
appearance contract. This refinement does not mark Phase 13 complete.

Verification: **98/98** Android unit tests passed. The broader **29/29**
Compose/device regression suite passed, followed by **11/11** final focused
login/theme checks after the pt-BR error-presentation refinement. Coverage includes
actual rendered password masking/revealing, masked recreation, credential
callbacks/keyboard actions, loading submission guards, announced/localized errors,
short-screen/large-font scrolling, both color schemes, persisted appearance
choices, and actual app-bar switching/recreation. Dark and light login captures
were visually inspected. `assembleDebug` succeeded; the verified debug app is
reinstalled after emulator test cleanup.

---

# Phase 10 — Observability

## Task 10.1 — Add Spring Boot Actuator

Add/configure:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Do not expose sensitive configuration.

### Exit criterion

Health and metrics endpoints work locally.

### Task 10.1 implementation record — 2026-09-14

Implemented by the developer and reviewed by Codex. The backend includes Spring
Boot Actuator and exposes only `health`, `info`, and `metrics` over HTTP. The
discovery page and all JMX endpoints are disabled. `GET /actuator/health` is
public and returns only the aggregate status; component health subpaths are
denied. Safe application metadata, the metric-name catalog, and individual
metric values require `LIDER`. All other application routes retain their
existing authentication policy.

Five isolated integration tests exercise the real Actuator endpoints through
the real JWT filter and Spring Security chain without requiring MySQL or
MongoDB. They verify public sanitized health, unauthenticated `401`, wrong-role
`403`, leader `200`, built-in JVM metrics, denied health details, and `404` for
unexposed endpoints. The focused suite passed 5 of 5 tests. The exit criterion
is satisfied.

---

## Task 10.2 — Improve application logs

Log meaningful events without secrets.

Include enough context to diagnose:

- authentication failures;
- authorization failures;
- AI provider failures;
- database failures;
- invalid workflow attempts.

Prefer structured parameterized logging.

### Exit criterion

A developer can diagnose a failed request from logs without seeing sensitive credentials.

### Task 10.2 implementation record — 2026-09-14

Implemented by the developer and reviewed/fixed by Codex. Spring Security now
records rejected unauthenticated and forbidden requests with HTTP method, path,
and controlled failure type. The centralized exception handler records rejected
login attempts, service-level authorization failures, invalid workflow attempts,
unexpected Spring data-access failures, and priority persistence/calculation
failures. Database responses remain generic and logs use parameterized SLF4J
messages without raw exception text.

The existing AI orchestration log remains the owner of provider-failure context:
it records idea ID, configured provider, and a controlled failure classification.
Existing audit persistence logs continue to identify event/resource context. No
password, JWT value, Authorization header, database connection detail, AI API
key, prompt, raw provider body, or generated summary is logged.

Sixteen focused tests passed. They include real JWT-filter `401`/`403` requests,
a real rejected login request through MVC, direct status/response verification
for invalid workflow and database failures, and AI failure classification. The
log-capture assertions also inject secret marker values and prove that none are
present in the formatted application messages. The combined observability,
authorization, authentication, AI, and controller regression selection passed
70 of 70 tests. The exit criterion is satisfied.

---

## Task 10.3 — Optional custom metrics

If time permits, add counters/timers for:

- AI evaluations;
- AI failures;
- idea approvals;
- project creations.

Do not let optional metrics block core delivery.

### Exit criterion

At least built-in application metrics exist; custom metrics are a bonus.

### Task 10.3 implementation record — 2026-09-14

Implemented by the developer and reviewed/fixed by Codex. `BusinessMetrics`
registers an AI-evaluation timer split by bounded `success`/`failure` outcomes,
an AI-failure counter, an idea-approval counter, and a project-creation counter.
The AI timer starts only after a valid idea and authenticated actor are resolved.
Approval counts change only for successful transitions to `APROVADA`. Project
counts are transaction-aware and increment after commit, never after rollback.
No identifiers, exception messages, credentials, tokens, or provider payloads are
used as metric tags.

Four focused registry/transaction tests cover timer outcomes, counters, immediate
non-transactional recording, commit deferral, and rollback behavior. Existing AI,
idea-status, project-conversion/audit, ownership/review, and real secured Actuator
tests were updated for the new dependency and metric hooks. The focused selection
passed 65 of 65 tests, including a leader read of a custom metric through
`/actuator/metrics/{name}`. The optional custom-metric work and the Phase 10 exit
criterion are satisfied.

The complete backend run executed 197 tests: 195 passed, while the two previously
documented unrelated baseline defects remained (`contextLoads` does not load
`.env`, leaving `${DB_URL}` unresolved, and the Gemini request test expects
`thinkingLevel=low` while the implementation sends `medium`). No Task 10.3
regression appeared outside the focused selection. Both defects were subsequently
resolved as part of Phase 11.

---

# Phase 11 — Test suite

## Task 11.1 — Security integration tests

Cover:

- `401` without authentication;
- `403` for wrong role;
- permitted role succeeds;
- idea ownership.

### Exit criterion

Critical role/ownership rules are automatically verified.

### Task 11.1 implementation record — 2026-09-14

Implemented and verified by Codex. Existing secured MVC suites cover missing
authentication (`401`), authenticated callers with the wrong role (`403`), and
successful calls by permitted roles. The JWT integration suite now also covers a
correctly signed but expired token. Service tests prove that an `OPERACIONAL`
owner can update/delete their idea while another operational user cannot.

---

## Task 11.2 — Idea workflow tests

Cover:

- valid creation;
- duplicate rules;
- vote uniqueness;
- manager scores;
- valid transitions;
- invalid transitions;
- priority score.

### Exit criterion

Core idea lifecycle is regression-tested.

### Task 11.2 implementation record — 2026-09-14

Implemented and verified by Codex. The idea suites now assert the complete
creation defaults and response, reject a duplicate title before loading related
entities, reject a second vote, reject removal when the caller has no vote, and
retain the existing coverage for manager scores, valid and invalid transitions,
the deterministic priority formula, recalculation triggers, and status
preservation.

---

## Task 11.3 — Project workflow tests

Cover:

- eligibility;
- one project per idea;
- idea status transition;
- financial validation.

### Exit criterion

Project conversion rules are regression-tested.

### Task 11.3 implementation record — 2026-09-14

Implemented and verified by Codex. The existing conversion suite covers every
ineligible idea status, the single-project invariant, transactional project/idea
write ordering, the transition to `CONVERTIDA_EM_PROJETO`, and rollback-sensitive
failure paths. Parameterized MVC tests now prove that each negative financial
input (`investimento`, ROI, return, cost reduction, and productivity gain) is
rejected with `400` before the service is invoked.

---

## Task 11.4 — Dashboard tests

Use known project fixtures and verify exact aggregates.

### Exit criterion

Dashboard numbers are proven correct by automated tests.

### Task 11.4 implementation record — 2026-09-14

Verified by Codex. `DashboardServiceTest` uses known fixtures to assert exact
project totals, status counts, investments, financial return, cost reduction,
estimated result, average ROI, and average productivity gain for global and
orientation-filtered summaries, including empty and legacy-null data.

---

## Task 11.5 — AI tests

Test without calling the real provider in the standard suite.

Cover:

- valid response;
- malformed JSON;
- out-of-range scores;
- timeout/provider error;
- MongoDB persistence;
- AI failure does not change idea status.

### Exit criterion

AI integration failures are controlled and deterministic application behavior is tested.

### Task 11.5 implementation record — 2026-09-14

Implemented and verified by Codex without a real Gemini call. Mocked HTTP/client
suites cover the structured success response, malformed JSON, out-of-range
scores, incomplete output, timeout, rate limiting, and provider failure. MongoDB
integration tests prove evaluation-history persistence and ordering. Service tests
prove controlled persistence failures and that neither successful nor failed AI
evaluation changes idea status. The request contract now asserts the selected
`thinkingLevel=medium` behavior.

---

## Task 11.6 — Audit tests

Verify critical operations create the expected audit events.

### Exit criterion

Governance behavior is covered by automated tests.

### Task 11.6 implementation record — 2026-09-14

Implemented and verified by Codex. Audit suites cover login, idea creation,
update and deletion, orientation mutations, project creation/update/completion,
and AI requested/completed/failed events. Ordering assertions ensure success
events occur only after the relational write, while rejected or failed mutations
do not emit false success events.

### Phase 11 verification — 2026-09-14

The standard backend command `./mvnw test` passes **208 of 208 tests** with local
MongoDB available. The application context smoke test no longer depends on the
`main` method's `.env` loader or external MySQL/MongoDB connections; persistence
integration tests remain explicit and use a test-only MongoDB database. Phase 11
is complete and all six exit criteria are satisfied.

---

# Phase 12 — API and delivery hardening

## Task 12.1 — Review Swagger/OpenAPI

Ensure Sprint 2 endpoints are documented.

Include:

- request DTOs;
- response DTOs;
- authentication requirements;
- validation;
- relevant response codes.

### Exit criterion

All presentation-critical APIs can be understood and tested from Swagger.

### Task 12.1 implementation record — 2026-09-14

Implemented by Codex. OpenAPI now defines an HTTP bearer/JWT scheme globally,
while public login and public operational registration explicitly opt out.
Presentation-critical authentication, idea creation/review/status, voting, AI
evaluation, project conversion/status, dashboard, orientation, and privileged
user-provisioning operations document their role/business rules and relevant
success/error status codes. Request contracts expose Jakarta validation limits
and safe examples; structured AI, dashboard, token, and domain return types are
generated from their response DTOs.

Documentation is disabled by default. The explicit `local` Spring profile enables
`/v3/api-docs` and Swagger UI and is the only profile in which security permits
those GET routes without a JWT. Focused integration tests prove default-profile
`404`, local unauthenticated access, the bearer scheme, public-operation overrides,
critical paths/responses, and score/financial schema bounds. All four focused
tests pass. The complete backend regression suite passes **211 of 211 tests**,
satisfying the Task 12.1 exit criterion.

---

## Task 12.2 — Update Postman collection

Add:

- login;
- role examples;
- manager scoring;
- AI evaluation;
- dashboard;
- project conversion;
- Actuator health where appropriate.

### Exit criterion

A reviewer can demonstrate the main Sprint 2 flow without constructing requests manually.

### Task 12.2 implementation record — 2026-09-14

Exported by the developer and reviewed/fixed by Codex. The active Postman v2.1
collection intentionally combines Sprint 1 and Sprint 2. The original export had
61 requests but embedded 16 bearer tokens, 12 literal passwords, fixed URLs/IDs,
no reusable variables or scripts, an invalid `400/100` manager-score example, a
blank request, and one mislabeled vote request pointing to an idea endpoint.

The corrected collection contains no committed credential values, uses
`{{baseUrl}}` and role/resource variables, and retains the broad endpoint catalog.
A numbered 14-request **Sprint 2 - Fluxo principal** folder logs in all three
roles, creates and captures an orientation and idea, votes, records valid manager
scores, follows `NOVA -> EM_ANALISE -> APROVADA`, requests structured AI analysis,
converts the idea into a project, queries global/filtered dashboards, and verifies
public health. Test scripts assert the principal responses and automatically
capture role JWTs and resource IDs.

`jq` validates the exported JSON. Structural checks find 73 requests, 66 bearer
references all expressed as variables, 15 password fields all expressed as
variables, no hard-coded request host, and no JWT or common API-key signature.
Import/run instructions are versioned beside the collection. Together with the
developer's completed Postman endpoint testing, the Task 12.2 exit criterion is
satisfied.

---

## Task 12.3 — Environment documentation

Document required configuration.

Example categories:

```text
MySQL
MongoDB
JWT
AI provider
Spring profile
```

Provide safe examples only.

### Exit criterion

Another developer can run the backend without receiving private credentials from the repository.

### Task 12.3 implementation record — 2026-09-14

Implemented by Codex. `docs/environment.md` now defines the exact external
configuration contract for MySQL, MongoDB, JWT signing, optional Gemini access,
and Spring profile selection. It documents required versus optional values,
defaults, JVM/environment/`.env` precedence, safe local provisioning, connectivity
and health checks, secret-handling rules, and controlled startup-failure guidance.

The tracked `.env.example` contains placeholders only and explains the required
services and opt-in `local` profile. The real `backend/.env` remains ignored.
Architecture no longer advertises the unused `JWT_EXPIRATION` variable: the
current two-hour token lifetime is accurately documented as fixed in code.
Configuration-key and secret scans plus documentation link checks satisfy the
Task 12.3 exit criterion without reading or exposing the developer's local `.env`.
The isolated application-context verification also passes 2 of 2 tests without
external databases or dotenv configuration.

---

## Task 12.4 — Final README update

Document:

- architecture;
- prerequisites;
- environment variables;
- database startup;
- backend startup;
- Android API URL;
- MongoDB role;
- AI feature;
- role matrix;
- Swagger;
- health endpoint;
- test commands.

### Exit criterion

The repository is self-explanatory enough for evaluator setup.

### Task 12.4 implementation record — 2026-09-14

Implemented by Codex. The root README is reorganized around evaluator setup rather
than implementation chronology. It now contains the active polyglot-persistence
architecture, repository scope, prerequisites, native MySQL/MongoDB startup and
connectivity commands, safe environment setup, backend startup/health check, the
complete role matrix and workflow, Gemini behavior, Android emulator/physical-
device base URLs, Swagger/Postman usage, Actuator access, automated test commands,
and controlled first-leader bootstrap.

Every required Task 12.4 topic is directly discoverable by a README heading or
table. Local-link, documented-path, command-wrapper, configuration-key, safe-
placeholder, and Markdown whitespace checks pass. No application source change
was needed. Together with Tasks 12.1–12.3, this completes Phase 12; live role and
device journeys remain the separate Phase 13 verification gate.

---

# Phase 13 — Final end-to-end verification

Test these scenarios manually.

For each real role, also verify that login opens Home, the greeting uses the
session name, metrics match the API/database, and shortcuts reach the existing
screens. Check refresh after idea creation/editing, partial API failures, empty
accounts, dark theme, narrow screens, and large system text. Verify that Dashboard
remains accessible only to `LIDER`; record snapshots must not claim to be a full
audit history or invent vote/status events.

## OPERACIONAL

1. login;
2. create idea;
3. edit own idea;
4. fail to edit another user's idea;
5. vote;
6. view orientations;
7. view projects.

## GESTOR

1. login;
2. view submitted ideas;
3. set impact/viability;
4. request AI evaluation;
5. view structured AI analysis;
6. prioritize idea;
7. approve idea;
8. create project from approved idea;
9. verify idea becomes `CONVERTIDA_EM_PROJETO`.

## LIDER

1. login;
2. create/update strategic orientation;
3. open dashboard;
4. view global metrics;
5. filter by strategy;
6. inspect project result.

## Governance

Verify MongoDB contains:

- audit events;
- AI evaluation history.

## Observability

Verify:

- `/actuator/health`;
- `/actuator/metrics`;
- meaningful logs;
- no JWT/API key leakage.

### Exit criterion

All three user journeys work from Android through the real Spring Boot API with persisted data and no mocks.

---

# Phase 14 — Sprint 2 completion gate

Sprint 2 is complete only when all of the following are true:

- [ ] Backend remains Java Spring Boot.
- [ ] Android consumes real backend APIs.
- [ ] JWT authentication works.
- [ ] All three roles are implemented.
- [ ] Authorization is enforced server-side.
- [ ] Idea ownership is enforced.
- [ ] Strategic orientation management matches role rules.
- [ ] Idea review/prioritization workflow works.
- [ ] Project conversion workflow is consistent.
- [ ] MySQL remains healthy for relational domain data.
- [ ] MongoDB is actively used by the application.
- [ ] External AI API is integrated.
- [ ] AI feature is non-chatbot and functional.
- [ ] AI evaluation history is persisted.
- [ ] AI does not make final approval decisions.
- [ ] Backend dashboard aggregation works.
- [ ] Dashboard filtering meets Sprint requirement.
- [ ] Audit events are persisted.
- [ ] Logs are useful and do not expose secrets.
- [ ] Actuator health/metrics are available.
- [ ] Known idea-status contract bug is fixed.
- [ ] Critical security tests exist.
- [ ] Critical domain tests exist.
- [ ] AI failure cases are tested.
- [ ] Swagger is updated.
- [ ] Postman collection is updated.
- [ ] README/setup documentation is current.
- [ ] Full end-to-end demo succeeds.

---

# Suggested Codex task cadence

Do not ask Codex to "implement Sprint 2" in one prompt.

Use one roadmap task or one tightly related task group per Codex session.

Recommended pattern:

```text
Read AGENTS.md and roadmap.md.

Implement Phase X, Task X.Y only.

Before editing:
1. inspect the existing implementation;
2. summarize what is already present;
3. list the files you expect to change.

Then implement the smallest coherent solution.

Run relevant tests and compilation.

Do not perform unrelated refactors.

At the end report:
- files changed;
- behavior implemented;
- tests/commands run;
- result;
- remaining issues.

Do not mark the roadmap task complete unless its exit criterion is satisfied.
```

After Codex finishes a task:

1. review diff;
2. run tests yourself;
3. exercise the endpoint manually if it is presentation-critical;
4. commit;
5. continue to the next roadmap task.

This keeps the Sprint 2 implementation incremental, reviewable and recoverable.
