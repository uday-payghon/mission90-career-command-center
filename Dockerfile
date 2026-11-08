FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S mission && adduser -S mission -G mission && mkdir -p /app/data && chown -R mission:mission /app/data
COPY --from=build /app/target/job-mission-1.0.0.jar app.jar
USER mission
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=144m -XX:ActiveProcessorCount=2"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
