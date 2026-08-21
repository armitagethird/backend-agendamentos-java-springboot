# Plano de correção — projeto `agendamentos`

> Levantado em 19/08/2026, com o compilador rodado de verdade (`./mvnw compile`).
> **Ordem: de baixo para cima na pilha.** Cada bloco depende do anterior estar correto.
> Marque `[x]` conforme for fechando, e rode o comando de checkpoint no fim de cada bloco.

---

## Bloco 0 — Encoding (desbloqueia o build)

O Maven morre aqui **antes de compilar qualquer `.java`**. Enquanto isso não for resolvido,
nenhum outro item pode ser verificado.

- [ ] **`src/main/resources/application.properties`** está em ISO-8859-1 → salvar em **UTF-8**
  - Erro atual: `MalformedInputException: Input length = 1`
  - Causa: os acentos em `Conexão` (linha 4) e `é` (linha 9), que são só comentários
  - IntelliJ: canto inferior direito → clicar no encoding → *Convert to UTF-8*

**Checkpoint:** `./mvnw compile` — o erro do `maven-resources-plugin` some e passam a aparecer os erros de Java.

---

## Bloco 1 — `repository/AgendamentoRepository.java`

Base de tudo: o service chama este arquivo. Se a assinatura aqui estiver errada, o service nunca compila.

### Sintaxe (o compilador acusa)

- [ ] **L23** — `@Param("inicio") LocalDateTime,` → falta o **nome do parâmetro** (`inicio`)
- [ ] **L24** — `@Param(("fim") LocalDateTime,` → **parêntese duplo** + falta o nome do parâmetro (`fim`)

### JPQL (o compilador NÃO acusa — só quebra no boot)

O conteúdo da `@Query` é uma *text block*: para o Java é texto puro. Quem valida é o Hibernate, ao subir.

- [ ] **L17** — `dataFim > :inicio` → falta o alias: **`a.dataFim`**
  - Em JPQL o alias não é opcional como no SQL
- [ ] **L18** — `a.id <> : ignoreId` → **espaço depois dos dois pontos**; o parâmetro é `:ignoreId`, colado

### Nome do método

- [ ] Confirmar o nome final: **`existsConflito`**
  - O service hoje chama `existConfilto` (sem o `s`, e `Conflito` → `Confilto`). Um dos dois muda — decida agora, porque o Bloco 3 depende disso.

**Checkpoint:** `./mvnw compile` — os erros deste arquivo saem da lista.

---

## Bloco 2 — `mapper/AgendamentoMapper.java`

Aqui moram **2 dos 4 bugs de lógica**. São os itens mais importantes do plano inteiro:
eles compilam, sobem, rodam — e entregam dado errado sem avisar.

### Sintaxe

- [ ] **L14** — `new Agendamento.builder()` → o **`new` está sobrando**: `Agendamento.builder()`
  - `builder()` é método estático; `new` é para construtor
- [ ] **L27, L30, L33, L36** — `if (req.titulo()) != null{` → parêntese fechado cedo demais
  - Correto: `if (req.titulo() != null) {` — mesmo erro nas quatro linhas

### 🔥 Lógica

- [ ] **L18** — `.dataInicio(req.dataFim())` → deveria ser **`req.dataInicio()`**
  - Efeito: todo agendamento nasce com início = fim, e o `CHECK (data_inicio < data_fim)` do banco derruba **todo POST**
- [ ] **L36** — a guarda checa `req.descricao()` mas o corpo atribui `dataFim`
  - Deveria checar **`req.dataFim()`**
  - Efeito hoje: PUT só com `dataFim` não altera nada; PUT só com `descricao` **grava null em `dataFim`**

**Checkpoint:** `./mvnw compile`.

---

## Bloco 3 — `service/AgendamentoService.java`

### Sintaxe

- [ ] **L39** — `new EntityNotFoundException("Agendamento não encontrado));` → **aspas não fechada**
- [ ] **L51 e L60** — `.orElseThrow(() -> EntityNotFoundException(...))` → **falta o `new`**

### Nomes que não batem (declarado ≠ chamado)

- [ ] **L72** — declarado `validarIntervalor`, chamado `validarIntervalo` (L29, L41) → `r` sobrando
- [ ] **L78** — declarado `cheacharConfilto`, chamado `checarConflito` (L30, L42) → dois typos
- [ ] **L79** — chama `repo.existConfilto`, o repository expõe `existsConflito` (ver Bloco 1)

