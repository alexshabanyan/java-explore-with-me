FROM amazoncorretto:11
COPY server/target/*.jar stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]