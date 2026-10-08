# Backend — primeiro incremento da baseline

Java 21, Spring Boot 3.5.7, Maven Wrapper 3.9.11, Spring Web/Data JPA, Bean Validation, MySQL e Flyway. Versões estão fixadas; futuras atualizações serão explícitas. Spring Boot fornece as versões compatíveis das dependências gerenciadas.

## Implementado

CRUD de categorias, autores, livros, leitores e exemplares, empréstimos e devoluções, lista paginada (20 por padrão, máximo 100), validação de nome, unicidade conforme collation MySQL e respostas de erro ProblemDetail. Flyway cria `categories`, `authors`, `books`, `book_authors`, `readers`, `book_copies` e `loans`; Hibernate valida o schema.

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | /api/categories | 201 e Location |
| GET | /api/categories?page=0&size=20 | 200, content e metadados de page |
| GET | /api/categories/{id} | 200 ou 404 |
| PUT | /api/categories/{id} | 200, 400, 404 ou 409 |
| DELETE | /api/categories/{id} | 204 ou 404 |

POST/PUT recebem `{"name":"História"}`. Nomes são aparados; espaços não formam um nome válido; máximo de 100 caracteres. `Ficção` e `ficcao` conflitam pela collation `utf8mb4_0900_ai_ci`. A V2 acrescenta FKs que impedem excluir categorias e autores associados a livros (409). Excluir livro remove seus vínculos de autoria, preservando os autores.

## Executar em outra máquina

Pré-requisitos: JDK 21 completo (não apenas JRE), Python não é necessário para a aplicação, e MySQL 8.4 disponível. O usuário do banco precisa de permissões para as migrations em um schema dedicado. Maven Wrapper baixa uma distribuição verificada por SHA-256; acesso ao Maven Central é necessário na primeira execução.

Configure em seu terminal `DB_URL`, `DB_USER` e `DB_PASSWORD`, usando sua própria instância e senha. Exemplo não secreto de URL: `jdbc:mysql://127.0.0.1:3306/library`. Não coloque valores de senha em commits, histórico de comandos ou exemplos. No Windows use `mvnw.cmd` em vez de `./mvnw`.

A partir de `backend/`:

```bash
./mvnw spring-boot:run
```

Por padrão o servidor aceita conexões apenas em 127.0.0.1, porta 8080. `SERVER_ADDRESS` e `SERVER_PORT` permitem configurar isso; esta baseline sem autenticação é para desenvolvimento local. Não disponibilizar escrita pública nesta etapa.

Para testes de integração, configure **DB_URL para um schema MySQL de testes separado**, com as mesmas credenciais apropriadas, e execute:

```bash
./mvnw test
./mvnw package
```

A suíte contém 4 testes unitários (1 com Mockito e 3 de ISBN) e 39 testes Spring Boot/MockMvc contra MySQL. Usa transações com rollback para dados de teste, mas Flyway cria schema e histórico de migrations; nunca apontar os testes para um banco de produção. Testes de categoria usam rollback; testes de catálogo confirmam operações e limpam apenas seus registros. Use um schema de testes dedicado sem dados preexistentes. Os testes ainda não demonstram TDD: foram escritos neste incremento, sem histórico RED/GREEN registrado.

## Ambiente de nuvem atual

JDK em `/workspace/tools/java21`; dependências e configuração de proxy do Maven ficam fora do checkout. Credenciais locais são geradas aleatoriamente e armazenadas em arquivo de permissão restrita fora do repositório. MySQL roda no container `library-mysql`, exposto somente em loopback, com volume nomeado. `library` é o schema de desenvolvimento; `library_test`, o de testes.

Comandos locais preparados e verificados:

```bash
bash /workspace/library-runtime/setup.sh
python3 /workspace/library-runtime/run.py test package
python3 /workspace/library-runtime/run.py spring-boot:run
```

O helper é específico desta máquina e não substitui os pré-requisitos de outra máquina. Instalação e instruções de início estão também no rascunho de configuração da nuvem; processos não devem ser presumidos como restaurados em novas tarefas. Não há Compose do projeto ou frontend ainda.

## Organização didática

Pacotes globais `controller`, `service`, `repository`, `entity` e `dto` deixam a funcionalidade dispersa. `CategoryService` repete busca e mapeamento nas operações e depende de HTTP por `ResponseStatusException`. São candidatos plausíveis para análise posterior, não justificativa para refatorar antes de completar a baseline. Integridade, validação e testes são preservados desde o início.

## Autores e livros

As mesmas operações CRUD estão disponíveis em `/api/authors` e `/api/books`, com GET por id e lista paginada. Autor recebe `{"name":"Machado de Assis"}` e permite homônimos. Livro recebe, usando ids existentes:

```json
{
  "isbn": "0-306-40615-2",
  "title": "Livro demonstrativo",
  "description": null,
  "publicationYear": 2000,
  "categoryId": 1,
  "authorIds": [1, 2]
}
```

ISBN válido é normalizado para ISBN-13 e tem unicidade no banco. ISBN-10 equivalente não permite novo cadastro. Ano entre 1 e 9999, título obrigatório e autores não vazios; autor ou categoria inexistente retorna 404, sem salvar parcialmente. PUT substitui os detalhes e a lista de autores. Resposta inclui autores ordenados por id; não serializa entidades JPA.

Busca do catálogo e histórico de empréstimos estão disponíveis. A V3 impede excluir livros com exemplares. A V4 e os services impedem mudar ISBN de livro com histórico. Paginação hoje pode executar consultas adicionais para carregar autores; otimização e análise de N+1 ficam para diagnóstico posterior com evidência.

