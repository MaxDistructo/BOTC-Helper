FROM --platform=$BUILDPLATFORM gradle:8.12.1-jdk21-alpine as builder
COPY . /app
WORKDIR /app
RUN gradle build
RUN cp /app/build/libs/app.jar /app.jar

FROM eclipse-temurin:21.0.5_11-jre-alpine
#RUN echo "1.1" > version
COPY --from=builder /app.jar /opt/app/BOTC-Helper.jar
CMD java -jar /opt/app/BOTC-Helper.jar
