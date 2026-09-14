# AguiaExpress Postman collection

`aguiaexpress.postman_collection.json` is the combined Sprint 1 and Sprint 2
collection. It uses Postman Collection v2.1 and contains no environment-specific
credentials.

## Import

1. In Postman, select **Import**.
2. Select `aguiaexpress.postman_collection.json`.
3. Open the imported collection's **Variables** tab.
4. Fill the current values for:
   - `liderEmail` and `liderPassword`;
   - `operacionalEmail` and `operacionalPassword`;
   - `gestorEmail` and `gestorPassword`.
5. Keep token and generated-ID variables empty. The test scripts populate them.

`baseUrl` defaults to `http://localhost:8080`, and `categoriaId` defaults to `1`.
Change those values only if the local setup uses a different backend URL or
category fixture.

Never save real passwords or generated JWTs as initial values before exporting or
committing the collection.

## Sprint 2 demonstration

Start MySQL, MongoDB, and the backend first. Set `AI_ENABLED=true` and provide a
valid external `AI_API_KEY` if the AI-evaluation step must succeed.

Run the folder **Sprint 2 - Fluxo principal** in its numbered order. It performs:

1. leader login and strategic-orientation creation;
2. operational login, idea creation, and voting;
3. manager login, impact/viability review, transition to analysis, AI evaluation,
   approval, and project conversion;
4. global and orientation-filtered leader dashboards;
5. the public Actuator health check.

The folder's scripts assert the expected HTTP statuses and important response
fields. They also capture `orientationId`, `ideaId`, `projectId`, user IDs, and
role-specific JWTs as collection variables, so the reviewer does not need to copy
values between requests.

The remaining folders preserve the broader Sprint 1 and Sprint 2 endpoint catalog
for individual exploration. Their protected requests use `{{token}}` unless a
specific role is required. Running any login request updates `{{token}}` and the
matching role token.
