# AguiaExpress Architecture

## 1. Purpose

AguiaExpress is an academic innovation-management platform developed for the FIAP / Grupo Águia Branca challenge.

The platform supports the flow from strategic direction to employee ideas, prioritization, project execution, and executive results.

The core business flow is:

```text
Strategic Orientations
        ↓
      Ideas
        ↓
Voting + Manager Review
        ↓
   Prioritization
        ↓
 Approval / Rejection
        ↓
      Projects
        ↓
Results / Dashboard
        ↓
     Badges
```

This document describes:

- the current system architecture;
- the target architecture for Sprint 2;
- application boundaries;
- persistence responsibilities;
- security and authorization;
- idea and project workflows;
- AI integration;
- dashboard responsibilities;
- audit and observability;
- architectural constraints that should remain stable during implementation.

This document should be read together with:

- `AGENTS.md` — rules for how Codex should assist with the project;
- `roadmap.md` — ordered Sprint 2 implementation plan;
- `README.md` — setup and operational documentation.

---

# 2. Architectural principles

The Sprint 2 implementation should follow these principles.

## 2.1 Evolve instead of rebuild

Sprint 1 already established a working Android application and Spring Boot backend.

Sprint 2 should extend and harden the existing system rather than rewrite it.

Avoid:

- replacing the existing backend;
- migrating the backend to Kotlin;
- replacing MySQL without a business reason;
- redesigning the entire Android application;
- introducing microservices;
- introducing infrastructure that does not directly support Sprint 2 requirements.

---

## 2.2 Backend remains Java Spring Boot

The backend must remain:

```text
Java
Spring Boot
Spring MVC
Spring Data JPA / Hibernate
Spring Security
JWT
```

New backend production code must be written in Java.

---

## 2.3 Backend owns business rules

The Android application is a client.

The backend is authoritative for:

- authentication;
- authorization;
- idea ownership;
- workflow transitions;
- project creation rules;
- prioritization calculations;
- dashboard calculations;
- audit generation;
- persistence.

The Android app may hide unavailable actions for usability, but backend security must never depend on UI restrictions.

---

## 2.4 AI supports decisions; it does not make them

AI is a decision-support mechanism.

It may:

- analyze an idea;
- score strategic alignment;
- estimate impact;
- estimate feasibility;
- assess innovation potential;
- provide a concise explanation.

It must not autonomously:

- approve an idea;
- reject an idea;
- change an idea's workflow status;
- create a project;
- override a manager.

Final business decisions remain with the appropriate human role.

---

## 2.5 Use each database for an appropriate responsibility

The architecture should use:

- **MySQL** for transactional business data;
- **MongoDB** for flexible append-oriented Sprint 2 documents such as audit events and AI evaluation history.

Do not duplicate the complete relational domain in MongoDB.

---

# 3. Current architecture

The current system consists of a native Android application and a Java Spring Boot backend.

The active implementation is located exclusively in the repository's `backend/`
and `frontend/` directories. `aguiaexpress-entrega/` is an archived snapshot from
the previous semester; it is not part of the running system, the target architecture,
or the Sprint 2 implementation scope.

```text
┌─────────────────────────────┐
│ Android App                 │
│                             │
│ Kotlin                      │
│ Jetpack Compose             │
│ Retrofit                    │
│ Coroutines                  │
│ ViewModel / StateFlow       │
└──────────────┬──────────────┘
               │
               │ REST / JSON
               │ Bearer JWT
               ▼
┌─────────────────────────────┐
│ Spring Boot Backend         │
│                             │
│ Java                        │
│ Spring MVC                  │
│ Spring Data JPA             │
│ Spring Security             │
│ JWT + BCrypt                │
│ Flyway                      │
│ Swagger / OpenAPI           │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ MySQL                       │
│                             │
│ users                       │
│ profiles                    │
│ categories                  │
│ strategic orientations      │
│ ideas                       │
│ votes                       │
│ projects                    │
│ badges                      │
│ user badges                 │
└─────────────────────────────┘
```

The current system already contains the majority of the core Sprint 1 business functionality.

Sprint 2 primarily adds:

- stronger server-side authorization;
- NoSQL persistence;
- AI integration;
- backend analytics;
- audit;
- logs;
- metrics;
- stronger testing;
- workflow corrections.

---

# 4. Target Sprint 2 architecture

The target architecture is:

```text
                         ┌──────────────────────────────┐
                         │ Android App                  │
                         │                              │
                         │ Kotlin + Jetpack Compose     │
                         │ Retrofit                     │
                         └──────────────┬───────────────┘
                                        │
                                        │ REST / JSON
                                        │ Bearer JWT
                                        ▼
                  ┌──────────────────────────────────────────┐
                  │ AguiaExpress Backend                     │
                  │                                          │
                  │ Java + Spring Boot                       │
                  │ Spring MVC                               │
                  │ Spring Security                          │
                  │ Spring Data JPA                          │
                  │ Spring Data MongoDB                      │
                  │ Validation                              │
                  │ Actuator / Micrometer                    │
                  └───────┬──────────────┬─────────────┬─────┘
                          │              │             │
                          │              │             │ HTTPS
                          ▼              ▼             ▼
                ┌────────────────┐ ┌──────────────┐ ┌───────────────┐
                │ MySQL          │ │ MongoDB      │ │ External AI   │
                │                │ │              │ │ Provider      │
                │ Core domain    │ │ Audit events │ │               │
                │ transactions   │ │ AI history   │ │ Gemini /      │
                │                │ │              │ │ OpenAI        │
                └────────────────┘ └──────────────┘ └───────────────┘
```

