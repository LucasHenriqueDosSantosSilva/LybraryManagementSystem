# Etapa 13 — Frontend, incremento 3: leitores e exemplares

Novas rotas `/readers` e `/copies`, acessíveis pelo cabeçalho. Ambas oferecem criação, edição, exclusão confirmada, paginação de dez itens e estados de carregamento, vazio, erro/retry e sucesso confirmado. O hook usePagedResource concentra somente leitura paginada, com cancelamento ao desmontar e retorno à página anterior quando a atual fica vazia. Formulários e ações de negócio permanecem por tela.

Leitores têm matrícula, nome e e-mail opcional; e-mail vazio é enviado como null. Situação ativa/inativa é exibida e alterada por PATCH específico, após confirmação, sem ser incorporada ao PUT de dados cadastrais. Desativação impede novos empréstimos e preserva devoluções e histórico. Exclusão com histórico permanece bloqueada pela API, e a interface exibe o conflito.

Exemplares têm código patrimonial e livro, selecionado entre todas as páginas do catálogo. Falha ao carregar referências tem retry, e ausência de livros oferece link para cadastro. Lista diferencia disponível, emprestado e retirado. Retirada exige confirmação com aviso de que não há reativação no MVP; a ação é desabilitada para exemplares não disponíveis ou já retirados. Essa proteção visual não substitui a transação no servidor: mudanças concorrentes podem retornar 409, que é exibido sem falso sucesso. Exclusão e mudança de vínculo preservam restrições de histórico da API.

## Verificação

**20 testes de frontend**, zero falhas/ignorados, typecheck e build Vite aprovados. Oito novos testes verificam leitor com e-mail null, confirmação de status e atualização da lista, conflito de exclusão com histórico, cadastro de exemplar com bookId numérico, confirmação/atualização de retirada, bloqueio da retirada quando emprestado, PUT de leitor sem alterar status e conflito de edição de exemplar preservando dados.

Os testes de componentes usam API substituta. Requisições reais através do proxy Vite verificaram criação de fixtures próprias de categoria, autor, livro, leitor e exemplar (201); PUT de leitor/exemplar, PATCH de desativação/reativação e POST de retirada (200); GET das listas (200). A limpeza removeu somente essas fixtures em ordem exemplar, leitor, livro, autor, categoria (204). Nenhum empréstimo ou histórico foi criado nesse smoke.

Backend não foi alterado; sua suíte anterior de 79 testes não foi reexecutada nesta entrega. Não houve teste completo em navegador, inspeção visual ou auditoria de acessibilidade. Permanecem os elementos nativos e associações de mensagens de campo; confirmação é um bloco na página.

Próximo incremento: circulação, com registro de empréstimos, devoluções e consulta de histórico. Veja [execução](../../frontend/README.md).
