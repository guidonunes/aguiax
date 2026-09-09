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

A política da Task 1.1 está documentada. A aplicação das restrições pendentes,
incluindo impedir o cadastro público de perfis privilegiados, será realizada nas
Tasks 1.2–1.4; a documentação não indica que essas correções já estejam ativas.
