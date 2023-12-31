FROM openjdk:17
ARG IDLE_PROFILE
ARG JAR_FILE=build/libs/*.jar
ENV ENV_IDLE_PROFILE=$IDLE_PROFILE
COPY ${JAR_FILE} app.jar
COPY /home/ubuntu/config/firebase-service-account.json /app/firebase-service-account.json
RUN echo $ENV_IDLE_PROFILE
ENTRYPOINT ["java", "-Dspring.profiles.active=${ENV_IDLE_PROFILE}", "-jar","/app.jar"]