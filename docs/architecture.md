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
                │ transactions   │ │ AI history   │ │ Google        │
                │                │ │              │ │ Gemini        │
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

Task 1.4 introduced a separate `POST /usuarios/admin` route restricted to
`LIDER` for provisioning accounts with any of the three existing profiles. Reuse
account validation and BCrypt hashing, but keep public and privileged authorization
paths explicit. Ordinary profile updates never promote users. The first leader
must be provisioned by a trusted deployment operator through a controlled local
setup using an externally supplied password and BCrypt hash, not an unauthenticated
HTTP bootstrap route or committed default credentials. The reproducible procedure
is documented in the README; already-existing valid leader accounts can be used.

This resolves the previously undocumented provisioning decision. `SecurityConfig`
permits only the exact public registration route, and `UsuarioService` validates and
selects `OPERACIONAL` rather than trusting privileged input. The separate admin
service path may use the requested existing profile after the leader role check.

## 9.2 Complete current controller inventory

All **65 explicitly mapped operations across 13 controllers** were inspected through
2026-09-11. Existing Sprint 1 paths remain Portuguese; the new dashboard route uses
the documented `/api/dashboard` contract. Query filters do not change policy.
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
| UsuarioController | `POST /usuarios` | Public, creates OPERACIONAL only | Implemented | 400/no insert for privileged input verified; 1.4 |
| UsuarioController | `POST /usuarios/admin` | LIDER; provision any existing role | Implemented | Authentication/role and delegation verified; 1.4 |
| UsuarioController | `GET /usuarios` | LIDER | Implemented | Role verified; 1.4 |
| UsuarioController | `GET /usuarios/{id}` | Self or LIDER | Implemented | Self, leader, and cross-user denial verified; 1.4 |
| UsuarioController | `PUT /usuarios/{id}` | Self or LIDER | Implemented | Authorization ordering and protected fields verified; 1.4 |
| UsuarioController | `DELETE /usuarios/{id}` | LIDER | Implemented | Role verified; relational constraints unchanged; 1.4 |
| CategoriaController | `POST /categorias` | LIDER | Implemented | Role verified; 1.4 |
| CategoriaController | `GET /categorias` | Authenticated | Implemented | Auth; 1.4 |
| CategoriaController | `GET /categorias/{id}` | Authenticated | Implemented | Auth; 1.4 |
| CategoriaController | `PUT /categorias/{id}` | LIDER | Implemented | Role verified; 1.4 |
| CategoriaController | `DELETE /categorias/{id}` | LIDER | Implemented | Role verified; 1.4 |
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
| IdeiaController | `PATCH /ideias/{id}/status` | GESTOR | Implemented | Role and explicit state-transition policy verified; 1.2 and 4.2 |
| IdeiaController | `PATCH /ideias/{id}/avaliacao` | GESTOR | Implemented | Role, `0..10` validation, persistence, and audit verified; 4.1 |
| VotoIdeiaController | `POST /votos-ideias/ideia/{ideiaId}` | Authenticated, own vote only | Implemented | Auth, actor from principal, duplicate vote; 11.2 |
| VotoIdeiaController | `DELETE /votos-ideias/ideia/{ideiaId}` | Authenticated, own vote only | Implemented | Auth, other votes unchanged; 11.2 |
| VotoIdeiaController | `GET /votos-ideias` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/{id}` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/ideia/{ideiaId}` | Authenticated | Implemented | Auth; 11.2 |
| VotoIdeiaController | `GET /votos-ideias/usuario/{usuarioId}` | Authenticated | Implemented | Auth; 11.2 |
| ProjetoController | `POST /projetos` | GESTOR | Implemented | Role, approved-idea eligibility, uniqueness, and transactional conversion verified; 1.2 and 4.3 |
| ProjetoController | `GET /projetos` | Authenticated | Implemented | Auth; 1.2 |
| ProjetoController | `GET /projetos/{id}` | Authenticated | Implemented | Auth plus complete project-result contract and controlled missing resource verified; 1.2 and 6.4 |
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
| UsuarioBadgeController | `POST /usuario-badges/usuario/{usuarioId}/badge/{badgeId}` | GESTOR | Implemented | Role verified; 1.4 |
| UsuarioBadgeController | `POST /usuario-badges/usuario/{usuarioId}/nome/{nomeBadge}` | GESTOR | Implemented | Role and no grant-by-name bypass verified; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges` | GESTOR/LIDER | Implemented | Role verified; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/{id}` | Self or GESTOR/LIDER | Implemented | Ownership uses award recipient, not award ID; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/usuario/{usuarioId}` | Self or GESTOR/LIDER | Implemented | Self/cross-user/role override verified; 1.4 |
| UsuarioBadgeController | `GET /usuario-badges/badge/{badgeId}` | GESTOR/LIDER | Implemented | Role verified; 1.4 |
| UsuarioBadgeController | `DELETE /usuario-badges/usuario/{usuarioId}/badge/{badgeId}` | GESTOR | Implemented | Role verified; 1.4 |
| StatusIdeiaController | `GET /status-ideias` | Authenticated | Implemented | Auth; 1.4 |
| StatusIdeiaController | `GET /status-ideias/{id}` | Authenticated | Implemented | Auth; 1.4 |
| StatusProjetoController | `GET /status-projetos` | Authenticated | Implemented | Auth; 1.4 |
| StatusProjetoController | `GET /status-projetos/{id}` | Authenticated | Implemented | Auth; 1.4 |
| DashboardController | `GET /api/dashboard/summary[?orientationId={id}]` | LIDER | Implemented | Controller-wide role policy plus global and filtered `401`/`403`/`200` behavior verified; 6.2–6.5 |
| HelloWorldController | `GET /api/english` | Authenticated | Implemented | Auth; legacy diagnostic, no new privilege; 1.2 |
| HelloWorldController | `GET /api/deutsch` | Authenticated | Implemented | Auth; legacy diagnostic, no new privilege; 1.2 |

## 9.3 Planned operations and infrastructure boundaries

These are target policies, separate from the existing inventory. Add exact route
rows when their owning roadmap tasks introduce the controllers.

| Operation / endpoint | Target policy | Current state | Owning task |
|---|---|---|---|
| `POST /api/ideas/{ideaId}/ai-evaluation` | GESTOR | Implemented; `401`/`403`/`200` and controlled failure behavior verified | 8.1 |
| AI history read endpoint, if exposed | GESTOR only; internal scoring service may read persisted results | No HTTP route exposed; internal Mongo repository implemented | 8.2 |
| Audit events | Internal service writes only; no client read/write API in Sprint 2 | Implemented for current domain and AI-evaluation operations | 3.1–3.2, 8.1 |
| `GET /actuator/health` | Public, overall status only; no component details | Implemented and integration-tested | 10.1 |
| `GET /actuator/info`, `GET /actuator/metrics`, `GET /actuator/metrics/{name}` | LIDER, sanitized metadata/metrics only | Implemented and integration-tested | 10.1 |

Other Actuator endpoints must remain unexposed. Swagger UI and OpenAPI GET routes
may be public only in an explicitly enabled local/development documentation profile;
they must be disabled in non-development deployments unless a separate policy is
documented. Task 12.1 implements this through the explicit `local` profile: base
configuration disables Springdoc, while `application-local.properties` enables
it and lets `SecurityConfig` permit only its GET routes. Public login and public
operational registration omit the global bearer requirement; every other API
operation inherits it.
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

Task 9.4 centralizes these client decisions in a fail-closed `RoleUiPolicy`.
Authenticated roles share orientation, idea, project, badge, and voting access.
The operational, manager, and leader mutation capabilities exactly match the
matrix above. Unknown roles and missing-token sessions receive no protected
capabilities. `AppNavigation` uses the policy both to build its bottom navigation
and to guard every privileged create/edit/dashboard destination before the screen
and its ViewModel are instantiated. The own-idea action additionally compares the
session user ID with the response owner ID.

Client guards are never security evidence by themselves. The focused Task 9.4
server verification executes MVC authorization for project/orientation reads and
mutations and reruns the established idea, AI, dashboard, catalog, user, category,
and badge security suites. Wrong roles receive `403`, missing authentication
receives `401`, and allowed roles retain their documented success responses. The
remaining full real-user/device journeys belong to Phases 13 and 14.

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

The backend enforces this lifecycle in `IdeiaService`. The manager-facing
`PATCH /ideias/{id}/status` endpoint may perform the review transitions through
`APROVADA` or `REJEITADA`, but it must reject a direct request for
`CONVERTIDA_EM_PROJETO`, even when the current status is `APROVADA`. Conversion is
not a standalone manager decision: Task 4.3 owns `APROVADA ->
CONVERTIDA_EM_PROJETO` as part of the same relational transaction that creates the
single associated project. Invalid and repeated transitions produce the standard
structured `400 Bad Request` contract and do not mutate or audit the idea.

---

# 13. Idea review and prioritization

Sprint 2 should evolve prioritization beyond votes alone.

Manager review is exposed through:

```http
PATCH /ideias/{id}/avaliacao
```

Only `GESTOR` may call this endpoint. Both `impacto` and `viabilidade` are required
integer values in the inclusive range `0..10`. Task 4.1 introduced persistence of
these human signals; Task 8.3 subsequently connected the centralized deterministic
formula so manager review also recalculates `prioridade` without duplicating the
scoring rule.

Task 9.1 connects this contract to Android. `IdeiaDto` and the domain model retain
the two manager inputs and decimal priority returned by the backend.
`IdeiaRepository` sends the dedicated review request, while `IdeiaViewModel` owns
the dialog state, validates both inclusive ranges before the network call, and
updates the displayed idea from the persisted response. `IdeiasScreen` exposes the
review dialog only when `SessionManager.podeAvaliarIdeia()` identifies a `GESTOR`.
This visibility rule is a convenience boundary; Spring Security remains
authoritative and rejects other roles. Reopening the application reloads the
scores from MySQL through the normal idea-list request rather than local storage.

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

Task 8.3 uses the following authoritative formula:

```text
normalizedVotes = min(max(totalVotes, 0), 10)

