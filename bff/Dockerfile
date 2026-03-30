FROM eclipse-temurin:22-jdk AS buildstage 
 
RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src

RUN mvn clean package

FROM eclipse-temurin:22-jdk 

COPY --from=buildstage /app/target/*.jar /app/bff.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar","/app/bff.jar" ]