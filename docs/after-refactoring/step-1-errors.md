# Etapa 10 — Incremento 1: exceções independentes de HTTP

Implementa o item P01 da [análise da baseline](../before-refactoring/analysis.md). A tag v0.1-baseline-backend permanece intacta. Os demais itens P02–P08 continuam pendentes.

## Antes → problema → princípio

CategoryService, AuthorService, BookService, ReaderService, BookCopyService e LoanService conheciam HttpStatus e ResponseStatusException. Uma operação de aplicação escolhia simultaneamente a falha de negócio e sua tradução para transporte web.

Exemplo anterior:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada.");
```

O custo era acoplamento a HTTP, mesmo quando a regra seria executada sem controller. A separação de responsabilidades e o ocultamento de detalhes de transporte motivam a mudança; não é necessário apresentar isso como implementação completa de todos os princípios SOLID.

## Refatoração → depois

Três exceções específicas em `exception/`, sem dependência Spring:

- ResourceNotFoundException: recurso esperado não existe;
- BusinessConflictException: estado do domínio impede a operação;
- InvalidInputException: entrada semanticamente inválida na operação.

Services preservam as mensagens anteriores e passam a usar essas exceções. Exemplo:

```java
throw new ResourceNotFoundException("Categoria não encontrada.");
```

ApiExceptionHandler traduz para 404, 409 e 400, mantendo ProblemDetail e mensagens. ResponseStatusException continua suportada para validações próprias dos controllers, como paginação. Tratamento existente de constraints SQL conhecidas não mudou.

As exceções continuam RuntimeException, preservando rollback padrão das operações transacionais. Bloqueios, isolamento, queries, DTOs, migrations e regras de circulação não foram alterados. Não introduzimos códigos HTTP em enum de domínio, hierarquia genérica de services ou interfaces artificiais.

## Evidência e testes

Busca por imports `org.springframework.http` e `org.springframework.web.server` em service/: de **6 arquivos na baseline para 0**. Isso demonstra remoção de uma dependência específica; não mede todo o acoplamento do sistema. Services ainda dependem de Spring para injeção/transações e de persistência — esta mudança não implementa arquitetura hexagonal completa.

`python3 /workspace/library-runtime/run.py test package` passou: **47 testes**, zero falhas, erros ou ignorados. Os 43 testes anteriores permaneceram e quatro foram acrescentados:

1. Service ausente lança ResourceNotFoundException e preserva mensagem, sem gravação.
2. Advice HTTP traduz recurso ausente para 404/ProblemDetail.
3. Advice traduz conflito para 409 com detalhe preservado.
4. Advice traduz entrada inválida para 400 com detalhe preservado.

A suíte completa inclui transações MySQL, histórico, queries e concorrência, evitando validar apenas a classe nova. Tests de tradução usam MockMvc standalone e não dependem de MySQL; o restante da suíte de integração continua usando schema dedicado.

API reiniciada e HTTP real de categoria ausente confirmou 404 e detalhe anterior; paginação inválida continuou 400 e dashboard respondeu 200.

Não há TDD registrado neste incremento: os testes verificam refatoração e contrato, sem evidência RED/GREEN. Não houve medição de latência, cobertura ou complexidade. Consultas N+1 da baseline ainda existem; nenhum benefício de desempenho foi alegado.

## O que entender e defender

A aplicação informa a natureza da falha; a fronteira HTTP escolhe a resposta. Isso permite testar a regra sem depender de códigos de status e preserva o contrato com o frontend futuro. A distinção entre conflito e entrada inválida continua explícita.

Em Análise e Projeto: responsabilidades, acoplamento e refatoração que mantém comportamento. Em Banco de Dados: o rollback continua sendo garantido pela mesma transação, porque as novas exceções não são capturadas dentro da operação nem deixam de ser unchecked.

Próximo incremento: centralizar a classificação ACTIVE/OVERDUE/RETURNED, hoje repetida em circulação e dashboard (P03), com testes de datas.
