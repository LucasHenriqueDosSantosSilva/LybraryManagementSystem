# Etapa 3 — Casos de uso

Especificação proposta, baseada no [escopo do MVP](mvp-scope.md). Nenhuma operação está implementada nesta etapa. Ator principal: bibliotecário; leitores são participantes do domínio e não atores de acesso. Na primeira execução local, o ator descreve o papel humano, não uma permissão já autenticada.

## Convenções

Cada alteração deve ser confirmada pelo servidor antes de a interface informar sucesso. Falha de validação ou conflito não pode deixar alterações parciais. Recurso removido ou inexistente gera mensagem específica. Consultas sem resultados são estados vazios válidos; falhas de conexão são erros, não listas vazias.

Não assumimos que pré-condições verificadas na interface continuem verdadeiras no servidor. As condições relevantes são revalidadas na transação. Rotas HTTP e nomes de classes serão definidos na etapa de arquitetura/API.

## UC01 — Manter leitores

**Objetivo:** identificar quem pode tomar exemplares emprestados.

**Pré-condições:** para editar, excluir ou alterar status, leitor existente. Cadastro não exige registros anteriores.

**Fluxo principal (cadastro):**
1. Bibliotecário informa nome, matrícula e e-mail opcional.
2. Sistema valida campos e verifica unicidade da matrícula.
3. Sistema registra leitor ativo e retorna identificação e dados persistidos.
4. Interface informa sucesso e permite consultar o leitor.

**Alternativas:** edição mantém unicidade e validações; e-mail informado deve ter formato válido e pode ser compartilhado. Desativação impede novos empréstimos, preservando histórico e permitindo devoluções. Ativação volta a permitir empréstimos. Exclusão física exige ausência de qualquer empréstimo, inclusive devolvido; caso contrário, orientar desativação. Dados inválidos ou matrícula duplicada rejeitam a operação.

**Pós-condições:** uma única alteração persistida; empréstimos existentes nunca apagados. **Rastreabilidade:** RF01; AC01, AC11, AC13.

## UC02 — Manter autores e categorias

**Objetivo:** organizar a identificação e classificação do catálogo.

**Pré-condições:** registro existente para edição/exclusão.

**Fluxo principal:** bibliotecário escolhe autor ou categoria, informa nome, sistema valida, persiste e confirma. Na consulta, sistema lista registros com busca e paginação.

**Alternativas:** nomes de autores podem se repetir; nome de categoria duplicado, conforme collation documentada na modelagem, é rejeitado. Edição de categoria também verifica unicidade. Exclusão com livros vinculados é rejeitada; bibliotecário pode revisar associações em uma operação separada. Cadastro vazio é rejeitado.

**Pós-condições:** catálogo auxiliar consistente; nenhuma exclusão em cascata de livros. **Rastreabilidade:** RF03; AC13.

## UC03 — Manter livros

**Objetivo:** registrar uma edição bibliográfica, independentemente de seus exemplares físicos.

**Pré-condições:** ao menos um autor e uma categoria existentes para cadastro; livro existente para edição/exclusão.

**Fluxo principal:**
1. Bibliotecário informa ISBN, título, ano, categoria, um ou mais autores e descrição opcional.
2. Sistema valida ISBN e normaliza para ISBN-13 canônico, convertendo ISBN-10 válido e recalculando dígito verificador.
3. Sistema valida unicidade, campos e existência de todos os vínculos.
4. Sistema persiste livro e associações de autores atomicamente.
5. Interface mostra o livro; zero exemplares é uma situação válida.

**Alternativas:** ISBN inválido ou equivalente ao já cadastrado é rejeitado. Categoria/autor removido entre seleção e envio causa rejeição sem cadastro parcial. Edição nunca deixa livro sem autor/categoria. Mudança de ISBN é bloqueada se qualquer exemplar do livro tiver histórico. Exclusão exige ausência de exemplares, mesmo retirados; vínculos BookAuthor podem ser removidos junto com o livro, sem remover autores.

**Pós-condições:** edição identificada de modo único, com vínculos válidos. **Rastreabilidade:** RF04; AC02, AC03, AC13.

