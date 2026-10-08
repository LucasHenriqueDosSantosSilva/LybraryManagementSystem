# Desenvolvimento e contribuições

Este projeto será desenvolvido em etapas para permitir estudo e comparação da arquitetura. Consulte o [escopo](docs/requirements/mvp-scope.md) e a [estrutura prevista](docs/development/repository-structure.md) antes de ampliar funcionalidades.

## Fluxo Git

Para uma mudança relevante, crie uma branch curta a partir de main, por exemplo `feat/catalog-baseline`, `test/loan-concurrency` ou `refactor/loan-responsibilities`. Faça commits que correspondam a entregas reais. Use `feat:`, `fix:`, `test:`, `refactor:`, `docs:` ou `chore:`; não produza commits artificiais para simular evolução.

Antes de integrar, revise `git diff --check`, os arquivos alterados e os comandos de validação pertinentes. Pull request é útil para registrar objetivo, decisões e resultados mesmo em um projeto individual, sem exigir uma burocracia extensa.

## Preservação e segurança

Não versionar credenciais, arquivos .env ou dados pessoais reais. Exemplos e demonstrações usam dados fictícios. Respeitar mudanças locais existentes. As tarefas de nuvem já têm ambiente isolado: usar o checkout existente; não criar Git worktree sem solicitação explícita.

## Critérios de conclusão

Uma funcionalidade termina quando atende seus critérios de aceite, foi verificada por testes ou execução apropriada e tem documentação atualizada. Não afirmar que build, Docker, MySQL ou interface funcionam sem executar os respectivos checks. A etapa atual é documental e ainda não oferece comandos de execução da aplicação.

Refatoração deve preservar comportamento. Registre problemas da baseline antes de corrigi-los e compare as mesmas funcionalidades. Novas regras de negócio são alterações funcionais e devem ser descritas separadamente.

## Documentação

Mantenha português na documentação e inglês no código. Identifique diagramas propostos versus implementados, testes executados versus planejados e números medidos versus avaliações qualitativas. Regras e casos de uso precisam continuar coerentes com o schema e a API conforme forem implementados.
