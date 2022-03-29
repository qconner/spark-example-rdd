import Dependencies.projectDependencies

ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "org.example"
ThisBuild / organizationName := "Quentin Conner"

//
//  Copyright(c) 2022
//  Quentin Alan Conner
//
//  All Rights Reserved.  You may not copy, distribute or use this work
//  without prior written authorization.
//

lazy val assemblySettings = Seq(
  // set jar name
  //assembly / assemblyJarName := ((name) map { (n) => n + ".jar" }).value,

  // don't run tests
  //assembly / test := {},

  // main class
  //assembly / mainClass := Some("org.example.Main"),
/*
  assembly / packageOptions ~= { pos =>
    pos.filterNot { po =>
      po.isInstanceOf[Package.MainClass]
    }
  },
 */

  //
  // merge strategies (AKA dependency hell)
  // this and regex are why we are able to earn a living by typing
  //
  assembly / assemblyMergeStrategy := {
    // netty
    case PathList("io", "netty", xs @ _*)                            => MergeStrategy.first

    // keep first for a runnable jar
    case PathList("META-INF", xs @ _*) =>
      xs match {
        case "MANIFEST.MF" :: Nil =>
          println(s"META-INF / ${xs}")
          MergeStrategy.first
        //case "io.netty", ys @ @_*) =>
          //println(s"io.netty ${ys}")
          //MergeStrategy.concat
        case _  =>
          MergeStrategy.last
      }

    // jackson xml
    case "module-info.class"                                         => MergeStrategy.first

    // log4j/slf4j/commons logging
    case PathList("org", "apache", "commons", "logging", xs @ _*)    => MergeStrategy.first
    case "log4j.properties"                                          => MergeStrategy.concat

    // Spark unused (may I discard?)
    case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard //first

    // use default strategy for anything else
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val root = (project in file("."))
  .settings(
    name := "spark-example-rdd",
    libraryDependencies ++= projectDependencies,
    assemblySettings
  )

