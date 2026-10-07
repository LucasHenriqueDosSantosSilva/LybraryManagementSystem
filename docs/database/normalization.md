# Etapa 5 — Dependências funcionais e normalização

Análise do [modelo inicial](initial-model.md), considerando as regras do MVP. Esta é uma avaliação lógica: não há schema criado ou testes de banco executados. As conclusões dependem das regras explicitadas; uma amostra de dados sem duplicatas não prova uma dependência funcional.

## Conceitos usados

`X → Y` significa que duas tuplas com o mesmo X têm necessariamente o mesmo Y, em todo estado válido da relação. Chave candidata é um conjunto mínimo que determina todos os atributos; superchave também determina todos, mas pode conter atributos desnecessários. Atributo primo pertence a alguma chave candidata.

- **1FN:** cada posição contém um valor do domínio, sem grupos repetidos ou listas de autores.
- **2FN:** 1FN e nenhum atributo não primo depende de parte própria de uma chave candidata composta.
- **3FN:** para toda dependência funcional não trivial `X → A`, X é superchave ou A é atributo primo.
- **BCNF:** em toda dependência funcional não trivial, o determinante é superchave.

NULL em SQL não é um valor comum do modelo relacional clássico. Usaremos campos opcionais somente quando necessários. Nenhuma chave candidata proposta admite NULL; UNIQUE de um campo nullable não seria suficiente para afirmar que ele é uma chave candidata.

## Relações, chaves e dependências

A notação usa os nomes reais propostos para as colunas. `id → demais` representa um conjunto de dependências com uma coluna por lado direito, não uma nova regra de negócio.

| Relação | Chaves candidatas | Dependências relevantes |
| --- | --- | --- |
| readers(id, registration_number, name, email, active) | {id}; {registration_number} | id → demais; registration_number → id |
| authors(id, name) | {id} | id → name |
| categories(id, name) | {id}; {name} | id → name; name → id |
| books(id, isbn, title, description, publication_year, category_id) | {id}; {isbn} | id → demais; isbn → id |
| book_authors(book_id, author_id) | {book_id, author_id} | Nenhuma dependência não trivial adicional |
| book_copies(id, inventory_code, book_id, circulation_status) | {id}; {inventory_code} | id → demais; inventory_code → id |
| loans(id, reader_id, copy_id, loan_date, due_date, returned_date) | {id} | id → demais |

As dependências das chaves comerciais também determinam todos os atributos por transitividade com id. A comparação de `categories.name` segue a collation definida: diferenças ignoradas pela collation não criam categorias distintas. A normalização de ISBN faz a unicidade se aplicar à representação canônica.

Não assumir: `name → id` em autores/leitores; `email → reader_id`; `title → book_id`; `book_id → author_id`; `copy_id → loan_id`; ou `(copy_id, loan_date) → loan_id`. Homônimos, contatos compartilhados, vários autores e sucessivos empréstimos são permitidos; um exemplar pode ser devolvido e emprestado novamente no mesmo dia.

O limite de um empréstimo ativo por exemplar é uma restrição condicional de um subconjunto das linhas, não a dependência `copy_id → id` de toda a relação loans. Desigualdades de datas e participação mínima de autor também não são dependências funcionais.

## Verificação por relação

| Relação | 1FN | 2FN | 3FN e BCNF sob as regras conhecidas |
| --- | --- | --- | --- |
| readers | Nome, matrícula e contato são valores únicos; nenhum conjunto de contatos | Chaves candidatas simples, sem dependência parcial | Determinantes não triviais conhecidos são chaves; nome/email não determinam outros campos |
| authors | Um nome por linha | Chave simples | Apenas id determina name |
| categories | Um nome por linha | Chaves simples | id e name são chaves |
| books | Uma edição, um ISBN e uma categoria por linha; autores em outra relação | Chaves simples | id e isbn são chaves; não contém nome de categoria ou dados de autor |
| book_authors | Uma associação por linha | Chave composta, mas nenhum atributo não primo | Não há dependências não triviais com determinante menor que a chave |
| book_copies | Uma unidade física por linha | Chaves simples | id e inventory_code são chaves; não contém título/ISBN do livro |
| loans | Um evento de empréstimo por linha, um leitor e um exemplar | Chave simples | Apenas id determina atributos do evento; não repete nome do leitor ou livro do exemplar |

Todas atendem à meta de 3FN e, com o conjunto de dependências declarado, também à BCNF. Não fazemos afirmação automática de 4FN/5FN; não há necessidade de forçar decomposições adicionais para o escopo. Adicionar um id técnico não normaliza por si só uma tabela: dependências entre atributos não-chave continuariam existindo.

## Como as redundâncias seriam introduzidas

### Dados de categoria dentro de books

Se incluíssemos `category_name` em books, teríamos `category_id → category_name`, embora category_id não seja superchave de books e category_name não seja primo. Seria violação de 3FN.

Anomalias: renomear categoria exigiria atualizar todos os livros; inserir uma categoria sem livros seria difícil; excluir o último livro poderia eliminar a única cópia do nome. A decomposição em categories e books evita esses problemas.

### Dados do livro dentro de loans

