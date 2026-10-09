# Etapa 10 — Incremento 8: organização por domínio (P06)

O backend passa de pacotes globais por camada para agrupamentos por responsabilidade. Cada domínio conserva suas camadas, e o Spring continua descobrindo os componentes abaixo de `LibraryApplication`.

| Pacote | Responsabilidade e camadas |
| --- | --- |
| `catalog` | Categorias, autores, livros e exemplares; controller, dto, entity, repository, service e domain (Isbn) |
| `readers` | Leitores; controller, dto, entity, repository e service |
| `loans` | Empréstimos e devoluções; controller, dto, entity, repository, service e domain (LoanStatus) |
| `reporting` | Dashboard; controller, dto, repository e service |
| `api`, `exception`, `configuration` | Paginação, tradução HTTP, erros de aplicação e configuração de tempo |

Imports e testes foram ajustados; ApiExceptionHandlerTest acompanha o pacote `api`. A formatação Google Java Format foi aplicada aos arquivos Java de produção. Nomes simples das entidades JPA, tabelas, migrations, rotas, DTOs, SQL, transações e bloqueios foram preservados.

## Limites da organização

São grupos lógicos, sem isolamento imposto por ferramenta de módulos. Catálogo e leitores consultam LoanRepository para ocupação ou histórico; empréstimos usam entidades e repositórios de catálogo e leitores; relatórios reutilizam LoanResponse e LoanStatus. Há dependências em ambas as direções entre alguns grupos. A mudança facilita localizar funcionalidades, mas não demonstra redução de acoplamento nem um grafo acíclico de módulos. Interfaces adicionais ou eventos exigiriam um problema concreto que justificasse sua introdução.

## Validação

`python3 /workspace/library-runtime/run.py clean test package` passou: **62 testes, zero falhas, erros ou ignorados**, além do empacotamento. O helper seleciona `library_test` quando `test` aparece em qualquer posição dos argumentos, inclusive depois de `clean`. O build limpo evita depender de classes antigas após mover pacotes.

Após reiniciar a aplicação, GET das seis listagens e do dashboard respondeu 200 com os contratos esperados; `/api/books?size=0` respondeu 400 e “Paginação inválida.”. A suíte existente verifica operações, integridade e concorrência contra MySQL; não foram adicionados testes que apenas reproduzem a estrutura dos diretórios.

## Comparação ao concluir a etapa

| Evidência | Baseline | Depois |
| --- | ---: | ---: |
| Testes executados | 43 | 62 |
| Arquivos Java de produção | 44 | 50 |
| Linhas físicas de produção | 778 | 1842 |
| Linhas não vazias de produção | 778 | 1575 |
| Arquivos Java de teste | 6 | 11 |
| Services com import HTTP identificado pelo script | 6 | 0 |

As contagens textuais estão em `../before-refactoring/source-metrics.json` e [source-metrics.json](source-metrics.json), com revisões fixadas. O aumento de linhas inclui expansão da formatação e extração de responsabilidades; não é medida de complexidade, cobertura ou produtividade. Não foram calculados índices de coesão ou acoplamento.

Os experimentos anteriores de catálogo e exemplares registraram 3/4/5 SELECTs antes e 3/3/3 depois, para páginas de 1/2/3 itens. Seus arquivos de observação continuam como evidência dos incrementos 4 e 5; não são uma nova medição deste incremento nem um benchmark de latência.

Os oito problemas selecionados na etapa 9 receberam mudanças verificáveis. A próxima etapa aprofundará a estratégia de testes e demonstrará um ciclo TDD com uma regra concreta. Os testes já existentes não constituem, por si, evidência de TDD. Frontend, autenticação e implantação pública continuam fora desta entrega de backend.
