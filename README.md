# spark-example-rdd
Apache Spark RDD example

**Description**:  This is an example batch application of the Apache Spark RDD API, using the Scala language,
whereby a webserver log file is read and some web site usage analytics are computed.
The top N URLs for each day are identified.  The top N client IP addresses are also determined.

This Scala project uses the lower-level RDD API and traditional Map Reduce operations in a batch processing mode.
This example does not cover example usage of Spark Streaming (where a Stream of RDDs is read) 
nor Spark Structured Streaming, where SQL statements and DataFrames are featured.

I started to use Spark 3.x with Hadoop 3.x but sbt-assembly wouldn't create a proper runnable jar.  I'll likely revisit in the future.

![](https://www.getdeveloper.net/wp-content/uploads/2018/01/9A41ED48-70B6-4A73-B20F-3FD39883411A-e1515486109106.jpeg)

## Dependencies

  - **JRE 1.8**: Important!  Java 1.8 is required for running sbt or the application jar, even if just building the container.
  - **Apache Spark 2.4.5**: Distributed analytics and data movement tool for Scala.
  - **Hadoop 2.7.7**:  Seems to work with Spark and have less merging than 3.x for the assembly.

Docker desktop (or compatible) can be used for the container-based method of execution.

sbt 1.3.5 or higher is recommended for building the jar or doing local development.

## Installation

First, check out the source code to a Linux or MacOS environment using "git clone https://github.com/qconner/spark-example-rdd".

There is a script named DOCKER_BUILD in the top level directory.  Run this to build the sbt assembly (a fat jar) and to create the Docker image locally.

```
$ ./DOCKER_BUILD
```

If this doesn't work and you have sbt installed, try:

```
$ sbt run
```

or

```
$ sbt clean test assembly
$ java -jar target/scala-2.11/spark-example-rdd.jar
```

## Automated Test Execution

Some automated ScalaTest specs were created, to help with the Parser developement.  A mock Spark RDD implementation would be a
great addition, with the ability to interrogate the resulting RDD.

## Source Code Test Coverage

HTML source code automated test coverage is provided.  Open the `target/scala-2.11/scoverage-report/index.html` file after
running the following:
```
sbt clean coverage test coverageReport
```

## Configuration

The setting for N, the number of URLs or Hosts to show for each day can be specified as the "TOP_N" parameter when running the docker container.
If you are running the jar file locally from a "sbt assembly", this parameter can be passed as the only argument.

For example, to see the top 100 locally, run `java -jar target/scala-2.11/spark-example-rdd.jar 100`

If you are executing the docker container, add the parameter (15 in this example) with:

```
$ docker run -e TOP_N=15 qconner/spark-example-rdd:latest
```

If you have used DOCKER_BUILD above and want to run your local copy of the container image, try:
```
$ docker run -e TOP_N=15 spark-example-rdd:latest
```

## Known issues

Retrieving the datafile from github needs to be implemented.  A "does HTTPS resource exist" function and a gzip decompression HTTP client is needed.

----

Copyright (C) 2022 Quentin Alan Conner - All Rights Reserved

You may not use, distribute or modify this code.  All rights will remain with the author.  Contact the author with any permission or licensing requests:

Quentin Conner

13100 Delphinus Walk

Austin, TX  78732


