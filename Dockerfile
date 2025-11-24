FROM eclipse-temurin:17-jdk

WORKDIR /eod

COPY build/libs/eod.jar eod.jar

ENTRYPOINT ["java", "-jar", "eod.jar"]