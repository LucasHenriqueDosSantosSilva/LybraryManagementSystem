# Etapa 17 — Incremento 2: plano de VPS, HTTPS e limite de login

O usuário escolheu planejar uma VPS. Nenhum servidor foi contratado, conexão SSH realizada, domínio alterado ou serviço publicado. A configuração permanece local até serem definidos servidor/domínio e acesso de administração.

## Topologia proposta

Uma VPS Linux com Docker Engine e Compose, MySQL e backend na rede privada de containers, frontend Nginx terminando TLS. Só HTTPS recebe tráfego público; banco, backend e readiness HTTP não publicam portas. A implantação inicial usa uma única instância de backend, compatível com sessões e limite de login em memória.

Ubuntu 24.04 LTS é uma opção de sistema para o roteiro, não uma máquina já provisionada. CPU/RAM/disco precisam ser dimensionados conforme dados e carga; não foi feito benchmark que sustente capacidade, preço ou disponibilidade de provedor. Antes de publicar serão necessários IP/servidor, domínio com DNS, SSH por chave, credenciais externas e definição de armazenamento/backup. Não enviar chaves/senhas pelo chat.

## Limitação de login implementada

Uma janela fixa compartilhada por processo permite por padrão 10 POSTs de login em 60 segundos. LIBRARY_LOGIN_LIMIT aceita 1–100; LIBRARY_LOGIN_WINDOW_SECONDS aceita 1–3600. Conta única: todas as origens e nomes de usuário compartilham orçamento; mudar username ou forjar X-Forwarded-For não contorna o limite. Não há confiança em IP encaminhado pelo cliente.

O filtro atua antes de verificar senha e após CSRF. Tentativas com CSRF inválido já são rejeitadas sem BCrypt; logins válidos também contam. Excesso retorna 429 ProblemDetail e Retry-After em segundos, arredondado para cima. Relógio monotônico e sincronização protegem a janela sob concorrência. Não há bloqueio permanente de conta.

O custo é possível indisponibilidade temporária para usuários legítimos quando o orçamento é consumido. Janela fixa também permite rajadas junto à fronteira; não é proteção DDoS ou limite distribuído. Reiniciar o processo limpa a janela. Escalar réplicas exige limiter/sessões compartilhados ou política adicional na borda, com confiança no proxy explicitamente definida. Não resetar orçamento para contornar bloqueio normal em produção.

## HTTPS local implementado e verificado

`docker/compose.https.yaml` sobrepõe portas do frontend e ativa cookie Secure. Requer Compose >=2.24.4 (tag !override). Nginx recebe TLS em 8443 e certificados cert.pem/key.pem em volume externo read-only; UID 101 precisa ler a chave privada. Porta 8080 interna oferece apenas /api/health, sem login, SPA ou catálogo. Não fica publicada.

Exemplo da verificação em nuvem, após os builds do projeto:

```bash
TLS_TEST_DIR=/workspace/library-runtime/tls-test scripts/prepare_local_tls.sh
TLS_VOLUME=library-local-tls-test docker compose --env-file /workspace/library-runtime/compose.env -f compose.yaml -f docker/compose.https.yaml up -d --wait
```

O helper gera certificado localhost/127.0.0.1 de um dia e o renova quando faltam menos de uma hora. Só aceita volume com prefixo library-local-tls-, para não sobrescrever volume de certificados públicos. Não é ACME nem certificado de produção. Depois de renovação, recrie/recarregue o frontend para usar o novo certificado.

`scripts/verify_local_tls.py` requer TLS_CA_CERT apontando ao certificado de teste e LIBRARY_ADMIN_USERNAME/PASSWORD carregados de arquivo privado/ambiente, sem imprimir valores. Usa trust explícito e verifica hostname; prova também que o certificado descartável é rejeitado pelo trust padrão. Precisa de janela de login nova/ociosa, pois consome o orçamento para testar 429. Não tem -k, ignoreHTTPSerrors ou desativação de verificação.

## Sequência para publicação futura

1. Selecionar VPS/domínio e preparar acesso SSH por chave, atualizações e firewall. Nenhuma compra ou mudança remota foi autorizada apenas por este plano.
2. Construir imagens da revisão escolhida, configurar credenciais privadas e volume de banco. Validar serviços localmente na VPS antes de expor portas.
3. Obter certificado público correspondente ao domínio, definir emissão/renovação (ACME/DNS conforme provedor) e carregar cadeia/chave no volume com permissões compatíveis. Não usar o helper de certificado local para isso.
4. Revisar configuração de domínio no Nginx e binding público de 443 para 8443, substituindo loopback conscientemente. O overlay atual continua em 127.0.0.1; não publica sozinho. Se houver proxy externo, revisar cadeia de TLS/cabeçalhos confiáveis em vez de aceitar forwarding arbitrário.
5. Fazer backup e ensaio de restore em schema/volume separado, definir rollback de aplicação/migrations e credenciais. down --volumes não é rollback.
6. Verificar login, cookie Secure/HttpOnly, CSRF, 429, certificado/hostname, circulação e histórico com dados fictícios. Só então publicar e registrar URL/revisão/evidências reais.

Backup/restore foi ensaiado no [incremento 3](backup-restore.md); renovação pública ainda não foi ensaiada. nenhuma URL pública existe. Próximo passo: definir informações da VPS/domínio e operação externa de backup.

## Evidências desta entrega

86 testes backend aprovados, zero falhas/erros/ignorados: três unitários novos para janela/arredondamento, concorrência e configuração; uma integração para 429 inclusive com forwarding forjado. Builds Docker e três healthchecks passaram com TLS local. O verificador HTTPS passou para certificado/hostname, Secure cookie, sessão, CSRF, logout e Retry-After. Não houve teste de navegador com certificado autoassinado; os três cenários Chromium autenticados anteriores continuam como evidência de interface, sem atribuir a eles TLS desta execução. Frontend React não foi alterado neste incremento.
