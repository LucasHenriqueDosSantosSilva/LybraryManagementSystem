# Backend — primeiro incremento da baseline

Java 21, Spring Boot 3.5.7, Maven Wrapper 3.9.11, Spring Web/Data JPA, Bean Validation, MySQL e Flyway. Versões estão fixadas; futuras atualizações serão explícitas. Spring Boot fornece as versões compatíveis das dependências gerenciadas.

## Implementado

CRUD de categorias, lista paginada (20 por padrão, máximo 100), validação de nome, unicidade conforme collation MySQL e respostas de erro ProblemDetail. Flyway cria apenas `categories`; Hibernate valida o schema. As demais entidades e circulação ainda não existem.

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | /api/categories | 201 e Location |
| GET | /api/categories?page=0&size=20 | 200, content e metadados de page |
| GET | /api/categories/{id} | 200 ou 404 |
| PUT | /api/categories/{id} | 200, 400, 404 ou 409 |
| DELETE | /api/categories/{id} | 204 ou 404 |

POST/PUT recebem `{"name":"História"}`. Nomes são aparados; espaços não formam um nome válido; máximo de 100 caracteres. `Ficção` e `ficcao` conflitam pela collation `utf8mb4_0900_ai_ci`. Nesta primeira migration não existem livros nem FKs de categorias; a proteção contra exclusão de categoria vinculada será acrescentada com o catálogo.

## Executar em outra máquina

Pré-requisitos: JDK 21 completo (não apenas JRE), Python não é necessário para a aplicação, e MySQL 8.4 disponível. O usuário do banco precisa de permissões para as migrations em um schema dedicado. Maven Wrapper baixa uma distribuição verificada por SHA-256; acesso ao Maven Central é necessário na primeira execução.

Configure em seu terminal `DB_URL`, `DB_USER` e `DB_PASSWORD`, usando sua própria instância e senha. Exemplo não secreto de URL: `jdbc:mysql://127.0.0.1:3306/library`. Não coloque valores de senha em commits, histórico de comandos ou exemplos. No Windows use `mvnw.cmd` em vez de `./mvnw`.

A partir de `backend/`:

```bash
./mvnw spring-boot:run
```

Por padrão o servidor aceita conexões apenas em 127.0.0.1, porta 8080. `SERVER_ADDRESS` e `SERVER_PORT` permitem configurar isso; esta baseline sem autenticação é para desenvolvimento local. Não disponibilizar escrita pública nesta etapa.

Para testes de integração, configure **DB_URL para um schema MySQL de testes separado**, com as mesmas credenciais apropriadas, e execute:

```bash
./mvnw test
./mvnw package
```

A suíte contém 1 teste unitário com Mockito e 6 testes Spring Boot/MockMvc contra MySQL. Usa transações com rollback para dados de teste, mas Flyway cria schema e histórico de migrations; nunca apontar os testes para um banco de produção. Os testes ainda não demonstram TDD: foram escritos neste incremento, sem histórico RED/GREEN registrado.

## Ambiente de nuvem atual

JDK em `/workspace/tools/java21`; dependências e configuração de proxy do Maven ficam fora do checkout. Credenciais locais são geradas aleatoriamente e armazenadas em arquivo de permissão restrita fora do repositório. MySQL roda no container `library-mysql`, exposto somente em loopback, com volume nomeado. `library` é o schema de desenvolvimento; `library_test`, o de testes.

Comandos locais preparados e verificados:

```bash
bash /workspace/library-runtime/setup.sh
python3 /workspace/library-runtime/run.py test package
python3 /workspace/library-runtime/run.py spring-boot:run
```

O helper é específico desta máquina e não substitui os pré-requisitos de outra máquina. Instalação e instruções de início estão também no rascunho de configuração da nuvem; processos não devem ser presumidos como restaurados em novas tarefas. Não há Compose do projeto ou frontend ainda.

## Organização didática

Pacotes globais `controller`, `service`, `repository`, `entity` e `dto` deixam a funcionalidade dispersa. `CategoryService` repete busca e mapeamento nas operações e depende de HTTP por `ResponseStatusException`. São candidatos plausíveis para análise posterior, não justificativa para refatorar antes de completar a baseline. Integridade, validação e testes são preservados desde o início.