---

# 5. Application boundaries

## 5.1 Android application

The Android application is responsible for:

- login UI;
- role-specific navigation;
- displaying strategic orientations;
- submitting and viewing ideas;
- voting;
- manager review actions;
- displaying AI evaluation results;
- displaying projects;
- displaying dashboard information;
- badges;
- client-side loading/error states.

The Android app should not own authoritative business calculations.

In particular, Sprint 2 should move executive dashboard aggregation to the backend.

---

## 5.2 Spring Boot backend

The backend is responsible for:

- authentication;
- JWT generation and validation;
- role authorization;
- resource ownership;
- validation;
- business workflows;
- relational persistence;
- MongoDB persistence;
- AI orchestration;
- dashboard aggregation;
- audit creation;
- logs;
- metrics;
- API documentation.

---

## 5.3 External AI provider

The AI provider is an external dependency.

The backend must wrap the provider behind an application-owned client/service boundary.

Android should never call the AI provider directly.

This provides:

- centralized API-key protection;
- provider abstraction;
- response validation;
- consistent error handling;
- traceability;
- easier testing.

---

# 6. Backend layering

The backend should continue using a layered structure.

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository / External Client
    ↓
Database / External API
```

---

## 6.1 Controllers

Controllers are responsible for:

- HTTP routing;
- request DTO validation;
- extracting authenticated identity when needed;
- calling application services;
- returning response DTOs.

Controllers should remain thin.

They should not implement:

- scoring formulas;
- ownership rules;
- workflow transitions;
- persistence decisions.

---

## 6.2 Services

Services own business behavior.

Examples:

```text
IdeiaService
ProjetoService
OrientacaoEstrategicaService
DashboardService
AiIdeaEvaluationService
AuditService
```

Services are responsible for:

- resource ownership;
- business invariants;
- status transitions;
- priority calculation;
- project conversion;
- AI orchestration;
- audit triggering;
- transaction boundaries.

---

## 6.3 Repositories

Repositories are responsible only for persistence operations.

Examples:

```text
IdeiaRepository
ProjetoRepository
UsuarioRepository
AuditEventRepository
AiEvaluationRepository
```

Business rules should not be hidden inside repository methods.

---

## 6.4 DTOs

Controllers should communicate through request/response DTOs.

Do not expose JPA entities directly as the public API contract.

DTOs should protect against leaking:

- password hashes;
- internal security fields;
- provider secrets;
- raw AI provider payloads;
- unnecessary persistence details.

---

# 7. Suggested package organization

Existing package organization should be preserved where practical.

New Sprint 2 capabilities may use feature-focused packages such as:

```text
br.com.fiap.aguiaexpress
│
├── ai
│   ├── client
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── audit
│   ├── model
│   ├── repository
│   └── service
│
├── dashboard
│   ├── controller
│   ├── dto
│   └── service
│
├── config
│   ├── security
│   ├── mongo
│   └── observability
│
├── ideia
├── projeto
├── usuario
└── ...
```

This structure is illustrative.

Do not reorganize existing working packages merely to match this diagram.

---

# 8. Authentication architecture

Authentication uses:

```text
Email + Password
      ↓
Spring Security
      ↓
BCrypt password verification
      ↓
JWT generated
      ↓
Android stores active session
      ↓
Authorization: Bearer <token>
      ↓
Protected backend endpoint
```

Expected behavior:

```text
POST /auth/login
     ↓
credentials validated
     ↓
JWT returned
     ↓
Android adds JWT to protected requests
```

JWT claims should include enough information to identify:

- the authenticated user;
- the user's role/profile.

The backend remains stateless.

---

# 9. Authorization architecture

Authentication answers:

> Who is the user?

Authorization answers:

> Is this user allowed to perform this operation?

Sprint 2 must enforce authorization on the backend.

Target authorization matrix:

| Operation | OPERACIONAL | GESTOR | LIDER |
|---|---:|---:|---:|
| View strategic orientations | yes | yes | yes |
| Create strategic orientation | no | no | yes |
| Edit strategic orientation | no | no | yes |
| Delete/deactivate strategic orientation | no | no | yes |
| Create idea | yes | no | no |
| Edit own idea | yes | no | no |
| Delete own idea | yes | no | no |
| Edit another user's idea | no | no | no |
| View ideas | yes | yes | yes |
| Vote on ideas | yes | yes | yes |
| Review/prioritize ideas | no | yes | no |
| Approve/reject ideas | no | yes | no |
| Create/manage projects | no | yes | no |
| View projects | yes | yes | yes |
| View executive dashboard | no | no | yes |

Role checks may use Spring Security annotations such as:

```java
@PreAuthorize("hasRole('GESTOR')")
```

Resource ownership must still be validated in the service.

Example:

```text
OPERACIONAL
    ↓
