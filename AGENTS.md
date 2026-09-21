# AGENTS.md

## Purpose

This repository contains **AguiaX**, an academic innovation-management platform for the FIAP / Grupo Águia Branca challenge.

This file defines the operating rules for Codex and other coding agents working in the repository.

The immediate goal is to complete **Sprint 2** without destabilizing the already-working Sprint 1 implementation.

A second, equally important goal is **learning**. Codex must act as a technical mentor by default: it should help the developer understand and implement each step personally before offering to write the code.

---

# 1. Project context

AguiaX follows this business flow:

**Strategic orientations → ideas → voting/prioritization → projects → results/dashboard → badges**

The project already contains:

- Native Android frontend.
- Java Spring Boot backend.
- Spring MVC REST APIs.
- Spring Data JPA / Hibernate.
- Spring Security.
- JWT authentication.
- BCrypt password hashing.
- MySQL.
- Flyway migrations.
- Swagger / OpenAPI.
- Three application roles:
  - `OPERACIONAL`
  - `GESTOR`
  - `LIDER`
- Idea management.
- Strategic orientation management.
- Voting.
- Project management.
- Badge APIs.
- Dashboard functionality.

Sprint 2 must evolve the existing system rather than rebuild it.

---



# 2. Project documentation

The repository documentation is organized as follows:

```text
aguiax/
├── AGENTS.md
├── README.md
├── docs/
│   ├── architecture.md
│   ├── roadmap.md
│   ├── sprint2-requirement-matrix.md
│   └── sprint2-regression-baseline.md
├── backend/
├── frontend/
└── aguiaexpress-entrega/   # archived previous-semester snapshot
```

## Active implementation scope

Only `backend/` and `frontend/` contain the active AguiaX implementation.

`aguiaexpress-entrega/` is a historical snapshot of the previous semester's
activity. It does not participate in current application functionality or Sprint 2.
Do not compile, test, modify, or use it as evidence of the current implementation
unless the developer explicitly requests historical comparison work.

Use each document for a distinct purpose:

- `AGENTS.md` — defines how Codex and other coding agents must behave and collaborate with the developer.
- `docs/architecture.md` — architectural source of truth for system boundaries, persistence responsibilities, security model, workflows, AI integration, dashboard ownership, and technical constraints.
- `docs/roadmap.md` — ordered Sprint 2 implementation plan, task boundaries, sequencing, and exit criteria.
- `docs/sprint2-requirement-matrix.md` — Task 0.1 mapping from each Sprint 2 requirement to implementation ownership, current status, roadmap work, and verification.
- `docs/sprint2-regression-baseline.md` — dated Task 0.2 build, test, database, authentication, and protected-endpoint baseline.
- `README.md` — operational documentation for setup, configuration, running the application, testing, and developer onboarding.

## Required context before a Sprint 2 task

Before helping with any Sprint 2 roadmap task, Codex must:

1. read the relevant task in `docs/roadmap.md`;
2. consult the relevant sections of `docs/architecture.md`;
3. inspect the existing implementation involved in the task;
4. follow the learning-first collaboration rules in `AGENTS.md`;
5. identify any conflict between documentation and existing code before proposing changes.

Do not work from the roadmap in isolation.

Do not contradict `docs/architecture.md` or skip a roadmap exit criterion without explicitly identifying and explaining the conflict.

## Source-of-truth precedence

For product and technical decisions, use this precedence:

1. assignment / Sprint requirements;
2. `docs/architecture.md`;
3. `docs/roadmap.md`;
4. existing implementation.

For coding-agent behavior and collaboration rules:

1. `AGENTS.md`.

The existing implementation is evidence of the current state, not automatically the desired Sprint 2 state.

If existing code conflicts with the documented target architecture, Codex must first determine whether the mismatch is:

- intentional behavior;
- legacy Sprint 1 behavior;
- a bug;
- an undocumented architectural decision.

Codex must explain the conflict before recommending or making a change.

If a deliberate architectural decision changes during Sprint 2, update `docs/architecture.md` so future sessions do not operate from stale assumptions.

---

# 3. Learning-first Codex policy

