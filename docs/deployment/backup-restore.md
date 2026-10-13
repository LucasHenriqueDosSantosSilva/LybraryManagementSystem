# Etapa 17 — Incremento 3: backup lógico e ensaio de restore

`scripts/mysql_backup.py` usa mysqldump/mysql do container MySQL, lendo a senha root existente somente dentro dele. Não pede senha no chat, não usa senha em argumentos e não imprime SQL/dados. Requer Docker no host e acesso autorizado ao container. Bancos de sistema são recusados.

## Criar backup

Exemplo da verificação local, com arquivos fora do checkout:

```bash
mkdir -p /workspace/library-runtime/backups
chmod 700 /workspace/library-runtime/backups
python3 scripts/mysql_backup.py backup --container library-management-mysql-1 --database library_test --file /workspace/library-runtime/backups/library-session.sql.gz
```

Escolha nome único para cada execução; destino, metadata ou log existente não são sobrescritos. Em VPS, substitua container/schema/caminho pelos reais e use diretório privado fora do repositório. Uma rotina agendada deve gerar nomes únicos e verificar saída zero, nunca assumir sucesso pela existência de um arquivo.

Dump com single-transaction/quick inclui dados, tabelas, triggers, routines e events, sem CREATE DATABASE/USE gerados por --databases e sem DROP TABLE inicial. InnoDB permite snapshot transacional; não executar migrations/DDL durante o dump. Binlogs e bancos de sistema não entram; isso não oferece recuperação para qualquer instante.

Saída: SQL gzip e `.json` com formato, schema de origem, instante UTC técnico, tamanho e SHA-256 do arquivo comprimido. Gzip é validado antes de publicar o arquivo; publicação do dump é atômica e recusa arquivo concorrente existente. Um dump falho não vira backup final. Arquivos temporários são removidos; erro do mysqldump vai para log privado. Um backup já publicado sem metadata após falha de filesystem precisa ser tratado como incompleto, não restaurado automaticamente.

Arquivo, metadata e logs são criados com permissão 600; diretório deve ser 700. Gzip **não é criptografia**. Backups podem conter nomes, e-mails e histórico: armazenamento externo requer criptografia e acesso restrito. SHA-256 detecta alterações acidentais, não autentica um arquivo contra atacante que altere também a metadata. Use somente backups confiáveis gerados pelo procedimento, nunca SQL arbitrário: o importador administrativo não é sandbox de SQL.

## Restaurar somente em banco novo

```bash
python3 scripts/mysql_backup.py restore --container library-management-mysql-1 --database library_restore_review_01 --file /workspace/library-runtime/backups/library-session.sql.gz
```

O destino deve começar com library_restore_, ter identificador minúsculo válido e não existir. Não há modo de substituir library/library_test. O runner valida formato/checksum e gzip completo antes de criar o schema. CREATE DATABASE não usa IF NOT EXISTS, recusando criação concorrente. Depois importa nesse banco novo.

Se o import falhar, o novo banco parcial fica para diagnóstico, e o runner termina com erro; não há apagamento automático ou tentativa de escrever no banco atual. Log privado identifica a falha. O prefixo e os guardrails evitam restauração acidental sobre dados normais, mas não substituem confiança no dump: stored routines/events podem ter referências específicas a schema e exigem revisão antes de mudança de nome. O projeto atual não tem routines/events próprias.

Depois do restore, compare schema, dados, histórico de migrations e constraints; provisione um usuário de aplicação com acesso somente ao schema restaurado (não usar root) e abra uma instância isolada apontando para ele e verifique casos funcionais antes de decidir qualquer corte. O teste deste incremento não mudou o datasource nem promoveu o banco restaurado para produção.

## Ensaio executado

```bash
python3 scripts/test_backup_restore.py --container library-management-mysql-1 --application-database library_test --directory /workspace/library-runtime/backups
```

O teste criou schemas novos com UUID, aplicou migrations reais V1–V4, copiou apenas metadata Flyway e inseriu fixtures fictícias próprias: categoria/autor/livro/associação, dois leitores, dois exemplares e dois empréstimos (um ativo e um devolvido). Não copiou registros pessoais do banco da aplicação para as fixtures.

Passaram:

- Backup e metadata privados, com gzip/hash válidos.
- Restore e igualdade dos dados/DDL das oito tabelas (incluindo Flyway).
- Histórico ativo/devolvido e rejeição por UNIQUE ativo, FK e CHECK de datas, verificando códigos MySQL 1062/1452/3819.
- Recusa de sobrescrita do arquivo, destino existente e destino normal da aplicação.
- Recusa de checksum alterado e gzip inválido, sem criar o destino.
- Contagens do banco da aplicação inalteradas; somente schemas UUID criados pelo teste foram apagados ao concluir.

Arquivos de ensaio ficaram privados fora do Git. Interrupção/falha pode deixar schema parcial para revisão; não executar limpeza por wildcard. O ensaio usa poucos registros, não mede RTO ou desempenho de dump/import em carga. A aplicação não foi alterada, portanto suas suítes anteriores (86 backend/29 frontend/3 browser) não foram reexecutadas.

## Plano de operação na VPS

Proposta a aprovar ao definir infraestrutura: backup diário e antes de migrations, cópia criptografada fora da VPS e ensaio periódico de restore isolado. Retenção, destino externo, chaves de recuperação, agenda e alertas dependem da operação escolhida; nada foi agendado, enviado ou apagado automaticamente neste incremento. Não afirmar RPO/RTO garantidos sem medir e monitorar a rotina real.

Volume mysql-data preserva dados entre recriações, mas não é backup contra perda da VPS. .env, credenciais e certificados não estão no dump: precisam de recuperação segura separada. Rollback de imagem não desfaz migrations nem perda de dados. Toda promoção de restore/reversão precisa de procedimento próprio, pausa de escritas quando necessária e verificação prévia.

Veja [plano VPS](vps-plan.md). Próximas informações necessárias para publicar: servidor/destino, domínio e mecanismo seguro de acesso; sem esses dados não houve conexão remota nem alteração DNS.
