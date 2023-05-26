# minimal runtime environment
From openjdk:11-jre-slim-buster

# copy jar from the first stage
COPY ./auth-web/target/auth-web-0.0.1.jar auth-web-0.0.1.jar

EXPOSE 8811

CMD ["java", "-jar", "auth-web-0.0.1.jar"]