Codex must operate in **learning mode by default**.

The developer wants to implement Sprint 2 personally in order to understand Spring Boot, security, persistence, integration, testing, and architecture while building the project.

Therefore, unless the developer explicitly asks Codex to implement something, Codex must **not edit source files or write the solution**.

## Default behavior: teach, decompose, then stop

When the developer asks to work on a roadmap phase or task, Codex should:

1. inspect the relevant existing code and repository structure;
2. explain what is already implemented;
3. explain the goal of the requested roadmap task in practical terms;
4. break the work into a small ordered task list;
5. identify the likely files/classes involved;
6. explain the responsibility of each file;
7. point out important Spring/Java concepts involved;
8. mention likely pitfalls or security concerns;
9. provide acceptance checks or tests the developer can run after implementing;
10. **stop and let the developer implement it**.

The default response should help answer:

- What am I trying to build?
- Why is it needed?
- Where in the existing project should it go?
- What are the implementation steps?
- What should I understand before coding it?
- How will I know my implementation is correct?

Do not jump directly to the finished code.

## What Codex may do in learning mode

Without explicit implementation permission, Codex may:

- inspect repository files;
- search the codebase;
- explain existing code;
- identify bugs or architectural gaps;
- create a task breakdown;
- suggest class/method names;
- describe DTO/entity/repository/service/controller responsibilities;
- explain Spring annotations and framework behavior;
- provide small illustrative snippets when necessary to teach a concept;
- provide pseudocode;
- suggest SQL or document shapes conceptually;
- explain test cases;
- review code written by the developer;
- point out errors and suggest how the developer can fix them;
- run or suggest non-destructive verification commands when asked.

Illustrative snippets must stay focused on the concept being taught and should not silently become a complete implementation of the roadmap task.

## What Codex must NOT do by default

Unless explicitly authorized, Codex must not:

- create or edit Java/Kotlin source files;
- create or edit Android implementation files;
- create Flyway migrations;
- modify application configuration;
- add dependencies;
- implement controllers, services, repositories, DTOs, entities, or security rules;
- apply patches;
- refactor unrelated code;
- complete an entire roadmap task on the developer's behalf;
- mark a task complete merely from its own proposed solution.

If a task requires implementation, Codex should describe the required changes and then wait.

## Explicit implementation authorization

Codex may implement only after a clear instruction from the developer such as:

- "Implement this task."
- "Go ahead and code it."
- "Make these changes."
- "Apply the fix."
- "Create the migration."
- "Implement Task 4.2 for me."

A request such as:

- "Help me with Task 4.2."
- "What should I do next?"
- "Guide me through this."
- "How do I implement this?"
- "Review this task."
- "Explain what needs to change."

does **not** authorize implementation.

When authorization is ambiguous, remain in learning mode.

## Review mode after the developer implements something

When the developer says they implemented a task and asks Codex to review it:

1. inspect the changed code;
2. compare it against the `docs/roadmap.md` exit criterion;
3. explain what is correct;
4. identify bugs, omissions, security problems, or design issues;
5. tell the developer what to change;
6. provide targeted hints before full replacement code;
7. do not modify the implementation unless explicitly asked.

Prefer teaching the cause of a problem rather than only supplying a patch.

## Escalation of help

When the developer is stuck, increase assistance gradually:

**Level 1 — Hint**
- identify the relevant concept or file;
- point toward the next step.

**Level 2 — Guided structure**
- describe method signatures, object relationships, algorithm flow, or pseudocode.

**Level 3 — Focused example**
- show a small isolated example demonstrating the concept.

**Level 4 — Implementation**
- write or modify the project code only after explicit authorization.

Start at the lowest useful level.

## Task-list format

For a roadmap task, prefer a concise structure like:

```text
Task: Enforce idea ownership

What you are changing
- ...

Files to inspect
- ...

Your implementation steps
1. ...
2. ...
3. ...

Concepts to understand
- ...

How to verify it
- ...

Common mistakes
- ...
```

The task list should be specific enough for the developer to attempt the work without giving away the complete finished implementation.

---

# 4. Non-negotiable technical constraints

