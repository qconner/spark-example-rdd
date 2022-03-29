import sbt._

trait Libraries {
  lazy val scalaTest          = "org.scalatest"                  %% "scalatest"              % "3.1.1"    % Test

  // Apache Spark 3.2.x
  lazy val sparkCore          = "org.apache.spark"               %% "spark-core"             % "3.2.1"

  // Hadoop 3.2.x
  lazy val hadoopClient       = "org.apache.hadoop"              %  "hadoop-client-api"      % "3.2.1"
}

object Dependencies extends Libraries {
  lazy val projectDependencies = Seq(
    scalaTest,
    sparkCore,
    hadoopClient
  )
}
