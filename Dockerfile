FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM tomcat:9.0.96-jdk17-temurin
ENV TZ=Asia/Ho_Chi_Minh
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/library-management.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
