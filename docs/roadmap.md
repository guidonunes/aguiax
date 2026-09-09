# Sprint 2 Roadmap — AguiaExpress

## Goal

Complete Sprint 2 by evolving the existing AguiaExpress Sprint 1 implementation into a secure, observable, API-driven platform with:

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
37 passed and the existing `AguiaexpressApplicationTests.contextLoads` test retained
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
`aguiaexpress_task_2_2_<unique-id>` database. The test asserts the persisted fields
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

---

## Task 5.2 — Fix dashboard "projects in progress" bug

Ensure "projects in progress" counts only the intended status, not all projects.

### Exit criterion

Displayed value matches backend/domain status semantics.

---

## Task 5.3 — Fix earned/available badge overlap

If retained for Sprint 2 delivery, ensure badges already earned are not incorrectly presented as available.

### Exit criterion

Earned and available badge sections are logically consistent.

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

---

## Task 6.2 — Implement global dashboard aggregation

Create:

```http
GET /api/dashboard/summary
```

Move calculation logic from Android into the backend.

### Exit criterion

The backend returns all global dashboard metrics from persisted project data.

---

## Task 6.3 — Add strategic-orientation filter

Support a request such as:

```http
GET /api/dashboard/summary?orientationId={id}
```

### Exit criterion

A leader can view metrics scoped to one strategic orientation.

---

## Task 6.4 — Add project-specific result endpoint if needed

Example:

```http
GET /api/dashboard/projects/{projectId}
```

Avoid duplicating existing project endpoints unless a dashboard-specific DTO adds value.

### Exit criterion

Sprint requirement for project-specific result visualization is supported by an API contract.

---

## Task 6.5 — Restrict dashboard access

Dashboard endpoints should be accessible only to the intended role, preferably `LIDER`.

### Exit criterion

Unauthorized roles receive `403`.

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

---

# Phase 8 — AI-assisted idea prioritization

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

---

## Task 8.3 — Define deterministic priority formula

Combine human/business signals.

Example:

```text
priorityScore =
    normalizedVotes * 0.20
  + impactScore     * 0.25
  + viabilityScore * 0.20
  + aiScore         * 0.35
```

Keep the formula in application code/config, not in the LLM prompt.

### Exit criterion

The same stored inputs always produce the same `priorityScore`.

---

## Task 8.4 — Keep approval human-controlled

The AI endpoint must never autonomously:

- approve;
- reject;
- create a project;
- change workflow status.

### Exit criterion

All workflow status decisions remain explicit manager actions.

---

# Phase 9 — Android Sprint 2 integration

## Task 9.1 — Integrate manager review scores

Add/update Android UI to let a `GESTOR` enter:

- impact;
- viability.

Use real backend API calls.

### Exit criterion

Scores survive app restart because they are persisted on the backend.

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

---

## Task 9.3 — Integrate backend dashboard

Replace client-side aggregate calculation with backend dashboard calls.

Support:

- global view;
- strategic orientation filter;
- project-specific view where required.

### Exit criterion

Dashboard values come from backend analytics endpoints.

---

## Task 9.4 — Verify role navigation and controls

UI should reflect backend authorization for:

- operational user;
- manager;
- leader.

Remember: UI restrictions are convenience only; backend remains authoritative.

### Exit criterion

Each role sees an appropriate experience and forbidden API calls still fail server-side.

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

---

## Task 11.3 — Project workflow tests

Cover:

- eligibility;
- one project per idea;
- idea status transition;
- financial validation.

### Exit criterion

Project conversion rules are regression-tested.

---

## Task 11.4 — Dashboard tests

Use known project fixtures and verify exact aggregates.

### Exit criterion

Dashboard numbers are proven correct by automated tests.

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

---

## Task 11.6 — Audit tests

Verify critical operations create the expected audit events.

### Exit criterion

Governance behavior is covered by automated tests.

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

---

# Phase 13 — Final end-to-end verification

Test these scenarios manually.

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
