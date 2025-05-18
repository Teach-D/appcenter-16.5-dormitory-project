FROM openjdk:17-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# JAR 복사
COPY build/libs/*.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]