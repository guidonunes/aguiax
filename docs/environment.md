# AguiaX environment configuration

This document is the configuration contract for the active Spring Boot backend in
`backend/`. It contains placeholders only. Never commit `backend/.env`, database
credentials, JWT signing secrets, Gemini keys, or generated tokens.

## Prerequisites

- Java 21;
- a MySQL server with an empty `aguiax` database and a local application
  user that can create and alter objects in that database;
- a reachable MongoDB server;
- a Gemini API key only when real AI evaluation is enabled.

Flyway creates and validates the relational tables, but it does not create the
MySQL database or its login. MongoDB creates the application database and
collections on the first successful document write.

## Recommended local setup

From the repository root:

```bash
cd backend
cp .env.example .env
chmod 600 .env
```

Edit only `backend/.env`. Replace every placeholder required by the local setup:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/aguiax
DB_USER=aguiax_local
DB_PASSWORD=replace-with-your-local-mysql-password
JWT_SECRET=replace-with-a-long-random-local-secret

MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=aguiax

AI_ENABLED=false
AI_PROVIDER=gemini
AI_API_KEY=
AI_MODEL=gemini-3.6-flash
AI_BASE_URL=https://generativelanguage.googleapis.com/v1beta
AI_CONNECT_TIMEOUT_MS=3000
AI_READ_TIMEOUT_MS=15000
```

The tracked `.env.example` contains the same safe template. The real `.env` is
ignored by Git. Confirm this before adding files to a commit:

```bash
git check-ignore -v backend/.env
```

Generate the JWT secret locally rather than reusing the placeholder:

```bash
openssl rand -base64 48
```

Copy that command's output only into the untracked `JWT_SECRET` value. Do not use
the same secret in a shared or deployed environment.

## Configuration reference

| Variable | Requirement | Purpose and accepted value |
|---|---|---|
| `DB_URL` | Required | MySQL JDBC URL, for example `jdbc:mysql://localhost:3306/aguiax`. |
| `DB_USER` | Required | MySQL application username. |
| `DB_PASSWORD` | Required | Password belonging to `DB_USER`; no password is supplied by the repository. |
| `JWT_SECRET` | Required | Private HMAC signing secret. Use a long random value unique to the environment. |
| `MONGODB_URI` | Required | MongoDB connection string, for example `mongodb://localhost:27017`. Include credentials only in the untracked local value when authentication is enabled. |
| `MONGODB_DATABASE` | Required | MongoDB database for audit events and AI evaluation history; local default is `aguiax`. |
| `AI_ENABLED` | Optional | Defaults to `false`. Set to `true` only when the remaining enabled-AI configuration is valid. |
| `AI_PROVIDER` | Conditional | Defaults to and currently supports only `gemini`. |
| `AI_API_KEY` | Conditional | Required when `AI_ENABLED=true`; otherwise leave empty. Backend-only Google AI Studio key. |
| `AI_MODEL` | Optional | Defaults to `gemini-3.6-flash`. |
| `AI_BASE_URL` | Optional | Defaults to the HTTPS Gemini Developer API v1beta base URL shown above. |
| `AI_CONNECT_TIMEOUT_MS` | Optional | Positive integer milliseconds; defaults to `3000`. |
| `AI_READ_TIMEOUT_MS` | Optional | Positive integer milliseconds; defaults to `15000`. |

There is no `JWT_EXPIRATION` environment variable in the current implementation.
JWT lifetime is fixed at two hours by `TokenService`; documenting an unused
variable would give operators a false configuration control.

## Loading and precedence

For the variables in the table, `AguiaXApplication` uses this precedence:

1. JVM system property, such as `-DDB_URL=...`;
2. process environment variable;
3. value in `backend/.env`;
4. the application default, only for the optional AI settings.

Run Maven from `backend/` when relying on `.env`, because the dotenv loader looks
for the file in the process working directory. Blank required values are ignored
and will cause Spring Boot startup to fail instead of silently using a credential.

CI or deployed environments should inject process environment variables or JVM
properties and should not copy a `.env` file into the artifact.

## Spring profiles

No Spring profile is required for the normal API. The default configuration keeps
Swagger/OpenAPI disabled.

Use the explicit `local` profile only when local API documentation is needed:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

`SPRING_PROFILES_ACTIVE` is a standard Spring environment variable, not one of the
keys loaded from this project's `.env` whitelist. Set it in the process
environment or use `-Dspring.profiles.active=local`; do not expect an entry in
`.env` to activate the profile. The `local` profile only enables Springdoc and
does not replace the database, JWT, or AI configuration.

## Database preparation and connectivity

Create the MySQL database and user with local values appropriate to the machine.
For example, from an administrative MySQL session:

```sql
CREATE DATABASE aguiax
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
CREATE USER 'aguiax_local'@'localhost'
    IDENTIFIED BY 'replace-with-your-local-mysql-password';
GRANT ALL PRIVILEGES ON aguiax.*
    TO 'aguiax_local'@'localhost';
```

The password above is an explicit placeholder, not a project credential. Use the
same locally chosen value in `DB_PASSWORD` and do not commit it.

Check both services before starting the backend:

```bash
mysql --host=localhost --user=aguiax_local --password aguiax \
  --execute='SELECT 1;'
mongosh "mongodb://localhost:27017/admin" --quiet \
  --eval 'db.runCommand({ping: 1}).ok'
```

The MySQL client prompts for its password. The MongoDB command should print `1`.
For an authenticated or remote MongoDB deployment, use the corresponding private
URI only in `.env` or the deployment secret store.

## Start and verify the backend

With MySQL and MongoDB reachable:

```bash
cd backend
./mvnw spring-boot:run
```

Successful startup includes completed Flyway validation/migration and an HTTP
server listening on port 8080. In another terminal:

```bash
curl -i http://localhost:8080/actuator/health
```

The expected response is `200 OK` with aggregate status `UP`. If it is `DOWN`,
inspect the component connection and backend logs without posting credentials or
connection strings in an issue.

When `AI_ENABLED=false`, the rest of the backend runs normally, but
`POST /api/ideas/{ideaId}/ai-evaluation` deliberately returns `503 Service
Unavailable`. To demonstrate AI, set `AI_ENABLED=true`, place the private key in
`AI_API_KEY`, restart the backend, and keep the key out of Android and Postman.

## Common startup failures

| Symptom | Likely cause |
|---|---|
| Unresolved `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`, `${JWT_SECRET}`, `${MONGODB_URI}`, or `${MONGODB_DATABASE}` | Required value is missing, blank, or `.env` was not found because Maven was started outside `backend/`. |
| MySQL connection refused or access denied | MySQL is stopped, the database/user was not created, or the three `DB_*` values disagree. |
| Flyway validation error | The configured database has migrations that differ from the repository; do not edit an already-applied migration. |
| MongoDB connection refused | MongoDB is stopped or `MONGODB_URI` points to the wrong host/port. |
| Startup fails when AI is enabled | `AI_API_KEY` is blank, the provider is not `gemini`, the base URL is not HTTPS, or a timeout is not positive. |
| Swagger returns `404` | Expected in the default profile; restart with `SPRING_PROFILES_ACTIVE=local`. |
