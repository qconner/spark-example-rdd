import sbt._

trait Libraries {
  lazy val scalaTest          = "org.scalatest"                  %% "scalatest"              % "3.1.1"    % Test

  // Apache Spark 2.4.5
  // TODO: figure out 3.2.x Main class in assembly issue
  lazy val sparkCore          = "org.apache.spark"               %% "spark-core"             % "2.4.5"

  // Hadoop 2.4.5
  // TODO: figure out 3.2.x Main class in assembly issue
  lazy val hadoopClient       = "org.apache.hadoop"              %  "hadoop-client"          % "2.6.5"   // searched 2.7.7 - 2.10.1
}

object Dependencies extends Libraries {
  lazy val projectDependencies = Seq(
    scalaTest,
    sparkCore,
    hadoopClient
  )
}
