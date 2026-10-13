# Revisão de aceite antes do deploy

Matriz do [MVP](../requirements/mvp-scope.md), baseada no código/testes e nas execuções registradas. Não é aprovação institucional, auditoria externa ou conclusão de deploy. Fontes de critérios iniciais são históricas; esta tabela aponta evidência implementada e seus limites.

| Critério | Evidência e alcance |
| --- | --- |
| AC01 matrícula única | ReaderAndCopyIntegrationTest: duplicidade/rollback no MySQL |
| AC02 vínculos de livro | CatalogIntegrationTest: categoria/autor inexistentes, autores vazios e ausência de livro parcial |
| AC03 ISBN equivalente | IsbnTest e CatalogIntegrationTest; normalização/checksum e unicidade |
| AC04 exemplares versus edição | ReaderAndCopyIntegrationTest.twoCopiesReferenceOneBook e dashboard; não manter quantidade redundante |
| AC05 empréstimo e prazo | LoanIntegrationTest e cenário real Chromium; vencimento default 14 e indisponibilidade |
| AC06 leitor inativo | LoanIntegrationTest; UI não oferece leitores inativos para novo empréstimo |
| AC07 concorrência | LoanIntegrationTest com chamadas coordenadas, MySQL e UNIQUE; não é benchmark de carga |
| AC08 exemplar retirado | LoanIntegrationTest; UI bloqueia escolha/retirada incompatível, servidor mantém autoridade |
| AC09 devolução | LoanIntegrationTest, UI e fluxo Chromium; preserva histórico/libera exemplar |
| AC10 repetir devolução | LoanIntegrationTest e smoke HTTP; conflito sem sobrescrever data |
| AC11 inatividade e retorno | LoanIntegrationTest e browser: desativação preserva devolução |
| AC12 atraso | LoanStatusTest e Clock controlado em integração; dia do vencimento/seguinte/retorno |
| AC13 exclusão/histórico | Integrações de catálogo/circulação e conflitos exibidos em componentes |
| AC14 vazio versus falha | Componentes com erro/retry e listas vazias; não afirmar que toda falha de rede foi simulada em navegador |
| AC15 submissão pendente | Novo teste LoansPage bloqueia segundo clique com primeira resposta pendente; não demonstra todas as telas/dispositivos |
| AC16 320 px/teclado | Browser em sete rotas: sem overflow horizontal, headings e foco do link de salto; labels nativas. Auditoria/leitor de tela ainda pendentes |
| AC17 schema limpo | Flyway V1–V4 em MySQL novo e fluxo nos containers; restore comparou DDL/dados/constraints em schemas novos |

Os caminhos de teste estão em backend/src/test e frontend/src. Veja [browser](browser-integration.md), [backup/restore](../deployment/backup-restore.md) e [CI](ci.md) para comandos e distinções de ambiente.

## Limitações atuais

Segurança local implementada: sessão/CSRF/BCrypt, Secure cookie em HTTPS, limite por processo e rejeição de forwarding forjado para contorná-lo. Não há limiter distribuído, múltiplas contas, recuperação de senha, teste de carga, cobertura instrumentada, auditoria formal de acessibilidade ou restore promovido para uso de aplicação real.

Autenticação é verificada em testes dedicados e browser; os testes anteriores de negócio desabilitam segurança para isolar suas regras. Backup lógico foi restaurado com fixtures próprias, mas offsite, criptografia do arquivo, retenção/agendamento e RPO/RTO operacionais não foram implementados/medidos. TLS local foi verificado com trust explícito, não com certificado público/renovação ACME.

Preferência de VPS confirmada, porém servidor/domínio/acesso não informados. Nenhuma compra, SSH, DNS ou publicação executada. Licença depende de decisão do autor. A revisão acadêmica final deve ocorrer após resolver requisitos institucionais e, se exigido, a demonstração pública real.
