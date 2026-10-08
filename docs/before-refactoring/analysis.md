# Etapa 9 — Análise técnica da baseline

Referência: tag `v0.1-baseline-backend`, commit `3f18c2605eed14d6749fb8a3e43d65a7885c79f3`. Esta etapa não altera código da aplicação, contratos, schema ou testes. A baseline funciona e possui 43 testes; o objetivo é examinar custos reais de manutenção, não declarar que todo componente está errado.

## Evidência e método

[Medidas de fonte](source-metrics.json), geradas por `python3 scripts/measure_source.py`, usam `git show` da revisão especificada: 44 arquivos Java de produção, 778 linhas físicas (778 não vazias); 6 arquivos Java de testes, 450 linhas físicas (449 não vazias). Seis services importam tipos HTTP. O JSON lista valores por arquivo e revisão completa.

São medidas textuais, não número de responsabilidades, cobertura, complexidade ciclomática, índice de coesão ou grau formal de acoplamento. A baseline compacta vários comandos na mesma linha: menos linhas não implica software mais simples. A refatoração poderá aumentar linhas ao melhorar legibilidade.

[Observação de consultas](catalog-query-observation.json): GET de catálogo em processo local com Hibernate SQL DEBUG e três livros de teste com um autor compartilhado, sem outra requisição concorrente durante a coleta. Contexto de persistência novo por requisição, sem cache de segundo nível configurado. SQL observado:

| Livros retornados | SELECTs observados | Composição |
| --- | --- | --- |
| 1 | 3 | Consulta da página + count + autores de um livro |
| 2 | 4 | Consulta da página + count + autores de dois livros |
| 3 | 5 | Consulta da página + count + autores de três livros |

Não medimos latência, throughput ou desempenho sob carga. O teste demonstra crescimento de consultas para essa listagem; não prova comportamento de todos os endpoints. Fixtures próprias foram removidas pela API e nenhum dado preexistente foi apagado.

## P01 — Service conhece HTTP

1. **Arquivos:** CategoryService, AuthorService, BookService, ReaderService, BookCopyService e LoanService, em `backend/src/main/java/com/lucashenrique/library/service/`.
2. **Problema:** regras e busca de recursos lançam ResponseStatusException e escolhem HttpStatus.
3. **Princípio/propriedade:** acoplamento da operação de aplicação ao transporte; ocultamento de informação e separação de responsabilidades. Não afirmar uma violação universal de DIP apenas pela presença de um import.
4. **Consequência:** executar a mesma operação por outra entrada exige entender exceções web; testes de regra ficam ligados à tradução HTTP.
5. **Gravidade:** média para manutenção; não foi observada falha funcional por isso.
6. **Solução proposta:** exceções de aplicação para recurso inexistente, conflito e entrada inválida, traduzidas no advice HTTP.
7. **Justificativa:** preservar regras e mover escolha de status para a fronteira de transporte.
8. **Alteração realizada:** nenhuma nesta etapa; apenas levantamento de imports reproduzível.
9. **Benefício obtido:** nenhum benefício de código ainda medido; espera-se menor dependência de HTTP no service.
10. **Impacto nos testes:** manter códigos e mensagens dos testes HTTP; acrescentar teste unitário de tradução e de exceções de regra quando pertinente.

## P02 — Listagem de livros tem consultas adicionais por livro

1. **Arquivos:** BookService.list/response, BookRepository.search e Book.authors.
2. **Problema:** mapear DTO acessa coleção lazy de autores para cada livro da página.
3. **Princípio/propriedade:** acesso a dados pouco explícito e acoplamento entre mapeamento e carregamento; não confundir N+1 com violação de normalização.
4. **Consequência:** páginas maiores exigem mais idas ao banco; observação 3, 4 e 5 SELECTs para 1, 2 e 3 livros.
5. **Gravidade:** média; custo de consultas confirmado, impacto em latência não medido.
6. **Solução proposta:** paginar ids/livros primeiro e buscar autores da página em uma segunda consulta em lote; alternativamente batch fetching verificado.
7. **Justificativa:** manter paginação no banco. JOIN FETCH de coleção diretamente numa página pode produzir paginação em memória ou linhas duplicadas; não adotá-lo cegamente.
8. **Alteração realizada:** nenhuma; coleta de SQL registrada.
9. **Benefício obtido:** apenas diagnóstico; redução esperada só será afirmada após nova coleta com as mesmas fixtures.
10. **Impacto nos testes:** preservar filtros, ordenação, totalElements e autores completos; comparar consultas para os mesmos tamanhos de página.

## P03 — Status do empréstimo calculado em dois lugares

