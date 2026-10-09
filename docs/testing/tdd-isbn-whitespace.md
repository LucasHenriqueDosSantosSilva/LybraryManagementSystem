# Etapa 11 — Testes e TDD: ISBN copiado

## Comportamento acrescentado

Cadastro, edição e filtro de ISBN aceitam separadores de espaço Unicode, incluindo tabulação, espaço inseparável U+00A0 e espaço inseparável estreito U+202F, além do hífen ASCII já aceito. O valor persistido continua ISBN-13 canônico; checksum, prefixos 978/979 e unicidade continuam obrigatórios. O limite HTTP de 30 caracteres de entrada permanece. Não são convertidos algarismos de outros alfabetos nem removidos caracteres arbitrários.

Esta é uma extensão da normalização do catálogo, útil para texto copiado de documentos. Não altera regras de empréstimos, banco ou rotas.

## Ciclo executado em 2026-10-09

Base: commit `0fc792fbc712867b1991156f619a4bec2441abb2`. Os passos aconteceram nesta ordem na máquina de desenvolvimento; não foram fabricados commits intermediários.

1. **RED:** acrescentamos duas famílias parametrizadas em IsbnTest: três ISBNs válidos com os separadores novos e três com dígito final inválido. Executamos `python3 /workspace/library-runtime/run.py test -Dtest=IsbnTest`. Resultado: 9 testes, 0 falhas de asserção, **3 erros por IllegalArgumentException**, 0 ignorados, BUILD FAILURE e saída 1. Os três erros eram os casos válidos que a normalização antiga não aceitava. Os negativos continuaram passando.
2. **GREEN:** substituímos a remoção apenas de espaço ASCII pela remoção de whitespace Unicode. Reexecutamos o mesmo comando: 9 testes, 0 falhas/erros/ignorados, BUILD SUCCESS e saída 0.
3. **REFACTOR:** reunimos hífen e whitespace em um Pattern estático nomeado `SEPARATORS`, com UNICODE_CHARACTER_CLASS. Isso explicita a política e evita compilar esse regex em cada chamada. Não foi medido ganho de desempenho.
4. Acrescentamos uma regressão HTTP/MySQL para edição com espaço inseparável, filtro com espaço estreito, checksum inválido com tabulação (400) e criação equivalente duplicada (409). Esse teste foi escrito depois da implementação e é apresentado como regressão de integração, sem atribuir a ele TDD.
5. **Verificação final:** `python3 /workspace/library-runtime/run.py test package`: **69 testes**, zero falhas, erros ou ignorados, BUILD SUCCESS. Os seis casos unitários e um teste de integração acrescentam sete execuções aos 62 anteriores.

Os logs completos locais estão em `/workspace/library-runtime/tdd-isbn-red.log`, `tdd-isbn-green.log` e `tdd-isbn-suite.log`; não fazem parte do checkout. Para reproduzir RED sem alterar main, aplique apenas as adições de IsbnTest à revisão base em uma cópia temporária e use as mesmas dependências. Não chamar os 62 testes anteriores de TDD.

## Relação com a estratégia de testes

Veja [estratégia e matriz](strategy.md). A separação de domínio tornou possível verificar a normalização sem Spring ou MySQL; a integração confirma que os caminhos HTTP realmente utilizam a mesma regra. A mudança não apresenta percentual de cobertura, teste de carga ou validação de frontend.
