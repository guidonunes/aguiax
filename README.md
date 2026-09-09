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
