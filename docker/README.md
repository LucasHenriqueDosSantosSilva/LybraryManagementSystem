# Etapa 15 — Execução com Docker Compose

Docker Engine e Compose v2 com BuildKit. O projeto compila backend e frontend em imagens por etapas, instala dependências do frontend por lockfile e fixa as bases por digest. Maven 3.9.11/Java 21, Node 24.10.0, Nginx 1.28.0 e MySQL 8.4.11 foram usados. Imagens finais de backend e Nginx executam com usuários sem root; MySQL segue seu entrypoint oficial.

```bash
cp .env.example .env
chmod 600 .env
# Edite .env e defina MYSQL_PASSWORD e MYSQL_ROOT_PASSWORD distintos.
docker compose config --quiet
docker compose up --build -d --wait
docker compose ps
```

Abra a aplicação local na porta FRONTEND_PORT (padrão 8088). Só essa porta é publicada, em 127.0.0.1. MySQL e backend permanecem na rede interna. `/api/` é encaminhado por Nginx ao backend, enquanto as rotas do React recebem index.html; não há proxy Vite no container final.

Healthchecks verificam conexão com o schema pelo usuário da aplicação, consulta funcional do backend e consulta da API pelo Nginx. depends_on aguarda saúde inicial; restart não substitui diagnóstico de erros persistentes. Healthcheck unhealthy, por si só, não reinicia um container que permanece em execução.

```bash
docker compose logs --tail=100 backend frontend
docker compose down
docker compose up -d --wait
```

`down` preserva o volume mysql-data. `down --volumes` **apaga os dados**; reserve essa opção a um ambiente descartável cujo conteúdo possa ser removido. Senhas e MYSQL_DATABASE são usados na inicialização de volume novo: mudar .env não altera usuários/schemas de um volume existente. Atualizar imagens fixadas exige nova revisão/testes; fixar digest não substitui manutenção.

## Builds e testes

Backend compila e empacota com `-DskipTests`: testes dependem de MySQL dedicado e devem ser executados separadamente, conforme backend/README.md. Frontend faz typecheck/build; `npm test` e Playwright permanecem comandos separados. Não interpretar build da imagem como aprovação de testes.

Nesta tarefa, os builds passaram, os três serviços ficaram healthy e os 26 testes Vitest passaram. Dois cenários Playwright executaram contra **Nginx + backend + MySQL dos containers**, incluindo circulação real e viewport 320 px. Usamos schema library_test exclusivo num volume próprio desse Compose; fixtures foram removidas. A persistência foi verificada recriando os três containers e consultando uma categoria fictícia criada antes; ela foi removida ao finalizar. A suíte anterior de 79 testes backend não foi reexecutada; Flyway e mapeamentos foram validados na inicialização do banco dos containers.

Para repetir E2E, use um projeto Compose descartável separado, com MYSQL_DATABASE=library_test e senhas próprias, antes de inicializar seu volume. Não apontar para dados de desenvolvimento ou produção. Exemplo após subir esse projeto na porta 8088:

```bash
cd frontend
E2E_DB_SCHEMA=library_test E2E_BASE_URL=http://127.0.0.1:8088 E2E_MYSQL_CONTAINER=library-management-mysql-1 PLAYWRIGHT_CHROMIUM_PATH=/usr/bin/chromium npm run test:e2e
```

E2E_MYSQL_CONTAINER deve ser o container desse projeto (outro nome de projeto gera outro nome); a limpeza administrativa remove somente o empréstimo da fixture com ids/matrícula próprios. E2E_DB_SCHEMA confirma a configuração, não muda o datasource. Consulte [integração em navegador](../docs/testing/browser-integration.md).

## Ambiente de nuvem com proxy

O build padrão atende máquinas com acesso aos registries/repositórios. Neste ambiente foi necessário um override local, fora do Git, com resolução do proxy e rede do host somente no build. Maven settings, truststore Java e CA do Node entram por secrets BuildKit opcionais: `maven_settings`, `java_cacerts`, `ca_certs`. Não copiar credenciais ou certificados privados para o contexto de build; não desativar validação TLS.

O helper salvo em `/workspace/library-runtime/compose-build.yaml` fornece os secrets externos e ajustes de rede desse ambiente. O Buildx usa diretório gravável porque o padrão aqui é somente leitura:

```bash
BUILDX_CONFIG=/workspace/tools/buildx docker compose --env-file /workspace/library-runtime/compose.env -f compose.yaml -f /workspace/library-runtime/compose-build.yaml build
docker compose --env-file /workspace/library-runtime/compose.env up -d --wait
```

compose.env guarda senhas locais fora do checkout, com permissão 600, e configura library_test para esta verificação. Nunca imprimir `docker compose config` resolvido com segredos; use `config --quiet`. Os helpers e processos precisam das instruções de ambiente salvas; não pressupor processos restaurados.

Esta é execução local reproduzível, sem autenticação, HTTPS ou deploy público. Próxima etapa: consolidar documentação e roteiro acadêmico de demonstração.