PUT /ideias/42
    ↓
authenticated?
    ↓
role allowed?
    ↓
does user own idea 42?
    ↓
YES → update
NO  → 403
```

## 9.1 Task 1.1 policy decisions

Recorded by Codex on 2026-09-08 after inspecting the active backend and Android
clients. These decisions complete the authorization specification. They do not
claim that the missing checks have been implemented or tested. Enforcement belongs
to Tasks 1.2–1.4 and the feature-specific tasks listed below.

The existing role matrix above remains unchanged. Roles are independent:
`LIDER` does not inherit `GESTOR` or `OPERACIONAL` permissions. There is no `ADMIN`
role. For previously unspecified operations, use these policies:

- **Users:** `LIDER` can list users and delete accounts. Any authenticated user can
  read/update their own profile; `LIDER` can read/update any profile. Profile updates
  are limited to the existing `AtualizarUsuarioDTO` fields (`nome`, `email`, `cargo`,
  `departamento`); they cannot change role, points, account status, or password.
  Deletion permission does not override relational constraints or authorize
  cascading deletion of the user's ideas/projects.
- **Categories:** all three roles can read; only `LIDER` can create/edit/delete.
  Categories are shared governance data, not manager-owned project data.
- **Badge awards:** only `GESTOR` can grant/remove an award, including the grant-by-name
  variant. This fits the manager's idea/project review responsibility. Awards remain
  manual; automatic gamification is not introduced. `GESTOR` and `LIDER` can inspect
  all awards; other users can inspect only their own awards. This also limits access
  to recipient emails currently included in `MostrarUsuarioBadgeDTO`.
- **Badge definitions, role definitions, and idea/project status catalogs:** reads
  are authenticated where an endpoint exists. There is no HTTP catalog mutation
  permission for any role. Definitions remain managed through reviewed code/Flyway
  changes. Do not add CRUD endpoints to enum-backed catalogs for this task.
- **Ideas and votes:** the service derives the actor from authentication. Only an
  owning `OPERACIONAL` can edit/delete an idea, and update preserves its original
  owner. Any role can cast/remove its own vote. Vote reads remain shared among
  authenticated users; the current application does not implement anonymous voting.
- **Projects and orientations:** any `GESTOR` may manage a project and any `LIDER`
  may manage an orientation, irrespective of its creator. A project DTO's `usuarioId`
  is a domain assignment, not proof of the caller's identity or authorization.

### Registration and privileged provisioning

Keep public self-registration at `POST /usuarios`, but make it operational-only.
For compatibility, the existing required `nomePerfil` field accepts only
`OPERACIONAL` on this route. A request for `GESTOR` or `LIDER` returns `400` and
creates no account, even if the caller happens to be an authenticated leader.
The service must select the operational profile itself after validation; it must
not trust a caller-selected privileged profile.

Task 1.4 will introduce a separate `POST /usuarios/admin` route restricted to
`LIDER` for provisioning accounts with any of the three existing profiles. Reuse
account validation and BCrypt hashing, but keep public and privileged authorization
paths explicit. Ordinary profile updates never promote users. The first leader
must be provisioned by a trusted deployment operator through a controlled local
setup using an externally supplied password and BCrypt hash, not an unauthenticated
HTTP bootstrap route or committed default credentials. Task 1.4 must document the
reproducible bootstrap procedure; already-existing valid leader accounts can be used.

This resolves the previously undocumented provisioning decision. The current
`SecurityConfig` still permits public registration and `UsuarioService` still uses
the requested profile: the privilege-escalation bug remains open until Task 1.4.

## 9.2 Complete current controller inventory

All **62 explicitly mapped operations across 12 controllers** were inspected on
2026-09-08. Paths below are the existing Portuguese routes; do not rename them to
match illustrative `/api/...` examples elsewhere. Query filters do not change policy.
Framework-generated HEAD/OPTIONS handling is not counted as a separate business
operation and must not provide a bypass to these policies.

Policy labels:

- `Public`: no authentication required; route-specific validation still applies.
- `Authenticated`: any of `OPERACIONAL`, `GESTOR`, or `LIDER`.
- A role name: only that role, with no implicit hierarchy.
- `Self or LIDER`: authenticated user ID equals the path user ID, or caller is `LIDER`.
- `OPERACIONAL + owner`: both the role and idea ownership must match.
- `Self or GESTOR/LIDER`: own badge recipient ID, or either named role.
- `GESTOR/LIDER`: either named role, with no ownership requirement.

Current state describes authorization observed in source: **Implemented** means
the relevant checks exist, **Partial** means authentication exists but required
role/ownership/input restrictions are incomplete, and **Missing** means the route
does not exist (used in section 9.3). This is not a claim of passing integration
tests. `Auth` in the final column means verify permitted access and `401` without a
token; `Role` adds `403` for every other role; `Owner` adds cross-user denial and no
mutation. Each controller name refers to `backend/src/main/java/br/com/fiap/aguiaexpress/controller/`.

| Controller | Endpoint | Target policy | Current state | Verification / enforcement task |
|---|---|---|---|---|
| AuthController | `POST /auth/login` | Public | Implemented | Valid/invalid credentials; 11.1 |
| UsuarioController | `POST /usuarios` | Public, creates OPERACIONAL only | Partial | Privileged profile gets 400, no insert; 1.4 |
| UsuarioController | `GET /usuarios` | LIDER | Partial | Role; 1.4 |
| UsuarioController | `GET /usuarios/{id}` | Self or LIDER | Partial | Owner, leader access; 1.4 |
| UsuarioController | `PUT /usuarios/{id}` | Self or LIDER | Partial | Owner, preserve privileged fields; 1.4 |
| UsuarioController | `DELETE /usuarios/{id}` | LIDER | Partial | Role, relational constraints; 1.4 |
| CategoriaController | `POST /categorias` | LIDER | Partial | Role; 1.4 |
| CategoriaController | `GET /categorias` | Authenticated | Implemented | Auth; 1.4 |
| CategoriaController | `GET /categorias/{id}` | Authenticated | Implemented | Auth; 1.4 |
| CategoriaController | `PUT /categorias/{id}` | LIDER | Partial | Role; 1.4 |
| CategoriaController | `DELETE /categorias/{id}` | LIDER | Partial | Role; 1.4 |
| OrientacaoEstrategicaController | `POST /orientacoes` | LIDER | Implemented | Role; 1.2 |
| OrientacaoEstrategicaController | `GET /orientacoes` | Authenticated | Implemented | Auth; 1.2 |
| OrientacaoEstrategicaController | `GET /orientacoes/ativas` | Authenticated | Implemented | Auth; 1.2 |
| OrientacaoEstrategicaController | `GET /orientacoes/{id}` | Authenticated | Implemented | Auth; 1.2 |
| OrientacaoEstrategicaController | `GET /orientacoes/area` | Authenticated | Implemented | Auth; 1.2 |
| OrientacaoEstrategicaController | `PUT /orientacoes/{id}` | LIDER | Implemented | Role, any leader; 1.2 |
| OrientacaoEstrategicaController | `DELETE /orientacoes/{id}` | LIDER | Implemented | Role, soft deactivation; 1.2 |
| IdeiaController | `POST /ideias` | OPERACIONAL | Implemented | Role verified by focused MVC tests; 1.2 |
| IdeiaController | `GET /ideias` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `GET /ideias/{id}` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `PUT /ideias/{id}` | OPERACIONAL + owner | Implemented | Role + Owner and preserved author verified by focused tests; 1.2–1.3 |
| IdeiaController | `DELETE /ideias/{id}` | OPERACIONAL + owner | Implemented | Role + Owner verified by focused tests; 1.2–1.3 |
| IdeiaController | `GET /ideias/usuario/{usuarioId}` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `GET /ideias/categoria/{categoriaId}` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `GET /ideias/status/{status}` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `GET /ideias/orientacao/{orientacaoId}` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `GET /ideias/buscar` | Authenticated | Implemented | Auth; 1.2 |
| IdeiaController | `PATCH /ideias/{id}/status` | GESTOR | Implemented | Role; 1.2; transition rules still missing in 4.2 |
| VotoIdeiaController | `POST /votos-ideias/ideia/{ideiaId}` | Authenticated, own vote only | Implemented | Auth, actor from principal, duplicate vote; 11.2 |
| VotoIdeiaController | `DELETE /votos-ideias/ideia/{ideiaId}` | Authenticated, own vote only | Implemented | Auth, other votes unchanged; 11.2 |
| VotoIdeiaController | `GET /votos-ideias` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/{id}` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/ideia/{ideiaId}` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/usuario/{usuarioId}` | Authenticated | Implemented | Auth; 11.2 |
| ProjetoController | `POST /projetos` | GESTOR | Implemented | Role; 1.2; conversion invariants in 4.3 |
| ProjetoController | `GET /projetos` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `GET /projetos/{id}` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `PUT /projetos/{id}` | GESTOR | Implemented | Role, any manager; 1.2 |
| ProjetoController | `DELETE /projetos/{id}` | GESTOR | Implemented | Role; 1.2 |
| ProjetoController | `GET /projetos/usuario/{usuarioId}` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `GET /projetos/status/{status}` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `GET /projetos/buscar` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `GET /projetos/ideia/{ideiaId}` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `PATCH /projetos/{id}/status` | GESTOR | Implemented | Role; 1.2; lifecycle tests in 11.3 |
| BadgeController | `GET /badges` | Authenticated | Implemented | Auth; 1.4 |
| BadgeController | `GET /badges/ativas` | Authenticated | Implemented | Auth; 1.4 |
| BadgeController | `GET /badges/{id}` | Authenticated | Implemented | Auth; 1.4 |
| BadgeController | `GET /badges/nome/{nomeBadge}` | Authenticated | Implemented | Auth; 1.4 |
| UsuarioBadgeController | `POST /usuario-badges/usuario/{usuarioId}/badge/{badgeId}` | GESTOR | Partial | Role; 1.4 |
| UsuarioBadgeController | `POST /usuario-badges/usuario/{usuarioId}/nome/{nomeBadge}` | GESTOR | Partial | Role, no grant-by-name bypass; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges` | GESTOR/LIDER | Partial | Role; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/{id}` | Self or GESTOR/LIDER | Partial | Owner uses award recipient, not award ID; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/usuario/{usuarioId}` | Self or GESTOR/LIDER | Partial | Owner, Android own-badges flow; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/badge/{badgeId}` | GESTOR/LIDER | Partial | Role; 1.4 |
| UsuarioBadgeController | `DELETE /usuario-badges/usuario/{usuarioId}/badge/{badgeId}` | GESTOR | Partial | Role; 1.4 |
| StatusIdeiaController | `GET /status-ideias` | Authenticated | Implemented | Auth; 1.4 |
| StatusIdeiaController | `GET /status-ideias/{id}` | Authenticated | Implemented | Auth; 1.4 |
| StatusProjetoController | `GET /status-projetos` | Authenticated | Implemented | Auth; 1.4 |
| StatusProjetoController | `GET /status-projetos/{id}` | Authenticated | Implemented | Auth; 1.4 |
| HelloWorldController | `GET /api/english` | Authenticated | Implemented | Auth; legacy diagnostic, no new privilege; 1.2 |
| HelloWorldController | `GET /api/deutsch` | Authenticated | Implemented | Auth; legacy diagnostic, no new privilege; 1.2 |

