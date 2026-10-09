# Frontend — incrementos 1–2

React 19, TypeScript e Vite; Node 24 e npm 11 verificados neste ambiente. Dependências fixadas em package-lock.json.

```bash
cd frontend
npm ci --cache /workspace/tools/npm-cache
npm test
npm run build
npm run dev
```

O caminho de cache acima atende o ambiente de nuvem; localmente pode ser omitido. O backend deve rodar na porta 8080. O servidor Vite usa loopback e encaminha `/api` para o backend, permitindo mesma origem no desenvolvimento sem mudar CORS. Não há configuração de produção ou autenticação nesta entrega. O build estático exige que o servidor final encaminhe `/api` e forneça index.html para navegação do React Router; `vite preview` sozinho não integra o backend.

Rotas implementadas: `/` (dashboard), `/categories` e `/authors` (listagem paginada, criação, edição e exclusão confirmada), `/books` (busca combinada, cadastro, edição e exclusão confirmada com categoria e múltiplos autores). Erros e vazios são diferenciados; sucesso só aparece após confirmação da API. Erros de campos são associados ao input. Requisições de leitura são canceladas ao sair da tela; mutações não têm repetição automática. A API continua sendo autoridade de integridade e unicidade.

Tema escuro com CSS, layout adaptável, labels, foco visível, link de salto e ações com nomes acessíveis. Confirmação de exclusão usa bloco na página, sem modal ou armadilha de foco. Não houve inspeção em navegador gráfico nem auditoria formal de acessibilidade nesta tarefa.

Validação: 12 testes Vitest/Testing Library (falha/retry, submissão, conflito sem perder dados, confirmação de exclusão e cliente HTTP), typecheck e build. Os testes de componentes usam substitutos da API; requisições HTTP reais pelo proxy verificaram dashboard, categorias e criação/consulta/exclusão de uma fixture própria, removida ao terminar. Isso não equivale a teste completo de navegador.

Próximos incrementos: leitores/exemplares e circulação. Veja [incremento do catálogo](../docs/frontend/increment-2.md). O dashboard consulta dados reais dessas funcionalidades já disponíveis no backend.