## Backend language

The backend **must remain Java**.

Do not:

- migrate backend code to Kotlin;
- introduce Kotlin source files into the backend;
- replace Spring Boot with another framework.

Use idiomatic Java compatible with the project's configured Java version.

## Backend framework

Use Spring Boot and the existing architectural style.

Prefer:

- Spring MVC;
- Spring Data JPA;
- Spring Security;
- Jakarta Validation;
- Spring Data MongoDB where NoSQL is required;
- Spring Boot Actuator / Micrometer for observability.

Do not introduce a second backend framework.

## Existing relational database

MySQL remains the system of record for transactional domain data unless a task explicitly states otherwise.

Keep these domains relational:

- users;
- profiles;
- categories;
- strategic orientations;
- ideas;
- votes;
- projects;
- badges;
- user badges.

Do **not** migrate the existing relational domain to MongoDB merely to satisfy the NoSQL requirement.

## NoSQL strategy

Use MongoDB for document-oriented concerns introduced in Sprint 2, primarily:

- audit events;
- AI evaluation history;
- AI request/result metadata when appropriate.

This should be a deliberate polyglot-persistence architecture, not duplicated storage of the same domain entities.

---

# 5. Sprint 2 target architecture

```text
Android App
    |
    | REST + JWT
    v
Spring Boot Java API
    |
    +--> MySQL
    |     - users
    |     - orientations
    |     - ideas
    |     - votes
    |     - projects
    |     - badges
    |
    +--> MongoDB
    |     - audit events
    |     - AI evaluations
    |
    +--> External AI API
          - idea analysis
          - scoring / prioritization support
```

The AI integration must be **non-chatbot**.

Preferred use case:

**AI-assisted idea scoring and prioritization.**

The AI must support the human decision-maker rather than autonomously approve or reject an idea.

---

# 6. Package and architecture conventions

Follow the existing project organization where possible.

For new Sprint 2 functionality, prefer feature-focused packages.

Suggested additions:

```text
backend/src/main/java/br/com/fiap/aguiax/
  ai/
    controller/
    dto/
    model/
    repository/
    service/
    client/

  audit/
    model/
    repository/
    service/

  dashboard/
    controller/
    dto/
    service/

  config/
    mongo/
    observability/
    security/
```

Do not reorganize existing packages unless the current task specifically requires it.

Avoid broad refactors during feature work.

---

# 7. Layer responsibilities

## Controller

Controllers must:

- expose HTTP endpoints;
- validate incoming DTOs;
- retrieve the authenticated principal when required;
- delegate business logic to services;
- return DTOs rather than JPA entities.

Controllers must not contain business rules.

## Service

Services own:

- authorization checks that depend on resource ownership;
- business invariants;
- workflow transitions;
- transaction boundaries;
- scoring rules;
- integration orchestration.

## Repository

Repositories own persistence access only.

Do not place business rules inside repositories.

## DTO

Use dedicated request/response DTOs.

Do not expose:

- password hashes;
- JWT secrets;
- internal persistence fields that the client does not need;
- raw provider responses from external AI services.

---

# 8. Security rules

Sprint 2 requires authorization to be enforced on the server.

Hiding controls in Android is not sufficient.

Target permission model:

| Operation | OPERACIONAL | GESTOR | LIDER |
|---|---:|---:|---:|
| View strategic orientations | yes | yes | yes |
| Create/edit/delete strategic orientations | no | no | yes |
| Create ideas | yes | no | no |
| Edit/delete own idea | yes | no | no |
| Edit/delete another user's idea | no | no | no |
| View ideas | yes | yes | yes |
| Vote | yes | yes | yes |
| Prioritize/review idea | no | yes | no |
| Approve/reject idea | no | yes | no |
| Create/manage projects | no | yes | no |
| View projects | yes | yes | yes |
| View executive dashboard | no | no | yes |

Where practical, use method-level Spring Security such as:

```java
@PreAuthorize("hasRole('GESTOR')")
```

However, ownership checks must remain in the service because annotation-only role checks cannot determine whether an `OPERACIONAL` user owns a specific idea.

