# Etapa 10 — Incremento 7: contrato de paginação

Resolve P04. A condição e os valores padrão se repetiam em CategoryController, AuthorController, BookController, ReaderController, BookCopyController e LoanController. P06 (organização por domínio) continua pendente; a tag da baseline permanece intacta.

## Antes → problema → depois

Cada controller validava `page < 0 || size < 1 || size > 100`, com os mesmos defaults e mensagem. Uma alteração no limite exigia sincronizar seis locais.

PageParameters, em api/, é um record imutável com page/size validados no construtor. Centraliza DEFAULT_PAGE="0", DEFAULT_SIZE="20" e MAX_SIZE=100. Os controllers mantêm parâmetros HTTP page/size, usam os defaults nas anotações e passam os valores do objeto validado aos services.

Entrada fora dos limites lança InvalidInputException, traduzida pelo advice existente para 400 e detalhe `Paginação inválida.`. Spring continua convertendo parâmetros numéricos; texto não numérico também retorna 400. Não alteramos o formato paginado de saída, filtros, ordenação, rotas ou assinaturas dos services.

Escolhemos um objeto pequeno com responsabilidade específica, sem controller-base genérico, reflection, annotations customizadas ou Utils. A abstração evita repetir uma regra comum; não tenta unificar CRUDs com regras diferentes.

## Validação

`python3 /workspace/library-runtime/run.py test package`: **62 testes**, zero falhas, erros ou ignorados; build e empacotamento passaram. São 10 testes unitários, 3 HTTP standalone e 49 de integração. Seis execuções parametrizadas novas, uma para cada recurso, verificam:

- default page=0 e size=20;
- size=1 e size=100 aceitos;
- size=0 e size=101 rejeitados com mensagem preservada;
- page=-1 rejeitada com mensagem preservada;
- parâmetro não numérico rejeitado.

Os 56 testes anteriores foram mantidos, incluindo paginação com múltiplos autores, disponibilidade e concorrência MySQL. A API foi reiniciada e os seis endpoints também foram consultados por HTTP real para defaults e erro de limite.

Não escrevemos testes que apenas repetem a expressão do record; verificamos o contrato através de endpoints reais. A mudança não acrescenta filtros, query ou coluna, e não reduz a contagem SQL. Dashboard usa limit para listas resumidas, com contrato próprio de 1 a 20, e não foi forçado a usar page/size.

## O que entender e defender

Uma regra compartilhada pode ter um responsável específico sem transformar toda a aplicação em um framework genérico. O record representa entrada validada; não é um mecanismo de persistência ou padrão de projeto obrigatório. Defaults em annotation precisam de constantes de compilação, por isso as constantes usadas por RequestParam são strings.

Em Análise e Projeto: coesão, eliminação de duplicação e proteção contra mudanças inconsistentes. Em Banco de Dados: parâmetros limitam o volume consultado, mas continuam sem substituir índices e análise de planos.

Próximo incremento: organizar componentes por domínio (P06), tornando dependências entre catálogo, leitores, circulação e relatórios explícitas, sem alterar schema ou dividir o deployment.