priorityScore =
    normalizedVotes * 0.20
  + impactScore     * 0.25
  + viabilityScore * 0.20
  + aiScore         * 0.35
```

All four inputs use the `0..10` scale. Vote normalization uses a fixed cap of ten,
so a vote on an unrelated idea cannot change this idea's score. Missing impact,
viability, or AI values contribute zero. The AI input is the `overallScore` from
the most recently completed evaluation for the idea. The application rounds only
the final weighted result to two decimal places using `HALF_UP`; it does not round
the individual weighted components. Equal evaluation timestamps use descending
document ID as a stable selection tie-breaker. `nr_prioridade` is stored in MySQL as
`DECIMAL(4,2)`; MongoDB retains the AI history and supplies the latest successful
`overallScore` when votes or manager scores trigger recalculation.

Priority must be recalculated after vote creation, vote removal, manager review,
and a successfully completed AI evaluation. The same stored inputs must therefore
always produce the same result regardless of which of those operations triggered
the calculation. The formula is application-owned and must never approve, reject,
create a project, or otherwise change the idea workflow status.

Architectural requirements:

- formula is deterministic;
- formula is implemented in application code;
- weights are explicit;
- vote normalization is local to the idea and capped at ten;
- missing human or AI inputs contribute zero;
- only the final result is rounded to two decimals with `HALF_UP`;
- the most recently completed AI evaluation supplies `aiScore`;
- vote, manager-review, and successful AI-evaluation writes trigger recalculation;
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

The relational changes occur in one Spring-managed transaction in
`ProjetoService.criarProjeto`. `IdeiaService` owns validation and application of
the `APROVADA -> CONVERTIDA_EM_PROJETO` transition. The project is initially saved
as `PLANEJADO`, the idea status and update timestamp are changed, and the JPA
persistence context is flushed before the success audit is requested. Runtime
persistence failures therefore roll back both MySQL changes and do not emit a
successful `PROJECT_CREATED` event.

Uniqueness is enforced twice: the service rejects an already-linked idea with a
controlled conflict, while `t_projetos.id_ideia` retains its database unique
constraint for concurrent or bypass attempts.

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

### 16.1 Audit failure policy

Audit persistence is synchronous and best-effort. `AuditService` validates required
event fields before attempting persistence, but catches Spring `DataAccessException`
failures from MongoDB, records an `ERROR` log containing only event/resource
identifiers and the exception type, and does not fail an otherwise successful
relational business operation. Programming and validation errors are not swallowed.

Domain services must request the audit write only after their relational operation
succeeds. This does not provide a distributed transaction between MySQL and
MongoDB; the audit document remains supporting evidence rather than business truth.

### 16.2 Audit trigger ownership

The authenticated caller is always the audit actor. Domain ownership or assignment
is separate metadata; for example, a manager creating a project remains the actor
even when another manager is assigned as the project's responsible user.

Current trigger mapping:

| Workflow result | Audit event |
|---|---|
| Successful login | `USER_LOGIN` |
| Idea created, updated, or deleted | `IDEA_CREATED`, `IDEA_UPDATED`, `IDEA_DELETED` |
| Idea prioritized, approved, or rejected | `IDEA_PRIORITIZED`, `IDEA_APPROVED`, `IDEA_REJECTED` |
| Project created or edited | `PROJECT_CREATED`, `PROJECT_UPDATED`; creation records the idea's conversion statuses |
| Project status changed to completed | `PROJECT_COMPLETED` |
| Orientation created, edited, or deactivated | `ORIENTATION_CREATED`, `ORIENTATION_UPDATED` |
| AI evaluation requested, completed, or failed | `AI_EVALUATION_REQUESTED`, `AI_EVALUATION_COMPLETED`, `AI_EVALUATION_FAILED` |

Rejected authentication and rejected authorization attempts do not create these
success events. `AiIdeaEvaluationService` owns the three AI events and records the
authenticated manager as actor. Its failure metadata is deliberately limited to
provider, model, and failure classification; it excludes credentials and raw
provider responses.

---

# 17. AI architecture

Sprint 2 uses AI for **idea evaluation and prioritization support**.

This is intentionally not a chatbot.

## 17.1 Selected provider

Task 7.1 selects the **Google Gemini Developer API free tier** and the stable
`gemini-3.6-flash` model for Sprint 2. The Flash family matches the academic
project's priorities: no-cost development availability, low-latency evaluation,
structured-output support, and enough contextual reasoning for a single idea and
its strategic context. The selected model supports structured outputs and has an
input limit of 1,048,576 tokens; this is a token context limit, not a count of
model parameters.

The originally considered `gemini-1.5-flash` identifier is not usable: Google
shut the Gemini 1.5 models down on 2025-09-29. The implementation must use the
explicit stable `gemini-3.6-flash` identifier rather than `gemini-flash-latest`,
whose target may change. Provider availability, pricing, model lifecycle, and
rate limits must be checked before demonstrations or deployment because Google
may change them. The application must not assume a permanent 15 RPM quota; active
limits are model- and project-specific and are visible in Google AI Studio.

Official references:

- [Gemini 3.6 Flash model](https://ai.google.dev/gemini-api/docs/models/gemini-3.6-flash)
- [Gemini model lifecycle](https://ai.google.dev/gemini-api/docs/deprecations)
- [Gemini API pricing](https://ai.google.dev/gemini-api/docs/pricing)
- [Gemini API rate limits](https://ai.google.dev/gemini-api/docs/rate-limits)
- [Gemini structured outputs](https://ai.google.dev/gemini-api/docs/structured-output)

The configuration contract implemented by Task 7.4 is:

| Environment variable | Required | Planned default / purpose |
|---|---|---|
| `AI_ENABLED` | No | `false`; keeps ordinary local startup independent of an AI credential. |
| `AI_PROVIDER` | No | `gemini`; identifies the selected provider. |
| `AI_API_KEY` | Only when enabled | Google AI Studio key; transmitted in `x-goog-api-key` and never logged. |
| `AI_MODEL` | No | `gemini-3.6-flash`; explicit stable model identifier. |
| `AI_BASE_URL` | No | `https://generativelanguage.googleapis.com/v1beta`. |
| `AI_CONNECT_TIMEOUT_MS` | No | `3000`; maximum connection establishment time. |
| `AI_READ_TIMEOUT_MS` | No | `15000`; maximum wait for the non-streaming evaluation response. |