## UC04 — Manter exemplares

**Objetivo:** identificar e controlar cada unidade física.

**Pré-condições:** livro existente para cadastro; exemplar existente para demais operações.

**Fluxo principal:** bibliotecário seleciona livro, informa código patrimonial; sistema valida unicidade e persiste exemplar em circulação. Consulta apresenta livro, código, situação de circulação e disponibilidade derivada.

**Alternativas:** código duplicado ou livro inexistente rejeitam cadastro. Edição do código exige unicidade e preserva id e histórico; não permite trocar o livro associado. Retirada de circulação exige ausência de empréstimo ativo. Exemplar com histórico não pode ser excluído; sem histórico, exclusão é permitida. Não há reativação de exemplar retirado neste MVP.

**Pós-condições:** disponibilidade calculada a partir de circulação e empréstimo ativo, sem contador independente. **Rastreabilidade:** RF05; AC04, AC08, AC13.

## UC05 — Realizar empréstimo

**Objetivo:** entregar um exemplar disponível a um leitor ativo.

**Pré-condições:** leitor e exemplar cadastrados; leitor ativo; exemplar em circulação e sem empréstimo ativo. Todas devem ser verificadas pelo servidor.

**Fluxo principal:**
1. Bibliotecário seleciona leitor e exemplar e solicita empréstimo.
2. Sistema verifica existência e condições atuais em operação transacional.
3. Sistema obtém a data atual no fuso da biblioteca e calcula vencimento usando o prazo configurado (inicialmente 14 dias corridos).
4. Sistema registra empréstimo ativo, com devolução ainda ausente.
5. Sistema confirma a persistência e retorna identificação e datas.
6. Interface informa sucesso e atualiza disponibilidade e listas.

**Alternativas:** leitor inativo, exemplar retirado ou já emprestado geram rejeição sem novo empréstimo. Recurso inexistente é informado. Em requisições concorrentes do mesmo exemplar, somente uma pode persistir; a outra recebe conflito. Retirada de circulação e empréstimo concorrentes devem preservar a regra: um exemplar retirado não recebe novo empréstimo. Desativação concorrente deve ser serializada com empréstimo: se empréstimo ocorrer antes, ele permanece válido; se desativação ocorrer antes, o empréstimo é rejeitado.

**Falha de resposta:** se o banco confirmar mas a resposta se perder, a interface não pode afirmar que houve rollback. Deve informar resultado incerto e orientar consulta dos empréstimos antes de repetir. Idempotência por chave é uma possível evolução, não uma garantia implementada. Repetir enquanto o primeiro empréstimo segue ativo não pode criar outro para o mesmo exemplar.

**Pós-condições de sucesso:** exatamente um novo empréstimo ativo e exemplar indisponível. **De rejeição:** nenhum novo empréstimo dessa tentativa. **Rastreabilidade:** RF06; AC05, AC06, AC07, AC08, AC15.

## UC06 — Registrar devolução

**Objetivo:** encerrar um empréstimo e tornar o exemplar novamente disponível.

**Pré-condições:** empréstimo existente e ainda ativo. Leitor não precisa estar ativo.

**Fluxo principal:** bibliotecário localiza empréstimo e confirma devolução; sistema revalida estado, registra a data atual da biblioteca atomicamente e confirma; interface atualiza o histórico e a disponibilidade derivada.

**Alternativas:** empréstimo inexistente é informado. Empréstimo devolvido gera conflito, sem substituir a primeira data. Duas devoluções concorrentes resultam em uma alteração e um conflito. Falha de resposta exige consultar estado antes de nova tentativa. Data anterior à do empréstimo, inclusive causada por configuração ou relógio incorreto, é rejeitada.

**Pós-condições:** histórico preservado e exemplar liberado; status do leitor não muda. **Rastreabilidade:** RF07; AC09, AC10, AC11, AC15.

## UC07 — Consultar catálogo

**Objetivo:** localizar livros e verificar seus exemplares.

**Pré-condições:** nenhuma além de acesso ao sistema local.

