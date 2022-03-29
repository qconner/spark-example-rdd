import Dependencies.allSpecified

ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "example"
ThisBuild / organizationName := "Quentin Conner"

lazy val assemblySettings = Seq(
  // jar name
  assembly / assemblyJarName := ((name) map { (n) => n + ".jar" }).value,

  // don't run test
  assembly / test := {},

  // merge strategies
  assembly / assemblyMergeStrategy := {
    case "log4j.properties"                                    => MergeStrategy.concat
    case PathList("io", "netty", xs @ _*)                      => MergeStrategy.last
    case PathList("javax", xs @ _*)                            => MergeStrategy.last
    case PathList("org", "slf4j", "impl", xs @ _*)             => MergeStrategy.last
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val root = (project in file("."))
  .settings(
    name := "clf-ip-url-top-N",
    libraryDependencies ++= allSpecified,    // see project/Dependencies.scala
    assemblySettings
  )

