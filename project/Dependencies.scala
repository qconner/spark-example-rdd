import sbt._

trait Libraries {
  lazy val scalaTest          = "org.scalatest"                  %% "scalatest"              % "3.1.1"    % Test

  // Apache Spark
  // TODO: figure out 3.2.x Main class in assembly issue
  lazy val sparkCore          = "org.apache.spark"               %% "spark-core"             % "2.4.5"

  // Hadoop
  // TODO: figure out 3.2.x Main class in assembly issue
  lazy val hadoopClient       = "org.apache.hadoop"              %  "hadoop-client"          % "2.9.2"
  lazy val hadoopAWS          = "org.apache.hadoop"              %  "hadoop-client"          % "2.9.2"
}

object Dependencies extends Libraries {
  lazy val projectDependencies = Seq(
    scalaTest,
    sparkCore,
    hadoopClient,
    hadoopAWS
  )
}
