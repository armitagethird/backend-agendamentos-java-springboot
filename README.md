# API de Agendamentos

API REST em Java com Spring Boot para gerenciamento de agendamentos, com controle
de conflito de horário por usuário.

> 🚧 Projeto de estudo, em desenvolvimento ativo.

## Stack

- **Java 17** · **Spring Boot 4.1**
- **Spring Data JPA** (Hibernate) · **PostgreSQL 18**
- **Flyway** para versionamento do schema
- **Docker Compose** para o banco de desenvolvimento
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

**Pré-requisitos:** JDK 17+ (desenvolvido com JDK 21 LTS) e Docker.

### 1. Suba o banco

O `compose.yaml` sobe um PostgreSQL 18 em container, publicado na porta **5433** do
host — escolhida para não conflitar com uma instalação local do PostgreSQL na 5432.

```bash
docker compose up -d
docker compose ps          # confirme STATUS "Up"
```

### 2. Crie o arquivo `.env` na raiz do projeto

```env
DB_USER=agendamentos
DB_PASSWORD=agendamentos
```

Os valores precisam ser iguais a `POSTGRES_USER` e `POSTGRES_PASSWORD` do `compose.yaml`.

### 3. Suba a aplicação

```bash
./mvnw spring-boot:run
```

O Flyway aplica as migrations pendentes no start. A API sobe em `http://localhost:8080`.

> As credenciais vêm de variáveis de ambiente — nada de senha no repositório.
> O `.env` está no `.gitignore`; use o `.env.example` como modelo.

## Banco de dados

### Comandos do container

| Comando | O que faz |
|---|---|
| `docker compose up -d` | sobe o banco em segundo plano |
| `docker compose stop` | para o container sem destruí-lo |
| `docker compose logs db` | mostra a saída do PostgreSQL |
| `docker compose down` | remove o container — **os dados sobrevivem** no volume |
| `docker compose down -v` | remove o container **e apaga o volume** |

Para abrir um `psql` dentro do container:

```bash
docker exec -it agendamentos-db psql -U agendamentos -d agendamentos
```

### Migrations

O schema é versionado pelo Flyway em `src/main/resources/db/migration`, no formato
`V{versão}__{descrição}.sql`. As migrations pendentes são aplicadas automaticamente
quando a aplicação sobe.

⚠️ **Migration já aplicada não se edita.** Alterar um arquivo existente quebra o
checksum e o próximo start falha com `ValidateException` — e, em produção, a mudança
nunca chegaria ao banco. Para corrigir ou evoluir o schema, crie a próxima versão.

Para recomeçar com o banco limpo em desenvolvimento:

```bash
docker compose down -v && docker compose up -d
```

## Alternativa sem Docker

É possível usar uma instalação local do PostgreSQL. Nesse caso, crie o banco
(`createdb agendamentos`), ajuste `spring.datasource.url` em
`src/main/resources/application.properties` para a porta correta e use as
credenciais dessa instância no `.env`.