## 9.3 Planned operations and infrastructure boundaries

These are target policies, separate from the existing inventory. Add exact route
rows when their owning roadmap tasks introduce the controllers.

| Operation / endpoint | Target policy | Current state | Owning task |
|---|---|---|---|
| `POST /usuarios/admin` | LIDER; provision any existing role | Missing | 1.4 |
| Manager review scores/prioritization endpoint | GESTOR | Missing | 4.1 |
| `GET /api/dashboard/summary` including `orientationId` filter | LIDER | Missing | 6.2–6.5 |
| `GET /api/dashboard/projects/{projectId}` if needed | LIDER | Missing | 6.4–6.5 |
| `POST /api/ideas/{ideaId}/ai-evaluation` | GESTOR | Missing | 8.1 |
| AI history read endpoint, if exposed | GESTOR only; internal scoring service may read persisted results | Missing | 8.2 |
| Audit events | Internal service writes only; no client read/write API in Sprint 2 | Missing | 3.1–3.2 |
| `GET /actuator/health` | Public, overall status only; no component details | Missing | 10.1 |
| `GET /actuator/info`, `GET /actuator/metrics`, `GET /actuator/metrics/{name}` | LIDER, sanitized metadata/metrics only | Missing | 10.1 |

Other Actuator endpoints must remain unexposed. Swagger UI and OpenAPI GET routes
may be public only in an explicitly enabled local/development documentation profile;
they must be disabled in non-development deployments unless a separate policy is
documented. Current security does not exempt these documentation routes (Task 12.1).
Framework error dispatch must return a controlled error without exposing internals;
it does not grant access to the original protected operation.

