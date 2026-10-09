# Estrutura atual do repositório — consolidação da etapa 16

A etapa 7 definiu a estrutura quando havia apenas documentação; o histórico Git preserva essa versão. Os caminhos abaixo refletem a implementação atual.

| Caminho | Responsabilidade |
| --- | --- |
| backend/pom.xml, mvnw, .mvn | Build Java 21, Spring Boot, wrapper Maven e distribuição verificada |
| backend/src/main/java/com/lucashenrique/library | LibraryApplication; catalog, readers, loans e reporting por domínio com camadas internas; api, exception e configuration compartilhados |
| backend/src/main/resources/db/migration | Fonte única do DDL: V1 categorias, V2 autores/livros, V3 leitores/exemplares, V4 empréstimos |
| backend/src/test | Unidade, contrato HTTP e integração MySQL; testes precisam de schema exclusivo |
| frontend/src | Telas, cliente HTTP tipado, leitura paginada e testes Vitest |
| frontend/e2e, playwright.config.ts | Cenários Chromium de circulação e layout/teclado, separados do Vitest |
| frontend/package-lock.json | Dependências npm fixadas; instalação com npm ci |
| backend/Dockerfile, frontend/Dockerfile, frontend/nginx.conf | Builds por etapas e execução sem root das aplicações; Nginx faz proxy de API e fallback SPA |
| compose.yaml, .env.example | MySQL, backend e frontend, volume, healthchecks e nomes de variáveis sem segredos |
| docker/README.md | Inicialização, isolamento, persistência e ajustes locais de nuvem |
| docs | Requisitos, banco, arquitetura, UML, análise/refatorações, testes, frontend e apresentação |
| scripts | Medições textuais e observações de consultas; não duplicam migrations |

Não há diretório database com cópias do DDL: migrations mantêm uma única fonte. Outputs (target, dist, node_modules, traces), .env e configurações privadas locais são ignorados. Screenshots documentais selecionados usam dados fictícios e são versionados.

Os domínios são grupos lógicos com dependências explícitas entre repositórios; não são módulos isolados. Veja [implementação atual](../uml/implemented-system.md) e [conclusão da refatoração](../after-refactoring/step-8-domain-organization.md).
