FROM openjdk:26-ea-22-slim-bookworm
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl tzdata && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set timezone to Vietnam
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN useradd --create-home --uid 1000 appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app

COPY --chown=appuser:appuser build/libs/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Ho_Chi_Minh"
USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
