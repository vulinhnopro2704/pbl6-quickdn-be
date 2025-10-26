# Sử dụng JDK 21 (phiên bản ổn định)
FROM openjdk:21-jdk-slim-bookworm
WORKDIR /app

# Cài curl cho health check (tùy chọn)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Tạo user không phải root
RUN useradd --create-home --uid 1000 appuser

# Copy JAR từ build Gradle
# Với Spring Boot + Gradle, file jar nằm trong: build/libs/
COPY --chown=appuser:appuser build/libs/*.jar app.jar

# Tối ưu JVM cho môi trường container
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"
USER appuser

# Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
