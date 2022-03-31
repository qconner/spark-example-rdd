FROM openjdk:8-jre-slim

# set a default value for N, when showing top-N hosts or urls
ENV TOP_N=10

# Set a value so the passive ftp can work (debugging)
ENV USER=nobody

WORKDIR /app

COPY target/scala-2.11/spark-example-rdd.jar /app/
COPY docker/run-app.sh /app/

# debugging ftp
RUN env|sort

CMD ["/bin/bash", "/app/run-app.sh"]
