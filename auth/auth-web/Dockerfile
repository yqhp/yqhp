FROM openjdk:11-jre-slim-buster

COPY ./target/*.jar auth.jar

CMD ["java", "-jar", "auth.jar"]