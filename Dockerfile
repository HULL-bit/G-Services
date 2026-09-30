# syntax=docker/dockerfile:1

# ============================================================================
#  G-SERVICES — image applicative (build multi-étapes)
# ============================================================================

# ---- Étape 1 : build Maven (JDK 21) --------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances : on copie d'abord le POM seul.
# Le cache BuildKit sur ~/.m2 accélère fortement les rebuilds.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline -Dmaven.wagon.http.retryHandler.count=3 || true

# Puis le code source et le packaging.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests

# ---- Étape 2 : runtime (JRE 21, utilisateur non-root) -------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl : utilisé par le healthcheck docker-compose.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system gservices \
 && useradd  --system --gid gservices --home /app gservices

COPY --from=build /app/target/gservices.war app.war
RUN chown -R gservices:gservices /app
USER gservices

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.war"]
