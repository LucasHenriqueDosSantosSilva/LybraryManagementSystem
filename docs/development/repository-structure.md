# Etapa 7 — Estrutura e convenções do repositório

A estrutura segue as decisões de [arquitetura](../architecture/architecture.md). Nesta etapa existem apenas documentação e arquivos de organização. Backend, frontend e banco ainda não foram implementados. Diretórios futuros serão criados com seus primeiros arquivos úteis.

## Estrutura atual

```text
LybraryManagementSystem/
├── .gitignore
├── CONTRIBUTING.md
├── README.md
└── docs/
    ├── requirements/
    │   ├── initial-analysis.md
    │   ├── mvp-scope.md
    │   └── use-cases.md
    ├── database/
    │   ├── initial-model.md
    │   └── normalization.md
    ├── architecture/
    │   └── architecture.md
    ├── development/
    │   └── repository-structure.md
    └── uml/
        └── use-cases.puml
```

## Estrutura prevista, ainda não criada

| Caminho | Quando criar e responsabilidade |
| --- | --- |
| backend/pom.xml | Primeiro incremento Java; build e versões de dependências |
| backend/mvnw e backend/.mvn/ | Wrapper Maven reproduzível, com versão fixada e distribuição verificada |
| backend/src/main/java/com/lucashenrique/library/ | Aplicação Spring e código da baseline |
| backend/src/main/resources/ | Configuração sem credenciais e migrations Flyway |
| backend/src/main/resources/db/migration/ | Fonte única das migrations |
| backend/src/test/java/com/lucashenrique/library/ | Testes de comportamento e integridade |
| frontend/package.json e package-lock.json | Primeiro incremento React; versões e lockfile versionados |
| frontend/src/ | Interface e integração HTTP |
| database/ | Consultas didáticas e dados demonstrativos, quando existirem; nunca cópia das migrations |
| docs/before-refactoring/ | Diagnóstico real da baseline validada |
| docs/after-refactoring/ | Comparação baseada em mudanças e medições executadas |
| docs/testing/ | Comandos e resultados reproduzíveis quando houver testes |
| docker-compose.yml | Serviços com configuração e readiness verificadas |
| .env.example | Nomes e exemplos não secretos das variáveis efetivamente necessárias |
| .github/workflows/ | CI somente após os comandos de build/teste funcionarem |

Não criar LICENSE sem escolha do autor. README de backend/frontend surgirá quando houver comandos específicos. Dockerfiles serão próximos de seus componentes; pasta docker/ só se tiver recursos adicionais úteis.

## Convenções

- Código e identificadores em inglês; documentação e mensagens da interface em português.
- Classes Java em PascalCase, métodos/campos em camelCase, pacotes minúsculos. Classe deve descrever responsabilidade concreta.
- Tabelas e colunas SQL em snake_case, conforme dicionário de dados.
- Componentes React em PascalCase; hooks começam com use; DTOs TypeScript acompanham o contrato HTTP.
- Versões de ferramenta e dependências fixadas em manifests/wrappers. Não usar latest em imagens para uma execução que se pretende reproduzível.
- Lockfiles versionados e instalação congelada quando suportada; não atualizar dependências incidentalmente.
- Não versionar target/, node_modules/, dist/, logs ou .env. Nunca colocar valores secretos em exemplos.

## Estado das ferramentas da máquina

Na inspeção desta etapa, `java -version` informou Java 21 e Node/npm estão disponíveis. `mvn` não foi encontrado, `javac` não foi encontrado no PATH e o cliente `mysql` também não. O comando `docker` existe, mas isso não comprova daemon acessível ou suporte a Testcontainers.

Esses resultados são observações da máquina atual, não requisitos de instalação concluídos nem garantia de disponibilidade em outra tarefa. No início do backend serão verificados JDK completo, wrapper Maven, acesso a artefatos, MySQL e capacidade de executar os testes. Nada foi instalado nesta etapa, porque ainda não há manifests ou comandos de build para validar.

## Baseline e evolução no Git

A organização atual da documentação não obriga a baseline Java a já ter o desenho final por domínio. Ela poderá começar com camadas globais, conforme a decisão didática, mantendo controllers limitados e operações seguras. A reorganização por domínio será uma mudança de estrutura observável, se demonstrar ganho.

Cada commit representa uma entrega real. A baseline receberá tag somente após execução e testes relevantes. Não duplicar o backend em pastas before/after: commits e tags preservam versões; docs/ registra evidência e análise. Não inventar histórico RED/GREEN ou métricas.

## Primeira entrega da etapa 8

Preparar backend mínimo com Java 21, Spring Boot e Maven Wrapper; configurar MySQL/Flyway por ambiente; executar uma primeira migration e cadastro pequeno antes de ampliar circulação. Fixar versões compatíveis ao implementar, instalar e verificar as ferramentas faltantes e documentar os comandos realmente executados.

A etapa 8 será incremental: cadastro de catálogo/leitores, exemplares e circulação, com validação funcional e integridade a cada incremento. O frontend vem na etapa prevista; não adicionar scaffold vazio só para parecer completo.

## O que entender e defender

Estrutura facilita encontrar arquivos; arquitetura define responsabilidades e dependências. Pastas por si só não provam coesão, SOLID ou modularidade. O histórico Git torna a evolução verificável, enquanto lockfiles e wrappers ajudam a reproduzir builds.

Em Análise e Projeto: organização, ocultamento de informação, manutenção e rastreabilidade. Em Banco de Dados: uma fonte de migrations impede divergência entre versões do schema.

Perguntas de professores: por que não duplicar migrations? Para haver uma ordem de evolução única. Por que não criar todas as pastas agora? Porque uma pasta vazia não demonstra funcionalidade. Como recuperar a versão inicial? Pelo commit/tag validado. Por que um Java runtime não basta? Compilar exige JDK completo e build configurado.
