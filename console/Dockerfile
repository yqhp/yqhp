# minimal runtime environment
From openjdk:11-jre-slim-buster

# copy jar from the first stage
COPY ./console-web/target/console-web-0.0.1.jar  console-web-0.0.1.jar

EXPOSE 8887

CMD ["java", "-jar", "console-web-0.0.1.jar"]