The REST operation is
`POST {AI_BASE_URL}/models/{AI_MODEL}:generateContent`, authenticated only from
the backend. Task 7.3 owns the client, while Task 7.4 binds the variables through
typed `app.ai` properties, registers the client only when enabled, and documents
safe placeholders in `.env.example`.

## 17.2 AI request flow

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
Google Gemini
  ↓
structured JSON
  ↓
validate
  ↓
map to application model
  ↓
persist evaluation in MongoDB
  ↓
calculate and store priority in MySQL
  ↓
audit completed evaluation
  ↓
return evaluation DTO
```

Task 8.1 implements the flow through validated DTO return plus the three audit
outcomes. The service obtains the provider client through `ObjectProvider`, so the
ordinary application still starts when `AI_ENABLED=false`; a request in that mode
returns controlled `503 Service Unavailable`. The provider call is not wrapped in
a MySQL transaction. Task 8.2 persists each validated result in MongoDB before a
successful response. Task 8.3 then calculates and stores the deterministic
application priority in MySQL before the completed audit and HTTP response.

---

## 17.3 AI input

Task 7.2 defines the provider-independent `AiIdeaEvaluationContext` record. Its
fields and validation rules are:

| Field | Type | Contract rule |
|---|---|---|
| `title` | `String` | Required, nonblank, trimmed. |
| `description` | `String` | Required, nonblank, trimmed. |
| `problem` | `String` | Optional for compatibility with legacy ideas; blank becomes `null`. |
| `proposedSolution` | `String` | Optional for compatibility with legacy ideas; blank becomes `null`. |
| `category` | `String` | Required, nonblank, trimmed. |
| `strategicOrientation` | `String` | Required, nonblank, trimmed. |
| `managerImpactScore` | `Integer` | Optional; when present, inclusive `0..10`. |
| `managerViabilityScore` | `Integer` | Optional; when present, inclusive `0..10`. |

`AiIdeaEvaluationService` derives this context from trusted MySQL entities. It
sends the idea content, category, strategic orientation, and existing manager
scores only; user identity, credentials, audit data, and unrelated domain content
are excluded.

---

## 17.4 AI output contract

The backend requires structured output and separates model-owned evaluation data
from application-owned resource identity:

- `AiIdeaEvaluationResult` is the provider-independent result returned by the AI
  client. It contains `strategicAlignment`, `impact`, `feasibility`, `innovation`,
  `overallScore`, and `summary`.
- `AiIdeaEvaluationResponse` is the HTTP response. It flattens the validated
  result and adds the trusted `ideaId` supplied by application code.

The four dimension scores use nullable `Integer` components so a missing JSON
field cannot silently become zero. Every dimension is required and must be in the
inclusive `0..10` range. `overallScore` is a required `BigDecimal` in the same
range, preserving provider decimal precision. `summary` is required, nonblank,
and trimmed. Response IDs must be positive. All violations fail construction with
`IllegalArgumentException`; the Task 7.3 client translates provider
mapping/validation failures into its controlled integration exception.

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

Required ranges:

```text
strategicAlignment: 0..10
impact:             0..10
feasibility:        0..10
innovation:         0..10
overallScore:       0..10
```

Gemini must never generate or select `ideaId`. The application combines its known
idea ID with a validated result through `AiIdeaEvaluationResponse.from(...)`.
Provider-specific request and response envelopes remain internal to the Task 7.3
Gemini client and must not leak into this contract.

Task 9.2 consumes this application contract in Android through
`AiIdeaEvaluationDto` and maps it to the provider-independent
`AiIdeaEvaluation` UI model. The mapper independently enforces the positive idea
ID, every required `0..10` score, decimal overall score, and nonblank trimmed
summary. It also verifies that the response idea ID matches the idea requested.
The expanded idea detail presents the six result fields in a bounded dialog with
loading, failure/retry, and success states. Android calls only the Spring Boot
endpoint; the Gemini key and provider schema never enter the mobile application.

---

## 17.5 AI persistence

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

Task 8.2 implements this contract as the immutable `AiIdeaEvaluation` document in
the `idea_ai_evaluations` collection. Each evaluation has an application-generated
UUID, and `AiIdeaEvaluationRepository` provides
`findAllByIdeaIdOrderByCreatedAtDesc(...)` for internal historical lookup. Saving
a new document for every successful request retains multiple evaluations rather
than overwriting the previous analysis.

History persistence occurs after provider-output validation and before the
completed audit event and HTTP success response. A MongoDB data-access failure is
wrapped as `AiEvaluationPersistenceException`, maps to controlled `503 Service
Unavailable`, and follows the failed audit path. There is no public history-read
endpoint in Sprint 2.

---

## 17.6 Provider abstraction

Application code should depend on an internal abstraction.

Conceptually:

```text
AiIdeaEvaluationClient
        |
        +------ GeminiAiIdeaEvaluationClient
