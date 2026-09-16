# AguiaExpress

AguiaExpress é uma plataforma acadêmica de gestão de inovação desenvolvida para
o Challenge FIAP / Grupo Águia Branca. O fluxo principal conecta orientações
estratégicas, ideias, votação e priorização, projetos, resultados executivos e
medalhas.

O sistema ativo é composto por um aplicativo Android nativo em Kotlin/Jetpack
Compose e uma API REST Java/Spring Boot. A Sprint 2 acrescenta autorização no
servidor, MongoDB, avaliação consultiva por IA, dashboard no backend, auditoria,
métricas, testes e documentação de entrega.

## Arquitetura

```text
Android (Kotlin + Compose)
          |
          | REST/JSON + Bearer JWT
          v
Spring Boot API (Java 21)
          |
          +--> MySQL
          |      domínio transacional e Flyway
          |
          +--> MongoDB
          |      eventos de auditoria e histórico de IA
          |
          +--> Google Gemini Developer API
                 avaliação estruturada de ideias
```

O Android apresenta e envia dados, mas não é autoridade para autorização,
transições de fluxo, prioridade ou agregações executivas. Essas regras pertencem
ao backend. A IA apoia a decisão humana e nunca aprova, rejeita ou muda o status
de uma ideia.

## Estrutura do repositório

```text
aguiax/
├── backend/                # API Spring Boot ativa
├── frontend/               # aplicativo Android ativo
├── docs/                   # arquitetura, configuração, roadmap e evidências
├── postman/                # coleção conjunta das Sprints 1 e 2
├── aguiaexpress-entrega/   # snapshot histórico do semestre anterior
└── README.md
```

Somente `backend/` e `frontend/` participam da aplicação atual.
`aguiaexpress-entrega/` não deve ser compilado, testado ou usado como evidência
da Sprint 2.

## Pré-requisitos

Para executar o backend:

- Java 21;
- MySQL instalado e acessível;
- MongoDB instalado e acessível;
- `mongosh` e o cliente `mysql` para os testes de conectividade;
- `curl` para validar a API;
- uma chave Google AI Studio somente para testar a avaliação real por IA.

Maven não precisa ser instalado globalmente: o repositório inclui `backend/mvnw`.
Não há Docker Compose neste projeto; os exemplos abaixo usam serviços instalados
localmente.

Para executar o aplicativo:

- Android Studio;
- Android SDK 36;
- emulador ou aparelho com Android 9/API 28 ou superior.

## Início rápido do backend

### 1. Inicie MySQL e MongoDB

Em uma distribuição Linux com `systemd`:

```bash
sudo systemctl start mysql
sudo systemctl start mongod
systemctl is-active mysql
systemctl is-active mongod
```

Algumas distribuições nomeiam o serviço MySQL como `mysqld`. Nesse caso, troque
`mysql` por `mysqld`. Os dois comandos de estado devem retornar `active`.

### 2. Crie o banco e usuário MySQL locais

Abra uma sessão administrativa:

```bash
sudo mysql
```

Escolha uma senha local e execute, uma vez:

```sql
CREATE DATABASE aguiaexpress
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
CREATE USER 'aguiaexpress_local'@'localhost'
    IDENTIFIED BY 'replace-with-your-local-mysql-password';
GRANT ALL PRIVILEGES ON aguiaexpress.*
    TO 'aguiaexpress_local'@'localhost';
```

O texto `replace-with-your-local-mysql-password` é apenas um marcador. Use uma
senha escolhida localmente. Flyway criará e validará as tabelas quando o backend
iniciar; ele não cria o banco nem o usuário MySQL.

### 3. Crie a configuração local

```bash
cd backend
cp .env.example .env
chmod 600 .env
```

Edite `backend/.env` e informe o usuário, a senha e um segredo JWT privados. Para
gerar um segredo local:

```bash
openssl rand -base64 48
```

O arquivo `.env` real é ignorado pelo Git. O contrato completo, a precedência dos
valores e o diagnóstico de falhas estão em
[`docs/environment.md`](docs/environment.md).

### 4. Confira as conexões

```bash
mysql --host=localhost --user=aguiaexpress_local --password aguiaexpress \
  --execute='SELECT 1;'
mongosh "mongodb://localhost:27017/admin" --quiet \
  --eval 'db.runCommand({ping: 1}).ok'
```

O cliente MySQL solicitará a senha. O comando MongoDB deve imprimir `1`.

### 5. Inicie e valide a API

Execute o Maven a partir de `backend/`, pois é nesse diretório que o carregador
procura `.env`:

```bash
./mvnw spring-boot:run
```

Em outro terminal:

```bash
curl -i http://localhost:8080/actuator/health
```

O resultado esperado é `200 OK` com `{"status":"UP"}`. A API usa a porta 8080.

