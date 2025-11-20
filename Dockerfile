FROM gradle:8.3-jdk17 AS builder

WORKDIR /app

COPY . .

RUN ls -al gradle/wrapper

RUN chmod +x gradlew
RUN ./gradlew bootJar
RUN ls -l build/libs

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

EXPOSE 8080

COPY --from=builder app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]