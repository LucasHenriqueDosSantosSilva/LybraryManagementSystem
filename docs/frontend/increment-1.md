# Etapa 13 — Frontend, incremento 1

Primeira entrega de interface: dashboard com totais, ranking e atividade recente; gerenciamento de categorias; navegação React Router e página não encontrada. Cliente HTTP centralizado trata ProblemDetail, falha de conexão e respostas 204 sem tentar ler JSON.

Os componentes mantêm estado local, formulários e feedback por tela. O fluxo de categorias inclui paginação de 10 itens, carregamento, lista vazia, erro com retry, criação, edição e exclusão com confirmação. Uma página que fica vazia após exclusão volta à anterior. Integridade dos vínculos é aplicada no servidor, com conflito exibido sem sucesso falso.

O build de TypeScript/Vite passou e 7 testes de frontend passaram, sem ignorados. A suíte backend de 79 testes não foi reexecutada: nenhum arquivo do backend foi alterado. O servidor Vite e o backend rodando nesta máquina foram verificados por HTTP; proxy retornou 200 em dashboard/categorias e 201/200/204 no ciclo de uma categoria fictícia própria, removida ao final.

A implementação inclui CSS responsivo e suporte básico de teclado/semântica, mas não houve inspeção visual real, teste de leitor de tela ou auditoria de acessibilidade. Tampouco foram implementadas ainda as telas de autores, livros, leitores, exemplares e empréstimos. Essas limitações são distintas de falha do build ou de integração HTTP.

Consulte [execução e limites](../../frontend/README.md) e [contrato REST](../api/rest-contract.md). A próxima entrega amplia o catálogo, mantendo frontend incremental.
