FROM openjdk:26-ea-21-jdk-slim-bookworm
WORKDIR /app

RUN useradd --create-home --uid 1000 appuser

COPY --chown=appuser:appuser target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Dspring.profiles.active=prod"
EXPOSE 8080

USER appuser
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
