FROM openjdk:8-jre-slim
# set a default value for N, when showing top-N hosts or urls
ENV TOP_N=100
# set a value so the ftp can work
ENV USER=nobody
WORKDIR /app
COPY target/scala-2.11/spark-example-rdd.jar /app/
COPY docker/run-app.sh /app/
RUN env|sort
CMD ["/bin/bash", "/app/run-app.sh"]
