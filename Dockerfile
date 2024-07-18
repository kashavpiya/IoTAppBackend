
FROM openjdk:17
ADD target/spring-boot-security-jwt-0.0.1-SNAPSHOT.jar ecs.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Djavax.net.ssl.trustStore=/Users/yuzhang/Library/Java/JavaVirtualMachines/azul-11.0.18/Contents/Home/lib/security/cacerts" ,"ecs.jar"]
