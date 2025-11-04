#build jar
FROM eclipse-temurin:21 AS builder
WORKDIR /app

#get the necessary mvn scripts and pom.xml files into the correct directories for cacheing dependencies first
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

#copies the pom.xml of these directories into the integration and learnd_main directories
#   these directories are created by these commands automatically since they do not exist yet
COPY integration/pom.xml integration
COPY learnd_main/pom.xml learnd_main

#install the dependencies for cacheing
RUN ./mvnw dependency:go-offline

#copy the remaining source code into directory
COPY . .

#package dependencies and source code into .jar
RUN ./mvnw package

#exclude build only material in final image
FROM eclipse-temurin:21-alpine

WORKDIR /app

EXPOSE 8080

COPY --from=builder /app/learnd_main/target/*.jar app.jar

CMD ["java","-jar","/app/app.jar"]

