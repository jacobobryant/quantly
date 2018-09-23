FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/quantly-0.0.1-SNAPSHOT-standalone.jar /quantly/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/quantly/app.jar"]
