FROM openjdk:8-alpine
EXPOSE 8080
COPY target/reactor-kafka-example-consumer-app.jar app.jar
ENTRYPOINT ["java","-jar","-noverify","app.jar","--server.port=8080"]