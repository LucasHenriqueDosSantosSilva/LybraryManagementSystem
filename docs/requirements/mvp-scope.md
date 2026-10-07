# Etapa 2 — Escopo do MVP

A análise inicial foi aprovada para continuidade. Este documento fixa uma referência de escopo para orientar casos de uso, modelagem e critérios de aceite. Funcionalidades descritas aqui ainda não estão implementadas.

## Objetivo e usuário

Um bibliotecário gerencia o catálogo e a circulação de uma única biblioteca. O resultado mínimo útil é registrar um leitor e um exemplar, realizar um empréstimo e sua devolução, preservando o histórico. Leitores não acessam o sistema no MVP local.

## O que entra

| Área | Entrega | Limite |
| --- | --- | --- |
| Leitores | Cadastro, consulta, edição, ativação/desativação e histórico | Matrícula única; e-mail opcional, sem unicidade, pois leitores podem compartilhar contato |
| Autores | Cadastro, consulta, edição e exclusão sem vínculos | Homônimos são permitidos |
| Categorias | Cadastro, consulta, edição e exclusão sem vínculos | Uma categoria por livro; nome único segundo a collation escolhida e documentada |
| Livros | Cadastro, consulta, edição e exclusão quando possível | Uma edição por registro, ISBN obrigatório e único; ao menos um autor |
| Exemplares | Cadastro, consulta, edição do código patrimonial e retirada de circulação | Código único; livro associado não pode ser trocado após cadastro |
| Empréstimos | Registro, consulta, filtros e devolução | Um exemplar por empréstimo; sem renovação, reserva ou multa |
| Dashboard | Totais e atividade recente | Distinguir títulos, exemplares e exemplares disponíveis; ranking por número de empréstimos registrados |
| Interface | Fluxos de cadastro e circulação, busca, paginação e feedback | Dark mode responsivo, navegação por teclado e redução de movimento |
| Qualidade | Migrations, validação, testes críticos e documentação | MySQL real para verificar integridade e concorrência |

Excluir leitor sem histórico será permitido. Leitor com histórico será desativado. Livro só poderá ser removido quando não houver exemplares associados; exemplar só poderá ser removido quando não houver histórico. Autor e categoria vinculados não serão removidos. Essas restrições devem retornar mensagem clara, sem cascata que apague histórico.

Retirar exemplar de circulação exige ausência de empréstimo ativo. Não haverá reativação de exemplar retirado no MVP; corrigir essa decisão depois será uma evolução explícita. Disponibilidade é derivada: exemplar em circulação e sem empréstimo ativo.

## O que fica para depois

Reservas, renovação, multas, envio de e-mail, importação de catálogo, leitura de código de barras, múltiplas bibliotecas, aplicativo mobile, auditoria completa, permissões com vários perfis e relatórios exportáveis. Não fazem parte da demonstração inicial.

Autenticação fica fora da primeira execução local isolada. Antes de exposição pública com operações de escrita, autenticação e autorização de bibliotecários entram como requisito de publicação. Cadastro de leitor não equivale a cadastro de credenciais.

## Decisões de domínio

- Prazo inicial: 14 dias corridos, definido pelo servidor e configurável; não é escolhido livremente pelo cliente no MVP.
- Empréstimo e devolução usam a data atual da biblioteca; datas retroativas ficam fora do escopo.
- Atraso começa no dia seguinte ao vencimento. Devolução no vencimento não é atrasada.
- A devolução de leitor desativado continua permitida.
- Não há limite de empréstimos por leitor nesta versão.
- Um leitor pode emprestar exemplares diferentes do mesmo título. O impedimento obrigatório se refere ao mesmo exemplar físico.
- Livro pode existir sem exemplares, mas não sem autor ou categoria.
- ISBN aceita formato ISBN-10 ou ISBN-13 válido, é normalizado sem espaços/hífens e preservado como texto. Equivalência entre ISBN-10 e ISBN-13 deve ser tratada pela normalização canônica para impedir duplicação da mesma edição.
- Mudança de ISBN de livro com histórico será bloqueada; corrigir a identificação exige uma decisão específica, sem reatribuir silenciosamente empréstimos a outra edição.
- Ranking é por título, inclui empréstimos devolvidos e ativos e usa o identificador do livro como desempate estável. Atividade recente é ordenada por data e identificador decrescente.