## Variáveis de ambiente

O modelo seguro versionado é [`backend/.env.example`](backend/.env.example).

| Variável | Obrigatoriedade | Finalidade/padrão |
|---|---|---|
| `DB_URL` | Obrigatória | URL JDBC do MySQL; exemplo local: `jdbc:mysql://localhost:3306/aguiaexpress`. |
| `DB_USER` | Obrigatória | Usuário MySQL da aplicação. |
| `DB_PASSWORD` | Obrigatória | Senha privada do usuário MySQL. |
| `JWT_SECRET` | Obrigatória | Segredo privado usado para assinar JWTs. |
| `MONGODB_URI` | Obrigatória | URI do MongoDB; exemplo local: `mongodb://localhost:27017`. |
| `MONGODB_DATABASE` | Obrigatória | Banco documental; exemplo local: `aguiaexpress`. |
| `AI_ENABLED` | Opcional | `false`; habilita explicitamente a integração Gemini. |
| `AI_PROVIDER` | Opcional | `gemini`; único provedor aceito atualmente. |
| `AI_API_KEY` | Condicional | Obrigatória somente quando `AI_ENABLED=true`. |
| `AI_MODEL` | Opcional | `gemini-3.6-flash`. |
| `AI_BASE_URL` | Opcional | `https://generativelanguage.googleapis.com/v1beta`. |
| `AI_CONNECT_TIMEOUT_MS` | Opcional | `3000`, em milissegundos. |
| `AI_READ_TIMEOUT_MS` | Opcional | `15000`, em milissegundos. |

Não existe `JWT_EXPIRATION` configurável: a validade atual do token é fixa em duas
horas. Propriedades JVM têm precedência sobre variáveis do processo, que têm
precedência sobre `.env`.

Nunca versione senhas, JWTs, URIs autenticadas, chaves Gemini ou o arquivo
`backend/.env`.

## Responsabilidade dos bancos

| Armazenamento | Responsabilidade |
|---|---|
| MySQL | Usuários, perfis, categorias, orientações, ideias, votos, projetos, medalhas e relacionamentos transacionais. |
| MongoDB | `audit_events` e `idea_ai_evaluations`, usados para rastreabilidade e histórico documental. |

MySQL é a fonte de verdade do domínio. MongoDB não é uma cópia das entidades
relacionais e não substitui transações MySQL. O banco e suas coleções MongoDB
podem aparecer no Compass somente depois da primeira gravação de auditoria ou IA;
apenas iniciar a aplicação não garante a materialização imediata.

## Perfis e autorização

Os papéis são independentes; `LIDER` não herda permissões de `GESTOR` ou
`OPERACIONAL`. Controles ocultos no Android melhoram a experiência, mas toda
autorização relevante também é validada pelo backend.

| Operação | OPERACIONAL | GESTOR | LIDER |
|---|:---:|:---:|:---:|
| Consultar orientações, ideias e projetos | sim | sim | sim |
| Criar/editar/excluir orientações | não | não | sim |
| Criar ideia | sim | não | não |
| Editar/excluir ideia própria | sim | não | não |
| Editar/excluir ideia de outra pessoa | não | não | não |
| Votar/remover o próprio voto | sim | sim | sim |
| Avaliar impacto/viabilidade e mudar status da ideia | não | sim | não |
| Solicitar avaliação por IA | não | sim | não |
| Criar e gerenciar projetos | não | sim | não |
| Consultar dashboard executivo | não | não | sim |
| Administrar usuários e categorias | não | não | sim |
| Conceder/remover medalhas de usuários | não | sim | não |

Comportamento HTTP esperado:

- sem autenticação: `401 Unauthorized`;
- autenticado sem permissão: `403 Forbidden`;
- recurso inexistente: `404 Not Found`;
- entrada inválida ou transição proibida: `400 Bad Request`;
- conflito de unicidade: `409 Conflict`.

O cadastro público `POST /usuarios` cria somente `OPERACIONAL`. Depois do primeiro
líder, contas privilegiadas são criadas por `POST /usuarios/admin` com JWT de
`LIDER`.

## Fluxo principal da Sprint 2

```text
LIDER cria orientação
        |
OPERACIONAL cria ideia e usuários votam
        |
GESTOR informa impacto/viabilidade e solicita avaliação de IA
        |
GESTOR prioriza, aprova ou rejeita
        |
ideia APROVADA vira projeto em uma transação MySQL
        |
ideia passa para CONVERTIDA_EM_PROJETO
        |
LIDER acompanha resultados no dashboard
```

As transições aceitas são:

```text
NOVA -> EM_ANALISE
EM_ANALISE -> PRIORIZADA | APROVADA | REJEITADA
PRIORIZADA -> APROVADA | REJEITADA
APROVADA -> CONVERTIDA_EM_PROJETO
```