### 🔥 Lógica

- [ ] **L41–L42** — valida `req.dataInicio()` / `req.dataFim()`, mas deveria validar **`entity.getDataInicio()` / `entity.getDataFim()`**
  - `AgendamentoUpdateRequest` é atualização **parcial**: os campos chegam nulos quando não vieram no JSON
  - Efeito hoje: um PUT que só muda o título estoura `Intervalo inválido`
  - Depois do `merge`, quem tem o valor correto é a `entity`, não o `req`
- [ ] **L40–L41** — o `merge` acontece **antes** da validação → objeto é mutado antes de ser validado
  - Regra: validar primeiro, mutar depois

### Boas práticas

- [ ] **L37** — `atualizar` é o único método de escrita **sem `@Transactional`** (lê → modifica → salva não é atômico)
- [ ] **L12** — trocar `jakarta.transaction.Transactional` por `org.springframework.transaction.annotation.Transactional`
  - O do Spring é o canônico e tem `readOnly`, `propagation`, `isolation`
- [ ] Remover os `@Valid` dos parâmetros de service (L27, L37) — sem `@Validated` na classe eles não fazem nada; a validação real é a do controller

**Checkpoint:** `./mvnw compile`.

---

## Bloco 4 — `controller/AgendamentoController.java`

### Sintaxe

- [ ] **L13** — `private final AgendamentoService(AgendamentoService service){` não é um construtor. Faltam **duas** coisas:
  - o campo: `private final AgendamentoService service;`
  - o construtor, que precisa ter o **nome da classe** (`AgendamentoController`)
- [ ] **L17** — `AgendamentoCreaterRequest` → `AgendamentoCreateRequest`, **e adicionar o import**

### Rotas (o Spring falha no boot)

Nenhum mapeamento tem path. Três `@PutMapping` idênticos → **`Ambiguous mapping`**.
E há `@PathVariable Long id` sem `{id}` declarado em lugar nenhum.

- [ ] **L10** — `@RequestMapping` precisa de path (ex.: `/agendamentos`)
- [ ] **L20** — `atualizar` → PUT em `/{id}`
- [ ] **L24** — `cancelar` → PUT em `/{id}/cancelar`
- [ ] **L28** — `concluir` → PUT em `/{id}/concluir`
- [ ] **L32** — `buscarPorId` → GET em `/{id}`

### Boa prática

- [ ] **L16** — POST de criação deve responder **201 Created**, não 200

**Checkpoint:** `./mvnw spring-boot:run` — agora a aplicação precisa **subir**. Se quebrar, o erro será de JPQL (Bloco 1) ou de mapeamento.

---

## Bloco 5 — O que falta no projeto (não é correção, é ausência)

- [ ] **Tratamento de exceção — `@RestControllerAdvice`**
  - Hoje **toda** exceção vira HTTP **500**, inclusive as que têm código próprio:
  - `EntityNotFoundException` → **404 Not Found**
  - `IllegalArgumentException` (intervalo inválido) → **400 Bad Request**
  - conflito de agenda → **409 Conflict**
  - ⚠️ E a resposta padrão pode vazar detalhe interno da aplicação
- [ ] **`git init`** — a pasta não é repositório. Nada disso está versionado.
- [ ] **Nenhum teste** além do `contextLoads()` gerado pelo Initializr

---

## Resumo numérico

| Bloco | Itens | Tipo |
|---|---|---|
| 0 — Encoding | 1 | build |
| 1 — Repository | 5 | 2 sintaxe · 2 JPQL · 1 nome |
| 2 — Mapper | 3 | 1 sintaxe (×5 linhas) · 2 lógica 🔥 |
| 3 — Service | 8 | 2 sintaxe · 3 nomes · 2 lógica 🔥 · 3 boa prática |
| 4 — Controller | 8 | 2 sintaxe · 5 rotas · 1 boa prática |
| 5 — Ausências | 3 | — |

🔥 **Os 4 itens de lógica** (Mapper L18, Mapper L36, Service L41–42, Service L40–41) são os únicos que
sobreviveriam ao compilador. Todos são a mesma família de erro: *linha copiada, palavra não trocada*.
