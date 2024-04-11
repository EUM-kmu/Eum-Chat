#FROM --platform=linux/amd64 openjdk:17-jdk
FROM openjdk:17-jdk

LABEL maintainer="ytjdud01@kookmin.ac.kr"

VOLUME /eum-chat

EXPOSE 8080

ARG JAR_FILE=build/libs/EUM-CHAT-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} chat-springboot.jar

ENTRYPOINT ["java", "-jar","/chat-springboot.jar"]