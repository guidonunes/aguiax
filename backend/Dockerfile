FROM eclipse-temurin:21-jdk-jammy AS build

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system aguiax \
    && useradd --system --gid aguiax --home-dir /app --shell /usr/sbin/nologin aguiax

WORKDIR /app

COPY --from=build --chown=aguiax:aguiax \
    /workspace/target/aguiax-0.0.1-SNAPSHOT.jar \
    /app/aguiax.jar

USER aguiax

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/aguiax.jar"]
