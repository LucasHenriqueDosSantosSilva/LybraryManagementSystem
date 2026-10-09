# Roteiro de demonstração local — etapa 16

Roteiro sugerido de 10–12 minutos, com dados fictícios. Use a execução [Compose](../../docker/README.md), confirme os três healthchecks e abra a aplicação local. Não iniciar demonstração se a API estiver indisponível. Não mostrar arquivos .env, senhas ou configuração resolvida na projeção.

## Preparação

Escolha nomes e códigos próprios para a sessão. Consulte o catálogo para evitar ISBN repetido; exemplos válidos: 9780306406157 e 9780804429573. Se ambos já existirem, reutilize a edição e seus autores/categoria, criando somente exemplar e leitor próprios. Não alterar uma edição com histórico para acomodar a apresentação. O prazo padrão é 14 dias; um empréstimo feito hoje não fica atrasado na hora.

## Sequência na interface

1. **Contexto e dashboard (1 min):** explique o ator bibliotecário, títulos versus exemplares e disponibilidade derivada. Apresente as telas e o tema responsivo.
2. **Catálogo (2 min):** crie categoria/autor próprios se necessários; cadastre edição com ISBN válido, ano, categoria e autor. Busque por título/autor/ISBN; mostre que o ISBN é normalizado. Se quiser demonstrar duplicidade, tente cadastrar a mesma edição e observe 409 sem perder o formulário.
3. **Acervo e leitor (2 min):** cadastre exemplar com patrimônio exclusivo; crie leitor com matrícula exclusiva. Mostre situação ativa e disponibilidade do exemplar.
4. **Circulação (2 min):** selecione leitor/exemplar e registre empréstimo. Mostre data/vencimento retornados pelo servidor, histórico e desaparecimento do exemplar entre as opções disponíveis. A API também protege empréstimos concorrentes; cite o teste, sem alegar que um único clique demonstra concorrência.
5. **Devolução (2 min):** desative o leitor, volte à circulação e confirme devolução. Mostre que inatividade impede novo empréstimo, mas permite retornar o pendente. Filtre histórico pelo leitor e RETURNED, e confira disponibilidade restaurada em exemplares/dashboard.
6. **Integridade e evolução (1–3 min):** tente excluir leitor ou exemplar com histórico e mostre conflito. Explique que o histórico fica preservado. Abra a comparação da baseline e o registro do ciclo TDD para apoiar a discussão acadêmica.

Os filtros ACTIVE incluem todos os não devolvidos, inclusive atrasados; OVERDUE restringe os que passaram do vencimento. Para atraso, use evidência dos testes com Clock controlado, sem mudar relógio de produção nem adulterar histórico de demonstração.

## Depois da apresentação

Preserve empréstimo e histórico da sessão. Remova pela interface somente registros sem vínculos quando apropriado. A limpeza administrativa do E2E vale apenas para suas próprias fixtures em schema de testes; não copiar esse procedimento para apagar histórico normal. docker compose down para serviços preserva volume.

## Alternativa diante de falhas

Se houver falha de API/rede, mostre o estado de erro e retome após restaurar serviços; não narrar sucesso inexistente. Os [screenshots](../testing/browser-integration.md), as migrations e as evidências de testes permitem explicar o projeto quando a demonstração ao vivo estiver indisponível. Não representam um serviço público nem substituem a execução quando ela é exigida.
