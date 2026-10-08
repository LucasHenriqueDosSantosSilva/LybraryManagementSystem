# Validação — catálogo com autores e livros

Segundo incremento da etapa 8, executado no mesmo ambiente MySQL 8.4.11 com JDK 21, Spring Boot 3.5.7 e Maven Wrapper 3.9.11. A V1 foi preservada; V2 cria authors, books e book_authors e estabelece FKs restritivas.

## Resultados executados

`python3 /workspace/library-runtime/run.py test package`: 19 testes, zero falhas, erros ou ignorados; empacotamento passou. Inclui os 7 testes anteriores, 3 testes unitários de ISBN e 9 testes de integração do catálogo.

Casos adicionais: ISBN-10/13 equivalente e ISBN-10 terminado em X; checksums e formatos inválidos; vínculos válidos; duplicidade de edição com rollback; autor inexistente sem cadastro parcial; categoria inexistente; lista de autores vazia; ano inválido; exclusão de autor/categoria vinculados bloqueada; alteração das associações; exclusão de livro sem remover autores; CRUD de autores com homônimos.

Os testes de catálogo executam requisições sem uma transação externa envolvendo o caso inteiro, permitindo confirmar rollback do service e consultar o estado após conflito. Fixtures criam registros próprios; limpeza remove esses registros e seus vínculos, sem truncar tabelas ou limpar schema. A suíte deve usar banco de testes dedicado.

API foi reiniciada, aplicou V2 no schema de desenvolvimento e passou no smoke HTTP: criar categoria, autor e livro; normalizar ISBN; impedir exclusões vinculadas e edição duplicada; consultar, editar e excluir livro; preservar autor. Os registros temporários foram removidos.

## Aprendizado e limites

Relação N:M exige tabela de associação; um JOIN ou coleção não substitui a FK. A constraint UNIQUE opera sobre ISBN canônico, enquanto o dígito verificador é validado pelo código. Levar livro e associações na mesma transação evita registros parciais.

A baseline continua com camadas globais, repetição de busca/mapeamento e dependência de HTTP nos services. Não refatoramos ainda; não coletamos métricas, nem demonstramos TDD. Busca por filtros, leitores, exemplares, empréstimos e frontend não estão implementados. Não existe ainda histórico para restringir mudança de ISBN.
