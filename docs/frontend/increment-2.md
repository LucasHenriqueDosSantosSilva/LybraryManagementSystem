# Etapa 13 — Frontend, incremento 2: autores e livros

Novas rotas `/authors` e `/books`, com navegação no cabeçalho. Autores têm cadastro, edição, exclusão confirmada e paginação. Nome aceita até 150 caracteres e homônimos são permitidos pelo backend. Categorias e autores compartilham o pequeno formulário/listagem de registros nomeados; livros mantêm seu componente próprio, com relações e filtros específicos.

Livros têm criação, edição e exclusão confirmada, título, ISBN, ano, descrição, categoria e múltiplos autores. O servidor valida ISBN e vínculos; conflitos preservam os campos. Autores nos seletores incluem identificador para distinguir homônimos. Os seletores carregam todas as páginas de 100 referências, com cancelamento de leitura ao sair e erro/retry; falta de categoria ou autor desabilita cadastro e oferece links para as telas correspondentes.

Busca combinada por título, autor, ISBN e categoria envia parâmetros codificados, aplica filtros somente ao submeter e reinicia na página zero. Limpar busca remove filtros. Resultados são paginados de dez em dez. Livro com exemplares/histórico mantém restrições de alteração/exclusão do backend; não foi criada exclusão em cascata na interface.

## Verificação

12 testes de frontend passaram, sem ignorados, mais typecheck e build Vite. Novos testes verificam filtros codificados, valores numéricos e múltiplos autores no payload, conflito mantendo dados, pré-requisitos faltantes, endpoint/limite de nome de autor e referências além da primeira página. Os testes de componentes usam API substituta; não são testes completos de navegador.

O proxy Vite para API real foi exercitado criando categoria, autor e livro próprios (201), buscando com título/autor (200) e editando título/ano (200). A limpeza removeu somente esses três registros em ordem livro, autor, categoria (204). Backend não foi modificado, e os 79 testes anteriores não foram reexecutados nesta entrega.

Não houve inspeção visual em navegador, teste real de leitor de tela ou auditoria de acessibilidade. CSS e elementos nativos seguem a base responsiva; seleção múltipla usa select nativo com instrução. Grandes catálogos de referências poderão exigir busca de seletores em entrega futura; nesta versão não há corte silencioso em cem registros.

Próximo incremento: leitores e exemplares, preparando a interface de circulação. Consulte [execução](../../frontend/README.md).
