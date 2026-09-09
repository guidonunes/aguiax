# Sprint 2 Requirement Matrix

## Purpose and scope

This is the evidence artifact for roadmap Task 0.1. It maps every Sprint 2
requirement to its backend location, endpoint, authorized role, Android consumer,
persistence responsibility, current state, and verification method.

Only `backend/` and `frontend/` are in scope. The `aguiaexpress-entrega/`
historical snapshot is excluded.

## Status legend

- **Implemented** — current behavior substantially satisfies the target.
- **Partial** — useful Sprint 1 behavior exists, but Sprint 2 work remains.
- **Missing** — the target capability has no current implementation.
- **Non-compliant** — current behavior conflicts with the documented target.
- **Optional** — explicitly non-blocking roadmap work.

## Foundation, API, and Android integration

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| Java Spring Boot backend | `AguiaexpressApplication`, MVC controllers, services, and JPA repositories | N/A | Retrofit consumes REST | MySQL | **Implemented.** Java 21 with Spring Boot, Security, JWT, Flyway, and OpenAPI. | `cd backend && ./mvnw clean compile`; preserve throughout Sprint 2. |
| Core REST APIs | Existing user, catalog, orientation, idea, vote, project, badge, and status controllers | Varies | Existing Retrofit APIs, repositories, ViewModels, and Compose screens | MySQL | **Partial.** Most Sprint 1 CRUD exists; Sprint 2 dashboard, manager review, AI, and audit APIs are absent. | CRUD/contract integration tests; Phases 1, 4, 6, 8, 11, and 12. |
| Real Android integration | Existing REST endpoints through `RetrofitClient` | Authenticated and role-dependent | Login, orientation, idea, voting, project, badge, and dashboard screens | Backend-owned | **Partial.** Existing features use the real API and `SessionMock` is unused; new Sprint 2 APIs are not integrated. | Three real role journeys; Phases 9 and 13. |
| Stable API contract | Existing Portuguese root paths; target `/api/dashboard` and `/api/ideas/{id}/ai-evaluation` | Varies | Kotlin API interfaces and DTOs | N/A | **Partial.** Working Sprint 1 paths exist, but field mismatches remain. Do not rename them without compatibility planning. | Contract/deserialization tests; Tasks 5.1, 8.1, 12.1, and 12.2. |
| DTO validation and safe responses | Existing request/response records and controller `@Valid` usage | Varies | Kotlin request/response DTOs | N/A | **Partial.** Entities are generally not exposed, but validation and cross-client contracts lack tests. | MockMvc `400` tests and Swagger review; Phases 11 and 12. |
| Predictable API errors | `ApplicationExceptionHandler` and Security handlers | All callers | Repository/ViewModel error state | N/A | **Partial.** `401`, `403`, `404`, and `409` paths exist; validation, unexpected, and provider failures need a complete contract. | Integration tests for `400/401/403/404/409/5xx`; Phases 1, 7–8, 11, and 12. |

## Authentication and authorization

