FROM openjdk:8
ARG JAR_VERSION
RUN mkdir /java
RUN mkdir /output
COPY ./build/libs/mingpao-scrapping-${JAR_VERSION}-standalone.jar /java/mingpao.jar
COPY retrieve-today.sh /java
COPY retrieve2.sh /java
WORKDIR /java
ENTRYPOINT ["bash","./retrieve-today.sh"]