## Leitores e exemplares

CRUD paginado em `/api/readers` e `/api/copies`, com consulta por id. POST/PUT de leitor recebem:

```json
{"registrationNumber":"STUDENT-01","name":"Leitor demonstrativo","email":null}
```

Matrícula é única, aparada e convertida para maiúsculas; aceita letras ASCII, números, ponto, hífen e sublinhado. Nome obrigatório; e-mail opcional com formato válido, sem unicidade. Cadastro inicia ativo. `PATCH /api/readers/{id}/status` recebe `{"active":false}` ou `{"active":true}`. PUT de dados não muda status. Leitores sem histórico podem ser excluídos; com histórico, DELETE retorna 409 e orienta desativação.

POST/PUT de exemplar recebem:

```json
{"inventoryCode":"COPY-01","bookId":1}
```

Código patrimonial único segue a normalização da matrícula, com limite de 40 caracteres. Cadastro exige livro existente. PUT pode editar código, mas deve manter bookId; tentar trocar livro retorna 409. `POST /api/copies/{id}/withdrawal` retira de circulação. Repetir retirada mantém WITHDRAWN; não há endpoint de reativação. Exemplar sem histórico pode ser excluído sem apagar o livro.

Resposta de exemplar tem id, inventoryCode, bookId, circulationStatus e available. available deriva de circulação habilitada e ausência de empréstimo ativo. Retirada com empréstimo ativo retorna 409; exclusão com qualquer histórico também retorna 409. Após devolução, retirada é permitida, mas exclusão segue bloqueada.

V3 estabelece as constraints de matrícula/código, status e FK exemplar → livro. Tentativa de excluir livro com exemplares retorna 409 e reverte também a remoção dos vínculos com autores.

## Empréstimos e devoluções

`POST /api/loans` recebe `{"readerId":1,"copyId":1}` e retorna 201 com identificação e datas. O servidor define a data atual e o vencimento; não aceita prazo arbitrário do cliente. `POST /api/loans/{id}/return` devolve e retorna 200; segunda devolução retorna 409. Não existem PUT/DELETE de empréstimos no MVP, preservando eventos.

Consulta individual em `GET /api/loans/{id}`. Lista paginada em `GET /api/loans?readerId=1&status=ACTIVE`; readerId é opcional e permite histórico do leitor. Status aceita ACTIVE, RETURNED ou OVERDUE. ACTIVE inclui atrasados; resposta individual usa OVERDUE após o vencimento, se ainda não devolvido. Ordem: loanDate e id decrescentes. Leitor inexistente retorna 404; filtro inválido, 400.

Configuração: `LIBRARY_TIME_ZONE` (padrão America/Sao_Paulo) e `LIBRARY_LOAN_DAYS` (padrão 14, de 1 a 365). Datas ISO representam calendário da biblioteca. Clock injetado permite testes determinísticos; atraso é calculado, não persistido. Alterar o prazo não muda vencimentos já registrados.

Leitor inativo e exemplar indisponível não podem iniciar empréstimos. Desativar leitor não impede devolução. ISBN de livro com histórico não pode mudar, mesmo após a devolução; ISBN equivalente normalizado não é uma mudança de identidade.

### Garantias de concorrência

Operações mutáveis de circulação usam READ_COMMITTED e bloqueios pessimistas em ordem leitor → livro → exemplar → empréstimo, conforme os recursos envolvidos. Leituras escalares descobrem associações antes de carregar entidades protegidas. Status, retirada e edição de ISBN seguem a mesma coordenação.

V4 cria coluna virtual gerada active_copy_id e UNIQUE para impedir dois empréstimos ativos do exemplar, mantendo múltiplos históricos devolvidos. A coluna não é escrita pelo JPA ou exposta na API. CHECKs garantem consistência básica de datas e FKs preservam histórico. Nem toda regra do serviço é garantida por SQL: acesso direto ao banco é administrativo, não alternativa pública à API.

Testes reais de concorrência cobrem empréstimos, devoluções, retirada versus empréstimo e desativação com bloqueio do leitor. Isso não prova ausência de todos os deadlocks ou valida todas as possíveis falhas de rede. Resultado incerto de commit deve ser consultado antes de repetir a ação.

## Busca do catálogo

GET `/api/books` aceita filtros opcionais combinados por AND: `title` (substring), `author` (substring do nome), `isbn` (exato após normalização) e `categoryId` (id). Exemplo: `/api/books?title=Livro&author=Machado&page=0&size=20`. Textos vazios são ignorados; ISBN inválido e categoria não positiva retornam 400. Categoria positiva inexistente retorna lista vazia. A busca respeita collation do MySQL, sem distinção de caixa/acentos. `%` e `_` são literais, não curingas. Ordem fixa por id. `EXISTS` evita repetir livros com múltiplos autores correspondentes.

## Dashboard

GET `/api/dashboard?limit=5` retorna totalBooks (edições), totalCopies (inclui retirados), totalReaders (inclui inativos), availableCopies, activeLoans (inclui atrasados), overdueLoans, mostBorrowed e recentLoans. Limit de 1 a 20 controla tamanho do ranking e recentes; inválido retorna 400. Livros sem empréstimos não entram no ranking. Empate é resolvido por bookId crescente; recentes por loanDate e id decrescentes.

Dashboard usa consultas parametrizadas com JdbcTemplate dentro de uma transação REPEATABLE_READ: todas as consultas leem o mesmo snapshot InnoDB e usam uma única data de referência. Não há garantia de atualização em tempo real. Ranking conta eventos ativos e devolvidos, sem juntar autores para multiplicar resultados. Nenhuma migration adicional foi necessária neste incremento.