**Fluxo principal:** bibliotecário informa busca por título, ISBN, autor e/ou categoria; sistema valida paginação, consulta e retorna resultados com ordem estável; seleção do livro mostra detalhes e exemplares com disponibilidade.

**Alternativas:** nenhum resultado mostra estado vazio; filtros inválidos geram orientação; falha do servidor mostra erro e opção de tentar novamente. ISBN em formato de apresentação deve ser normalizado para busca exata. Listas grandes não retornam todos os registros indiscriminadamente.

**Pós-condições:** nenhuma alteração de dados. Disponibilidade exibida é a observada na consulta, não uma reserva. **Rastreabilidade:** RF09; AC04, AC14, AC16.

## UC08 — Consultar empréstimos e histórico

**Objetivo:** acompanhar circulação atual e passada.

**Pré-condições:** leitor existente para histórico individual; consulta geral não depende de leitor selecionado.

**Fluxo principal:** bibliotecário escolhe consulta geral ou leitor, aplica filtros e paginação; sistema retorna empréstimos com leitor, livro, exemplar e datas. Filtros distinguem ativo, devolvido e atrasado.

**Alternativas:** histórico vazio é válido. Atrasado é um subconjunto dos ativos: `returnedDate` ausente e `dueDate` anterior à data atual. No dia do vencimento, empréstimo segue ativo e não atrasado. Empréstimo devolvido não aparece como atraso atual, mesmo se devolvido depois do prazo. Leitor excluído/inexistente gera erro no histórico individual.

**Pós-condições:** dados preservados. **Rastreabilidade:** RF02, RF08; AC12, AC14.

## UC09 — Acompanhar dashboard

**Objetivo:** resumir catálogo e circulação.

**Pré-condições:** nenhuma além de acesso ao sistema local.

**Fluxo principal:** bibliotecário abre dashboard; sistema consulta totais de livros (registros de edição), exemplares, leitores, exemplares disponíveis, empréstimos ativos e atrasados; retorna também ranking por título e empréstimos recentes; interface apresenta valores com rótulos inequívocos.

**Alternativas:** banco sem dados apresenta zeros e estados vazios; falha apresenta erro, não zeros falsos. Ranking inclui empréstimos ativos e devolvidos, ordena por contagem decrescente e id do livro crescente. Recentes ordena data do empréstimo e id decrescentes. Limites de quantidade são definidos no contrato posterior. A consulta pode refletir mudanças posteriores; não se presume atualização em tempo real.

**Pós-condições:** nenhuma alteração. **Rastreabilidade:** RF10; AC14.

## Requisitos transversais e cobertura

RF11 e AC14 aplicam-se às falhas dos casos de uso; AC15 aos formulários e operações pendentes; AC16 a todas as telas; AC17 à execução integrada de UC01–UC06 após migrations em banco limpo. Esses critérios não são casos de uso independentes do bibliotecário.

Garantias de concorrência e rollback exigem testes de integração MySQL. A modelagem e a arquitetura definirão constraints, ordem de bloqueios e transações; estes fluxos descrevem o comportamento exigido sem antecipar classes que ainda não existem.

## O que entender e defender

Um caso de uso descreve o objetivo e a interação do ator com o sistema, não uma sequência de métodos Java. Pré-condições não substituem validação transacional. Pós-condições ajudam a definir assertions nos testes e integridade no banco.

Os conceitos presentes são levantamento de requisitos, comportamento observável, fluxos alternativos e rastreabilidade em Análise e Projeto; atomicidade, integridade referencial e concorrência em Banco de Dados. Não aplicamos padrões de projeto apenas para desenhar os fluxos.

Perguntas para apresentação: por que leitor não é ator? Porque não opera a interface neste escopo. Por que atraso não é status persistido? Porque depende da data atual. Por que informar resultado incerto? Porque perder uma resposta não desfaz um commit. Como provar exclusividade? Com regra no banco, transação e teste concorrente, na implementação.

Próxima etapa: modelo inicial do banco, com atributos, chaves, cardinalidades e restrições que sustentem estes casos de uso.
