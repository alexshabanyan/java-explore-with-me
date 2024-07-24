FROM amazoncorretto:11
COPY service-module/target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]