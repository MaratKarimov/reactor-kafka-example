FROM adoptopenjdk/openjdk17:alpine as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk/openjdk17:alpine
EXPOSE 8080
RUN apk add --no-cache tzdata bind-tools iputils nmap curl busybox-extras && \
 addgroup --gid 10001 app && \
 adduser -h app -s /bin/sh -G app -D -u 10001 app
ENV TZ Europe/Moscow
USER 10001
COPY --from=builder dependencies/ ./
RUN true
COPY --from=builder snapshot-dependencies/ ./
RUN true
COPY --from=builder spring-boot-loader/ ./
RUN true
COPY --from=builder application/ ./
RUN true
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]