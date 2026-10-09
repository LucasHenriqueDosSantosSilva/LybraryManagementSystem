# Maven 3.9.11 / Java 21. Exact manifests are fixed for reproducible base images.
FROM maven@sha256:6fdc855a6ed81d288ca7ca37ac6ff5e9308b612485c0801d70b25a858c83d237 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=secret,id=maven_settings \
    --mount=type=secret,id=java_cacerts \
    set -eu; \
    if [ -f /run/secrets/java_cacerts ]; then export MAVEN_OPTS="-Djavax.net.ssl.trustStore=/run/secrets/java_cacerts"; fi; \
    if [ -f /run/secrets/maven_settings ]; then mvn -s /run/secrets/maven_settings -B -DskipTests package; else mvn -B -DskipTests package; fi
FROM eclipse-temurin@sha256:cff19e6215689161eb6162c11b86b0c60ddf802164f2eaf48d570f8fb79a36c5
WORKDIR /app
COPY --from=build --chown=10001:10001 /app/target/library-0.1.0-SNAPSHOT.jar /app/library.jar
USER 10001:10001
ENV SERVER_ADDRESS=0.0.0.0 SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/library.jar"]
