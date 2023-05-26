# minimal runtime environment
From openjdk:11-jre-slim-buster

# copy jar from the first stage
COPY ./gateway/target/gateway-0.0.1.jar gateway-0.0.1.jar

EXPOSE 18888

CMD ["java", "-jar", "gateway-0.0.1.jar"]
