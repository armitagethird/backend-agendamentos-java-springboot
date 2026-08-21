# API de Agendamentos

API REST em Java com Spring Boot para gerenciamento de agendamentos, com controle
de conflito de horário por usuário.

> 🚧 Projeto de estudo, em desenvolvimento ativo.

## Stack

- **Java 17** · **Spring Boot 4.1**
- **Spring Data JPA** (Hibernate) · **PostgreSQL**
- **Flyway** para versionamento do schema
- **Bean Validation** para validação de entrada
- **Lombok** · **Maven**

## Funcionalidades

- Criar, atualizar, buscar, cancelar e concluir agendamentos
- Validação de intervalo: `dataInicio` precisa ser anterior a `dataFim`
- **Verificação de conflito**: bloqueia dois agendamentos sobrepostos para o mesmo usuário
- Ciclo de status: `AGENDADO` → `CONCLUIDO` / `CANCELADO`

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/agendamentos` | Cria um agendamento |
| `GET` | `/agendamentos/{id}` | Busca por id |
| `PUT` | `/agendamentos/{id}` | Atualização parcial |
| `PUT` | `/agendamentos/{id}/cancelar` | Cancela |
| `PUT` | `/agendamentos/{id}/concluir` | Conclui |

## Arquitetura

Organização em camadas, com o request atravessando:

```
controller  →  service  →  repository  →  PostgreSQL
     ↓            ↓
    DTO        mapper
```

- **controller** — expõe as rotas e valida a entrada (`@Valid`)
- **DTO** — `record`s de entrada e saída; a entidade nunca é exposta na API
- **service** — regras de negócio (intervalo e conflito) e transações
- **repository** — acesso a dados via Spring Data JPA
- **Flyway** é o dono do schema; o Hibernate roda com `ddl-auto=validate` e apenas confere

## Como rodar

**Pré-requisitos:** JDK 17+ e PostgreSQL rodando na porta 5432.

```bash
# 1. crie o banco
createdb agendamentos

# 2. crie o arquivo .env na raiz do projeto
DB_USER=seu_usuario
DB_PASSWORD=sua_senha

# 3. suba a aplicação (Flyway aplica as migrations no start)
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

> As credenciais vêm de variáveis de ambiente — nada de senha no repositório.