1. **Arquivos:** LoanService.response e DashboardService.get (row mapper de recentLoans).
2. **Problema:** ambos repetem `returned != null ? RETURNED : due < today ? OVERDUE : ACTIVE`.
3. **Princípio/propriedade:** duplicação de uma regra de domínio; Information Expert e Protected Variations ajudam a escolher um ponto responsável.
4. **Consequência:** alterar limite de atraso numa consulta pode deixar outra incoerente.
5. **Gravidade:** média; hoje os dois cálculos são equivalentes e os testes passam.
6. **Solução proposta:** conceito LoanStatus com função pequena que recebe vencimento, devolução e data de referência; uso tanto pelo mapeamento JPA quanto JDBC.
7. **Justificativa:** uma regra única, sem injetar repositories ou Clock numa enum. Não transformar isso em State: não há transições complexas que justifiquem o padrão.
8. **Alteração realizada:** nenhuma.
9. **Benefício obtido:** nenhum ainda; redução de fontes da regra deve ser demonstrada no diff.
10. **Impacto nos testes:** testar vencimento hoje, dia seguinte e devolução; manter status nas respostas de histórico e dashboard.

## P04 — Paginação validada repetidamente nos controllers

1. **Arquivos:** CategoryController, AuthorController, BookController, ReaderController, BookCopyController e LoanController.
2. **Problema:** seis verificações repetem `page < 0 || size < 1 || size > 100` e mensagem.
3. **Princípio/propriedade:** duplicação de contrato de entrada; coesão de validação.
4. **Consequência:** mudar limite exige sincronizar vários endpoints.
5. **Gravidade:** baixa; não há discrepância funcional atual.
6. **Solução proposta:** objeto de parâmetros paginados validado/reutilizável, mantendo defaults, ou componente pequeno de validação com nome específico.
7. **Justificativa:** centralizar somente contrato compartilhado, sem base controller genérico para todo CRUD e sem classe Utils.
8. **Alteração realizada:** nenhuma.
9. **Benefício obtido:** nenhum ainda; não criar abstração cujo custo exceda estas seis repetições.
10. **Impacto nos testes:** parametrizar endpoints para limites 0, 1, 100 e 101 e página negativa, preservando 400 e payload.

## P05 — Dashboard mistura consulta SQL, mapeamento e coordenação

1. **Arquivo:** DashboardService.get.
2. **Problema:** coordena snapshot/data e contém oito consultas, row mappers e cálculo de status num mesmo método.
3. **Princípio/propriedade:** motivos distintos para mudar, SRP e coesão; JDBC é escolha válida, não o defeito.
4. **Consequência:** mudança de schema ou formato do relatório ocorre no mesmo lugar da política de data e leitura consistente.
5. **Gravidade:** média para evolução; resultado atual foi validado.
6. **Solução proposta:** DashboardRepository dedicado às consultas/projeções; service mantém data e transação REPEATABLE_READ.
7. **Justificativa:** separar persistência de coordenação sem criar interface de repository artificial se não houver substituição útil.
8. **Alteração realizada:** nenhuma.
9. **Benefício obtido:** nenhum ainda; espera-se service mais concentrado na operação.
10. **Impacto nos testes:** preservar agregações, desempate, limites e snapshot; mover consultas não pode abrir transações separadas por indicador.

## P06 — Camadas globais dispersam uma funcionalidade

1. **Arquivos:** árvore de controller/, service/, repository/, dto/ e entity/.
2. **Problema:** manutenção de uma regra de leitores ou catálogo exige navegar por vários grupos globais.
3. **Princípio/propriedade:** organização e coesão por motivo de mudança; estrutura de pastas não prova coesão de código.
4. **Consequência:** componentes próximos conceitualmente ficam distantes; acesso a todos os tipos por imports wildcard facilita dependências pouco explícitas.
5. **Gravidade:** baixa a média no tamanho atual.
6. **Solução proposta:** agrupar catalog, readers, loans e reporting, mantendo camadas internas onde úteis e tipos públicos só quando necessários.
7. **Justificativa:** facilitar navegação sem dividir deployment. Associações JPA e operações históricas atravessam módulos e precisam ser documentadas, não escondidas.
8. **Alteração realizada:** nenhuma.
9. **Benefício obtido:** nenhum ainda; não prometer redução numérica de acoplamento apenas por mover pacotes.
10. **Impacto nos testes:** ajustar imports e descoberta Spring; executar toda a suíte após mudanças de pacotes, preservando nomes de tabelas e migrations.

## P07 — Código excessivamente comprimido e imports amplos

