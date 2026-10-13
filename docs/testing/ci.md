# Validação contínua — preparação para publicação

Workflow `.github/workflows/verify.yaml` para push em main, pull requests e disparo manual. Dois jobs independentes em Ubuntu 24.04: backend com Java 21/MySQL 8.4 e frontend com Node 24. Não publica imagens, não faz deploy e não acessa servidores/credenciais de produção.

Backend usa schema library_test exclusivo no serviço MySQL efêmero. Senhas declaradas no workflow são fixtures descartáveis de CI, não segredos existentes do aplicativo. Healthcheck verifica usuário/schema antes de Maven; wrapper valida a distribuição fixada. Comando: `./mvnw -B test package`. Testes de autenticação/CSRF/limite habilitam segurança com credencial fictícia própria; testes de regras isolam segurança explicitamente.

Frontend instala com npm ci e lockfile, executa npm test e npm run build (inclui TypeScript). Cache guarda dependências, não dumps/credenciais. Actions oficiais checkout/setup-java/setup-node são fixadas por commits resolvidos das refs v5 durante esta entrega; atualizar SHA requer revisão. Checkout não mantém credencial Git, e o workflow só concede contents:read. Execuções obsoletas da mesma ref são canceladas.

## Verificação

Actionlint 1.7.7 aprovou o workflow. Binário obtido da release oficial com SHA-256 publicado verificado antes de executar. Os comandos equivalentes foram executados nesta máquina: **86 testes backend**, **30 testes frontend**, instalação congelada, typecheck e builds aprovados, zero falhas/erros/ignorados. Um teste novo verifica AC15 mantendo a primeira requisição de empréstimo pendente e recusando segundo clique sem falso sucesso.

Essa execução local usa o proxy/trust e as credenciais locais via helper; ela não comprova o resultado em runner do GitHub. A primeira execução remota deve ser consultada na aba Actions; não marcar CI remoto como aprovado apenas porque YAML e comandos locais passaram. Políticas da conta/repositório podem impedir execução automática.

## Escopo e limites

Workflow atual não executa browser, build de imagens, TLS ou restore. Esses procedimentos têm evidência local documentada e precisam de jobs isolados próprios antes de ampliar CI, sem conectar a produção. Não há medição de cobertura, validação completa de acessibilidade ou prova de prontidão pública.

A [matriz de aceite](acceptance-review.md) registra o alcance de cada evidência. Um pipeline verde não substitui revisão, backup, operação de certificados, proteção de segredos ou decisão de publicação.
