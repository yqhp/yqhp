# minimal runtime environment
From openjdk:11-jre-slim-buster

# copy jar from the first stage
COPY ./file-web/target/file-web-0.0.1.jar file-web-0.0.1.jar

EXPOSE 8866

CMD ["java", "-jar", "file-web-0.0.1.jar"]
