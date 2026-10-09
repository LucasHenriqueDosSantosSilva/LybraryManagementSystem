# Library Management System

Projeto acadêmico de Projeto de Banco de Dados e Análise e Projeto de Software: gestão de catálogo e circulação de uma biblioteca, com baseline preservada, refatorações justificadas e evidências de testes.

## Sistema implementado

Categorias, autores, livros/edições, exemplares físicos e leitores; empréstimos, devoluções, histórico, busca e dashboard. Frontend React/TypeScript/Vite, backend Java 21/Spring Boot 3.5.7 e MySQL 8.4 com Flyway. Docker Compose integra MySQL, backend e Nginx na mesma origem. A interface e a documentação são em português.

Etapas 1–16 concluídas para execução local. Não há demonstração pública, autenticação ou licença definida. Etapas de deploy e revisão acadêmica final permanecem pendentes.

## Executar e demonstrar

Siga [Docker Compose](docker/README.md): copie `.env.example` para `.env`, defina as duas senhas e execute `docker compose up --build -d --wait`. Apenas a porta local do frontend é publicada, por padrão 8088; MySQL e backend ficam na rede interna. As instruções distinguem builds, testes, volumes e configuração da nuvem.

Alternativas: [backend](backend/README.md) e [frontend](frontend/README.md) em desenvolvimento. O [contrato REST](docs/api/rest-contract.md) registra entradas, respostas, paginação e erros. Use o [roteiro de demonstração](docs/presentation/demo-guide.md) e os [diagramas implementados](docs/uml/implemented-system.md).

## Evidências de validação

| Verificação | Resultado registrado |
| --- | --- |
| Backend | 79 testes aprovados na etapa 12; não reexecutados nas entregas apenas de frontend/Docker |
| Frontend | 26 testes de componentes/cliente HTTP e typecheck/build aprovados |
| Navegador | 2 cenários Chromium aprovados com API/MySQL reais, inclusive nos containers; rotas em 320 px e link de salto por teclado |
| Containers | Builds e healthchecks aprovados; fixture preservada após recriar containers e removida ao concluir |

Veja [estratégia de testes](docs/testing/strategy.md), [TDD observado](docs/testing/tdd-isbn-whitespace.md) e [integração em navegador](docs/testing/browser-integration.md). Não há percentual de cobertura, benchmark de latência ou auditoria formal de acessibilidade. Capturas de [desktop](docs/testing/browser/dashboard-desktop.png) e [mobile](docs/testing/browser/dashboard-mobile.png) foram inspecionadas.

## Material acadêmico

| Tema | Documentação |
| --- | --- |
| Requisitos e escopo | [Análise inicial](docs/requirements/initial-analysis.md), [MVP](docs/requirements/mvp-scope.md), [casos de uso](docs/requirements/use-cases.md) |
| Banco de dados | [Modelo](docs/database/initial-model.md), [normalização](docs/database/normalization.md); DDL implementado nas migrations de backend/src/main/resources/db/migration |
| Arquitetura | [Decisões iniciais e atualização](docs/architecture/architecture.md), [estrutura atual](docs/development/repository-structure.md), [diagramas](docs/uml/implemented-system.md) |
| Evolução | [Diagnóstico da baseline](docs/before-refactoring/analysis.md), [conclusão e comparação da refatoração](docs/after-refactoring/step-8-domain-organization.md) |
| Interface | [Dashboard/categorias](docs/frontend/increment-1.md), [autores/livros](docs/frontend/increment-2.md), [leitores/exemplares](docs/frontend/increment-3.md), [circulação](docs/frontend/increment-4.md) |
| Apresentação | [Roteiro](docs/presentation/demo-guide.md), [guia de defesa técnica](docs/presentation/academic-guide.md) |

Os documentos das primeiras etapas preservam contexto histórico; os contratos, diagramas implementados e instruções atuais identificam diferenças. Não presumir que propostas antigas constituem funcionalidades entregues.

## Baseline e contribuição

A tag `v0.1-baseline-backend` preserva a revisão anterior à refatoração, com 43 testes e migrations V1–V4 verificadas em schema novo. Não representa o produto completo. main contém entregas verificáveis, com commits docs/feat/test/fix/refactor. Siga [CONTRIBUTING.md](CONTRIBUTING.md); nunca versionar senhas, `.env`, dados pessoais ou outputs de teste com informações reais.