```

This avoids coupling service logic to one provider's HTTP schema.

It also makes tests easier because the provider client can be mocked.

Task 7.3 implements this boundary with `AiIdeaEvaluationClient` and
`GeminiAiIdeaEvaluationClient`. The Gemini adapter uses Spring `RestClient`, sends
the key only through `x-goog-api-key`, applies JDK HTTP connection and read
timeouts, requests `application/json` with `responseJsonSchema`, and converts the
provider envelope into `AiIdeaEvaluationResult`. For the bounded evaluation, the
request uses `thinkingLevel=medium` and a 4096-token maximum covering both thinking
and visible output. The medium level is a deliberate product choice so the
evaluation can reason more fully about the idea and strategic context while the
explicit output limit prevents unbounded generation. Provider envelopes remain
private to the adapter. Task 7.4 registers
the adapter conditionally: no AI client bean exists when `AI_ENABLED=false`, while
enabled configuration validates the provider, key, model, HTTPS base URL, and
positive timeouts before startup completes.

---

## 17.7 AI failure behavior

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

Task 8.1 implements this boundary. Provider timeout maps to `504`; rate limiting,
provider outage, and disabled AI map to `503`; rejected requests and invalid
provider output map to `502`; request-construction failure maps to `500`. These
responses use the application error DTO and never include the raw provider body.
The adapter logs safe structural diagnostics such as `finishReason=MAX_TOKENS` or
the validation exception type, but never logs the prompt, generated content, or
API key.

Task 9.2 gives the Android caller controlled messages for authentication,
authorization, missing ideas, invalid provider output, unavailable service, and
backend/client timeouts without rendering raw exception or provider content. The
result dialog can be retried after a failure and cannot be dismissed while the
request is in flight. Its 30-second HTTP read timeout exceeds the backend's
default 15-second AI read timeout, preserving the backend's controlled response
whenever that timeout is reached.

---

## 17.8 Human decision boundary

Task 8.4 keeps AI evaluation strictly advisory. The AI operation may persist an
evaluation document in MongoDB and update the deterministic `prioridade` value in
MySQL. It must not call the idea-status workflow, create a project, or return an
approval, rejection, workflow status, or project identifier in its response.

Governance remains split into explicit `GESTOR` commands:

```text
POST /api/ideas/{id}/ai-evaluation
        -> analysis + priority only

