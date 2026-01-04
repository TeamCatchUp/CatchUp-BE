FROM gradle:8.3-jdk17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY . .

RUN ls -al gradle/wrapper

RUN chmod +x gradlew
RUN ./gradlew bootJar -x test --no-daemon
RUN ls -l build/libs

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

ENV TZ=Asia/Seoul

EXPOSE 8080

COPY --from=builder app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]