A criação do projeto e a conversão da ideia são atômicas, e existe no máximo um
projeto por ideia.

## Medalhas por marcos

O backend concede automaticamente ao autor `OPERACIONAL`:

- `PRIMEIRA_IDEIA`: primeira ideia cadastrada com sucesso;
- `IDEIA_APROVADA`: uma ideia aprovada (a conversão também preserva essa conquista);
- `PROJETO_IMPLEMENTADO`: uma ideia convertida em projeto, conforme o critério do catálogo.

Somente medalhas ativas são concedidas automaticamente. As concessões não se
duplicam nem alteram a data original, e participam da transação da ideia/projeto.
Reinicie o backend para aplicar a migração V13, que reconhece marcos já atingidos.
A tela Medalhas atualiza as conquistas ao retornar a ela.
`TOP_COLABORADOR` e `MESTRE_DA_INOVACAO` continuam com concessão manual pelo
`GESTOR` até a definição dos seus limites numéricos. Nenhuma variável nova no
`.env` é necessária para os três marcos definidos ou para `thinkingLevel=low`.

O teste MySQL é opcional e usa um banco temporário separado dos dados da aplicação.
Com MySQL ativo e o usuário do `.env` autorizado a criar bancos, execute em `backend/`:

```bash
bash ./mvnw -o -Dtest=MilestoneBadgeMySqlIntegrationTest -Dmysql.test.enabled=true test
```

## Avaliação de ideias por IA

O endpoint protegido do `GESTOR` é:

```http
POST /api/ideas/{ideaId}/ai-evaluation
```

O backend envia contexto estruturado ao Gemini e valida alinhamento estratégico,
impacto, viabilidade, inovação, pontuação geral e resumo. A resposta é persistida
no MongoDB e participa do cálculo determinístico de prioridade no MySQL. O cliente
usa `thinkingLevel=low` e saída JSON estruturada.

A integração fica desabilitada por padrão. Sem uma chave, mantenha:

```dotenv
AI_ENABLED=false
AI_API_KEY=
```

Nesse modo, apenas o endpoint de IA retorna `503 Service Unavailable`; o restante
da aplicação continua funcionando. Para uma demonstração real, configure a chave
somente no `.env` local, altere `AI_ENABLED=true` e reinicie o backend. A chave
nunca deve chegar ao Android ou à coleção Postman.

## Android

Após o login, todos os perfis abrem a mesma tela **Início**, com conteúdo conforme
o papel: contribuição pessoal para `OPERACIONAL`, pendências e atalhos para
`GESTOR`, e resumo executivo para `LIDER`. O dashboard completo continua exclusivo
do líder, acessível pelo atalho na Home. Os indicadores usam APIs reais; dados
indisponíveis não são apresentados como zero. “Atualizações recentes” resume os
registros atuais e conquistas, não o histórico completo de auditoria.

O ícone de aparência na barra superior (também disponível no login) permite
selecionar **Claro**, **Escuro** ou **Sistema**. **Escuro** é o padrão quando ainda
não existe uma seleção salva; escolhas anteriores continuam sendo respeitadas.
A preferência fica salva localmente
com Preferences DataStore e permanece após reiniciar o app ou sair da sessão.
“Sistema” acompanha a aparência do Android. O tema usa a paleta coral/azul/dourado
e cores semânticas para informação, conquistas, aprovação, pendências e erros.
No login, o ícone de olho permite mostrar/ocultar a senha; ela começa oculta e
volta a ficar oculta quando a tela é recriada. O botão exibe “Entrando...” e fica
desabilitado durante a autenticação já existente.

O Retrofit está configurado em
[`RetrofitClient.kt`](frontend/app/src/main/java/br/com/fiap/aguiaexpress/data/remote/RetrofitClient.kt)
com:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

Esse endereço é correto para o emulador Android: `10.0.2.2` representa o
`localhost` da máquina que executa o backend.

Para um aparelho físico, substitua temporariamente `BASE_URL` pelo IP LAN da
máquina, por exemplo `http://192.168.0.10:8080/`, mantendo a barra final. O celular
e o computador devem estar na mesma rede, e a porta 8080 precisa estar liberada no
firewall. Não use `localhost` no Android, pois ele apontaria para o próprio
emulador/aparelho.

O manifesto permite HTTP sem TLS para desenvolvimento local. Uma implantação
real deve usar HTTPS e restringir tráfego em texto claro.

Para compilar e executar os testes unitários:

```bash
cd frontend
bash ./gradlew testDebugUnitTest
bash ./gradlew assembleDebug
```

Depois, abra `frontend/` no Android Studio, selecione o emulador/aparelho e execute
o módulo `app`.

## Swagger/OpenAPI

A documentação fica desabilitada no perfil padrão. Para habilitá-la somente no
desenvolvimento local:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

