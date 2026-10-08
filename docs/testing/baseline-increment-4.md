# Validação — empréstimos e devoluções

Quarto incremento da etapa 8, com Java 21, Spring Boot 3.5.7, Maven Wrapper 3.9.11 e MySQL 8.4.11. V4 cria loans, FKs, CHECKs e índice único condicional por coluna virtual gerada. V1–V3 foram preservadas.

## Resultados

`python3 /workspace/library-runtime/run.py test package`: **39 testes**, zero falhas, erros ou ignorados; empacotamento passou. São 4 testes unitários e 35 de integração. Os 12 testes novos de circulação usam Clock controlável, MySQL real e requisições sem uma transação externa envolvendo o teste inteiro.

Cobertura comportamental adicional:

- empréstimo com prazo de 14 dias e atualização de disponibilidade;
- devolução, liberação e novo empréstimo do mesmo exemplar, preservando dois eventos;
- leitor inativo e exemplar retirado rejeitados;
- duplicidade de empréstimo e retirada durante empréstimo rejeitadas;
- devolução de leitor inativo permitida;
- exclusão de leitor/exemplar com histórico e alteração de ISBN histórico bloqueadas;
- devolução anterior ao empréstimo rejeitada; segunda devolução não altera a primeira data;
- atraso começa após vencimento e deixa de ser atraso atual após devolução;
- empréstimos concorrentes: um 201 e um 409, um registro ativo;
- devoluções concorrentes: um 200 e um 409;
- retirada versus empréstimo: somente um estado incompatível pode vencer;
- desativação que mantém bloqueio do leitor impede empréstimo após o commit;
- escrita SQL direta duplicada bloqueada pelo UNIQUE e vencimento inválido pelo CHECK;
- referências ausentes e filtro inválido tratados pela API.

A primeira execução de um teste SQL esperava DataIntegrityViolationException, mas o driver traduziu violação de CHECK para UncategorizedSQLException, com código MySQL 3819. A expectativa foi corrigida para verificar classe, código e constraint ck_loans_due; a constraint não foi alterada ou desativada.

## Verificação HTTP

API reiniciada; V4 aplicada no schema de desenvolvimento e schema validado pelo Hibernate. Smoke criou fixtures próprias, emprestou, confirmou indisponibilidade e conflitos, desativou leitor, devolveu, confirmou liberação e histórico, rejeitou segunda devolução e exclusões históricas.

Como o domínio impede apagar histórico pela API, a limpeza administrativa removeu somente os ids dos registros fictícios desta execução, numa transação e em ordem de dependência. Nenhum dado preexistente foi removido. Essa limpeza não é uma funcionalidade pública do produto.

## Conceitos e limites

Bloqueios coordenam verificações de estado; UNIQUE é uma defesa independente para exclusividade. A coluna gerada é um mecanismo físico redundante, distinguido do modelo lógico normalizado. Prazo contratado é armazenado; atraso e disponibilidade são derivados.

O relógio controlável permitiu verificar limites de datas sem esperar dias reais. Os testes foram escritos neste incremento; não há histórico RED/GREEN e não serão apresentados como TDD. Não há métricas de cobertura coletadas.

Dashboard, busca do catálogo, frontend e autenticação pública ainda não existem. A baseline não recebe tag de conclusão até fechar os fluxos restantes e sua documentação. Os testes concorrentes exercitam cenários específicos, sem prometer ausência universal de deadlocks, falhas de rede ou problemas em topologias não verificadas.