PATCH /ideias/{id}/status
        -> explicit manager approval/rejection decision

POST /projetos
        -> explicit manager conversion of an APROVADA idea
```

`IdeiaService.atualizarStatusIdeia` remains the only manager-facing idea-status
operation. `ProjetoService.criarProjeto` separately validates `APROVADA`, creates
the project transactionally, and applies `CONVERTIDA_EM_PROJETO`. The AI response
contract contains evaluation scores and a summary only. Success, disabled-provider,
provider-failure, MongoDB-failure, and priority-persistence-failure tests preserve
the idea's status; the AI orchestration tests also allow-list its only interactions
with `IdeiaService` as idea lookup and, after successful persistence, priority
update.

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

Project-specific results reuse the existing project-detail contract:

```http
GET /projetos/{projectId}
```

A separate dashboard route would duplicate data already returned by that endpoint,
so Task 6.4 deliberately does not introduce one.

The backend owns the formulas.

Android owns visualization.

## 18.1 Dashboard summary contract

Task 6.1 fixes the backend response boundary as the Java record
`DashboardSummaryResponse`, under the existing `dto.dashboard` package.
Its public JSON fields and Java types are:

| JSON field | Java type | Contract rule |
|---|---|---|
| `totalProjects` | `long` | Non-negative and equal to the sum of all status counts. |
| `projectsByStatus` | `Map<NomeStatusProjeto, Long>` | Contains every project status, including zero-count statuses, and is immutable after construction. |
| `totalInvestment` | `BigDecimal` | Required decimal value. |
| `totalFinancialReturn` | `BigDecimal` | Required decimal value. |
| `totalCostReduction` | `BigDecimal` | Required decimal value. |
| `estimatedResult` | `BigDecimal` | Required decimal value and may represent a negative result. |
| `averageRoi` | `BigDecimal` | Required decimal value. |
| `averageProductivityGain` | `BigDecimal` | Required decimal value. |

The status keys are the existing enum names `PLANEJADO`, `EM_ANDAMENTO`,
`CONCLUIDO`, and `CANCELADO`. The DTO validates structural consistency but does
not calculate business values. Aggregation formulas remain the responsibility of
the dashboard service introduced by Task 6.2. Task 6.4 confirmed the existing
project-detail contract for project-specific visualization. Task 9.3 now maps this
contract in Android without converting its decimal values to `Double` or
recalculating its metrics. The dashboard controller is leader-only through a
class-level authorization rule, verified by Task 6.5.

## 18.2 Global aggregation rules

`DashboardService.buscarResumo(...)` reads the persisted projects through
`ProjetoRepository` and calculates the authoritative global summary in the
backend. The formulas fixed by Task 6.2 are:

```text
totalInvestment = sum(investment)
totalFinancialReturn = sum(financial return)
totalCostReduction = sum(cost reduction)
estimatedResult = totalFinancialReturn + totalCostReduction - totalInvestment
averageRoi = sum(ROI) / totalProjects
averageProductivityGain = sum(productivity gain) / totalProjects
```

Status counts include every persisted project. Decimal fields left `null` by
legacy data are interpreted as zero, matching the current creation service's
zero-default rule. Empty datasets return zero for every metric. Averages include
all projects and are rounded to two decimal places with `RoundingMode.HALF_UP`.
The response is exposed at `GET /api/dashboard/summary` and is restricted to
`LIDER`; the controller delegates all calculation to the service.

## 18.3 Strategic-orientation filter

The optional `orientationId` query parameter scopes the same summary contract and
formulas to projects linked through:

```text
Project -> Idea -> StrategicOrientation
```

`ProjetoRepository.findByIdeia_OrientacaoEstrategica_Id(...)` performs this
filter in MySQL, so unrelated projects are not loaded for filtered requests. An
omitted parameter returns the global summary. An existing orientation without
projects returns the complete zero-valued summary; an unknown orientation returns
the standard controlled `404 Not Found`. Active and inactive orientations remain
queryable because historical executive results must not disappear when an
orientation is deactivated.

## 18.4 Project-specific result contract

Task 6.4 evaluated the optional dashboard-specific route and found no additional
DTO value. `GET /projetos/{projectId}` already returns `MostrarProjetoDTO`, which
contains the project and linked-idea identity, responsible user, status, title,
description, objective, investment, ROI, financial return, cost reduction,
productivity gain, delivery dates, and audit timestamps. Those stored values are
the complete per-project input needed by the existing Android project-result view.

The route keeps the established project-read policy: every authenticated role may
view it, including `LIDER`, while unauthenticated callers receive `401`. A missing
project uses the centralized `404` response. Android already consumes the same
contract through `ProjetoApi.buscarProjetoPorId`, `ProjetoDto`, and
`ProjetoRepository`, and `ProjetosScreen` renders the result metrics. Therefore no
duplicate `GET /api/dashboard/projects/{projectId}` route is part of the target
architecture.

## 18.5 Dashboard authorization boundary

`DashboardController` declares `@PreAuthorize("hasRole('LIDER')")` at class level.
Every current and future handler added to `/api/dashboard` therefore inherits the
executive role restriction by default. `SecurityConfig` still requires
authentication globally and has method security enabled. Consequently, requests
without valid authentication receive `401`, authenticated `OPERACIONAL` and
`GESTOR` callers receive `403`, and only `LIDER` reaches `DashboardService`.

The policy applies equally when `orientationId` is omitted or supplied. The
existing `/projetos/{id}` contract reused by Task 6.4 remains governed by the
separate project-read policy and is not a dashboard endpoint.

## 18.6 Android dashboard consumption

Task 9.3 introduces an Android `DashboardApi`, validated `DashboardSummaryDto`,
provider-independent `DashboardSummary` model, and `DashboardRepository`.
`DashboardViewModel` now stores the returned summary directly: it contains no
sum, average, ROI, estimated-result, or project-status grouping formula. The UI
reads `EM_ANDAMENTO` directly from `projectsByStatus` and formats the backend's
`BigDecimal` metrics only for display.

The first request omits `orientationId` for the global view. The leader can select
any active or inactive strategic orientation from the complete authenticated
catalog; the selected ID is then forwarded to
`GET /api/dashboard/summary?orientationId={id}`. A sequence guard prevents a
slower earlier response from replacing a newer selection. A known orientation
with no projects renders an empty filtered state, while an unknown ID uses the
backend's controlled `404` response. Project-specific visualization remains the
existing `GET /projetos/{projectId}` flow and is not recomputed in this screen.

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

Task 10.2 implements the operational failure boundary as follows:

- the Spring Security entry point and access-denied handler log HTTP method,
  request path, and controlled exception type for `401` and `403` responses;
- the centralized exception handler logs rejected login, service authorization,
  invalid workflow, generic Spring data-access, and priority persistence failures;
- AI orchestration logs idea ID, provider, and controlled provider-failure type;
- audit persistence logs event type and resource identifiers.

Known rejections use `WARN`; database and priority persistence failures use
`ERROR`. Raw exception text from authentication and database failures is not
logged because it can contain submitted credentials, connection information, or
driver diagnostics. Requests are diagnosed through method, path, safe business
reason where the message is application-controlled, resource identifiers, and
failure classification. Focused log-capture tests verify that JWTs, Authorization
headers, passwords, database URLs/passwords, and AI API keys are absent.

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

Task 10.3 adds these custom metrics:

```text
aguiaexpress.ai.evaluation.duration     timer, outcome=success|failure
aguiaexpress.ai.evaluation.failures    counter
aguiaexpress.ideas.approved             counter
aguiaexpress.projects.created           counter
```

The AI timer measures the complete validated evaluation orchestration and its
built-in count represents the number of completed attempts. An attempt begins
only after the idea and authenticated actor have been resolved. Failures include
controlled provider, configuration, AI-history persistence, priority-persistence,
and unexpected orchestration failures. Missing ideas and authentication failures
do not start an AI measurement.

An idea is counted only when a valid manager transition reaches `APROVADA`. A
project is counted only after the relational project-creation transaction commits;
rollbacks never increment the counter. Metric tags remain bounded to the two AI
outcomes and must not contain idea/user identifiers, exception messages, JWTs, or
other secrets. The counters represent the current application process and reset
when that process restarts.

Task 10.1 implements the baseline with Spring Boot Actuator and Micrometer.
HTTP exposure is an explicit allowlist containing only `health`, `info`, and
`metrics`; JMX exposure and the Actuator discovery page are disabled. Public
health output never includes component names or details, and health component
subpaths are denied. Safe application metadata and built-in metrics require an
authenticated `LIDER`. Five isolated integration tests use the real JWT filter,
security chain, and Actuator endpoints to verify the `200`/`401`/`403`/`404`
contract without depending on MySQL or MongoDB.

Task 10.3 centralizes custom instrumentation in `BusinessMetrics`; focused meter,
service, transaction-synchronization, and Actuator tests verify the metric
lifecycle and retain the Task 10.1 leader-only read policy.

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

The contract defines `bearerAuth` as an HTTP bearer scheme with JWT format.
Swagger/OpenAPI is disabled by default and available only when the `local` Spring
profile is explicitly active. The local routes are:

```text
/swagger-ui/index.html
/swagger-ui.html
/v3/api-docs
```

The generated schemas preserve Jakarta validation constraints from request DTOs.
Presentation-critical operations additionally document role/ownership rules,
workflow invariants, and expected success and controlled error response codes.

The versioned Postman v2.1 collection complements OpenAPI with an executable
demonstration. Its main folder follows the documented role/workflow sequence and
captures JWTs and resource identifiers through collection-variable scripts.
Collection exports must contain empty credential/token variables; real passwords,
JWTs, cookies, and provider keys are never versioned.

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
  DB_USER
  DB_PASSWORD

MongoDB
  MONGODB_URI
  MONGODB_DATABASE

JWT
  JWT_SECRET

AI
  AI_ENABLED
  AI_PROVIDER
  AI_API_KEY
  AI_MODEL
  AI_BASE_URL          # if required by provider
  AI_CONNECT_TIMEOUT_MS
  AI_READ_TIMEOUT_MS
```

