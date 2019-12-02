FROM openjdk:11.0.5-jre-stretch

ADD ["target/ip-test-1.jar", "/"]

CMD ["/usr/local/openjdk-11/bin/java -jar ip-test-1.jar"]