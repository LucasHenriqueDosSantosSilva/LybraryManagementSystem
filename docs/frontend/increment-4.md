# Etapa 13 — Frontend, incremento 4: circulação

Rota `/loans`, acessível por Circulação, completa as telas do fluxo principal: registro de empréstimo, devolução confirmada e histórico paginado com leitor/situação. Cadastro carrega todas as páginas de leitores, exemplares e livros; somente leitores ativos e exemplares disponíveis/em circulação são oferecidos para novo empréstimo. Falhas de referência têm retry, e ausência de pré-requisitos oferece links para cadastro.

Histórico mantém leitores inativos e identifica leitor, patrimônio, livro, datas e situação. ACTIVE no filtro é apresentado como “Não devolvidos (inclui atrasados)”, preservando a semântica da API. Datas são formatadas a partir de strings de calendário, sem conversão UTC que possa alterar o dia.

Registro e devolução aguardam confirmação do servidor antes de apresentar sucesso, com datas retornadas pela API. Ambos atualizam o histórico e os seletores para refletir disponibilidade. Empréstimos devolvidos não têm ação de devolver. A interface não bloqueia devolução por leitor inativo. Conflitos são exibidos sem falso sucesso e preservam os dados para revisão; não há repetição automática de mutações. Falha de conexão orienta atualizar e consultar histórico antes de tentar novamente, porque a requisição pode ter sido registrada.

## Verificação

**26 testes de frontend**, zero falhas/ignorados, typecheck e build Vite aprovados. Seis testes novos verificam seleção de leitor ativo/exemplar disponível, histórico com leitor inativo, payload numérico e atualização de referências após empréstimo, conflito preservando seleção, devolução confirmada e histórico atualizado, filtros com semântica ACTIVE e orientação diante de falha de conexão sem retry automático.

Backend e Vite foram reiniciados, pois os processos anteriores não estavam ativos. Pelo proxy e API real, criamos fixtures próprias de categoria/autor/livro/leitor/exemplar e um empréstimo (201), verificamos indisponibilidade, empréstimo duplicado rejeitado (409), desativamos o leitor e devolvemos com sucesso (200). Confirmamos disponibilidade restaurada, segunda devolução rejeitada (409) e histórico RETURNED por leitor (200).

A API não permite apagar histórico. Somente o empréstimo fictício criado neste smoke foi removido administrativamente, com DELETE restrito simultaneamente ao id do empréstimo, exemplar e leitor próprios. Em seguida, as cinco fixtures foram removidas pelas rotas normais (204). Nenhum histórico preexistente foi removido. Esse procedimento serve para teste isolado, não para operação de produção.

Backend inalterado; sua suíte de 79 testes não foi reexecutada nesta entrega. Testes de componentes usam API substituta e o smoke foi HTTP: ainda não houve execução completa em navegador ou auditoria visual/de acessibilidade.

## Situação da etapa

As telas principais do MVP estão implementadas: dashboard, categorias, autores, livros, leitores, exemplares e circulação. Isso conclui a implementação incremental da etapa 13; a etapa 14 deverá verificar a integração por navegador e resolver problemas encontrados. Autenticação, deploy público e containers do projeto continuam em etapas posteriores. Consulte [execução](../../frontend/README.md).
