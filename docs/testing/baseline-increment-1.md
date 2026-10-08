# Validação — primeiro incremento da baseline

Incremento executado em 8 de outubro de 2026: CRUD de categorias com Java 21, Maven 3.9.11, Spring Boot 3.5.7 e MySQL 8.4.11. Fonte: testes em backend/src/test e logs locais da execução. Nenhuma métrica de cobertura foi coletada.

## Resultados

- Maven Wrapper: distribuição ZIP verificada por SHA-256, após validação do artefato por SHA-512 publicado no Maven Central.
- `test package`: build e empacotamento passaram; 7 testes executados, 0 falhas, 0 erros, 0 ignorados.
- Flyway: V1 criou categories em schemas dedicados; instalação repetida preservou dados e não reaplicou a migration.
- Hibernate: validação do schema passou, com Open Session in View desabilitado.
- Smoke HTTP com servidor iniciado: criação 201, consulta 200, duplicidade 409, edição 200, exclusão 204, consulta posterior 404 e nome vazio 400. Registro temporário removido ao final.

Os 6 testes de integração cobrem criação com normalização, nome vazio, paginação inválida, inexistência, equivalência de nomes pela collation e edição/exclusão. O teste unitário verifica normalização antes de persistir. Integração usa MySQL real e MockMvc; o smoke usa HTTP real.

## Problemas de ambiente resolvidos

Sistema de pacotes sem escrita e ausência de sudo: JDK completo foi obtido de imagem Temurin verificada pelo Docker, em /workspace. Maven precisava de repositório local gravável, proxy da plataforma e truststore da máquina: configuração externa ajustada, sem desabilitar TLS. Um checksum inicialmente associado ao formato errado da distribuição foi corrigido usando o ZIP verificado pela fonte oficial; o wrapper depois passou.

## Limites

Sem testes de empréstimo, concorrência de circulação, frontend, autenticação ou Testcontainers; essas funcionalidades ainda não existem. Docker executou MySQL, mas o projeto ainda não tem Compose. Sem validação de implantação pública ou restauração em uma nova tarefa. Este incremento prepara desenvolvimento de categorias, não conclui a etapa 8 inteira.
