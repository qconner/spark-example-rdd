import sbt._

trait Libraries {
  lazy val scalaTest          = "org.scalatest"                  %% "scalatest"              % "3.1.1"    % Test

  // typesafe config
  lazy val typesafeConfig     = "com.typesafe"                    % "config"                 % "1.4.2"

  // Apache Spark
  lazy val sparkCore          = "org.apache.spark"               %% "spark-core"             % "3.2.1" // "2.4.8"

  // why do I need this for core Spark?
  lazy val hadoopCommon       = "org.apache.hadoop"              %  "hadoop-common"          % "3.3.1"  % "provided"
  lazy val hadoopClient       = "org.apache.hadoop"              %  "hadoop-client"          % "3.3.1"
  lazy val hadoopAWS          = "org.apache.hadoop"              %  "hadoop-aws"             % "3.3.1"
  // /home/quentin/src/spark-example-rdd/target/bg-jobs/sbt_43efb294/target/6a8adcaa/81ecd14d/hadoop-client-api-3.3.1.jar
}

object Dependencies extends Libraries {
  lazy val allSpecified = Seq(
    scalaTest,
    sparkCore,
    hadoopClient
  )
}
