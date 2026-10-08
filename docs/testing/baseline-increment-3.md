# Validação — leitores e exemplares

Terceiro incremento da etapa 8. A V3 acrescenta readers e book_copies sem alterar V1/V2. Ambiente: Java 21, Maven Wrapper 3.9.11, Spring Boot 3.5.7 e MySQL 8.4.11.

## Execução

`python3 /workspace/library-runtime/run.py test package` passou: **27 testes**, zero falhas, erros ou ignorados. São 4 testes unitários e 23 de integração, incluindo os 19 anteriores e 8 novos. Hibernate validou o schema e o JAR foi empacotado.

Novos testes verificam:

- CRUD de leitor, normalização e ativação/desativação;
- matrícula única com rollback, e-mail compartilhado permitido;
- entrada inválida, status ausente e paginação fora do limite;
- CRUD de exemplar, normalização, disponibilidade inicial e retirada;
- código patrimonial único e rejeição de troca de livro, preservando os dados;
- rejeição de exclusão de livro com exemplares e rollback dos vínculos de autores;
- livro inexistente e código inválido;
- dois exemplares associados a um único registro de livro.

Fixtures de integração criam registros próprios e removem somente esses registros em ordem de dependência. Continuar usando schema de testes separado, sem dados pessoais ou de produção.

A API foi reiniciada e aplicou V3 em desenvolvimento. Smoke HTTP confirmou cadastro de leitor, matrícula duplicada 409, desativação, cadastro de exemplar, tentativa de trocar livro 409, retirada com available=false e exclusão de livro vinculado 409, preservando autorias. Os dados temporários foram removidos.

## Conceitos e limites

Matrícula e código patrimonial são chaves candidatas mutáveis; ids mantêm as referências estáveis. Relação livro 1:N exemplares diferencia edição e unidade física. DELETE do livro e remoção da autoria estão na mesma transação: falha por FK reverte tudo.

Sem empréstimos, todos os exemplares em circulação aparecem disponíveis. Esse cálculo será ampliado para verificar empréstimos ativos; retirada e exclusão passarão a respeitar histórico. Leitores hoje não têm histórico. Ainda não há testes de circulação concorrente, dashboard, busca por filtros ou frontend. Não concluir a baseline inteira ou registrar tag de versão funcional completa nesta etapa.

Próximo incremento: empréstimo e devolução com datas consistentes, índice único condicional, coordenação transacional e preservação de histórico.
