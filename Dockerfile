FROM openjdk:11.0.5-jre-stretch

ADD ["target/ip-test-1.jar", "/"]

RUN ln -s /usr/local/openjdk-11/bin/java /usr/local/bin/java

CMD ["java -jar ip-test-1.jar"]