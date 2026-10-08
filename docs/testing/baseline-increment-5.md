# Validação — busca e dashboard

Quinto incremento da etapa 8. Não altera schema nem migrations V1–V4; acrescenta consultas e contratos HTTP.

## Resultados

`python3 /workspace/library-runtime/run.py test package`: 43 testes, zero falhas, erros ou ignorados; build e empacotamento passaram. São 4 testes unitários e 39 de integração. Quatro testes novos verificam dashboard sem atividade, indicadores/ranking com dois autores, filtros combinados com ISBN canônico e caracteres percent/underscore literais.

Dashboard distingue edições e exemplares, leitores registrados e ativos, empréstimos ativos e atrasados. O teste de circulação cria dois eventos do mesmo exemplar, um devolvido e um ativo, e confirma contagem 2 no ranking, sem multiplicar pelos dois autores. Após devolução e retirada, disponibilidade continua zero e total de exemplares continua um. Os exemplos são dados de teste, não métricas de uso da biblioteca.

A busca usa EXISTS para autoria, sem duplicar livros; títulos/autores são substrings conforme a collation do MySQL. Não afirmamos que os índices atuais tornam essas buscas eficientes em qualquer volume: planos e desempenho continuam sem medição.

API reiniciada e smoke HTTP passou com dois autores de nomes correspondentes: busca retornou um livro, dashboard registrou um evento e rejeitou limite inválido. Fixtures temporárias foram removidas administrativamente, somente pelos ids criados, em transação.

## Banco criado do zero

Em um schema novo `library_validation`, Flyway aplicou as quatro migrations até V4 e a suíte completa passou novamente: 43 testes, zero falhas, erros ou ignorados. O schema temporário criado para essa verificação foi removido ao final. Isso valida instalação limpa do banco nesta máquina; não comprova restauração em uma nova tarefa da nuvem.

## Consistência

As consultas do dashboard compartilham snapshot InnoDB em transação REPEATABLE_READ e uma data de referência obtida do Clock. Isso mantém consistência entre indicadores naquela leitura, sem prometer atualização em tempo real. Ranking e recentes têm desempate explícito e limites de resultado.

## Estado da baseline

O backend cobre cadastros, exemplares, circulação, histórico, busca e dashboard. Ainda não há frontend, autenticação pública, Compose do projeto ou deploy. Os pontos de projeto da baseline permanecem para análise posterior: camadas globais, repetição de mapeamento/busca, acoplamento dos services a HTTP, possíveis consultas adicionais por livro e lógica de status duplicada no relatório. São candidatos a examinar, não métricas já coletadas.

Nenhum teste foi apresentado como TDD. A próxima etapa registra evidências dos problemas e define refatorações que preservem comportamento.

A tag `v0.1-baseline-backend` preserva esta versão para análise/refatoração. Representa a baseline do backend, não uma versão completa do produto com frontend ou pronta para publicação pública.
