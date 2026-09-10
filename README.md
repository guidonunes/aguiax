# AguiaExpress

Projeto acadêmico desenvolvido para o Challenge Águia Branca.

O **AguiaExpress** é uma aplicação mobile nativa para Android, desenvolvida com **Kotlin + Jetpack Compose**, integrada a uma **API REST em Spring Boot**. A proposta inicial do projeto é permitir o registro, organização, priorização e acompanhamento de ideias, atendendo aos requisitos da disciplina.

## Stack do Projeto

### Frontend
- Kotlin
- Jetpack Compose
- Android Studio
- Retrofit para consumo da API REST

### Backend
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL Database
- Spring Data MongoDB
- MongoDB
- Flyway
- Lombok
- Maven

## Estrutura do Repositório

```txt
aguiaexpress/
├── backend/                # API REST ativa em Spring Boot
├── frontend/               # Aplicativo Android ativo com Jetpack Compose
├── docs/                   # Arquitetura, roadmap e baseline da Sprint 2
├── aguiaexpress-entrega/   # Snapshot histórico da atividade do semestre anterior
└── README.md
```

O desenvolvimento, a compilação e os testes atuais consideram apenas `backend/` e
`frontend/`. O diretório `aguiaexpress-entrega/` é mantido somente como registro
histórico e não participa da funcionalidade, da arquitetura nem do escopo da Sprint 2.

## Documentação da Sprint 2

- [Arquitetura](docs/architecture.md)
- [Política de autorização e inventário de endpoints — Task 1.1](docs/architecture.md#91-task-11-policy-decisions)
- [Roadmap](docs/roadmap.md)
- [Matriz de requisitos da Sprint 2](docs/sprint2-requirement-matrix.md)
- [Baseline de regressão](docs/sprint2-regression-baseline.md)

A política da Task 1.1 e sua aplicação nas Tasks 1.2–1.5 estão documentadas. O
cadastro público cria apenas `OPERACIONAL`; contas privilegiadas usam a rota
leader-only `POST /usuarios/admin`. O interceptor Android não registra JWTs nem o
cabeçalho Authorization.

## Configuração local dos bancos

O MySQL continua sendo a fonte de verdade dos dados de negócio. O MongoDB é um
banco complementar destinado aos eventos de auditoria e ao histórico de avaliações
de IA; ele não replica as entidades relacionais.

1. Crie a configuração local do backend a partir do modelo e substitua somente os
   valores locais necessários:

   ```bash
   cd backend
   cp .env.example .env
   ```

2. Para uma instalação nativa do MongoDB no Linux, inicie e valide o serviço:

   ```bash
   sudo systemctl start mongod
   mongosh "mongodb://localhost:27017/admin" --quiet --eval 'db.runCommand({ping: 1}).ok'
   ```

   O segundo comando deve retornar `1`. As variáveis padrão para esse serviço são
   `MONGODB_URI=mongodb://localhost:27017` e
   `MONGODB_DATABASE=aguiaexpress`.

3. Com MySQL e MongoDB ativos, execute o backend a partir de `backend/`:

   ```bash
   ./mvnw spring-boot:run
   ```

O banco de produção `aguiaexpress` pode não aparecer no MongoDB Compass
imediatamente. A Task 2.2 valida escrita e leitura em um banco de teste temporário
e exclusivo para cada execução, removendo-o ao terminar. Com as Tasks 3.1–3.2, o
MongoDB materializa `aguiaexpress` e a coleção `audit_events` após a primeira ação
auditada bem-sucedida, como login, criação de ideia, projeto ou orientação. Apenas
iniciar o backend não cria necessariamente o banco ou a coleção.

Para executar apenas a prova de persistência da Task 2.2:

```bash
./mvnw -Dtest=MongoPersistenceIntegrationTest test
```

Por padrão, o teste usa `mongodb://localhost:27017`. Outro servidor de teste pode
ser informado com `-Dmongodb.test.uri=mongodb://host:27017`. Nunca direcione esse
teste para um banco de produção.

## Bootstrap controlado do primeiro líder

Se o banco já possui uma conta `LIDER` válida, use essa conta e não repita este
procedimento. Em uma instalação vazia, um operador confiável deve criar exatamente
o primeiro líder diretamente no MySQL local/de implantação. Não existe rota HTTP
pública de bootstrap e nenhuma senha padrão deve ser adicionada ao repositório.

1. Em uma máquina administrativa, gere um hash BCrypt (custo 10, compatível com o
   `BCryptPasswordEncoder` atual) a partir de uma senha fornecida fora do código:

   ```bash
   read -rsp "Senha inicial do líder: " FIRST_LEADER_PASSWORD
   printf '\n'
   FIRST_LEADER_HASH="$(htpasswd -bnBC 10 bootstrap "$FIRST_LEADER_PASSWORD" | cut -d: -f2)"
   unset FIRST_LEADER_PASSWORD
   printf '%s\n' "$FIRST_LEADER_HASH"
   ```

   O comando `htpasswd` pertence ao pacote `apache2-utils` (Debian/Ubuntu). Copie
   somente o hash exibido e não registre a senha em arquivo ou histórico.

2. Abra o cliente MySQL de forma interativa (ele solicitará a senha do banco):

   ```bash
   MYSQL_HISTFILE=/dev/null mysql --host=localhost --user=<usuario-do-banco> --password <nome-do-banco>
   ```

3. Substitua os valores entre `<...>` e execute uma única vez:

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

4. Confirme que exatamente uma linha foi inserida, autentique em `POST /auth/login`
   e passe a criar contas seguintes por `POST /usuarios/admin` com o JWT do líder.
   Depois, descarte a variável local com `unset FIRST_LEADER_HASH`.