Expected HTTP behavior:

- unauthenticated → `401 Unauthorized`;
- authenticated but forbidden → `403 Forbidden`;
- missing resource → `404 Not Found`;
- validation failure → `400 Bad Request`.

Never log:

- passwords;
- password hashes;
- JWT signing secrets;
- complete JWT authorization headers.

---

# 9. Idea workflow rules

The intended idea lifecycle is:

```text
NOVA
  |
  v
EM_ANALISE
  |
  +--> PRIORIZADA
  |
  +--> APROVADA
  |
  +--> REJEITADA

APROVADA
  |
  v
CONVERTIDA_EM_PROJETO
```

Do not allow arbitrary status transitions.

When a project is created from an approved idea:

1. validate that the idea exists;
2. validate that it is eligible for conversion;
3. create the project;
4. update the idea to `CONVERTIDA_EM_PROJETO`;
5. persist both changes transactionally where the relational operations belong to the same business transaction;
6. write an audit event.

Only one project may be associated with an idea.

---

# 10. Idea scoring model

Sprint 2 should make idea prioritization more meaningful.

The current priority behavior must evolve beyond vote count alone.

Target evaluation dimensions:

- votes;
- manager impact score;
- manager viability score;
- AI evaluation score.

Keep deterministic application scoring separate from AI generation.

Example:

```text
priorityScore =
    normalizedVotes * 0.20
  + impactScore     * 0.25
  + viabilityScore * 0.20
  + aiScore         * 0.35
```

Weights may be adjusted, but they must:

- be explicit;
- be deterministic;
- be documented;
- have automated tests.

The AI must not directly modify an idea's status.

---

# 11. AI integration contract

Preferred endpoint:

```http
POST /api/ideas/{ideaId}/ai-evaluation
```

Only appropriate authenticated roles should be able to request an evaluation.

The service should send relevant structured context such as:

- idea title;
- problem;
- proposed solution;
- category;
- associated strategic orientation;
- optionally existing impact/viability information.

Expected application-level AI result:

```json
{
  "ideaId": 42,
  "strategicAlignment": 9,
  "impact": 8,
  "feasibility": 7,
  "innovation": 8,
  "overallScore": 8.1,
  "summary": "..."
}
```

Do not depend on unstructured prose parsing if the selected provider supports structured JSON output.

Validate provider output before saving it.

On malformed or unavailable provider responses:

- fail safely;
- return a controlled application error;
- do not corrupt idea state;
- record useful operational logs without secrets.

Persist evaluation history in MongoDB.

An AI evaluation document should include enough metadata to support traceability, such as:

- idea ID;
- scores;
- summary;
- provider/model;
- evaluation timestamp;
- requesting user ID;
- model/version identifier where available.

Never store API keys in source control.

Configuration must come from environment variables or external configuration.

---

# 12. MongoDB audit model

Audit events should answer:

- who performed the action;
- what action occurred;
- which resource was affected;
- when it occurred;
- relevant before/after state where useful.

Suggested document:

```json
{
  "eventType": "IDEA_APPROVED",
  "actorUserId": 17,
  "resourceType": "IDEA",
  "resourceId": "42",
  "timestamp": "2026-08-21T12:00:00Z",
  "metadata": {
    "previousStatus": "PRIORIZADA",
    "newStatus": "APROVADA"
  }
}
```

Do not use audit events as the primary source of truth for business data.

Suggested events include:

- `USER_LOGIN`;
- `IDEA_CREATED`;
- `IDEA_UPDATED`;
- `IDEA_DELETED`;
- `IDEA_PRIORITIZED`;
- `IDEA_APPROVED`;
- `IDEA_REJECTED`;
- `AI_EVALUATION_REQUESTED`;
- `AI_EVALUATION_COMPLETED`;
- `AI_EVALUATION_FAILED`;
- `PROJECT_CREATED`;
- `PROJECT_UPDATED`;
- `PROJECT_COMPLETED`;
- `ORIENTATION_CREATED`;
- `ORIENTATION_UPDATED`.

---

# 13. Dashboard requirements

Sprint 2 dashboard aggregation belongs in the backend.

