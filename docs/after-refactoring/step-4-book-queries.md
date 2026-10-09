# Etapa 10 — Incremento 4: autores carregados em lote

Resolve P02 da análise da baseline. Código medido no commit 1616cfb; revisão completa no [JSON da observação](catalog-query-observation.json). A tag v0.1-baseline-backend permanece intacta.

## Antes → problema → depois

BookService.list paginava livros e acessava uma coleção lazy de autores por item ao mapear DTOs. No experimento da baseline, páginas com 1, 2 e 3 livros executavam 3, 4 e 5 SELECTs: página, count e uma consulta de autores por livro.

Agora a consulta original continua paginada no banco, com os mesmos filtros, total e ordem. Para uma página não vazia, uma segunda consulta `fetchAuthors(ids)` faz LEFT JOIN FETCH somente dos livros selecionados. O contexto JPA inicializa as coleções das mesmas instâncias; o mapeamento posterior não precisa disparar uma consulta para cada livro.

Não aplicamos JOIN FETCH de coleção à consulta paginada, evitando paginação em memória ou duplicação da página. Não buscamos o catálogo inteiro, nem limitamos indevidamente a coleção aos autores que corresponderam ao filtro. Página vazia não executa o fetch de autores.

## Medição antes/depois

Mesmo script, fixtures (três livros com um autor compartilhado), limites e processo local com logging SQL DEBUG:

| Livros retornados | Baseline: SELECTs | Após refatoração: SELECTs |
| --- | --- | --- |
| 1 | 3 | 3 |
| 2 | 4 | 3 |
| 3 | 5 | 3 |

No experimento, os três SELECTs são página, count e autores em lote. Dependendo da página, Spring Data pode omitir count quando consegue inferir o total; portanto não prometemos exatamente três consultas em todo caso. A medição confirma remoção do crescimento por livro neste fluxo e configuração. Não medimos latência, throughput ou uso de memória, nem extrapolamos um percentual de ganho para o sistema inteiro.

Evidências originais permanecem em docs/before-refactoring/catalog-query-observation.json. Fixtures desta observação foram removidas pela API; nenhum dado preexistente foi apagado.

## Validação

`python3 /workspace/library-runtime/run.py test package`: **53 testes**, zero falhas, erros ou ignorados; build e empacotamento passaram. O novo teste usa três livros, dois autores no primeiro e páginas de tamanho dois para confirmar:

- ordenação estável por id;
- totalElements=3 e totalPages=2;
- dois resultados na primeira página e um na segunda;
- autores completos em cada livro;
- filtro por um autor não remove o outro autor da resposta.

Os 52 testes anteriores, inclusive filtros combinados e concorrência de circulação, permanecem passando. A observação usou HTTP real e registrou SQL da versão medida. A API foi reiniciada sem logging SQL de diagnóstico ao terminar.

## Conceitos e limites

O problema não era a normalização ou a relação N:M, mas a estratégia de carregamento. Paginar registros principais e carregar relações da página em lote mantém o modelo e reduz idas ao banco. Isso conecta consultas SQL e persistência JPA à separação entre entidade e DTO.

Este incremento não corrige o custo de disponibilidade por exemplar (P08), mistura de consultas no DashboardService (P05), duplicação da paginação (P04) ou organização por domínio (P06). Nenhuma migration ou contrato HTTP mudou.

Próximo incremento: observar e corrigir as consultas de disponibilidade da listagem de exemplares (P08), preservando a regra derivada e a concorrência de circulação.
