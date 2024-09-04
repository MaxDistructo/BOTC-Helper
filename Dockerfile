FROM --platform=$BUILDPLATFORM gradle:8.10.0-jdk21 as builder
COPY . /app
WORKDIR /app
RUN gradle jar
RUN cp /app/build/libs/app.jar /app.jar

FROM ghcr.io/graalvm/jdk-community:21
#RUN echo "1.1" > version
COPY --from=builder /app.jar /opt/app/BOTC-Helper.jar
CMD java -jar /opt/app/BOTC-Helper.jar
