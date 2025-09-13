FROM openjdk:17

WORKDIR /eod

COPY build/libs/eod.jar eod.jar

ENTRYPOINT ["java", "-jar", "eod.jar"]