No Swagger, faça login, clique em **Authorize** e informe apenas o JWT; o prefixo
`Bearer` é adicionado automaticamente. Não use o perfil `local` para publicar a
documentação em produção.

## Postman

A coleção conjunta das Sprints 1 e 2 está em
[`postman/aguiaexpress.postman_collection.json`](postman/aguiaexpress.postman_collection.json).
Ela não contém credenciais e inclui a pasta numerada **Sprint 2 - Fluxo
principal**, que captura tokens e IDs automaticamente. Consulte as
[`instruções da coleção`](postman/README.md).

## Actuator, logs e métricas

| Endpoint | Acesso |
|---|---|
| `GET /actuator/health` | Público; apenas estado agregado. |
| `GET /actuator/info` | Somente `LIDER`. |
| `GET /actuator/metrics` | Somente `LIDER`. |
| `GET /actuator/metrics/{nome}` | Somente `LIDER`. |

Outros endpoints Actuator, detalhes/componentes de saúde, descoberta e JMX não
ficam expostos. As métricas de negócio são:

- `aguiaexpress.ai.evaluation.duration`;
- `aguiaexpress.ai.evaluation.failures`;
- `aguiaexpress.ideas.approved`;
- `aguiaexpress.projects.created`.

Os logs usam SLF4J e não devem conter senhas, JWTs, cabeçalhos Authorization,
chaves de IA, prompts ou respostas do provedor.

## Testes

### Backend

Com o MongoDB local ativo:

```bash
cd backend
./mvnw test
```

A última suíte completa verificada em 14/09/2026 passou **211 de 211 testes**. Os
testes de IA simulam o Gemini e nunca consomem a API real.

Testes úteis isolados:

```bash
# Contexto sem .env nem bancos externos
./mvnw -Dtest=AguiaexpressApplicationTests test

# Persistência MongoDB em banco exclusivo de teste
./mvnw -Dtest=MongoPersistenceIntegrationTest test

# Segurança/contrato do Actuator
./mvnw -Dtest=BusinessMetricsTest,ActuatorEndpointSecurityTest test
```

O teste MongoDB aceita outra URI com
`-Dmongodb.test.uri=mongodb://host:27017`. Nunca use um banco de produção.

### Android

```bash
cd frontend
bash ./gradlew testDebugUnitTest
bash ./gradlew assembleDebug
```

A última verificação passou **56 de 56 testes unitários** e gerou o APK de debug.
Os testes instrumentados exigem emulador/aparelho conectado:

```bash
bash ./gradlew connectedDebugAndroidTest
```

## Bootstrap controlado do primeiro LIDER

Não existe senha padrão nem endpoint HTTP público para criar o primeiro `LIDER`.
Em uma instalação vazia, um operador confiável precisa criá-lo uma única vez no
MySQL.

Gere um hash BCrypt de custo 10 sem colocar a senha no histórico:

```bash
read -rsp "Senha inicial do líder: " FIRST_LEADER_PASSWORD
printf '\n'
FIRST_LEADER_HASH="$(htpasswd -bnBC 10 bootstrap "$FIRST_LEADER_PASSWORD" | cut -d: -f2)"
unset FIRST_LEADER_PASSWORD
printf '%s\n' "$FIRST_LEADER_HASH"
```

`htpasswd` pertence ao pacote `apache2-utils` no Debian/Ubuntu. Abra o cliente
MySQL sem histórico e informe a senha do banco interativamente:

```bash
MYSQL_HISTFILE=/dev/null mysql \
  --host=localhost --user=aguiaexpress_local --password aguiaexpress
```

Substitua os marcadores e execute uma única vez:

```sql
INSERT INTO t_usuarios (
    id_perfil, nm_nome, ds_email, ds_senha, ds_cargo, ds_departamento,
    nr_pontos, st_status, dt_criacao, dt_data_atualizacao
)
SELECT
    id_perfil, '<nome>', '<email>', '<hash-bcrypt>', 'Lider', '<departamento>',
    0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_tipos_perfis
WHERE nm_perfil = 'LIDER'
  AND NOT EXISTS (
      SELECT 1 FROM t_usuarios WHERE ds_email = '<email>'
  );
```

Confirme o login em `POST /auth/login`; depois use
`POST /usuarios/admin` com o JWT do líder para criar contas `GESTOR` ou `LIDER`.
Finalize com `unset FIRST_LEADER_HASH`.

## Documentação complementar

- [Configuração completa do ambiente](docs/environment.md)
- [Arquitetura e decisões técnicas](docs/architecture.md)
- [Roadmap da Sprint 2](docs/roadmap.md)
- [Matriz de requisitos](docs/sprint2-requirement-matrix.md)
- [Baseline de regressão](docs/sprint2-regression-baseline.md)
- [Coleção Postman](postman/README.md)
