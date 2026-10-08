# Library Management System

Projeto acadêmico para as disciplinas de Projeto de Banco de Dados e Análise e Projeto de Software. A proposta é construir um sistema de gerenciamento de biblioteca e analisar sua evolução por meio de uma versão inicial funcional, seguida de refatorações justificadas.

## Estado atual

Etapas 1–7: análise, escopo, casos de uso, modelagem, normalização, arquitetura e estrutura do repositório. **Etapa 8 iniciada:** backend executável com CRUD de categorias, autores e livros, migrations MySQL e 19 testes passando. Leitores, exemplares, empréstimos e frontend ainda não estão implementados.

Leia a [análise inicial](docs/requirements/initial-analysis.md), que registra escopo, requisitos, regras, arquitetura, tecnologias e plano de evolução.

O [escopo do MVP](docs/requirements/mvp-scope.md) define os limites do produto e os critérios de aceite para as próximas etapas.

Os [casos de uso](docs/requirements/use-cases.md) detalham fluxos e alternativas. O [diagrama PlantUML](docs/uml/use-cases.puml) representa o ator e os objetivos do MVP proposto; sua renderização ainda não foi validada.

O [modelo inicial do banco](docs/database/initial-model.md) registra o dicionário de dados, relacionamentos, restrições e decisões de concorrência propostas para MySQL.

A [análise de normalização](docs/database/normalization.md) apresenta chaves candidatas, dependências funcionais e justificativas de 1FN, 2FN e 3FN, distinguindo modelo lógico e mecanismos físicos.

A [arquitetura proposta](docs/architecture/architecture.md) registra responsabilidades, módulos, contratos, transações e o plano da baseline didática.

A [estrutura de desenvolvimento](docs/development/repository-structure.md) distingue arquivos atuais e futuros. As [convenções de contribuição](CONTRIBUTING.md) registram o fluxo Git, segurança e critérios de conclusão.

Veja [execução do backend](backend/README.md) e [evidências do primeiro incremento](docs/testing/baseline-increment-1.md) e [validação do catálogo](docs/testing/baseline-increment-2.md).

## Tecnologias

Implementados: Java 21, Spring Boot, Maven Wrapper, MySQL, Flyway, JPA, Bean Validation, JUnit e Mockito. React, TypeScript e Vite continuam previstos para o frontend.

## Desenvolvimento

A análise inicial foi aprovada para continuidade. A baseline do backend está em desenvolvimento incremental. O próximo incremento acrescenta leitores e exemplares, seguindo a modelagem e as decisões de arquitetura. Não há demonstração publicada nem licença definida nesta etapa.

## Git

A branch `main` guarda etapas verificáveis. Branches curtas, como `feat/initial-backend`, podem organizar mudanças maiores. Usaremos commits que representem entregas reais, com prefixos `docs:`, `feat:`, `test:`, `fix:` e `refactor:`.