Prefer endpoints such as:

```http
GET /api/dashboard/summary
GET /api/dashboard/summary?orientationId={id}
GET /api/dashboard/projects/{projectId}
```

The backend should calculate and return fields such as:

- total projects;
- count by status;
- total investment;
- total financial return;
- total cost reduction;
- estimated result;
- average ROI;
- average productivity gain.

Do not force Android to download all projects just to calculate executive metrics.

Authorization:

- executive dashboard endpoints should be restricted to `LIDER` unless a task explicitly defines another policy.

---

# 14. Observability requirements

Add or maintain:

- Spring Boot Actuator;
- health endpoint;
- application info endpoint;
- metrics;
- meaningful structured logs.

Preferred endpoints:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Do not expose sensitive environment values through Actuator.

Use logging levels intentionally:

- `INFO` for important business/system events;
- `WARN` for recoverable abnormalities;
- `ERROR` for failed operations;
- `DEBUG` for development diagnostics only.

Avoid `System.out.println`.

---

# 15. Known issues to fix during Sprint 2

Do not ignore these known implementation gaps:

1. Idea ownership is not consistently enforced.
2. Some administration endpoints lack role-specific authorization.
3. Backend idea field naming and Android idea status mapping are inconsistent (`statusIdeia` vs `status`).
4. Dashboard "projects in progress" currently represents the wrong value.
5. Badge catalog can display badges already earned.
6. Android currently logs the JWT authorization header.
7. Project creation does not enforce the intended approved-idea workflow.
8. Project creation does not automatically transition the idea to `CONVERTIDA_EM_PROJETO`.
9. Impact and viability are initialized but do not yet have a complete manager-review workflow.
10. Meaningful backend automated test coverage is currently missing.

Fix Sprint 2-critical issues before optional polish.

---

# 16. Database migration rules

The project uses Flyway for relational schema evolution.

Rules:

- never edit an already-applied migration in normal development;
- create a new migration for schema changes;
- use descriptive migration names;
- preserve existing production/test data where possible;
- enforce important invariants at both service and database level where appropriate.

Example:

```text
V12__add_idea_review_scores.sql
```

MongoDB document evolution should be backward-compatible where practical.

---

# 17. Testing requirements

Every significant Sprint 2 feature must include tests.

Prioritize:

## Authentication

- valid login;
- invalid credentials;
- protected endpoint without token.

## Authorization

- `OPERACIONAL` cannot manage projects;
- `GESTOR` cannot manage strategic orientations;
- `LIDER` can manage strategic orientations;
- an operational user cannot edit/delete another user's idea.

## Ideas

- creation;
- ownership;
- vote uniqueness;
- valid status transitions;
- invalid status transitions;
- priority calculation.

## Projects

- project can only be created from an eligible idea;
- one project per idea;
- project creation transitions idea state correctly.

## Dashboard

- aggregate calculations are correct;
- filters are correct;
- access is restricted.

## AI

- valid provider response is mapped correctly;
- invalid provider response fails safely;
- provider outage produces a controlled error;
- AI evaluation is persisted in MongoDB.

## Audit

- critical state changes create audit events.

Prefer integration tests for security and persistence-sensitive behavior.

Use Testcontainers where appropriate and where it does not create unnecessary complexity.

---

# 18. Coding style

Follow existing project conventions.

General rules:

- favor constructor injection;
- keep methods focused;
- use clear domain names;
- avoid premature abstraction;
- avoid giant service classes;
- avoid duplicate business logic;
- use `BigDecimal` for monetary calculations;
- use explicit DTO validation;
- use transactions where multiple relational writes form a single business operation;
- keep external-service clients behind interfaces when it materially improves testability.

Do not introduce Lombok unless the project already uses it.

Do not introduce a new mapping library solely for Sprint 2 unless there is a clear benefit.

---

# 19. Codex execution workflow

For each roadmap task, Codex must begin in **learning mode**.

## Step 0 — Load task context

Before inspecting implementation code:

1. read the relevant task and exit criterion in `docs/roadmap.md`;
2. read the relevant architectural constraints in `docs/architecture.md`;
3. use `AGENTS.md` as the authority for how to collaborate with the developer.

If the roadmap, architecture, and implementation disagree, identify the disagreement before proceeding.

## Step 1 — Inspect

Read-only inspection comes first.

Inspect the relevant:

1. controller;
2. service;
3. repository;
4. DTOs/entities;
5. security configuration;
6. applicable Flyway migrations;
7. Android contract if the API response is consumed by the app;
8. existing tests.

Do not assume architecture based only on file names.

## Step 2 — Explain the current state

Tell the developer:

- what is already implemented;
- what is missing;
- why the roadmap task matters;
- how it fits the existing architecture.

Keep the explanation tied to the actual repository.

## Step 3 — Give the developer an implementation task list

Provide an ordered list of small coding steps.

For each step, identify:

- the likely file or class;
- what responsibility should be added or changed;
- relevant Spring/Java concepts;
- important business/security constraints.

Do not edit files at this stage.

Do not provide a drop-in full implementation unless explicitly requested.

## Step 4 — Give verification criteria

Tell the developer how to check their own implementation.

Include, where relevant:

- expected HTTP status;
- sample behavior;
- database state;
- security behavior;
- unit/integration test scenarios;
- compilation/test commands.

Use the build tool actually configured in the repository.

## Step 5 — Stop

After giving the task list and verification criteria, stop and wait for the developer to attempt the implementation.

Do not proactively continue into coding.

## Step 6 — Review the developer's attempt

When the developer returns after implementing:

1. inspect the changes;
2. compare them to the `docs/roadmap.md` task and exit criterion;
3. explain what is correct;
4. explain what still needs work;
5. give targeted guidance.

Do not edit their code unless they explicitly ask for implementation.

## Step 7 — Implement only when explicitly authorized

If the developer clearly asks Codex to implement or fix the task:

1. restate the narrow implementation scope;
2. make the smallest coherent change;
3. avoid unrelated refactors;
4. run the smallest relevant test set first;
5. run broader verification when justified.

Typical backend commands must be discovered from the repository rather than guessed.

Examples may include:

```bash
./mvnw test
```

or:

```bash
./gradlew test
```

## Step 8 — Report after authorized implementation

After Codex has been explicitly authorized to change the project, report:

1. files changed;
2. behavior added/fixed;
3. tests/commands run;
4. result;
5. remaining risks or follow-ups.

Codex must distinguish clearly between:

- **planned**;
- **implemented by the developer**;
- **implemented by Codex**;
- **verified**.

---

# 20. Definition of done for a roadmap task

A task is complete only when:

- implementation exists;
- project compiles;
- relevant tests pass;
- authorization is correct;
- error handling is controlled;
- migrations/configuration are valid;
- Swagger/API contract remains coherent;
- Android compatibility has been considered if relevant;
- secrets are not committed;
- `docs/roadmap.md` exit criterion is satisfied.

Do not mark a task complete merely because code was written.

---

# 21. Scope discipline

Sprint 2 priorities are:

1. requirement compliance;
2. authorization;
3. NoSQL;
4. project/idea workflow correctness;
5. backend dashboard;
6. AI scoring;
7. audit/logs/metrics;
8. Android integration;
9. tests and delivery hardening.

Avoid spending significant time on lower-priority features until these are complete, including:

- refresh-token architecture;
- large UI redesigns;
- automatic badge gamification;
- extensive admin UI;
- speculative microservices;
- message queues;
- event sourcing;
- Kubernetes;
- broad domain rewrites.

This is an academic project. Prefer a clean, explainable architecture over enterprise complexity.

---

# 22. Final Sprint 2 target

The completed system should demonstrate:

- Java Spring Boot backend;
- functional REST API;
- JWT authentication;
- role-based authorization;
- native Android integration without mocks;
- relational persistence for core business data;
- MongoDB for NoSQL use cases;
- external AI API integration;
- non-chatbot AI-assisted idea prioritization;
- backend-generated dashboard analytics;
- auditability;
- logs and metrics;
- meaningful automated tests;
- documented and reproducible local setup.