Variable names should ultimately match the actual repository configuration.

Do not duplicate conflicting configuration names simply to match this document.

`JWT_EXPIRATION` is deliberately absent from the implemented configuration
contract: `TokenService` currently fixes token lifetime at two hours. It must not
be presented as externally configurable until the application actually binds and
uses such a setting.

The backend loads only the listed `DB_*`, `JWT_SECRET`, `MONGODB_*`, and `AI_*`
keys from an optional `backend/.env`. JVM system properties have highest
precedence, followed by process environment variables and then `.env` values.
Required blank values fail startup. Optional AI values have application defaults,
and AI remains disabled unless explicitly enabled with a valid external key.

Spring profile selection is separate from the dotenv whitelist. The default
profile keeps API documentation disabled; operators set
`SPRING_PROFILES_ACTIVE=local` in the process environment (or the equivalent JVM
property) to enable local Swagger/OpenAPI. The local profile does not provide
database credentials or change persistence ownership.

Use safe placeholders in:

```text
.env.example
README.md
```

Never commit real credentials.

The executable setup contract, safe template, database prerequisites, startup
checks, and failure guide are maintained in `docs/environment.md` and
`backend/.env.example`.

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

## 26.1 Phase 11 verification baseline

Phase 11 completed the risk-focused backend suite on 2026-09-14. The standard
`./mvnw test` run passes **208 of 208 tests** and covers:

