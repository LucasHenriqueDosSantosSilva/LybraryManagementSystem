# Library Management System

Projeto acadêmico para as disciplinas de Projeto de Banco de Dados e Análise e Projeto de Software. A proposta é construir um sistema de gerenciamento de biblioteca e analisar sua evolução por meio de uma versão inicial funcional, seguida de refatorações justificadas.

## Estado atual

Etapas 1–7: análise, escopo, casos de uso, modelagem, normalização, arquitetura e estrutura do repositório. **Etapa 8 — baseline do backend concluída; etapa 9 — análise registrada; etapa 10 — refatoração concluída:** backend executável com CRUD de categorias, autores, livros, leitores e exemplares, empréstimos e devoluções, busca do catálogo e dashboard, migrations MySQL e 79 testes passando. Frontend com dashboard, categorias, autores, livros, leitores, exemplares e circulação; autenticação pública ainda não implementada.

Leia a [análise inicial](docs/requirements/initial-analysis.md), que registra escopo, requisitos, regras, arquitetura, tecnologias e plano de evolução.

O [escopo do MVP](docs/requirements/mvp-scope.md) define os limites do produto e os critérios de aceite para as próximas etapas.

Os [casos de uso](docs/requirements/use-cases.md) detalham fluxos e alternativas. O [diagrama PlantUML](docs/uml/use-cases.puml) representa o ator e os objetivos do MVP proposto; sua renderização ainda não foi validada.

O [modelo inicial do banco](docs/database/initial-model.md) registra o dicionário de dados, relacionamentos, restrições e decisões de concorrência propostas para MySQL.

A [análise de normalização](docs/database/normalization.md) apresenta chaves candidatas, dependências funcionais e justificativas de 1FN, 2FN e 3FN, distinguindo modelo lógico e mecanismos físicos.

A [arquitetura proposta](docs/architecture/architecture.md) registra responsabilidades, módulos, contratos, transações e o plano da baseline didática.

A [estrutura de desenvolvimento](docs/development/repository-structure.md) distingue arquivos atuais e futuros. As [convenções de contribuição](CONTRIBUTING.md) registram o fluxo Git, segurança e critérios de conclusão.

Veja [execução do backend](backend/README.md) e [evidências do primeiro incremento](docs/testing/baseline-increment-1.md) e [validação do catálogo](docs/testing/baseline-increment-2.md) e [validação de leitores e exemplares](docs/testing/baseline-increment-3.md) e [validação da circulação](docs/testing/baseline-increment-4.md) e [validação das consultas](docs/testing/baseline-increment-5.md).

## Tecnologias

Implementados: Java 21, Spring Boot, Maven Wrapper, MySQL, Flyway, JPA, Bean Validation, JUnit e Mockito. React, TypeScript e Vite continuam previstos para o frontend.

## Desenvolvimento

A análise inicial foi aprovada para continuidade. A baseline do backend está preservada para comparação. A [análise dos problemas da baseline](docs/before-refactoring/analysis.md) registra evidências e propostas. A [primeira refatoração](docs/after-refactoring/step-1-errors.md) separa exceções de aplicação de HTTP. A [segunda refatoração](docs/after-refactoring/step-2-loan-status.md) centraliza a classificação dos empréstimos. A [terceira refatoração](docs/after-refactoring/step-3-readability.md) melhora a legibilidade dos services. A [quarta refatoração](docs/after-refactoring/step-4-book-queries.md) carrega autores em lote, mantendo a paginação. A [quinta refatoração](docs/after-refactoring/step-5-copy-queries.md) consulta ocupações de exemplares em lote. A [sexta refatoração](docs/after-refactoring/step-6-dashboard-repository.md) separa SQL do dashboard de sua coordenação. A [sétima refatoração](docs/after-refactoring/step-7-pagination.md) centraliza o contrato de paginação. A [oitava refatoração](docs/after-refactoring/step-8-domain-organization.md) organiza o código por domínio e conclui esta etapa. A [etapa de testes e TDD](docs/testing/tdd-isbn-whitespace.md) demonstra o ciclo em uma melhoria da normalização de ISBN, com [estratégia de testes](docs/testing/strategy.md) documentada. A [consolidação REST](docs/api/rest-contract.md) documenta os contratos e verifica erros, Location e exclusão sem corpo. O [primeiro incremento do frontend](docs/frontend/increment-1.md) implementa dashboard e categorias. O [segundo incremento](docs/frontend/increment-2.md) acrescenta autores e catálogo de livros, com busca e seleção de referências. O [terceiro incremento](docs/frontend/increment-3.md) implementa leitores e exemplares. O [quarto incremento](docs/frontend/increment-4.md) implementa empréstimos, devoluções e histórico, concluindo as telas principais. A [integração em navegador](docs/testing/browser-integration.md) verificou dois cenários Chromium e as telas em 320 px. A [execução com Docker Compose](docker/README.md) compila e integra os três serviços com volume e healthchecks. Próxima etapa: consolidar documentação e demonstração. Não há demonstração publicada nem licença definida nesta etapa.

## Git

A branch `main` guarda etapas verificáveis. Branches curtas, como `feat/initial-backend`, podem organizar mudanças maiores. Usaremos commits que representem entregas reais, com prefixos `docs:`, `feat:`, `test:`, `fix:` e `refactor:`.

## Baseline acadêmica

A tag `v0.1-baseline-backend` preserva a primeira versão funcional do backend antes da análise e refatoração. As quatro migrations e os 43 testes também foram verificados em um schema MySQL novo. A tag não representa o sistema completo com interface ou deploy.
