# First stage: complete build environment
FROM maven:3.8.6-jdk-11 AS builder

# add pom.xml and source code
ADD ./ /

# package jar
RUN mvn clean package

# Second stage: minimal runtime environment
From openjdk:11-jre-slim

# copy jar from the first stage
COPY --from=builder auth-web/target/auth-sdk-gateway-0.0.1.jar auth-sdk-gateway-0.0.1.jar

EXPOSE 8811

CMD ["java", "-jar", "auth-sdk-gateway-0.0.1.jar"]