- authentication failures, expired JWTs, role authorization, and idea ownership;
- idea creation defaults, duplicate rejection, vote uniqueness/removal, manager
  review, controlled transitions, and deterministic priority calculation;
- project eligibility, one-project-per-idea, transactional idea conversion, and
  validation of all non-negative financial inputs;
- exact global and orientation-filtered dashboard aggregates from known fixtures;
- mocked Gemini success/failure contracts, MongoDB evaluation history, and the
  invariant that AI never changes workflow status;
- critical audit creation, write ordering, and suppression of false success
  events after rejected or failed operations.

The regular suite never calls the real Gemini service. Its adapter contract uses
the documented `thinkingLevel=medium` choice and a bounded structured response.
The application-context smoke test supplies isolated repository doubles and
disables external persistence auto-configuration, so it does not depend on the
`main` method's `.env` bootstrap. MongoDB repository integration remains a
separate explicit test and uses a test-only database.

---

# 27. Current known architectural gaps

The current codebase contains several known gaps that Sprint 2 should address.

## Security

Phase 1 resolved the previously recorded ownership, administrative-authorization,
and Android credential-logging gaps. New feature endpoints must continue following
the authorization matrix and the logging restrictions in section 20.2.

## Idea prioritization

- backend manager review now persists validated impact and viability scores;
- Android manager review now sends and reloads those persisted scores through the
  real backend API;
