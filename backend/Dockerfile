FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system fintrack && useradd --system --gid fintrack fintrack
COPY --from=build /app/target/fintrack-1.0.0.jar app.jar
COPY aiven-ca.pem /app/aiven-ca.pem
RUN keytool -importcert -noprompt -trustcacerts -alias fintrack-aiven -file /app/aiven-ca.pem -cacerts -storepass changeit
COPY schema-cloud.sql /app/schema-cloud.sql
ENV SPRING_SQL_INIT_MODE=always
ENV SPRING_SQL_INIT_SCHEMA_LOCATIONS=file:/app/schema-cloud.sql
USER fintrack
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
