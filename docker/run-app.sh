#!/bin/bash
exec java -Dspark.hadoop.fs.ftp.data.connection.mode="PASSIVE_LOCAL_DATA_CONNECTION_MODE" -jar /app/spark-example-rdd.jar ${TOP_N:-10}
