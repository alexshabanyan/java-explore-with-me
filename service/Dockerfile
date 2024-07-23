FROM amazoncorretto:11
COPY service/target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]