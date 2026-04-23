FROM debian:stable-slim
LABEL authors="Kvochkin AY"
LABEL description="Docker image for target indicators application"
LABEL version="0.0.1"

WORKDIR /app
# 2. НАСТРАИВАЕМ РУССКИЙ ЯЗЫК В КОНТЕЙНЕРЕ
# Обновляем списки пакетов, устанавливаем локали, генерируем русскую кодировку и чистим кэш
RUN apt-get update && \
    apt-get install -y locales && \
    sed -i '/ru_RU.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen ru_RU.UTF-8
# Задаем переменные окружения для Debian, чтобы он говорил по-русски
ENV LANG ru_RU.UTF-8
ENV LANGUAGE ru_RU:ru
ENV LC_ALL ru_RU.UTF-8
# 3. Копируем и распаковываем Java
COPY dependencies/openjdk-17.0.2_linux-x64_bin.tar.gz /tmp/openjdk.tar.gz
RUN mkdir -p /opt/java && \
    tar -xzf /tmp/openjdk.tar.gz -C /opt/java --strip-components=1 && \
    rm /tmp/openjdk.tar.gz
# 4. переменные окружения Java
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY setting.properties setting.properties
COPY target/target.indicators-0.0.1.jar target.indicators.jar
EXPOSE 8087
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dconsole.encoding=UTF-8", "-jar", "target.indicators.jar"]