## 9.4 Enforcement and verification contract

`SecurityConfig` owns public-route exceptions and the authenticated default;
`@EnableMethodSecurity` is already enabled. Controllers may use `@PreAuthorize` for
role restrictions. `hasRole('GESTOR')` matches `ROLE_GESTOR`, which `Usuario` already
exposes through `NomePerfil`. Services own resource-dependent checks using
`UsuarioAutenticadoService`, never caller-supplied actor IDs. Repositories remain
persistence-only; no authorization schema migration is needed for these policies.

Use two operational users, one manager, and one leader to verify each matrix row.
For protected endpoints, unauthenticated callers receive `401`, wrong-role or
wrong-owner callers receive `403`, and an authorized request for a missing resource
receives `404`. Perform ownership checks before duplicate-field validation or
mutation. Invalid allowed-role requests receive `400` for validation failures.
For successful requests, retain existing endpoint response codes and DTOs.
For vote removal, absence of the caller's own vote produces `404` and never removes
another user's vote. Test both badge grant routes, public role escalation attempts,
and privileged provisioning explicitly; all denied writes must leave MySQL unchanged.

Android currently calls login, authenticated catalogs, and its own user-badge list;
the selected policies preserve those paths. There is no current registration or
user-administration API client. The idea menu now compares the authenticated user
ID with the idea owner ID; server checks remain authoritative. New admin UI is not
required.

Task 1.1 verification is an inventory/document consistency check. Runtime
authorization verification remains pending; the Task 0.2 `${DB_URL}` backend test
configuration failure is recorded in [the regression baseline](sprint2-regression-baseline.md)
and must be addressed before the security integration suite can pass. A completed
policy document is not evidence that Phase 1 is secure or complete.

---

# 10. Core domain model

At a high level:

```text
Usuario
  |
  +--------< Ideia
  |            |
  |            +------ Categoria
  |            |
  |            +------ OrientacaoEstrategica
  |            |
  |            +------ StatusIdeia
  |            |
  |            +------< VotoIdeia
  |            |
  |            +------ Projeto
  |
  +--------< UsuarioBadge >-------- Badge
```

A project is created from an eligible idea.

An idea belongs to a strategic context and category.

Users participate according to their profile.

---

# 11. Strategic orientation architecture

Strategic orientations represent organizational priorities.

Conceptually:

