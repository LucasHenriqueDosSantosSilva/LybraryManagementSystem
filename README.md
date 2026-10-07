# Library Management System

Projeto acadêmico para as disciplinas de Projeto de Banco de Dados e Análise e Projeto de Software. A proposta é construir um sistema de gerenciamento de biblioteca e analisar sua evolução por meio de uma versão inicial funcional, seguida de refatorações justificadas.

## Estado atual

Etapas 1–5: análise, escopo, casos de uso, modelagem e normalização do banco. **Ainda não há aplicação, banco criado, dependências instaladas ou testes automatizados.** As tecnologias abaixo são propostas, não implementadas.

Leia a [análise inicial](docs/requirements/initial-analysis.md), que registra escopo, requisitos, regras, arquitetura, tecnologias e plano de evolução.

O [escopo do MVP](docs/requirements/mvp-scope.md) define os limites do produto e os critérios de aceite para as próximas etapas.

Os [casos de uso](docs/requirements/use-cases.md) detalham fluxos e alternativas. O [diagrama PlantUML](docs/uml/use-cases.puml) representa o ator e os objetivos do MVP proposto; sua renderização ainda não foi validada.

O [modelo inicial do banco](docs/database/initial-model.md) registra o dicionário de dados, relacionamentos, restrições e decisões de concorrência propostas para MySQL.

A [análise de normalização](docs/database/normalization.md) apresenta chaves candidatas, dependências funcionais e justificativas de 1FN, 2FN e 3FN, distinguindo modelo lógico e mecanismos físicos.

## Stack proposta

Java 21, Spring Boot, Maven, MySQL e Flyway no backend; React, TypeScript e Vite no frontend. A adoção de bibliotecas adicionais será justificada conforme surgirem necessidades.

## Desenvolvimento

A análise inicial foi aprovada para continuidade. A próxima etapa define a arquitetura; a implementação seguirá a modelagem e as decisões de arquitetura. Os comandos de execução, configuração e testes serão documentados quando existirem e forem verificados. Não há demonstração publicada nem licença definida nesta etapa.

## Git

A branch `main` guarda etapas verificáveis. Branches curtas, como `feat/initial-backend`, podem organizar mudanças maiores. Usaremos commits que representem entregas reais, com prefixos `docs:`, `feat:`, `test:`, `fix:` e `refactor:`.
