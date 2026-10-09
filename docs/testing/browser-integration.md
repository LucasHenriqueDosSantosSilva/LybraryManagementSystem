# Etapa 14 — Integração em navegador

Playwright 1.56.1 executado com Chromium 151.0.7922.173 disponível em `/usr/bin/chromium`. O download do navegador empacotado falhou por bloqueio dos domínios CDN; a execução usou o navegador instalado, sem desativar TLS nem ampliar a política de rede. Versão do navegador da imagem pode variar em outras máquinas.

## Preparação e execução verificadas

Requer Node/npm, dependências do frontend (`npm ci`), Java/Maven, Docker com container `library-mysql` e schema **library_test exclusivo e vazio**. Não executar junto da suíte backend nem apontar para produção. O teste cria fixtures e precisa de acesso administrativo local para remover somente seu próprio empréstimo fictício, pois a API preserva histórico. Não é um teste adequado contra servidor público.

No checkout, iniciar backend de teste em sessão persistente:

```bash
docker start library-mysql
python3 /workspace/library-runtime/run.py spring-boot:run '-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/library_test --server.port=8081'
```

Aguardar startup e GET de `/api/categories` na porta 8081 com 200. Em outra sessão, no diretório frontend:

```bash
LIBRARY_API_TARGET=http://127.0.0.1:8081 npm run dev -- --port 5174 --strictPort
```

Confirmar `/api/categories` via porta 5174. Na terceira sessão, em frontend:

```bash
E2E_DB_SCHEMA=library_test PLAYWRIGHT_CHROMIUM_PATH=/usr/bin/chromium npm run test:e2e
```

E2E_DB_SCHEMA é uma confirmação da configuração realizada, não muda o datasource e não comprova por si o banco usado. O operador deve conferir o comando do backend. PLAYWRIGHT_CHROMIUM_PATH seleciona o navegador do sistema; omiti-la usa o navegador empacotado do Playwright, que precisa estar instalado. Não presumir compatibilidade com qualquer versão do Chromium.

## Evidências

Dois cenários passaram, sem retries ou ignorados:

- Fluxo com UI real e API/MySQL: categoria, autor, livro com relações, leitor, exemplar, empréstimo, desativação de leitor, devolução e histórico filtrado. Confere indisponibilidade após empréstimo, conflito duplicado via API, disponibilidade após retorno e ausência de botão para devolver novamente. Captura erros JavaScript da página e espera lista vazia de erros.
- Todas as sete rotas em viewport 320×800: heading visível e ausência de transbordamento horizontal, link de salto por teclado e transferência de foco ao main. O main ganhou tabIndex=-1 para ser destino de foco, sem entrar na ordem normal de Tab.

26 testes Vitest continuam passando e typecheck/build aprovados. Backend não foi alterado; os 79 testes anteriores não foram reexecutados. Os testes de navegador são separados de Vitest pela configuração, com um worker e sem paralelismo de fixtures.

A inspeção das capturas de [desktop](browser/dashboard-desktop.png) e [320 px](browser/dashboard-mobile.png) verificou disposição, legibilidade e quebra de navegação do dashboard. São artefatos de uma execução com dados fictícios; não screenshots de produção ou prova de todas as telas/estados.

## Isolamento e limites

Fixtures têm nomes/matrícula/patrimônio únicos. A limpeza de histórico restringe empréstimo por id, leitor, exemplar e matrícula própria normalizada, usando o container local; depois remove os próprios cadastros pela API em ordem dependente. Não remove histórico preexistente. Uma interrupção forçada pode exigir limpeza manual dos ids próprios registrados no trace; não esvaziar o schema indiscriminadamente.

Os primeiros ensaios revelaram consultas imprecisas do teste a selects e uma comparação de matrícula sem sua normalização em maiúsculas. Foram corrigidos nos testes; fixtures desses ensaios foram identificadas e removidas individualmente. Não houve regressão da regra de negócio. Traces/screenshots de falha são ignorados no Git; screenshots documentais selecionados são versionados.

Ainda não há auditoria formal de acessibilidade, testes de leitor de tela, Firefox/WebKit, carga ou rede instável no navegador. A seleção múltipla foi exercitada para cadastro com um autor; múltiplos autores seguem cobertos nos testes de componentes. Próxima etapa: Docker para reprodução do projeto.

Os mesmos cenários foram verificados contra os containers na etapa 15. E2E_BASE_URL permite escolher a origem local e E2E_MYSQL_CONTAINER identifica o container de testes para limpeza própria; veja [Docker](../../docker/README.md).
