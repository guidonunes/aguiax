# Sprint 2 Regression Baseline

## Purpose

This document records the state of the active AguiaX implementation before
Sprint 2 feature work. It is the evidence artifact for roadmap Task 0.2 and allows
future failures to be distinguished from defects already present at the baseline.

## Baseline identity

- Date: 2026-09-08
- Git commit: `87ba538`
- Active implementation scope: `backend/` and `frontend/`
- Excluded historical snapshot: `aguiaexpress-entrega/`
- Java: 21.0.6
- Maven Wrapper: 3.9.15
- Gradle Wrapper: 9.2.1
- Android instrumented-test device: `Pixel_6(AVD) - 12`

The working tree already contained documentation changes and untracked historical
snapshot files when this baseline was recorded. Build outputs are ignored by the
repository and were not added to version control.

## Verification results

| Check | Command or method | Result | Evidence or observation |
|---|---|---:|---|
| Backend compilation | `cd backend && ./mvnw clean compile` | PASS | 81 Java source files compiled for Java 21. |
| Backend tests | `cd backend && ./mvnw test` | FAIL | 1 test executed with 1 error while loading the Spring context. |
| Android debug compilation | `cd frontend && ./gradlew assembleDebug` | PASS | Debug APK assembly completed successfully. |
| Android local tests | `cd frontend && ./gradlew testDebugUnitTest --rerun-tasks` | PASS | 1 test executed with 0 failures. |
| Android instrumented tests | `cd frontend && ./gradlew connectedDebugAndroidTest` | PASS | 1 test executed on the Pixel 6 AVD with 0 failures. |
| Local MySQL | Application startup and Flyway logs | PASS | MySQL 8.0 was reachable; Flyway validated all 11 migrations and schema version 11 was current. |
| Backend runtime | Local Spring Boot process on port 8080 | PASS | The application connected to MySQL and served HTTP requests. |
| Login | Disposable `OPERACIONAL` baseline account | PASS | `POST /auth/login` returned `200` and a JWT. |
| Protected endpoint without JWT | `GET /orientacoes` | PASS | Returned `401 Unauthorized`. |
| Protected endpoint with JWT | `GET /orientacoes` | PASS | Returned `200 OK`. |
| Baseline data cleanup | Delete disposable baseline account | PASS | `DELETE /usuarios/{id}` returned `204`; no baseline account was intentionally retained. |

No passwords, database credentials, JWTs, or authorization headers are included in
this document.

## Existing failure: backend test environment

The only backend test is
`AguiaXApplicationTests.contextLoads`. It fails because the test starts the
Spring application context without invoking `AguiaXApplication.main`.

The current application loads `.env` values and copies them into system properties
inside `main`. During `@SpringBootTest`, that setup is not executed, so
`application.properties` passes the unresolved `${DB_URL}` placeholder to the
MySQL driver. The resulting error is:

```text
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}
```

This is a pre-Sprint-2 test-isolation/configuration defect. It must not be treated
as a regression introduced by later work. A test profile or database-aware test
configuration should eventually make the test reproducible without depending on
the production-style `main` method setup.

## Coverage limitations at baseline

- The backend has one context-loading test and no meaningful security or domain tests.
- The Android local test is the generated arithmetic example.
- The Android instrumented test only verifies the application package context.
- No automated test currently verifies authentication, authorization, ownership,
  idea workflow, project conversion, dashboard calculations, AI, or audit behavior.
- Android compilation reports deprecation warnings in navigation, dashboard locale
  construction, and exposed-dropdown menu APIs.
- `AuthInterceptor` prints the complete authorization header; this is the known
  sensitive logging defect assigned to roadmap Task 1.5.

## Task 0.2 conclusion

Task 0.2 is complete as a baseline activity: every required check was executed and
its result is known. The failing backend test is explicitly recorded as an existing
defect. Passing this test is future remediation work and is not evidence that the
baseline itself was not established.
