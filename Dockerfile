FROM debian:stable-slim
LABEL authors="Kvochkin AY"
LABEL description="Docker image for target indicators application"
LABEL version="0.0.1"
WORKDIR /app

COPY setting.properties setting.properties
COPY target/target.indicators-0.0.1.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
