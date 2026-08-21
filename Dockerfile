FROM --platform=$BUILDPLATFORM gradle:9.1.0-jdk21-alpine as builder
COPY . /app
WORKDIR /app
RUN gradle build
RUN cp /app/build/libs/app.jar /app.jar

FROM eclipse-temurin:25.0.4_7-jre-alpine
#RUN echo "1.1" > version
COPY --from=builder /app.jar /opt/app/BOTC-Helper.jar
CMD java -jar /opt/app/BOTC-Helper.jar