1. **Arquivos:** LoanService, BookCopyService, BookService e outros arquivos da baseline; medidas por arquivo estão no JSON.
2. **Problema:** várias declarações, atribuições ou condições na mesma linha, com imports wildcard.
3. **Princípio/propriedade:** clareza e manutenção; não equivale automaticamente a alta complexidade ciclomática.
4. **Consequência:** dificulta ler ordem de bloqueios, revisar alterações e localizar falhas.
5. **Gravidade:** baixa funcional, média para apresentação e revisão.
6. **Solução proposta:** formatação consistente, imports explícitos e nomes/expressões intermediárias onde esclareçam a intenção.
7. **Justificativa:** tornar operações e invariantes visíveis sem criar classes para toda expressão.
8. **Alteração realizada:** nenhuma no código da aplicação.
9. **Benefício obtido:** nenhum ainda; mais linhas pode ser um resultado desejável, não regressão.
10. **Impacto nos testes:** comportamento permanece idêntico; build e suíte devem passar. Não criar testes de estilo que reproduzam cada linha.

## P08 — Dependência de persistência por BookCopy na disponibilidade

1. **Arquivo:** BookCopyService.response, usado pelo método list.
2. **Problema:** para cada exemplar, o mapper chama `existsByCopyIdAndReturnedDateIsNull`.
3. **Princípio/propriedade:** mapeamento acoplado a consultas; mesma família de custo de P02.
4. **Consequência:** inspeção indica consulta de disponibilidade por item; o número exato neste endpoint ainda não foi coletado e não será inventado.
5. **Gravidade:** média potencial, sem medida de desempenho.
6. **Solução proposta:** projeção ou consulta em lote das ocupações da página, mantendo disponibilidade derivada.
7. **Justificativa:** não introduzir contador disponível persistido para resolver consultas.
8. **Alteração realizada:** nenhuma.
9. **Benefício obtido:** nenhum ainda; verificar SQL antes/depois ao abordar este item.
10. **Impacto nos testes:** preservar disponíveis, emprestados e retirados na mesma página e restrições sob concorrência.

## Escolhas que não devem ser corrigidas sem motivo

Clock injetado facilita teste de datas. UNIQUE condicional, FKs e CHECKs preservam integridade. Transações e bloqueios têm coordenação explícita e testes concorrentes; não removê-los para diminuir linhas ou dependências. DTOs evitam expor entidades. ISBN tem responsabilidade específica; não é uma Utils genérica. Monólito e JDBC em relatórios são adequados. O fato de LoanService coordenar vários repositories não o torna automaticamente uma classe inadequada: o caso de uso atravessa esses recursos.

Não há evidência de necessidade de microsserviços, Observer, Abstract Factory, Visitor ou Composite. Prazo único configurável não exige Strategy. Ausência de frontend e autenticação pública é limite de estágio, não problema introduzido deliberadamente para refatoração.

## Plano da etapa 10

1. Separar exceções de aplicação da tradução HTTP (P01), preservando códigos e mensagens.
2. Centralizar status calculado (P03), com testes dos limites de datas.
3. Tornar código legível (P07), sem misturar mudança funcional.
4. Corrigir consultas paginadas de livros (P02), medir pelos mesmos comandos e inspecionar exemplares (P08).
5. Separar consultas do dashboard (P05), preservar snapshot.
6. Revisar paginação (P04) e organização por domínio (P06) quando benefícios justificarem custo.

A ordem pode mudar por dependências de implementação; todos os itens terão status e justificativa explícitos. Cada incremento deve passar nos testes relevantes e na suíte de caracterização. A tag permanece intacta. Novas funcionalidades são registradas separadamente de refatoração.

## Reprodução das evidências

```bash
python3 scripts/measure_source.py --ref v0.1-baseline-backend
```

Para observação SQL, iniciar a baseline com logging `org.hibernate.SQL=DEBUG` em processo local isolado. Na máquina preparada:

```bash
python3 /workspace/library-runtime/run.py spring-boot:run '-Dspring-boot.run.arguments=--logging.level.org.hibernate.SQL=DEBUG' > /workspace/library-runtime/observations.log 2>&1
python3 scripts/observe_catalog_queries.py --log /workspace/library-runtime/observations.log --output /tmp/catalog-observation.json
```

Redirecionar logs do processo ao caminho indicado antes da observação, parar apenas a API que você iniciou e garantir que os três ISBNs demonstrativos estejam livres. O script falha se existir ISBN conflitante e nunca remove registros preexistentes. O processo deve executar a revisão sob análise; o script registra HEAD atual, portanto não usar resultados de outra revisão como se fossem da baseline. Em máquinas futuras, configurar JDK, MySQL e proxy conforme documentação de execução; não presumir que helper local existe em outro computador.

## O que entender e defender

Problema de projeto deve ter evidência, consequência e proposta proporcional. Coesão/acoplamento podem ser discutidos qualitativamente, mas não receberam índices objetivos nesta análise. Contagem de consultas mostra comportamento de persistência; linhas físicas mostram tamanho de texto, não qualidade.

As disciplinas se encontram na análise de responsabilidade do service e no custo/consistência das consultas. Para apresentação, explique por que N:M pode duplicar resultados, por que lazy loading acrescenta consultas e por que preservar comportamento durante refatoração exige testes e uma versão recuperável.
