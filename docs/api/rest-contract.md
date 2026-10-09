# Etapa 12 — Contrato REST do backend

Contrato implementado para desenvolvimento local. Base `/api`, JSON em requisições e respostas; erros em `application/problem+json`. Sem autenticação nesta versão. As rotas abaixo orientam a próxima etapa de frontend.

## Recursos e métodos

| Caminho | Métodos e resultado |
| --- | --- |
| `/categories`, `/authors`, `/books`, `/readers`, `/copies` | GET lista paginada; POST cria (201 e Location relativa para o recurso) |
| Recursos anteriores `/{id}` | GET consulta (200); PUT substitui campos editáveis (200); DELETE remove quando permitido (204 sem corpo) |
| `/readers/{id}/status` | PATCH com `{"active":false}` ou true (200) |
| `/copies/{id}/withdrawal` | POST sem corpo; retira exemplar livre de circulação (200) |
| `/loans` | GET lista paginada; POST com readerId e copyId cria (201 e Location) |
| `/loans/{id}` | GET consulta (200); sem edição ou exclusão de histórico |
| `/loans/{id}/return` | POST sem corpo; registra devolução (200); repetição conflita (409) |
| `/dashboard` | GET resumo (200); limit padrão 5, de 1 a 20 |

Os caminhos na tabela são relativos a `/api`. Não enviar corpo a GET. PUT não altera status de leitor nem estado de circulação; use as ações correspondentes. Retirada não tem reativação no MVP.

## Entradas de cadastro e edição

| Recurso | Campos |
| --- | --- |
| Categoria | name obrigatório, até 100 caracteres |
| Autor | name obrigatório, até 150; homônimos permitidos |
| Livro | isbn obrigatório até 30 na entrada, title obrigatório até 250, description opcional até 5000, publicationYear obrigatório 1–9999, categoryId positivo, authorIds não vazio com ids positivos |
| Leitor | registrationNumber obrigatório até 30, name obrigatório até 150, email opcional válido até 254 |
| Exemplar | inventoryCode obrigatório até 40, bookId positivo |
| Empréstimo | readerId e copyId positivos; datas definidas pelo servidor |

Matrícula e código patrimonial aceitam letras ASCII, números, ponto, hífen e sublinhado, com espaços externos normalizados. ISBN aceita hífen ASCII e whitespace Unicode; valida checksum e retorna ISBN-13 canônico. Categoria, ISBN, matrícula e patrimônio têm unicidade; autor não. Não enviar ids/datas/status derivados como campos editáveis.

## Respostas e filtros

Listas retornam `{"content":[],"page":{"size":20,"number":0,"totalElements":0,"totalPages":0}}`. page começa em zero, padrão 0; size padrão 20, permitido 1–100. Ordens são definidas pelo backend; não há parâmetro público sort. Páginas vazias são 200. Não inferir última página somente pelo tamanho de content; usar metadados.

Livros aceitam title, author, categoryId e isbn; title/author são busca textual, com percentagem e sublinhado tratados como texto literal. Livro retorna id, isbn, title, description, publicationYear, categoryId e authors (objetos id/name). Exemplar retorna id, inventoryCode, bookId, circulationStatus e available; disponibilidade é derivada.

Empréstimos aceitam readerId e status (ACTIVE, RETURNED, OVERDUE). ACTIVE no filtro significa **todos os não devolvidos**, inclusive atrasados; o status de cada resposta distingue ACTIVE e OVERDUE. Empréstimo retorna id, readerId, copyId, bookId, loanDate, dueDate, returnedDate e status. Datas são ISO de calendário no fuso da biblioteca; returnedDate é null até devolução. Atraso começa no dia após dueDate. Empréstimos são ordenados por loanDate e id decrescentes.

Dashboard retorna totalBooks, totalCopies, totalReaders, availableCopies, activeLoans, overdueLoans, mostBorrowed (bookId/title/loanCount) e recentLoans (mesmo contrato do empréstimo). ActiveLoans inclui atrasados; totais de leitores incluem inativos e de exemplares incluem retirados.

## Erros que o frontend deve tratar

| HTTP | Significado |
| --- | --- |
| 400 | Campos inválidos, JSON ilegível, identificador não numérico, parâmetros ou filtros inválidos |
| 404 | Recurso referenciado não encontrado |
| 405 | Método não permitido; header Allow informa métodos aceitos |
| 409 | Unicidade, vínculos/histórico ou estado incompatível com operação |
| 415 | Content-Type não suportado |
| 500 | Falha interna; não expor SQL ou stack trace ao usuário |

ProblemDetail contém type, title, status, detail e instance. Para validação de campos, errors contém objetos field/message. Exemplo:

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"Revise os campos informados.","instance":"/api/categories","errors":[{"field":"name","message":"Informe o nome."}]}
```

Mensagens de framework podem variar e não constituem códigos de negócio. O cliente deve decidir pelo status e pelos campos, sem comparar mensagens traduzidas. Campos opcionais de erro não devem impedir renderização. Não repetir automaticamente POST de empréstimo: não há chave de idempotência. Após resposta ambígua por falha de conexão, consulte o estado antes de tentar novamente.

## Verificação desta entrega

`python3 /workspace/library-runtime/run.py test package`: **79 testes**, zero falhas, erros ou ignorados, build e empacotamento aprovados. RestContractTest acrescenta dez execuções: seis recursos com id malformado, JSON inválido, validação por campo, 405 com Allow e criação/consulta/exclusão com Location e 204. Os demais testes preservam circulação, integridade e contratos existentes. Esta entrega acrescenta regressões, sem novo ciclo TDD.

Não foi necessário alterar controllers/advice. O contrato é documentado em Markdown; ainda não há especificação OpenAPI gerada, SDK ou testes de frontend. O próximo passo é implementar a interface usando esses contratos. CORS e autenticação devem acompanhar a estratégia de integração/deploy, sem pressupor acesso público agora.