- backend priority combines capped votes, manager scores, and the latest AI score
  through the documented deterministic formula;
- Android prioritization presentation remains pending.

## Project conversion

- project creation enforces approved-idea eligibility and one project per idea;
- project insertion and the transition to `CONVERTIDA_EM_PROJETO` share one MySQL transaction;
- Android should still limit its project-creation picker to eligible ideas.

## Android/backend contract

- Task 5.1 established backend `statusIdeia` as the canonical idea-response field;
- Android maps `statusIdeia` to its domain `status` value and rejects a missing
  field instead of silently substituting `NOVA`.

## Dashboard

- Tasks 6.1–6.5 make backend aggregation and dashboard authorization
  authoritative;
- Task 9.3 consumes the global and orientation-filtered summaries in Android;
- Android formats the returned metrics but owns no dashboard calculation formula;
- project-specific results continue through the existing project-detail contract.

## Badges

- Task 5.3 makes the Android earned and available sections disjoint by badge ID;
- progress uses earned active badges over the complete active catalog;
- inactive historical awards remain visible as achievements without affecting
  active-catalog progress.

## Testing

- Phase 11 provides the required backend regression coverage for Sprint 2
  security, ideas, projects, dashboard, AI, and audit behavior;
- Android Compose instrumentation and final real-device role journeys remain
  delivery work rather than backend Phase 11 gaps.

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