Se loans tivesse `book_id` e `book_title`, ocorreria `copy_id → book_id` e `book_id → book_title`. Esses determinantes não identificam um empréstimo. O vínculo passa por book_copies e books, sem duplicar esses dados em loans.

### Nome de autor em book_authors

Se a tabela associativa tivesse author_name, haveria `author_id → author_name`: dependência de parte da chave `(book_id, author_id)`, violando 2FN. O nome pertence a authors. Lista de autores em books não resolve o problema: prejudica atomicidade, consulta e integridade referencial.

## Decomposição sem perda e preservação

Para uma decomposição binária guiada por dependências funcionais, a interseção das duas relações determinar uma delas é um critério de junção sem perda. No exemplo de categoria duplicada, a interseção é category_id, chave de categories: juntar por essa chave recompõe o registro sem gerar associações espúrias. No exemplo de exemplar duplicado em loans, copy_id determina o registro de book_copies.

As dependências principais permanecem verificáveis localmente pelas PKs e UNIQUEs de cada relação. FKs garantem referências existentes. A associação N:M é representada por pares explícitos, sem inferir que todo autor de um livro escreveu todos os livros do catálogo.

Isso não autoriza chamar qualquer grande JOIN do sistema de decomposição sem perda. BookAuthor, por exemplo, produz várias linhas por livro e pode multiplicar contagens. Dashboard deve agregar por entidade ou usar consultas/subconsultas apropriadas, sem contar linhas de um JOIN N:M como se fossem livros ou empréstimos únicos.

Regras entre tabelas, como livro ter ao menos um autor, não ficam automaticamente garantidas apenas porque o modelo é normalizado. Elas continuam exigindo validação transacional.

## Dados derivados e fatos históricos

Disponibilidade exige consultar circulação e empréstimo ativo; atraso exige uma data de referência. Não serão armazenados como colunas mantidas manualmente. Quantidades e ranking também são resultados de agregações.

Normalização por dependências funcionais não proíbe todo dado derivado ou todo cache: o problema aqui é manter várias representações mutáveis do mesmo fato sem necessidade. O empréstimo guarda `due_date` porque o prazo foi estabelecido naquele evento. Não existe a dependência global `loan_date → due_date`: dois empréstimos da mesma data podem ter sido criados sob configurações de prazo distintas. A data do empréstimo sozinha não determina o vencimento no domínio.

`returned_date` representa um evento real. `active` do leitor e `circulation_status` do exemplar são decisões independentes, não estados derivados de empréstimos. Uma biblioteca pode desativar um leitor sem empréstimos ou retirar um exemplar disponível.

### Coluna técnica gerada para unicidade condicional

A proposta física `active_copy_id = CASE WHEN returned_date IS NULL THEN copy_id ELSE NULL END` depende de copy_id e returned_date. No modelo lógico, esse campo não é um fato independente; no schema físico, será derivado automaticamente e usado para uma constraint.

Se tratado como atributo comum de loans, seu determinante não é superchave e o atributo não é primo: não devemos afirmar que essa extensão física mantém literalmente a mesma prova de 3FN/BCNF. É uma redundância técnica controlada pelo banco, sem atualização manual, para viabilizar a unicidade condicional em MySQL. A decisão e o funcionamento serão verificados na implementação.

UNIQUE em active_copy_id também não o transforma em chave candidata de loans: múltiplos empréstimos devolvidos terão NULL. Não incluir essa coluna em DTOs ou permitir escrita manual.

## Resultado da revisão

Manter as sete relações propostas; não adicionar tabelas apenas para aumentar a contagem. Manter catálogo separado da circulação, PK composta da associação e atributos comerciais únicos. Não incluir contadores de disponibilidade, autor concatenado, nome de categoria em books ou book_id em loans.

A revisão confirma a meta lógica de 3FN, com BCNF sob as dependências conhecidas. Não comprova que migrations, ORM ou consultas futuras estarão corretos. Antes de considerar o banco validado, será necessário executar DDL no MySQL, testar restrições, concorrência e rollback e verificar resultados das agregações.

## O que entender e defender

Banco de Dados: distinguir dependência funcional de FK, restrição condicional e CHECK; justificar decomposição; reconhecer anomalias de atualização, inserção e exclusão. Análise e Projeto: separar conceitos com responsabilidades distintas, sem presumir que tabelas normalizadas garantem serviços coesos.

Questões de apresentação:

- **Por que id não resolve toda redundância?** Porque atributos não-chave podem continuar determinando outros atributos não-chave.
- **Por que author_id sozinho não identifica book_authors?** Porque um autor participa de vários livros; o par identifica a associação.
- **Por que due_date permanece?** Porque registra o prazo contratado e não deve mudar junto com configurações futuras.
- **Toda regra cabe em dependência funcional?** Não: datas, estado ativo, histórico e concorrência têm outras formas de integridade.
- **A coluna gerada contradiz a análise?** Não, desde que se diferencie modelo lógico normalizado e mecanismo físico redundante para uma garantia específica.

Próxima etapa: definir a arquitetura e registrar decisões de módulos, camadas, contratos e transações, preparando uma baseline funcional que possa ser analisada e refatorada depois.