Task 1.1's complete operation inventory and policy decisions are in
[architecture sections 9.1–9.4](architecture.md#91-task-11-policy-decisions), recorded
on 2026-09-08. Policy definition is complete; runtime gaps below remain open until
their owning implementation tasks and tests are complete.

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| Complete authorization specification | Architecture section 9; all 63 existing controller operations plus planned features | Explicit public, role, and ownership policies | Existing API consumers checked | MySQL roles/ownership; internal MongoDB boundaries | **Implemented.** Documentation produced by Codex; runtime enforcement was completed in Tasks 1.2–1.5. | Controller inventory matches documentation; Phase 1 focused tests pass. |
| Login and JWT | `POST /auth/login`, `AuthController`, `TokenService`, `TokenVerification` | Public login; token identifies all three roles | `LoginScreen`, `LoginViewModel`, `SessionManager`, `AuthInterceptor` | Users/profiles in MySQL; stateless JWT | **Implemented with test gaps.** Task 0.2 manually verified login and protected access. | Valid/invalid login, malformed/expired token, and protected endpoint tests; Task 11.1. |
| View orientations | `GET /orientacoes` and read variants | Any authenticated role | `OrientacoesScreen` and idea forms | MySQL | **Implemented.** Global authentication protects reads. | Every role gets `200`; unauthenticated gets `401`; Tasks 1.1 and 11.1. |
| Manage orientations | `POST/PUT/DELETE /orientacoes` with leader annotation and service check | `LIDER` | Create/edit orientation screens | MySQL | **Implemented with test gaps.** Delete is soft deactivation. Android sends an unused delete query parameter. | Leader succeeds; other roles get `403`; Tasks 1.1–1.2 and 11.1. |
| Create ideas | `POST /ideias` and `IdeiaService.criarIdeia` | `OPERACIONAL` | `CriarIdeiaScreen`; FAB role-limited | MySQL | **Implemented.** Backend method authorization and focused MVC role tests pass. | Operational gets `201`; manager/leader get `403`; verified in Task 1.2, broader workflow coverage in 11.2. |
| Edit/delete own idea | `PUT/DELETE /ideias/{id}`; ownership enforced in `IdeiaService` | Owning `OPERACIONAL` | `IdeiasScreen` and `EditarIdeiaScreen` | MySQL | **Implemented.** Method role checks and service ownership checks prevent cross-user mutation; update preserves the original owner. | Five ownership tests and focused MVC role tests pass; Task 1.3 verified, broader integration coverage in 11.1. |
| Ownership-aware UI | Idea response `usuarioId` plus backend enforcement | Owning `OPERACIONAL` | Idea action menu | N/A | **Implemented.** Android compares the session user ID with each idea owner before showing edit/delete controls. | Android compiles and unit tests pass; backend remains authoritative; Task 1.3 verified, UI coverage may be extended in 9.4. |
| View ideas | Existing list/detail/filter/search endpoints | Any authenticated role | `IdeiasScreen` | MySQL | **Partial.** Access exists, but status deserialization is defective. | Every status renders accurately for every role; Tasks 5.1 and 11.2. |
| Vote/remove own vote | `POST/DELETE /votos-ideias/ideia/{id}` | Any authenticated role | Idea voting controls | MySQL | **Implemented with test gaps.** Authenticated actor is used; service and DB both prevent duplicates. | First vote succeeds, duplicate conflicts, own removal succeeds; Task 11.2. |
| Review/prioritize/approve/reject | Manager review endpoint plus controlled status actions | `GESTOR` | Target idea review UI | MySQL | **Partial.** Status patch is manager-only; score workflow and transition rules are missing. | Wrong roles get `403`; valid/invalid transitions tested; Tasks 4.1–4.2, 9.1, and 11.2. |
| Manage projects | Existing `POST/PUT/DELETE/PATCH /projetos` | `GESTOR` | Project create/edit screens | MySQL | **Partial.** Role annotations exist; conversion and lifecycle invariants do not. | Wrong roles get `403`; conversion tests pass; Tasks 4.3 and 11.3. |
| View projects | Existing project read endpoints | Any authenticated role | `ProjetosScreen` | MySQL | **Implemented with test gaps.** | All roles get `200`; unauthenticated gets `401`; Task 11.1. |
| Executive dashboard access | Target `GET /api/dashboard/**` | `LIDER` | Dashboard navigation/route guard | MySQL aggregates | **Partial/non-compliant.** Android guards the screen, but no backend dashboard endpoint exists to enforce the policy. | Leader gets `200`; other roles get `403`; Tasks 6.5, 9.4, and 11.4. |
| User administration and profile ownership | `UsuarioController`, `UsuarioService`; `GET/PUT/DELETE /usuarios` variants | List/delete: `LIDER`; detail/update: self or `LIDER` | No current user-administration client | MySQL users/profiles | **Implemented.** Role annotations protect list/delete; service checks protect detail/update and the update DTO preserves role/password/points/status. | Focused MVC and service tests cover wrong role/non-owner `403`, self/leader access, ordering, and protected fields; Task 1.4 verified, broader integration in 11.1. |
| Category administration | `CategoriaController`; `POST/PUT/DELETE /categorias` variants | `LIDER` mutations; all roles read | `CategoriaApi` in idea forms | MySQL categories | **Implemented.** Reads remain authenticated and every mutation is leader-only. | Focused MVC tests cover permitted and forbidden mutations; Task 1.4 verified. |
| Badge assignment and award access | `UsuarioBadgeController`, `UsuarioBadgeService`; both POST grant variants, DELETE, and GET variants | `GESTOR` grants/removes; self or `GESTOR`/`LIDER` reads own/detail; global reads `GESTOR`/`LIDER` | `UsuarioBadgeApi`, `BadgeViewModel` uses current user ID | MySQL user badges | **Implemented.** Controller roles protect mutation/global access and service checks use the award recipient for individual ownership. | Focused MVC and service tests cover both grant variants, global roles, own/cross-user reads, and award-ID confusion; Task 1.4 verified. |
| Catalog access and definition ownership | `BadgeController`, `StatusIdeiaController`, `StatusProjetoController`; profile definitions have no controller | All roles read existing APIs; no role may mutate definitions through HTTP | `BadgeApi`; status selection where used | MySQL catalogs; Flyway plus matching enums | **Implemented.** Current APIs are read-only; definitions are migration-managed. | Focused MVC tests verify every role gets `200`, no token gets `401`, and mutation routes do not exist; Task 1.4 verified. |
| Safe public registration | `POST /usuarios`, `RegistrarUsuarioDTO`, `UsuarioService` | Public; only `OPERACIONAL` creation permitted | No current registration screen | MySQL users/profiles | **Implemented.** Service rejects privileged input before repository access and explicitly selects the operational profile. | Focused tests verify `201` delegation, controlled `400`, operational persistence, and no persistence interaction for `GESTOR`/`LIDER`; Task 1.4 verified. |
| Privileged account provisioning | `POST /usuarios/admin`; controlled first-leader bootstrap | `LIDER` provisions any existing role; first leader provisioned by trusted deployment operator | No admin UI required | MySQL users/profiles; BCrypt; credentials external | **Implemented.** Leader-only route reuses validation/hashing through an explicit admin service path; README documents trusted bootstrap without defaults. | Focused tests verify `401`, wrong-role `403`, leader delegation, and requested-role persistence; Task 1.4 verified, broader integration in 11.1. |
| No secret/token logging | Backend and Android logging | All | `AuthInterceptor` | N/A | **Implemented.** The Android interceptor adds the header without logging the JWT, Authorization value, or request URL; no active backend credential log was found. | Three interceptor tests, Android unit suite/build, and active-source scan pass; Task 1.5 verified. |

## Idea scoring and project workflow

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| Manager impact/viability scores | Target review DTO/endpoint and `IdeiaService` method | `GESTOR` | Target review controls | MySQL idea fields | **Partial.** Fields exist and start at zero; no API workflow or `0..10` validation exists. | Persist valid bounds; reject invalid values/wrong roles; Tasks 4.1, 9.1, and 11.2. |
| Allowed idea transitions | `PATCH /ideias/{id}/status` with service transition policy | `GESTOR`; conversion by project service | Manager idea screen | MySQL | **Non-compliant.** Any enum status can replace any other status. | Test all allowed transitions and representative invalid ones; Tasks 4.2 and 11.2. |
| Deterministic priority score | Target scoring service combining normalized votes, human scores, and AI score | System calculation; manager consumes | Review/prioritization UI | MySQL score; MongoDB AI history | **Non-compliant.** Current formula is only `impacto + viabilidade + votos` and runs on vote changes. | Exact formula unit tests and trigger tests; Tasks 8.3 and 11.2. |
| Project only from approved idea | `POST /projetos` orchestration | `GESTOR` | Project creation should list eligible ideas | MySQL | **Non-compliant.** Service accepts any idea status; Android loads every idea. | Approved succeeds; all ineligible states fail; Tasks 4.3 and 11.3. |
| One project per idea | Service `existsByIdeia` plus DB unique constraint | `GESTOR` | Project creation | MySQL | **Implemented with test gaps.** Both service and schema enforce uniqueness. | Second project gets controlled conflict; Tasks 4.3 and 11.3. |
| Transactional conversion | `ProjetoService.criarProjeto` with a transaction | `GESTOR` | Project creation flow | MySQL transaction | **Missing.** Idea is not changed to `CONVERTIDA_EM_PROJETO` and no transaction boundary exists. | Project insert and idea update succeed/fail together; Tasks 4.3 and 11.3. |
| Controlled project lifecycle | `PATCH /projetos/{id}/status` with transition rules | `GESTOR` | `EditarProjetoScreen` | MySQL | **Partial.** Completion date is added for `CONCLUIDO`, but arbitrary enum transitions are accepted. | Valid/invalid transition and completion-date tests; Task 11.3. |
| Stable idea status field | `MostrarIdeiaDTO` and Android `IdeiaDto` | Any idea viewer | All idea screens | N/A | **Non-compliant.** Backend emits `statusIdeia`; Android expects nullable `status` and silently falls back to `NOVA`. | Deserialize every lifecycle status without fallback; Task 5.1. |

## MongoDB, audit, and external AI

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| MongoDB configuration | Spring Data MongoDB dependency and external `MONGODB_URI` / `MONGODB_DATABASE` configuration | Internal infrastructure | None directly | MongoDB | **Implemented.** The application loads external MongoDB settings, keeps `.env` ignored, and starts with both MySQL and MongoDB connected. | Startup verified against local MySQL and MongoDB on 2026-09-09; Task 2.1 complete. |
| MongoDB persistence proof | Focused Spring Data MongoDB persistence integration test | Internal infrastructure | None | Dedicated MongoDB test database | **Implemented.** `MongoPersistenceIntegrationTest` writes, reads, verifies, and cleans up a test-only document through `MongoTemplate`. | Focused test passed against local MongoDB on 2026-09-09; Task 2.2 complete. |
| Audit document/service | `audit/model/AuditEvent`, `AuditEventRepository`, and best-effort `AuditService` | Internal using authenticated actor | No direct UI required | `audit_events` | **Implemented.** Typed event/resource values, actor, resource ID, UTC timestamp, and defensive metadata are persisted independently of MySQL. MongoDB data-access failures are logged without failing relational business operations. | Four service tests and one isolated repository integration test pass; Task 3.1 complete. |
| Critical audit events | Calls from login, idea, project, orientation, and AI workflows | Actor varies | No direct UI required | MongoDB | **Missing.** | Each critical operation produces the expected document; Tasks 3.2 and 11.6. |
| External service consumption | Target `AiIdeaEvaluationClient` and Gemini/OpenAI implementation | Internal; requested by manager | AI review action | Provider over HTTPS | **Missing.** No external provider client exists. | Mocked provider maps a valid response; Tasks 7.1 and 7.3. |
| AI provider configuration | External `AI_API_KEY`, `AI_MODEL`, and optional `AI_BASE_URL` | Internal infrastructure | None | External configuration only | **Missing.** | Start safely without committed credentials; Task 7.4. |
| Structured AI contract | Application request context and validated score response DTO | `GESTOR` requests | Idea evaluation display | MongoDB result history | **Missing.** | Validate structured fields and `0..10` bounds; Task 7.2. |
| AI evaluation endpoint | `POST /api/ideas/{ideaId}/ai-evaluation` and orchestration service | `GESTOR` | Target “Analyze with AI” action and result state | MySQL read plus MongoDB write | **Missing.** | Manager gets structured scores; wrong role gets `403`; Task 8.1. |
| AI evaluation history | Target evaluation document/repository | `GESTOR` creates/reads via API if exposed; internal scoring service can read persisted results | Evaluation history/result UI if required | `idea_ai_evaluations` | **Missing.** Access policy defined in architecture section 9.3. | Multiple evaluations remain traceable; any history route rejects other roles; Tasks 8.2 and 11.5. |
| Human-controlled approval | AI service must not change idea status, approve, reject, or create projects | `GESTOR` decides separately | Separate AI and workflow actions | MySQL status remains authoritative | **Missing feature, target rule defined.** There is no AI code yet, so enforcement must be designed into the implementation. | AI success/failure leaves status unchanged; Tasks 8.4 and 11.5. |
| Safe AI failure behavior | Controlled timeout, malformed-output, rate-limit, and outage handling | `GESTOR` caller | Error/retry state | Optional failed audit event; no MySQL mutation | **Missing.** | Mock malformed JSON, invalid bounds, timeout, `429`, and `5xx`; Tasks 7.3, 8.1, and 11.5. |

## Dashboard and badges

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| Dashboard response DTO | Target `DashboardSummaryResponse` with status counts and financial/result metrics | `LIDER` | Android dashboard DTO | MySQL-derived | **Missing.** | Contract contains all documented metrics using `BigDecimal` for money; Task 6.1. |
| Global backend aggregation | `GET /api/dashboard/summary` and `DashboardService` | `LIDER` | `DashboardScreen` | MySQL project queries | **Missing/non-compliant.** Android currently downloads all projects and calculates authoritative metrics using `Double`. | Exact fixture aggregates; Tasks 6.2 and 11.4. |
| Orientation dashboard filter | `GET /api/dashboard/summary?orientationId={id}` | `LIDER` | Target filter control | MySQL idea/orientation/project relations | **Missing.** | Filter includes only projects linked through the selected orientation; Tasks 6.3 and 11.4. |
| Project-specific result | Target `GET /api/dashboard/projects/{projectId}` only if existing project DTO is insufficient | `LIDER` | Target project result view | MySQL | **Missing/conditional.** Product need must be checked before adding a duplicate endpoint. | Required result view is supported without duplicated contracts; Task 6.4. |
| Android uses backend dashboard | Dashboard API/repository/ViewModel integration | `LIDER` | `DashboardScreen` and `DashboardViewModel` | Backend-owned | **Non-compliant.** Current ViewModel owns all aggregate formulas. | UI renders backend fixture response without recomputing it; Task 9.3. |
| “Projects in progress” value | Backend status count for `EM_ANDAMENTO` | `LIDER` | Dashboard metric card | MySQL | **Non-compliant.** Card displays total project count under the in-progress label. | Mixed-status fixture displays only `EM_ANDAMENTO` count; Tasks 5.2 and 11.4. |
| Earned versus available badges | Existing badge/user-badge APIs plus filtering rule | Any authenticated user | `BadgeViewModel` and `MedalhasScreen` | MySQL | **Non-compliant.** All active badges are shown as available, including already-earned badges. | Earned badge IDs are excluded from available results; Task 5.3. |

## Observability, testing, and delivery

| Requirement | Backend feature / endpoint | Role | Android location | Persistence | Current state | Verification / roadmap |
|---|---|---|---|---|---|---|
| Actuator health/info/metrics | Target `/actuator/health`, `/actuator/info`, `/actuator/metrics` | Exposure policy must be safe; health suitable for local/evaluator access | None required | Runtime metrics | **Missing.** Actuator dependency/configuration is absent. | Endpoints respond without leaking environment values; Task 10.1. |
| Meaningful backend logs | SLF4J parameterized logs around security, workflows, AI, and DB failures | Internal operators | Android has separate client logs | Log stream | **Missing/partial.** Framework logs exist, but no intentional business logging was found. | Exercise failure paths and inspect contextual logs with no secrets; Task 10.2. |
| Custom metrics | Micrometer counters/timers for AI, approvals, and project creation | Internal operators | None | Metrics registry | **Optional/missing.** | Counters/timers change on controlled operations; Task 10.3. |
| Backend automated tests | Security, ideas, projects, dashboard, AI, and audit suites | Test identities for all roles | N/A | Isolated test DBs | **Non-compliant baseline.** Only one context test exists and currently fails because test DB properties are unresolved. | `./mvnw test` passes with meaningful scenarios; Phase 11 and baseline defect remediation. |
| Android automated tests | ViewModel/DTO/UI role and integration-sensitive tests | Test identities | Android test source sets | Test fixtures | **Non-compliant baseline.** Only generated arithmetic and package-context tests exist. | `testDebugUnitTest` and `connectedDebugAndroidTest` cover real contracts/role UI; Phases 9 and 11. |
| Swagger/OpenAPI | Existing springdoc and `OpenApiConfig` | API consumers | No app change | N/A | **Partial.** Base docs exist, but Sprint 2 endpoints and a documented bearer security scheme are absent. | All presentation APIs and response codes are usable from Swagger; Task 12.1. |
| Postman demonstration | Collection for login, roles, scores, AI, dashboard, conversion, and health | All demonstration roles | N/A | N/A | **Missing.** No active Postman collection was found. | Reviewer runs the principal flow without constructing requests; Task 12.2. |
| Environment documentation | Safe MySQL, MongoDB, JWT, AI, profile, and Android URL instructions | Developer/operator | Retrofit base URL guidance | External configuration | **Non-compliant.** README is minimal and no `.env.example` exists. | A new developer can start the stack without private credentials; Tasks 12.3–12.4. |
| Final end-to-end flow | All active backend endpoints and Android integrations | `OPERACIONAL`, `GESTOR`, `LIDER` | Entire app | MySQL + MongoDB + external AI | **Missing for Sprint 2.** Sprint 1 paths work, but the complete target flow cannot yet run. | Execute all role, governance, and observability journeys; Phases 13 and 14. |

## Confirmed implementation conflicts and disposition

1. **Resolved in Task 1.2:** only `OPERACIONAL` can create ideas.
2. **Resolved in Task 1.3:** idea update/delete enforce persisted ownership and
   updates preserve the original owner.
3. **Resolved in Task 1.4:** public registration rejects privileged profiles before
   persistence; separate leader-only provisioning handles authorized role assignment.
4. The backend returns `statusIdeia` while Android reads `status` and defaults
   missing values to `NOVA`.
5. The documented idea lifecycle is restricted, but the current status endpoint
   permits arbitrary enum transitions.
6. Project creation enforces uniqueness but not approved-idea eligibility,
   transactional conversion, or `CONVERTIDA_EM_PROJETO`.
7. Dashboard aggregation belongs in the backend, but it currently lives in Android;
   the in-progress card displays total projects.
8. MongoDB, audit, AI, Actuator, purposeful business logs, and meaningful automated
   tests are target architecture elements with no current implementation.

## Task 0.1 conclusion

Task 0.1 is complete as an audit activity. Every Sprint 2 requirement has a concrete
target location and verification method. “Complete” here means the matrix exists;
it does not mean the Sprint 2 features have been implemented.
