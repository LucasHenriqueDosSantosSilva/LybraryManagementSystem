# Estratégia de testes do backend

A suíte executada na etapa 11 tem 69 testes: 16 unitários, 3 HTTP standalone e 50 de integração. Casos parametrizados contam por execução. Não há percentual de cobertura coletado.

| Nível | Evidência existente | Falha que ajuda a detectar |
| --- | --- | --- |
| Unidade | IsbnTest, LoanStatusTest, CategoryServiceTest e CategoryServiceFailureTest | Normalização/checksum, limites de datas e erros de aplicação |
| HTTP standalone | api/ApiExceptionHandlerTest | Tradução 400/404/409 e preservação de ProblemDetail |
| Integração HTTP/MySQL | CategoryIntegrationTest e CatalogIntegrationTest | CRUD, validação, associações, ISBN único, rollback e paginação |
| Integração de circulação | ReaderAndCopyIntegrationTest e LoanIntegrationTest | Disponibilidade, histórico, datas e conflitos de operações |
| Concorrência e banco | LoanIntegrationTest | Empréstimo único, devolução única, retirada versus empréstimo, bloqueio de leitor e constraints |
| Consultas e transações | DashboardTransactionTest, LoanIntegrationTest e PaginationContractTest | Snapshot do dashboard, totais/filtros, lote de ocupações e contrato de páginas |

## Execução e isolamento

No ambiente preparado, use `python3 /workspace/library-runtime/run.py test package` a partir do checkout. O helper configura Java 21, Maven e conexão para o schema dedicado `library_test` quando há o argumento `test`. Para teste unitário direcionado, acrescente `-Dtest=IsbnTest`. Para instalação convencional, siga backend/README.md e configure DB_URL/DB_USER/DB_PASSWORD para um schema descartável de testes.

Os testes de integração aplicam Flyway e validam mapeamentos Hibernate no MySQL real. O schema deve ser exclusivo da suíte, vazio antes da execução e sem outro processo escrevendo: vários testes verificam totais globais ou usam ISBNs fixos. A limpeza remove fixtures próprias; não execute contra produção. Não habilite paralelismo de métodos/classes sem mudar essa estratégia de isolamento.

Clock controlável elimina espera por datas reais. Testes concorrentes coordenam chamadas por barreiras e verificam respostas e estado persistido; não são testes de carga nem prova de todas as intercalações possíveis. Não substituir MySQL por H2 para alegar validação das constraints e bloqueios específicos.

## Evidência e limites

As mudanças anteriores contam como testes de regressão. O [ciclo TDD do ISBN](tdd-isbn-whitespace.md) registra um comportamento novo com RED, GREEN e REFACTOR observados. Essa distinção evita atribuir método de desenvolvimento somente porque existe uma suíte.

Ainda não há testes de frontend, autenticação, deploy, recuperação de backup, carga ou cobertura instrumentada. A etapa de consolidação REST deverá revisar contratos e erros de entrada; a integração com frontend terá seus próprios testes. Aumentar a quantidade de testes sem risco ou comportamento novo não é uma meta.
