FROM openjdk:11.0.5-jre-stretch

RUN apt update && apt install -y nmap

ADD ["target/ip-test-1-jar-with-dependencies.jar", "/"]

CMD java -jar ip-test-1-jar-with-dependencies.jar