```text
LIDER
  ↓
creates strategic orientation
  ↓
orientation becomes available
  ↓
OPERACIONAL associates new idea
  ↓
GESTOR evaluates alignment
  ↓
projects remain linked to strategy through idea
```

Sprint 2 should preserve this relationship because it enables dashboard filtering by strategic orientation.

---

# 12. Idea lifecycle

The intended lifecycle is:

```text
NOVA
  |
  v
EM_ANALISE
  |
  +--------> PRIORIZADA
  |              |
  |              v
  |          APROVADA
  |
  +--------> APROVADA
  |
  +--------> REJEITADA

APROVADA
   |
   v
CONVERTIDA_EM_PROJETO
```

Allowed transitions must be explicitly enforced.

The system should reject arbitrary transitions.

Example invalid behavior:

```text
NOVA → CONVERTIDA_EM_PROJETO
```

without review/approval.

---

# 13. Idea review and prioritization

Sprint 2 should evolve prioritization beyond votes alone.

The scoring architecture should separate four signals:

```text
Votes
  +
Manager Impact Score
  +
Manager Viability Score
  +
AI Evaluation Score
  ↓
Deterministic Priority Score
```

A possible formula is:

```text
priorityScore =
    normalizedVotes * 0.20
  + impactScore     * 0.25
  + viabilityScore * 0.20
  + aiScore         * 0.35
```

The exact weights may change.

Architectural requirements:

- formula is deterministic;
- formula is implemented in application code;
- weights are explicit;
- formula is testable;
- LLM does not calculate hidden business rules;
- final approval remains human-controlled.

---

# 14. Project lifecycle

Projects represent approved initiatives being executed.

Target flow:

```text
Idea APROVADA
      ↓
GESTOR creates project
      ↓
Backend validates:
  - idea exists
  - idea is eligible
  - no project already exists
      ↓
Project created
      ↓
Idea → CONVERTIDA_EM_PROJETO
      ↓
Audit event recorded
```

The relational changes should occur in one transaction.

Project status lifecycle:

```text
PLANEJADO
    ↓
EM_ANDAMENTO
    ↓
CONCLUIDO
```

A project may also become:

```text
CANCELADO
```

according to business rules.

Projects contain operational and financial information such as:

- manager;
- title;
- objective;
- investment;
- expected return;
- ROI;
- cost reduction;
- productivity gain;
- start date;
- expected completion;
- actual completion;
- status.

---

# 15. Persistence architecture

## 15.1 MySQL

MySQL remains the transactional system of record.

It stores business entities that benefit from:

- relations;
- foreign keys;
- uniqueness;
- transactional consistency;
- predictable schemas.

Expected relational data includes:

```text
profiles
users
categories
idea_statuses
strategic_orientations
ideas
idea_votes
project_statuses
projects
badges
user_badges
```

Spring Data JPA / Hibernate accesses these entities.

Flyway owns schema migrations.

---

## 15.2 MongoDB

MongoDB satisfies the Sprint 2 NoSQL requirement through legitimate document use cases.

Target collections:

```text
audit_events
idea_ai_evaluations
```

MongoDB should not become a mirror of MySQL.

---

## 15.3 Why polyglot persistence

The design intentionally uses two persistence technologies.

```text
MySQL
  ↓
strong relational business model
transactions
constraints

MongoDB
  ↓
append-oriented documents
flexible metadata
AI evaluation history
audit history
```

The goal is not "use two databases because the assignment asks for it."

The goal is to give each database a clear responsibility.

---

# 16. Audit architecture

Audit provides traceability for important actions.

An audit event should answer:

```text
WHO?
WHAT?
WHICH RESOURCE?
WHEN?
WHAT CHANGED?
```

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

Suggested events:

```text
USER_LOGIN

IDEA_CREATED
IDEA_UPDATED
IDEA_DELETED
IDEA_PRIORITIZED
IDEA_APPROVED
IDEA_REJECTED

AI_EVALUATION_REQUESTED
AI_EVALUATION_COMPLETED
AI_EVALUATION_FAILED

PROJECT_CREATED
PROJECT_UPDATED
PROJECT_COMPLETED

ORIENTATION_CREATED
ORIENTATION_UPDATED
```

Audit events are supporting evidence, not domain truth.

If audit storage fails, the failure strategy should be decided explicitly rather than accidentally coupling all business operations to MongoDB availability.

---

# 17. AI architecture

Sprint 2 uses AI for **idea evaluation and prioritization support**.

This is intentionally not a chatbot.

## 17.1 AI request flow

```text
GESTOR
  ↓
POST /api/ideas/{id}/ai-evaluation
  ↓
AiIdeaEvaluationService
  ↓
load idea + strategic context
  ↓
build structured AI request
  ↓
AiIdeaEvaluationClient
  ↓
Gemini / OpenAI
  ↓
structured JSON
  ↓
validate
  ↓
map to application model
  ↓
persist evaluation in MongoDB
  ↓
return evaluation DTO
```

---

## 17.2 AI input

The provider may receive:

- idea title;
- description;
- problem;
- proposed solution;
- category;
- strategic orientation;
- manager impact score when available;
- manager viability score when available.

Avoid sending unrelated user data.

---

## 17.3 AI output contract

The backend should require structured output.

Example application-level response:

