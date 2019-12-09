FROM openjdk:11.0.5-jre-stretch

ADD ["sources.list", "/etc/apt/"]

RUN apt update && apt install -y

ADD ["target/ip-test-1-jar-with-dependencies.jar", "/"]

CMD java -jar ip-test-1-jar-with-dependencies.jar