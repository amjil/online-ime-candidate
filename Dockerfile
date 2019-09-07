FROM openjdk:8-alpine

COPY target/uberjar/online-ime-candidate.jar /online-ime-candidate/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/online-ime-candidate/app.jar"]
