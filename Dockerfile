FROM --platform=$BUILDPLATFORM gradle:8.9.0-jdk21 as builder
COPY . /app
WORKDIR /app
RUN gradle build
#RUN cp ./build/libs/platinum.jar .

FROM eclipse-temurin:21.0.4_7-jre-jammy
#RUN echo "1.1" > version
COPY --from=builder /app/build/libs/BOTC-Helper.jar /opt/app/BOTC-Helper.jar
RUN apt update
CMD java -jar /opt/app/BOTC-Helper.jar