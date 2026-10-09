# Guia de defesa técnica — etapa 16

Use a documentação como evidência, distinguindo implementado, medido e previsto. A demonstração local cobre o MVP; autenticação e deploy público não foram entregues.

| Questão | Decisão e evidência |
| --- | --- |
| Por que separar livro/edição e exemplar? | ISBN identifica edição; patrimônio identifica objeto físico. Book 1:N BookCopy permite circulação/histórico independentes. Veja modelo e migrations V2/V3. |
| Como representar autoria? | Relação N:M via book_authors, PK composta e FKs. Não duplicar nomes de autores em cada livro nem impor unicidade a homônimos. |
| Por que não armazenar quantidade disponível? | Derivar estado de circulação e empréstimos não devolvidos evita fontes redundantes. Testes verificam liberação após devolução. |
| Como impedir dois empréstimos do mesmo exemplar? | Transação READ_COMMITTED, bloqueio pessimista coordenado e UNIQUE na coluna gerada active_copy_id. V4 e LoanIntegrationTest sustentam a garantia; consulta prévia sozinha não bastaria. |
| Como preservar histórico? | FKs RESTRICT e regras de exclusão/alteração; devolução registra data, sem excluir evento. Desativação de leitor preserva devoluções. |
| Como tratar tempo? | Datas de calendário e Clock configurável, padrão America/Sao_Paulo. Atraso começa após vencimento; testes verificam limites sem esperar dias reais. |
| Por que monólito em camadas? | Fluxo cabe em transações locais; componentes Spring, DTOs e repositories explicitam responsabilidades. Domínios lógicos não têm isolamento estrito. |
| Onde está SOLID? | Separação de coordenação/SQL no dashboard e tradução HTTP fora dos services melhoram responsabilidades. Não afirmar aplicação integral dos cinco princípios ou menor acoplamento mensurado. |
| Quais padrões foram necessários? | Injeção por construtor, repositories Spring Data e DTOs são mecanismos utilizados. LoanStatus é classificação pura por enum; não criar State/Strategy sem variação que justifique. |
| Como comprovar melhoria? | Observações controladas de SELECTs e métricas textuais com revisão fixa; suíte preservada. Não alegar latência ou cobertura a partir dessas contagens. |
| O que foi TDD? | ISBN com whitespace Unicode: RED observado em 3 entradas válidas, GREEN e REFACTOR; regressão HTTP veio depois e é identificada como tal. |
| O que Compose resolve? | Builds/serviços, proxy da mesma origem, volume e readiness inicial. Não entrega autenticação, backups, TLS, deploy ou garantia de disponibilidade. |

## Antes e depois

Baseline preservada em v0.1-baseline-backend: 43 testes. Ao concluir os oito incrementos de refatoração: 62; contratos REST elevaram a suíte a 79. Frontend tem 26 testes; 2 cenários Playwright verificam browser/API/MySQL. São níveis distintos, não uma contagem de cobertura.

Nos experimentos de páginas com 1/2/3 registros, listagens de livros e de exemplares passaram de 3/4/5 para 3/3/3 SELECTs. Não garantir três consultas em toda situação; páginas vazias, otimizações de contagem e datasets podem mudar a observação. Linhas físicas de produção passaram de 778 para 1842 na conclusão da refatoração, incluindo expansão da formatação e extrações; essa métrica não mede complexidade.

Leia [comparação](../after-refactoring/step-8-domain-organization.md), [TDD](../testing/tdd-isbn-whitespace.md), [REST](../api/rest-contract.md), [browser](../testing/browser-integration.md) e [Docker](../../docker/README.md). As migrations, e não os diagramas propostos, são a fonte do DDL executado.

## Limitações e trabalho restante

Sem licença definida, autenticação ou demonstração pública. Auditoria formal de acessibilidade, outros browsers, cobertura instrumentada, carga, backup/restore e deploy ainda não foram verificados. A etapa de deploy precisa definir destino e acesso, além de implementar autenticação antes de escrita pública. Revisão acadêmica final deverá conferir apresentação, referências e requisitos da instituição, sem inventar resultados que não foram medidos.
