# Etapa 17 — Incremento 1: autenticação para preparar o deploy

A API agora exige autenticação por padrão, inclusive para leitura do catálogo. Uma conta de bibliotecário é configurada externamente; ela não é um Reader nem entra nos totais do dashboard. Não há cadastro público de contas, recuperação de senha ou gestão de múltiplos usuários nesta entrega.

## Configuração

LIBRARY_ADMIN_USERNAME padrão bibliotecario; LIBRARY_ADMIN_PASSWORD é obrigatória com autenticação habilitada: ao menos 12 caracteres e no máximo 72 bytes UTF-8 para BCrypt. Ausência/valor inadequado impede inicialização; não há senha padrão nem senha gerada pelo Spring nos logs. O usuário configurado tem papel LIBRARIAN, e o hash BCrypt é mantido em memória. Alterar credencial exige reiniciar a aplicação.

Para Compose, preencha o novo campo em .env; o serviço encaminha usuário/senha para o backend e mantém autenticação ativada. Não imprimir configuração resolvida. No ambiente de nuvem, credencial local aleatória fica em `/workspace/library-runtime/auth.env` e também no compose.env privado; o helper run.py carrega auth.env sem exibir valores. Não copiar essas senhas para documentação, commits, screenshots ou chat.

LIBRARY_AUTH_ENABLED=false é um override explícito para testes/desenvolvimento isolado. Os testes de negócio anteriores utilizam esse override, enquanto AuthenticationIntegrationTest força autenticação e uma credencial fictícia de teste. Compose não expõe uma opção para desabilitar autenticação. Não usar o override em publicação.

## Sessão e proteção de escrita

Cookie JSESSIONID HttpOnly e SameSite=Lax; timeout de 30 minutos de inatividade, sessão no servidor. Spring Security mantém autenticação e proteção contra fixação de sessão. Não há JWT ou senha em localStorage/sessionStorage. A senha existe apenas no campo/estado temporário do formulário e é limpa após tentativa.

GET /api/auth/session fornece authenticationRequired, authenticated e username. GET /api/auth/csrf fornece token mascarado e nome do header, sem autenticação prévia, permitindo proteger login. POST /api/auth/login usa form-urlencoded com username/password e token CSRF; sucesso 204, credenciais inválidas 401. POST /api/auth/logout exige CSRF, invalida sessão e remove cookie, retornando 204.

O cliente consulta um token antes de cada operação não segura, incluindo login/logout, evitando reutilizar token anterior à autenticação. Não repete mutações automaticamente. GET protegido sem sessão retorna 401; token ausente/inválido em escrita retorna 403. A interface retorna ao login quando a API informa 401. Problemas de conexão/credenciais não mostram dados privados nem sucesso falso.

GET /api/health é público e retorna apenas status UP/DOWN (200/503), testando conexão ao banco. Substitui consultas de catálogo nos healthchecks; acesso anônimo ao catálogo continua bloqueado. Falhas de autenticação/CSRF usam application/problem+json.

## Validação

Backend: **82 testes**, zero falhas/erros/ignorados. Três novos testes cobrem acesso anônimo/readiness, login sem CSRF/credenciais inválidas e sessão válida com CSRF obrigatório e logout invalidando sessão. Frontend: **29 testes**, typecheck e build aprovados; testes novos cobrem gate de sessão/retry, login/logout sem persistência de senha e header CSRF antes de mutação.

Navegador: três cenários Chromium passaram com autenticação habilitada — circulação autenticada, layout/teclado em 320 px e login inválido/logout bloqueando acesso privado. E2E usa E2E_USERNAME/E2E_PASSWORD de conta descartável, sem trace de requisições que possa registrar senha/token. O fluxo administrativo de limpeza usa cookie/CSRF e remove somente fixtures próprias; não usar credenciais de produção nesses testes.

## O que ainda falta para publicar

Não houve deploy. É necessário escolher destino, configurar HTTPS, habilitar LIBRARY_COOKIE_SECURE=true na origem HTTPS e tratar confiança no proxy, limites de tentativas no login, gestão segura das credenciais, backup/restore e operação. O incremento 2 acrescentou limite por processo de 10 tentativas/60 segundos, com 429 e Retry-After; isso não é configuração pública final. Veja [VPS e HTTPS](vps-plan.md). Cookie Secure permanece false somente para HTTP local. Sessões são em memória e não sobrevivem a reinícios; não há replicação entre instâncias.

Roteiro atual: escolher a infraestrutura e testar esses ajustes antes de publicar. As evidências locais e de containers não comprovam acesso em domínio público.

As imagens/healthchecks Compose foram reconstruídos e os mesmos três cenários passaram também contra Nginx, backend e MySQL dos containers com autenticação habilitada.