```json
{
  "ideaId": 42,
  "strategicAlignment": 9,
  "impact": 8,
  "feasibility": 7,
  "innovation": 8,
  "overallScore": 8.1,
  "summary": "The idea strongly supports the selected strategic orientation..."
}
```

Suggested ranges:

```text
strategicAlignment: 0..10
impact:             0..10
feasibility:        0..10
innovation:         0..10
overallScore:       0..10
```

All provider output must be validated.

---

## 17.4 AI persistence

AI evaluation history belongs in MongoDB.

Suggested document:

```json
{
  "ideaId": 42,
  "requestedByUserId": 17,
  "provider": "gemini",
  "model": "...",
  "strategicAlignment": 9,
  "impact": 8,
  "feasibility": 7,
  "innovation": 8,
  "overallScore": 8.1,
  "summary": "...",
  "createdAt": "..."
}
```

Multiple evaluations may exist for the same idea.

This gives the system historical traceability instead of overwriting previous AI analysis.

---

## 17.5 Provider abstraction

Application code should depend on an internal abstraction.

Conceptually:

```text
AiIdeaEvaluationClient
        |
        +------ GeminiAiIdeaEvaluationClient
```

or:

```text
AiIdeaEvaluationClient
        |
        +------ OpenAiIdeaEvaluationClient
```

This avoids coupling service logic to one provider's HTTP schema.

It also makes tests easier because the provider client can be mocked.

---

## 17.6 AI failure behavior

The external provider is not guaranteed to be available.

Failure must not corrupt domain state.

Examples:

```text
provider timeout
malformed JSON
invalid score
HTTP 429
HTTP 500
network error
```

Expected result:

```text
AI evaluation fails
       ↓
controlled API error
       ↓
idea status unchanged
       ↓
no project created
       ↓
operational error logged
       ↓
optional AI_EVALUATION_FAILED audit event
```

---

# 18. Dashboard architecture

The dashboard is an executive reporting concern.

Sprint 1 currently calculates much of this information on the Android side.

Sprint 2 should move aggregation to the backend.

Target request:

```http
GET /api/dashboard/summary
```

Possible response:

```json
{
  "totalProjects": 12,
  "projectsByStatus": {
    "PLANEJADO": 3,
    "EM_ANDAMENTO": 5,
    "CONCLUIDO": 3,
    "CANCELADO": 1
  },
  "totalInvestment": 850000.00,
  "totalFinancialReturn": 1200000.00,
  "totalCostReduction": 210000.00,
  "estimatedResult": 560000.00,
  "averageRoi": 32.7,
  "averageProductivityGain": 11.4
}
```

Additional filtering:

```http
GET /api/dashboard/summary?orientationId={id}
```

Possible project-specific endpoint:

```http
GET /api/dashboard/projects/{projectId}
```

The backend owns the formulas.

Android owns visualization.

---

# 19. Dashboard calculation boundaries

The backend should calculate values such as:

```text
total investment
total return
total cost reduction
estimated result
average ROI
average productivity gain
project counts by status
```

Use `BigDecimal` for monetary values.

Do not make Android recompute authoritative financial/business metrics.

---

# 20. Observability architecture

Sprint 2 requires:

```text
audit
logs
metrics
```

These are related but distinct.

---

## 20.1 Audit

Audit answers:

> What business action happened?

Stored as business-oriented historical events, preferably in MongoDB.

---

## 20.2 Logs

Logs answer:

> What happened inside the application while processing requests?

Use normal Spring/SLF4J logging.

Examples:

```text
authentication failure
forbidden operation
AI provider timeout
database failure
invalid status transition
application startup
```

Do not log:

- passwords;
- password hashes;
- JWT signing secret;
- complete JWT token;
- AI API keys.

Avoid:

```java
System.out.println(...)
```

Use parameterized application logging.

---

## 20.3 Metrics

Metrics answer:

> How is the application behaving over time?

Spring Boot Actuator / Micrometer should expose at least:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Optional custom metrics may include:

```text
AI evaluation count
AI provider failures
ideas approved
projects created
```

---

# 21. Error-handling architecture

The API should return predictable error behavior.

Target semantics:

```text
400 — invalid request / validation
401 — not authenticated
403 — authenticated but forbidden
404 — resource not found
409 — conflicting business state where appropriate
500 — unexpected internal error
502/503 — external provider failure where appropriate
```

The project should use centralized exception handling rather than ad-hoc controller error bodies.

Do not expose internal stack traces or secrets to Android.

---

# 22. Transaction boundaries

Use relational transactions when multiple MySQL changes represent one business operation.

Example:

```text
Create project
   ↓
INSERT project
   +
UPDATE idea status
```

These relational operations should succeed or fail together.

MongoDB audit/history writes introduce a separate persistence technology and therefore are not automatically part of the same relational transaction.

Avoid pretending the system has a distributed transaction unless one is deliberately implemented.

For this academic project, prefer clear failure behavior over introducing distributed-transaction complexity.

---

# 23. API architecture

Primary communication style:

```text
REST
JSON
JWT Bearer authentication
```

Swagger/OpenAPI documents the public backend contract.

The API should use stable resource-oriented paths.

Examples:

```text
/api/ideas
/api/projects
/api/orientations
/api/dashboard
/api/ideas/{id}/ai-evaluation
```

Do not expose provider-specific AI endpoints to the mobile client.

For example, prefer:

```text
/api/ideas/{id}/ai-evaluation
```

over:

```text
/api/gemini/analyze
```

The application owns the use case; the provider is an implementation detail.

---

# 24. Configuration architecture

Secrets and environment-specific values must remain outside source control.

Expected external configuration includes:

```text
MySQL
  DB_URL
  DB_USERNAME
  DB_PASSWORD

MongoDB
  MONGODB_URI
  MONGODB_DATABASE

JWT
  JWT_SECRET
  JWT_EXPIRATION

AI
  AI_API_KEY
  AI_MODEL
  AI_BASE_URL          # if required by provider
```

Variable names should ultimately match the actual repository configuration.

Do not duplicate conflicting configuration names simply to match this document.

Use safe placeholders in:

```text
.env.example
README.md
```

Never commit real credentials.

---

# 25. Flyway ownership

Flyway owns relational schema evolution.

Rules:

```text
existing migration → immutable
new relational schema change → new migration
```

Examples:

```text
V12__add_idea_review_scores.sql
V13__add_project_stage.sql
```

Do not edit migrations that have already been applied in normal development.

MongoDB collections do not use Flyway.

---

# 26. Testing architecture

Sprint 2 should introduce meaningful tests around business risk rather than chasing arbitrary percentage coverage.

Priority test areas:

```text
Security
  ├── authentication
  ├── roles
  └── ownership

Ideas
  ├── creation
  ├── voting
  ├── review scores
  ├── transitions
  └── priority score

Projects
  ├── eligibility
  ├── uniqueness
  └── idea conversion

Dashboard
  └── exact aggregate calculations

AI
  ├── valid response
  ├── malformed response
  ├── provider outage
  └── persistence

Audit
  └── critical event creation
```

Normal automated tests should not depend on a real paid AI call.

Mock or stub the AI client boundary.

Use Testcontainers for database-sensitive integration tests where practical.

---

# 27. Current known architectural gaps

The current codebase contains several known gaps that Sprint 2 should address.

## Security

- idea ownership is not consistently enforced;
- some administrative APIs are authenticated without sufficient role restrictions;
- Android currently exposes sensitive authorization information in logging.

## Idea prioritization

- impact and viability exist conceptually but do not yet form a complete manager-review workflow;
- priority is currently heavily driven by votes.

## Project conversion

- project creation does not fully enforce approved-idea eligibility;
- project creation does not yet guarantee the intended transition to `CONVERTIDA_EM_PROJETO`.

## Android/backend contract

- idea status naming is inconsistent between backend and Android (`statusIdeia` vs `status`).

## Dashboard

- dashboard aggregation currently belongs too heavily to Android;
- "projects in progress" has an incorrect semantic/counting behavior.

## Badges

- earned badges may still appear in the available catalog.

## Testing

- current automated backend verification is too limited for Sprint 2 security and business behavior.

These should be treated as existing technical debt, not as evidence that the whole application needs to be redesigned.

---

# 28. Non-goals for Sprint 2

Unless the assignment changes, Sprint 2 does not require:

- microservices;
- Kubernetes;
- Kafka;
- event sourcing;
- CQRS;
- distributed transactions;
- GraphQL;
- replacing REST;
- replacing MySQL;
- rewriting Android;
- Kotlin backend;
- autonomous AI agents;
- chatbot functionality;
- refresh-token architecture;
- large-scale admin portal redesign.

Prefer a small number of well-implemented architectural decisions.

---

# 29. Final Sprint 2 interaction flow

The final demonstration should be explainable as three role journeys.

## OPERACIONAL

```text
Login
  ↓
View strategic orientations
  ↓
Create idea
  ↓
Associate idea with strategy/category
  ↓
Vote / participate
  ↓
Track idea/project information
```

---

## GESTOR

```text
Login
  ↓
Review submitted ideas
  ↓
Set impact + viability
  ↓
Request AI evaluation
  ↓
Review structured AI analysis
  ↓
Prioritize
  ↓
Approve / reject
  ↓
Create project from approved idea
  ↓
Manage execution/results
```

---

## LIDER

```text
Login
  ↓
Manage strategic orientations
  ↓
View executive dashboard
  ↓
Global results
  ↓
Filter by strategic orientation
  ↓
Inspect project outcomes
```

Supporting the entire flow:

```text
JWT security
MySQL transactions
MongoDB audit
AI history
logs
metrics
Swagger
tests
```

---

# 30. Architectural source of truth

When implementation questions arise, use the documents in this order:

```text
Assignment requirements
        ↓
architecture.md
        ↓
roadmap.md
        ↓
existing working code
        ↓
AGENTS.md workflow rules
```

If the existing code contradicts the Sprint 2 target architecture, do not silently rewrite it.

First determine whether the mismatch is:

- intentional;
- legacy Sprint 1 behavior;
- a bug;
- an undocumented architectural decision.

Then update the implementation deliberately.

If an architectural decision changes during Sprint 2, update this document so future Codex sessions do not rely on stale assumptions.