Essas escolhas serão refletidas nos contratos e no dicionário de dados. O fuso da biblioteca será configurado na implementação, sem depender implicitamente do fuso da máquina.

## Critérios de aceite

| ID | Cenário verificável | Resultado esperado |
| --- | --- | --- |
| AC01 | Cadastrar leitor com matrícula existente | Rejeitar duplicidade sem criar registro |
| AC02 | Cadastrar livro com categoria inexistente ou sem autores | Rejeitar cadastro e não persistir dados parciais |
| AC03 | Cadastrar edição com ISBN equivalente já existente | Rejeitar após normalização |
| AC04 | Cadastrar dois exemplares de um livro | Mostrar dois exemplares disponíveis e um título |
| AC05 | Emprestar um exemplar a leitor ativo | Criar empréstimo com vencimento em 14 dias e reduzir disponibilidade derivada |
| AC06 | Emprestar a leitor inativo | Rejeitar e manter disponibilidade |
| AC07 | Enviar dois empréstimos concorrentes do mesmo exemplar | Apenas um sucesso; apenas um empréstimo ativo persistido |
| AC08 | Emprestar exemplar retirado de circulação | Rejeitar sem criar empréstimo |
| AC09 | Devolver empréstimo ativo | Registrar data, liberar exemplar e preservar histórico |
| AC10 | Repetir devolução | Informar conflito sem alterar a primeira devolução |
| AC11 | Desativar leitor com empréstimo ativo e devolver | Impedir novos empréstimos, permitir devolução |
| AC12 | Consultar no vencimento e no dia seguinte | Classificar atraso apenas no dia seguinte, se ainda ativo |
| AC13 | Excluir registro vinculado ou com histórico | Rejeitar sem perder vínculos ou histórico |
| AC14 | Acessar listas vazias ou API indisponível | Exibir estado vazio ou erro compreensível, sem apresentar sucesso falso |
| AC15 | Submeter formulário duas vezes enquanto pendente | Bloquear repetição na interface; backend ainda garante integridade |
| AC16 | Usar interface a 320px e por teclado | Acessar ações essenciais, labels e foco; tabelas podem usar scroll horizontal |
| AC17 | Recriar banco limpo pelas migrations | Obter schema válido e executar fluxo de circulação completo |

Os critérios são compromissos de verificação futura, não testes executados. AC07 deve ser verificado contra MySQL, não apenas com mocks.

## Entregas incrementais

1. Casos de uso, modelagem, normalização e decisões de arquitetura.
2. Baseline do backend: catálogo, leitores, exemplares e circulação; execução local com MySQL e validações críticas.
3. Registro dos problemas de projeto e testes de caracterização; refatorações que preservem o comportamento.
4. Consolidação dos contratos REST e frontend integrado.
5. Compose completo, documentação de execução, medição antes/depois e preparação da apresentação.
6. Avaliação de deploy, acrescentando proteção de acesso antes da publicação pública.

A versão inicial poderá ter responsabilidades pouco bem distribuídas para análise acadêmica. Não terá defeitos deliberados de segurança, perda de dados ou concorrência. A mesma lista de critérios permitirá comparar as versões sem mudar as regras durante a refatoração.

## O que entender nesta etapa

Escopo define a fronteira do produto; requisito descreve comportamento; critério de aceite mostra como verificar esse comportamento. A separação entre livro e exemplar remove a necessidade de sincronizar um contador de disponibilidade com os empréstimos. Em Banco de Dados isso prepara cardinalidades, restrições e normalização; em Análise e Projeto prepara casos de uso e responsabilidades.

Questões para defender: por que excluir reservas? Porque exigem fila e política adicional, sem serem necessárias ao fluxo principal. Por que um exemplar por empréstimo? Simplifica devoluções independentes e histórico; múltiplos itens exigiriam outra modelagem. Por que não armazenar quantidade disponível? Para evitar fontes concorrentes de verdade. Por que testar concorrência? Porque validar disponibilidade antes de inserir, sem proteção transacional, não impede duas requisições simultâneas.

Próxima etapa: detalhar os casos de uso com pré-condições, fluxo principal, alternativas e pós-condições, usando este escopo como referência.
